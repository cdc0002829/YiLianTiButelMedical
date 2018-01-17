package cn.redcdn.hvs.im.column;

import android.provider.BaseColumns;

/**
 * Desc
 * Created by wangkai on 2017/2/24.
 */

public class GroupTable {

    public static final String TABLENAME = "t_multi_chat_groups"; // 表名

    public static class Column implements BaseColumns {

        public static final String ID = "id";
        public static final String GID = "gid";
        public static final String GNAME = "gName";
        public static final String HEAD_URL = "headUrl";
        public static final String MGR_NUBE = "mgrNube";
        public static final String CREATE_TIME = "createTime";
        public static final String RESERVER_STR1 = "reserverStr1";
    }

    public static final String[] SELECT_COLUMNS = new String[]{Column.ID, Column.GID, Column.GNAME,
            Column.HEAD_URL, Column.MGR_NUBE, Column.CREATE_TIME};

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLENAME + " ("
            + Column.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + Column.GID
            + " TEXT," + Column.GNAME + " TEXT," + Column.HEAD_URL + " TEXT,"
            + Column.MGR_NUBE + " TEXT," + Column.CREATE_TIME + " TIMESTAMP,"
            + Column.RESERVER_STR1 + " TEXT);";
}
