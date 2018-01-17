package cn.redcdn.hvs.database;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteOpenHelper;

import cn.redcdn.hvs.contacts.contact.hpucontact.DtNoticesTable;
import cn.redcdn.hvs.contacts.contact.hpucontact.HpuContactsTable;
import cn.redcdn.hvs.im.column.FriendRelationTable;
import cn.redcdn.hvs.im.column.StrangerMessageTable;
import cn.redcdn.hvs.util.NotificationUtil;
import cn.redcdn.hvs.util.StringUtil;
import cn.redcdn.log.CustomLog;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Desc
 * Created by wangkai on 2017/2/23.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private Context mContext;
    private final String TAG = "DatabaseHelper";
    private String dbFileName = "";
    private String dbFileFolder = "";
    private SQLiteDatabase tmpDb = null;


    /**
     * 构造方法
     * @param context  上下文
     * @param dbName   数据库名称
     */
    protected DatabaseHelper(Context context, String dbName) {
        super(context, dbName, null, DBConstant.DATABASE_VERSION_4);
        CustomLog.i(TAG,"DatabaseHelper 构造方法");
        dbFileName = dbName;
        mContext = context;
        dbFileFolder = DBConstant.SQLITE_FILE_ROM_FOLDER;//   路径  data/data/cn.redcdn.hvs/files
        CustomLog.d(TAG,"数据库路径为" + dbFileFolder);
    }


    /**
     * 获取database实例   并且通过该方法调用   getWriteableDataBase
     */
    protected SQLiteDatabase getdatabase() {
        CustomLog.i(TAG,"getdatabase");
        return openDataBase();
    }


    private SQLiteDatabase openDataBase() throws SQLException {
        CustomLog.d(TAG,"openDataBase"+ "打开数据库:" + dbFileFolder+"/" + dbFileName);
        try {
            File filepath = new File(dbFileFolder);
            if (!filepath.exists()) {
                filepath.mkdirs();
            }
            CustomLog.i(TAG, "getWritableDatabase exec begin");
            // 通过此方法，让SQLiteOpenHelper执行onCreate或onUpgrade，管理数据库的创建和升级
            getWritableDatabase();
            if (!StringUtil.isEmpty(dbFileFolder)) {
                File databases_dir = new File(dbFileFolder + "/" + dbFileName);//  data/data/cn.redcdn.hvs/files//nubeNumber.db
                if (!databases_dir.exists()) {
                    createDB();
                }
                getTmpDb();
                CustomLog.i(TAG, "getdatabase path:" + tmpDb != null ? tmpDb.getPath() : "null"
                    + " dbIsOpen:" + tmpDb.isOpen());
            }
            CustomLog.i(TAG, "getWritableDatabase exec end");
        } catch (SQLiteDatabaseCorruptException e) {
            CustomLog.e(TAG, "SQLiteDatabaseCorruptException 打开数据库异常：" + e.toString());
        } catch (Exception ex) {
            CustomLog.e("Exception 打开数据库异常：", ex.toString());
        } finally {
            super.close();
        }
        return tmpDb;
    }


    private void getTmpDb() {
        if (tmpDb == null){
            tmpDb = SQLiteDatabase.openDatabase(dbFileFolder + '/' + dbFileName, null,
                SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        }
    }


    private void copySqlite() {
        CustomLog.i(TAG,"copySqlite");
        File file = new File(dbFileFolder);
        if (file.exists()) {
            for (File files : file.listFiles()) {
                String fileName = files.getName();
                CustomLog.d(TAG, "fileName:" + fileName);
                if (files.isFile() && fileName.equals(dbFileName)) {
                    boolean deleteSuccess = files.delete();
                    CustomLog.d(TAG, "数据库更新前,删除(" + dbFileFolder + dbFileName
                        + ")是否成功:" + deleteSuccess);
                    break;
                }
            }
        }
        createDB();
    }


    private void createDB() {
        CustomLog.i(TAG,"createDB");
        try {
            // 拷贝数据库到内存
            copySqlite2Rom(mContext);
        } catch (Exception e) {
            CustomLog.e(TAG, "内存空间不足，数据库拷贝失败" + e.toString());
            NotificationUtil.sendNoSpaceNotifacation();
        }
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        CustomLog.d(TAG, "onCreate begin");
        copySqlite();
        CustomLog.d(TAG, "onCreate end");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        CustomLog.i(TAG,"onUpgrade "+ "oldVersion=" + oldVersion + "|newVersion=" + newVersion);
        getTmpDb();
        switch (oldVersion) {
            case 2:
                CustomLog.i(TAG,"execSQL | " +StrangerMessageTable.CREATE_TABLE +"|"+FriendRelationTable.CREATE_TABLE);
                tmpDb.execSQL(StrangerMessageTable.CREATE_TABLE);
                tmpDb.execSQL(FriendRelationTable.CREATE_TABLE);
                break;
            case 3:
                CustomLog.i(TAG,"execSQL |"+ HpuContactsTable.CREATETABLE + "|" + DtNoticesTable.CREATETABLE);
                tmpDb.execSQL(HpuContactsTable.CREATETABLE);
                tmpDb.execSQL(DtNoticesTable.CREATETABLE);
                break;
            default:
                upGradeDB(newVersion);//目前支持   oldVersion==1 或者 oldversion==4
                break;
        }
    }


    private void upGradeDB(int _version) {
        CustomLog.i(TAG,"upGradeDB _version=="+_version);

        if (_version == 3) {
            SQLiteDatabase db = DatabaseManager.getInstance().getSQLiteDatabase();
            try {
                if (db != null) {
                    db.close();
                    db = null;
                }
                copySqlite();
            } catch (Exception e) {
                CustomLog.e("copySqlite出现异常", e.toString());
            }
        }
    }


    /**
     * 将拷贝  assets目录下面的  medical.db 到  data/data/cn.redcdn.hvs/files/
     *
     * @param context
     * @throws IOException
     */
    public void copySqlite2Rom(Context context)
        throws Exception {
        CustomLog.d(TAG, "copySqlite2Rom begin,");
        FileOutputStream fos = null;
        try {
            String newPath = DBConstant.SQLITE_FILE_ROM_FOLDER + "/" + dbFileName;
            File filedb = new File(newPath);
            if (filedb.exists()) {
                CustomLog.d(TAG, "手机内存中存在数据库文件:" + newPath);
            } else {
                CustomLog.d(TAG, "复制数据库文件到手机内存:" + newPath);
                fos = context.openFileOutput(dbFileName, Context.MODE_APPEND);
                byte[] b = new byte[context.getAssets()
                    .open(DBConstant.SQLITE_FILE_NAME_DEFAULT).available()];
                context.getAssets().open(DBConstant.SQLITE_FILE_NAME_DEFAULT)
                    .read(b);
                fos.write(b);
            }

        } catch (IOException e) {
            CustomLog.e(TAG, "ioexception" + e.toString());
            throw e;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                    fos = null;
                } catch (IOException e) {
                    CustomLog.e(TAG, "copySqlite2Rom" + "fos.close() ioexception" + e.toString());
                }
            }
        }
        CustomLog.d(TAG, "copySqlite2Rom end,");
    }
}
