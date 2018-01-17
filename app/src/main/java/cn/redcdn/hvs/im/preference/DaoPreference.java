package cn.redcdn.hvs.im.preference;

import android.content.Context;
import android.text.TextUtils;

import java.util.HashSet;

import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.im.IMConstant;
import cn.redcdn.hvs.im.dao.UserParmDao;
import cn.redcdn.hvs.im.dao.UserParmDaoImpl;
import cn.redcdn.log.CustomLog;

/**
 * Desc
 * Created by wangkai on 2017/3/4.
 */

public class DaoPreference {
    private static final String TAG = "DaoPreference";

    /***
     * 下面的参数分为两种：帐号相关参数和公共参数（mHashSet中及为公共参数）
     * 在初始化参数时，需要注意：帐号相关参数初始化必须放在登陆成功以后初始化， 否则无法保存成功。
     * 在手动登录或切换帐号登录时，先要保存PrefType.BEFORE_LOGIN_NUMBER 再经行帐号相关参数的保存
     */
    public enum PrefType {

        // LOGIN_PHONE_NUMBER,//登录后保存的手机号码 //miaolk delete 20140123(已弃用，删除)
        LOGIN_PASSWORD, // 登录后保存的客户密码
        LOGIN_ACCESSTOKENID, // tokenid 令牌 访问资源接口时必须传入的参数
        LOGIN_USER_UUID, // 系统的唯一标示
        LOGIN_MOBILE, // 用户手机号
        LOGIN_NUBENUMBER, // NUBE NUMBER的佰库号
        BEFORE_LOGIN_NUMBER, // 前一个登录的账号
        LOGIN_NUMBER_CHANGE, // 用户是否切换账号（""也做比较） 1 切换账号，0 没切换账号
        IMPORT_NUMBER_RECORD, // 当前程序对应的导入视频号账户
        CUSTOM_SERVICE_PHONE, // 客户服务电话
        ONEKEYVISIT_SERVICE_PHONE, // 一键回家体验tv视频号码
        KEY_BAIKU_PASSPORT_V2_URL, // 新用户中心URL
        KEY_BAIKU_PASSPORT_URL, // 佰库通行证 key
        KEY_BAIKU_UNIFIEDSERVICET_URL, // 佰库通行证 key  20150707 修改searchAccount
        KEY_USER_CENTER_URL, // 用户中心 key
        KEY_MESSAGE_SHARE_URL, // 消息服务器 key
        KEY_APP_SERVER_URL, // 联系人同步 key
        KEY_LOGIN_SERVICE_URL, // 登录 key
        KEY_NUBE_ACTIVITY_URL, // 活动服务key
        KEY_ORDER_SERVICE_URL, // 视频服务查询、订购key
        KEY_RECHARGE_URL, // 充值服务key
        KEY_DOWN_LOAD_APK_ID, // 下载管理表中产生的记录id
        KEY_DOWN_LOAD_APK_FILE_PATH, // 下载后的本地apk地址
        KEY_SERVER_SOFT_VERSION, // 保存服务器上的版本号
        KEY_SERVER_APK_DOWNLOAD_URL, // 服务器上新apk的下载地址
        KEY_BOOT_STRAP_IP, // SIP BOOT STRAP　IP
        KEY_BOOT_STRAP_PORT, // SIP BOOT STRAP　PORT
        KEY_FRIST_GUIDE, // 首次使用，需要加载引导页
        KEY_FRIST_LOAD_CACHE, // 首次加载活动缓存
        KEY_ECHO_TAIL, // 回声消除时延
        KEY_FOR_VOICE_QUALITY, // 通话音频质量参数设置
        KEY_FOR_SUPPORT_ONLINE, // sip是否支持在线状态
        KEY_FOR_SUPPORT_EXTRA_FEATURE, // 软件是否支持IP通话等扩展功能（除基础版以外的所有功能：音视频通话、传输音视频文件）
        KEY_FOR_SIP_REG_OK, // SIP是否注册成功
        KEY_IS_ACTIVITY_POPED, // 活动相关参数设置相关 活动框是否已经弹过 true 弹过；false 未谈
        USER_EMAIL, // 用户邮箱
        USER_NICKNAME, // 用户昵称
        USER_SEX, // 用户性别
        USER_PASSWORD, // 用户密码
        USER_NAME, // 用户姓名
        USER_REMARK_NAME, // 对方设置拒接陌生人来电->己方弹出框中输入的名称
        USER_INFO_GET_SUCC,	//是否已经获取过登录帐号的相关信息（searchAccout 或 getAllUserinfo）
        USER_DOWNLOAD_METHEDS, // 用户下载方式：0.使用WIFI网络连接时自动接收文件。1.自动接收文件 2 手动接收文件
        USER_KEY_DBSRC_STORE_TYPE, // 用户保存数据的方式
        USER_CHANGE_BACKGROUND, // 是设置选择 来自相册还是拍照经典图片
        USER_BG_URL_ADDRESS, // 设置背景存放的URL
        USER_HEAD_ICON_URL, // 修改用户信息的头像
        WIFI_IS_ALERT, //
        PRIVATE_SETTING_NEW, // 隐私设置 1 接受陌生人来电和消息，0 不接收陌生人来电和消息
        LOGIN_MSG_SERVER, // 是否登录消息服务器
        FRIEND_RECOMMEND, // 推荐好友
        KEY_FRIEND_RECOMMEND_IKNOW_IS_SHOWED,//推荐好友遮罩层（只显示一次，与账户无关）0未显示，1已显示
        FRIEND_ONLINE_MSG, // 上线通知
        ACCESS_DEVNET, // 切换NPS环境
        DOWNLOAD_CHANNELPHONE_URL, // 家视通软件升级URL
        FILE_UPLOAD_SERVER_URL, // 文件上传服务器url
        FORCE_UPGRADE, // 强制升级 0;非 强制升级1
        FORCE_UPGRADE_CONTENT, // 强制升级内容
        KEY_SERVER_APK_SHA1,  //服务器端的APK的SHA1值，在发布版本时，由发布文档中提交；客户端用于比较完整性
        LOG_UPLOAD_SERVER_URL, // 上传日志接口域名
        CAMERA_STATUS, // camera状态
        ENABLE_VIDEO_CALL, // 支持视频通话
        ENABLE_IP_CALL, // 支持语音通话
        PHONE_CALL_RUNNING, // 是否正在通话中
        KEY_BOOT_STRAP_IP_1, // SIP BOOT STRAP　IP_1
        KEY_BOOT_STRAP_PORT_1, // SIP BOOT STRAP　PORT_1
        KEY_ROUTECENTER_MAIN_URL, // routeCenterMainUrl
        KEY_ROUTECENTER_MAIN_PORT, // routeCenterMainPort
        KEY_ROUTECENTER_BACKUP_URL, // routeCenterBakUrl
        KEY_ROUTECENTER_BACKUP_PORT, // routeCenterBakPort
        KEY_OFFSITE_TIP, // 异地登录或被踢提示
        NOT_WIFI_DATACONSUME_ALERT, // 非Wifi网络下数据使用告警(20151210暂不使用)
        NOT_WIFI_DATACONSUME_ALERT_4_NOTICE, // 非Wifi网络下数据使用告警(消息-视频)
        VALUE_VIDEO_FORMAT_SET, // 手动设置video视频的支持格式，方便测试
        VALUE_SPEEX_QUALITY_SET, // 手动设置SpeexQuality ，方便测试
        VALUE_UPLOAD_BWSTEP_SET,	// 手动设置带宽，方便测试
        MEDIA_AUTOADJUST_STATUS_SET,	// 设置是否支持通话中自适应调节功能
        ONEKEYVISIT_FIRST_LOGIN, // 一键回家场合，首次登录标志
        ONEKEYVISIT_SCAN_GUIDED, // 一键回家场合，引导向左滑动到扫一扫
        ONEKEYVISIT_SWITCH_GUIDED, // 一键回家场合，引导左右滑动切换亲情号码
        ONEKEYVISIT_HAS_LOGGED, // 一键回家场合，是否首次登录一键回家主界面
        VALUE_VIDEO_FORMAT_VGA, // 友盟配置参数：指定手机型号支持VGA视频格式
        KEY_GET_NPS_SUCCESS_TIME, // 成功获取nps的时间
        KEY_GET_NPS_INTERVAL_TIME, // 获取nps的时间间隔（单位：小时）
        KEY_BUTEL_INFO_1, // 后台配置的手机版本的相关信息
        KEY_BUTEL_INFO_2,
        KEY_BUTEL_INFO_3, //通讯录(老版本兼容)
        KEY_BUTEL_INFO_4, //微信好友(老版本兼容)
        KEY_BUTEL_INFO_5, //微信朋友圈(老版本兼容)
        KEY_BUTEL_INFO_6_TITLE,  //QQ好友分享
        KEY_BUTEL_INFO_6_CONTENT,
        KEY_BUTEL_INFO_6_URL,
        KEY_BUTEL_INFO_7, //统一邀请信息
        // 不再显示butel使用前声明
        HIDE_BUTEL_STATEMENT,
        KEY_REPLY_CACHE_LIST, // 回复消息的临时缓存（仅在插入失败的场景下）
        //所有未接来电通知栏ID
        KEY_NOTIFICATION_IDS,
        KEY_NOTIFICATION_MSG_IDS,  //所有消息的通知栏id
        KEY_NOTIFICATION_FRI_IDS,  //所有新朋友的通知栏id
        HAS_SHOW_NEW_VERSION_DIALOG,//是否已弹出版本升级对话框
        KEY_BUTEL_PUBLIC_NO,        //可视官方帐号
        KEY_BUTEL_REGISTER_PRIVACY,  //注册用户协议

        AUDIO_DETECT_UPLOAD_SUCCESS,//回声测试上传成功
        HAS_AUDIO_DETECTED_OK,//已进行回声测试，且测试成功
        HAS_AUDIO_DETECTED_NG_TIMES,//连续检测失败次数
        HAS_AUDIO_DETECTED_NG_REASON,//失败的原因
        PICTURE_COMPRESSION,// 客户端图片压缩上限（K）
        MESSAGE_LIST_FOR_REJECT, //来电拒接-》回复消息内容列表
        KEY_ACD_CENTER_URL, // ACD中心的的URL
        KEY_ACD_PREFIX, // ACD接入号的前缀
        KEY_UPDATE_PHONENUMBER_COMPLETE, // 已完成通话记录中手机号码的更新
        KEY_LOCAL_VERSION_NAME,           // 本地的版本号，用于比较是否有软件升级
        KEY_GROUP_MANAGER_URL,		  // 群组管理URL
        KEY_GROUP_PUBLIC_NUMBER,		  // 群事件发送着帐号
        KEY_CHAT_DONT_DISTURB_LIST,   // 免打扰消息列表，与账户有关(形式采用如下方式;nube;;gid;)
        KEY_CHAT_REMIND_LIST,// 有人@我处理，与账户有关
        KEY_V_CHANNEL_URL, // 公众号管理URL
        KEY_V_CHANNEL_CMT_FRAG_URL, // V频道评论URL
        KEY_V_CHANNEL_CONT_SHARE_URL, // V频道内容分享URL
        KEY_V_CHANNEL_ACTIVITYNO, // V频道活动页显示（20160121 过年回家活动）
        KEY_ALLOWED_CALL_2G3G4G,  //2G3G4G下是否允许音视频通话
        KEY_JMEET_URL,//级会议功能nps地址
        KEY_BUTEL_OVELL,//Butel能力值
        KEY_APPHELP_URL,//帮助反馈url地址
        KEY_PUBLIC_NO_MASK,// 0表示未弹出过，需要弹；非0表示已弹出过，不需要弹
        KEY_FAVORITE_SERVER_URL,//收藏分享的URl
        KEY_COLLECTION_QUERY_STARTTIME//查询收藏分享时的，startTime值
    }

    // 公共参数集，用于表示与个人帐号无关的参数
    private HashSet<String> mHashSet = new HashSet<String>();

    private static DaoPreference mDaoPreference;
    private static UserParmDao mUserParmDao = null;

    private static final String COMMFLAG = "ALL";

    DaoPreference(Context context) {
        mUserParmDao = new UserParmDaoImpl(context);
        mHashSet.add(PrefType.KEY_FRIST_GUIDE.name());
        mHashSet.add(PrefType.KEY_FRIST_LOAD_CACHE.name());
        mHashSet.add(PrefType.BEFORE_LOGIN_NUMBER.name());
        // mHashSet.add(PrefType.LOGIN_MOBILE.name());
        mHashSet.add(PrefType.LOGIN_NUMBER_CHANGE.name());
        mHashSet.add(PrefType.CUSTOM_SERVICE_PHONE.name());
        mHashSet.add(PrefType.ONEKEYVISIT_SERVICE_PHONE.name());
        mHashSet.add(PrefType.KEY_BAIKU_PASSPORT_V2_URL.name());
        mHashSet.add(PrefType.KEY_BAIKU_PASSPORT_URL.name());
        mHashSet.add(PrefType.KEY_BAIKU_UNIFIEDSERVICET_URL.name());

        mHashSet.add(PrefType.KEY_USER_CENTER_URL.name());
        mHashSet.add(PrefType.KEY_MESSAGE_SHARE_URL.name());
        mHashSet.add(PrefType.KEY_APP_SERVER_URL.name());
        mHashSet.add(PrefType.KEY_LOGIN_SERVICE_URL.name());
        mHashSet.add(PrefType.KEY_NUBE_ACTIVITY_URL.name());
        mHashSet.add(PrefType.KEY_ORDER_SERVICE_URL.name());
        mHashSet.add(PrefType.KEY_RECHARGE_URL.name());
        mHashSet.add(PrefType.KEY_FOR_SUPPORT_EXTRA_FEATURE.name());
        mHashSet.add(PrefType.USER_KEY_DBSRC_STORE_TYPE.name());
        mHashSet.add(PrefType.KEY_BOOT_STRAP_IP.name());
        mHashSet.add(PrefType.KEY_BOOT_STRAP_PORT.name());
        mHashSet.add(PrefType.ACCESS_DEVNET.name());
        mHashSet.add(PrefType.DOWNLOAD_CHANNELPHONE_URL.name());
        mHashSet.add(PrefType.FILE_UPLOAD_SERVER_URL.name());
        mHashSet.add(PrefType.LOG_UPLOAD_SERVER_URL.name());
        mHashSet.add(PrefType.CAMERA_STATUS.name());
        mHashSet.add(PrefType.ENABLE_VIDEO_CALL.name());
        mHashSet.add(PrefType.ENABLE_IP_CALL.name());
        // //应该是帐号相关参数
        mHashSet.add(PrefType.KEY_SERVER_APK_DOWNLOAD_URL.name());
        mHashSet.add(PrefType.KEY_DOWN_LOAD_APK_ID.name());
        mHashSet.add(PrefType.KEY_SERVER_SOFT_VERSION.name());
        mHashSet.add(PrefType.KEY_DOWN_LOAD_APK_FILE_PATH.name());
        mHashSet.add(PrefType.FORCE_UPGRADE.name());
        mHashSet.add(PrefType.FORCE_UPGRADE_CONTENT.name());
        mHashSet.add(PrefType.KEY_SERVER_APK_SHA1.name());
        // 统一信令版增加部分参数
        mHashSet.add(PrefType.KEY_BOOT_STRAP_IP_1.name());
        mHashSet.add(PrefType.KEY_BOOT_STRAP_PORT_1.name());
        mHashSet.add(PrefType.KEY_ROUTECENTER_MAIN_URL.name());
        mHashSet.add(PrefType.KEY_ROUTECENTER_MAIN_PORT.name());
        mHashSet.add(PrefType.KEY_ROUTECENTER_BACKUP_URL.name());
        mHashSet.add(PrefType.KEY_ROUTECENTER_BACKUP_PORT.name());
        mHashSet.add(PrefType.KEY_OFFSITE_TIP.name());
        mHashSet.add(PrefType.NOT_WIFI_DATACONSUME_ALERT.name());
        mHashSet.add(PrefType.NOT_WIFI_DATACONSUME_ALERT_4_NOTICE.name());
        mHashSet.add(PrefType.VALUE_VIDEO_FORMAT_SET.name());
        mHashSet.add(PrefType.VALUE_SPEEX_QUALITY_SET.name());
        mHashSet.add(PrefType.VALUE_UPLOAD_BWSTEP_SET.name());
        mHashSet.add(PrefType.MEDIA_AUTOADJUST_STATUS_SET.name());
        mHashSet.add(PrefType.VALUE_VIDEO_FORMAT_VGA.name());
        mHashSet.add(PrefType.KEY_GET_NPS_SUCCESS_TIME.name());
        mHashSet.add(PrefType.KEY_GET_NPS_INTERVAL_TIME.name());
        // 后台配置的手机版本的相关信息
        mHashSet.add(PrefType.KEY_BUTEL_INFO_1.name());
        mHashSet.add(PrefType.KEY_BUTEL_INFO_2.name());
        mHashSet.add(PrefType.KEY_BUTEL_INFO_3.name());
        mHashSet.add(PrefType.KEY_BUTEL_INFO_4.name());
        mHashSet.add(PrefType.KEY_BUTEL_INFO_5.name());
        mHashSet.add(PrefType.KEY_BUTEL_INFO_6_CONTENT.name());
        mHashSet.add(PrefType.KEY_BUTEL_INFO_6_TITLE.name());
        mHashSet.add(PrefType.KEY_BUTEL_INFO_6_URL.name());
        mHashSet.add(PrefType.KEY_BUTEL_INFO_7.name());
        mHashSet.add(PrefType.HIDE_BUTEL_STATEMENT.name());
        mHashSet.add(PrefType.KEY_NOTIFICATION_IDS.name());
        mHashSet.add(PrefType.KEY_NOTIFICATION_MSG_IDS.name());
        mHashSet.add(PrefType.KEY_NOTIFICATION_FRI_IDS.name());
        mHashSet.add(PrefType.HAS_SHOW_NEW_VERSION_DIALOG.name());
        mHashSet.add(PrefType.KEY_BUTEL_PUBLIC_NO.name());
        mHashSet.add(PrefType.KEY_BUTEL_REGISTER_PRIVACY.name());
        mHashSet.add(PrefType.AUDIO_DETECT_UPLOAD_SUCCESS.name());
        mHashSet.add(PrefType.HAS_AUDIO_DETECTED_OK.name());
        mHashSet.add(PrefType.HAS_AUDIO_DETECTED_NG_TIMES.name());
        mHashSet.add(PrefType.HAS_AUDIO_DETECTED_NG_REASON.name());
        mHashSet.add(PrefType.PICTURE_COMPRESSION.name());
        mHashSet.add(PrefType.MESSAGE_LIST_FOR_REJECT.name());
        mHashSet.add(PrefType.KEY_ACD_CENTER_URL.name());
        mHashSet.add(PrefType.KEY_ACD_PREFIX.name());

        mHashSet.add(PrefType.KEY_FRIEND_RECOMMEND_IKNOW_IS_SHOWED.name());
        mHashSet.add(PrefType.KEY_UPDATE_PHONENUMBER_COMPLETE.name());
        mHashSet.add(PrefType.KEY_LOCAL_VERSION_NAME.name());
        mHashSet.add(PrefType.KEY_GROUP_MANAGER_URL.name());
        mHashSet.add(PrefType.KEY_GROUP_PUBLIC_NUMBER.name());
        mHashSet.add(PrefType.KEY_V_CHANNEL_URL.name());
        mHashSet.add(PrefType.KEY_V_CHANNEL_CMT_FRAG_URL.name());
        mHashSet.add(PrefType.KEY_V_CHANNEL_CONT_SHARE_URL.name());
        mHashSet.add(PrefType.KEY_V_CHANNEL_ACTIVITYNO.name());
        mHashSet.add(PrefType.KEY_ALLOWED_CALL_2G3G4G.name());
        //添加级会议功能nps参数
        mHashSet.add(PrefType.KEY_JMEET_URL.name());
        mHashSet.add(PrefType.KEY_BUTEL_OVELL.name());
        //添加帮助反馈url参数
        mHashSet.add(PrefType.KEY_APPHELP_URL.name());
        mHashSet.add(PrefType.KEY_PUBLIC_NO_MASK.name());
        //收藏内容服务器地址
        mHashSet.add(PrefType.KEY_FAVORITE_SERVER_URL.name());
    }

    public static DaoPreference getInstance(Context context) {
        if (mDaoPreference == null) {
            mDaoPreference = new DaoPreference(context);
        }
        return mDaoPreference;
    }

    public String getKeyValue(PrefType secrecyKeyType, String defaultValue) {

        String result = null;
        if (PrefType.LOGIN_NUMBER_CHANGE.name().equals(secrecyKeyType.name())
                && IMConstant.CHANGE_LOGINNUMBER_UNCHANGED
                .equals(MedicalApplication.LOGIN_NUMBER_CHANGE)) {
            return MedicalApplication.LOGIN_NUMBER_CHANGE;
        }
        if (TextUtils.isEmpty(result)) {
            if (mHashSet.contains(secrecyKeyType.name())) {
                // 与帐号无关的参数
                result = mUserParmDao.getUserParm(secrecyKeyType.name(),
                        COMMFLAG);
            } else {
                // 与帐号有关的参数getKeyValue
                result = mUserParmDao.getUserParm(
                        PrefType.BEFORE_LOGIN_NUMBER.name(), COMMFLAG);
                result = mUserParmDao
                        .getUserParm(secrecyKeyType.name(), result);
            }
            if (TextUtils.isEmpty(result)) {
                result = defaultValue;
            }
            CustomLog.d(TAG, "getKeyValue" + "key=" + secrecyKeyType.name()
                    + "|value=" + result);
        }
        if (PrefType.LOGIN_NUMBER_CHANGE.name().equals(secrecyKeyType.name())) {
            MedicalApplication.LOGIN_NUMBER_CHANGE = result;
        }
        return result;
    }

    /**
     * 登陆后（DB 创建之后）使用该方法缓存信息
     *
     * @Title: getKeyValue
     * @Description: 根据key 返回 value
     * @param secrecyKeyType
     * @param value
     * @return
     * @date: 2013-8-9 上午10:03:04
     */
    public void setKeyValue(PrefType secrecyKeyType, String value) {
        if (TextUtils.isEmpty(value)) {
            value = "";
        }
        CustomLog.d(TAG, "setKeyValue key=" + secrecyKeyType.name()
                + "|value=" + value);
        String test = mUserParmDao.getUserParm(PrefType.BEFORE_LOGIN_NUMBER.name(), COMMFLAG);
        if (mHashSet.contains(secrecyKeyType.name())) {
            mUserParmDao.updateUserParm(secrecyKeyType.name(), value, COMMFLAG);
        } else {
            mUserParmDao.updateUserParm(
                    secrecyKeyType.name(),
                    value,
                    mUserParmDao.getUserParm(
                            PrefType.BEFORE_LOGIN_NUMBER.name(), COMMFLAG));
        }

        if (PrefType.LOGIN_NUMBER_CHANGE.name().equals(secrecyKeyType.name())) {
            MedicalApplication.LOGIN_NUMBER_CHANGE = value;
        }
    }

    /**
     * 清空参数
     */
    public void clearALLPreference(PrefType secrecyKeyType) {
        printParams();
        if (mHashSet.contains(secrecyKeyType.name())) {
            mUserParmDao.updateUserParm(secrecyKeyType.name(), "", COMMFLAG);
        } else {
            mUserParmDao.updateUserParm(
                    secrecyKeyType.name(),
                    "",
                    mUserParmDao.getUserParm(
                            PrefType.BEFORE_LOGIN_NUMBER.name(), COMMFLAG));
        }
    }

    public void printParams() {
    }
}
