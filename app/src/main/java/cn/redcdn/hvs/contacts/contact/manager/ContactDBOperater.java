package cn.redcdn.hvs.contacts.contact.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.List;

import cn.redcdn.hvs.contacts.contact.database.ASqlExecutor;
import cn.redcdn.log.CustomLog;

public class ContactDBOperater {
  private static final String TAG = ContactDBOperater.class.getSimpleName();
  private ASqlExecutor mSqlExecutor;
  private static ContactDBOperater mInstance;

  public static synchronized ContactDBOperater getInstance(Context context) {
    if (mInstance == null) {
      mInstance = new ContactDBOperater(context);
    }
    return mInstance;
  }

  private ContactDBOperater(Context context) {
    CustomLog.i(TAG, "ContactDBOperater::ContactDBOperater()");
    mSqlExecutor = ASqlExecutor.getInstance(context);
    // TODO 此处不传 account 进去，是否存在问题
  }

  public Cursor queryAllContacts(final String sql, final String account) {
    return mSqlExecutor.rawQuery(sql, account);
  }

  public long getMaxTimeStamp(final String sql, final String account) {
    Cursor cursor = mSqlExecutor.rawQuery(sql, account);
    long timestamp = 0;
    try {
      if (cursor != null && cursor.getCount() > 0) {
        if (cursor.moveToFirst()) {
          timestamp = cursor.getLong(0);
        }
      }
    } catch (Exception e) {

    } finally {
      if (cursor != null) {
        cursor.close();
        cursor = null;
      }
    }
    CustomLog.i(TAG, "ContactDBOperater::getMaxTimeStamp() timestamp:"
        + timestamp);
    return timestamp;
  }

  public Cursor queryNeedUpdateContacts(final String sql, final String account) {
    return mSqlExecutor.rawQuery(sql, account);
  }

  public Cursor rawQuery(final String sql, final String account) {
    return mSqlExecutor.rawQuery(sql, account);
  }

  public long insert(final String table, final ContentValues values) {
    long id = mSqlExecutor.insertCmd(table, values);
    return id;
  }

  public long applyUpdateBatch(final String table,
                                final List<ContentValues> list) {
    long id = mSqlExecutor.applyUpdateBatch(table, list);
    return id;
  }
  public long applyUpdateShareBatch(final String table,
                                      final List<ContentValues> list) {
    long id = mSqlExecutor.applyUpdateShareBatch(table, list);
    return id;
  }

  public long applyInsertBatch(final String table,
      final List<ContentValues> list) {
    long id = mSqlExecutor.applyInsertBatch(table, list);
    return id;
  }

  public long update(final String table, final ContentValues values,
      final String whereClause, final String[] whereArgs) {
    long id = mSqlExecutor.updateCmd(table, values, whereClause, whereArgs);
    return id;
  }

  public long delete(final String table, final String whereClause,
      final String[] whereArgs) {
    long id = mSqlExecutor.deleteCmd(table, whereClause, whereArgs);
    return id;
  }
  public long applyInsertBatchlogic(final String table, final List<ContentValues> list) {
    long id = mSqlExecutor.applyInsertBatchlogic(table, list);
    return id;
  }

  public long deleteAll(final String sql,final String table){
    long id = mSqlExecutor.deleteAllCmd(sql,table);
    return id;
  }



  public final void release() {
    mSqlExecutor.release();
    mInstance = null;
  }
}
