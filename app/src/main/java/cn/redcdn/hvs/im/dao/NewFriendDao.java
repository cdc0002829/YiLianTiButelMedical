package cn.redcdn.hvs.im.dao; /**
 * @Title: NewFriendDao.java
 * @Package com.channelsoft.netphone.dao
 * @author miaolikui
 * @date 2013-8-27 上午11:06:09
 * @version V1.0
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import cn.redcdn.hvs.im.bean.ContactFriendBean;
import cn.redcdn.hvs.im.bean.NewFriendBean;
import cn.redcdn.hvs.im.column.NewFriendTable;
import cn.redcdn.hvs.im.provider.ProviderConstant;
import cn.redcdn.hvs.util.DateUtil;
import com.butel.connectevent.utils.CommonUtil;
import com.butel.connectevent.utils.LogUtil;
import java.util.ArrayList;
import java.util.List;

import static cn.redcdn.hvs.util.DateUtil.FORMAT_DATABASE;
import static cn.redcdn.hvs.util.DateUtil.getCurrentTimeSpecifyFormat;

/**
 * @ClassName: NewFriendDao
 * @author miaolikui
 * @date 2013-8-27 上午11:06:09
 */
public class NewFriendDao {

    public static String[] select_columns = {
        NewFriendTable.NEWFRIEND_COLUMN_ID,
        NewFriendTable.NEWFRIEND_COLUMN_LASTTIME,
        NewFriendTable.NEWFRIEND_COLUMN_NAME,
        NewFriendTable.NEWFRIEND_COLUMN_NUMBER,
        NewFriendTable.NEWFRIEND_COLUMN_NUBENUMBER,
        NewFriendTable.NEWFRIEND_COLUMN_STATUS,
        NewFriendTable.NEWFRIEND_COLUMN_ISNEW,
        NewFriendTable.NEWFRIEND_COLUMN_CONTACTUSERID,
        NewFriendTable.NEWFRIEND_COLUMN_HEADURL,
        NewFriendTable.NEWFRIEND_COLUMN_REALNAME,
        NewFriendTable.NEWFRIEND_COLUMN_VISIBLE,
        NewFriendTable.NEWFRIEND_COLUMN_SEX };

    private Context mcontext = null;

    public NewFriendDao(Context context) {
        this.mcontext = context;
    }

    public Cursor getAllNewFriends() {
        Log.d("NewFriendDao", "getAllNewFriends");
        LogUtil.d("getAllNewFriends");
        Cursor cursor = null;
        try {
            cursor = mcontext.getContentResolver().query(
                ProviderConstant.NETPHONE_NEWFRIEND_URI, select_columns,
                NewFriendTable.NEWFRIEND_COLUMN_VISIBLE + "!=1", null,
                NewFriendTable.NEWFRIEND_COLUMN_LASTTIME + " DESC");
        } catch (Exception e) {
            LogUtil.e("getAllNewFriends Exception", e);
        }
        return cursor;
    }

    public Cursor getAllLocalFindNewFriends() {
        Log.d("NewFriendDao", "getAllLocalFindNewFriends");
        LogUtil.d("getAllLocalFindNewFriends");
        Cursor cursor = null;
        try {
            cursor = mcontext.getContentResolver().query(
                ProviderConstant.NETPHONE_NEWFRIEND_URI,
                select_columns,
                NewFriendTable.NEWFRIEND_COLUMN_VISIBLE + "!=1 and "
                    + NewFriendTable.NEWFRIEND_COLUMN_STATUS + "== 5",
                null, NewFriendTable.NEWFRIEND_COLUMN_LASTTIME + " DESC");
        } catch (Exception e) {
            LogUtil.e("getAllLocalFindNewFriends Exception", e);
        }
        return cursor;
    }

    public int getAllNewFriendsCount() {
        Log.d("NewFriendDao", "getAllNewFriendsCount");
        int count = 0;
        Cursor cursor = null;
        try {
            cursor = mcontext.getContentResolver().query(
                ProviderConstant.NETPHONE_NEWFRIEND_URI, select_columns,
                NewFriendTable.NEWFRIEND_COLUMN_VISIBLE + "!=1", null,
                NewFriendTable.NEWFRIEND_COLUMN_LASTTIME + " DESC");
            if (cursor != null) {
                count = cursor.getCount();
                cursor.close();
                cursor = null;
            }
        } catch (Exception e) {
            LogUtil.e("getAllNewFriendsCount Exception", e);
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return count;
    }

    public List<NewFriendBean> getAllNewFriendsBean() {
        Log.d("NewFriendDao", "getAllNewFriendsCount");
        Cursor cursor = getAllNewFriends();
        try {
            if (cursor != null) {
                List<NewFriendBean> list = new ArrayList<NewFriendBean>();
                NewFriendBean bean = null;
                while (cursor.moveToNext()) {
                    bean = NewFriendTable.pureCursor(cursor);
                    if (bean != null) {
                        list.add(bean);
                    }
                }
                cursor.close();
                cursor = null;
                return list;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return null;
    }

    public List<ContactFriendBean> getLoacalFindNewFriendsBean() {
        Log.d("NewFriendDao", "getLoacalFindNewFriendsBean");
        Cursor cursor = getAllLocalFindNewFriends();
        try {
            if (cursor != null) {
                List<ContactFriendBean> list = new ArrayList<ContactFriendBean>();
                NewFriendBean bean = null;
                ContactFriendBean info = null;
                while (cursor.moveToNext()) {
                    bean = NewFriendTable.pureCursor(cursor);
                    info = new ContactFriendBean();
                    info.setSourcesId(bean.getId());
                    info.setNumber(bean.getNumber());
                    info.setNubeNumber(bean.getNubeNumber());
                    info.setName(bean.getName());
                    info.setInviteType("1");
                    info.setPym(CommonUtil.trackValue(bean.getName()));
                    Log.d("TAG", "NewFriendDao:手机号：" + info.getNumber());
                    if (bean != null) {
                        list.add(info);
                    }
                }
                cursor.close();
                cursor = null;
                return list;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return null;
    }

    public NewFriendBean getNewFriendById(String id) {
        Log.d("NewFriendDao", "getNewFriendById");
        Cursor cursor = null;
        try {
            cursor = mcontext.getContentResolver().query(
                ProviderConstant.NETPHONE_NEWFRIEND_URI, select_columns,
                NewFriendTable.NEWFRIEND_COLUMN_ID + " =? ",
                new String[] { id }, null);
            if (cursor != null) {
                NewFriendBean bean = null;
                if (cursor.moveToFirst()) {
                    bean = NewFriendTable.pureCursor(cursor);
                }
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
                return bean;
            }
        } catch (Exception e) {
            LogUtil.e("getNewFriendById Exception", e);
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return null;
    }

    // niuben start 20140928
    public NewFriendBean getNewFriendByNubeNumber(String nubeNumber) {
        Log.d("NewFriendDao", "getNewFriendByNubeNumber");
        Cursor cursor = null;
        try {
            cursor = mcontext.getContentResolver().query(
                ProviderConstant.NETPHONE_NEWFRIEND_URI, select_columns,
                NewFriendTable.NEWFRIEND_COLUMN_NUBENUMBER + " =? ",
                new String[] { nubeNumber }, null);
            if (cursor != null) {
                NewFriendBean bean = null;
                if (cursor.moveToFirst()) {
                    bean = NewFriendTable.pureCursor(cursor);
                }
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
                return bean;
            }
        } catch (Exception e) {
            LogUtil.e("getNewFriendByNubeNumber Exception", e);
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return null;
    }

    // NewFriendDao add start
    // 插入信息是否重复
    private boolean EliminateRepeat(NewFriendBean bean) {
        Log.d("NewFriendDao", "EliminateRepeat");
        boolean isEliminateRepeat = false;
        int status = bean.getStatus();
        String nubenumber = bean.getNubeNumber();
        List<NewFriendBean> beans = getAllNewFriendsBean();
        for (NewFriendBean b : beans) {
            if (b.getNubeNumber().equals(nubenumber)) {
                if (status == NewFriendBean.ADD_FRIEND_STATUS
                    || status == NewFriendBean.GREETING_MESSAGE_STATUS
                    || (status == NewFriendBean.LOCAL_FIND_STATUS && b
                    .getStatus() == NewFriendBean.LOCAL_FIND_STATUS)) {
                    UpdatNewFriend(bean);
                }
                isEliminateRepeat = true;
                break;
            }
        }
        return isEliminateRepeat;
    }

    private int UpdatNewFriend(NewFriendBean bean) {
        Log.d("NewFriendDao", "updateStatusByNubenumber");
        int count = 0;
        ContentValues value = new ContentValues();
        value.put(NewFriendTable.NEWFRIEND_COLUMN_ID, CommonUtil.getUUID());
        value.put(NewFriendTable.NEWFRIEND_COLUMN_LASTTIME,
            DateUtil.getDBOperateTime());
        value.put(NewFriendTable.NEWFRIEND_COLUMN_NAME, bean.getName());
        value.put(NewFriendTable.NEWFRIEND_COLUMN_NUMBER, bean.getNumber());
        value.put(NewFriendTable.NEWFRIEND_COLUMN_NUBENUMBER,
            bean.getNubeNumber());
        value.put(NewFriendTable.NEWFRIEND_COLUMN_STATUS, bean.getStatus());
        value.put(NewFriendTable.NEWFRIEND_COLUMN_ISNEW, bean.getIsNews());
        value.put(NewFriendTable.NEWFRIEND_COLUMN_CONTACTUSERID,
            bean.getContactUserId());
        value.put(NewFriendTable.NEWFRIEND_COLUMN_HEADURL, bean.getHeadUrl());
        value.put(NewFriendTable.NEWFRIEND_COLUMN_REALNAME, bean.getRealName());
        value.put(NewFriendTable.NEWFRIEND_COLUMN_VISIBLE, bean.getVisible());
        value.put(NewFriendTable.NEWFRIEND_COLUMN_SEX, bean.getSex());
        try {
            count = mcontext.getContentResolver().update(
                ProviderConstant.NETPHONE_NEWFRIEND_URI, value,
                NewFriendTable.NEWFRIEND_COLUMN_NUBENUMBER + " =? ",
                new String[] { bean.getNubeNumber() });
        } catch (Exception e) {
            LogUtil.e("UpdatNewFriend Exception", e);
        }
        value = null;
        return count;
    }

    public boolean insertNewFriend(NewFriendBean bean) {
        Log.d("NewFriendDao", "insertNewFriend");
        Uri uri = null;
        if (!EliminateRepeat(bean)) {
            Log.e("NewFriendDao", "insertNewFriend" + bean.getNubeNumber());
            ContentValues value = new ContentValues();
            value.put(NewFriendTable.NEWFRIEND_COLUMN_ID, CommonUtil.getUUID());
            value.put(NewFriendTable.NEWFRIEND_COLUMN_LASTTIME,
                DateUtil.getDBOperateTime());
            value.put(NewFriendTable.NEWFRIEND_COLUMN_NAME, bean.getName());
            value.put(NewFriendTable.NEWFRIEND_COLUMN_NUMBER, bean.getNumber());
            value.put(NewFriendTable.NEWFRIEND_COLUMN_NUBENUMBER,
                bean.getNubeNumber());
            value.put(NewFriendTable.NEWFRIEND_COLUMN_STATUS, bean.getStatus());
            value.put(NewFriendTable.NEWFRIEND_COLUMN_ISNEW, bean.getIsNews());
            value.put(NewFriendTable.NEWFRIEND_COLUMN_CONTACTUSERID,
                bean.getContactUserId());
            value.put(NewFriendTable.NEWFRIEND_COLUMN_HEADURL,
                bean.getHeadUrl());
            value.put(NewFriendTable.NEWFRIEND_COLUMN_REALNAME,
                bean.getRealName());
            value.put(NewFriendTable.NEWFRIEND_COLUMN_VISIBLE,
                bean.getVisible());
            value.put(NewFriendTable.NEWFRIEND_COLUMN_SEX, bean.getSex());
            try {
                uri = mcontext.getContentResolver().insert(
                    ProviderConstant.NETPHONE_NEWFRIEND_URI, value);
            } catch (Exception e) {
                LogUtil.e("insertNewFriend Exception", e);
            }
            value = null;
            if (uri != null) {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

    public boolean updateFriendStatusByNubeNumber(String nubeNumber,
                                                  String status) {
        Log.d("NewFriendDao", "updateFriendStatusByFriendId");
        int count = 0;
        Uri updateUri = Uri.parse(ProviderConstant.NETPHONE_NEWFRIEND_URI + "/"
            + status + "/" + nubeNumber);
        ContentValues value = new ContentValues();
        value.put(NewFriendTable.NEWFRIEND_COLUMN_LASTTIME,
            DateUtil.getDBOperateTime());
        value.put(NewFriendTable.NEWFRIEND_COLUMN_STATUS, status);
        try {
            count = mcontext.getContentResolver().update(
                updateUri,
                value,
                NewFriendTable.NEWFRIEND_COLUMN_NUBENUMBER + " =? and "
                    + NewFriendTable.NEWFRIEND_COLUMN_VISIBLE + "!=1",
                new String[] { nubeNumber });
            if (count > 0) {
                return true;
            }
        } catch (Exception e) {
            LogUtil.e("updateFriendStatusByNubeNumber Exception", e);
            value = null;
            return false;
        }
        return false;
    }

    // niuben end 20140928

    public int updateNewFriendStatus(String id, int status) {
        Log.d("NewFriendDao", "updateNewFriendStatus");
        int count = 0;
        ContentValues value = new ContentValues();
        value.put(NewFriendTable.NEWFRIEND_COLUMN_LASTTIME,
            DateUtil.getDBOperateTime());
        value.put(NewFriendTable.NEWFRIEND_COLUMN_STATUS, status);
        try {
            count = mcontext.getContentResolver().update(
                ProviderConstant.NETPHONE_NEWFRIEND_URI,
                value,
                NewFriendTable.NEWFRIEND_COLUMN_ID + " =? and "
                    + NewFriendTable.NEWFRIEND_COLUMN_VISIBLE + "!=1",
                new String[] { id });
        } catch (Exception e) {
            LogUtil.e("updateNewFriendStatus Exception", e);
            value = null;
        }
        return count;
    }

    public int updateNewFriendStatusByNumber(String number, int status) {
        Log.d("NewFriendDao", "updateNewFriendStatusByNumber");
        int count = 0;
        ContentValues value = new ContentValues();
        value.put(NewFriendTable.NEWFRIEND_COLUMN_LASTTIME,
            DateUtil.getDBOperateTime());
        value.put(NewFriendTable.NEWFRIEND_COLUMN_STATUS, status);
        try {
            count = mcontext.getContentResolver().update(
                ProviderConstant.NETPHONE_NEWFRIEND_URI,
                value,
                NewFriendTable.NEWFRIEND_COLUMN_NUMBER + " =? and "
                    + NewFriendTable.NEWFRIEND_COLUMN_VISIBLE + "!=1",
                new String[] { number });
        } catch (Exception e) {
            LogUtil.e("updateNewFriendStatusByNumber Exception", e);
        }
        value = null;
        return count;
    }

    /**
     *
     * @author: Administrator
     * @Title: deleteFriendStatusByNumber 删除相应状态的视频号中的新朋友记录
     * @Description:
     * @param number
     *            视频号码
     * @param status
     *            状态 -1 表示删除除5 以外的其它记录，否则删除对应状态的记录
     * @return
     * @date: 2014-3-6 下午2:25:03
     */
    public boolean deleteFriendStatusByNumber(String number, String status) {
        Log.d("NewFriendDao", "deleteFriendStatusByNumber");
        LogUtil.d("删除相应状态的视频号中的新朋友记录");
        try {
            Uri deleteUri = Uri.withAppendedPath(ProviderConstant.MEDICAL_URI,
                "DELETE_FRIEND_ITEM/" + number + "/" + status);

            int count = mcontext.getContentResolver().delete(deleteUri, null,
                null);
            if (count > 0) {
                return true;
            }
        } catch (Exception e) {
            LogUtil.e("Exception", e);
        }
        return false;
    }

    public int updateSendStatusByNubenumber(String nubenumber, int status,
                                            boolean updatetime) {
        Log.d("NewFriendDao", "updateSendStatusByNubenumber");
        int count = 0;
        ContentValues value = new ContentValues();
        if (updatetime) {
            value.put(NewFriendTable.NEWFRIEND_COLUMN_LASTTIME,
                DateUtil.getDBOperateTime());
        }
        value.put(NewFriendTable.NEWFRIEND_COLUMN_STATUS, status);
        try {
            count = mcontext.getContentResolver().update(
                ProviderConstant.NETPHONE_NEWFRIEND_URI,
                value,
                NewFriendTable.NEWFRIEND_COLUMN_STATUS + "=0 AND "
                    + NewFriendTable.NEWFRIEND_COLUMN_NUBENUMBER
                    + " =? ", new String[] { nubenumber });
        } catch (Exception e) {
            LogUtil.e("updateSendStatusByNubenumber Exception", e);
        }
        value = null;
        return count;
    }

    public int updateRecievedStatusByNubenumber(String nubenumber, int status,
                                                boolean updatetime) {
        Log.d("NewFriendDao", "updateRecievedStatusByNubenumber");
        int count = 0;
        ContentValues value = new ContentValues();
        if (updatetime) {
            value.put(NewFriendTable.NEWFRIEND_COLUMN_LASTTIME,
                DateUtil.getDBOperateTime());
        }
        value.put(NewFriendTable.NEWFRIEND_COLUMN_STATUS, status);
        try {
            count = mcontext.getContentResolver().update(
                ProviderConstant.NETPHONE_NEWFRIEND_URI,
                value,
                NewFriendTable.NEWFRIEND_COLUMN_STATUS + "=2 AND "
                    + NewFriendTable.NEWFRIEND_COLUMN_NUBENUMBER
                    + " =? ", new String[] { nubenumber });
        } catch (Exception e) {
            LogUtil.e("updateRecievedStatusByNubenumber Exception", e);
        }
        value = null;
        return count;
    }

    public int updateLocalFoundStatusByNubenumber(String nubenumber,
                                                  int status, boolean updatetime) {
        Log.d("NewFriendDao", "updateLocalFoundStatusByNubenumber");
        int count = 0;
        ContentValues value = new ContentValues();
        if (updatetime) {
            value.put(NewFriendTable.NEWFRIEND_COLUMN_LASTTIME,
                DateUtil.getDBOperateTime());
        }
        value.put(NewFriendTable.NEWFRIEND_COLUMN_STATUS, status);
        try {
            count = mcontext.getContentResolver().update(
                ProviderConstant.NETPHONE_NEWFRIEND_URI,
                value,
                NewFriendTable.NEWFRIEND_COLUMN_STATUS + "=5 AND "
                    + NewFriendTable.NEWFRIEND_COLUMN_NUBENUMBER
                    + " =? ", new String[] { nubenumber });
        } catch (Exception e) {
            LogUtil.e("updateLocalFoundStatusByNubenumber Exception", e);
        }
        value = null;
        return count;
    }

    /**
     * @modify:极会议版本测试发现同一个手机号有时返回两个用户信息(productId:prod001),而UE中要求：
     *                                                              e、手机通讯录有重复数据时
     *                                                              （
     *                                                              同一个手机号保存为不同的联系人
     *                                                              ），
     *                                                              按系统推荐时间只取第一个最早推荐的
     *                                                              所以，
     *                                                              此处需要根据手机号滤重
     *                                                              :将
     * @return
     */
    public int getNewFriendUnreadCount() {
        LogUtil.d("getNewFriendUnreadCount");
        // 为了保证查询的未读好友数字显示正确，修改2处：
        // 1.select distinct number from nube表---对手机号进行排重
        Uri uri = Uri.withAppendedPath(ProviderConstant.MEDICAL_URI,
            "GET_UNREAD_NEW_FRIEND");
        int count = 0;
        Cursor cursor = null;
        try {

            cursor = mcontext.getContentResolver().query(uri, null, null, null,
                null);
            // cursor = mcontext.getContentResolver().query(
            // ProviderConstant.NETPHONE_NEWFRIEND_URI,
            // select_columns,
            // NewFriendTable.NEWFRIEND_COLUMN_ISNEW + " =1 AND "
            // + NewFriendTable.NEWFRIEND_COLUMN_VISIBLE + "!=1",
            // null, null);
            if (cursor != null) {
                // 登录号码：
                // String telphoneString = MedicalApplication.getPreference()
                //     .getKeyValue(PrefType.LOGIN_MOBILE, "");
                count = cursor.getCount();
                // 2.排除登录号码对应的手机号--因为最近发现登录号码为63000051的手机号调searchAccount发现好友时
                //，返回了一条数据：50010166--2015-12-14
                if (cursor.moveToFirst()) {
                    do {
                        String number = cursor
                            .getString(cursor
                                .getColumnIndexOrThrow(NewFriendTable.NEWFRIEND_COLUMN_NUMBER));
                        //						LogUtil.d("number=" + number);
                        // if (telphoneString.equals(number)) {
                        //     count -= 1;
                        // }
                    } while (cursor.moveToNext());
                }
            }
            LogUtil.d("getNewFriendUnreadCount：count=" + count);
        } catch (Exception e) {
            LogUtil.e("getNewFriendUnreadCount Exception", e);
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return count;
    }

    public int setReadStatus() {
        LogUtil.d("setReadStatus");
        int count = 0;
        ContentValues value = new ContentValues();
        value.put(NewFriendTable.NEWFRIEND_COLUMN_ISNEW, 0);
        try {
            count = mcontext.getContentResolver().update(
                ProviderConstant.NETPHONE_NEWFRIEND_URI,
                value,
                NewFriendTable.NEWFRIEND_COLUMN_ISNEW + " =1 AND "
                    + NewFriendTable.NEWFRIEND_COLUMN_VISIBLE + "!=1",
                null);
        } catch (Exception e) {
            LogUtil.e("Exception", e);
        }
        value = null;
        return count;
    }

    public int deleteNewFriendById(String id) {
        Log.d("NewFriendDao", "deleteNewFriendById");
        int count = 0;
        try {
            count = mcontext.getContentResolver().delete(
                ProviderConstant.NETPHONE_NEWFRIEND_URI,
                NewFriendTable.NEWFRIEND_COLUMN_ID + " =? ",
                new String[] { id });
        } catch (Exception e) {
            LogUtil.e("deleteNewFriendById Exception", e);
        }
        return count;
    }

    public int deleteNewFriendByNubenumber(String Nubenumber) {
        Log.d("NewFriendDao", "deleteNewFriendByNubenumber");
        int count = 0;
        try {
            count = mcontext.getContentResolver().delete(
                ProviderConstant.NETPHONE_NEWFRIEND_URI,
                NewFriendTable.NEWFRIEND_COLUMN_NUBENUMBER + " =? ",
                new String[] { Nubenumber });
        } catch (Exception e) {
            LogUtil.e("deleteNewFriendByNubenumber Exception", e);
        }
        return count;
    }

    public int deleteAllNewFriends() {
        Log.d("NewFriendDao", "deleteAllNewFriends");
        int count = 0;
        try {
            count = mcontext.getContentResolver().delete(
                ProviderConstant.NETPHONE_NEWFRIEND_URI, null, null);
        } catch (Exception e) {
            LogUtil.e("deleteAllNewFriends Exception", e);
        }
        return count;
    }

    public boolean queryNewFriendByNubeNumber(String nubeNumber) {
        Log.d("NewFriendDao", "queryNewFriendByNubeNumber");
        Cursor cursor = null;
        try {
            cursor = mcontext.getContentResolver().query(
                ProviderConstant.NETPHONE_NEWFRIEND_URI,
                null,
                NewFriendTable.NEWFRIEND_COLUMN_NUBENUMBER + "=? and "
                    + NewFriendTable.NEWFRIEND_COLUMN_STATUS
                    + " =? and "
                    + NewFriendTable.NEWFRIEND_COLUMN_VISIBLE + "!=1",
                new String[] { nubeNumber, "5" }, null);
            if (cursor != null && cursor.getCount() > 0) {
                if (cursor != null) {
                }
                return false;
            }
        } catch (Exception e) {
            LogUtil.e("queryNewFriendByNubeNumber Exception", e);
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return true;
    }

    public String queryNewFriendExist(String nubeNumber) {
        Log.d("NewFriendDao", "queryNewFriendExist");
        Cursor cursor = null;
        String id = "";
        try {
            cursor = mcontext.getContentResolver().query(
                ProviderConstant.NETPHONE_NEWFRIEND_URI,
                new String[] { NewFriendTable.NEWFRIEND_COLUMN_ID },
                NewFriendTable.NEWFRIEND_COLUMN_NUBENUMBER + "=? and "
                    + NewFriendTable.NEWFRIEND_COLUMN_VISIBLE + "!=1",
                new String[] { nubeNumber }, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                id = cursor.getString(0);
            }
        } catch (Exception e) {
            LogUtil.e("queryNewFriendExist Exception", e);
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return id;
    }

    public boolean queryNewFriendByNubeNumber(String nubeNumber, String statues) {
        Log.d("NewFriendDao", "queryNewFriendByNubeNumber");
        Cursor cursor = null;
        try {
            cursor = mcontext.getContentResolver()
                .query(ProviderConstant.NETPHONE_NEWFRIEND_URI,
                    null,
                    NewFriendTable.NEWFRIEND_COLUMN_NUBENUMBER
                        + "=? and "
                        + NewFriendTable.NEWFRIEND_COLUMN_STATUS
                        + " =? and "
                        + NewFriendTable.NEWFRIEND_COLUMN_VISIBLE
                        + " <> 1",
                    new String[] { nubeNumber, statues }, null);
            if (cursor != null && cursor.getCount() > 0) {
                return true;
            }
        } catch (Exception e) {
            LogUtil.e("queryNewFriendByNubeNumber Exception", e);
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return false;
    }

    public boolean updateFriendStatusByFriendId(String newFriendId,
                                                String status, String nubeNumber) {
        Log.d("NewFriendDao", "updateFriendStatusByFriendId");
        int count = 0;
        Uri updateUri = Uri.parse(ProviderConstant.NETPHONE_NEWFRIEND_URI + "/"
            + status + "/" + nubeNumber);
        ContentValues value = new ContentValues();
        // 2014-11-26 孙剑要求修改为，添加成功，不更新时间戳
        // value.put(NewFriendTable.NEWFRIEND_COLUMN_LASTTIME,
        // DateUtil.getDBOperateTime());
        value.put(NewFriendTable.NEWFRIEND_COLUMN_STATUS, status);
        try {
            count = mcontext.getContentResolver().update(
                updateUri,
                value,
                NewFriendTable.NEWFRIEND_COLUMN_ID + " =? and "
                    + NewFriendTable.NEWFRIEND_COLUMN_VISIBLE + "!=1",
                new String[] { newFriendId });
            if (count > 0) {
                return true;
            }
        } catch (Exception e) {
            LogUtil.e("updateFriendStatusByFriendId Exception", e);
            value = null;
            return false;
        }

        return false;
    }

    /**
     * Description:所有数据库操作时间取值都采用本方法
     *
     * @return 当前时间yyyyMMddHHmmss格式，如：20120219111945
     */
    public static String getDBOperateTime() {
        return getCurrentTimeSpecifyFormat(FORMAT_DATABASE);
    }
}
