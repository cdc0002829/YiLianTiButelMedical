package cn.redcdn.hvs.contacts.contact.hpucontact;

import android.database.Cursor;
import android.net.Uri;

import cn.redcdn.hvs.im.activity.ChatActivity;
import cn.redcdn.hvs.im.bean.NoticesBean;
import cn.redcdn.hvs.im.column.GroupMemberTable;
import cn.redcdn.hvs.im.column.NubeFriendColumn;
import cn.redcdn.hvs.im.provider.ProviderConstant;
import cn.redcdn.log.CustomLog;

/**
 * Created by caizx on 2017/11/24.
 */

public class DtNoticesTable {
    public  static String TABLE_NAME = "t_hpu_notices";

    public static String NOTICE_COLUMN_ID = "id";
    public static String NOTICE_COLUMN_TYPE = "type";
    public static String NOTICE_COLUMN_ISNEW = "isNews";
    public static String NOTICE_COLUMN_ISREAD = "isRead";
    public static String NOTICE_COLUMN_BODY = "body";
    public static String NOTICE_COLUMN_SENDTIME = "sendTime";
    public static String NOTICE_COLUMN_SENDER = "sender";
    public static String NOTICE_COLUMN_RECIEVER = "receiver";
    public static String NOTICE_COLUMN_STATUS = "status";
    public static String NOTICE_COLUMN_RECEIVEDTIME = "receivedTime";
    public static String NOTICE_COLUMN_TITLE = "title";
    // miaolk add 20140926 支持消息回复的功能（类似朋友圈）
    public static String NOTICE_COLUMN_MSGID = "msgId";
    public static String NOTICE_COLUMN_EXTINFO = "extInfo";
    public static String NOTICE_COLUMN_FAIL_REPLYID = "failReplyId";
    public static String NOTICE_COLUMN_THREADSID = "threadsId";
    public static String NOTICE_COLUMN_SEVERID = "severid";
    public static String NOTICE_COLUMN_RESERVERSTR1 = "reserverStr1";

    public static final Uri URI  = Uri.withAppendedPath(ProviderConstant.MEDICAL_URI, TABLE_NAME);
    public static final String CREATETABLE =  "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
            " (" + NOTICE_COLUMN_ID + " VARCHAR(32) PRIMARY KEY, "
            + NOTICE_COLUMN_SEVERID + " TEXT, "
            + NOTICE_COLUMN_TITLE + " TEXT, "
            + NOTICE_COLUMN_RECEIVEDTIME + " TIMESTAMP, "
            + NOTICE_COLUMN_TYPE + " VARCHAR(32), "
            + NOTICE_COLUMN_BODY + " TEXT, "
            + NOTICE_COLUMN_SENDER + " VARCHAR(64) ,"
            + NOTICE_COLUMN_RECIEVER + " TEXT, "
            +NOTICE_COLUMN_ISNEW +" INT(4), "
            +NOTICE_COLUMN_STATUS + " INT(11), "
            +NOTICE_COLUMN_MSGID + " VARCHAR(32), "
            +NOTICE_COLUMN_EXTINFO + " TEXT, "
            +NOTICE_COLUMN_FAIL_REPLYID + " TEXT, "
            +NOTICE_COLUMN_ISREAD + " INT(4), "
            +NOTICE_COLUMN_SENDTIME + " TIMESTAMP, "
            +NOTICE_COLUMN_THREADSID + " VARCHAR(32), "
            +NOTICE_COLUMN_RESERVERSTR1 + " TEXT)";
    public static NoticesBean pureCursor(Cursor cursor) {
        if (cursor == null || cursor.isClosed()) {
            return null;
        }
        NoticesBean bean = new NoticesBean();
        try {
            bean.setId(cursor.getString(cursor
                    .getColumnIndexOrThrow(NOTICE_COLUMN_ID)));
            bean.setType(cursor.getInt(cursor
                    .getColumnIndexOrThrow(NOTICE_COLUMN_TYPE)));
            bean.setIsNew(cursor.getInt(cursor
                    .getColumnIndexOrThrow(NOTICE_COLUMN_ISNEW)));
            bean.setIsRead(cursor.getInt(cursor
                    .getColumnIndexOrThrow(NOTICE_COLUMN_ISREAD)));
            bean.setBody(cursor.getString(cursor
                    .getColumnIndexOrThrow(NOTICE_COLUMN_BODY)));
            bean.setSendTime(cursor.getLong(cursor
                    .getColumnIndexOrThrow(NOTICE_COLUMN_SENDTIME)));
            bean.setSender(cursor.getString(cursor
                    .getColumnIndexOrThrow(NOTICE_COLUMN_SENDER)));
            bean.setReciever(cursor.getString(cursor
                    .getColumnIndexOrThrow(NOTICE_COLUMN_RECIEVER)));
            bean.setStatus(cursor.getInt(cursor
                    .getColumnIndexOrThrow(NOTICE_COLUMN_STATUS)));
            // miaolk add 20130821
            bean.setReceivedTime(cursor.getLong(cursor
                    .getColumnIndexOrThrow(NOTICE_COLUMN_RECEIVEDTIME)));
            bean.setTitle(cursor.getString(cursor
                    .getColumnIndexOrThrow(NOTICE_COLUMN_TITLE)));
            // miaolk add 20140926 支持消息回复的功能（类似朋友圈）
            bean.setMsgId(cursor.getString(cursor
                    .getColumnIndexOrThrow(NOTICE_COLUMN_MSGID)));
            bean.setExtInfo(cursor.getString(cursor
                    .getColumnIndexOrThrow(NOTICE_COLUMN_EXTINFO)));
            bean.setFailReplyId(cursor.getString(cursor
                    .getColumnIndexOrThrow(NOTICE_COLUMN_FAIL_REPLYID)));
            bean.setThreadsId(cursor.getString(cursor
                    .getColumnIndexOrThrow(NOTICE_COLUMN_THREADSID)));

        } catch (Exception e) {
            CustomLog.e("NoticesTable","Exception" + e.toString());
            return null;
        }
        return bean;
    }

    public static NoticesBean pureChatCursor(Cursor cursor,int convType) {
        if (cursor == null || cursor.isClosed()) {
            return null;
        }
        NoticesBean bean = new NoticesBean();
        try {
            bean.setId(cursor.getString(cursor
                    .getColumnIndexOrThrow(NOTICE_COLUMN_ID)));
            bean.setType(cursor.getInt(cursor
                    .getColumnIndexOrThrow(NOTICE_COLUMN_TYPE)));
            bean.setIsNew(cursor.getInt(cursor
                    .getColumnIndexOrThrow(NOTICE_COLUMN_ISNEW)));
            bean.setIsRead(cursor.getInt(cursor
                    .getColumnIndexOrThrow(NOTICE_COLUMN_ISREAD)));
            bean.setBody(cursor.getString(cursor
                    .getColumnIndexOrThrow(NOTICE_COLUMN_BODY)));
            bean.setSendTime(cursor.getLong(cursor
                    .getColumnIndexOrThrow(NOTICE_COLUMN_SENDTIME)));
            bean.setSender(cursor.getString(cursor
                    .getColumnIndexOrThrow(NOTICE_COLUMN_SENDER)));
            bean.setReciever(cursor.getString(cursor
                    .getColumnIndexOrThrow(NOTICE_COLUMN_RECIEVER)));
            bean.setStatus(cursor.getInt(cursor
                    .getColumnIndexOrThrow(NOTICE_COLUMN_STATUS)));
            // miaolk add 20130821
            bean.setReceivedTime(cursor.getLong(cursor
                    .getColumnIndexOrThrow(NOTICE_COLUMN_RECEIVEDTIME)));
            bean.setTitle(cursor.getString(cursor
                    .getColumnIndexOrThrow(NOTICE_COLUMN_TITLE)));
            // miaolk add 20140926 支持消息回复的功能（类似朋友圈）
            bean.setMsgId(cursor.getString(cursor
                    .getColumnIndexOrThrow(NOTICE_COLUMN_MSGID)));
            bean.setExtInfo(cursor.getString(cursor
                    .getColumnIndexOrThrow(NOTICE_COLUMN_EXTINFO)));
            bean.setFailReplyId(cursor.getString(cursor
                    .getColumnIndexOrThrow(NOTICE_COLUMN_FAIL_REPLYID)));
            bean.setThreadsId(cursor.getString(cursor
                    .getColumnIndexOrThrow(NOTICE_COLUMN_THREADSID)));
            try {
                if(convType == ChatActivity.VALUE_CONVERSATION_TYPE_MULTI){
                    bean.setMemberName(cursor.getString(cursor
                            .getColumnIndexOrThrow(NubeFriendColumn.NAME)));
                    bean.setmNickName(cursor.getString(cursor
                            .getColumnIndexOrThrow(GroupMemberTable.Column.NICK_NAME)));
                    bean.setmPhone(cursor.getString(cursor
                            .getColumnIndexOrThrow(GroupMemberTable.Column.PHONE_NUM)));
                    if(cursor.getColumnIndex(GroupMemberTable.Column.GENDER) != -1){
                        bean.setSex(cursor.getString(cursor
                                .getColumnIndexOrThrow(GroupMemberTable.Column.GENDER)));
                    }
                    bean.setHeadUrl(cursor.getString(cursor
                            .getColumnIndexOrThrow(GroupMemberTable.Column.HEAD_URL)));
                }else{
                    bean.setMemberName(cursor.getString(cursor
                            .getColumnIndexOrThrow(NubeFriendColumn.NAME)));
                    bean.setHeadUrl(cursor.getString(cursor
                            .getColumnIndexOrThrow("mheadUrl")));
                    bean.setSex(cursor.getString(cursor
                            .getColumnIndexOrThrow(NubeFriendColumn.SEX)));
                }
            } catch (Exception e) {
                CustomLog.e("NoticesTable","Exception" + e.toString());
            }
        } catch (Exception e) {
            CustomLog.e("NoticesTable","Exception" + e.toString());
            return null;
        }
        return bean;
    }


    public static NoticesBean pureUnReadCursor(Cursor cursor) {
        if (cursor == null || cursor.isClosed()) {
            return null;
        }
        NoticesBean bean = new NoticesBean();
        try {
            bean.setId(cursor.getString(cursor
                    .getColumnIndexOrThrow(NOTICE_COLUMN_ID)));
            bean.setServerId(cursor.getString(cursor
                    .getColumnIndexOrThrow(NOTICE_COLUMN_SEVERID)));
        } catch (Exception e) {
            CustomLog.e("NoticesTable","Exception" + e.toString());
            return null;
        }
        return bean;
    }
}
