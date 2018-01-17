package cn.redcdn.hvs.contacts.contact.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import cn.redcdn.hvs.database.DatabaseHelper;
import cn.redcdn.hvs.database.DatabaseManager;
import cn.redcdn.log.CustomLog;

public class ASqlExecutor {
  protected DatabaseHelper dbHelper;
  protected SQLiteDatabase mDb;
  private static ASqlExecutor instance;
  private static final Object mute = new Object();

  public static ASqlExecutor getInstance(Context context) {
    synchronized (mute) {

      if (null == instance) {
        instance = new ASqlExecutor();
//        instance.dbHelper = new DatabaseHelper(context);
//        instance.mDb = instance.dbHelper.getWritableDatabase();
//        instance.mDb.enableWriteAheadLogging();

//        instance.dbHelper = DatabaseHelper.getInstance();
        instance.mDb = DatabaseManager.getInstance().getSQLiteDatabase();
//        instance.mDb.enableWriteAheadLogging();
      }else {
        //通讯录在界面切换的时候会空白
         if (instance.mDb == null){
           instance.mDb = DatabaseManager.getInstance().getSQLiteDatabase();
         }
      }
    }
    return instance;
  }

  /************************* 联系人相关接口 **************************************/
  public final Cursor rawQuery(String sql, String table) {
    CustomLog.d("ASqlExecutor", " rawQuery " + sql + " table " + table);
    Cursor cursor = null;
    try {
//      dbHelper.checkTablebyName(table);
      cursor = DatabaseManager.getInstance().getSQLiteDatabase().rawQuery(sql, null);
    } catch (Exception ex) {
      CustomLog.e("ASqlExecutor", " rawQuery Error " + ex);
    }
    return cursor;
  }

  public final long insertCmd(String tableName, ContentValues cv) {
    CustomLog.d("ASqlExecutor", " insertCmd" + tableName);
    long rowid = -1;
    try {
//      dbHelper.checkTablebyName(tableName);
      if (cv != null
          && !isContactExist(cv.getAsString("nubeNumber"), tableName)) {
        rowid = DatabaseManager.getInstance().getSQLiteDatabase().insert(tableName, null, cv);
      } else {
        rowid = -2;
      }
    } catch (Exception ex) {
      CustomLog.e("ASqlExecutor", "insert Error " + ex);
    }
    return rowid;
  }

  public final int updateCmd(String table, ContentValues values,
      String whereClause, String[] whereArgs) {
    CustomLog.d("ASqlExecutor", " updateCmd" + table);
    int rownum = -1;
    try {
//      dbHelper.checkTablebyName(table);
      rownum = DatabaseManager.getInstance().getSQLiteDatabase().update(table, values, whereClause, whereArgs);
    } catch (Exception ex) {
      CustomLog.e("ASqlExecutor", "updateCmd Error " + ex);
    }
    return rownum;
  }

  public int deleteCmd(String table, String whereClause, String[] whereArgs) {
    CustomLog.d("ASqlExecutor", " deleteCmd" + table);
    int result = -1;
    try {
//      dbHelper.checkTablebyName(table);
      DatabaseManager.getInstance().getSQLiteDatabase().delete(table, whereClause, whereArgs);
      result = 0;
    } catch (Exception e) {
      CustomLog.e("ASqlExecutor", "deleteCmd Error " + e);
    }
    return result;
  }

  public int deleteAllCmd(String sql,String table) {
    CustomLog.d("ASqlExecutor", " deleteCmd" + table);
    int result = -1;
    try {
//      dbHelper.checkTablebyName(table);
      DatabaseManager.getInstance().getSQLiteDatabase().execSQL(sql);
      result = 0;
    } catch (Exception e) {
      CustomLog.e("ASqlExecutor", "deleteCmd Error " + e);
    }
    return result;
  }





  public void release() {
    closeDB();
    instance = null;
  }

  public void closeDB() {
    if(null!=dbHelper){
      dbHelper.close();
    }
  }

  public int applyUpdateBatch(String table, List<ContentValues> list) {
    CustomLog.d("ASqlExecutor", " applyUpdateBatch" + table);
    int resutlt = -1;
    DatabaseManager.getInstance().getSQLiteDatabase().beginTransaction();// 开始事务
    try {
//      dbHelper.checkTablebyName(table);
      for (int i = 0; i < list.size(); i++) {
        DatabaseManager.getInstance().getSQLiteDatabase().update(table, list.get(i), "contactId = ?",
            new String[] { (String) list.get(i).get("contactId") });
      }
      DatabaseManager.getInstance().getSQLiteDatabase().setTransactionSuccessful();// 设置事务标记为successful
      resutlt = 0;
    } catch (Exception e) {
      CustomLog.e("ASqlExecutor", "applyUpdateBatch Error " + e);
    } finally {
      DatabaseManager.getInstance().getSQLiteDatabase().endTransaction();// 结束事务
    }
    return resutlt;
  }
  //添加更新共享通讯录数据库
  public int applyUpdateShareBatch(String table, List<ContentValues> list) {
    CustomLog.d("ASqlExecutor", " applyUpdateShareBatch" + table);
    int resutlt = -1;
    DatabaseManager.getInstance().getSQLiteDatabase().beginTransaction();// 开始事务
    try {
//      dbHelper.checkTablebyName(table);
      for (int i = 0; i < list.size(); i++) {
        DatabaseManager.getInstance().getSQLiteDatabase().update(table, list.get(i), "nubeNumber = ?",
                new String[] { (String) list.get(i).get("nubeNumber") });
      }
      DatabaseManager.getInstance().getSQLiteDatabase().setTransactionSuccessful();// 设置事务标记为successful
      resutlt = 0;
    } catch (Exception e) {
      CustomLog.e("ASqlExecutor", "applyUpdateBatch Error " + e);
    } finally {
      DatabaseManager.getInstance().getSQLiteDatabase().endTransaction();// 结束事务
    }
    return resutlt;
  }

  public int applyInsertBatch(String table, List<ContentValues> list) {
      CustomLog.d("ASqlExecutor", " applyInsertBatch" + table);
    int resutlt = -1;
    DatabaseManager.getInstance().getSQLiteDatabase().beginTransaction();// 开始事务
    try {
//      dbHelper.checkTablebyName(table);
      for (int i = 0; i < list.size(); i++) {
        if (!isContactExist(list.get(i).getAsString("nubeNumber"), table)) {
          DatabaseManager.getInstance().getSQLiteDatabase().insert(table, null, list.get(i));
        }
      }
      DatabaseManager.getInstance().getSQLiteDatabase().setTransactionSuccessful();// 设置事务标记为successful
      resutlt = 0;
    } catch (Exception e) {
      CustomLog.e("ASqlExecutor", "applyInsertBatch Error " + e);
    } finally {
      DatabaseManager.getInstance().getSQLiteDatabase().endTransaction();// 结束事务
    }
    return resutlt;
  }

  public int applyInsertBatchlogic(String table, List<ContentValues> list) {
    CustomLog.d("ASqlExecutor", " applyInsertBatch" + table);
    int resutlt = -1;
    DatabaseManager.getInstance().getSQLiteDatabase().beginTransaction();// 开始事务
    try {
//      dbHelper.checkTablebyName(table);
      for (int i = 0; i < list.size(); i++) {
          DatabaseManager.getInstance().getSQLiteDatabase().insert(table, null, list.get(i));
      }
      DatabaseManager.getInstance().getSQLiteDatabase().setTransactionSuccessful();// 设置事务标记为successful
      resutlt = 0;
    } catch (Exception e) {
      CustomLog.e("ASqlExecutor", "applyInsertBatch Error " + e);
    } finally {
      DatabaseManager.getInstance().getSQLiteDatabase().endTransaction();// 结束事务
    }
    return resutlt;
  }
  private boolean isContactExist(String nubeNumber, String myTable) {
    String sql = "select count(*) from " + myTable + " where nubeNumber = '"
        + nubeNumber + "' and isDeleted = 0 ";
    Cursor c = DatabaseManager.getInstance().getSQLiteDatabase().rawQuery(sql, null);
    if (c != null && c.moveToNext()) {
      if (c.getInt(0) > 0) {
        return true;
      }
    }
    return false;
  }
}
