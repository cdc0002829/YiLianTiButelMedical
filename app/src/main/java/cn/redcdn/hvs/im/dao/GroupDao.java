package cn.redcdn.hvs.im.dao;

import android.annotation.TargetApi;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.text.TextUtils;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.im.bean.ContactFriendBean;
import cn.redcdn.hvs.im.bean.GroupBean;
import cn.redcdn.hvs.im.bean.GroupMemberBean;
import cn.redcdn.hvs.im.bean.ShowNameUtil;
import cn.redcdn.hvs.im.column.GroupMemberTable;
import cn.redcdn.hvs.im.column.GroupTable;
import cn.redcdn.hvs.im.column.NubeFriendColumn;
import cn.redcdn.hvs.im.provider.ProviderConstant;
import cn.redcdn.log.CustomLog;
import com.butel.connectevent.utils.LogUtil;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Desc
 * Created by wangkai on 2017/2/24.
 */

public class GroupDao extends ContextWrapper {

    private final String TAG = "GroupDao";


    public GroupDao(Context base) {
        super(base);
    }


    public int getGroupColumnId(String gid) {
        GroupBean bean = queryGroup(gid);
        if (bean != null) {
            return bean.getId();
        }
        return 0;
    }


    public String getGroupManager(String gid) {
        GroupBean bean = queryGroup(gid);
        if (bean != null) {
            return bean.getMgrNube();
        }
        return "";
    }


    public String getGroupHeadUrl(String gid) {
        GroupBean bean = queryGroup(gid);
        if (bean != null) {
            return bean.getHeadUrl();
        }
        return "";
    }


    /**
     * 根据群号，查询该群的名称
     *
     * @param gid 群号
     * @return 群的名称
     */
    public String getGroupNameByGid(String gid) {
        CustomLog.d(TAG, "getGroupNameByGid begin");
        Cursor cursor = null;
        Uri uri = Uri.withAppendedPath(ProviderConstant.MEDICAL_URI,
            "GROUP_3_MEMBERS_NAME_BY_GID" + "/" + gid);
        String name = "";
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor == null || cursor.getCount() == 0) {
                CustomLog.d(TAG, "未查到");
                return getString(R.string.groupChat);
            }
            cursor.moveToFirst();
            String gName = cursor.getString(cursor.getColumnIndexOrThrow(GroupTable.Column.GNAME));
            if (!TextUtils.isEmpty(gName)) {
                CustomLog.d(TAG, "查到name：" + gName);
                return gName;
            }
            //获取默认的群名，群主放首位
            ArrayList<String> nameList = new ArrayList<String>();
            String owner = getGroupManager(gid);
            GroupMemberBean bean = queryGroupMember(gid, owner);
            if (bean != null && bean.isMember()) {
                nameList.add(bean.getDispName());
            }
            do {
                String nube = cursor.getString(
                    cursor.getColumnIndexOrThrow(GroupMemberTable.Column.NUBE_NUMBER));
                if (!nube.equals(owner)) {
                    String nameTxt = ShowNameUtil.getShowName(ShowNameUtil.getNameElement(
                        cursor.getString(cursor.getColumnIndexOrThrow(NubeFriendColumn.NAME)),
                        cursor.getString(
                            cursor.getColumnIndexOrThrow(GroupMemberTable.Column.NICK_NAME)),
                        cursor.getString(
                            cursor.getColumnIndexOrThrow(GroupMemberTable.Column.PHONE_NUM)),
                        nube));
                    nameList.add(nameTxt);
                }
                if (nameList.size() >= 3) {
                    break;
                }
            } while (cursor.moveToNext());

            name = list2String(nameList, '、');
            CustomLog.d(TAG, "拼接name=" + name);
        } catch (Exception e) {
            CustomLog.e(TAG, "Exception:" + e.toString());
            name = getString(R.string.groupChat);
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return name;
    }


    /**
     * 根据群号，查询该群的名称(设置聊天页面的item用的)
     *
     * @param gid 群号
     * @return 群的名称
     */
    public String getGroupNameByGidTitle(String gid) {
        CustomLog.d(TAG, "getGroupNameByGid begin");
        Cursor cursor = null;
        Uri uri = Uri.withAppendedPath(ProviderConstant.MEDICAL_URI,
            "GROUP_3_MEMBERS_NAME_BY_GID" + "/" + gid);
        String name = "";
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor == null || cursor.getCount() == 0) {
                CustomLog.d(TAG, "未查到");
                return getString(R.string.groupChat);
            }
            cursor.moveToFirst();
            String gName = cursor.getString(cursor.getColumnIndexOrThrow(GroupTable.Column.GNAME));
            if (!TextUtils.isEmpty(gName)) {
                CustomLog.d(TAG, "查到name：" + gName);
                return gName;
            }
            //获取默认的群名，群主放首位
            ArrayList<String> nameList = new ArrayList<String>();
            String owner = getGroupManager(gid);
            GroupMemberBean bean = queryGroupMember(gid, owner);
            if (bean != null && bean.isMember()) {
                nameList.add(bean.getDispName());
            }
            do {
                String nube = cursor.getString(
                    cursor.getColumnIndexOrThrow(GroupMemberTable.Column.NUBE_NUMBER));
                if (!nube.equals(owner)) {
                    String nameTxt = ShowNameUtil.getShowName(ShowNameUtil.getNameElement(
                        cursor.getString(cursor.getColumnIndexOrThrow(NubeFriendColumn.NAME)),
                        cursor.getString(
                            cursor.getColumnIndexOrThrow(GroupMemberTable.Column.NICK_NAME)),
                        cursor.getString(
                            cursor.getColumnIndexOrThrow(GroupMemberTable.Column.PHONE_NUM)),
                        nube));
                    nameList.add(nameTxt);
                }
                if (nameList.size() >= 3) {
                    break;
                }
            } while (cursor.moveToNext());

            //            name = list2String(nameList, '、');
            name = getString(R.string.groupChat);
            CustomLog.d(TAG, "拼接name=" + name);
        } catch (Exception e) {
            CustomLog.e(TAG, "Exception:" + e.toString());
            name = getString(R.string.groupChat);
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return name;
    }

    public String getOriginGroupNameByGid(String gid){
        CustomLog.d(TAG, "getOriginGroupNameByGid begin");
        Cursor cursor = null;
        Uri uri = Uri.withAppendedPath(ProviderConstant.MEDICAL_URI,
                "GROUP_3_MEMBERS_NAME_BY_GID" + "/" + gid);
        String name = "";
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor == null || cursor.getCount() == 0) {
                CustomLog.d(TAG, "未查到");
                return "";
            }
            cursor.moveToFirst();
            name =  cursor.getString(cursor.getColumnIndexOrThrow(GroupTable.Column.GNAME));
        } catch (Exception e) {
            CustomLog.e(TAG, "Exception:" + e.toString());
            name = "";
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return name;
    }

    /**
     * 查询指定的群聊信息
     *
     * @param gid 群聊id
     * @return 群聊信息
     */
    public GroupBean queryGroup(String gid) {
        if (TextUtils.isEmpty(gid)) {
            return null;
        }

        Cursor cursor = null;
        GroupBean bean = null;

        try {
            cursor = getContentResolver().query(
                ProviderConstant.NETPHONE_GROUP_URI,
                GroupTable.SELECT_COLUMNS, GroupTable.Column.GID + " = ? ",
                new String[] { gid }, null);
            if (cursor != null && cursor.moveToFirst()) {
                bean = new GroupBean();
                bean.setId(cursor.getInt(0));
                bean.setGid(cursor.getString(1));
                bean.setgName(cursor.getString(2));
                bean.setHeadUrl(cursor.getString(3));
                bean.setMgrNube(cursor.getString(4));
                bean.setCreateTime(cursor.getString(5));
            }
        } catch (Exception e) {
            CustomLog.e(TAG, "queryGroup exception" + e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return bean;
    }


    /**
     * 查询群聊成员信息
     *
     * @param gid 群聊id
     * @param nubeNum 成员nube号码
     * @return 成员信息
     */
    public GroupMemberBean queryGroupMember(String gid, String nubeNum) {
        if (TextUtils.isEmpty(gid) || TextUtils.isEmpty(nubeNum)) {
            return null;
        }

        Cursor cursor = null;

        Uri queryUri = Uri.withAppendedPath(ProviderConstant.MEDICAL_URI,
            "QUERY_GROUP_MEMBER_ITEM/" + gid + "/" + nubeNum);

        try {
            cursor = getContentResolver().query(queryUri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                GroupMemberBean bean = new GroupMemberBean();
                bean.setId(cursor.getInt(0));
                bean.setGid(cursor.getString(1));
                bean.setMid(cursor.getString(2));
                bean.setUid(cursor.getString(3));
                bean.setNubeNum(cursor.getString(4));
                bean.setPhoneNum(cursor.getString(5));
                bean.setNickName(cursor.getString(6));
                bean.setGroupNick(cursor.getString(7));
                bean.setShowName(cursor.getString(8));
                bean.setHeadUrl(cursor.getString(9));
                bean.setRemoved(cursor.getInt(10));
                bean.setName(cursor.getString(11));
                bean.setGender(cursor.getInt(12));
                return bean;
            }
        } catch (Exception e) {
            CustomLog.e(TAG, "queryGroupMember exception" + e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return null;
    }


    /***
     * @param list 字符串数组
     * @param separator 分隔符
     * @return 组合后的字符串
     * @Description: 将字符串数组用分隔符组成一个字符串
     */
    public String list2String(ArrayList<String> list, char separator) {
        if (list == null || list.size() == 0) {
            return "";
        }
        StringBuffer buf = new StringBuffer();
        int i = 0;
        int size = list.size();
        for (i = 0; i < size - 1; i++) {
            buf.append(list.get(i));
            buf.append(separator);
        }
        buf.append(list.get(size - 1));
        return buf.toString();
    }


    /**
     * 查询群聊成员信息
     *
     * @param gid 群聊id
     * @return 群聊成员信息
     */
    public LinkedHashMap<String, GroupMemberBean> queryGroupMembers(String gid) {
        if (TextUtils.isEmpty(gid)) {
            return null;
        }

        Cursor cursor = null;
        LinkedHashMap<String, GroupMemberBean> memberBeanMap =
            new LinkedHashMap<String, GroupMemberBean>();

        Uri queryUri = Uri.withAppendedPath(ProviderConstant.MEDICAL_URI,
            "QUERY_GROUP_MEMBERS/" + gid);

        try {
            cursor = getContentResolver().query(queryUri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    GroupMemberBean bean = new GroupMemberBean();
                    bean.setId(cursor.getInt(0));
                    bean.setGid(cursor.getString(1));
                    bean.setMid(cursor.getString(2));
                    bean.setUid(cursor.getString(3));
                    bean.setNubeNum(cursor.getString(4));
                    bean.setPhoneNum(cursor.getString(5));
                    bean.setNickName(cursor.getString(6));
                    bean.setGroupNick(cursor.getString(7));
                    bean.setShowName(cursor.getString(8));
                    bean.setHeadUrl(cursor.getString(9));
                    bean.setRemoved(cursor.getInt(10));
                    bean.setName(cursor.getString(11));
                    bean.setGender(cursor.getInt(12));
                    memberBeanMap.put(bean.getNubeNum(), bean);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            CustomLog.e(TAG, "queryGroupMembers exception" + e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return memberBeanMap;
    }


    /**
     * 查询群成员人数
     */
    public int queryGroupMemberCnt(String gid) {
        if (TextUtils.isEmpty(gid)) {
            return 0;
        }
        Cursor cursor = null;
        Uri uri = Uri.withAppendedPath(ProviderConstant.MEDICAL_URI,
            "QUERY_GROUP_MEMBER_CNT/" + gid);
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } catch (Exception e) {
            CustomLog.e(TAG, "Exception" + e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return 0;
    }


    /**
     * 查询是否是群组成员
     *
     * @param nubeNum 成员nube号码
     * @param gid 群聊id
     * @return 是否是成员
     */
    public boolean isGroupMember(String gid, String nubeNum) {
        if (TextUtils.isEmpty(nubeNum) || TextUtils.isEmpty(gid)) {
            return false;
        }

        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(
                ProviderConstant.NETPHONE_GROUP_MEMBER_URI,
                new String[] { GroupMemberTable.Column.REMOVED },
                GroupMemberTable.Column.GID + " = ? and "
                    + GroupMemberTable.Column.NUBE_NUMBER + " = ?",
                new String[] { gid, nubeNum }, null);
            if (cursor != null && cursor.moveToFirst()) {
                if (cursor.getInt(0) == GroupMemberTable.REMOVED_FALSE) {
                    return true;
                }
            }
        } catch (Exception e) {
            CustomLog.e(TAG, "Exception" + e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return false;
    }


    /**
     * 查询全部群聊（已产生会话的，且自身未被移除的群聊）
     *
     * @return 全部群聊
     */
    public List<ContactFriendBean> queryAllGroup() {
        Cursor cursor = null;
        List<ContactFriendBean> values = null;

        Uri queryUri = Uri.withAppendedPath(ProviderConstant.MEDICAL_URI, "QUERY_THREAD_GROUPS/"
            + AccountManager.getInstance(MedicalApplication.getContext()).getNube());

        try {
            cursor = getContentResolver().query(queryUri,
                null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                values = new ArrayList<ContactFriendBean>();

                do {
                    String threadId = cursor.getString(3);
                    if (TextUtils.isEmpty(threadId)) {
                        // 未产生会话的群聊
                        continue;
                    }

                    String removedStr = cursor.getString(4);
                    if (TextUtils.isEmpty(removedStr)) {
                        // 自己不属于群聊成员
                        continue;
                    } else {
                        if ((GroupMemberTable.REMOVED_TRUE + "").equals(removedStr)) {
                            // 已被移出群聊
                            continue;
                        }
                    }

                    ContactFriendBean po = new ContactFriendBean();

                    String gid = cursor.getString(0);
                    String name = cursor.getString(1);
                    String headUrl = cursor.getString(2);

                    if (TextUtils.isEmpty(name)) {
                        // 群名称为空的场合，获取默认群名称
                        name = getGroupNameByGid(gid);
                    }

                    po.setNubeNumber(gid);
                    po.setContactId(gid);
                    po.setName(name);
                    po.setHeadUrl(headUrl);
                    values.add(po);

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            CustomLog.e(TAG, "queryAllGroup exception" + e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

        return values;
    }


    /**
     * 本地是否存在指定的群聊
     *
     * @param gid 群聊id
     */
    public boolean existGroup(String gid) {
        if (TextUtils.isEmpty(gid)) {
            CustomLog.d(TAG, "existGroup false1");
            return false;
        }
        Cursor cursor = null;
        try {
            cursor = getContentResolver()
                .query(ProviderConstant.NETPHONE_GROUP_URI,
                    new String[] { GroupTable.Column.ID },
                    GroupTable.Column.GID + " = ? ",
                    new String[] { gid }, null);
            if (cursor != null && cursor.moveToFirst()) {
                CustomLog.d(TAG, "true");
                return true;
            }
        } catch (Exception e) {
            CustomLog.d(TAG, "existGroup exception" + e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        CustomLog.d(TAG, "false2");
        return false;
    }


    /**
     * 创建群组信息
     *
     * @param gid 群聊id
     * @param gName 群聊名称
     * @param headUrl 群聊头像
     * @param mgrNube 管理员nube号
     * @param createTime 创建时间 YYYY-MM-DD HH:mm:ss
     */
    public void createGroup(String gid, String gName, String headUrl,
                            String mgrNube, String createTime) {
        if (TextUtils.isEmpty(gid)) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(GroupTable.Column.GID, gid);
        values.put(GroupTable.Column.GNAME, gName);
        values.put(GroupTable.Column.HEAD_URL, headUrl);
        values.put(GroupTable.Column.MGR_NUBE, mgrNube);
        values.put(GroupTable.Column.CREATE_TIME, createTime);

        getContentResolver()
            .insert(ProviderConstant.NETPHONE_GROUP_URI, values);
    }


    public boolean updateGroupHeadUrl(String gid, String headurl) {
        if (TextUtils.isEmpty(gid)) {
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(GroupTable.Column.HEAD_URL, headurl);
        int count = getContentResolver().update(
            ProviderConstant.NETPHONE_GROUP_URI, values,
            GroupTable.Column.GID + " = ? ", new String[] { gid });
        return count > 0 ? true : false;
    }


    /**
     * 更新指定群聊名称
     *
     * @param gid 群聊id
     * @param gName 群聊名称
     */
    public void updateGroupName(String gid, String gName) {
        if (TextUtils.isEmpty(gid)) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(GroupTable.Column.GNAME, gName);

        getContentResolver().update(ProviderConstant.NETPHONE_GROUP_URI,
            values, GroupTable.Column.GID + " = ? ", new String[] { gid });
    }


    /**
     * 群聊成员存在的场合，先删除，再插入该成员信息；
     * 不存在的场合，直接插入成员信息；
     * 保证成员被加入到数据表的顺序
     */
    public void addMemberAfterDelete(List<GroupMemberBean> users) {
        if (users == null || users.size() == 0) {
            return;
        }
        String gid = "";
        String nube = "";
        for (GroupMemberBean bean : users) {
            gid = bean.getGid();
            nube = bean.getNubeNum();
            delMemberByNube(nube, gid);
        }

        addGroupMember(users);
    }


    /**
     * 删除指定群聊的指定nube号码的成员
     *
     * @param nubeNum 成员nube号码
     * @param gid 群聊id
     */
    public void delMemberByNube(String nubeNum, String gid) {
        if (TextUtils.isEmpty(nubeNum) || TextUtils.isEmpty(gid)) {
            return;
        }

        int count = getContentResolver().delete(
            ProviderConstant.NETPHONE_GROUP_MEMBER_URI,
            GroupMemberTable.Column.GID + " = ? and "
                + GroupMemberTable.Column.NUBE_NUMBER + " = ? ",
            new String[] { gid, nubeNum });

        CustomLog.d(TAG, "delMemberByNube gid=" + gid + " nube=" + nubeNum + " count=" + count);
    }


    /**
     * 添加群聊成员
     *
     * @param users 群聊成员
     */
    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public void addGroupMember(List<GroupMemberBean> users) {
        if (users == null || users.size() == 0) {
            return;
        }

        ArrayList<ContentProviderOperation> cpoList = new ArrayList<ContentProviderOperation>();

        for (GroupMemberBean bean : users) {
            ContentValues values = new ContentValues();
            values.put(GroupMemberTable.Column.GID, bean.getGid());
            values.put(GroupMemberTable.Column.UID, bean.getUid());
            values.put(GroupMemberTable.Column.MID, bean.getMid());
            values.put(GroupMemberTable.Column.NUBE_NUMBER, bean.getNubeNum());
            values.put(GroupMemberTable.Column.PHONE_NUM, bean.getPhoneNum());
            values.put(GroupMemberTable.Column.NICK_NAME, bean.getNickName());
            values.put(GroupMemberTable.Column.GROUP_NICK, bean.getGroupNick());
            values.put(GroupMemberTable.Column.HEAD_URL, bean.getHeadUrl());
            values.put(GroupMemberTable.Column.SHOW_NAME, bean.getShowName());
            values.put(GroupMemberTable.Column.GENDER, bean.getGender());
            values.put(GroupMemberTable.Column.REMOVED, bean.getRemoved());
            cpoList.add(ContentProviderOperation
                .newInsert(ProviderConstant.NETPHONE_GROUP_MEMBER_URI)
                .withValues(values).build());
        }

        try {
            getContentResolver()
                .applyBatch(ProviderConstant.AUTHORITY, cpoList);
            CustomLog.d(TAG, "批量插入群聊成员成功");
        } catch (RemoteException e) {
            CustomLog.e(TAG, "applyBatch error" + e.toString());
        } catch (OperationApplicationException e) {
            CustomLog.e(TAG, "applyBatch error" + e.toString());
        }
    }


    /**
     * 群聊成员存在的场合，更新成员信息；不存在的场合，插入成员信息
     *
     * @param users 群聊成员信息
     */
    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public void addOrUpdateMember(List<GroupMemberBean> users) {
        CustomLog.d(TAG,"addOrUpdateMember call");
        if (users == null || users.size() == 0) {
            return;
        }

        ArrayList<ContentProviderOperation> cpoList = new ArrayList<ContentProviderOperation>();
        Uri uri = Uri.withAppendedPath(ProviderConstant.MEDICAL_URI,
            "GROUP_MEMBER_ADD_UPDATE");

        for (GroupMemberBean bean : users) {
            ContentValues values = new ContentValues();
            values.put(GroupMemberTable.Column.GID, bean.getGid());
            values.put(GroupMemberTable.Column.UID, bean.getUid());
            values.put(GroupMemberTable.Column.MID, bean.getMid());
            values.put(GroupMemberTable.Column.NUBE_NUMBER, bean.getNubeNum());
            values.put(GroupMemberTable.Column.PHONE_NUM, bean.getPhoneNum());
            values.put(GroupMemberTable.Column.NICK_NAME, bean.getNickName());
            values.put(GroupMemberTable.Column.GROUP_NICK, bean.getGroupNick());
            values.put(GroupMemberTable.Column.HEAD_URL, bean.getHeadUrl());
            values.put(GroupMemberTable.Column.SHOW_NAME, bean.getShowName());
            values.put(GroupMemberTable.Column.GENDER, bean.getGender());
            values.put(GroupMemberTable.Column.REMOVED, bean.getRemoved());
            cpoList.add(ContentProviderOperation.newUpdate(uri)
                .withValues(values).build());
        }

        try {
            getContentResolver()
                .applyBatch(ProviderConstant.AUTHORITY, cpoList);
            CustomLog.d(TAG, "批量插入或更新群聊成员成功");
        } catch (RemoteException e) {
            CustomLog.e(TAG, "applyBatch error" + e.toString());
        } catch (OperationApplicationException e) {
            CustomLog.e(TAG, "applyBatch error" + e.toString());
        }
    }


    /**
     * 移出成员（收到移出成员事件及主动移出成员时调用，将removed状态置为被移出）
     *
     * @param gid 群聊id
     * @param nubeNum 成员nube号码
     */
    public void setMemberRemoved(String gid, String nubeNum) {
        if (TextUtils.isEmpty(gid) || TextUtils.isEmpty(nubeNum)) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(GroupMemberTable.Column.REMOVED, GroupMemberTable.REMOVED_TRUE);

        getContentResolver().update(ProviderConstant.NETPHONE_GROUP_MEMBER_URI,
            values, GroupMemberTable.Column.GID + " = ? and "
                + GroupMemberTable.Column.NUBE_NUMBER + " = ? ",
            new String[] { gid, nubeNum });
    }


    /**
     * 删除指定群聊
     *
     * @param gid 群聊id
     */
    public void delGroup(String gid) {
        if (TextUtils.isEmpty(gid)) {
            return;
        }

        getContentResolver().delete(ProviderConstant.NETPHONE_GROUP_URI,
            GroupTable.Column.GID + " = ? ", new String[] { gid });
    }


    /**
     * 删除指定群聊的全部成员
     *
     * @param gid 群聊id
     */
    public void delMembersByGid(String gid) {
        if (TextUtils.isEmpty(gid)) {
            return;
        }

        getContentResolver().delete(ProviderConstant.NETPHONE_GROUP_MEMBER_URI,
            GroupMemberTable.Column.GID + " = ? ", new String[] { gid });
    }


    /**
     * 更新群聊管理员
     *
     * @param gid 群聊id
     * @param nubeNumber 管理员nube号码
     * @return 成功or失败
     */
    public boolean updateGroupManager(String gid, String nubeNumber) {
        if (TextUtils.isEmpty(gid)) {
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(GroupTable.Column.MGR_NUBE, nubeNumber);
        int count = getContentResolver().update(
            ProviderConstant.NETPHONE_GROUP_URI, values,
            GroupTable.Column.GID + " = ? ", new String[] { gid });
        return count > 0;
    }


    /**
     * 移出所有成员
     *
     * @param gid 群聊id
     */
    public void setAllMemberRemoved(String gid) {
        CustomLog.d(TAG,"setAllMemberRemoved 移除所有成员");
        if (TextUtils.isEmpty(gid)) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(GroupMemberTable.Column.REMOVED, GroupMemberTable.REMOVED_TRUE);

        getContentResolver().update(ProviderConstant.NETPHONE_GROUP_MEMBER_URI,
            values, GroupMemberTable.Column.GID + " = ? ",
            new String[] { gid });
    }


    /**
     * 更新指定群信息
     *
     * @param gid 群聊id
     */
    public void updateGroup(String gid, String gName, String headUrl,
                            String mgrNube, String createTime) {
        if (TextUtils.isEmpty(gid)) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(GroupTable.Column.GNAME, gName);
        values.put(GroupTable.Column.HEAD_URL, headUrl);
        values.put(GroupTable.Column.MGR_NUBE, mgrNube);
        values.put(GroupTable.Column.CREATE_TIME, createTime);
        getContentResolver().update(ProviderConstant.NETPHONE_GROUP_URI,
            values, GroupTable.Column.GID + " = ? ", new String[] { gid });
    }


    /*
        * 查询对应gid下的全部群成员
        */
    public ArrayList<GroupMemberBean> queryAllGroupMembers(String gid, String nubeNumber) {
        if (TextUtils.isEmpty(gid)) {
            return null;
        }

        Cursor cursor = null;
        ArrayList<GroupMemberBean> memberBeanMap =
            new ArrayList<GroupMemberBean>();

        Uri queryUri = Uri.withAppendedPath(ProviderConstant.MEDICAL_URI,
            "QUERY_GROUP_MEMBERS/" + gid);

        try {
            cursor = getContentResolver().query(queryUri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    GroupMemberBean bean = new GroupMemberBean();
                    bean.setId(cursor.getInt(0));
                    bean.setGid(cursor.getString(1));
                    bean.setMid(cursor.getString(2));
                    bean.setUid(cursor.getString(3));
                    bean.setNubeNum(cursor.getString(4));
                    bean.setPhoneNum(cursor.getString(5));
                    bean.setNickName(cursor.getString(6));
                    bean.setGroupNick(cursor.getString(7));
                    bean.setShowName(cursor.getString(8));
                    bean.setHeadUrl(cursor.getString(9));
                    bean.setRemoved(cursor.getInt(10));
                    bean.setName(cursor.getString(11));
                    bean.setGender(cursor.getInt(12));
                    if (!bean.getNubeNum().equals(nubeNumber)) {
                        memberBeanMap.add(bean);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            LogUtil.e("queryGroupMembers exception", e);
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return memberBeanMap;
    }


    /**
     * 查询群聊成员nube
     *
     * @param gid 群聊id
     * @return 群聊成员nube
     */
    public ArrayList<String> queryGroupNumbers(String gid) {
        if (TextUtils.isEmpty(gid)) {
            return null;
        }

        Cursor cursor = null;
        ArrayList<String> memberBeanMap = new ArrayList<String>();

        Uri queryUri = Uri.withAppendedPath(ProviderConstant.MEDICAL_URI,
            "QUERY_GROUP_MEMBERS/" + gid);

        try {
            cursor = getContentResolver().query(queryUri, null, null, null,
                null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    memberBeanMap.add(cursor.getString(4));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            LogUtil.e("queryGroupMembers exception", e);
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return memberBeanMap;
    }



}
