package cn.redcdn.hvs.im;

import cn.redcdn.hvs.MedicalApplication;

/**
 * Desc
 * Created by wangkai on 2017/2/24.
 */

public class IMConstant {

    /** 应用pack名称 */
    public static final String APP_PACKAGE = MedicalApplication.getContext().getPackageName();

    /** 消息列表一页数据条数 */
    public static final int NOTICE_PAGE_CNT = 30;

    /** 应用外置存储卡根目录 */
    public static final String APP_ROOT_FOLDER = "Medical";

    // @功能特殊字符
    public static final char SPECIAL_CHAR = 8197;

    public static final String letter = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String NEW_CALL_DATA_SUB_KEY_MOBILE="caller_mobile_num";
    /** 上传图片默认大小 */
    public static final long DEFAULT_IMAGE_SEND_SIZE = 300 * 1024;

    /** 手机内存中数据库文件存放目录 */
    public static final String SQLITE_FILE_ROM_FOLDER = "data/data/" + APP_PACKAGE + "/files/";

    /** 视频文件最大限制：30M */
    public static final long MAX_VIDEO_FILE_SIZE = 30 * 1024 * 1024;

    /** 图片文件最大限制：10M */
    public static final long MAX_IMAGE_FILE_SIZE = 10 * 1024 * 1024;

    /** 拍摄视频目录 */
    public static final String VIDEO_FOLDER = "videos";

    /**
     * 不允许 将400号加为好友
     */
    public final static String BAN_ADD_FRIEND="400";
    public final static int NICK_NAME_MAX_LENGTH=20;//昵称最大长度
    public final static int NOTE_NAME_MAX_LENGTH=20;//备注名最大长度
    public final static int GROUP_NAME_MAX_LENGTH=30;//群名称 最大长度

    /** 是否切换了登录帐号 */
    public static final String CHANGE_LOGINNUMBER_CHANGED = "1";
    public static final String CHANGE_LOGINNUMBER_UNCHANGED = "0";


    /** 活动管理接口（活动查询、TV一键回家业务）返回的 token 无效 */
    public static final String ACTIVITY_ACCESSTOKEN_INVALID = "-2";

    /**
     *  im 链接状态
     */
    public static boolean isP2PConnect = false;

}
