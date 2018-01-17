package cn.redcdn.hvs.im.work;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cn.redcdn.hvs.R;
import cn.redcdn.hvs.im.fileTask.FileTaskManager;
import cn.redcdn.hvs.im.util.IMCommonUtil;
import cn.redcdn.hvs.im.work.MessageReceiveAsyncTask.PrivateMessage;
import cn.redcdn.log.CustomLog;
/**
 * Desc
 * Created by wangkai on 2017/2/28.
 */

public class MessageFileParse extends MessageBaseParse {

    private PrivateMessage msg = null;
    private int type = -1;
    private ExtInfo extInfo = null;

    public MessageFileParse(PrivateMessage msg){
        this.msg = msg;
        this.type = FileTaskManager.NOTICE_TYPE_FILE;
    }

    public ExtInfo getExtInfoAfterParse(){
        return extInfo;
    }

    /**
     * 解析文件的消息，并保存到本地
     * @return  true:成功保存到本地（有一条成功，就认为是全部成功）
     */
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
                CustomLog.d("MessageFileParse","91以前消息版本,不解析直接丢弃");
                break;
            case BizConstant.MSG_VERSION_INT_BASE_00:
            default:{
                //把多张图片拆解成多条记录插入到本地
                IMCommonUtil.setKeyValue(txt, msg.gid);
                String body = noticesDao.createRecCollectionFileMsgBody(msg.extendedInfo,msg.body);

                String id =msg.msgId;//.replace("-", "");
                String uuid = noticesDao.createReceiveMsgNotice(id,
                        msg.sender,
                        msg.receivers,
                        body,
                        type,
                        getStringById(R.string.messsge_title_meeting),
                        msg.extendedInfo,
                        msg.time,
                        msg.gid,extInfo.serverId);
                CustomLog.d("MessageFileParse","V1.00消息版本，插入本地文字 uuid="+uuid);
                if(!TextUtils.isEmpty(uuid)){
                    succ = true;
                }
            }
            break;
        }
        return succ;
    }

}
