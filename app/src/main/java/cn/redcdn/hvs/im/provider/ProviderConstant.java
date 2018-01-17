package cn.redcdn.hvs.im.provider;

import android.net.Uri;

import cn.redcdn.hvs.contacts.contact.hpucontact.DtNoticesTable;
import cn.redcdn.hvs.im.column.FriendRelationTable;
import cn.redcdn.hvs.im.column.GroupMemberTable;
import cn.redcdn.hvs.im.column.GroupTable;
import cn.redcdn.hvs.im.column.NewFriendTable;
import cn.redcdn.hvs.im.column.NoticesTable;
import cn.redcdn.hvs.im.column.NubeFriendColumn;
import cn.redcdn.hvs.im.column.NumberCacheTable;
import cn.redcdn.hvs.im.column.StrangerMessageTable;
import cn.redcdn.hvs.im.column.ThreadsTable;

/**
 * Desc
 * Created by wangkai on 2017/2/23.
 */

public class ProviderConstant {

    // 授权码
    public static final String AUTHORITY = "cn.redcdn.android.hvs.provider";
    // url节点头
    public static final Uri MEDICAL_URI = Uri.parse("content://" + AUTHORITY);
    // 用户参数授权码
    public static final String SETTING_AUTHORITY = "cn.redcdn.android.hvs.setting.provider";
    // 云电话url节点头
    public static final Uri SETTING_URI = Uri.parse("content://"
        + SETTING_AUTHORITY);
    public static final Uri NETPHONE_NOTICE_URI = Uri.withAppendedPath(
        MEDICAL_URI, NoticesTable.TABLENAME);
    public static final Uri NETPHONE_HPU_NOTICE_URI = Uri.withAppendedPath(MEDICAL_URI, DtNoticesTable.TABLE_NAME);
    public static final Uri NETPHONE_NEWFRIEND_URI = Uri.withAppendedPath(
        MEDICAL_URI, NewFriendTable.TABLENAME);
    public static final Uri NETPHONE_THREADS_URI = Uri.withAppendedPath(
        MEDICAL_URI, ThreadsTable.TABLENAME);
    public static final Uri NETPHONE_NUBEFRIEND_URI = Uri.withAppendedPath(
        MEDICAL_URI, NubeFriendColumn.TABLENAME);
    public static final Uri NETPHONE_GROUP_URI = Uri.withAppendedPath(
        MEDICAL_URI, GroupTable.TABLENAME);
    public static final Uri NETPHONE_GROUP_MEMBER_URI = Uri.withAppendedPath(
        MEDICAL_URI, GroupMemberTable.TABLENAME);
    // 手机号与视频号的映射缓存表
    public static final Uri NETPHONE_NUMBERCACHE_URI = Uri.withAppendedPath(
        MEDICAL_URI, NumberCacheTable.TABLENAME);
    //好友关系表
    public static final Uri Friend_Relation_URI = Uri.withAppendedPath(
        MEDICAL_URI, FriendRelationTable.TABLENAME);
    //陌生人消息表
    public static final Uri Strange_Message_URI = Uri.withAppendedPath(
        MEDICAL_URI, StrangerMessageTable.TABLENAME);
    //共享通讯录表
    public static final Uri Share_Contact_URI = Uri.withAppendedPath(
            MEDICAL_URI, "t_hpu_friends");

    //    public static final Uri NETPHONE_ACTIVITY_URI = Uri.withAppendedPath(
    //            MEDICAL_URI, ActivityTable.TABLENAME);
    //    public static final Uri NETPHONE_CALLRECORDS_URI = Uri.withAppendedPath(
    //            MEDICAL_URI, CallRecordsColumn.TABLENAME);
    //    public static final Uri NETPHONE_CALLRECORDS_GET_ISNEW_MISS_URI = Uri.withAppendedPath(
    //            NETPHONE_CALLRECORDS_URI, CallRecordsColumn.ISNEW);
    //    public static final Uri NETPHONE_FAMILY_NUMBER_URI = Uri.withAppendedPath(
    //            MEDICAL_URI, FamilyColumn.TABLENAME);
    //
    //    public static final Uri NETPHONE_REPLY_MSG_URI = Uri.withAppendedPath(
    //            MEDICAL_URI, ReplyMsgColumn.TABLENAME);
    //    public static final Uri NETPHONE_DEVICE_URI = Uri.withAppendedPath(
    //            MEDICAL_URI, DeviceColumn.TABLENAME);
    //    // add Alert Uri--wxy-15-3-27
    //    public static final Uri NETPHONE_ALARM_URI = Uri.withAppendedPath(
    //            MEDICAL_URI, AlarmTable.TABLENAME);
    //    // 手机号与视频号的映射缓存表
    //    public static final Uri NETPHONE_NUMBERCACHE_URI = Uri.withAppendedPath(
    //            MEDICAL_URI, NumberCacheTable.TABLENAME);
    //    public static final Uri NETPHONE_PUBLIC_NO_CACHE_URI = Uri.withAppendedPath(
    //            MEDICAL_URI, PublicNOCacheTable.TABLENAME);
    //
    //    public static final Uri NETPHONE_PUBLIC_NO_HISTORY_URI = Uri.withAppendedPath(
    //            MEDICAL_URI, PublicNOHistoryTable.TABLENAME);
    //
    //    public static final Uri NETPHONE_SEARCH_HIS_URI = Uri.withAppendedPath(
    //            MEDICAL_URI, SearchHistoryTable.TABLENAME);
    //    public static final Uri NETPHONE_MEET_HIS_URI = Uri.withAppendedPath(MEDICAL_URI, MeetHistoryTable.TABLENAME);
}
