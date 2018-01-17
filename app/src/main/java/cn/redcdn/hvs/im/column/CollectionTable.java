package cn.redcdn.hvs.im.column;

import android.net.Uri;
import cn.redcdn.hvs.im.provider.ProviderConstant;

/**
 * @ClassName: CollectionTable
 */

public class CollectionTable {
    public static String TABLENAME = "t_collect";

    public final static String ID = "id";
    public final static String OPERATETIME = "operateTime";
    public final static String STATUS = "status";
    public final static String SYNCSTATUS = "syncStatus";
    public final static String OPERATORNUBE = "operatorNube";
    public final static String TYPE = "type";
    public final static String BODY = "body";
    public final static String EXTINFO = "extInfo";

    /**
     * 单表查询时，需要的uri
     */
    public static final Uri URI = Uri.withAppendedPath(ProviderConstant.MEDICAL_URI, TABLENAME);

    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +TABLENAME+
        " (" +
        ID + " TEXT," +
        OPERATETIME + " TIMESTAMP," +
        STATUS + " INTEGER," +
        SYNCSTATUS + " INTEGER," +
        OPERATORNUBE + " VARCHAR," +
        TYPE + " INTEGER," +
        BODY + " TEXT," +
        EXTINFO+" TEXT" +
        ")" ;
}
