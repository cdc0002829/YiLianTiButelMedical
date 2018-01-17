package cn.redcdn.hvs.im.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import cn.redcdn.hvs.im.bean.FriendInfo;
import cn.redcdn.hvs.im.column.FriendRelationTable;
import cn.redcdn.hvs.im.provider.ProviderConstant;
import cn.redcdn.log.CustomLog;
import com.butel.connectevent.utils.LogUtil;
import java.util.ArrayList;
import java.util.List;

public class FriendsRelationDao {
    private Context mContext;
    private String TAG = getClass().getName();


    public FriendsRelationDao(Context context) {
        this.mContext = context;
    }


    public void insert(FriendInfo friendInfo) {
        CustomLog.i(TAG,"insert friendInfo=="+friendInfo.toString());
        insert(entity2ContentValue(friendInfo));
    }


    /**
     * 插入数据库
     */
    private Uri insert(ContentValues values) {
        CustomLog.i(TAG," private insert  ");
        if (values == null) {
            return null;
        }
        Uri uri = null;
        try {
            uri = mContext.getContentResolver().insert(FriendRelationTable.URI, values);
        } catch (Exception e) {
            LogUtil.e("Exception", e);
        }
        return uri;
    }


    /**
     * 根据nube号删除数据
     */
    public int deleteFriendByNubeNumber(String NubeNumber) {//返回值?
        CustomLog.i("FriendsRelationDao", "deleteFriendByNubeNumber");
        int count = 0;
        try {
            count = mContext.getContentResolver().delete(
                FriendRelationTable.URI,
                FriendRelationTable.NUBE_NUMBER + " = ? ",
                new String[] { NubeNumber });
        } catch (Exception e) {
            LogUtil.e("deleteNewFriendByNubeNumber Exception", e);
        }
        return count;
    }


    /**
     * 删除所有数据
     */
    public int deleteAllFriendInfo() {//返回值?
        CustomLog.i("FriendsRelationDao", "deleteAllFriendInfo");
        int count = 0;
        try {
            count = mContext.getContentResolver().delete(
                FriendRelationTable.URI,
                null,
                null);
        } catch (Exception e) {
            LogUtil.e("deleteNewFriendByNubeNumber Exception", e);
        }
        return count;
    }


    public int updateFriendRelationStatus(String nubeNumber, int status) {
        CustomLog.d(TAG, "updateFriendRelationStatus nubeNumber=="+nubeNumber + " 目标 status=="+ status );
        int count = 0;
        ContentValues value = new ContentValues();
        value.put(FriendRelationTable.RELATION_TYPE, status);
        try {
            count = mContext.getContentResolver().update(
                FriendRelationTable.URI,
                value,
                FriendRelationTable.NUBE_NUMBER + " =?  ",
                new String[] { nubeNumber });
        } catch (Exception e) {
            CustomLog.e("updateFriendRelationStatus Exception", e.toString());
        }

        return count;
    }


    public int updateFriendInfo(FriendInfo friendInfo) {
        CustomLog.i("FriendRelation", "updateFriendInfo friendInfo=="+friendInfo.toString());
        int count = 0;
        ContentValues value = entity2ContentValue(friendInfo);
        try {
            count = mContext.getContentResolver().update(
                FriendRelationTable.URI,
                value,
                FriendRelationTable.NUBE_NUMBER + " =?  ",
                new String[] { friendInfo.getNubeNumber() });
        } catch (Exception e) {
            LogUtil.e("updateFriendRelationStatus Exception", e);
        }
        return count;
    }


    public int updateFriendRelationIsDelete(String nubeNumber, int isDelete) {
        CustomLog.d("FriendRelation", "updateFriendRelationIsDelete nubeNumber=="+nubeNumber +"isDelete=="+isDelete);
        int count = 0;
        ContentValues value = new ContentValues();
        value.put(FriendRelationTable.IS_DELETED, isDelete);
        try {
            count = mContext.getContentResolver().update(
                FriendRelationTable.URI,
                value,
                FriendRelationTable.NUBE_NUMBER + " =?  ",
                new String[] { nubeNumber });
        } catch (Exception e) {
            LogUtil.e("updateFriendRelationStatus Exception", e);
        }
        return count;
    }


    public FriendInfo queryFriendInfoByNubeNumber(String nubeNumber) {
        CustomLog.i("FriendsRelationDao", "queryFriendByNubeNumber");
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(
                FriendRelationTable.URI, null,
                FriendRelationTable.NUBE_NUMBER + " = ? ",
                new String[] { nubeNumber }, null);
        } catch (Exception e) {
            LogUtil.e("queryNewFriendByNubeNumber Exception", e);
        }
        if (cursor != null) {
            return cursor2Entity(cursor);
        } else {
            return null;
        }
    }


    public Cursor queryRelationTypeByNubeNumber(String nubeNumber) {
        CustomLog.i(TAG, "queryRelationStateByNubeNumber  nubeNumber=="+nubeNumber);
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(
                FriendRelationTable.URI, new String[] { FriendRelationTable.RELATION_TYPE },
                FriendRelationTable.NUBE_NUMBER + " =? ",
                new String[] { nubeNumber }, null);
        } catch (Exception e) {
            CustomLog.e("queryNewFriendByNubeNumber Exception", e.toString());
        }
        return cursor;
    }


    public List<FriendInfo> getAllNewFriendInfo() {
        CustomLog.i("FriendsRelationDao", "getAllNewFriendInfos");
        List<FriendInfo> list = new ArrayList<>();
        Cursor cursor;
        Uri uri = Uri.withAppendedPath(ProviderConstant.MEDICAL_URI, "t_friends_relation_order_by_update_time");
        try {
            cursor = mContext.getContentResolver().query(
                uri, null, FriendRelationTable.IS_DELETED + "='0'"
                , null,
                null);
            list = cursor2FriendInfoList(cursor);
        } catch (Exception e) {
            LogUtil.e("getAllNewFriendInfos Exception", e);
        }
        return list;
    }


    public Cursor getAllFriend() {
        CustomLog.i("FriendsRelationDao", "getAllNewFriendInfos");
        Cursor cursor = null;
        Uri uri = Uri.withAppendedPath(ProviderConstant.MEDICAL_URI, "t_friends_relation_order_by_update_time");
        try {
            cursor = mContext.getContentResolver().query(
                uri, null, FriendRelationTable.IS_DELETED + "='0'"
                , null,
                null);
        } catch (Exception e) {
            LogUtil.e("getAllNewFriendInfos Exception", e);
        }
        return cursor;
    }


    public Cursor getAllFriendIncludeIsDelete() {
        CustomLog.i("FriendsRelationDao", "getAllNewFriendInfos");
        Cursor cursor = null;
        Uri uri = Uri.withAppendedPath(ProviderConstant.MEDICAL_URI, FriendRelationTable.TABLENAME);
        try {
            cursor = mContext.getContentResolver().query(
                uri, null, null
                , null,
                null);
        } catch (Exception e) {
            LogUtil.e("getAllNewFriendInfos Exception", e);
        }
        return cursor;
    }

    private List<FriendInfo> cursor2FriendInfoList(Cursor cursor) {
        List<FriendInfo> friendInfoList = new ArrayList<>();
        FriendInfo friendInfo;
        while (cursor.moveToNext()) {
            try {
                friendInfo = new FriendInfo();
                friendInfo.setNubeNumber(cursor.getString(cursor.getColumnIndexOrThrow(FriendRelationTable.NUBE_NUMBER)));
                friendInfo.setName(cursor.getString(cursor.getColumnIndexOrThrow(FriendRelationTable.NAME)));
                friendInfo.setHeadUrl(cursor.getString(cursor.getColumnIndexOrThrow(FriendRelationTable.HEAD_URL)));
                friendInfo.setRelationType(cursor.getInt(cursor.getColumnIndexOrThrow(FriendRelationTable.RELATION_TYPE)));
                friendInfo.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(FriendRelationTable.EMAIL)));
                friendInfo.setWorkUnitType(cursor.getString(cursor.getColumnIndexOrThrow(FriendRelationTable.WORK_UNIT_TYPE)));
                friendInfo.setWorkUnit(cursor.getString(cursor.getColumnIndexOrThrow(FriendRelationTable.WORK_UNIT)));
                friendInfo.setDepartment(cursor.getString(cursor.getColumnIndexOrThrow(FriendRelationTable.DEPARTMENT)));
                friendInfo.setProfessional(cursor.getString(cursor.getColumnIndexOrThrow(FriendRelationTable.PROFESSIONAL)));
                friendInfo.setOfficeTel(cursor.getString(cursor.getColumnIndexOrThrow(FriendRelationTable.OFFICETEL)));
                friendInfo.setUserFrom(cursor.getInt(cursor.getColumnIndexOrThrow(FriendRelationTable.USER_FROM)));
                friendInfo.setIsDeleted(cursor.getInt(cursor.getColumnIndexOrThrow(FriendRelationTable.IS_DELETED)));
                friendInfo.setNumber(cursor.getString(cursor.getColumnIndexOrThrow(FriendRelationTable.PHONE_NUMBER)));
            } catch (Exception e) {
                LogUtil.e("Exception", e);
                return null;
            }
            friendInfoList.add(friendInfo);
        }
        return friendInfoList;
    }


    private ContentValues entity2ContentValue(FriendInfo friendInfo) {
        if (friendInfo == null) {
            return null;
        }
        CustomLog.i(TAG,friendInfo.toString());
        ContentValues value = new ContentValues();
        value.put(FriendRelationTable.NUBE_NUMBER, friendInfo.getNubeNumber());
        value.put(FriendRelationTable.NAME, friendInfo.getName());
        value.put(FriendRelationTable.HEAD_URL, friendInfo.getHeadUrl());
        value.put(FriendRelationTable.RELATION_TYPE, friendInfo.getRelationType());
        value.put(FriendRelationTable.EMAIL, friendInfo.getEmail());
        value.put(FriendRelationTable.WORK_UNIT_TYPE, friendInfo.getWorkUnitType());
        value.put(FriendRelationTable.WORK_UNIT, friendInfo.getWorkUnit());
        value.put(FriendRelationTable.DEPARTMENT, friendInfo.getDepartment());
        value.put(FriendRelationTable.PROFESSIONAL, friendInfo.getProfessional());
        value.put(FriendRelationTable.OFFICETEL, friendInfo.getOfficeTel());
        value.put(FriendRelationTable.USER_FROM, friendInfo.getUserFrom());
        value.put(FriendRelationTable.PHONE_NUMBER, friendInfo.getNumber());
        value.put(FriendRelationTable.IS_DELETED, friendInfo.getIsDeleted());
        return value;
    }


    private FriendInfo cursor2Entity(Cursor cursor) {
        if (cursor == null || cursor.isClosed() || cursor.getCount() == 0) {
            return null;
        }
        FriendInfo friendInfo = new FriendInfo();
        try {
            if (cursor.moveToFirst()) {
                friendInfo.setNubeNumber(cursor.getString(cursor.getColumnIndexOrThrow(FriendRelationTable.NUBE_NUMBER)));
                friendInfo.setName(cursor.getString(cursor.getColumnIndexOrThrow(FriendRelationTable.NAME)));
                friendInfo.setHeadUrl(cursor.getString(cursor.getColumnIndexOrThrow(FriendRelationTable.HEAD_URL)));
                friendInfo.setRelationType(cursor.getInt(cursor.getColumnIndexOrThrow(FriendRelationTable.RELATION_TYPE)));
                friendInfo.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(FriendRelationTable.EMAIL)));
                friendInfo.setWorkUnitType(cursor.getString(cursor.getColumnIndexOrThrow(FriendRelationTable.WORK_UNIT_TYPE)));
                friendInfo.setWorkUnit(cursor.getString(cursor.getColumnIndexOrThrow(FriendRelationTable.WORK_UNIT)));
                friendInfo.setProfessional(cursor.getString(cursor.getColumnIndexOrThrow(FriendRelationTable.PROFESSIONAL)));
                friendInfo.setOfficeTel(cursor.getString(cursor.getColumnIndexOrThrow(FriendRelationTable.OFFICETEL)));
                friendInfo.setUserFrom(cursor.getInt(cursor.getColumnIndexOrThrow(FriendRelationTable.USER_FROM)));
                friendInfo.setIsDeleted(cursor.getInt(cursor.getColumnIndexOrThrow(FriendRelationTable.IS_DELETED)));
                friendInfo.setNumber(cursor.getString(cursor.getColumnIndexOrThrow(FriendRelationTable.PHONE_NUMBER)));
                friendInfo.setDepartment(cursor.getString(cursor.getColumnIndexOrThrow(FriendRelationTable.DEPARTMENT)));
            }
        } catch (Exception e) {
            LogUtil.e("Exception", e);
            return null;
        }

        CustomLog.i(TAG,"cursor2Entity friendInfo=="+friendInfo.toString());
        return friendInfo;
    }
}