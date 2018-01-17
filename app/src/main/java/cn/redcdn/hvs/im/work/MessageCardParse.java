package cn.redcdn.hvs.im.work;


import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

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

public class MessageCardParse extends MessageBaseParse {

    private PrivateMessage msg = null;
    private int type = -1;

    public MessageCardParse(PrivateMessage msg){
        this.msg = msg;
        this.type = FileTaskManager.NOTICE_TYPE_VCARD_SEND;
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
                CustomLog.d("MessageCardParse","91以前消息版本,不解析直接丢弃");
                // 20141120 sunjian 要求直接消费掉，不再下载；
                succ = true;
                break;
//				LogUtil.d("postcard消息版本 ver为空,暂且认为是1.00版本");
            case BizConstant.MSG_VERSION_INT_BASE_00:
            default:{
                List<String> romteUrlList = getFileUrl(msg.body);
                String romteUrl = "";
                if(romteUrlList!=null&&romteUrlList.size()>0){
                    romteUrl = romteUrlList.get(0);
                }
                // 解析title中的msgInfo信息，并保存用于转发时使用
                String title = msg.title;
                if(!TextUtils.isEmpty(title)){
                    try {
                        JSONObject object = new JSONObject(title);
                        title = object.optString("msgInfo");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if(TextUtils.isEmpty(title)){
                    title = getStringById(R.string.messsge_title_card);
                }
                //把记录插入到本地
                String body = noticesDao.createRecCardMsgBody(romteUrl,msg.extendedInfo);
                String id =msg.msgId;//.replace("-", "");
                String uuid = noticesDao.createReceiveMsgNotice(id,
                        msg.sender,
                        msg.receivers,
                        body,
                        type,
                        title,
                        msg.extendedInfo,
                        msg.time,
                        msg.gid,extInfo.serverId);
                if(AppP2PAgentManager.getInstance().msgReceiveListener != null){
                    if(ImConnectService.relatedGroupId.equals(msg.gid)){
                        AppP2PAgentManager.getInstance().msgReceiveListener.onMsgReceive(uuid);
                    }
                }
                CustomLog.d("MessageCardParse","V1.00消息版本，插入本地名片 uuid="+uuid);
                if(!TextUtils.isEmpty(uuid)){
                    succ = true;
                }
            }
            break;
        }
        return succ;
    }
}
