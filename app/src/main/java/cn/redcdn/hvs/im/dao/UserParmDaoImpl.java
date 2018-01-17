package cn.redcdn.hvs.im.dao;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.redcdn.hvs.im.column.UserParmColumn;
import cn.redcdn.hvs.im.provider.ProviderConstant;
import cn.redcdn.log.CustomLog;

/**
 * Desc
 * Created by wangkai on 2017/3/3.
 */

public class UserParmDaoImpl extends ContextWrapper implements UserParmDao{
    public UserParmDaoImpl(Context base) {
        super(base);
    }

    @Override
    public void updateUserParm(String commomKey, String commomValue,String userId) {
        if(TextUtils.isEmpty(commomKey)) return;
//		if(TextUtils.isEmpty(userId)) userId = ALL_MATCH;
        Uri updateUri = Uri.parse("content://" + ProviderConstant.SETTING_AUTHORITY+ "/UPDATE_USER_PARM/"+userId);
        ContentValues values = new ContentValues();
        values.put(UserParmColumn.COMMONKEY, commomKey);
        values.put(UserParmColumn.COMMONVALUE, commomValue);
        try {
            getContentResolver().insert(updateUri, values);
            CustomLog.d("UserParmDaoImpl","updateUserParm commomKey :"+commomKey+" success");
        } catch (Exception e) {
            e.printStackTrace();
            CustomLog.d("UserParmDaoImpl","updateUserParm commomKey :"+commomKey+"exception");
        }
    }

    @Override
    public String getUserParm(String commomKey, String userId) {
        if (TextUtils.isEmpty(commomKey))
            return "";
        Uri querytUri = Uri.parse("content://"
                + ProviderConstant.SETTING_AUTHORITY + "/GET_USER_PARM");
        Cursor cursor = null;
        String commomValue = "";
        try {
            cursor = getContentResolver().query(
                    querytUri,
                    UserParmColumn.USERPARM_PROJECTION,
                    UserParmColumn.COMMONKEY + "=? and "
                            + UserParmColumn.USER_ID + "=?",
                    new String[] { commomKey, userId }, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                commomValue = cursor.getString(1);
            }
            CustomLog.d("UserParmDaoImpl","getUserParm commomKey :" + commomKey + " success,value:"
                    + commomValue);
        } catch (Exception e) {
            CustomLog.d("UserParmDaoImpl","getUserParm commomKey :" + commomKey + "exception");
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return commomValue;
    }

    @Override
    public boolean initUserParams(ConcurrentHashMap<String,String> mCurrentHashMap) {

        ArrayList<ContentProviderOperation> cpoList =  null;
        ContentProviderOperation cpoItem = null;
        ContentValues values = null;
        Uri updateUri = Uri.parse("content://" + ProviderConstant.SETTING_AUTHORITY+ "/UPDATE_USER_PARM");
        try {
            ConcurrentHashMap<String, String> map = mCurrentHashMap;
            if (map != null && map.size() > 0) {
                cpoList = new ArrayList<ContentProviderOperation>();
                for (Map.Entry<String, String> paramsItem : map.entrySet()) {

                    values = new ContentValues();
                    values.put(UserParmColumn.COMMONKEY, paramsItem.getKey());
                    values.put(UserParmColumn.COMMONVALUE, paramsItem.getValue());
                    CustomLog.d("UserParmDaoImpl","key=="+ paramsItem.getKey()+"  value=="+paramsItem.getValue());
                    cpoItem  = ContentProviderOperation.newInsert(updateUri).withValues(values).build();
                    cpoList.add(cpoItem);
                }
                getContentResolver().applyBatch(ProviderConstant.SETTING_AUTHORITY, cpoList);
                return true;
            }

        } catch (Exception e) {
        }
        return false;
    }
}
