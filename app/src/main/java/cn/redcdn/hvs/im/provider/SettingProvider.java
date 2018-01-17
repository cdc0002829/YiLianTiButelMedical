package cn.redcdn.hvs.im.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

import cn.redcdn.hvs.database.SettingHelper;
import cn.redcdn.hvs.im.column.UserParmColumn;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.log.CustomLog;

/**
 * Desc
 * Created by wangkai on 2017/3/6.
 */

public class SettingProvider extends ContentProvider {

    private final String TAG = "SettingProvider";

    private SettingHelper dbHelper;
    private SQLiteDatabase db;

    private static final String AUTHORITY = ProviderConstant.SETTING_AUTHORITY;
//	private static final Uri CONTENT_URI = ProviderConstant.SETTING_URI;

    /** TODO: setting start */
    private static final int UPDATE_USER_PARM = 100;
    private static final int GET_USER_PARM = 101;

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "UPDATE_USER_PARM/*", UPDATE_USER_PARM);
        uriMatcher.addURI(AUTHORITY, "GET_USER_PARM", GET_USER_PARM);
    }

    @Override
    public boolean onCreate() {
        CustomLog.d(TAG,"SettingProvider onCreate start");

        dbHelper = new SettingHelper(getContext());
        checkDatabase();

        CustomLog.d(TAG,"SettingProvider onCreate:" + db);
        return (db == null) ? false : true;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    private synchronized void checkDatabase(){
        if(db == null || !db.isOpen()){
            db = dbHelper.getdatabase();
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        checkDatabase();
        switch (uriMatcher.match(uri)) {
            case GET_USER_PARM:
                cursor = db.query(UserParmColumn.TABLENAME, projection, selection,
                        selectionArgs, null, null, null);
                break;
            default:
                break;
        }
        if (null != cursor) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri _uri = null;

        checkDatabase();


        int test = uriMatcher.match(uri);

        switch (uriMatcher.match(uri)) {

            case UPDATE_USER_PARM:
                if (values == null){
                    break;
                }
                String useId = uri.getPathSegments().get(1);
                String key = values.getAsString(UserParmColumn.COMMONKEY);
                if(TextUtils.isEmpty(useId)) useId = null;
                CustomLog.d(TAG,"根据key:" + key + "更新用户参数值");

                Cursor cursorUserId = null;
                boolean mark = false;
                try {
                    cursorUserId = db.query(UserParmColumn.TABLENAME,
                            UserParmColumn.USERPARM_PROJECTION,
                            UserParmColumn.COMMONKEY + "=? and "+ UserParmColumn.USER_ID+"=?", new String[] { key,useId},
                            null, null, null);

                    if (null != cursorUserId && cursorUserId.getCount() > 0) {
                        mark = true;
                    }
                    if (cursorUserId != null) {
                        cursorUserId.close();
                        cursorUserId = null;
                    }

                    if (mark) {
                        // update
                        db.update(UserParmColumn.TABLENAME, values,
                                UserParmColumn.COMMONKEY + "=? and "+ UserParmColumn.USER_ID+"=?", new String[] { key,useId});
                    } else {
                        // insert
                        values.put(UserParmColumn.USER_ID, useId);
                        values.put(UserParmColumn.ID, CommonUtil.getUUID());
                        db.insert(UserParmColumn.TABLENAME, null, values);
//					if (rowID > 0) {
//						_uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
//						getContext().getContentResolver().notifyChange(_uri, null);
//					}
                    }
                } catch (Exception e) {
                    CustomLog.e("UPDATE_USER_PARM   Exception:", e.toString());
                }finally{
                    if (cursorUserId != null) {
                        cursorUserId.close();
                        cursorUserId = null;
                    }
                }
                break;
            default:
                break;
        }
        return _uri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        checkDatabase();
        switch (uriMatcher.match(uri)) {
            default:
                break;
        }
        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int count = 0;
        checkDatabase();
        switch (uriMatcher.match(uri)) {
            default:
                break;
        }
        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }
}
