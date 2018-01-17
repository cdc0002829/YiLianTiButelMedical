package cn.redcdn.hvs.im.work;

import android.text.TextUtils;

import cn.redcdn.hvs.MedicalApplication;

/**
 * Desc    BizService常量
 * Created by wangkai on 2017/2/27.
 */

public class BizConstant {

    public static final String NET_PHONE_ACCOUNT_TYPE = MedicalApplication.getContext().getPackageName();
    //文字
    public static final String MSG_BODY_TYPE_TXT = "text2";

    // TODO:P2P Connect 版本中定义一个common类型，以后将不再随意增加type类型;
    // 需要在其定义接口的text 参数中再做字类型的判定
    public static final String MSG_BODY_TYPE_COMMON = "common";

    // x1报警消息：图片
    public static final String MSG_BODY_TYPE_HK_IMG = "housekeeping_img";

    //图片、视频的回复消息类型
    public static final String MSG_BODY_TYPE_MSGRP = "msgreply";

    //消息体中type参数类型
    public static final String MSG_BODY_TYPE_VIDEO = "videomessage";
    public static final String MSG_BODY_TYPE_VIDEO_2 = "videomessage2";
    public static final String MSG_BODY_TYPE_AUDIO = "audio2";
    public static final String MSG_BODY_TYPE_PIC = "picture";
    public static final String MSG_BODY_TYPE_PIC_2 = "picture2";
    public static final String MSG_BODY_TYPE_VCARD = "vcard";
    public static final String MSG_BODY_TYPE_POSTCARD = "postcard";
    public static final String MSG_BODY_TYPE_MULTITRUST = "multitrust";
    public static final String MSG_BODY_TYPE_IPCALL = "ipcall";
    public static final String MSG_BODY_TYPE_ONEKEYVISIT = "onekeyvisit";
    public static final String MSG_BODY_TYPE_ARTICLE = "article";

    //每次变更信息的版本号,都要在此定义,以及对应的int常量
    public static final String MSG_VERSION_STRING_BASE_00 ="1.00";
    public static final int MSG_VERSION_INT_BASE_00 =100;
    //    public static final String MSG_VERSION_STRING_BASE_01 ="1.01";
//    public static final String MSG_VERSION_STRING_BASE_02 ="1.02";
    // 消息的版本号，主要是区分json的内容解析区分
    // 此值应该是上述定义的_Base_00系列中最高的
    public static final String MSG_VERSION =MSG_VERSION_STRING_BASE_00;

    //会议邀请 子类型
    public static final String MSG_SUB_TYPE_MEETING = "meeting_invite";
    //会议预约 子类型
    public static final String MSG_SUB_TYPE_MEETING_BOOK = "meeting_book";

    //诊疗操作 子类型 "subtype":"reception_msg"
    public static final String MSG_SUB_TYPE_DT_OPERATION = "reception_msg";

    //诊疗操作 子类型, 转诊 "subtype":"diagnosis_msg"
    public static final String MSG_SUB_TYPE_DT_TRANSFER_OPERATION = "diagnosis_msg";

    //诊疗结论子类型
    public static final String MSG_SUB_TYPE_DT_RESULT = "diagnosis_msg";
    //诊疗结论为本地治疗
    public static final String MSG_DT_RESULT_TYPE_LOCAL = "local";
    //诊疗结论为转诊
    public static final String MSG_DT_RESULT_TYPE_TRANSFER = "transfer";

    public static final String MSG_SUB_TYPE_CHATRECORD = "chat_record";
    public static final String MSG_SUB_TYPE_ARTICLE = "article";
    public static final String MSG_SUB_TYPE_STRANGER = "stranger_msg";
    public static final String MSG_SUB_TYPE_ADDFRIEND = "add_friend_msg";
    public static final String MSG_SUB_TYPE_DELETEFRIEND = "delete_friend_msg";
    public static final String MSG_SUB_TYPE_REMIND_NOTICE = "remind_notice";
    public static final String MSG_SUB_TYPE_REMIND_ONE_NOTICE = "remind_notice_one";

    public static final String MSG_BODY_TYPE_ATTACHMENT = "attachment";


    //文件转发 子类型
    public static final String MSG_SUB_TYPE_FILE = "file";

    public static final int MSG_VERSION_INT_BASE_X =0;


    // 来电或外呼时，取消APK的下载线程，释放网络带宽
    public static final String JMEETING_INVITE_ACTION = NET_PHONE_ACCOUNT_TYPE
            + ".action.jmeeting.invate";


    public static int getVersionInt(String serVer){
        if(MSG_VERSION_STRING_BASE_00.equalsIgnoreCase(serVer)){
            return MSG_VERSION_INT_BASE_00;
        }
        return -1;
    }

    /**
     * 比较消息的版本号，按下述规则比较消息的版本号是否兼容
     * 格式:1.00（当前的消息版本号 BizConstant.MSG_VERSION定义）
     * 其中:第一位用于判断消息是否兼容。如果两个消息第一位相同，即标识消息兼容。
     *     第二位用于确定消息版本，不同版本用不同方式解决。
     * @param serVer 消息中带回的版本号，不能为空
     * @return true:兼容    false:不兼容
     */
    public static boolean compareMsgVersion(String serVer){
        if(!TextUtils.isEmpty(serVer)){
            int index = serVer.indexOf('.');
            String serVerPrefix = index!=-1?serVer.substring(0, index):serVer;
            String localVer = BizConstant.MSG_VERSION;
            index = localVer.indexOf('.');
            String localVerPrefix = index!=-1?localVer.substring(0, index):localVer;
            if(serVerPrefix.equalsIgnoreCase(localVerPrefix)){
                return true;
            }
        }
        return false;
    }
}
