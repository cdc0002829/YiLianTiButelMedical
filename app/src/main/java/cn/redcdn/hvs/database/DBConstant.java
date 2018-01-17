package cn.redcdn.hvs.database;

import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.im.IMConstant;

/**
 * Desc
 * Created by wangkai on 2017/2/23.
 */

public class DBConstant {

    public static final int DATABASE_VERSION = 2;
    public static final int DATABASE_VERSION_3 = 3;
    public static final int DATABASE_VERSION_4 = 4;

    /** 数据库文件名 */
    public static final String SQLITE_FILE_NAME = "medical";

    /** 数据库名与佰库帐号之间的连接符 */
    public static final String SQLITE_FILE_CONNECTOR = "_";

    /** 数据库文件扩展名 */
    public static final String SQLITE_FILE_NAME_EXTENSION = ".db";

    /** 手机内存中数据库文件存放目录 */
    public static final String SQLITE_FILE_ROM_FOLDER = MedicalApplication.getContext().getFilesDir().getAbsolutePath().replace("databases", "files");//  获取当前程序路径

    /** 手机内存中VCF文件存放目录*/
    public static final String VCF_FILE_ROM_FOLDER = "data/data/" + IMConstant.APP_PACKAGE + "/files/vcf";

    /** 数据库文件名（默认） */
    public static final String SQLITE_FILE_NAME_DEFAULT = SQLITE_FILE_NAME
            + SQLITE_FILE_NAME_EXTENSION;
    /** 需要分页获取上传数据时，每次取得的条数 */
    public static final int UPLOAD_FATCH_UNIT_SIZE = 500;
}
