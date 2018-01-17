package cn.redcdn.hvs.im.column;

import android.content.ContentValues;
import android.database.Cursor;

import cn.redcdn.hvs.im.activity.ChatActivity;
import cn.redcdn.hvs.im.bean.NoticesBean;
import cn.redcdn.log.CustomLog;

/**
 * Desc
 * Created by wangkai on 2017/2/24.
 */

public class NoticesTable {

    public static String TABLENAME = "t_notices";

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

    public static ContentValues makeContentValue(NoticesBean bean) {
        if (bean == null) {
            return null;
        }
        ContentValues value = new ContentValues();
        value.put(NOTICE_COLUMN_ID, bean.getId());
        value.put(NOTICE_COLUMN_TYPE, bean.getType());
        value.put(NOTICE_COLUMN_ISNEW, bean.getIsNew());
        value.put(NOTICE_COLUMN_ISREAD, bean.getIsRead());
        value.put(NOTICE_COLUMN_BODY, bean.getBody());
        value.put(NOTICE_COLUMN_SENDTIME, bean.getSendTime());
        value.put(NOTICE_COLUMN_SENDER, bean.getSender());
        value.put(NOTICE_COLUMN_RECIEVER, bean.getReciever());
        value.put(NOTICE_COLUMN_STATUS, bean.getStatus());
        // miaolk add 20130821
        value.put(NOTICE_COLUMN_RECEIVEDTIME, bean.getReceivedTime());
        value.put(NOTICE_COLUMN_TITLE, bean.getTitle());
        // miaolk add 20140926 支持消息回复的功能（类似朋友圈）
        value.put(NOTICE_COLUMN_MSGID, bean.getMsgId());
        value.put(NOTICE_COLUMN_EXTINFO, bean.getExtInfo());
        value.put(NOTICE_COLUMN_FAIL_REPLYID, bean.getFailReplyId());
        value.put(NOTICE_COLUMN_THREADSID, bean.getThreadsId());
        value.put(NOTICE_COLUMN_SEVERID, bean.getServerId());
        value.put(NOTICE_COLUMN_RESERVERSTR1, bean.getReserverStr1());

        return value;
    }

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
