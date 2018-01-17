package cn.redcdn.hvs.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import cn.redcdn.hvs.im.column.UserParmColumn;
import cn.redcdn.log.CustomLog;


/**
 * Desc
 * Created by wangkai on 2017/3/6.
 */

public class SettingHelper extends SQLiteOpenHelper {

    private final String TAG = "SettingHelper";
    private static final String DATABASE_NAME = "setting.sqlite";
    private static final int DATABASE_VERSION = 1;
    private SQLiteDatabase myDataBase;

    public SettingHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public SQLiteDatabase getdatabase() {
        if (myDataBase == null || !myDataBase.isOpen()) {
            myDataBase = getWritableDatabase();
        }
        return myDataBase;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        CustomLog.d(TAG,"SettingHelper onCreate start");
        createAllTable(db);
        CustomLog.d(TAG,"SettingHelper onCreate end");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        CustomLog.d(TAG,"SettingHelper.onUpgrade start");
        if (newVersion != oldVersion) {
            // 升级数据库
            dropAllTable(db);
            createAllTable(db);
            CustomLog.d(TAG,"SettingHelper.onUpgrade end");
        }
    }
    private void createAllTable(SQLiteDatabase db) {
        try {
            db.execSQL(UserParmColumn.CREATE_TABLE);
        } catch (Exception e) {
            CustomLog.e("Exception", e.toString());
        }
    }
    private void dropAllTable(SQLiteDatabase db) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + UserParmColumn.TABLENAME);
        } catch (Exception e) {
            CustomLog.e("Exception", e.toString());
        }
    }
}
