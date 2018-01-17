package cn.redcdn.hvs.im.work;

import android.text.TextUtils;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.im.agent.AppP2PAgentManager;
import cn.redcdn.hvs.im.fileTask.FileTaskManager;
import cn.redcdn.hvs.im.meetingIm.ImConnectService;
import cn.redcdn.hvs.im.util.IMCommonUtil;
import cn.redcdn.hvs.im.work.MessageReceiveAsyncTask.PrivateMessage;
import cn.redcdn.log.CustomLog;

/**
 * Desc
 * Created by wangkai on 2017/2/28.
 */

public class MessageTextParse extends MessageBaseParse{

    private PrivateMessage msg = null;
    private int type = -1;
    private ExtInfo extInfo = null;

    public MessageTextParse(PrivateMessage msg){
        this.msg = msg;
        this.type = FileTaskManager.NOTICE_TYPE_TXT_SEND;
    }

    public ExtInfo getExtInfoAfterParse(){
        return extInfo;
    }

    public boolean parseMessage(){
        if(msg==null){
            return false;
        }
        String serVer = "";
        String txt = "";
        extInfo = convertExtInfo(msg.extendedInfo);
        if(extInfo!=null){
            serVer = extInfo.ver;
            txt = extInfo.text;
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
                CustomLog.d("MessageTextParse","91以前消息版本,不解析直接丢弃");
                break;
            case BizConstant.MSG_VERSION_INT_BASE_00:
            default:{
                //把多张图片拆解成多条记录插入到本地
                IMCommonUtil.setKeyValue(txt, msg.gid);
                String body = noticesDao.createRecTxtMsgBody(txt);
                String id =msg.msgId;//.replace("-", "");
                String uuid = noticesDao.createReceiveMsgNotice(id,
                        msg.sender,
                        msg.receivers,
                        body,
                        type,
                        getStringById(R.string.messsge_title_txt),
                        msg.extendedInfo,
                        msg.time,
                        msg.gid,extInfo.serverId);
                if(AppP2PAgentManager.getInstance().msgReceiveListener != null){
                    if(ImConnectService.relatedGroupId.equals(msg.gid)){
                        AppP2PAgentManager.getInstance().msgReceiveListener.onMsgReceive(uuid);
                    }
                }
                CustomLog.d("MessageTextParse","V1.00消息版本，插入本地文字 uuid="+uuid);
                if(!TextUtils.isEmpty(uuid)){
                    succ = true;
                }
            }
            break;
        }
        return succ;
    }

    public boolean parseDTMessage(){
        if(msg==null){
            return false;
        }
        String serVer = "";
        String txt = "";
        extInfo = convertExtInfo(msg.extendedInfo);
        if(extInfo!=null){
            serVer = extInfo.ver;
            txt = extInfo.text;
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
                CustomLog.d("MessageTextParse","91以前消息版本,不解析直接丢弃");
                break;
            case BizConstant.MSG_VERSION_INT_BASE_00:
            default:{
                //把多张图片拆解成多条记录插入到本地
                IMCommonUtil.setKeyValue(txt, msg.gid);
                String body = dtNoticesDao.createRecTxtMsgBody(txt);
                String id =msg.msgId;//.replace("-", "");
                String uuid = dtNoticesDao.createReceiveMsgNotice(id,
                    msg.sender,
                    msg.receivers,
                    body,
                    type,
                    getStringById(R.string.messsge_title_txt),
                    msg.extendedInfo,
                    msg.time,
                    msg.gid,extInfo.serverId);
                if(AppP2PAgentManager.getInstance().msgReceiveListener != null){
                    if(ImConnectService.relatedGroupId.equals(msg.gid)){
                        AppP2PAgentManager.getInstance().msgReceiveListener.onMsgReceive(uuid);
                    }
                }
                CustomLog.d("MessageTextParse","V1.00消息版本，插入本地文字 uuid="+uuid);
                if(!TextUtils.isEmpty(uuid)){
                    succ = true;
                }
            }
            break;
        }
        return succ;
    }

}
