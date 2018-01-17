package cn.redcdn.hvs.contacts.contact.hpucontact;

import android.net.Uri;

import cn.redcdn.hvs.im.provider.ProviderConstant;

/**
 * Created by caizx on 2017/11/24.
 */

public class HpuContactsTable {

    public  static String TABLE_NAME = "t_hpu_friends";

    public static final String ID = "id"; // 主键
    public static final String PHUID = "phuId";
    public static final String PHUNAME = "phuName";
    public static final String NUBENUMBER = "nubeNumber";
    public static final String NICKNAME = "nickName";
    public static final String HEADUTRL = "headUrl";
    public static final String WORKUNIT = "workUnit";
    public static final String DEPARTMENT = "department";
    public static final String UPDATETIME = "updateTime";
    public static final String RESERVESTR1 =  "reserveStr1";
    public static final String RESERVESTR2 = "reserveStr2";
    public static final String RESERVESTR3 = "reserveStr3";

    public static final Uri URI  = Uri.withAppendedPath(ProviderConstant.MEDICAL_URI, TABLE_NAME);
    public static final String CREATETABLE =  "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
            " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + PHUID + " TEXT, "
            + PHUNAME + " TEXT, "
            + NUBENUMBER + " TEXT, "
            + NICKNAME + " TEXT, "
            + HEADUTRL + " TEXT, "
            + WORKUNIT + " TEXT ,"
            + DEPARTMENT + " TEXT, "
            +UPDATETIME +" NUMERIC, "
            +RESERVESTR1 + " TEXT, "
            +RESERVESTR2 + " TEXT, "
            +RESERVESTR3 + " TEXT)";

}
