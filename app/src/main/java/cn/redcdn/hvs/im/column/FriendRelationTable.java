package cn.redcdn.hvs.im.column;

import android.net.Uri;
import cn.redcdn.hvs.im.provider.ProviderConstant;

/**
 * @ClassName: CollectionTable
 */

public class FriendRelationTable {
    public static String TABLENAME = "t_friends_relation";

    public static final String ID = "id";//主键
    public static final String NUBE_NUMBER = "nubeNumber"; // Nube号码
    public static final String NAME = "name"; // 姓名
    public static final String HEAD_URL = "headUrl"; // 头像地址
    public static final String RELATION_TYPE = "relationType"; // 好友关系状态
    public static final String EMAIL = "email"; // 邮箱
    public static final String WORK_UNIT_TYPE = "workUnitType"; //单位类型 1：医院, 2：公司
    public static final String WORK_UNIT = "workUnit";  //公司、医院名称
    public static final String DEPARTMENT = "department";  //科室、部门
    public static final String PROFESSIONAL = "professional";  //职称、职位
    public static final String OFFICETEL = "officeTel";  //科室电话、公司电话
    public static final String USER_FROM = "userFrom"; //用户来源 0:视讯号搜索，1:手机通讯录好友推荐，2:手机号搜索， 3:邮箱搜索,  4:二维码扫描,  5:群内添加,  6:陌生人聊天添加
    public static final String IS_DELETED = "isDeleted"; // 删除标记(0:未删除,1:已删除)
    public static final String PHONE_NUMBER = "phonenumber"; //手机号
    /**
     * 单表查询时，需要的uri
     */
    public static final Uri URI = Uri.withAppendedPath(ProviderConstant.MEDICAL_URI, TABLENAME);

    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLENAME +
        " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
        + NUBE_NUMBER + " VARCHAR(8), "
        + NAME + " TEXT, "
        + HEAD_URL + " TEXT, "
        + RELATION_TYPE + " INTEGER, "
        + EMAIL + " TEXT, "
        + WORK_UNIT_TYPE + " TEXT, "
        + WORK_UNIT + " TEXT, "
        + DEPARTMENT + " TEXT, "
        + PROFESSIONAL + " TEXT, "
        + OFFICETEL + " TEXT, "
        + USER_FROM + " INTEGER, "
        + IS_DELETED + " INTEGER, "
        + PHONE_NUMBER + " TEXT)";
     // CREATE TABLE IF NOT EXISTS  t_friends_relation
     //   ( id INTEGER PRIMARY KEY AUTOINCREMENT,
     //    nubeNumber  VARCHAR(8),
     //    name TEXT,
     //    headUrl TEXT,
     //    relationType   INTEGER,
     //    email  TEXT,
     //    workUnitType  TEXT,
     //    workUnit   TEXT,
     //    department  TEXT,
     //    professional   TEXT,
     //    officeTel   TEXT,
     //    userFrom   INTEGER,
     //    isDeleted  INTEGER,
     //    phonenumber  TEXT);
     //用于在工具库中生成数据

    //插入数据  insert into  t_friends_relation (nubeNumber)  values(?) ,new String [] {"12345678"}
    // insert into t_friends_relation(nubeNumber,name,headUrl,relationType,email,workUnitType,workUnit,department,professional,officeTel,userFrom,updateTime,isDeleted) values("12345678", "caiguo", "www,hvs,com", 2,
    //     "119@qq.com", "beijing", "304", "erke",
    //     "主任", "123456778", 1,1234566, 0)

}
