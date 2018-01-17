
package cn.redcdn.hvs.config;

public class NpsParamConfig {
    // 公共参数
    public static final String COMMON = "X1MeetingCommon";
    public static final String COMMON_RC_URL = "RC_URL"; // STP 地址
    public static final String COMMON_MASTER_MS_URL = "MS_URL"; //主会诊接口地址
    public static final String COMMON_SLAVE_MS_URL = "SLAVE_MS_URL"; // 从会诊接口地址

    public static final String COMMON_EUC_URL = "EUC_URL"; // 企业用户中心地址
    public static final String COMMON_PUC_URL = "PUC_URL"; // 个人用户中心地址
    public static final String COMMON_Persion_Contact_URL = "Persion_Contact_URL";  // 个人通讯录地址
    public static final String COMMON_Persion_Head_Upload_URL = "Head_Upload_URL";  // 头像上传服务器地址
    public static final String COMMON_Friendship_Service_URL = "Friendship_Service_URL";//好友关系服务器地址
    public static final String COMMON_ArticlePreview = "ArticlePreview"; //文章webview地址

    // 应用升级
    public static final String APP_UPDATE = "MedicalAppUpdate";
    public static final String APP_UPDATE_MASTER_ServerUrl = "ServerUrl";
    public static final String APP_UPDATE_SLAVE_ServerUrl = "SLAVE_ServerUrl";
    public static final String APP_UPDATE_ProjectName = "ProjectName";
    public static final String APP_UPDATE_DeviceType = "DeviceType";

    // 流媒体
    public static final String MediaPlay = "X1MeetingMediaPlay";
    public static final String MediaPlay_Jfec_in = "Jfec_in";
    public static final String MediaPlay_Jfec_out = "Jfec_out";

    // 帮助
    public static final String HELP = "X1MeetingHelp";
    public static final String HELP_URL = "Mobile_Help_Url";
    public static final String HELP_URL_TV = "Mobile_TV_Help_Url"; //TV产品

    //手机直播黑名单机型
    public static final String MOBILE_LIVE_BLACKLISTING = "Mobile_Live_Blacklisting";

    // 极会诊官网
    public static final String HELP_WEBSITE = "JMeeting_Website";

    // 推荐中使用的应用下载地址
    public static final String HELP_DOWNLOAD_LINK = "Mobile_Download_Link";


    // 日志上传
    public static final String LogUpload = "MedicalLogUpload";
    public static final String LogUpload_serverIp = "ServerIP";
    public static final String LogUpload_serverPort = "ServerPort";

    //Medical APP
    public static final String Medical_COMMON = "MedicalCommon";
    public static final String Medical_COMMON_PUC_URL = "PUC_URL"; // 个人用户中心地址
    public static final String Medical_COMMON_SLAVE_PUC_URL = "SLAVE_PUC_URL"; //从个人用户中心地址
    public static final String Medical_COMMON_Persion_Contact_URL = "Persion_Contact_URL";  // 个人通讯录地址
    public static final String Medical_COMMON_Favorite_Server_Url = "Favorite_Server_Url";  // 收藏服务器地址
    public static final String Medical_COMMON_MDS_URL = "Medical_Data_Service_URL"; // 医疗数据服务器地址
    public static final String Medical_COMMON_SLAVE_MDS_URL = "SLAVE_Medical_Data_Service_URL"; // 从医疗数据服务器地址
    public static final String Medical_COMMON_CDN_URL = "CDN_Server_Url"; //CDN服务器地址
    public static final String Medical_COMMON_MEETING_NPS_URL = "Master_Meeting_Nps_Url"; //会议nps地址
    public static final String Medical_COMMON_SLAVE_MEETING_NPS_URL = "Slave_Meeting_Nps_Url"; //从会议nps地址
    public static final String Medical_COMMON_APPKEY = "appKey"; //appkey 登录使用
    public static final String Medical_COMMON_CDN_APPID = "CDN_appid"; //appid,登录cdn时使用
    public static final String Medical_COMMON_GROUP = "GroupManager"; //加入群聊的http


    //MedicalHelp
    public static final String Medical_HELP = "MedicalHelp";
    public static final String Medical_HELP_DisclaimerUrl = "DisclaimerUrl";
    public static final String Medical_HELP_ReviewNum = "ReviewNum";
    public static final String Medical_HELP_HelpUrl = "HelpUrl";
    public static final  String Medical_HELP_DownloadUrl = "AppDownloadUrl";
    public static final  String Medical_HELP_CustomerTel = "CustomerServiceNum";
    public static final  String Medical_HELP_ShareUrl = "ShareUrl";
    public static final String Medical_HELP_AdminNubeNum = "AdminNubeNum";
    public static final String Medical_HELP_AdminHeadUrl = "AdminHeadUrl";
    public static final String Medical_HELP_AdminNickname= "AdminNickname";

}
