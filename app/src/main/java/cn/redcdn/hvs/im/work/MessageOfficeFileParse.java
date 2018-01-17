package cn.redcdn.hvs.im.work;

import android.text.TextUtils;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.im.agent.AppP2PAgentManager;
import cn.redcdn.hvs.im.fileTask.FileTaskManager;
import cn.redcdn.hvs.im.meetingIm.ImConnectService;
import cn.redcdn.hvs.im.work.MessageReceiveAsyncTask.PrivateMessage;
import cn.redcdn.log.CustomLog;
import java.util.List;

/**
 * 附件类型消息（PDF, WORD）解析并存入数据酷
 */

public class MessageOfficeFileParse extends MessageBaseParse {
    private static final String TAG = MessageOfficeFileParse.class.getSimpleName();

    private PrivateMessage msg = null;
    private int type = -1;


    public MessageOfficeFileParse(PrivateMessage msg) {
        CustomLog.i(TAG,"MessageOfficeFileParse()");
        this.msg = msg;
        this.type = FileTaskManager.NOTICE_TYPE_ATTACHMENT_FILE;
    }


    public boolean parseMessage() {
        CustomLog.i(TAG, "parseMessage()");

        if (msg == null) {
            return false;
        }
        ExtInfo extInfo = null;
        String serVer = "";
        extInfo = convertExtInfo(msg.extendedInfo);
        if (extInfo != null) {
            serVer = extInfo.ver;
        }
        int verInt = -1;
        if (TextUtils.isEmpty(serVer)) {
            verInt = BizConstant.MSG_VERSION_INT_BASE_X;
        } else {
            verInt = BizConstant.getVersionInt(serVer);
        }

        //不认识的消息版本，判断是否兼容
        if (verInt == -1) {
            if (!BizConstant.compareMsgVersion(serVer)) {
                //不兼容，生成一条本地的txt文本，提醒用户升级
                insertIncompatibleTxtTip(msg, extInfo.serverId);
                return false;
            } else {
                //兼容，按照能识别的最高版本解析
                verInt = BizConstant.getVersionInt(BizConstant.MSG_VERSION);
            }
        }
        boolean succ = false;
        switch (verInt) {
            case BizConstant.MSG_VERSION_INT_BASE_X:
                CustomLog.d(TAG, "91以前消息版本,不解析直接丢弃");
                break;
            case BizConstant.MSG_VERSION_INT_BASE_00:
            default: {
                List<String> romteUrlList = getFileUrl(msg.body);
                List<String> thumbUrlList = null;
                if (extInfo != null) {
                    thumbUrlList = getFileUrl(extInfo.thumb);
                }

                //把拆解成多条记录插入到本地（本质上是一个）
                if (romteUrlList != null && romteUrlList.size() > 0) {
                    for (int i = 0; i < romteUrlList.size(); i++) {
                        CustomLog.d(TAG, "V1.00消息版本，插入本地附件 msgid=" + msg.msgId
                            + "| i = " + i);
                        String romteUrl = romteUrlList.get(i);
                        String thumbUrl = "";
                        if (thumbUrlList != null && thumbUrlList.size() > i) {
                            thumbUrl = thumbUrlList.get(i);
                        }

                        // 创建 body 字段，存储 CDN 文件地址
                        String body = noticesDao.createRecPAVMsgBody(
                            romteUrl,
                            thumbUrl,
                            msg.extendedInfo,
                            type);

                        // 赋值 msgID
                        String id = "";
                        if (i == 0) {
                            id = msg.msgId;//msg.msgId.replace("-", "");
                        } else {
                            id = "";
                        }

                        // 将附件消息插入数据库
                        String uuid = noticesDao.createReceiveMsgNotice(id,
                            msg.sender,
                            msg.receivers,
                            body,
                            type,
                            MedicalApplication.shareInstance()
                                .getString(R.string.messsge_title_attachment),
                            msg.extendedInfo,
                            msg.time,
                            msg.gid, extInfo.serverId);



                        if (AppP2PAgentManager.getInstance().msgReceiveListener != null) {
                            if (ImConnectService.relatedGroupId.equals(msg.gid)) {
                                AppP2PAgentManager.getInstance().msgReceiveListener.onMsgReceive(
                                    uuid);
                            }
                        }
                        CustomLog.d(TAG, "V1.00消息版本，插入附件 uuid=" + uuid);
                        if (!TextUtils.isEmpty(uuid)) {
                            succ = true;
                        }

                    }
                }
            }
            break;
        }
        return succ;
    }

    public boolean parseDTMessage() {
        CustomLog.i(TAG, "parseMessage()");

        if (msg == null) {
            return false;
        }
        ExtInfo extInfo = null;
        String serVer = "";
        extInfo = convertExtInfo(msg.extendedInfo);
        if (extInfo != null) {
            serVer = extInfo.ver;
        }
        int verInt = -1;
        if (TextUtils.isEmpty(serVer)) {
            verInt = BizConstant.MSG_VERSION_INT_BASE_X;
        } else {
            verInt = BizConstant.getVersionInt(serVer);
        }

        //不认识的消息版本，判断是否兼容
        if (verInt == -1) {
            if (!BizConstant.compareMsgVersion(serVer)) {
                //不兼容，生成一条本地的txt文本，提醒用户升级
                insertIncompatibleTxtTip(msg, extInfo.serverId);
                return false;
            } else {
                //兼容，按照能识别的最高版本解析
                verInt = BizConstant.getVersionInt(BizConstant.MSG_VERSION);
            }
        }
        boolean succ = false;
        switch (verInt) {
            case BizConstant.MSG_VERSION_INT_BASE_X:
                CustomLog.d(TAG, "91以前消息版本,不解析直接丢弃");
                break;
            case BizConstant.MSG_VERSION_INT_BASE_00:
            default: {
                List<String> romteUrlList = getFileUrl(msg.body);
                List<String> thumbUrlList = null;
                if (extInfo != null) {
                    thumbUrlList = getFileUrl(extInfo.thumb);
                }

                //把拆解成多条记录插入到本地（本质上是一个）
                if (romteUrlList != null && romteUrlList.size() > 0) {
                    for (int i = 0; i < romteUrlList.size(); i++) {
                        CustomLog.d(TAG, "V1.00消息版本，插入本地附件 msgid=" + msg.msgId
                            + "| i = " + i);
                        String romteUrl = romteUrlList.get(i);
                        String thumbUrl = "";
                        if (thumbUrlList != null && thumbUrlList.size() > i) {
                            thumbUrl = thumbUrlList.get(i);
                        }

                        // 创建 body 字段，存储 CDN 文件地址
                        String body = noticesDao.createRecPAVMsgBody(
                            romteUrl,
                            thumbUrl,
                            msg.extendedInfo,
                            type);

                        // 赋值 msgID
                        String id = "";
                        if (i == 0) {
                            id = msg.msgId;//msg.msgId.replace("-", "");
                        } else {
                            id = "";
                        }

                        // 将附件消息插入数据库
                        String uuid = dtNoticesDao.createReceiveMsgNotice(id,
                            msg.sender,
                            msg.receivers,
                            body,
                            type,
                            MedicalApplication.shareInstance()
                                .getString(R.string.messsge_title_attachment),
                            msg.extendedInfo,
                            msg.time,
                            msg.gid, extInfo.serverId);



                        if (AppP2PAgentManager.getInstance().msgReceiveListener != null) {
                            if (ImConnectService.relatedGroupId.equals(msg.gid)) {
                                AppP2PAgentManager.getInstance().msgReceiveListener.onMsgReceive(
                                    uuid);
                            }
                        }
                        CustomLog.d(TAG, "V1.00消息版本，插入附件 uuid=" + uuid);
                        if (!TextUtils.isEmpty(uuid)) {
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
