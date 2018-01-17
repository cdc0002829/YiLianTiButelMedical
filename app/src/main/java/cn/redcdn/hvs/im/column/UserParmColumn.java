package cn.redcdn.hvs.im.column;

import android.provider.BaseColumns;

/**
 * Desc
 * Created by wangkai on 2017/3/3.
 */

public class UserParmColumn implements BaseColumns{

    public static final String TABLENAME = "t_userParm";

    public static final String ID = "_id";
    public static final String USER_ID = "userId";
    public static final String COMMONKEY = "CommonKey";
    public static final String COMMONVALUE = "CommonValue";
    public static final String RESERVE = "Reserve";

    // 查询结果
    public static final String[] USERPARM_PROJECTION = {COMMONKEY, COMMONVALUE};

    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS t_userParm (_id text(32) PRIMARY KEY,userId text,CommonKey TEXT(64)," +
            "CommonValue TEXT(64),Reserve VARCHAR(32))";
}
