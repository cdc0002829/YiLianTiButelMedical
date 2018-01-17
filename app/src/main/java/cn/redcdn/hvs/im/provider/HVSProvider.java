package cn.redcdn.hvs.im.provider;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.contacts.contact.hpucontact.DtNoticesTable;
import cn.redcdn.hvs.database.DBConstant;
import cn.redcdn.hvs.database.DatabaseManager;
import cn.redcdn.hvs.im.column.CollectionTable;
import cn.redcdn.hvs.im.column.FriendRelationTable;
import cn.redcdn.hvs.im.column.GroupMemberTable;
import cn.redcdn.hvs.im.column.GroupTable;
import cn.redcdn.hvs.im.column.NoticesTable;
import cn.redcdn.hvs.im.column.NubeFriendColumn;
import cn.redcdn.hvs.im.column.StrangerMessageTable;
import cn.redcdn.hvs.im.column.ThreadsTable;
import cn.redcdn.log.CustomLog;
import java.util.ArrayList;


/**
 * Desc
 * Created by wangkai on 2017/2/23.
 */
public class HVSProvider extends ContentProvider {

    private final String TAG = "HVSProvider";

    private SQLiteDatabase db;
    public static final String AUTHORITY = ProviderConstant.AUTHORITY;
    public static final Uri CONTENT_URL = ProviderConstant.MEDICAL_URI;

    /** TODO:联系人 start */
    private static final int NET_PHONE_LINKMANS_LIST = 101;
    private static final int NET_PHONE_LINKMANS_ITEM = 102;

    private static final int UPLOAD_GET_COUNT = 201;
    private static final int QUERY_NUBE_FRIENDS_LIST = 202;
    private static final int UPDATE_SYNC_STATUS = 203;
    private static final int GET_MAX_TIMESTAMP = 204;
    private static final int QUERY_LINKMAN_ID = 205;
    private static final int DELETE_LINKMAN = 206;
    private static final int UPDATE_LINKMAN_ITEM = 207;
    private static final int INSERT_LINKMAN_ITEM = 208;
    private static final int UPDATE_LINKMAN_TIME = 209;
    private static final int LINKMAN_LIST = 210;
    /** 开放给第三方，应用内请勿使用和修改 */
    private static final int INSERT_BUTEL_LINKMAN = 211;

    private static final int QUERY_FIND_LINKMAN_COUNT = 401;
    private static final int QUERY_VIDEO_LINKMAN = 402;
    private static final int QUERY_FIND_LINKMAN = 403;
    private static final int QUERY_APP_LINKMAN_NUMBER = 404;
    private static final int GET_APP_LINKMAN_DATA = 405;
    private static final int GET_APP_LINKMAN_DATA_NEW = 449;
    private static final int UPDATE_CONTACT_SORTKEY_INFO = 406; // 更新联系人排序信息
    private static final int UPDATE_MULT_CONTACT_SORTKEY_INFO = 407; // 更新多个联系人的排序信息
    private static final int UPDATE_AUTH_STATUS = 408;
    private static final int QUERY_LINKMAN_ITEM = 409;
    private static final int UPDATE_ONLINE_STATUS_TIME = 410;
    private static final int QUERY_MAX_SORT = 411;
    private static final int BATCH_UPDATE_ONLINE_ITEM = 412;
    private static final int MATCH_NAME_BYNUMBER_ITEM = 413; // 公共item
    private static final int QUERY_ONLINE_COUNT_INFO = 414;
    private static final int QUERY_NEW_FRIEND_INFO = 415;//
    private static final int QUERY_NEW_FRIEND_COUNT_INFO = 416;
    private static final int DEL_NUBE_CONTACT_BY_ID = 417;// 删除联系人：逻辑删除
    private static final int UPDATE_CONTACT_INFO = 418;// 更新联系人信息
    private static final int MATCH_CONTACT_BYNUMBER_ITEM = 418;// 根据号码获取匹配联系人
    private static final int MATCH_CONTACT_BYNUBENUMBER_ITEM = 419; // 根据nube号码获取匹配联系人
    private static final int QUERY_APP_LINKMAN_COUNT = 420;
    private static final int QUERY_NUBE_FRIEND_INFO = 421;//查询所有的nube好友
    private static final int QUERY_VISIBLE_NUBE_FRIEND_INFO = 426;//查询可见的nube好友
    private static final int QUERY_NUBE_FRIEND_RECORD = 438;
    private static final int QUERY_NEW_FRIEND_RECORD = 439;
    private static final int UPDATE_NUBE_FRIEND_INFO = 422;
    private static final int QUERY_LINKMAN_BY_NUBENUMBER = 423;
    private static final int QUERY_LINKMAN_KEY = 424;
    private static final int QUERY_LINKMAN_BY_PHONENUMBER = 425;
    private static final int QUERY_EMPTY_LINKMAN_ITEM = 431;
    private static final int UPDATE_LINKMAN_STATUS_ITEM = 432;
    private static final int DELETE_LINKMAN_STATUS_ITEM = 433;
    private static final int QUERY_VIDEO_LINKMAN_BY_FULLPYM = 434;
    private static final int QUERY_SIMPLE_LINKMAN = 435;
    private static final int QUERY_VIDEO_LINKMAN_BY_FULLPYM_FOR_XIN = 436;// 星标用户数据查询
    private static final int QUERY_INFO_ROR_FAMILYE_BY_NUBENUMBER = 437;
    private static final int GET_APP_LINKMAN_LOCAL_FIND_DATA_NEW = 450;
    private static final int GET_APP_LINKMAN_NUMBER_DATA_NEW = 451;
    private static final int GET_APP_LINKMAN_NEW = 452;
    /** 开放给第三方，应用内请勿使用和修改 */
    private static final int QUERY_BUTEL_LINKMAN = 460;
    private static final int QUERY_BUTEL_LINKMAN_BY_NUBENUMBER = 461;

    /** TODO:联系人 end */

    /** 动态消息 */
    private static final int NET_PHONE_NOTICE_LIST = 301;
    private static final int NET_PHONE_NOTICE_ITEM = 302;
    // private static final int QUERY_NOTICES = 303;
    /** 统计名片信息 */
    private static final int COUNT_VCARD_NOTICES = 304;
    private static final int COUNT_NEW_NOTICES = 305;
    private static final int UPDATE_NEW_NOTICES_READ = 306;
    // private static final int QUERY_CONVST_NOTICES = 307;
    private static final int QUERY_PAGE_NOTICES = 308;
    private static final int QUERY_ALL_NOTICES = 309;
    private static final int QUERY_COND_NOTICES = 310;

    private static final int QUERY_UNREAD_NOTICES_COUNT = 311;
    private static final int QUERY_UNREAD_NOTICES_COUNT_NODISTURB = 312;

    // new friend table id define
    private static final int NET_PHONE_NEWFRIEND_LIST = 501;
    private static final int NET_PHONE_NEWFRIEND_ITEM = 502;
    /*** unread new friend----add on 2015-12-10 */
    private static final int GET_UNREAD_NEW_FRIEND = 507;
    private static final int DELETE_FRIEND_ITEM = 555;
    // @lihs 2013.11.19
    private static final int NET_PHONE_NUBEFRIEND = 504;
    private static final int QUERY_VCARD_CURSOR = 508;
    private static final int DELETE_VCARD_MSG = 509;
    // TODO @lihs 2013.11.17 活动表相关db的操作
    private static final int NET_PHONE_ACTIVITY_ITEM = 503;
    private static final int UPDATE_FRIEND_ITEM = 504;

    // call records table id define
    private static final int CALL_RECORD_LIST = 505;
    private static final int CALL_RECORD_ITEM = 506;

    private static final int DELETE_NEWFRIEND_TABLE = 600;
    private static final int QUERY_CONTACT_RECORD = 601;
    private static final int CHECK_CONTACT_RECORD = 602;
    private static final int CHECK_CONTACT_ISONLINE = 603;

    private static final int QUERY_SOURCE_ITEM = 611;

    private static final int ADD_FAMILY_NUMBER = 701;
    // private static final int ADD_DISTINCT_FAMILY_NUMBER = 702;
    private static final int UPDATE_FAMILY_NUMBER = 703;
    private static final int DELETE_FAMILY_NUMBER = 704;
    private static final int QUERY_FAMILY_NUMBER = 705;

    private static final int QUERY_FAMILY_INFO_BY_NUBENUMBER = 709;

    private static final int QUERY_NO_CONTACT_FAMILY_NUMBER = 706;
    private static final int QUERY_FAMILY_NUMBER_AND_NUBE_FRIEND_NAME = 707;
    private static final int UPDATE_FAMILY_2_NOTNEW = 708;
    private static final int UPDATE_FAMILY_ISNEW = 710;

    // 回复消息信息
    private static final int ADD_REPLYMSG = 801;
    private static final int DELETE_REPLYMSG = 802;
    private static final int QUERY_REPLYMSG = 803;

    // 会话表db的操作
    // 关联 会话表 和 消息表 查询出会话页面中的数据
    private static final int NETPHONE_THREADS_GETALL = 900;
    private static final int NETPHONE_THREADS_LIST = 901;
    private static final int NETPHONE_THREADS_ITEM = 902;
    private static final int NETPHONE_THREADS_GETALL_TOP = 904;

    // 我的设备表
    private static final int DEVICE_LIST = 1000;
    // private static final int ADD_MY_DEVICE_NUMBER = 1000;
    // private static final int UPDATE_MY_DEVICE_NUMBER = 1001;
    // private static final int DELETE_MY_DEVICE_NUMBER = 1002;
    // private static final int QUERY_MY_DEVICE_NUMBER = 1003;
    private static final int QUERY_MY_DEVICE_NUMBER_AND_NUBE_FRIEND_NAME = 1004;

    // 报警消息表
    private static final int ALARM_LIST = 1100;
    // private static final int ALARM_ITEM=1101;

    private static final int NUMBER_CACHE_LIST = 1200;
    private static final int NUMBER_CACHE_ITEM = 1201;

    /** 群聊 begin */
    private static final int GROUP_LIST = 1301;
    private static final int GROUP_MEMBER_LIST = 1302;
    private static final int GROUP_MEMBER_ADD_UPDATE = 1303;
    /** 群聊3个成员的名称组成默认群名称 */
    private static final int GROUP_3_MEMBERS_NAME_BY_GID = 1304;
    private static final int QUERY_GROUP_MEMBERS = 1307;
    private static final int QUERY_GROUP_MEMBER_ITEM = 1308;
    private static final int QUERY_GROUP_MEMBER_CNT = 1309;
    /** 查询全部群聊（已产生会话的，且自身未被移除的群聊） */
    private static final int QUERY_THREAD_GROUPS = 1310;
    /** 群聊 end */

    /*** 公众号begin ***/
    private static final int PUBLIC_NO_HISTORY = 1401;
    private static final int PUBLIC_NO_CACHE = 1402;
    private static final int SEARCH_HIS = 1403;
    /*** 公众号end***/

    /*** 会议首页历史记录搜索 ***/
    private static final int MEET_HIS = 1404;

    /** 收藏分享 相关 **/
    private static final int COLLECT_TABLE = 1500;

    private static final String query_all_collection_record = "query_all_collection_record";
    //连表查询所有收藏记录
    private static final int QUERY_ALL_COLLECTION_RECORD = 1501;//连表查询所有收藏记录
    public static final Uri URI_QUERY_ALL_COLLECTION_RECORD = Uri.withAppendedPath(
        ProviderConstant.MEDICAL_URI, query_all_collection_record);

    //好友关系表
    private static final int FRIEND_RELATION_TABLE = 1502;//好友关系插入
    private static final int FRIEND_RELATION_TABLE_INSERT = 1503;//好友关系插入
    private static final int FRIEND_RELATION_TABLE_DELETE = 1504;//好友关系删除
    private static final int FRIEND_RELATION_TABLE_UPDATE = 1505;//好友关系更新
    private static final int FRIEND_RELATION_TABLE_QUERY = 1506;//好友关系查询
    //陌生人消息表
    private static final int STRANGER_MESSAGE_TABLE = 1507;//陌生人消息表插入
    private static final int STRANGER_MESSAGE_TABLE_INSERT = 1508;//陌生人消息表插入
    private static final int STRANGER_MESSAGE_TABLE_DELETE = 1509;//陌生人消息表删除
    private static final int STRANGER_MESSAGE_TABLE_UPDATE = 1510;//陌生人消息表更新
    private static final int STRANGER_MESSAGE_TABLE_QUERY = 1511;//陌生人消息表查询
    private static final int STRANGER_MESSAGE_TABLE_QUERY_NOT_READ_MSG = 1512;//陌生人消息表查询
    private static final int T_FRIENDS_RELATION_ORDER_BY_UPDATE_TIME = 1513;//陌生人消息表查询

    //共享通讯录数据库表
    private static final int SHARE_INSERT_TABLE = 1514;//共享通讯录添加联系人数据
    private static final int SHARE_UPDATE_TABLE = 1515;//共享通讯录更新联系人数据
    private static final int SHARE_DELETE_TABLE = 1516;//共享通讯录删除联系人数据
    private static final int SHARE_QUERY_TABLE_BYHUBID = 1517;//共享通讯录查询 按照 医联体id
    private static final int SHARE_QUERY_TABLE_ALL = 1518;//共享通讯录 查询所有

    //会诊消息列表
    private static final int HPU_DT_NOTICE_LIST = 1519;//
    private static final int HPU_DT_NOTICE_ITEM = 1520;
    private static final int QUERY_ALL_DT_NOTICES = 1521;
    private static final int QUERY_COND_DT_NOTICES = 1522;
    private static final int QUERY_PAGE_DT_NOCTICES = 1523;
    private static final int QUERY_SIMPLE_DT_LINKMAN = 1524;
    private static final int QUERY_UNREAD_NOTICES_DT_COUNT = 1525;
    private static final int QUERY_UNREAD_NOTICES_DT_COUNT_NODISTURB = 1526;
    private static final int QUERY_NOTICES_DT_COUNT_T = 1527;

    private static final UriMatcher uriMatcher;
    private AccountManager manager;


    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH); // 常量UriMatcher.NO_MATCH表示不匹配任何路径的返回码(-1)。
        uriMatcher.addURI(AUTHORITY, "NET_PHONE_LINKMANS_LIST",
            NET_PHONE_LINKMANS_LIST);
        uriMatcher.addURI(AUTHORITY, "NET_PHONE_LINKMANS_ITEM",
            NET_PHONE_LINKMANS_ITEM);
        uriMatcher.addURI(AUTHORITY, "UPLOAD_GET_COUNT/*", UPLOAD_GET_COUNT);
        uriMatcher.addURI(AUTHORITY, "QUERY_NUBE_FRIENDS_LIST",
            QUERY_NUBE_FRIENDS_LIST);
        uriMatcher.addURI(AUTHORITY, "UPDATE_SYNC_STATUS", UPDATE_SYNC_STATUS);
        uriMatcher.addURI(AUTHORITY, "GET_MAX_TIMESTAMP/*", GET_MAX_TIMESTAMP);
        uriMatcher.addURI(AUTHORITY, "QUERY_LINKMAN_ID", QUERY_LINKMAN_ID);
        uriMatcher.addURI(AUTHORITY, NubeFriendColumn.TABLENAME, LINKMAN_LIST);
        uriMatcher.addURI(AUTHORITY, "DELETE_LINKMAN", DELETE_LINKMAN);
        uriMatcher
            .addURI(AUTHORITY, "UPDATE_LINKMAN_ITEM", UPDATE_LINKMAN_ITEM);
        uriMatcher
            .addURI(AUTHORITY, "INSERT_LINKMAN_ITEM", INSERT_LINKMAN_ITEM);
        uriMatcher
            .addURI(AUTHORITY, "INSERT_BUTEL_LINKMAN", INSERT_BUTEL_LINKMAN);
        uriMatcher
            .addURI(AUTHORITY, "UPDATE_LINKMAN_TIME", UPDATE_LINKMAN_TIME);

        uriMatcher.addURI(AUTHORITY, "QUERY_FIND_LINKMAN_COUNT",
            QUERY_FIND_LINKMAN_COUNT);
        uriMatcher
            .addURI(AUTHORITY, "QUERY_VIDEO_LINKMAN", QUERY_VIDEO_LINKMAN);
        uriMatcher.addURI(AUTHORITY, "QUERY_VIDEO_LINKMAN_BY_FULLPYM",
            QUERY_VIDEO_LINKMAN_BY_FULLPYM);
        uriMatcher.addURI(AUTHORITY, "QUERY_VIDEO_LINKMAN_BY_FULLPYM_FOR_XIN",
            QUERY_VIDEO_LINKMAN_BY_FULLPYM_FOR_XIN);
        uriMatcher.addURI(AUTHORITY, "QUERY_INFO_ROR_FAMILYE_BY_NUBENUMBER/*",
            QUERY_INFO_ROR_FAMILYE_BY_NUBENUMBER);

        uriMatcher.addURI(AUTHORITY, "QUERY_SIMPLE_LINKMAN",
            QUERY_SIMPLE_LINKMAN);
        uriMatcher.addURI(AUTHORITY, "QUERY_SIMPLE_DT_LINKMAN",
            QUERY_SIMPLE_DT_LINKMAN);
        uriMatcher.addURI(AUTHORITY, "QUERY_FIND_LINKMAN", QUERY_FIND_LINKMAN);
        uriMatcher.addURI(AUTHORITY, "QUERY_APP_LINKMAN_NUMBER",
            QUERY_APP_LINKMAN_NUMBER);
        uriMatcher.addURI(AUTHORITY, "GET_APP_LINKMAN_DATA",
            GET_APP_LINKMAN_DATA);
        uriMatcher.addURI(AUTHORITY, "GET_APP_LINKMAN_DATA_NEW",
            GET_APP_LINKMAN_DATA_NEW);
        uriMatcher.addURI(AUTHORITY, "QUERY_BUTEL_LINKMAN",
            QUERY_BUTEL_LINKMAN);
        uriMatcher.addURI(AUTHORITY, "QUERY_BUTEL_LINKMAN/*",
            QUERY_BUTEL_LINKMAN_BY_NUBENUMBER);
        uriMatcher.addURI(AUTHORITY, "GET_APP_LINKMAN_NUMBER_DATA_NEW",
            GET_APP_LINKMAN_NUMBER_DATA_NEW);

        uriMatcher.addURI(AUTHORITY, "GET_APP_LINKMAN_LOCAL_FIND_DATA_NEW",
            GET_APP_LINKMAN_LOCAL_FIND_DATA_NEW);
        uriMatcher.addURI(AUTHORITY, "GET_APP_LINKMAN_NEW/*",
            GET_APP_LINKMAN_NEW);

        uriMatcher.addURI(AUTHORITY, "UPDATE_AUTH_STATUS", UPDATE_AUTH_STATUS);
        uriMatcher
            .addURI(AUTHORITY, "QUERY_LINKMAN_ITEM/*", QUERY_LINKMAN_ITEM);
        uriMatcher.addURI(AUTHORITY, "UPDATE_ONLINE_STATUS_TIME",
            UPDATE_ONLINE_STATUS_TIME);
        uriMatcher.addURI(AUTHORITY, "QUERY_MAX_SORT", QUERY_MAX_SORT);
        uriMatcher.addURI(AUTHORITY, "BATCH_UPDATE_ONLINE_ITEM",
            BATCH_UPDATE_ONLINE_ITEM);
        uriMatcher.addURI(AUTHORITY, "MATCH_NAME_BYNUMBER_ITEM",
            MATCH_NAME_BYNUMBER_ITEM);
        uriMatcher.addURI(AUTHORITY, "DEL_NUBE_CONTACT_BY_ID",
            DEL_NUBE_CONTACT_BY_ID);
        uriMatcher
            .addURI(AUTHORITY, "UPDATE_CONTACT_INFO", UPDATE_CONTACT_INFO);
        uriMatcher.addURI(AUTHORITY, "QUERY_ONLINE_COUNT_INFO",
            QUERY_ONLINE_COUNT_INFO);
        uriMatcher.addURI(AUTHORITY, "QUERY_NEW_FRIEND_INFO",
            QUERY_NEW_FRIEND_INFO);
        uriMatcher.addURI(AUTHORITY, "QUERY_NEW_FRIEND_COUNT_INFO",
            QUERY_NEW_FRIEND_COUNT_INFO);
        uriMatcher.addURI(AUTHORITY, "MATCH_CONTACT_BYNUMBER_ITEM",
            MATCH_CONTACT_BYNUMBER_ITEM);
        uriMatcher.addURI(AUTHORITY, "MATCH_CONTACT_BYNUBENUMBER_ITEM",
            MATCH_CONTACT_BYNUBENUMBER_ITEM);
        uriMatcher.addURI(AUTHORITY, "UPDATE_CONTACT_SORTKEY_INFO",
            UPDATE_CONTACT_SORTKEY_INFO);
        uriMatcher.addURI(AUTHORITY, "UPDATE_MULT_CONTACT_SORTKEY_INFO/*/*",
            UPDATE_MULT_CONTACT_SORTKEY_INFO);
        uriMatcher.addURI(AUTHORITY, "QUERY_APP_LINKMAN_COUNT",
            QUERY_APP_LINKMAN_COUNT);
        uriMatcher.addURI(AUTHORITY, "QUERY_LINKMAN_BY_NUBENUMBER/*",
            QUERY_LINKMAN_BY_NUBENUMBER);
        uriMatcher.addURI(AUTHORITY, "QUERY_SOURCE_ITEM", QUERY_SOURCE_ITEM);

        uriMatcher.addURI(AUTHORITY, "QUERY_NUBE_FRIEND_INFO",
            QUERY_NUBE_FRIEND_INFO);
        uriMatcher.addURI(AUTHORITY, "QUERY_VISIBLE_NUBE_FRIEND_INFO",
            QUERY_VISIBLE_NUBE_FRIEND_INFO);
        uriMatcher.addURI(AUTHORITY, "QUERY_NUBE_FRIEND_RECORD",
            QUERY_NUBE_FRIEND_RECORD);
        uriMatcher.addURI(AUTHORITY, "QUERY_NEW_FRIEND_RECORD",
            QUERY_NEW_FRIEND_RECORD);
        uriMatcher.addURI(AUTHORITY, "UPDATE_NUBE_FRIEND_INFO",
            UPDATE_NUBE_FRIEND_INFO);
        uriMatcher.addURI(AUTHORITY, "QUERY_LINKMAN_KEY", QUERY_LINKMAN_KEY);

        uriMatcher.addURI(AUTHORITY, "QUERY_EMPTY_LINKMAN_ITEM",
            QUERY_EMPTY_LINKMAN_ITEM);

        uriMatcher.addURI(AUTHORITY, "UPDATE_LINKMAN_STATUS_ITEM",
            UPDATE_LINKMAN_STATUS_ITEM);
        uriMatcher.addURI(AUTHORITY, "DELETE_LINKMAN_STATUS_ITEM",
            DELETE_LINKMAN_STATUS_ITEM);

        // notices table matcher
        uriMatcher.addURI(AUTHORITY, "DELETE_VCARD_MSG", DELETE_VCARD_MSG);
        uriMatcher.addURI(AUTHORITY, "QUERY_VCARD_CURSOR", QUERY_VCARD_CURSOR);
        uriMatcher.addURI(AUTHORITY, NoticesTable.TABLENAME,
            NET_PHONE_NOTICE_LIST);
        uriMatcher.addURI(AUTHORITY, NoticesTable.TABLENAME + "/*",
            NET_PHONE_NOTICE_ITEM);

        uriMatcher.addURI(AUTHORITY, DtNoticesTable.TABLE_NAME,
            HPU_DT_NOTICE_LIST);
        uriMatcher.addURI(AUTHORITY, DtNoticesTable.TABLE_NAME + "/*",
            HPU_DT_NOTICE_ITEM);

        uriMatcher.addURI(AUTHORITY, "QUERY_PAGE_NOTICES/*/*/*",
            QUERY_PAGE_NOTICES);
        uriMatcher.addURI(AUTHORITY, "QUERY_ALL_NOTICES/*",
            QUERY_ALL_NOTICES);
        uriMatcher.addURI(AUTHORITY, "QUERY_COND_NOTICES/*/*",
            QUERY_COND_NOTICES);

        uriMatcher.addURI(AUTHORITY, "QUERY_PAGE_DT_NOTICES/*/*/*",
            QUERY_PAGE_DT_NOCTICES);
        uriMatcher.addURI(AUTHORITY, "QUERY_ALL_DT_NOTICES/*",
            QUERY_ALL_DT_NOTICES);
        uriMatcher.addURI(AUTHORITY, "QUERY_COND_DT_NOTICES/*/*",
            QUERY_COND_DT_NOTICES);

        uriMatcher.addURI(AUTHORITY, "QUERY_UNREAD_NOTICES_COUNT", QUERY_UNREAD_NOTICES_COUNT);
        uriMatcher.addURI(AUTHORITY, "QUERY_UNREAD_NOTICES_COUNT_NODISTURB"
            , QUERY_UNREAD_NOTICES_COUNT_NODISTURB);

        uriMatcher.addURI(AUTHORITY, "QUERY_UNREAD_NOTICES_DT_COUNT_NODISTURB",
            QUERY_UNREAD_NOTICES_DT_COUNT_NODISTURB);
        uriMatcher.addURI(AUTHORITY, "QUERY_UNREAD_NOTICES_DT_COUNT",
            QUERY_UNREAD_NOTICES_DT_COUNT);

        // uriMatcher.addURI(AUTHORITY, "QUERY_NOTICES", QUERY_NOTICES);
        // uriMatcher.addURI(AUTHORITY, "QUERY_CONVST_NOTICES",
        // QUERY_CONVST_NOTICES);
        uriMatcher
            .addURI(AUTHORITY, "COUNT_VCARD_NOTICES", COUNT_VCARD_NOTICES);
        uriMatcher.addURI(AUTHORITY, "COUNT_NEW_NOTICES", COUNT_NEW_NOTICES);
        uriMatcher.addURI(AUTHORITY, "UPDATE_NEW_NOTICES_READ",
            UPDATE_NEW_NOTICES_READ);

        uriMatcher.addURI(AUTHORITY, "QUERY_NOTICES_DT_COUNT_T", QUERY_NOTICES_DT_COUNT_T);

        // new friend table matcher
        //        uriMatcher.addURI(AUTHORITY, NewFriendTable.TABLENAME,
        //                NET_PHONE_NEWFRIEND_LIST);
        //        uriMatcher.addURI(AUTHORITY, NewFriendTable.TABLENAME + "/*",
        //                NET_PHONE_NEWFRIEND_ITEM);
        //        // TODO:unread new friend count
        //        uriMatcher.addURI(AUTHORITY, "GET_UNREAD_NEW_FRIEND",
        //                GET_UNREAD_NEW_FRIEND);
        //
        //        uriMatcher.addURI(AUTHORITY, NewFriendTable.TABLENAME + "/*/*",
        //                UPDATE_FRIEND_ITEM);
        //
        //        uriMatcher.addURI(AUTHORITY, "QUERY_LINKMAN_BY_PHONENUMBER/*",
        //                QUERY_LINKMAN_BY_PHONENUMBER);
        //
        //        // lihs @date 2013.11.18 活动
        //        uriMatcher.addURI(AUTHORITY, ActivityTable.TABLENAME,
        //                NET_PHONE_ACTIVITY_ITEM);
        //        uriMatcher.addURI(AUTHORITY, NubeFriendColumn.TABLENAME,
        //                NET_PHONE_NUBEFRIEND);
        //        uriMatcher.addURI(AUTHORITY, "DELETE_FRIEND_ITEM/*/*",
        //                DELETE_FRIEND_ITEM);
        //        // call record matcher
        //        uriMatcher.addURI(AUTHORITY, CallRecordsColumn.TABLENAME,
        //                CALL_RECORD_LIST);
        //        uriMatcher.addURI(AUTHORITY, CallRecordsColumn.TABLENAME + "/*",
        //                CALL_RECORD_ITEM);

        uriMatcher.addURI(AUTHORITY, "ADD_FAMILY_NUMBER/*", ADD_FAMILY_NUMBER);

        // uriMatcher.addURI(AUTHORITY, "ADD_DISTINCT_FAMILY_NUMBER",
        // ADD_DISTINCT_FAMILY_NUMBER);
        uriMatcher.addURI(AUTHORITY, "UPDATE_FAMILY_NUMBER",
            UPDATE_FAMILY_NUMBER);
        uriMatcher.addURI(AUTHORITY, "UPDATE_FAMILY_2_NOTNEW",
            UPDATE_FAMILY_2_NOTNEW);
        uriMatcher
            .addURI(AUTHORITY, "UPDATE_FAMILY_ISNEW", UPDATE_FAMILY_ISNEW);
        uriMatcher.addURI(AUTHORITY, "DELETE_FAMILY_NUMBER",
            DELETE_FAMILY_NUMBER);

        //        uriMatcher.addURI(AUTHORITY, DeviceColumn.TABLENAME, DEVICE_LIST);
        //        uriMatcher.addURI(AUTHORITY, AlarmTable.TABLENAME, ALARM_LIST);
        // uriMatcher
        // .addURI(AUTHORITY, AlarmTable.TABLENAME + "/*", ALARM_ITEM);
        // uriMatcher.addURI(AUTHORITY, "DELETE_MY_DEVICE_NUMBER",
        // DELETE_MY_DEVICE_NUMBER);
        // uriMatcher.addURI(AUTHORITY, "ADD_MY_DEVICE_NUMBER/*",
        // ADD_MY_DEVICE_NUMBER);
        // uriMatcher.addURI(AUTHORITY, "UPDATE_MY_DEVICE_NUMBER",
        // UPDATE_MY_DEVICE_NUMBER);
        uriMatcher.addURI(AUTHORITY,
            "QUERY_MY_DEVICE_NUMBER_AND_NUBE_FRIEND_NAME",
            QUERY_MY_DEVICE_NUMBER_AND_NUBE_FRIEND_NAME);
        // uriMatcher.addURI(AUTHORITY, "QUERY_MY_DEVICE_NUMBER",
        // QUERY_MY_DEVICE_NUMBER);

        uriMatcher
            .addURI(AUTHORITY, "QUERY_FAMILY_NUMBER", QUERY_FAMILY_NUMBER);
        uriMatcher.addURI(AUTHORITY, "QUERY_FAMILY_INFO_BY_NUBENUMBER",
            QUERY_FAMILY_INFO_BY_NUBENUMBER);
        uriMatcher.addURI(AUTHORITY, "QUERY_NO_CONTACT_FAMILY_NUMBER",
            QUERY_NO_CONTACT_FAMILY_NUMBER);
        uriMatcher.addURI(AUTHORITY,
            "QUERY_FAMILY_NUMBER_AND_NUBE_FRIEND_NAME",
            QUERY_FAMILY_NUMBER_AND_NUBE_FRIEND_NAME);

        uriMatcher.addURI(AUTHORITY, "DELETE_NEWFRIEND_TABLE",
            DELETE_NEWFRIEND_TABLE);
        uriMatcher.addURI(AUTHORITY, "QUERY_CONTACT_RECORD/*",
            QUERY_CONTACT_RECORD);
        uriMatcher.addURI(AUTHORITY, "CHECK_CONTACT_RECORD/*",
            CHECK_CONTACT_RECORD);
        uriMatcher.addURI(AUTHORITY, "CHECK_CONTACT_ISONLINE/*",
            CHECK_CONTACT_ISONLINE);

        uriMatcher.addURI(AUTHORITY, "ADD_REPLYMSG", ADD_REPLYMSG);
        uriMatcher.addURI(AUTHORITY, "DELETE_REPLYMSG", DELETE_REPLYMSG);
        uriMatcher.addURI(AUTHORITY, "QUERY_REPLYMSG", QUERY_REPLYMSG);
        // 会话
        uriMatcher.addURI(AUTHORITY, ThreadsTable.TABLENAME,
            NETPHONE_THREADS_LIST);
        uriMatcher.addURI(AUTHORITY, ThreadsTable.TABLENAME + "/*",
            NETPHONE_THREADS_ITEM);
        // TODO:
        uriMatcher.addURI(AUTHORITY, "NETPHONE_THREADS_GETALL",
            NETPHONE_THREADS_GETALL);
        uriMatcher.addURI(AUTHORITY, "NETPHONE_THREADS_GETALL_TOP", NETPHONE_THREADS_GETALL_TOP);

        //        uriMatcher.addURI(AUTHORITY, NumberCacheTable.TABLENAME,
        //                NUMBER_CACHE_LIST);
        //        uriMatcher.addURI(AUTHORITY, NumberCacheTable.TABLENAME + "/*",
        //                NUMBER_CACHE_ITEM);

        // 群聊
        uriMatcher.addURI(AUTHORITY, GroupTable.TABLENAME, GROUP_LIST);
        uriMatcher.addURI(AUTHORITY, GroupMemberTable.TABLENAME,
            GROUP_MEMBER_LIST);
        uriMatcher.addURI(AUTHORITY, "GROUP_MEMBER_ADD_UPDATE",
            GROUP_MEMBER_ADD_UPDATE);
        // uriMatcher.addURI(AUTHORITY, "GROUP_MEMBER_HEADURL",
        // GROUP_MEMBER_HEADURL);
        // uriMatcher.addURI(AUTHORITY, "QUERY_GROUP_MEMBER_NAME",
        // QUERY_GROUP_MEMBER_NAME);
        uriMatcher.addURI(AUTHORITY, "GROUP_3_MEMBERS_NAME_BY_GID/*",
            GROUP_3_MEMBERS_NAME_BY_GID);
        uriMatcher.addURI(AUTHORITY, "QUERY_GROUP_MEMBERS/*",
            QUERY_GROUP_MEMBERS);
        uriMatcher.addURI(AUTHORITY, "QUERY_GROUP_MEMBER_ITEM/*/*",
            QUERY_GROUP_MEMBER_ITEM);
        uriMatcher.addURI(AUTHORITY, "QUERY_GROUP_MEMBER_CNT/*",
            QUERY_GROUP_MEMBER_CNT);
        uriMatcher.addURI(AUTHORITY, "QUERY_THREAD_GROUPS/*",
            QUERY_THREAD_GROUPS);

        //        uriMatcher.addURI(AUTHORITY, PublicNOHistoryTable.TABLENAME, PUBLIC_NO_HISTORY);
        //        uriMatcher.addURI(AUTHORITY, PublicNOCacheTable.TABLENAME, PUBLIC_NO_CACHE);
        //        uriMatcher.addURI(AUTHORITY, SearchHistoryTable.TABLENAME, SEARCH_HIS);
        //        uriMatcher.addURI(AUTHORITY, MeetHistoryTable.TABLENAME, MEET_HIS);
        uriMatcher.addURI(AUTHORITY, CollectionTable.TABLENAME, COLLECT_TABLE);
        uriMatcher.addURI(AUTHORITY, query_all_collection_record, QUERY_ALL_COLLECTION_RECORD);
        //好友关系
        uriMatcher.addURI(AUTHORITY, FriendRelationTable.TABLENAME, FRIEND_RELATION_TABLE);
        uriMatcher.addURI(AUTHORITY, "t_friends_relation_order_by_update_time",
            T_FRIENDS_RELATION_ORDER_BY_UPDATE_TIME);
        uriMatcher.addURI(AUTHORITY, "friend_relation_table_insert", FRIEND_RELATION_TABLE_INSERT);
        uriMatcher.addURI(AUTHORITY, "friend_relation_table_delete", FRIEND_RELATION_TABLE_DELETE);
        uriMatcher.addURI(AUTHORITY, "friend_relation_table_update", FRIEND_RELATION_TABLE_UPDATE);
        uriMatcher.addURI(AUTHORITY, "friend_relation_table_query", FRIEND_RELATION_TABLE_QUERY);
        // 陌生人消息
        uriMatcher.addURI(AUTHORITY, StrangerMessageTable.TABLENAME, STRANGER_MESSAGE_TABLE);
        uriMatcher.addURI(AUTHORITY, "stranger_message_table_insert",
            STRANGER_MESSAGE_TABLE_INSERT);
        uriMatcher.addURI(AUTHORITY, "stranger_message_table_delete",
            STRANGER_MESSAGE_TABLE_DELETE);
        uriMatcher.addURI(AUTHORITY, "stranger_message_table_update",
            STRANGER_MESSAGE_TABLE_UPDATE);
        uriMatcher.addURI(AUTHORITY, "stranger_message_table_query", STRANGER_MESSAGE_TABLE_QUERY);
        uriMatcher.addURI(AUTHORITY, "STRANGER_MESSAGE_TABLE_QUERY_NOT_READ_MSG",
            STRANGER_MESSAGE_TABLE_QUERY_NOT_READ_MSG);
    }


    @Override
    public boolean onCreate() {
        CustomLog.d(TAG, "onCreate start");
        return true;
    }


    /**
     * 初始化数据库
     */
    public synchronized boolean checkProvider() {
        if ((null == db || !db.isOpen())) {
            initDatabase();
        }
        return db == null ? false : true;
    }


    private void initDatabase() {
        try {
            db = DatabaseManager.getInstance().getSQLiteDatabase();
        } catch (Exception e) {
            CustomLog.e(TAG, "MedicalProvider instanceProvider Error" + e);
        }
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        CustomLog.i(TAG, "query()");

        Cursor cursor = null;
        String sql = "";
        manager = AccountManager.getInstance(MedicalApplication.shareInstance());
        if (!checkProvider()) {
            return cursor;
        }

        switch (uriMatcher.match(uri)) {
            case QUERY_UNREAD_NOTICES_DT_COUNT:
                sql = "select t1.id from t_hpu_notices t1" +
                    "where t1.isNews  = 1";
                cursor = db.rawQuery(sql, null);
                CustomLog.i(TAG,
                    "query unread notices dt count | cursor count = " + cursor.getCount());
                break;
            case QUERY_UNREAD_NOTICES_DT_COUNT_NODISTURB:
                sql = "select t1.id from t_hpu_notices t1" +
                    "where t1.isNews  = 1";
                cursor = db.rawQuery(sql, null);
                CustomLog.i(TAG,
                    "QUERY_UNREAD_NOTICES_DT_COUNT_NODISTURB | cursor count = " +
                        cursor.getCount());
                break;
            case QUERY_UNREAD_NOTICES_COUNT:
                sql =
                    "select t1.id from t_notices t1  left join t_threads t2 on t1.threadsId = t2.id " +
                        "where t1.isNews  = 1 and t2.reserverStr1 = '0'";
                cursor = db.rawQuery(sql, null);
                CustomLog.i(TAG,
                    "QUERY_UNREAD_NOTICES_COUNT | cursor count = " + cursor.getCount());
                break;
            case QUERY_UNREAD_NOTICES_COUNT_NODISTURB:
                sql =
                    "select t1.id from t_notices t1  left join t_threads t2 on t1.threadsId = t2.id " +
                        "where t1.isNews  = 1 and t2.reserverStr1 = '1'";
                cursor = db.rawQuery(sql, null);
                CustomLog.i(TAG,
                    "QUERY_UNREAD_NOTICES_COUNT_NODISTURB | cursor count = " + cursor.getCount());
                break;
            case QUERY_VCARD_CURSOR:
                // 查询名片信息 @lihs 2014.1.3
                //                sql = "select n.id as id, n.title as title, n.receivedTime as receivedTime, n.type as type,"
                //                        + "n.isNews as isNews, n.body as body,n.createTime as createTime, n.sender as sender,"
                //                        + "n.receiver as receiver,case length(f.name) when 0 then (case length(f.nickname) "
                //                        + "when 0 then f.nubeNumber else f.nickname end) else f.name end as name,"
                //                        + "n.status as status, f.headUrl as headUrl, n.receiverNames as receiverNames,"
                //                        + "n.msgId as msgId, n.extInfo as extInfo, n.failReplyId as failReplyId from t_notices n "
                //                        + "left join t_nubefriend f on case when CAST(n.type AS INT) < 10 then "
                //                        + "n.receiver = f.nubeNumber else n.sender = f.nubeNumber end and f.isDeleted = 0 "
                //                        + "and f.isMutualTrust in(0,1) where n.type in ('"
                //                        + FileTaskManager.NOTICE_TYPE_VCARD_SEND
                //                        + "'"
                //                        + ") order by receivedTime desc";
                //                LogUtil.d(sql);
                //                cursor = db.rawQuery(sql, null);
                break;
            case NET_PHONE_NUBEFRIEND:
                cursor = db.query(NubeFriendColumn.TABLENAME, null, selection,
                    selectionArgs, null, null, null);
                CustomLog.i(TAG,
                    "NET_PHONE_NUBEFRIEND | cursor count = " + cursor.getCount());
                break;
            case NET_PHONE_ACTIVITY_ITEM:
                // 查询该活动是不是已经存在
                //                cursor = db.query(ActivityTable.TABLENAME, null, selection,
                //                        selectionArgs, null, null, sortOrder);
                break;
            case QUERY_LINKMAN_BY_PHONENUMBER:
                // 查询联系人信息
                String phoneNumber = uri.getPathSegments().get(1);
                sql =
                    "select contactUserId contactUserId , nubeNumber nubeNumber , nickname nickname , headurl headurl "
                        +
                        " from t_nubefriend tn where tn.isDeleted=0 and tn.number='"
                        + phoneNumber
                        + "'"
                        + " and nubeNumber is not null and ltrim(rtrim(nubeNumber)) <> ''";
                cursor = db.rawQuery(sql, null);
                CustomLog.i(TAG,
                    "QUERY_LINKMAN_BY_PHONENUMBER | cursor count = " + cursor.getCount());
                break;
            case QUERY_CONTACT_RECORD:
                String nubeNumber = uri.getPathSegments().get(1);
                sql =
                    "select contactId contactId,nubeNumber nubeNumber,name name,callDirection callDirection,time time,callType callType from t_callrecords "
                        + " where nubeNumber='"
                        + nubeNumber
                        + "' order by callDirection desc ,time desc";
                CustomLog.d(TAG, "查询联系人通话记录" + sql);
                cursor = db.rawQuery(sql, null);
                CustomLog.i(TAG,
                    "QUERY_CONTACT_RECORD | cursor count = " + cursor.getCount());
                break;
            case CHECK_CONTACT_RECORD:
                String nubeNumbers = uri.getPathSegments().get(1);
                sql = "select contactId contactId from t_callrecords "
                    + " where nubeNumber is not null and nubeNumber='"
                    + nubeNumbers + "'";
                CustomLog.i(TAG,
                    "CHECK_CONTACT_RECORD | cursor count = " + cursor.getCount());
                cursor = db.rawQuery(sql, null);
                break;
            case CHECK_CONTACT_ISONLINE:
                String nube = uri.getPathSegments().get(1);
                sql = "select isOnline isOnline from t_nubefriend "
                    + " where nubeNumber is not null and nubeNumber='"
                    + nube + "'";
                CustomLog.i(TAG,
                    "CHECK_CONTACT_ISONLINE | cursor count = " + cursor.getCount());
                cursor = db.rawQuery(sql, null);
                break;
            case NET_PHONE_LINKMANS_LIST:
                cursor = db.query(NubeFriendColumn.TABLENAME, new String[] {
                        NubeFriendColumn.CONTACTID, NubeFriendColumn.NUMBER },
                    selection, selectionArgs, null, null, null);
                CustomLog.i(TAG,
                    "NET_PHONE_LINKMANS_LIST | cursor count = " + cursor.getCount());
                break;
            case NET_PHONE_LINKMANS_ITEM:
                cursor = db.query(NubeFriendColumn.TABLENAME, null, selection,
                    selectionArgs, null, null, null);
                CustomLog.i(TAG,
                    "NET_PHONE_LINKMANS_ITEM | cursor count = " + cursor.getCount());
                break;
            case QUERY_NUBE_FRIEND_INFO:
                sql =
                    "select nubeNumber nubeNumber,contactId contactId,name name,nickname nickname from "
                        + NubeFriendColumn.TABLENAME
                        + " where isDeleted=0 group by nubeNumber";
                CustomLog.d(TAG, "同步时发现好友  查询好友联系人" + sql);
                cursor = db.rawQuery(sql, null);
                CustomLog.i(TAG,
                    "QUERY_NUBE_FRIEND_INFO | cursor count = " + cursor.getCount());
                break;
            case QUERY_VISIBLE_NUBE_FRIEND_INFO:
                sql =
                    "select nubeNumber nubeNumber,contactId contactId,name name,nickname nickname from "
                        + NubeFriendColumn.TABLENAME
                        + " where isDeleted =0";
                CustomLog.d(TAG, "同步时发现好友  查询可见的好友联系人" + sql);
                cursor = db.rawQuery(sql, null);
                CustomLog.i(TAG,
                    "QUERY_VISIBLE_NUBE_FRIEND_INFO | cursor count = " + cursor.getCount());
                break;
            case QUERY_NUBE_FRIEND_RECORD:
                sql = "select t.nubeNumber as nubeNumber,n.id as id from t_nubefriend t inner join "
                    +
                    "(select nubeNumber as number,id as id from t_newfriend where status=5 )n on n.number=t.nubeNumber "
                    + "where t.isDeleted=0";
                cursor = db.rawQuery(sql, null);
                CustomLog.i(TAG,
                    "QUERY_NUBE_FRIEND_RECORD | cursor count = " + cursor.getCount());
                break;
            case QUERY_NEW_FRIEND_RECORD:
                //                sql = "select id id from " + NewFriendTable.TABLENAME
                //                        + " where visible=1 and status=5";
                //                CustomLog.d(TAG," 查讯新朋友不可见记录" + sql);
                //                cursor = db.rawQuery(sql, null);
                break;
            case UPLOAD_GET_COUNT:
                // 得到需要上传的数据条数
                sql = "select count(contactId) scount,isMutualTrust isMutualTrust from "
                    + uri.getPathSegments().get(1)
                    + " where syncStat=0";
                cursor = db.rawQuery(sql, null);
                CustomLog.i(TAG,
                    "UPLOAD_GET_COUNT | cursor count = " + cursor.getCount());
                break;
            case QUERY_NUBE_FRIENDS_LIST:
                // 获取需要同步的佰酷好友数据
                sql =
                    "select contactId contactId,name name,nickname nickname,firstName firstName,lastName lastName,"
                        +
                        " lastTime lastTime,isDeleted isDeleted,pym pym,number number,nubeNumber nubeNumber,isMutualTrust isMutualTrust,isOnline isOnline,contactUserId contactUserId,headUrl headUrl,userType userType,sex sex,reserveStr1 reserveStr1 "
                        + " from t_nubefriend tn where tn.syncStat=0";
                cursor = db.rawQuery(sql, null);
                CustomLog.i(TAG,
                    "QUERY_NUBE_FRIENDS_LIST | cursor count = " + cursor.getCount());
                break;
            case QUERY_EMPTY_LINKMAN_ITEM:
                // 查询手机号码为空的数据
                sql = "select contactId contactId,number number,nubeNumber nubeNumber"
                    +
                    " from t_nubefriend tn where (number is null or number='') and tn.isDeleted=0 ";
                cursor = db.rawQuery(sql, null);
                CustomLog.i(TAG,
                    "QUERY_EMPTY_LINKMAN_ITEM | cursor count = " + cursor.getCount());
                break;
            case GET_MAX_TIMESTAMP:
                // 获取需要同步的佰酷好友数据
                sql = "select max(lastTime) lastTime from t_nubefriend";
                CustomLog.d(TAG, "获取最大时间戳" + sql);
                cursor = db.rawQuery(sql, null);
                CustomLog.i(TAG,
                    "GET_MAX_TIMESTAMP | cursor count = " + cursor.getCount());
                break;
            case QUERY_LINKMAN_ID:
                CustomLog.d(TAG, "provider query friendsTable data id");
                cursor = db.query(NubeFriendColumn.TABLENAME, projection,
                    selection, selectionArgs, null, null, null);
                CustomLog.i(TAG,
                    "QUERY_LINKMAN_ID | cursor count = " + cursor.getCount());
                break;
            case QUERY_FIND_LINKMAN_COUNT:
                //                String owener = NetPhoneApplication.getPreference().getKeyValue(
                //                        PrefType.LOGIN_NUBENUMBER, "");
                //                // 获取发现好友数量
                //                sql = "select count(contactId) findCount from t_nubefriend tn where tn.nubeNumber <> '"
                //                        + owener
                //                        + "' and tn.isMutualTrust in(0,1) and tn.isDeleted=0";
                //                LogUtil.d("获取发现好友数量" + sql);
                //                cursor = db.rawQuery(sql, null);
                break;
            case QUERY_LINKMAN_KEY:
                sql
                    = "select nubeNumber nubeNumber,contactId contactId,isDeleted isDeleted,sourcesId sourcesId from t_nubefriend where nubeNumber is not null and ltrim(rtrim(nubeNumber)) <> '' order by contactId asc";
                CustomLog.d(TAG, "查询同步替重的key" + sql);
                cursor = db.rawQuery(sql, null);
                CustomLog.i(TAG,
                    "QUERY_LINKMAN_KEY | cursor count = " + cursor.getCount());
                break;
            case QUERY_REPLYMSG:
                CustomLog.d(TAG, "provider query reply msg");
                //                cursor = db.query(ReplyMsgColumn.TABLENAME, projection, selection,
                //                        selectionArgs, null, null, sortOrder);
                break;
            case QUERY_FAMILY_NUMBER:
                //                LogUtil.d("provider query family number");
                //                cursor = db.query(FamilyColumn.TABLENAME, projection, selection,
                //                        selectionArgs, null, null, sortOrder);
                break;
            case DEVICE_LIST:
                //                LogUtil.d("provider query device list");
                //                cursor = db.query(DeviceColumn.TABLENAME, projection, selection,
                //                        selectionArgs, null, null, sortOrder);
                break;
            case QUERY_FAMILY_INFO_BY_NUBENUMBER:
                //                cursor = db.query(FamilyColumn.TABLENAME, projection, selection,
                //                        selectionArgs, null, null, sortOrder);
                break;
            case QUERY_NO_CONTACT_FAMILY_NUMBER:
                //                sql = "select nubeNumber from t_family where nubeNumber not in (select nubeNumber from t_nubefriend where isMutualTrust in(0,1))";
                //                LogUtil.d("查询不是联系人的亲情号码sql:" + sql);
                //                cursor = db.rawQuery(sql, null);
                break;
            case QUERY_FAMILY_NUMBER_AND_NUBE_FRIEND_NAME:
                //                sql = "select f.nubeNumber as nubeNumber, n.name as name, n.nickname as nickname from t_family f left join "
                //                        + "t_nubefriend n on f.nubeNumber=n.nubeNumber where n.isMutualTrust in(0,1) and n.isDeleted=0 "
                //                        + "order by f.timePoint desc ";
                //                LogUtil.d("查询亲情号码sql:" + sql);
                cursor = db.rawQuery(sql, null);
                break;
            case QUERY_MY_DEVICE_NUMBER_AND_NUBE_FRIEND_NAME:
                //                sql = "select td.nubeNum as nubeNum, td.headUrl as headUrl, td.nickName as nickName,td.sex as sex,td.status as status from t_device td "
                //                        + "order by td.relatedTime desc ";
                //                LogUtil.d("查询我的设备号码sql:" + sql);
                //                cursor = db.rawQuery(sql, null);
                break;

            case QUERY_APP_LINKMAN_COUNT:
                sql
                    = "select count(contactId) findCount from t_nubefriend tn where tn.isDeleted=0";
                //                LogUtil.d("获取app数量" + sql);
                //                cursor = db.rawQuery(sql, null);
                break;
            case QUERY_VIDEO_LINKMAN:
                // 查询视频联系人
                //                String selfNumber = NetPhoneApplication.getPreference()
                //                        .getKeyValue(PrefType.LOGIN_NUBENUMBER, "");
                //                sql = "select contactId _id,name name,nickname nickname,number number,nubeNumber nubeNumber,pym pym,lower(fullPym) fullPym,isOnline isOnline,isMutualTrust isMutualTrust, sortKey sortKey, headUrl headUrl,extraInfo extraInfo from t_nubefriend tn "
                //                        + " where tn.isMutualTrust in (0,1) and tn.isDeleted=0 and nubeNumber <> '"
                //                        + selfNumber
                //                        + "' and nubeNumber is not null and ltrim(rtrim(nubeNumber)) <> '' order by isOnline desc,fullPym asc";
                //                LogUtil.d(" 查询视频联系人" + sql);
                //                cursor = db.rawQuery(sql, null);
                break;
            case QUERY_VIDEO_LINKMAN_BY_FULLPYM:
                // 根据姓名查询视频联系人
                //                String selfNumberf = NetPhoneApplication.getPreference()
                //                        .getKeyValue(PrefType.LOGIN_NUBENUMBER, "");
                //                sql = "select contactId _id,name name,nickname nickname,number number,nubeNumber nubeNumber,pym pym,lower(fullPym) fullPym,isOnline isOnline,isMutualTrust isMutualTrust, sortKey sortKey, headUrl headUrl,extraInfo extraInfo from t_nubefriend tn "
                //                        + " where tn.isMutualTrust in (0,1) and tn.isDeleted=0 and nubeNumber <> '"
                //                        + selfNumberf
                //                        + "' and nubeNumber is not null and ltrim(rtrim(nubeNumber)) <> '' order by fullPym asc";
                //                LogUtil.d(" 查询视频联系人" + sql);
                //                cursor = db.rawQuery(sql, null);
                break;
            case QUERY_VIDEO_LINKMAN_BY_FULLPYM_FOR_XIN:
                // 查询视频联系人
                sql = "select * from ("

                    + " select * from ("
                    + " select * from ("
                    +
                    " select ifnull(fr.contactId, tf.id) as _id, fr.name as name, ifnull(fr.nickname,tf.nickname) as nickname, ifnull(fr.number, tf.number) as number,"
                    +
                    " tf.nubeNumber as nubeNumber, fr.pym as pym, ifnull(fr.fullPym, tf.fullPym) as fullPym, fr.isOnline as isOnline, fr.isMutualTrust as isMutualTrust, "
                    + " fr.sortKey as sortKey, ifnull(fr.headUrl,tf.headUrl) as headUrl, "
                    +
                    " fr.extraInfo as extraInfo, ifnull(fr.userType, 2) as userType, '1' as groupType, tf.isNews as isNews, tf.sex as sex"
                    +
                    " from t_family tf left join (select * from t_nubefriend where isDeleted=0 and nubeNumber is not null and ltrim(rtrim(nubeNumber)) <> '' ) fr on tf.nubeNumber = fr.nubeNumber "
                    + " order by fullPym asc)"

                    + " union all"

                    + " select * from ("
                    +
                    " select tn.contactId as _id, tn.name as name, tn.nickname as nickname, tn.number as number, tn.nubeNumber as nubeNumber, tn.pym as pym, "
                    +
                    " lower(tn.fullPym) as fullPym, tn.isOnline as isOnline, tn.isMutualTrust as isMutualTrust, tn.sortKey as sortKey, tn.headUrl as headUrl, "
                    +
                    " tn.extraInfo as extraInfo, tn.userType as userType, '1' as groupType, '1' as isNews, tn.sex as sex"
                    +
                    " from t_nubefriend tn where tn.userType = 1 and tn.isDeleted=0 and tn.nubeNumber not in (select nubeNumber from t_family)  and tn.nubeNumber is not null and ltrim(rtrim(tn.nubeNumber)) <> ''  order by fullPym asc"
                    + "))"

                    + " union all"
                    + " select * from ("
                    +
                    " select tn.contactId as _id, tn.name as name, tn.nickname as nickname, tn.number as number, tn.nubeNumber as nubeNumber, tn.pym as pym, "
                    +
                    " lower(tn.fullPym) as fullPym, tn.isOnline as isOnline, tn.isMutualTrust as isMutualTrust, tn.sortKey as sortKey, tn.headUrl as headUrl, "
                    +
                    " tn.extraInfo as extraInf, tn.userType as userType, '2' as groupType, '1' as isNews, tn.sex as sex"
                    +
                    " from t_nubefriend tn where tn.isDeleted=0 and tn.nubeNumber is not null and ltrim(rtrim(tn.nubeNumber)) <> '' "
                    + " order by fullPym asc)  "

                    + " ) order by groupType asc";
                cursor = db.rawQuery(sql, null);
                break;
            case QUERY_INFO_ROR_FAMILYE_BY_NUBENUMBER:
                String tempNubenumber = uri.getPathSegments().get(1);
                sql = "select ifnull("
                    +
                    "case length(fr.name) when 0 then (case length(fr.nickname) when 0 then fr.nubeNumber else fr.nickname end) else fr.name end,"
                    + " case length(tf.nickname) when 0 then tf.nubeNumber else tf.nickname end"
                    + ") as name"
                    + " from t_family tf left join "
                    +
                    "(select * from t_nubefriend where isDeleted=0 and nubeNumber = '"
                    + tempNubenumber
                    + "') fr on tf.nubeNumber = fr.nubeNumber  where tf.nubeNumber = '"
                    + tempNubenumber + "'";
                cursor = db.rawQuery(sql, null);
                break;
            case QUERY_SIMPLE_LINKMAN:
                // 查询视频联系人名称
                // add by zzwang: 添加弱化视讯号逻辑，添加了手机号显示的优先级
                sql =
                    "select nubeNumber as nubeNumber, case length(name) when 0 then (case length(nickname) "
                        +
                        "when 0 then (case length(number) when 0 then nubeNumber else number end) else nickname end) else name end as name from t_nubefriend "
                        +
                        " where isDeleted=0 and nubeNumber is not null and ltrim(rtrim(nubeNumber)) <> '' ";
                CustomLog.d(TAG, "查询视频联系人名称:" + sql);
                cursor = db.rawQuery(sql, null);
                break;

            case QUERY_SIMPLE_DT_LINKMAN:
                // 查询视频联系人名称
                // add by zzwang: 添加弱化视讯号逻辑，添加了手机号显示的优先级
                sql =
                    "select nubeNumber as nubeNumber, case length(name) when 0 then (case length(nickname) "
                        +
                        "when 0 then (case length(number) when 0 then nubeNumber else number end) else nickname end) else name end as name from t_nubefriend "
                        +
                        " where isDeleted=0 and nubeNumber is not null and ltrim(rtrim(nubeNumber)) <> '' ";
                CustomLog.d(TAG, "查询视频联系人名称:" + sql);
                cursor = db.rawQuery(sql, null);
                break;

            case GET_APP_LINKMAN_DATA:
                // 获取应用联系人
                //                String selfNumbers = NetPhoneApplication.getPreference()
                //                        .getKeyValue(PrefType.LOGIN_NUBENUMBER, "");
                //                sql = "select contactId _id,name name,number number,nickname nickname,headUrl headUrl,nubeNumber nubeNumber ,contactUserId contactUserId,sex sex from t_nubefriend tn "
                //                        + " where tn.isDeleted=0 and tn.isMutualTrust in (0,1) and nubeNumber <> '"
                //                        + selfNumbers
                //                        + "' and nubeNumber is not null and ltrim(rtrim(nubeNumber)) <> '' order by sortKey asc";
                //                LogUtil.d("查询发现好友" + sql);
                cursor = db.rawQuery(sql, null);
                break;
            case GET_APP_LINKMAN_DATA_NEW:
                // 获取应用联系人
                String selfNumberss = manager.getAccountInfo().nube;
                // String selfNumberss = MedicalApplication.getPreference()
                //     .getKeyValue(PrefType.LOGIN_NUBENUMBER, "");
                sql =
                    "select contactId _id,name name,number number,nickname nickname,headUrl headUrl,nubeNumber nubeNumber,"
                        +
                        "contactUserId contactUserId, fullPym,sex sex from t_nubefriend tn "
                        + " where tn.isDeleted=0 and accountType= 0 and nubeNumber <> '"
                        + selfNumberss + "' order by fullPym asc";
                CustomLog.d("TAG", "查询发现好友" + sql);
                cursor = db.rawQuery(sql, null);
                break;
            case QUERY_BUTEL_LINKMAN_BY_NUBENUMBER:
                /** 开放给第三方，应用内请勿使用和修改 */
                String nubenumber = uri.getLastPathSegment();
                //                sql = "select contactId _id,name name,nickname nickname,nubeNumber nubeNumber,number number,headUrl headUrl,"
                //                        + "contactUserId contactUserId,lower(fullPym) fullPym,sex sex,userType userType from t_nubefriend tn "
                //                        + " where tn.isDeleted=0 and tn.isMutualTrust in (0,1) and nubeNumber = '"
                //                        + nubenumber + "' order by fullPym asc";
                //                LogUtil.d("查询butel联系人:" + sql);
                cursor = db.rawQuery(sql, null);
                break;
            case QUERY_BUTEL_LINKMAN:

                /** 开放给第三方，应用内请勿使用和修改   and nubeNumber LIKE '[^5]%'*/
                // 获取应用联系人
                //                String loginNube = NetPhoneApplication.getPreference()
                //                        .getKeyValue(PrefType.LOGIN_NUBENUMBER, "");
                //                sql = "select contactId _id,name name,nickname nickname,nubeNumber nubeNumber,number number,headUrl headUrl,"
                //                        + "contactUserId contactUserId,lower(fullPym) fullPym,sex sex,userType userType from t_nubefriend tn "
                //                        + " where tn.isDeleted=0 and tn.isMutualTrust in (0,1) and nubeNumber <> '"
                //                        + loginNube + "' and nubeNumber not like '5%'and nubeNumber not like '7%' order by fullPym asc";
                //                LogUtil.d("查询butel联系人:" + sql);
                cursor = db.rawQuery(sql, null);
                break;

            case GET_APP_LINKMAN_NUMBER_DATA_NEW:
                // 查询nube好友号码信息
                sql
                    = "select number number from t_nubefriend tn where tn.isDeleted=0";
                CustomLog.d(TAG, "查询发现好友" + sql);
                cursor = db.rawQuery(sql, null);
                break;
            case GET_APP_LINKMAN_LOCAL_FIND_DATA_NEW:
                // 获取应用联系人
                //                String selfNumbersss = NetPhoneApplication.getPreference()
                //                        .getKeyValue(PrefType.LOGIN_NUBENUMBER, "");
                //                sql = "select contactId _id,name name,number number,nickname nickname,headUrl headUrl,nubeNumber nubeNumber,"
                //                        + "contactUserId contactUserId,lower(fullPym) fullPym,sex sex,sourcesId sourcesId from t_nubefriend tn "
                //                        + " where tn.isMutualTrust in (5) and nubeNumber <> '"
                //                        + selfNumbersss + "' order by fullPym asc";
                //                LogUtil.d("查询发现好友" + sql);
                cursor = db.rawQuery(sql, null);
                break;
            case GET_APP_LINKMAN_NEW:
                // 查询联系人信息
                //                String nube2 = uri.getPathSegments().get(1);
                //                sql = "select name name,number number,nickname nickname,headUrl headUrl,nubeNumber nubeNumber,"
                //                        + "contactUserId contactUserId,lower(fullPym) fullPym,sex sex from t_nubefriend tn "
                //                        + " where tn.isMutualTrust in (5) and nubeNumber = '"
                //                        + nube2 + "'";
                //                LogUtil.d("查询联系人状态为5的信息" + sql);
                cursor = db.rawQuery(sql, null);
                break;
            case QUERY_APP_LINKMAN_NUMBER:
                // 查询nube好友号码信息
                //                sql = "select number number,isMutualTrust isMutualTrust,nubeNumber nubeNumber from t_nubefriend tn";
                //                LogUtil.d("查询发现好友" + sql);
                //                cursor = db.rawQuery(sql, null);
                break;

            case QUERY_LINKMAN_ITEM:
                // 查询联系人信息
                String contactId = uri.getPathSegments().get(1);
                sql =
                    "select contactId contactId,name name,nickname nickname,firstName firstName,lastName lastName,"
                        +
                        " lastTime lastTime,isDeleted isDeleted,pym pym,number number,nubeNumber nubeNumber,isMutualTrust isMutualTrust, headUrl headUrl "
                        + " from t_nubefriend tn where tn.isDeleted=0 and tn.contactId='"
                        + contactId + "'";
                CustomLog.d(TAG, "查询联系人信息" + sql);
                cursor = db.rawQuery(sql, null);
                break;
            case QUERY_ONLINE_COUNT_INFO:
                sql = "select count(contactId) count "
                    +
                    " from t_nubefriend tn where tn.isDeleted=0 and tn.isOnline=1";
                CustomLog.d(TAG, "查询联系人信息" + sql);
                cursor = db.rawQuery(sql, null);
                break;
            case QUERY_NEW_FRIEND_COUNT_INFO:
                sql = "select count(id) count "
                    + " from t_newfriend tn where tn.isNews=1";
                CustomLog.d(TAG, "查询新朋友数量" + sql);
                cursor = db.rawQuery(sql, null);
                break;
            case MATCH_NAME_BYNUMBER_ITEM:
                cursor = db.query(NubeFriendColumn.TABLENAME, projection,
                    selection, selectionArgs, null, null, sortOrder);
                break;
            case QUERY_MAX_SORT:
                sql = "select max(sortKey) key from t_nubefriend tn where tn.isDeleted=0";
                CustomLog.d(TAG, "查询最大sortkey" + sql);
                cursor = db.rawQuery(sql, null);
                break;
            case QUERY_NEW_FRIEND_INFO:
                //                cursor = db.query(NewFriendTable.TABLENAME, projection, selection,
                //                        selectionArgs, null, null, sortOrder);
                break;
            case NET_PHONE_NOTICE_ITEM:
                SQLiteQueryBuilder sqb = new SQLiteQueryBuilder();
                sqb.appendWhere(NoticesTable.NOTICE_COLUMN_ID + "="
                    + uri.getLastPathSegment());
                sqb.setTables(NoticesTable.TABLENAME);
                cursor = sqb.query(db, projection, selection, selectionArgs, null,
                    null, sortOrder);
                sqb = null;
                break;
            case HPU_DT_NOTICE_ITEM:
                SQLiteQueryBuilder bsq = new SQLiteQueryBuilder();
                bsq.appendWhere(DtNoticesTable.NOTICE_COLUMN_ID + "="
                    + uri.getLastPathSegment());
                bsq.setTables(DtNoticesTable.TABLE_NAME);
                cursor = bsq.query(db, projection, selection, selectionArgs, null,
                    null, sortOrder);
                bsq = null;
                break;
            case NET_PHONE_NOTICE_LIST:
                cursor = db.query(NoticesTable.TABLENAME, projection, selection,
                    selectionArgs, null, null, sortOrder);
                int cnt = cursor.getCount();
                CustomLog.i(TAG, "query chat list notices | normal | cursor count  = " + cnt);
                break;
            case HPU_DT_NOTICE_LIST:
                cursor = db.query(DtNoticesTable.TABLE_NAME, projection, selection,
                    selectionArgs, null, null, sortOrder);
                int t = cursor.getCount();
                CustomLog.d(TAG, "HPU_DT_NOTICE_LIST:" + t + "   HPU_DT_NOTICE_LIST");
                break;
            case QUERY_ALL_NOTICES: {
                String convtId = uri.getPathSegments().get(1);
                sql =
                    " select n.*, tm.nickName,tm.phoneNum,tm.nubeNumber,tm.showName,tm.headUrl,tm.gender," +
                        "tf.sex,tf.headUrl as mheadUrl," +
                        "tf.name from t_notices n left join (select isDeleted,nubeNumber,headUrl,sex,name from t_nubefriend  union select 0 as isDeleted , nubeNumber,headUrl,null as sex,nickName as name from t_hpu_friends) tf on n.sender=tf.nubeNumber " +
                        "and tf.isDeleted=0 " +
                        "left join t_multi_chat_users tm on tm.gid = n.threadsId and tm.nubeNumber = n.sender and tm.removed=0"
                        + " where n.threadsId = '"
                        + convtId + "'" + " order by " + NoticesTable.NOTICE_COLUMN_SENDTIME +
                        " asc ";
                cursor = db.rawQuery(sql, null);

                CustomLog.i(TAG,
                    "query chat list notices | ALL | sql = " + sql);
                CustomLog.i(TAG,
                    "query chat list notices | ALL | cursor count = " + cursor.getCount());
            }
            break;
            case QUERY_ALL_DT_NOTICES: {
                String convtId = uri.getPathSegments().get(1);
                sql =
                    " select n.*, tm.nickName,tm.phoneNum,tm.nubeNumber,tm.showName,tm.headUrl,tm.gender," +
                        "tf.sex,tf.headUrl as mheadUrl," +
                        "tf.name from t_hpu_notices n left join(select isDeleted,nubeNumber,headUrl,sex,name from t_nubefriend  union select 0 as isDeleted , nubeNumber,headUrl,null as sex,nickName as name from t_hpu_friends) tf on n.sender=tf.nubeNumber " +
                        "and tf.isDeleted=0 " +
                        "left join t_multi_chat_users tm on tm.gid = n.threadsId and tm.nubeNumber = n.sender and tm.removed=0"
                        + " where n.threadsId = '"
                        + convtId + "'" + " order by " + DtNoticesTable.NOTICE_COLUMN_SENDTIME +
                        " asc ";
                cursor = db.rawQuery(sql, null);
            }
            break;
            case QUERY_COND_NOTICES:
                String conId = uri.getPathSegments().get(1);
                String recvTime = uri.getPathSegments().get(2);
                sql =
                    " select n.*, tm.nickName,tm.phoneNum,tm.nubeNumber,tm.showName,tm.headUrl,tm.gender," +
                        "tf.sex,tf.headUrl as mheadUrl," +
                        "tf.name from t_notices n left join (select isDeleted,nubeNumber,headUrl,sex,name from t_nubefriend  union select 0 as isDeleted , nubeNumber,headUrl,null as sex,nickName as name from t_hpu_friends) tf on n.sender=tf.nubeNumber " +
                        "and tf.isDeleted=0 " +
                        "left join t_multi_chat_users tm on tm.gid = n.threadsId and tm.nubeNumber = n.sender and tm.removed=0"
                        + " where n.threadsId = '"
                        + conId + "'"
                        + " and " + NoticesTable.NOTICE_COLUMN_SENDTIME + " >= "
                        + recvTime
                        + " order by " + NoticesTable.NOTICE_COLUMN_SENDTIME + " asc ";
                cursor = db.rawQuery(sql, null);
                int i = cursor.getCount();

                CustomLog.i(TAG, "query chat list notices | cond |  sql  = " + sql);
                CustomLog.d(TAG,
                    "query chat list notices | cond |  cursor count = " + i);
                break;
            case QUERY_COND_DT_NOTICES:
                CustomLog.d(TAG, "provider HPU_DT_NOTICE_LIST");
                String dtconId = uri.getPathSegments().get(1);
                String dtrecvTime = uri.getPathSegments().get(2);
                sql =
                    " select n.*, tm.nickName,tm.phoneNum,tm.nubeNumber,tm.showName,tm.headUrl,tm.gender," +
                        "tf.sex,tf.headUrl as mheadUrl," +
                        "tf.name from t_hpu_notices n left join (select isDeleted,nubeNumber,headUrl,sex,name from t_nubefriend  union select 0 as isDeleted , nubeNumber,headUrl,null as sex,nickName as name from t_hpu_friends) tf on n.sender=tf.nubeNumber " +
                        "and tf.isDeleted=0 " +
                        "left join t_multi_chat_users tm on tm.gid = n.threadsId and tm.nubeNumber = n.sender and tm.removed=0"
                        + " where n.threadsId = '"
                        + dtconId + "'"
                        + " and " + DtNoticesTable.NOTICE_COLUMN_SENDTIME + " >= "
                        + dtrecvTime
                        + " order by " + DtNoticesTable.NOTICE_COLUMN_SENDTIME + " asc ";
                cursor = db.rawQuery(sql, null);
                // cursor = db.query(NoticesTable.TABLENAME, projection, selection,
                // selectionArgs, null, null, sortOrder);
                int icur = cursor.getCount();
                CustomLog.d(TAG, "HPU_DT_NOTICE_LIST:" + icur + "   HPU_DT_NOTICE_LIST");
                break;
            case QUERY_PAGE_NOTICES:
                // 分页查询消息
                String convstId = uri.getPathSegments().get(1);
                String recvTimeBegin = uri.getPathSegments().get(2);
                String pageCnt = uri.getPathSegments().get(3);

                // 先按降序查询出符合条件的一页数据，然后再转为升序
                sql =
                    " select * from (select n.*, tm.nickName,tm.phoneNum,tm.nubeNumber,tm.showName,tm.headUrl," +
                        "tm.gender,tf.sex,tf.headUrl as mheadUrl," +
                        "tf.name from t_notices n left join (select isDeleted,nubeNumber,headUrl,sex,name from t_nubefriend  union select 0 as isDeleted , nubeNumber,headUrl,null as sex,nickName as name from t_hpu_friends) tf on n.sender=tf.nubeNumber " +
                        "and tf.isDeleted=0 " +
                        "left join t_multi_chat_users tm on tm.gid = n.threadsId and tm.nubeNumber = n.sender and tm.removed=0"
                        + " where n.threadsId = '"
                        + convstId + "'";

                if (!"0".equals(recvTimeBegin)) {
                    sql = sql + " and " + NoticesTable.NOTICE_COLUMN_SENDTIME
                        + " < " + recvTimeBegin;
                }

                sql = sql + " order by " + NoticesTable.NOTICE_COLUMN_SENDTIME
                    + " desc " + " limit 0," + pageCnt + ") order by "
                    + NoticesTable.NOTICE_COLUMN_SENDTIME + " asc ";

                cursor = db.rawQuery(sql, null);
                int j = cursor.getCount();

                CustomLog.d(TAG, "query chat list | page | sql = " + sql);
                CustomLog.d(TAG, "query chat list | page | cursor count = " + j);
                break;
            case QUERY_PAGE_DT_NOCTICES:
                // 分页查询消息
                String pconvstId = uri.getPathSegments().get(1);
                String precvTimeBegin = uri.getPathSegments().get(2);
                String ppageCnt = uri.getPathSegments().get(3);

                // 先按降序查询出符合条件的一页数据，然后再转为升序
                //                 sql = " select * from (select n.*, tf.name as nickName,tm.phoneNum,tm.nubeNumber,tm.showName,tf.headUrl," +
                //                         " tm.gender,tf.sex,tf.headUrl as mheadUrl," +
                //                         " tf.name from t_hpu_notices n left join (select isDeleted,nubeNumber,headUrl,sex,name from t_nubefriend  union select 0 as isDeleted , nubeNumber,headUrl,null as sex,nickName as name from t_hpu_friends) tf on n.sender=tf.nubeNumber " +
                //                         " and tf.isDeleted=0 " +
                //                         " left join t_multi_chat_users tm on tm.gid = n.threadsId and tm.nubeNumber = n.sender and tm.removed=0" +
                //                         " where n.threadsId = '"+pconvstId+"'"+")";
                sql =
                    " select * from (select n.*, tf.name as nickName,null as phoneNum,tf.nubeNumber as nubeNumber,tf.name as showName,tf.headUrl," +
                        "                          1 as gender,tf.sex,tf.headUrl as mheadUrl," +
                        "                         tf.name from t_hpu_notices n left join (select isDeleted,nubeNumber,headUrl,sex,name from t_nubefriend  union select 0 as isDeleted , nubeNumber,headUrl,null as sex,nickName as name from t_hpu_friends) tf on n.sender=tf.nubeNumber " +
                        "                         and tf.isDeleted=0 " +
                        "                          where n.threadsId = " + "'" + pconvstId + "'";

                //                             sql = "select * from (select * from " + DtNoticesTable.TABLE_NAME
                //                             + " where " + DtNoticesTable.NOTICE_COLUMN_THREADSID + " = '"
                //                             + pconvstId + "'";

                if (!"0".equals(precvTimeBegin)) {
                    sql = sql + " and " + DtNoticesTable.NOTICE_COLUMN_SENDTIME
                        + " < " + precvTimeBegin;
                }

                sql = sql + " order by " + DtNoticesTable.NOTICE_COLUMN_SENDTIME
                    + " desc " + " limit 0," + ppageCnt + ") order by "
                    + DtNoticesTable.NOTICE_COLUMN_SENDTIME + " asc ";

                CustomLog.d(TAG, "分页查询 DT 动态消息:" + sql);
                cursor = db.rawQuery(sql, null);
                int cursorCount = cursor.getCount();
                CustomLog.d(TAG, "HPU_DT_NOTICE_LIST:" + cursorCount + "   QUERY_PAGE_DT_NOTICES");
                break;
            case COUNT_NEW_NOTICES:
                // 统计新消息数
                //                sql = "select sum(isNews) as newNoticeCnt from t_notices where type in ('"
                //                        + FileTaskManager.NOTICE_TYPE_PHOTO_SEND
                //                        + "','"
                //                        + FileTaskManager.NOTICE_TYPE_VCARD_SEND
                //                        + "','"
                //                        + FileTaskManager.NOTICE_TYPE_VEDIO_SEND + "')";
                cursor = db.rawQuery(sql, null);
                break;
            case NET_PHONE_NEWFRIEND_ITEM: {
                //                SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
                //                qb.appendWhere(NewFriendTable.NEWFRIEND_COLUMN_ID + "="
                //                        + uri.getLastPathSegment());
                //                qb.setTables(NewFriendTable.TABLENAME);
                //                cursor = qb.query(db, projection, selection, selectionArgs, null,
                //                        null, sortOrder);
                //                qb = null;
            }
            break;
            //TODO:unread new friend count
            case GET_UNREAD_NEW_FRIEND:
                //                sql = "select distinct " + NewFriendTable.NEWFRIEND_COLUMN_NUMBER
                //                        + " from " + NewFriendTable.TABLENAME + " where "
                //                        + NewFriendTable.NEWFRIEND_COLUMN_ISNEW + " =1 and "
                //                        + NewFriendTable.NEWFRIEND_COLUMN_VISIBLE + " !=1";
                //                cursor=db.rawQuery(sql, null);
                //                LogUtil.d("查询unread新朋友:sql="+sql);
                break;
            case NET_PHONE_NEWFRIEND_LIST:
                //                cursor = db.query(NewFriendTable.TABLENAME, projection, selection,
                //                        selectionArgs, null, null, sortOrder);
                break;
            case ALARM_LIST:
                //                cursor = db.query(AlarmTable.TABLENAME, projection, selection,
                //                        selectionArgs, null, null, sortOrder);
                break;
            // case ALARM_ITEM:
            // cursor = db.query(AlarmTable.TABLENAME, projection, selection,
            // selectionArgs, null, null, sortOrder);
            // break;
            case CALL_RECORD_LIST:
                sql =
                    "select t.contactId as contactId,t.nubeNumber as nubeNumber,t.number as number,"
                        +
                        "case length(n.name) when 0 then (case length(n.nickName) when 0 then n.number else n.nickName end) "
                        + "else n.name end as name "
                        +
                        ",t.callDirection as callDirection ,t.callType as callType ,t.time as time ,t.headUrl as headUrl,"
                        +
                        " t.lastTime as lastTime,t.dataType as dataType,t.isNew as isNew ,t.name as savedname "
                        + "from t_callrecords t "
                        +
                        "left join (select nubeNumber as number,nickName as nickName, name as name from t_nubefriend  where isDeleted = 0)n on n.number=t.nubeNumber "
                        + "order by time desc";
                cursor = db.rawQuery(sql, null);
                break;
            case CALL_RECORD_ITEM: {
                //                cursor = db.query(CallRecordsColumn.TABLENAME, projection,
                //                        selection, selectionArgs, null, null, sortOrder);
                // SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
                // qb.appendWhere(CallRecordsColumn.NUBENUMBER + "="
                // + uri.getLastPathSegment());
                // qb.setTables(CallRecordsColumn.TABLENAME);
                // cursor = qb.query(db, projection, selection, selectionArgs, null,
                // null, sortOrder);
                // qb = null;
            }
            break;
            case MATCH_CONTACT_BYNUMBER_ITEM:
                cursor = db.query(NubeFriendColumn.TABLENAME, projection,
                    selection, selectionArgs, null, null, sortOrder);
                break;
            case MATCH_CONTACT_BYNUBENUMBER_ITEM:
                cursor = db.query(NubeFriendColumn.TABLENAME, projection,
                    selection, selectionArgs, null, null, sortOrder);
                break;
            case QUERY_LINKMAN_BY_NUBENUMBER:
                // 查询联系人信息
                String nubeNumbes = uri.getPathSegments().get(1);
                //                sql =
                //                    "select contactId contactId,name name,nickname nickname,firstName firstName,lastName lastName,"
                //                        +
                //                        " lastTime lastTime,isDeleted isDeleted,pym pym,number number,nubeNumber nubeNumber,isMutualTrust isMutualTrust, headUrl headUrl,fullPym fullPym,contactUserId contactUserId,sex sex,reserveStr1 reserveStr1 "
                //                        +
                //                        " from t_nubefriend tn where tn.isDeleted=0 and tn.nubeNumber='"
                //                        + nubeNumbes + "'";
                sql = "select * from(select contactId,name, nickname,firstName, lastName," +
                    "  lastTime, isDeleted, pym, number, nubeNumber, isMutualTrust,  headUrl, fullPym, contactUserId,sex sex,reserveStr1 reserveStr1" +
                    " from t_nubefriend tn where tn.isDeleted=0 union select null as contactId,nickName as name,nickName as nickname,null as fistName " +
                    " ,null as lastName,null as lastTime,0 as isDeleted,null as pym,null as number,nubeNumber as nubeNumber,null as isMutuaTrust,headUrl as headUrl,null as fullPym ,null as contactUserId,null as sex,null as reserveStr1 from " +
                    "t_hpu_friends) t where t.nubeNumber = '" + nubeNumbes + "'";
                cursor = db.rawQuery(sql, null);
                CustomLog.i(TAG, "QUERY_LINKMAN_BY_NUBENUMBER | sql = " + sql);
                break;
            case QUERY_SOURCE_ITEM:
                // 查询不可见系统id
                sql =
                    "select nubeNumber nubeNumber,isMutualTrust isMutualTrust,sourcesId sourcesId from t_nubefriend tn "
                        +
                        " where tn.isMutualTrust=5 and tn.isDeleted=0 and nubeNumber is not null and ltrim(rtrim(nubeNumber)) <> '' ";
                CustomLog.d(TAG, "查询不可见系统联系人id" + sql);
                cursor = db.rawQuery(sql, null);
                break;
            case NETPHONE_THREADS_GETALL:
                // TODO:on n1.receiver=f.nubeNumber or n1.sender=f.nubeNumber，因为nubeFriend表中没有自己，
                //所以关联的总是消息的对方--2015/7/1  recipientIds 为免打扰字段
                CustomLog.d(TAG, "provider 查询未置顶的会话消息");
                sql = "select th.id as threadsId,f.name,n2.nickName,n2.phoneNum,n2.nubeNumber"
                    +
                    ",th.lastTime as lastTime,th.reserverStr1,th.recipientIds,th.type as threadType"
                    + ",n2.noticesId,n2.sender,n2.sendTime,n2.status,n2.noticeType"
                    + ",n2.body,n2.isNews,th.extendInfo,f.headUrl,th.top"
                    + ",tg.headUrl as gHeadUrl,tg.gName from t_threads th left join "
                    +
                    "(select sum(n1.isNews) as isNews,max((case when n1.sendTime = 1 then n1.receivedTime else n1.sendTime end )) as sendTime"
                    + ",n1.id as noticesId,n1.sender,n1.type as noticeType,n1.status"
                    + ",n1.body,n1.threadsId,f.name,tu.nickName,tu.phoneNum,tu.nubeNumber"
                    +
                    ",f.headUrl from t_notices n1 left join (select nubeNumber,headUrl,name from t_nubefriend where isDeleted = 0"
                    + " union select nubeNumber,headUrl,nickName as name from t_hpu_friends) f "
                    +
                    "on n1.receiver=f.nubeNumber or n1.sender=f.nubeNumber left join t_multi_chat_users tu"
                    + " on n1.threadsId=tu.gid and n1.sender=tu.nubeNumber group by "
                    + "n1.threadsId) n2 on th.id=n2.threadsId left join t_multi_chat_groups "
                    +
                    "tg on th.id = tg.gid left join (select nubeNumber,headUrl,name from t_nubefriend where isDeleted = 0"
                    +
                    " union select nubeNumber,headUrl,nickName as name from t_hpu_friends) f on th.recipientIds = f.nubeNumber where th.top=0 order by th.top desc,th.lastTime desc";
                cursor = db.rawQuery(sql, null);
                break;
            case NETPHONE_THREADS_GETALL_TOP:
                // TODO:on n1.receiver=f.nubeNumber or n1.sender=f.nubeNumber，因为nubeFriend表中没有自己，
                //所以关联的总是消息的对方--2015/7/1  recipientIds 为免打扰字段
                CustomLog.d(TAG, "provider 查询置顶的会话消息");
                sql = "select th.id as threadsId,f.name,n2.nickName,n2.phoneNum,n2.nubeNumber"
                    +
                    ",th.lastTime as lastTime,th.reserverStr1,th.recipientIds,th.type as threadType"
                    + ",n2.noticesId,n2.sender,n2.sendTime,n2.status,n2.noticeType"
                    + ",n2.body,n2.isNews,th.extendInfo,f.headUrl,th.top"
                    + ",tg.headUrl as gHeadUrl,tg.gName from t_threads th left join "
                    +
                    "(select sum(n1.isNews) as isNews,max((case when n1.sendTime = 1 then n1.receivedTime else n1.sendTime end )) as sendTime"
                    + ",n1.id as noticesId,n1.sender,n1.type as noticeType,n1.status"
                    + ",n1.body,n1.threadsId,f.name,tu.nickName,tu.phoneNum,tu.nubeNumber"
                    +
                    ",f.headUrl from t_notices n1 left join (select nubeNumber,headUrl,name from t_nubefriend where isDeleted = 0"
                    + " union select nubeNumber,headUrl,nickName as name from t_hpu_friends) f "
                    +
                    "on n1.receiver=f.nubeNumber or n1.sender=f.nubeNumber left join t_multi_chat_users tu"
                    + " on n1.threadsId=tu.gid and n1.sender=tu.nubeNumber group by "
                    + "n1.threadsId) n2 on th.id=n2.threadsId left join t_multi_chat_groups "
                    +
                    "tg on th.id = tg.gid left join (select nubeNumber,headUrl,name from t_nubefriend where isDeleted = 0"
                    +
                    " union select nubeNumber,headUrl,nickName as name from t_hpu_friends) f on th.recipientIds = f.nubeNumber where th.top=1 order by th.reserverStr2 desc";
                cursor = db.rawQuery(sql, null);
                break;

            case GROUP_3_MEMBERS_NAME_BY_GID:
                CustomLog.d(TAG, "provider 查询群聊名称");
                String gid = uri.getLastPathSegment();
                sql =
                    " select tn.name, tm.nickName,tm.phoneNum,tg.gName,tm.nubeNumber from t_multi_chat_users tm"
                        + " left join t_nubefriend tn on tm.nubeNumber=tn.nubeNumber "
                        + " and tn.isDeleted=0 "
                        + " left join t_multi_chat_groups tg on tm.gid=tg.gid where tm.gid='"
                        + gid + "' and tm.removed=" + "'" + GroupMemberTable.REMOVED_FALSE + "'"
                        + " limit 3 offset 0 ";
                cursor = db.rawQuery(sql, null);
                break;

            case NETPHONE_THREADS_ITEM: {
                CustomLog.d(TAG, "provider query NETPHONE_THREADS_ITEM");
                SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
                qb.appendWhere(ThreadsTable.THREADS_COLUMN_ID + "="
                    + uri.getLastPathSegment());
                qb.setTables(ThreadsTable.TABLENAME);
                cursor = qb.query(db, projection, selection, selectionArgs, null,
                    null, sortOrder);
                qb = null;
            }
            break;
            case NETPHONE_THREADS_LIST:
                CustomLog.d(TAG, "provider query NETPHONE_THREADS_LIST");
                cursor = db.query(ThreadsTable.TABLENAME, projection, selection,
                    selectionArgs, null, null, sortOrder);
                break;
            case NUMBER_CACHE_ITEM: {
                //                SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
                //                qb.appendWhere(NumberCacheTable.NUMBERCACHE_COLUMN_ID + "="
                //                        + uri.getLastPathSegment());
                //                qb.setTables(NumberCacheTable.TABLENAME);
                //                cursor = qb.query(db, projection, selection, selectionArgs, null,
                //                        null, sortOrder);
                //                qb = null;
            }
            break;
            case NUMBER_CACHE_LIST:
                //                cursor = db.query(NumberCacheTable.TABLENAME, projection,
                //                        selection, selectionArgs, null, null, sortOrder);
                break;
            case GROUP_LIST:
                cursor = db.query(GroupTable.TABLENAME, projection, selection,
                    selectionArgs, null, null, sortOrder);
                break;
            case GROUP_MEMBER_LIST:
                cursor = db.query(GroupMemberTable.TABLENAME, projection,
                    selection, selectionArgs, null, null, sortOrder);
                break;
            case QUERY_GROUP_MEMBERS:
                sql = "select gm.id,gm.gid,gm.mid,gm.uid,gm.nubeNumber,gm.phoneNum,gm.nickName,"
                    + "gm.groupNick,gm.showName,gm.headUrl,gm.removed,nf.name,gm.gender "
                    +
                    " from t_multi_chat_users gm left join (select nubeNumber,isDeleted,name from t_nubefriend union select nubeNumber,0 as isDeleted,nickName as name from t_hpu_friends) nf on gm.nubeNumber=nf.nubeNumber "
                    + " and nf.isDeleted=0 "
                    + " where gm.gid='"
                    + uri.getPathSegments().get(1)
                    + "' and "
                    + " gm.removed="
                    + GroupMemberTable.REMOVED_FALSE
                    + " order by gm.id asc";
                CustomLog.d(TAG, "provider 查询未被移出的群的全部成员:" + sql);
                cursor = db.rawQuery(sql, null);
                break;
            case QUERY_GROUP_MEMBER_ITEM:
                sql = "select gm.id,gm.gid,gm.mid,gm.uid,gm.nubeNumber,gm.phoneNum,gm.nickName,"
                    + "gm.groupNick,gm.showName,gm.headUrl,gm.removed,nf.name,gm.gender "
                    +
                    " from t_multi_chat_users gm left join t_nubefriend nf on gm.nubeNumber=nf.nubeNumber "
                    + " and nf.isDeleted=0 "
                    + " where gm.gid='"
                    + uri.getPathSegments().get(1)
                    + "' and "
                    + " gm.nubeNumber='"
                    + uri.getPathSegments().get(2) + "'";
                CustomLog.d(TAG, "provider 查询群成员:" + sql);
                cursor = db.rawQuery(sql, null);
                break;
            case QUERY_GROUP_MEMBER_CNT:
                sql = "select count(*) from t_multi_chat_users " + " where gid='"
                    + uri.getPathSegments().get(1) + "' and " + " removed="
                    + GroupMemberTable.REMOVED_FALSE;
                CustomLog.d(TAG, "provider 查询群成员个数:" + sql);
                cursor = db.rawQuery(sql, null);
                break;
            case QUERY_THREAD_GROUPS:
                // 查询全部群聊（已产生会话的，且自身未被移除的群聊）
                String nubeNum = uri.getPathSegments().get(1);
                sql = "select tg.gid,tg.gName,tg.headUrl,th.id,tm.removed "
                    + " from t_multi_chat_groups tg left join t_threads th "
                    + " on tg.gid = th.recipientIds left join t_multi_chat_users tm "
                    + " on tg.gid = tm.gid and tm.nubeNumber = '" + nubeNum
                    + "' where 1=1";
                CustomLog.d(TAG, "provider 查询全部有效群聊:" + sql);
                cursor = db.rawQuery(sql, null);
                break;
            // case GROUP_MEMBER_HEADURL:
            // cursor = db.query(GroupMemberTable.TABLENAME, projection,
            // selection, selectionArgs, null, null, sortOrder);
            // break;
            // case QUERY_GROUP_MEMBER_NAME:
            // cursor = db.query(GroupMemberTable.TABLENAME, projection,
            // selection, selectionArgs, null, null, sortOrder);
            // break;

            case PUBLIC_NO_HISTORY:
                //                cursor = db.query(PublicNOHistoryTable.TABLENAME, projection, selection,
                //                        selectionArgs, null, null, sortOrder);
                break;
            case PUBLIC_NO_CACHE:
                //                cursor = db.query(PublicNOCacheTable.TABLENAME, projection, selection,
                //                        selectionArgs, null, null, sortOrder);
                break;
            case SEARCH_HIS:
                //                cursor = db.query(SearchHistoryTable.TABLENAME, projection, selection,
                //                        selectionArgs, null, null, sortOrder);
                break;
            case MEET_HIS:
                //                cursor = db.query(MeetHistoryTable.TABLENAME, projection, selection,
                //                        selectionArgs, null, null, sortOrder);
                break;
            case COLLECT_TABLE:
                cursor = db.query(CollectionTable.TABLENAME, projection, selection,
                    selectionArgs, null, null, sortOrder);
                break;
            case QUERY_ALL_COLLECTION_RECORD:
                sql = "select c1.id" +
                    ",c1.operateTime" +
                    ",c1.status" +
                    ",c1.syncStatus" +
                    ",c1.operatorNube" +
                    ",c1.type" +
                    ",c1.body" +
                    ",c1.extInfo" +
                    ",tf.headUrl" +
                    ",tf.name" +
                    ",tf.nickname" +
                    ",tf.number" +
                    " from t_collect c1 left join t_nubefriend tf on c1.operatorNube=tf.nubeNumber and tf.isDeleted=0 " +
                    " order by c1.operateTime desc";
                cursor = db.rawQuery(sql, null);
                break;
            case FRIEND_RELATION_TABLE:
                CustomLog.i(TAG, "query FRIEND_RELATION_TABLE");
                cursor = db.query(FriendRelationTable.TABLENAME, null, selection,
                    selectionArgs, null, null, null);
                break;
            case STRANGER_MESSAGE_TABLE:
                CustomLog.i(TAG, "query STRANGER_MESSAGE_TABLE");
                cursor = db.query(StrangerMessageTable.TABLENAME, null, selection,
                    selectionArgs, null, null, sortOrder);
                break;
            case STRANGER_MESSAGE_TABLE_QUERY_NOT_READ_MSG:
                CustomLog.i(TAG, "query STRANGER_MESSAGE_TABLE_QUERY_NOT_READ_MSG");
                sql = "select * from t_stranger_msg where  isRead = 0 group by strangerNubeNumber";
                cursor = db.rawQuery(sql, null);
                break;
            case T_FRIENDS_RELATION_ORDER_BY_UPDATE_TIME:
                CustomLog.i(TAG, "query T_FRIENDS_RELATION_ORDER_BY_UPDATE_TIME");
                sql
                    = "select * from t_friends_relation inner join(select  strangerNubeNumber,max(time) maxTime from  t_stranger_msg group by strangerNubeNumber) c on  t_friends_relation.nubeNumber=c.strangerNubeNumber  where t_friends_relation.isDeleted=0 order by maxTime desc";
                cursor = db.rawQuery(sql, null);
                break;
            case QUERY_NOTICES_DT_COUNT_T:
                CustomLog.i(TAG, "query QUERY_NOTICES_DT_COUNT_T 查询诊疗消息");
                cursor = db.query(DtNoticesTable.TABLE_NAME, projection, selection,
                    selectionArgs, null, null, sortOrder);
                break;
            default:
                break;
        }
        if (null != cursor) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;

    }


    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        CustomLog.i(TAG, "insert()");

        long rowID = 0;
        Uri _uri = uri;

        if (!checkProvider()) {
            return null;
        }

        switch (uriMatcher.match(uri)) {
            case HPU_DT_NOTICE_LIST:
                CustomLog.d(TAG, "provider insert dt notices data item");
                rowID = db.insert(DtNoticesTable.TABLE_NAME, null, values);
                if (rowID > -1) {
                    _uri = ContentUris.withAppendedId(
                        ProviderConstant.NETPHONE_HPU_NOTICE_URI, rowID);
                    getContext().getContentResolver().notifyChange(_uri, null);
                }
                break;
            case NET_PHONE_ACTIVITY_ITEM:
                // 插入该活动 记录
                //                rowID = db.insert(ActivityTable.TABLENAME, null, values);
                //                if (rowID > -1) {
                //                    _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
                //                    getContext().getContentResolver().notifyChange(_uri, null);
                //                }
                break;
            case INSERT_LINKMAN_ITEM:
                //                CustomLog.d(TAG,("provider insert friendsTable data item");
                rowID = db.insert(NubeFriendColumn.TABLENAME, null, values);
                if (rowID > -1) {
                    _uri = ContentUris.withAppendedId(uri, rowID);
                    getContext().getContentResolver().notifyChange(_uri, null);//通知注册者更新该条数据
                }
                break;
            case INSERT_BUTEL_LINKMAN:
                /** 开放给第三方，应用内请勿使用和修改 */
                CustomLog.d(TAG, "provider insert butel linkman 第三方插入nube好友");
                // 本地已有该联系人，不做重复插入
                String matchSql = "select * from " + NubeFriendColumn.TABLENAME
                    + " where nubeNumber = " + values.getAsString("nubeNumber")
                    + " and isDeleted=0";
                Cursor matchCursor = null;
                try {
                    matchCursor = db.rawQuery(matchSql, null);
                    if (matchCursor != null && matchCursor.getCount() > 0) {
                        CustomLog.d(TAG, "第三方插入好友，发现nube表中已有该好友nubeNumber="
                            + values.getAsString("nubeNumber"));
                        break;
                    }
                } catch (Exception e) {
                    //                    CustomLog.e(TAG,"第三方插入nube好友时， 根据号码匹配联系人 Exception", e);
                } finally {
                    if (matchCursor != null) {
                        matchCursor.close();
                        matchCursor = null;
                    }
                }
                //
                //                if (values != null) {
                //                    values.put("contactId", CommonUtil.getUUID());
                //                    values.put("isDeleted", 0);
                //                    values.put("isOnline", 0);
                //                    values.put("isMutualTrust", 1);
                //                    values.put("syncStat", 0);
                //                    values.put("userType", 0);
                //
                //                    String pym = CommonUtil.getPymByParams(values.getAsString("name"),
                //                            values.getAsString("nickname"), values.getAsString("nubeNumber"));
                //                    values.put(NubeFriendColumn.FULLPYM, pym);
                //                }
                //
                //                rowID = db.insert(NubeFriendColumn.TABLENAME, null, values);
                //                if (rowID > -1) {
                //                    _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
                //                    getContext().getContentResolver().notifyChange(_uri, null);
                //
                //                    // 插入成功后，广播通知同步联系人
                //                    Intent syncIntent = new Intent(BizConstant.SYNC_START_ACTION);
                //                    getContext().sendBroadcast(syncIntent);
                //                }
                break;

            case NET_PHONE_NOTICE_LIST:
                CustomLog.d(TAG, "insert chat list notices");
                rowID = db.insert(NoticesTable.TABLENAME, null, values);
                if (rowID > -1) {
                    _uri = ContentUris.withAppendedId(
                        ProviderConstant.NETPHONE_NOTICE_URI, rowID);
                    getContext().getContentResolver().notifyChange(_uri, null);
                }
                break;
            case NET_PHONE_NEWFRIEND_LIST:
                //                rowID = db.insert(NewFriendTable.TABLENAME, null, values);
                //                if (rowID > -1) {
                //                    _uri = ContentUris.withAppendedId(
                //                            ProviderConstant.NETPHONE_NEWFRIEND_URI, rowID);
                //                    getContext().getContentResolver().notifyChange(_uri, null);
                //                }
                break;
            case CALL_RECORD_LIST:
                //                rowID = db.insert(CallRecordsColumn.TABLENAME, null, values);
                //                if (rowID > -1) {
                //                    _uri = ContentUris.withAppendedId(
                //                            ProviderConstant.NETPHONE_CALLRECORDS_URI, rowID);
                //                    getContext().getContentResolver().notifyChange(_uri, null);
                //                }
                break;
            case ADD_FAMILY_NUMBER:
                CustomLog.d(TAG, "provider insert family number data item");
                String nubeNumber = uri.getPathSegments().get(1);
                // 先查询此nube号是否已经是i回家号，若是，则结束；若不是则插入；
                String sql = "select id id from t_family tf where tf.nubeNumber ='"
                    + nubeNumber + "'";
                CustomLog.d(TAG, "插入i回家号到family时，先查询此nube号是否已经是i回家号" + sql);
                Cursor cursor = db.rawQuery(sql, null);
                if (cursor != null && cursor.getCount() > 0) {
                    CustomLog.d(TAG, "该nube号已经是i回家号，不再插入family表");
                    break;
                }
                cursor.close();
                //                rowID = db.insert(FamilyColumn.TABLENAME, null, values);
                //                if (rowID > -1) {
                //                    _uri = ContentUris.withAppendedId(
                //                            ProviderConstant.NETPHONE_FAMILY_NUMBER_URI, rowID);
                //                    getContext().getContentResolver().notifyChange(_uri, null);
                //                }
                break;
            case DEVICE_LIST:
                //                LogUtil.d("provider insert device data item");
                //                String nubeNumberForDevice = values
                //                        .getAsString(DeviceColumn.NUBE_NUMBER);
                //                // 先查询此nube号是否已经是i回家号，若是，则结束；若不是则插入；
                //                String sqlDevice = "select status from t_device where nubeNum ='"
                //                        + nubeNumberForDevice + "'";
                //
                //                Cursor cursorDevice = db.rawQuery(sqlDevice, null);
                //                if (cursorDevice != null && cursorDevice.moveToFirst()) {
                //                    int status = cursorDevice.getInt(0);
                //                    if (status == DeviceColumn.STATUS_VERIFY
                //                            && values.getAsInteger(DeviceColumn.STATUS) == DeviceColumn.STATUS_NORMAL) {
                //                        // 原设备状态是等待验证，且新设备状态是正常的场合，将状态更新成正常
                //                        ContentValues val = new ContentValues();
                //                        val.put(DeviceColumn.STATUS, DeviceColumn.STATUS_NORMAL);
                //                        val.put(DeviceColumn.AUTO_DETECT,
                //                                values.getAsInteger(DeviceColumn.AUTO_DETECT));
                //                        val.put(DeviceColumn.RECEIVE_ALERTS,
                //                                values.getAsInteger(DeviceColumn.RECEIVE_ALERTS));
                //                        val.put(DeviceColumn.HOUSE_KEEPING,
                //                                values.getAsInteger(DeviceColumn.HOUSE_KEEPING));
                //                        int count = db.update(DeviceColumn.TABLENAME, val,
                //                                DeviceColumn.NUBE_NUMBER + " = ? ",
                //                                new String[] { nubeNumberForDevice });
                //                        if (count > 0) {
                //                            _uri = ContentUris.withAppendedId(
                //                                    ProviderConstant.NETPHONE_DEVICE_URI, count);
                //                            getContext().getContentResolver().notifyChange(_uri,
                //                                    null);
                //                        }
                //                    }
                //                } else {
                //                    rowID = db.insert(DeviceColumn.TABLENAME, null, values);
                //                    if (rowID > -1) {
                //                        _uri = ContentUris.withAppendedId(
                //                                ProviderConstant.NETPHONE_DEVICE_URI, rowID);
                //                        getContext().getContentResolver().notifyChange(_uri, null);
                //                    }
                //                }
                //                cursorDevice.close();
                break;
            case ADD_REPLYMSG:
                CustomLog.d(TAG, "provider insert reply msg data item");
                //                rowID = db.insert(ReplyMsgColumn.TABLENAME, null, values);
                //                if (rowID > -1) {
                //                    _uri = ContentUris.withAppendedId(
                //                            ProviderConstant.NETPHONE_REPLY_MSG_URI, rowID);
                //                    getContext().getContentResolver().notifyChange(_uri, null);
                //                }
                break;
            case NETPHONE_THREADS_LIST:
                CustomLog.d(TAG, "provider insert threads data item");
                rowID = db.insert(ThreadsTable.TABLENAME, null, values);
                if (rowID > -1) {
                    _uri = ContentUris.withAppendedId(
                        ProviderConstant.NETPHONE_THREADS_URI, rowID);
                    getContext().getContentResolver().notifyChange(_uri, null);
                }
                break;
            case ALARM_LIST:
                CustomLog.d(TAG, "provider insert alarm data item");
                //                rowID = db.insert(AlarmTable.TABLENAME, null, values);
                //                if (rowID > -1) {
                //                    _uri = ContentUris.withAppendedId(
                //                            ProviderConstant.NETPHONE_ALARM_URI, rowID);
                //                    getContext().getContentResolver().notifyChange(_uri, null);
                //                }
                break;
            case NUMBER_CACHE_LIST:
                //                rowID = db.insert(NumberCacheTable.TABLENAME, null, values);
                //                if (rowID > -1) {
                //                    _uri = ContentUris.withAppendedId(
                //                            ProviderConstant.NETPHONE_NUMBERCACHE_URI, rowID);
                //                    getContext().getContentResolver().notifyChange(_uri, null);
                //                }
                break;
            case GROUP_LIST:
                // 插入群聊分组
                rowID = db.insert(GroupTable.TABLENAME, null, values);
                if (rowID > -1) {
                    _uri = ContentUris.withAppendedId(
                        ProviderConstant.NETPHONE_GROUP_URI, rowID);
                    getContext().getContentResolver().notifyChange(_uri, null);
                }
                break;
            case GROUP_MEMBER_LIST:
                // 插入群聊成员
                rowID = db.insert(GroupMemberTable.TABLENAME, null, values);
                if (rowID > -1) {
                    _uri = ContentUris.withAppendedId(
                        ProviderConstant.NETPHONE_GROUP_MEMBER_URI, rowID);
                    getContext().getContentResolver().notifyChange(_uri, null);
                }
                break;
            case PUBLIC_NO_HISTORY:
                //                rowID = db.insert(PublicNOHistoryTable.TABLENAME, null, values);
                //                if (rowID > -1) {
                //                    _uri = ContentUris.withAppendedId(
                //                            ProviderConstant.NETPHONE_PUBLIC_NO_HISTORY_URI, rowID);
                //                    getContext().getContentResolver().notifyChange(_uri, null);
                //                }
                break;
            case PUBLIC_NO_CACHE:
                //                rowID = db.insert(PublicNOCacheTable.TABLENAME, null, values);
                //                if (rowID > -1) {
                //                    _uri = ContentUris.withAppendedId(
                //                            ProviderConstant.NETPHONE_PUBLIC_NO_CACHE_URI, rowID);
                //                    getContext().getContentResolver().notifyChange(_uri, null);
                //                }
                break;
            case SEARCH_HIS:
                //                rowID = db.insert(SearchHistoryTable.TABLENAME, null, values);
                //                if (rowID > -1) {
                //                    _uri = ContentUris.withAppendedId(
                //                            ProviderConstant.NETPHONE_SEARCH_HIS_URI, rowID);
                //                    getContext().getContentResolver().notifyChange(_uri, null);
                //                }
                break;
            case MEET_HIS:
                //                rowID = db.insert(MeetHistoryTable.TABLENAME, null, values);
                //                if (rowID > -1) {
                //                    _uri = ContentUris.withAppendedId(
                //                            ProviderConstant.NETPHONE_MEET_HIS_URI, rowID);
                //                    getContext().getContentResolver().notifyChange(_uri, null);
                //                }
                break;
            case COLLECT_TABLE:
                rowID = db.insert(CollectionTable.TABLENAME, null, values);
                //                if (rowID > -1) {
                //                    _uri = ContentUris.withAppendedId(
                //                            ProviderConstant.NETPHONE_MEET_HIS_URI, rowID);
                //                    getContext().getContentResolver().notifyChange(_uri, null);
                //                }
                break;
            case FRIEND_RELATION_TABLE:
                // 好友关系插入数据
                CustomLog.i(TAG, "insert FRIEND_RELATION_TABLE");
                rowID = db.insert(FriendRelationTable.TABLENAME, null, values);
                if (rowID > -1) {
                    _uri = ContentUris.withAppendedId(
                        ProviderConstant.Friend_Relation_URI, rowID);
                    getContext().getContentResolver().notifyChange(_uri, null);
                }
                break;
            case STRANGER_MESSAGE_TABLE:
                // 陌生人消息插入数据
                CustomLog.i(TAG, "insert STRANGER_MESSAGE_TABLE");
                rowID = db.insert(StrangerMessageTable.TABLENAME, null, values);
                if (rowID > -1) {
                    _uri = ContentUris.withAppendedId(
                        ProviderConstant.Strange_Message_URI, rowID);
                    getContext().getContentResolver().notifyChange(_uri, null);
                }
                break;
            default:
                break;
        }
        CustomLog.i(TAG, " insert sql 语句执行返回值 rowID==" + rowID);
        return _uri;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        CustomLog.i(TAG, "update()");

        int count = -1;

        if (!checkProvider()) {
            return 0;
        }

        switch (uriMatcher.match(uri)) {
            case NET_PHONE_ACTIVITY_ITEM:
                // 更新该活动 记录
                //                count = db.update(ActivityTable.TABLENAME, values, selection,
                //                        selectionArgs);
                break;
            case UPDATE_SYNC_STATUS:
                CustomLog.d(TAG, "provider update friendsTable sync status");
                count = db.update(NubeFriendColumn.TABLENAME, values, selection,
                    selectionArgs);
                break;
            case UPDATE_LINKMAN_STATUS_ITEM:
                CustomLog.d(TAG, "provider update friendsTable  auth status");
                count = db.update(NubeFriendColumn.TABLENAME, values, selection,
                    selectionArgs);
                break;
            case UPDATE_LINKMAN_ITEM:
                CustomLog.d(TAG, "provider update friendsTable data info");
                count = db.update(NubeFriendColumn.TABLENAME, values, selection,
                    selectionArgs);
                break;
            case UPDATE_NUBE_FRIEND_INFO:
                CustomLog.d(TAG, "provider update headinfo and nickName");
                count = db.update(NubeFriendColumn.TABLENAME, values, selection,
                    selectionArgs);
                break;
            case UPDATE_ONLINE_STATUS_TIME:
                CustomLog.d(TAG, "provider update online status");
                count = db.update(NubeFriendColumn.TABLENAME, values, selection,
                    selectionArgs);
                break;
            case UPDATE_AUTH_STATUS:
                CustomLog.d(TAG, "provider update friendsTable authStatus info");
                count = db.update(NubeFriendColumn.TABLENAME, values, selection,
                    selectionArgs);
                break;
            case UPDATE_LINKMAN_TIME:
                CustomLog.d(TAG, "provider update friendsTable time info");
                count = db.update(NubeFriendColumn.TABLENAME, values, selection,
                    selectionArgs);
                break;
            case NET_PHONE_NOTICE_ITEM:
                CustomLog.d(TAG, "provider update notices data item");
                count = db.update(
                    NoticesTable.TABLENAME,
                    values,
                    NoticesTable.NOTICE_COLUMN_ID
                        + "="
                        + uri.getLastPathSegment()
                        + (!TextUtils.isEmpty(selection) ? " AND ("
                        + selection + ')' : ""), selectionArgs);
                break;
            case HPU_DT_NOTICE_ITEM:
                CustomLog.d(TAG, "provider update dt notices data item");
                count = db.update(
                    DtNoticesTable.TABLE_NAME,
                    values,
                    DtNoticesTable.NOTICE_COLUMN_ID
                        + "="
                        + uri.getLastPathSegment()
                        + (!TextUtils.isEmpty(selection) ? " AND ("
                        + selection + ')' : ""), selectionArgs);
                break;
            case NET_PHONE_NOTICE_LIST:
                CustomLog.d(TAG, "update chat list notices");
                count = db.update(NoticesTable.TABLENAME, values, selection,
                    selectionArgs);
                CustomLog.d(TAG, "update chat list notices | count = " + count);
                break;

            case HPU_DT_NOTICE_LIST:
                CustomLog.d(TAG, "provider update dt notices data list");
                count = db.update(DtNoticesTable.TABLE_NAME, values, selection,
                    selectionArgs);
                break;

            case NET_PHONE_NEWFRIEND_ITEM:
                //                count = db.update(
                //                        NewFriendTable.TABLENAME,
                //                        values,
                //                        NewFriendTable.NEWFRIEND_COLUMN_ID
                //                                + "="
                //                                + uri.getLastPathSegment()
                //                                + (!TextUtils.isEmpty(selection) ? " AND ("
                //                                + selection + ')' : ""), selectionArgs);
                break;
            case NET_PHONE_NEWFRIEND_LIST:
                //                count = db.update(NewFriendTable.TABLENAME, values, selection,
                //                        selectionArgs);
                break;
            case CALL_RECORD_LIST:
                //                count = db.update(CallRecordsColumn.TABLENAME, values, selection,
                //                        selectionArgs);
                break;
            case CALL_RECORD_ITEM:
                //                count = db.update(
                //                        CallRecordsColumn.TABLENAME,
                //                        values,
                //                        CallRecordsColumn.NUBENUMBER
                //                                + "="
                //                                + uri.getLastPathSegment()
                //                                + (!TextUtils.isEmpty(selection) ? " AND ("
                //                                + selection + ')' : ""), selectionArgs);
                break;
            case UPDATE_CONTACT_SORTKEY_INFO:
                CustomLog.d(TAG, "provider update friendsTable sortkey info");
                count = db.update(NubeFriendColumn.TABLENAME, values, selection,
                    selectionArgs);
                break;
            case UPDATE_MULT_CONTACT_SORTKEY_INFO:
                // TODO 更新多个联系人排序值
                CustomLog.d(TAG, "provider update friendsTable mult contact sortkey info");
                String fromId = uri.getPathSegments().get(1);
                String toId = uri.getPathSegments().get(2);
                int _fromId = Integer.valueOf(fromId);
                int _toId = Integer.valueOf(toId);
                String sql = null;
                if (_fromId > _toId) { // 向上拖动
                    sql = "update " + NubeFriendColumn.TABLENAME
                        + " set sortkey = sortkey + 1 where sortkey >= " + toId
                        + " And sortkey < " + fromId;
                } else { // 向下拖动
                    sql = "update " + NubeFriendColumn.TABLENAME
                        + " set sortkey = sortkey - 1 where sortkey > "
                        + fromId + " And sortkey <= " + toId;
                }
                // db.rawQuery(sql, null);
                db.execSQL(sql);
                break;
            case BATCH_UPDATE_ONLINE_ITEM:
                // 批量更新好友状态
                CustomLog.d(TAG, "provider batchupdate online status");
                sql = "update " + NubeFriendColumn.TABLENAME + " set isOnline=0";
                CustomLog.d(TAG, "批量更新好友状态" + sql);
                db.execSQL(sql);
                break;
            case DEL_NUBE_CONTACT_BY_ID:
                count = db.update(NubeFriendColumn.TABLENAME, values, selection,
                    selectionArgs);
                break;
            case UPDATE_CONTACT_INFO:
                count = db.update(NubeFriendColumn.TABLENAME, values, selection,
                    selectionArgs);
                break;
            case UPDATE_FAMILY_NUMBER:
                //                count = db.update(FamilyColumn.TABLENAME, values, selection,
                //                        selectionArgs);
                //                if (count > 0) {
                //                    uri = ContentUris.withAppendedId(
                //                            ProviderConstant.NETPHONE_FAMILY_NUMBER_URI, count);
                //                }
                break;
            case DEVICE_LIST:
                //                count = db.update(DeviceColumn.TABLENAME, values, selection,
                //                        selectionArgs);
                //                if (count > 0) {
                //                    uri = ContentUris.withAppendedId(
                //                            ProviderConstant.NETPHONE_DEVICE_URI, count);
                //                }
                break;

            case ALARM_LIST:
                //                count = db.update(AlarmTable.TABLENAME, values, selection,
                //                        selectionArgs);
                //                if (count > 0) {
                //                    uri = ContentUris.withAppendedId(
                //                            ProviderConstant.NETPHONE_ALARM_URI, count);
                //                }
                break;
            case UPDATE_FAMILY_2_NOTNEW:
                // 将新亲情号码更新为非新亲情号码
                //                ContentValues content = new ContentValues();
                //                content.put(FamilyColumn.ISNEWS, FamilyColumn.ISNEWS_NOTNEW);
                //                db.update(FamilyColumn.TABLENAME, content, FamilyColumn.ISNEWS
                //                        + " = ? ", new String[] { "" + FamilyColumn.ISNEWS_NEW });
                break;
            case UPDATE_FAMILY_ISNEW:
                // 将新亲情号码心标用户置为已读
                //                count = db.update(FamilyColumn.TABLENAME, values, selection,
                //                        selectionArgs);
                //                if (count > 0) {
                //                    uri = ContentUris.withAppendedId(
                //                            ProviderConstant.NETPHONE_FAMILY_NUMBER_URI, count);
                //                }
                break;
            case UPDATE_FRIEND_ITEM:
                // 更新新朋友状态之前 先排重然后执行更新状态
                //                String statues = uri.getPathSegments().get(1);
                //                String nubeNumber = uri.getPathSegments().get(2);
                //                count = db.delete(NewFriendTable.TABLENAME,
                //                        NewFriendTable.NEWFRIEND_COLUMN_STATUS + " ='" + statues
                //                                + "' and "
                //                                + NewFriendTable.NEWFRIEND_COLUMN_NUBENUMBER
                //                                + " = " + nubeNumber, null);
                //                if (count > 0) {
                //                    getContext().getContentResolver().notifyChange(uri, null);
                //                }
                //                count = db.update(NewFriendTable.TABLENAME, values, selection,
                //                        selectionArgs);

                break;
            case UPDATE_NEW_NOTICES_READ:
                CustomLog.d(TAG, "UPDATE_NEW_NOTICES_READ");
                // TODO:type待整合
                //                sql = "update " + NoticesTable.TABLENAME + " set "
                //                        + NoticesTable.NOTICE_COLUMN_ISNEW + " = 0 " + "where "
                //                        + NoticesTable.NOTICE_COLUMN_ISNEW + " > 0 and "
                //                        + NoticesTable.NOTICE_COLUMN_TYPE + " in ('"
                //                        + FileTaskManager.NOTICE_TYPE_PHOTO_SEND + "','"
                //                        + FileTaskManager.NOTICE_TYPE_VCARD_SEND + "','"
                //                        + FileTaskManager.NOTICE_TYPE_VEDIO_SEND + "')";
                //                CustomLog.d(TAG,"UPDATE_NEW_NOTICES_READ:" + sql);
                //                db.execSQL(sql);
                break;
            case NETPHONE_THREADS_ITEM:
                CustomLog.d(TAG, "provider update threads data item");
                count = db.update(
                    ThreadsTable.TABLENAME,
                    values,
                    ThreadsTable.THREADS_COLUMN_ID
                        + "="
                        + uri.getLastPathSegment()
                        + (!TextUtils.isEmpty(selection) ? " AND ("
                        + selection + ')' : ""), selectionArgs);
                break;
            case NETPHONE_THREADS_LIST:
                CustomLog.d(TAG, "provider update threads data list");
                count = db.update(ThreadsTable.TABLENAME, values, selection,
                    selectionArgs);
                break;
            case NUMBER_CACHE_LIST:
                CustomLog.d(TAG, "provider update numbercahce data list");
                //                count = db.update(NumberCacheTable.TABLENAME, values, selection,
                //                        selectionArgs);
                break;
            case GROUP_MEMBER_ADD_UPDATE:
                CustomLog.d(TAG, "插入或更新群聊成员");
                String gid = values.getAsString(GroupMemberTable.Column.GID);
                String nubeNum = values
                    .getAsString(GroupMemberTable.Column.NUBE_NUMBER);
                Cursor cursor = db.rawQuery("select " + GroupMemberTable.Column.ID
                        + " from " + GroupMemberTable.TABLENAME + " where "
                        + GroupMemberTable.Column.GID + " = ? and "
                        + GroupMemberTable.Column.NUBE_NUMBER + " = ? ",
                    new String[] { gid, nubeNum });
                if (cursor != null && cursor.moveToFirst()) {
                    // 已存在的场合，更新成员信息
                    count = db.update(GroupMemberTable.TABLENAME, values,
                        GroupMemberTable.Column.ID + " = ? ",
                        new String[] { cursor.getString(0) });
                } else {
                    // 不存在的场合，插入成员信息
                    db.insert(GroupMemberTable.TABLENAME, null, values);
                    count = 1;
                }
                uri = ProviderConstant.NETPHONE_GROUP_MEMBER_URI;

                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }

                break;
            case GROUP_LIST:
                count = db.update(GroupTable.TABLENAME, values, selection,
                    selectionArgs);
                break;
            case GROUP_MEMBER_LIST:
                count = db.update(GroupMemberTable.TABLENAME, values, selection,
                    selectionArgs);
                break;
            case PUBLIC_NO_HISTORY:
                //                count = db.update(PublicNOHistoryTable.TABLENAME, values, selection,
                //                        selectionArgs);
                break;
            case PUBLIC_NO_CACHE:
                //                count = db.update(PublicNOCacheTable.TABLENAME, values, selection,
                //                        selectionArgs);
                break;
            case SEARCH_HIS:
                //                count = db.update(SearchHistoryTable.TABLENAME, values, selection,
                //                        selectionArgs);
                break;
            case MEET_HIS:
                //                count = db.update(MeetHistoryTable.TABLENAME, values, selection,
                //                        selectionArgs);
                break;
            case COLLECT_TABLE:
                count = db.update(CollectionTable.TABLENAME, values, selection,
                    selectionArgs);
                break;
            case FRIEND_RELATION_TABLE:
                CustomLog.i(TAG, " update FRIEND_RELATION_TABLE ");
                count = db.update(FriendRelationTable.TABLENAME, values, selection,
                    selectionArgs);
                break;
            case STRANGER_MESSAGE_TABLE:
                CustomLog.i(TAG, " update STRANGER_MESSAGE_TABLE");
                count = db.update(StrangerMessageTable.TABLENAME, values, selection,
                    selectionArgs);
                break;
            default:
                break;
        }
        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        CustomLog.i(TAG, "update sql语句执行返回值 count == " + count);
        return count;
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        CustomLog.i(TAG, "delete()");

        int count = -1;
        Uri _uri = null;

        if (!checkProvider()) {
            return 0;
        }

        switch (uriMatcher.match(uri)) {

            case DELETE_VCARD_MSG:
                CustomLog.d(TAG, "删除名片的数据:type =  4 or 40");
                count = db.delete(NoticesTable.TABLENAME, selection, selectionArgs);
                if (count > 0) {
                    // 删除 通知
                    _uri = ContentUris.withAppendedId(
                        ProviderConstant.NETPHONE_NOTICE_URI, count);
                    getContext().getContentResolver().notifyChange(_uri, null);
                }
                break;
            case DELETE_NEWFRIEND_TABLE:
                //                CustomLog.d(TAG,"provider clear t_newfriend data");
                //                String sql = " delete from " + NewFriendTable.TABLENAME;
                //                db.execSQL(sql);
                break;
            case DELETE_LINKMAN:
                CustomLog.d(TAG, "provider delete friendsTable data");
                count = db.delete(NubeFriendColumn.TABLENAME, selection,
                    selectionArgs);
                break;
            case DELETE_LINKMAN_STATUS_ITEM:
                CustomLog.d(TAG, "provider delete  data");
                count = db.delete(NubeFriendColumn.TABLENAME, selection,
                    selectionArgs);
                break;
            case NET_PHONE_NOTICE_ITEM:
                CustomLog.d(TAG, "provider delete notices data item");
                count = db.delete(
                    NoticesTable.TABLENAME,
                    NoticesTable.NOTICE_COLUMN_ID
                        + "="
                        + uri.getLastPathSegment()
                        + (!TextUtils.isEmpty(selection) ? " AND ("
                        + selection + ')' : ""), selectionArgs);
                break;
            case HPU_DT_NOTICE_ITEM:
                CustomLog.d(TAG, "provider delete dt notices data item");
                count = db.delete(
                    DtNoticesTable.TABLE_NAME,
                    DtNoticesTable.NOTICE_COLUMN_ID
                        + "="
                        + uri.getLastPathSegment()
                        + (!TextUtils.isEmpty(selection) ? " AND ("
                        + selection + ')' : ""), selectionArgs);
                break;
            case NET_PHONE_NOTICE_LIST:
                CustomLog.d(TAG, "provider delete notices data list");
                count = db.delete(NoticesTable.TABLENAME, selection, selectionArgs);
                break;
            case HPU_DT_NOTICE_LIST:
                CustomLog.d(TAG, "provider delete dt notices data list");
                count = db.delete(DtNoticesTable.TABLE_NAME, selection, selectionArgs);
            case NET_PHONE_NEWFRIEND_ITEM:
                //                count = db.delete(
                //                        NewFriendTable.TABLENAME,
                //                        NewFriendTable.NEWFRIEND_COLUMN_ID
                //                                + "="
                //                                + uri.getLastPathSegment()
                //                                + (!TextUtils.isEmpty(selection) ? " AND ("
                //                                + selection + ')' : ""), selectionArgs);
                break;
            case NET_PHONE_NEWFRIEND_LIST:
                //                count = db.delete(NewFriendTable.TABLENAME, selection,
                //                        selectionArgs);
                break;
            case ALARM_LIST:
                //                count = db.delete(AlarmTable.TABLENAME, selection, selectionArgs);
                //                if (count > 0) {
                //                    uri = ContentUris.withAppendedId(
                //                            ProviderConstant.NETPHONE_ALARM_URI, count);
                //                }
                break;
            // case ALARM_ITEM:
            // count = db
            // .delete(AlarmTable.TABLENAME, selection, selectionArgs);
            // if(count>0){
            // uri=ContentUris.withAppendedId(
            // ProviderConstant.NETPHONE_ALARM_URI, count);
            // }
            // break;
            case CALL_RECORD_LIST:
                //                count = db.delete(CallRecordsColumn.TABLENAME, selection,
                //                        selectionArgs);
                break;
            case CALL_RECORD_ITEM:
                //                count = db.delete(
                //                        CallRecordsColumn.TABLENAME,
                //                        CallRecordsColumn.NUBENUMBER
                //                                + "="
                //                                + uri.getLastPathSegment()
                //                                + (!TextUtils.isEmpty(selection) ? " AND ("
                //                                + selection + ')' : ""), selectionArgs);
                break;
            case DELETE_FAMILY_NUMBER:
                CustomLog.d(TAG, "DELETE_FAMILY_NUMBER");
                //                count = db.delete(FamilyColumn.TABLENAME, selection, selectionArgs);
                //                if (count > 0) {
                //                    uri = ContentUris.withAppendedId(
                //                            ProviderConstant.NETPHONE_FAMILY_NUMBER_URI, count);
                //                }
                break;
            case DEVICE_LIST:
                CustomLog.d(TAG, "DELETE device");
                //                count = db.delete(DeviceColumn.TABLENAME, selection, selectionArgs);
                //                if (count > 0) {
                //                    uri = ContentUris.withAppendedId(
                //                            ProviderConstant.NETPHONE_DEVICE_URI, count);
                //                }
                break;
            case DELETE_REPLYMSG:
                //                count = db.delete(ReplyMsgColumn.TABLENAME, selection,
                //                        selectionArgs);
                //                if (count > 0) {
                //                    uri = ContentUris.withAppendedId(
                //                            ProviderConstant.NETPHONE_REPLY_MSG_URI, count);
                //                }
                break;
            case DELETE_FRIEND_ITEM:
                String nubeNumber = uri.getPathSegments().get(1);
                //                String statues = uri.getPathSegments().get(2);
                //                if ("-1".equals(statues)) {
                //                    sql = "delete from t_newfriend where status <> 5 and nubeNumber = "
                //                            + nubeNumber;
                //                } else {
                //                    sql = "delete from t_newfriend where status = '" + statues
                //                            + "' and nubeNumber = " + nubeNumber;
                //                }
                //                db.execSQL(sql);
                //                CustomLog.d(TAG,"sql=" + sql);
                break;
            case NETPHONE_THREADS_ITEM:
                CustomLog.d(TAG, "provider delete threads data item");
                count = db.delete(
                    ThreadsTable.TABLENAME,
                    ThreadsTable.THREADS_COLUMN_ID
                        + "="
                        + uri.getLastPathSegment()
                        + (!TextUtils.isEmpty(selection) ? " AND ("
                        + selection + ')' : ""), selectionArgs);
                break;
            case NETPHONE_THREADS_LIST:
                CustomLog.d(TAG, "provider delete threads data list");
                count = db.delete(ThreadsTable.TABLENAME, selection, selectionArgs);
                break;
            case NUMBER_CACHE_ITEM:
                CustomLog.d(TAG, "provider delete number cache data item");
                //                count = db.delete(
                //                        NumberCacheTable.TABLENAME,
                //                        NumberCacheTable.NUMBERCACHE_COLUMN_ID
                //                                + "="
                //                                + uri.getLastPathSegment()
                //                                + (!TextUtils.isEmpty(selection) ? " AND ("
                //                                + selection + ')' : ""), selectionArgs);
                break;
            case NUMBER_CACHE_LIST:
                //                CustomLog.d(TAG,"provider delete number cache data list");
                //                count = db.delete(NumberCacheTable.TABLENAME, selection,
                //                        selectionArgs);
                break;
            case GROUP_MEMBER_LIST:
                // 删除成员
                count = db.delete(GroupMemberTable.TABLENAME, selection,
                    selectionArgs);
                break;
            case GROUP_LIST:
                // 删除群聊
                count = db.delete(GroupTable.TABLENAME, selection, selectionArgs);
                break;
            case PUBLIC_NO_HISTORY:
                //                count = db.delete(PublicNOHistoryTable.TABLENAME, selection, selectionArgs);
                break;
            case PUBLIC_NO_CACHE:
                //                count = db.delete(PublicNOCacheTable.TABLENAME, selection, selectionArgs);
                //                break;
            case SEARCH_HIS:
                //                count = db.delete(SearchHistoryTable.TABLENAME, selection, selectionArgs);
                break;
            case MEET_HIS:
                //                count = db.delete(MeetHistoryTable.TABLENAME, selection, selectionArgs);
                break;
            case COLLECT_TABLE:
                count = db.delete(CollectionTable.TABLENAME, selection, selectionArgs);
                break;
            case STRANGER_MESSAGE_TABLE:
                CustomLog.i(TAG, " delete STRANGER_MESSAGE_TABLE");
                count = db.delete(StrangerMessageTable.TABLENAME, selection, selectionArgs);
                if (count > 0) {   // 删除 通知
                    _uri = ContentUris.withAppendedId(
                        ProviderConstant.Strange_Message_URI, count);
                    getContext().getContentResolver().notifyChange(_uri, null);
                }
                break;
            case FRIEND_RELATION_TABLE:
                CustomLog.i(TAG, " delete FRIEND_RELATION_TABLE");
                count = db.delete(FriendRelationTable.TABLENAME, selection, selectionArgs);
                if (count > 0) {// 删除 通知
                    _uri = ContentUris.withAppendedId(
                        ProviderConstant.Friend_Relation_URI, count);
                    getContext().getContentResolver().notifyChange(_uri, null);
                }
                break;
            default:
                break;
        }
        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        CustomLog.i(TAG, "delete sql语句执行返回值 count == " + count);
        return count;
    }


    /**
     * 为contentProvider处理数据库添加事务
     */
    @Override
    public ContentProviderResult[] applyBatch(
        ArrayList<ContentProviderOperation> operations)
        throws OperationApplicationException {
        if (!checkProvider()) {
            return null;
        }
        db.beginTransaction();// 开始事务
        try {
            ContentProviderResult[] results = super.applyBatch(operations);
            db.setTransactionSuccessful();// 设置事务标记为successful
            return results;
        } finally {
            db.endTransaction();// 结束事务
        }
    }


    /**
     * 获取数据库名称
     */
    public String getDBPathName(String userNubeNumber) {
        CustomLog.d(TAG, "nubeNumber=" + userNubeNumber);
        if (TextUtils.isEmpty(userNubeNumber)) {
            return DBConstant.SQLITE_FILE_NAME
                + DBConstant.SQLITE_FILE_NAME_EXTENSION;
        } else {
            return DBConstant.SQLITE_FILE_NAME
                + DBConstant.SQLITE_FILE_CONNECTOR + userNubeNumber
                + DBConstant.SQLITE_FILE_NAME_EXTENSION;
        }
    }
}
