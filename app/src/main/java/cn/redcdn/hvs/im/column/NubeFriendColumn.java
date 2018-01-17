package cn.redcdn.hvs.im.column;

import android.provider.BaseColumns;

/**
 * Desc
 * Created by wangkai on 2017/2/24.
 */

public class NubeFriendColumn implements BaseColumns {
    public static final String TABLENAME = "t_nubefriend"; // 纳贝好友表名
    // 列名
    public static final String CONTACTID = "contactId"; // 好友ID
    public static final String SOURCESID = "sourcesId"; // 系统id
    public static final String NAME = "name"; // 姓名
    public static final String NICKNAME = "nickname"; // 别名
    public static final String FIRSTNAME = "firstName"; // 姓
    public static final String LASTNAME = "lastName"; // 名
    public static final String PYM = "pym"; // 简拼
    public static final String FULLPYM = "fullPym"; // 全拼
    public static final String LASTTIME = "lastTime"; // 时间戳
    public static final String ISDELETED = "isDeleted"; // 删除标记(0:未删除,1:已删除,默认0)
    public static final String ISONLINE = "isOnline"; // 是否在线（0:不在线,1:在线）
    public static final String NUMBER = "number"; // 号码
    public static final String HEADURL = "headUrl";  //  头像地址
    public static final String ISMUTUALTRUST = "isMutualTrust";
    public static final String NUBENUMBER = "nubeNumber"; // Nube号码
    public static final String CONTACTUSERID = "contactUserId"; // Nube号码ID
    public static final String SORTKEY = "sortKey"; // 联系人列表定制排序字段
    public static final String SYNCSTAT = "SyncStat"; // 同步标记(0:需要同步,2:已同步)
    public static final String EXTRAINFO = "extraInfo"; // 联系人附属信息
    public static final String USERTYPE = "userType"; // 用户类型   0：普通用户   1：型用户
    public static final String GROUPTYPE = "groupType"; // 分组类型：1是星标用户列表        2是普通用户列表
    public static final String ISNEWS = "isNews"; // 用来标志i回家号是不是新用户
    public static final String SEX = "sex"; // 性别 0：未知，1：男，2：女
    public static final String RESERVESTR1 = "reserveStr1"; // 扩展保留字段1  20151112被用于 个人名片页面 显示手机号字段 MOBILE_VISIBLE显示，MOBILE_INVISIBLE为不显示
    public static final String RESERVESTR2 = "reserveStr2"; // 扩展保留字段2
    public static final String RESERVESTR3 = "reserveStr3"; // 扩展保留字段3
    public static final String RESERVESTR4 = "reserveStr4"; // 扩展保留字段4
    public static final String RESERVESTR5 = "reserveStr5"; // 扩展保留字段5

    public static final String MOBILE_VISIBLE="visible";
    public static final String MOBILE_INVISIBLE="invisible";

    /**  isMutualTrust 的取值*/
    public static final int KEY_DEFAULT = 0;
    public static final int KEY_VISIBLE = 1; // 数据可见状态
    public static final int KEY_NO_VISIBLE = 5; // 数据不可见状态

    /** 性别：未知 */
    public static final int SEX_UNKNOWN = 0;
    /** 性别：男 */
    public static final int SEX_MALE = 1;
    /** 性别：女 */
    public static final int SEX_FEMALE = 2;
    /** 本地发现用户 */
    public static final String LOCAL_FIND = "1";
    /** 本地手机通讯录用户 */
    public static final String LOCAL_CONTACT = "2";
}
