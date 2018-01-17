package cn.redcdn.hvs.im.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import cn.redcdn.hvs.im.provider.ProviderConstant;
import cn.redcdn.hvs.im.bean.NumberCacheBean;
import cn.redcdn.hvs.im.column.NumberCacheTable;
import com.butel.connectevent.utils.LogUtil;

public class NumberCacheDao {

    private static String[] select_columns = {
        NumberCacheTable.NUMBERCACHE_COLUMN_ID,
        NumberCacheTable.NUMBERCACHE_COLUMN_PHONE,
        NumberCacheTable.NUMBERCACHE_COLUMN_NEBU };

    private Context mcontext = null;

    public NumberCacheDao(Context context){
        this.mcontext = context;
    }

    public void updatePhoneNumberByNubeNumber(String nubeNumber,String phoneNumber) {
        ContentValues values=new ContentValues();
        values.put(NumberCacheTable.NUMBERCACHE_COLUMN_PHONE, phoneNumber);
        mcontext.getContentResolver().update(ProviderConstant.NETPHONE_NUMBERCACHE_URI, values,NumberCacheTable.NUMBERCACHE_COLUMN_NEBU + " =? ", new String[] {nubeNumber});
    }

    public String insertItem(NumberCacheBean bean){
        if (bean == null) {
            return "";
        }
        ContentValues value = NumberCacheTable.makeContentValue(bean);
        String uuid = bean.getId();
        //        if(TextUtils.isEmpty(uuid)){
        //        	uuid = CommonUtil.getUUID();
        //        	value.put(NumberCacheTable.NUMBERCACHE_COLUMN_ID, uuid);
        //        }
        Uri url = null;
        if (value != null) {
            try {
                url = mcontext.getContentResolver().insert(
                    ProviderConstant.NETPHONE_NUMBERCACHE_URI, value);
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.e("Exception", e);
            }
            value = null;
        }
        if (url != null) {
            return uuid;
        }
        return "";
    }

    public String getPhoneNumberByNebu(String nebu){
        if (TextUtils.isEmpty(nebu)) {
            return "";
        }
        Cursor cursor = null;
        String phone = null;
        try {
            cursor = mcontext.getContentResolver().query(
                ProviderConstant.NETPHONE_NUMBERCACHE_URI, select_columns,
                NumberCacheTable.NUMBERCACHE_COLUMN_NEBU + " =?",
                new String[] { nebu }, "");
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                phone = cursor.getString(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("Exception", e);
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

        return phone;
    }

    public String getNebuNumberByPhone(String phone){

        if (TextUtils.isEmpty(phone)) {
            return "";
        }
        Cursor cursor = null;
        String nebu = null;
        try {
            cursor = mcontext.getContentResolver().query(
                ProviderConstant.NETPHONE_NUMBERCACHE_URI, select_columns,
                NumberCacheTable.NUMBERCACHE_COLUMN_PHONE + " =?",
                new String[] { phone }, "");
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                nebu = cursor.getString(2);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("Exception", e);
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return nebu;
    }

    public boolean isExistPhoneNumber(String phone){
        boolean exist = false;
        Cursor cursor = null;
        try {
            cursor = mcontext.getContentResolver().query(
                ProviderConstant.NETPHONE_NUMBERCACHE_URI, select_columns,
                NumberCacheTable.NUMBERCACHE_COLUMN_PHONE + " =?",
                new String[] { phone }, "");
            if (cursor != null && cursor.getCount() > 0) {
                exist = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("Exception", e);
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return exist;
    }

    public boolean isExistNebuNumber(String nebu){
        boolean exist = false;
        Cursor cursor = null;
        try {
            cursor = mcontext.getContentResolver().query(
                ProviderConstant.NETPHONE_NUMBERCACHE_URI, select_columns,
                NumberCacheTable.NUMBERCACHE_COLUMN_NEBU + " =?",
                new String[] { nebu }, "");
            if (cursor != null && cursor.getCount() > 0) {
                exist = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("Exception", e);
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return exist;
    }
}
