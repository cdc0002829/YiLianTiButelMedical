package cn.redcdn.hvs.contacts.contact.database;

public final class DBConf {
    public static final int DB_VERSION = 2;
    public static final String DB_NAME = "meeting.db";
    public static final String CONTACT_TB = "t_contact";

    public static final String SQLITE_FILE_CONNECTOR = "_";
    /**
     * 应用pack名称
     */
    public static final String APP_PACKAGE = "cn.redcdn.meeting";
    /** 应用外置存储卡根目录 */
    // public static final String APP_ROOT_FOLDER = "ipNetPhone";
    /**
     * 应用rom根目录
     */
    public static final String APP_ROM_PATH = "data/data/" + APP_PACKAGE;
    public static final String SQLITE_FILE_ROM_FOLDER = APP_ROM_PATH + "/files/";
    /**
     * 数据库文件目录
     */
    public static final String SQLITE_FOLDER = "sqlite";
    /**
     * 数据库文件名
     */
    public static final String SQLITE_FILE_NAME = "mobile_meeting";
    /**
     * 数据库文件扩展名
     */
    public static final String SQLITE_FILE_NAME_EXTENSION = ".sqlite";
    /**
     * 数据库文件名（默认）
     */
    public static final String SQLITE_FILE_NAME_DEFAULT = SQLITE_FILE_NAME
            + SQLITE_FILE_NAME_EXTENSION;
    /**
     * 手机内存中数据库文件
     */
    public static final String SQLITE_FILE_ROM_PATH = SQLITE_FILE_ROM_FOLDER
            + SQLITE_FILE_NAME_DEFAULT;
    // 列名
    public static final String CONTACTID = "contactId"; // 好友ID
    public static final String NAME = "name"; // 姓名
    public static final String NICKNAME = "nickname"; // 别名
    public static final String FIRSTNAME = "firstName"; // 姓
    public static final String LASTNAME = "lastName"; // 名
    public static final String LASTTIME = "lastTime"; // 时间戳
    public static final String ISDELETED = "isDeleted"; // 删除标记(0:未删除,1:已删除,默认0)
    public static final String ISONLINE = "isOnline"; // 是否在线
    public static final String PHONENUMBER = "number"; // 号码
    public static final String ISMUTUALTRUST = "isMutualTrust";
    public static final String NUBENUMBER = "nubeNumber"; // Nube号码
    public static final String CONTACTUSERID = "contactUserId"; // Nube号码ID
    public static final String PYM = "pym"; //pym
    public static final String PINYIN = "fullPym"; // 拼音
    public static final String PICURL = "headUrl"; // 头像地址
    public static final String SORTKEY = "sortKey"; // 排序关键字
    public static final String SYNCSTAT = "syncStat"; // 同步标记(0:需要同步,1:已同步)
    public static final String EXTRAINFO = "extraInfo"; // 额外信息
    public static final String USERTYPE = "userType"; // 用户类型 0：普通用户 1：全能用户
    public static final String SEX = "sex"; //性别
    public static final String USERFROM = "userFrom"; //用户来源 0:视讯号搜索，1:手机通讯录好友推荐，2:手机号搜索， 3:邮箱搜索,  4:二维码扫描,  5:群内添加,  6:陌生人聊天添加
    public static final String APPTYPE = "appType"; // 设备类型

    // 以下字段为医疗云平台项目新增字段
    public static final String EMAIL = "email"; // 邮箱
    public static final String ACCOUNT_TYPE = "accountType"; //账号类型 0: 个人， 1：群
    public static final String WORKUNIT_TYPE = "workUnitType"; //单位类型 1：医院, 2：公司
    public static final String WORK_UNIT = "workUnit";  //公司、医院名称
    public static final String DEPARTMENT = "department";  //科室、部门
    public static final String PROFESSIONAL = "professional";  //职称、职位
    public static final String OFFICETEL = "officeTel";  //科室电话、公司电话
    public static final String SAVE_TO_CONTACTS_TIME = "saveToContactsTime";  //保存到通讯录时间

    //医联体新增类型
    public static final String PHUID = "phuId";
    public static final String PHUNAME = "phuName";
    public static final String PHUNICKNAME = "nickName";
    public static final String UPDATETIME = "updateTime";


    public static final String RESERVESTR1 = "reserveStr1"; // 扩展保留字段1
    public static final String RESERVESTR2 = "reserveStr2"; // 扩展保留字段2
    public static final String RESERVESTR3 = "reserveStr3"; // 扩展保留字段3
    public static final String RESERVESTR4 = "reserveStr4"; // 扩展保留字段4
    public static final String RESERVESTR5 = "reserveStr5"; // 扩展保留字段5
    public static final String CREATE_SQLITE_TB_PRE = "CREATE TABLE IF NOT EXISTS ";
    public static final String CREATE_CONTACT_TB = " (" + CONTACTID
            + " TEXT PRIMARY KEY, " + NAME + " TEXT, " + NICKNAME + " TEXT, "
            + FIRSTNAME + " TEXT, " + LASTTIME + " TIMESTAMP, " + ISDELETED
            + " INT(11), " + PHONENUMBER + " TEXT, " + PICURL + " TEXT, "
            + NUBENUMBER + " TEXT, " + CONTACTUSERID + " TEXT, " + SYNCSTAT
            + " TINYINT, " + USERTYPE + " INT(4), " + USERFROM + " INT(4), "
            + APPTYPE + " TEXT, " + PINYIN + " TEXT, " + RESERVESTR1 + " TEXT, "
            + RESERVESTR2 + " TEXT, " + RESERVESTR3 + " TEXT, " + RESERVESTR4
            + " TEXT, " + RESERVESTR5 + " TEXT " + ")";
    public static final String[] contacTableColumn = new String[]{CONTACTID,
            NAME, NICKNAME, FIRSTNAME, LASTTIME,
            ISDELETED, PHONENUMBER, PICURL, NUBENUMBER,
            CONTACTUSERID, SYNCSTAT, USERTYPE, USERFROM, APPTYPE,
            PINYIN, RESERVESTR1, RESERVESTR2, RESERVESTR3, RESERVESTR4,
            RESERVESTR5, EMAIL, ACCOUNT_TYPE, WORKUNIT_TYPE, WORK_UNIT,
            DEPARTMENT, PROFESSIONAL, OFFICETEL, SAVE_TO_CONTACTS_TIME
    };
}