package cn.redcdn.hvs.im.work;

import android.text.TextUtils;

import java.util.List;

import cn.redcdn.hvs.R;
import cn.redcdn.hvs.im.IMConstant;
import cn.redcdn.hvs.im.agent.AppP2PAgentManager;
import cn.redcdn.hvs.im.fileTask.FileTaskManager;
import cn.redcdn.hvs.im.meetingIm.ImConnectService;
import cn.redcdn.hvs.im.work.MessageReceiveAsyncTask;
import cn.redcdn.hvs.im.work.MessageReceiveAsyncTask.PrivateMessage;
import cn.redcdn.log.CustomLog;

/**
 * Desc
 * Created by wangkai on 2017/2/28.
 */

public class MessageVedioParse extends MessageBaseParse {

    private final String TAG = "MessageVedioParse";

    private PrivateMessage msg = null;
    private int type = -1;
    private ExtInfo extInfo = null;

    public MessageVedioParse(PrivateMessage msg){
        this.msg = msg;
        this.type = FileTaskManager.NOTICE_TYPE_VEDIO_SEND;
    }

    public ExtInfo getExtInfoAfterParse(){
        return extInfo;
    }

    public boolean parseMessage(){
        if(msg==null){
            return false;
        }

        String serVer = "";
        extInfo = convertExtInfo(msg.extendedInfo);
        if(extInfo!=null){
            serVer = extInfo.ver;
        }
        int verInt = -1;
        if(TextUtils.isEmpty(serVer)){
            verInt = BizConstant.MSG_VERSION_INT_BASE_X;
        }else{
            verInt = BizConstant.getVersionInt(serVer);
        }

        //不认识的消息版本，判断是否兼容
        if(verInt==-1){
            if(!BizConstant.compareMsgVersion(serVer)){
                //不兼容，生成一条本地的txt文本，提醒用户升级
                insertIncompatibleTxtTip(msg,extInfo.serverId);
                return false;
            }else{
                //兼容，按照能识别的最高版本解析
                verInt = BizConstant.getVersionInt(BizConstant.MSG_VERSION);
            }
        }
        boolean succ = false;
        switch(verInt){
            case BizConstant.MSG_VERSION_INT_BASE_X:{
                List<String> romteUrlList = getFileUrl(msg.body);
                List<String> thumbUrlList = null;
                String txt = "";
                if(extInfo!=null){
                    thumbUrlList = getFileUrl(extInfo.thumb);
                    txt = extInfo.text;
                }
                //把多张图片拆解成多条记录插入到本地
                if (romteUrlList != null && romteUrlList.size() > 0) {
                    int length = romteUrlList.size();
                    for(int i = 0; i < length; i++){
                        CustomLog.d(TAG,"91之前消息版本，插入本地视频 msgid="+msg.msgId
                                +"|i="+i);
                        String romteUrl = romteUrlList.get(i);
                        String thumbUrl = "";
                        if(thumbUrlList!=null&&thumbUrlList.size()>i){
                            thumbUrl = thumbUrlList.get(i);
                        }
                        String body = noticesDao.createRecPAVMsgBody(romteUrl,
                                thumbUrl,
                                msg.extendedInfo,
                                type);
                        String id ="";
                        if(i==0){
                            id = msg.msgId;//msg.msgId.replace("-", "");
                        }else{
                            id = "";
                        }
                        String uuid = noticesDao.createReceiveMsgNotice(id,
                                msg.sender,
                                msg.receivers,
                                body,
                                type,
                                getStringById(R.string.messsge_title_vedio),
                                msg.extendedInfo,
                                msg.time,
                                msg.gid,extInfo.serverId);
                        CustomLog.d(TAG,"91之前消息版本，插入本地视频 uuid="+uuid);
                        if(!TextUtils.isEmpty(uuid)){
                            succ = true;
                        }
                    }
                }
                //把随消息带来的消息文本信息，当成一天txt消息插入本地
                if(!TextUtils.isEmpty(txt)){
                    String body = noticesDao.createRecTxtMsgBody(txt);
                    String uuid = noticesDao.createReceiveMsgNotice("",
                            msg.sender,
                            msg.receivers,
                            body,
                            FileTaskManager.NOTICE_TYPE_TXT_SEND,
                            getStringById(R.string.messsge_title_vedio_txt),
                            msg.extendedInfo,
                            msg.time,
                            msg.gid,extInfo.serverId);
                    CustomLog.d(TAG,"91之前消息版本，插入文字 uuid="+uuid);
                    if(!TextUtils.isEmpty(uuid)){
                        succ = true;
                    }
                }
            }
            break;
            case BizConstant.MSG_VERSION_INT_BASE_00:
            default:{
                List<String> romteUrlList = getFileUrl(msg.body);
                List<String> thumbUrlList = null;
                if(extInfo!=null){
                    thumbUrlList = getFileUrl(extInfo.thumb);
                }
                //把多张图片拆解成多条记录插入到本地
                if (romteUrlList != null && romteUrlList.size() > 0) {
                    int length = romteUrlList.size();
                    for(int i = 0; i < length; i++){
                        CustomLog.d(TAG,"V1.00消息版本，插入本地视频 msgid="+msg.msgId
                                +"|i="+i);
                        String romteUrl = romteUrlList.get(i);
                        String thumbUrl = "";
                        if(thumbUrlList!=null&&thumbUrlList.size()>i){
                            thumbUrl = thumbUrlList.get(i);
                        }
                        String body = noticesDao.createRecPAVMsgBody(romteUrl,
                                thumbUrl,
                                msg.extendedInfo,
                                type);
                        String id ="";
                        if(i==0){
                            id = msg.msgId;//.replace("-", "");
                        }else{
                            id = "";
                        }
                        String uuid = noticesDao.createReceiveMsgNotice(id,
                                msg.sender,
                                msg.receivers,
                                body,
                                type,
                                getStringById(R.string.messsge_title_vedio),
                                msg.extendedInfo,
                                msg.time,
                                msg.gid,extInfo.serverId);
                        if(AppP2PAgentManager.getInstance().msgReceiveListener != null){
                            if(ImConnectService.relatedGroupId.equals(msg.gid)){
                                AppP2PAgentManager.getInstance().msgReceiveListener.onMsgReceive(uuid);
                            }
                        }
                        CustomLog.d(TAG,"V1.00消息版本，插入本地视频 uuid="+uuid);
                        if(!TextUtils.isEmpty(uuid)){
                            succ = true;
                        }
                    }
                }
            }
            break;
        }
        return succ;
    }
}
