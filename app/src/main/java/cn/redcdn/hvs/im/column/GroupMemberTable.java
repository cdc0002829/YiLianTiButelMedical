package cn.redcdn.hvs.im.column;

import android.provider.BaseColumns;

/**
 * Desc
 * Created by wangkai on 2017/2/24.
 */

public class GroupMemberTable {
    public static final String TABLENAME = "t_multi_chat_users"; // 表名

    public static class Column implements BaseColumns {

        public static final String ID = "id";
        public static final String GID = "gid";
        public static final String MID = "mid";
        public static final String UID = "uid";
        public static final String NUBE_NUMBER = "nubeNumber";
        public static final String PHONE_NUM = "phoneNum";
        public static final String NICK_NAME = "nickName";
        public static final String GROUP_NICK = "groupNick";
        public static final String SHOW_NAME = "showName";
        public static final String HEAD_URL = "headUrl";
        public static final String REMOVED = "removed";
        public static final String GENDER = "gender";
        public static final String RESERVER_STR1 = "reserverStr1";
    }

    public static final int REMOVED_TRUE = 1;
    public static final int REMOVED_FALSE = 0;

    public static final int GENDER_MALE = 1;
    public static final int GENDER_FEMALE = 2;

    public static final String[] SELECT_COLUMNS = new String[]{Column.ID, Column.GID, Column.MID,
            Column.UID, Column.NUBE_NUMBER, Column.PHONE_NUM, Column.NICK_NAME,
            Column.GROUP_NICK, Column.SHOW_NAME, Column.HEAD_URL, Column.REMOVED, Column.GENDER};

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLENAME + " ("
            + Column.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + Column.GID
            + " TEXT," + Column.MID + " TEXT," + Column.UID + " TEXT,"
            + Column.NUBE_NUMBER + " TEXT," + Column.PHONE_NUM + " TEXT,"
            + Column.NICK_NAME + " TEXT," + Column.GROUP_NICK + " TEXT,"
            + Column.SHOW_NAME + " TEXT," + Column.HEAD_URL + " TEXT,"
            + Column.REMOVED + " INTEGER DEFAULT " + REMOVED_FALSE + ","
            + Column.GENDER + " INTEGER DEFAULT " + GENDER_MALE + ","
            + Column.RESERVER_STR1 + " TEXT);";
}
