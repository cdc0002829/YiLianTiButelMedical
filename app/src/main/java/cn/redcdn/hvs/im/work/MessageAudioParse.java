package cn.redcdn.hvs.im.work;


import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.im.agent.AppP2PAgentManager;
import cn.redcdn.hvs.im.fileTask.FileTaskManager;
import cn.redcdn.hvs.im.meetingIm.ImConnectService;
import cn.redcdn.hvs.im.work.MessageReceiveAsyncTask.PrivateMessage;
import cn.redcdn.log.CustomLog;


/**
 * Desc
 * Created by wangkai on 2017/2/28.
 */

public class MessageAudioParse extends MessageBaseParse {
    private PrivateMessage msg = null;
    private int type = -1;

    public MessageAudioParse(PrivateMessage msg){
        this.msg = msg;
        this.type = FileTaskManager.NOTICE_TYPE_AUDIO_SEND;
    }

    public boolean parseMessage(){
        if(msg==null){
            return false;
        }
        ExtInfo extInfo = null;
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
            case BizConstant.MSG_VERSION_INT_BASE_X:
                CustomLog.d("MessageAudioParse","91以前消息版本,不解析直接丢弃");
                break;
            case BizConstant.MSG_VERSION_INT_BASE_00:
            default:{
                List<String> romteUrlList = getFileUrl(msg.body);
                List<String> thumbUrlList = null;
                if(extInfo!=null){
                    thumbUrlList = getFileUrl(extInfo.thumb);
                }
                //把拆解成多条记录插入到本地（本质上是一个）
                if (romteUrlList != null && romteUrlList.size() > 0) {
                    int length = romteUrlList.size();
                    for(int i = 0; i < length; i++){
                        CustomLog.d("MessageAudioParse","V1.00消息版本，插入本地声音 msgid="+msg.msgId
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
                            id ="";
                        }
                        String uuid = noticesDao.createReceiveMsgNotice(id,
                                msg.sender,
                                msg.receivers,
                                body,
                                type,
                                getStringById(R.string.messsge_title_audio),
                                msg.extendedInfo,
                                msg.time,
                                msg.gid,extInfo.serverId);
                        if(AppP2PAgentManager.getInstance().msgReceiveListener != null){
                            if(ImConnectService.relatedGroupId.equals(msg.gid)){
                                AppP2PAgentManager.getInstance().msgReceiveListener.onMsgReceive(uuid);
                            }
                        }
                        CustomLog.d("MessageAudioParse","V1.00消息版本，插入本地声音 uuid="+uuid);
                        if(!TextUtils.isEmpty(uuid)){
                            succ = true;
                        }
                        //TODO：异步线程，下载声音文件
                        if(!TextUtils.isEmpty(uuid)){
//							NetPhoneApplication.getFileTaskManager()
//								.downAudioFileThread(romteUrl, uuid);
                            MedicalApplication.getFileTaskManager().addTask(uuid, null);
                        }
                    }
                }
            }
            break;
        }
        return succ;
    }
}
