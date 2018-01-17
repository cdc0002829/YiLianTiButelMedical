package cn.redcdn.hvs.im.column;

import android.content.ContentValues;
import android.database.Cursor;
import cn.redcdn.hvs.im.bean.NumberCacheBean;
import com.butel.connectevent.utils.LogUtil;

public class NumberCacheTable {
    public static String TABLENAME = "t_numbercache";

    public static String NUMBERCACHE_COLUMN_ID = "id";
    public static String NUMBERCACHE_COLUMN_PHONE = "phonenumber";
    public static String NUMBERCACHE_COLUMN_NEBU = "nebunumber";

    public static ContentValues makeContentValue(NumberCacheBean bean) {
        if (bean == null) {
            return null;
        }
        ContentValues value = new ContentValues();
        //value.put(NUMBERCACHE_COLUMN_ID, bean.getId());
        value.put(NUMBERCACHE_COLUMN_PHONE, bean.getPhonenumber());
        value.put(NUMBERCACHE_COLUMN_NEBU, bean.getNebunumber());

        return value;
    }

    public static NumberCacheBean pureCursor(Cursor cursor) {
        if (cursor == null || cursor.isClosed()) {
            return null;
        }
        NumberCacheBean bean = new NumberCacheBean();
        try {
            bean.setId(cursor.getString(cursor
                .getColumnIndexOrThrow(NUMBERCACHE_COLUMN_ID)));
            bean.setPhonenumber(cursor.getString(cursor
                .getColumnIndexOrThrow(NUMBERCACHE_COLUMN_PHONE)));
            bean.setNebunumber(cursor.getString(cursor
                .getColumnIndexOrThrow(NUMBERCACHE_COLUMN_NEBU)));
        } catch (Exception e) {
            LogUtil.e("Exception", e);
            return null;
        }
        return bean;
    }
}
