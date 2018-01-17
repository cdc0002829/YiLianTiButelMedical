package cn.redcdn.hvs.im.column;

import android.net.Uri;
import cn.redcdn.hvs.im.provider.ProviderConstant;

/**
 * @ClassName: CollectionTable
 */

public class StrangerMessageTable {
    public static String TABLENAME = "t_stranger_msg";
    public static final String ID = "id"; // 主键
    public static final String STRANGER_NUBE_NUMBER = "strangerNubeNumber"; // Nube号码
    public static final String STRANGER_HEAD = "strangerHead"; // 陌生人头像
    public static final String STRANGER_NAME = "strangerName"; // 陌生人名称
    public static final String MSG_DIRECTION = "msgDirection"; // 消息方向
    public static final String MSG_CONTENT = "msgContent"; // 消息内容
    public static final String TIME = "time"; //时间
    public static final String IS_Read = "isRead"; //是否被查看
    public static final int HAS_READ = 1; //已读
    public static final int NOT_READ = 0; //未读
    public static final int SEND = 0; //发送
    public static final int RECIEVE = 1; //接收

    /**
     * 单表查询时，需要的uri
     */
    public static final Uri URI = Uri.withAppendedPath(ProviderConstant.MEDICAL_URI, TABLENAME);
    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLENAME +
        " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
        + STRANGER_NUBE_NUMBER + " VARCHAR(8), "
        + STRANGER_HEAD + " TEXT, "
        + STRANGER_NAME + " TEXT, "
        + MSG_DIRECTION + " INTEGER, "
        + MSG_CONTENT + " TEXT, "
        + TIME + " TIMESTAMP ,"
        + IS_Read + " INTEGER)";
    // CREATE TABLE IF NOT EXISTS  t_stranger_msg
    //     (id INTEGER  PRIMARY KEY AUTOINCREMENT,
    //      strangerNubeNumber VARCHAR(8),
    //      strangerHead TEXT,
    //      strangerName TEXT,
    //      msgDirection INTEGER,
    //      msgContent TEXT,
    //      time TIMESTAMP,
    //      isRead INTEGER
    // )
}
