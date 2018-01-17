package cn.redcdn.hvs.im.column;

import android.content.ContentValues;
import android.database.Cursor;

import cn.redcdn.hvs.im.bean.ThreadsBean;
import cn.redcdn.log.CustomLog;

/**
 * Desc
 * Created by wangkai on 2017/2/24.
 */

public class ThreadsTable {

    public static String TABLENAME = "t_threads";

    public static final String THREADS_COLUMN_ID = "id";
    public static final String THREADS_COLUMN_CREATETIME = "createTime";
    public static final String THREADS_COLUMN_LASTTIME = "lastTime";
    public static final String THREADS_COLUMN_TYPE = "type";
    public static final String THREADS_COLUMN_RECIPIENTIDS = "recipientIds";
    public static final String THREADS_COLUMN_EXTENDINFO = "extendInfo";
    public static final String THREADS_COLUMN_TOP = "top"; // 是否置顶 0：不置顶，1：置顶
    public static final String THREADS_COLUMN_RESERVERSTR1 = "reserverStr1"; // 是否免打扰 "0"：打扰，"1"：免打扰
    public static final String THREADS_COLUMN_RESERVERSTR2 = "reserverStr2";

    public static final int TOP_YES = 1;
    public static final int TOP_NO = 0;

    public static final String DISTRUB_YES = "0"; //"0"：打扰
    public static final String DISTRUB_NO = "1";  //"1"：免打扰

    public static final int TYPE_SINGLE_CHAT = 1;
    public static final int TYPE_GROUP_CHAT = 2;

    public static final int SAVE_CONTACT_YES = 1;
    public static final int SAVE_CONTACT_NO = 0;

    public static ContentValues makeContentValue(ThreadsBean bean) {
        if (bean == null) {
            return null;
        }

        ContentValues value = new ContentValues();
        value.put(THREADS_COLUMN_ID, bean.getId());
        value.put(THREADS_COLUMN_CREATETIME, bean.getCreateTime());
        value.put(THREADS_COLUMN_LASTTIME, bean.getLastTime());
        value.put(THREADS_COLUMN_TYPE, bean.getType());
        value.put(THREADS_COLUMN_RECIPIENTIDS, bean.getRecipientIds());
        value.put(THREADS_COLUMN_EXTENDINFO, bean.getExtendInfo());
        value.put(THREADS_COLUMN_TOP, bean.getTop());
        value.put(THREADS_COLUMN_RESERVERSTR1, bean.getReserverStr1());
        value.put(THREADS_COLUMN_RESERVERSTR2, bean.getReserverStr2());
        return value;
    }

    public static ThreadsBean pureCursor(Cursor cursor) {
        if (cursor == null || cursor.isClosed()) {
            return null;
        }

        ThreadsBean bean = new ThreadsBean();
        try {
            bean.setId(cursor.getString(cursor
                    .getColumnIndexOrThrow(THREADS_COLUMN_ID)));
            bean.setCreateTime(cursor.getLong(cursor
                    .getColumnIndexOrThrow(THREADS_COLUMN_CREATETIME)));
            bean.setLastTime(cursor.getLong(cursor
                    .getColumnIndexOrThrow(THREADS_COLUMN_LASTTIME)));
            bean.setType(cursor.getInt(cursor
                    .getColumnIndexOrThrow(THREADS_COLUMN_TYPE)));
            bean.setRecipientIds(cursor.getString(cursor
                    .getColumnIndexOrThrow(THREADS_COLUMN_RECIPIENTIDS)));
            bean.setExtendInfo(cursor.getString(cursor
                    .getColumnIndexOrThrow(THREADS_COLUMN_EXTENDINFO)));
            bean.setTop(cursor.getInt(cursor
                    .getColumnIndexOrThrow(THREADS_COLUMN_TOP)));
            bean.setReserverStr1(cursor.getString(cursor
                    .getColumnIndexOrThrow(THREADS_COLUMN_RESERVERSTR1)));
            bean.setReserverStr2(cursor.getString(cursor
                    .getColumnIndexOrThrow(THREADS_COLUMN_RESERVERSTR1)));
        } catch (Exception e) {
            CustomLog.e("ThreadsTable","Exception" + e.toString());
            return null;
        }
        return bean;
    }
}
