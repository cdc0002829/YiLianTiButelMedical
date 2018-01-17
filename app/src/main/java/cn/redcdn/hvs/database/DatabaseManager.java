package cn.redcdn.hvs.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import cn.redcdn.hvs.contacts.contact.manager.ContactManager;
import cn.redcdn.log.CustomLog;

/**
 * Created by KevinZhang on 2017/3/7.
 */
public class DatabaseManager {
    private static final String TAG = DatabaseManager.class.getSimpleName();
    private static DatabaseManager mInstance;
    private SQLiteDatabase db;
    private Context mContext;

    private DatabaseManager() {

    }

    public static DatabaseManager getInstance() {
        if (mInstance == null) {
            mInstance = new DatabaseManager();
        }
        return mInstance;
    }

    public void init(Context context, String account) {
        CustomLog.i(TAG,"init");
        mContext = context;
        if (db == null) {
            if (!TextUtils.isEmpty(account)) {
                db = new DatabaseHelper(mContext, getDBPathName(account))
                    .getdatabase();
                boolean isOpen = db.isOpen();
                CustomLog.d(TAG, "db status:" + isOpen);
//                ContactManager.getInstance(MedicalApplication.shareInstance()).initData("");
            } else {
                CustomLog.e(TAG, "DatabaseManager::getSQLiteDatabase() account == null");
            }
        }
    }

    public void release() {
        ContactManager.getInstance(mContext).clearInfos();
        db.close();
        db = null;
    }

    public SQLiteDatabase getSQLiteDatabase() {
        return db;
    }

    /**
     * 获取数据库名称
     */
    public String getDBPathName(String userNubeNumber) {
        CustomLog.d(TAG, "nubeNumber=" + userNubeNumber);
        if (TextUtils.isEmpty(userNubeNumber)) {
            return DBConstant.SQLITE_FILE_NAME
                    + DBConstant.SQLITE_FILE_NAME_EXTENSION;
        } else {
            return DBConstant.SQLITE_FILE_NAME
                    + DBConstant.SQLITE_FILE_CONNECTOR + userNubeNumber
                    + DBConstant.SQLITE_FILE_NAME_EXTENSION;
        }
    }
}