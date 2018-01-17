package cn.redcdn.hvs.im.bean;

/**
 * Created by guoyx on 2017/2/25.
 */

import android.database.Cursor;

import cn.redcdn.log.CustomLog;

public class ThreadsTempTable {

    public static String TABLENAME = "t_thread_temp";
    public static String TEMP_COLUMN_THREADSID = "threadsId";
    //add at 15/6/23
    public static String TEMP_COLUMN_NAME = "name";
    public static String TEMP_COLUMN_NICKNAME = "nickName";
    public static String TEMP_COLUMN_PHONENUM = "phoneNum";
    public static String TEMP_COLUMN_NUBE_NUMBER = "nubeNumber";

    public static String TEMP_COLUMN_LASTTIME = "lastTime";
    // add 会话类型 1：单聊，2：群聊 at 15/6/17
    public static String TEMP_COLUMN_THREAD_TYPE = "threadType";
    public static String TEMP_COLUMN_RECIPIENTIDS = "recipientIds";
    public static String TEMP_COLUMN_NOTICESID = "noticesId";
    public static String TEMP_COLUMN_RECEIVEDTIME = "receivedTime";
    public static String TEMP_COLUMN_SENDTIME = "sendTime";
    public static String TEMP_COLUMN_STATUS = "status";
    public static String TEMP_COLUMN_NOTICE_TYPE = "noticeType";
    public static String TEMP_COLUMN_BODY = "body";
    public static String TEMP_COLUMN_ISNEWS = "isNews";
    public static String TEMP_COLUMN_EXTINFO = "extendInfo";
    public static String TEMP_COLUMN_HEADURL = "headUrl";
    public static String TEMP_COLUMN_SENDER = "sender";
    public static String TEMP_COLUMN_GHEADURL = "gHeadUrl";
    public static String TEMP_COLUMN_GNAME = "gName";
    public static String TEMP_COLUMN_ISTOP = "top";

    public static String TEMP_COLUMN_NOT_DISTURBE = "reserverStr1";


    public static ThreadsTempBean pureCursor(Cursor cursor) {
        if (cursor == null || cursor.isClosed()) {
            return null;
        }
        ThreadsTempBean bean = new ThreadsTempBean();
        try {
            bean.setThreadsId(cursor.getString(cursor
                    .getColumnIndexOrThrow(TEMP_COLUMN_THREADSID)));
            bean.setName(cursor.getString(cursor.getColumnIndexOrThrow(TEMP_COLUMN_NAME)));
            bean.setNickName(cursor.getString(cursor.getColumnIndexOrThrow(TEMP_COLUMN_NICKNAME)));
            bean.setPhoneNum(cursor.getString(cursor.getColumnIndexOrThrow(TEMP_COLUMN_PHONENUM)));
            bean.setNubeNumber(cursor.getString(cursor.getColumnIndexOrThrow(TEMP_COLUMN_NUBE_NUMBER)));
            bean.setLastTime(cursor.getLong(cursor
                    .getColumnIndexOrThrow(TEMP_COLUMN_LASTTIME)));
            bean.setThreadType(cursor.getInt(cursor
                    .getColumnIndexOrThrow(TEMP_COLUMN_THREAD_TYPE)));
            bean.setRecipientIds(cursor.getString(cursor
                    .getColumnIndexOrThrow(TEMP_COLUMN_RECIPIENTIDS)));
            bean.setNoticesId(cursor.getString(cursor
                    .getColumnIndexOrThrow(TEMP_COLUMN_NOTICESID)));
            //		bean.setReceivedTime(cursor.getLong(cursor
            //				.getColumnIndexOrThrow(TEMP_COLUMN_RECEIVEDTIME)));
            bean.setSendTime(cursor.getLong(cursor
                    .getColumnIndexOrThrow(TEMP_COLUMN_SENDTIME)));
            bean.setStatus(cursor.getInt(cursor
                    .getColumnIndexOrThrow(TEMP_COLUMN_STATUS)));
            bean.setNoticeType(cursor.getInt(cursor
                    .getColumnIndexOrThrow(TEMP_COLUMN_NOTICE_TYPE)));
            bean.setBody(cursor.getString(cursor
                    .getColumnIndexOrThrow(TEMP_COLUMN_BODY)));
            bean.setIsNews(cursor.getInt(cursor
                    .getColumnIndexOrThrow(TEMP_COLUMN_ISNEWS)));
            bean.setExtInfo(cursor.getString(cursor
                    .getColumnIndexOrThrow(TEMP_COLUMN_EXTINFO)));
            bean.setHeadUrl(cursor.getString(cursor
                    .getColumnIndexOrThrow(TEMP_COLUMN_HEADURL)));
            bean.setSender(cursor.getString(cursor
                    .getColumnIndexOrThrow(TEMP_COLUMN_SENDER)));
            bean.setgHeadUrl(cursor.getString(cursor
                    .getColumnIndexOrThrow(TEMP_COLUMN_GHEADURL)));
            bean.setgName(cursor.getString(cursor
                    .getColumnIndexOrThrow(TEMP_COLUMN_GNAME)));
            bean.setTop(cursor.getString(cursor
                    .getColumnIndexOrThrow(TEMP_COLUMN_ISTOP)));
            bean.setDoNotDisturb(
                    cursor.getString(cursor.getColumnIndexOrThrow(TEMP_COLUMN_NOT_DISTURBE)));
        }catch (IllegalArgumentException e){
            CustomLog.d("ThreadsTempBean",e.toString());
        }
        return bean;

    }
}
