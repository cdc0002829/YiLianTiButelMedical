package cn.redcdn.hvs.meeting.constant;

import android.media.AudioManager;
import android.os.Build;
import android.os.Environment;

import java.io.File;

/**
 * <dl>
 * <dt>CommonConstant.java</dt>
 * <dd>Description:常用常量类</dd>
 * <dd>Copyright: Copyright (C) 2011</dd>
 * <dd>Company: 安徽青牛信息技术有限公司</dd>
 * <dd>CreateDate: 2012-11-1 下午04:05:42</dd>
 */
public class CommonConstant {
	
	public final static int NICK_NAME_MAX_LENGTH=20;//昵称最大长度
	public final static int NOTE_NAME_MAX_LENGTH=20;//备注名最大长度
	public final static int GROUP_NAME_MAX_LENGTH=30;//群名称 最大长度
	
	/**
	 * 在版本升级覆盖安装时，为了显示引导页。可在打版本时，修改该值。
	 * 规则：当值与数据库KEY_FRIST_GUIDE不同时，显示引导页，每次+1。
	 */
	public final static String GUIDE_ACTIVITY_GLAG="1";
	/**
	 * 不允许 将400号加为好友
	 */
	public final static String BAN_ADD_FRIEND="400";
	
    /** 排序方式 */
    public enum OrderType {
        ASC, DESC
    }

    /** 字符检索条 */
    public static final String[] LETTERS = { "-99", "A", "C", "E", "G", "I",
            "K", "M", "O", "Q", "S", "U", "W", "Y", "#" };
    public static final String letter = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    /** 接口返回值：接口调用成功 */
    public static final String SUCCESS_RESLUT = "0";
    /** 如果N8上的帐号没有解绑就在手机上登录，则会提示终端串号不正确（-111）。如果N8上的帐号解绑了，就可以在手机上登录成功 */
    public static final String SERIAL_NUM_NOT_CORRECT = "-111";
    /** 登录失败，不允许跨终端登录 */
    public static final String FORBIDDEN_LOGIN_BY_NOT_SAME_TERMINAL = "-221";

    /** 授权令牌（accessToken）无效 */
    public static final String ACCESSTOKEN_INVALID = "-201";
    /** 消息服务返回的 token 无效，重新认证 */
    public static final String MSG_ACCESSTOKEN_INVALID = "-15";
    /** 联系人同步服务返回的 token 无效 */
    public static final String CONTACT_SYNC_ACCESSTOKEN_INVALID = "-2002";
    /** 活动管理接口（活动查询、TV一键回家业务）返回的 token 无效 */
    public static final String ACTIVITY_ACCESSTOKEN_INVALID = "-2";

    /** 接口返回码为-109 时，表示是论坛号登录的要对论坛号进行绑定 **/
    public static final String FORAMCODE = "-109";
    
    //接口返回码为-93时，表示登录账号不存在
    public static final String CONTACT_ACCOUNT_NOT_EXIST = "-93";

	// 接口返回码为-62时，表示账号格式有误
	public static final String CONTACT_ACCOUNT_FORMAT_ERROR = "-62";
    /***
     * 在登录接口 和 获取视频号接口中 参数imei 的值
     */
    public static String COMMOM_SERIAL_NUM = "mobileNumber";

    // public static String COMMOM_DEV_MODEL ="PC-Test";
    public static String COMMOM_DEV_MODEL = "AD_SN";
    public static String COMMOM_MAC = "mobileMAC";
    public static String COMMOM_serialNumber = "SNAD00000000001";
    /** 设备终端类型 手机 **/
    public static String DEV_TERMINAL_TYPE = "mobile";
    /** 设备终端类型 PC **/
    public static String DEV_TERMINAL_TYPE_PC = "pc";
    /** 设备终端类型 BBS **/
    public static String DEV_TERMINAL_TYPE_BBS = "bbs";

    public static String IMAGE_CACHE_DIR = "netphone/imageCache";

    public static String SOFT_NAME = "channelfone";

    /** 应用pack名称 */
    public static final String APP_PACKAGE = "com.channelsoft.netphoneip";
    /** 应用外置存储卡根目录 */
    public static final String APP_ROOT_FOLDER = "ipNetPhone";
    /** 应用rom根目录 */
    public static final String APP_ROM_PATH = "data/data/" + APP_PACKAGE;
    /** 应用raw目录 */
    public static final String APP_RAW_PATH = "android.resource://" + APP_PACKAGE + "/raw/";

    /** 拍摄头像图片目录 */
    public static String HEAD_ICON_FOLDER = "headIcon";
    /** 拍摄视频目录 */
    public static final String VIDEO_FOLDER = "videos";

    public final static String downLoadUrl = Environment
            .getExternalStorageDirectory().getPath()
            + File.separator
            + APP_ROOT_FOLDER + File.separator + "downloadApk";

//    // 友盟日志KEY定义
//    /** 查找好友 */
//    public static String UMENG_KEY_FIND_FRIEND = "findFriend";
//    /** 邀请注册 */
//    public static String UMENG_KEY_INVITE_REGISTER = "inviteRegister";
//    /** 图片分享 */
//    public static String UMENG_KEY_SHARE_PICTURE = "sharePicture";
//    /** 名片分享 */
//    public static String UMENG_KEY_SHARE_CARD = "shareCard";
//    /** 退出 */
//    public static String UMENG_KEY_EXIT = "exit";
//    /** 注册 */
//    public static String UMENG_KEY_REGISTER = "register";
//    /** 忘记密码 */
//    public static String UMENG_KEY_FIND_PWD = "findPwd";
//    /** ip呼叫 */
//    public static String UMENG_KEY_IPCALL = "ipCall";
    /** Sip 登陆注册时长 */
    public static String UMENG_KEY_SIPREG_DURATION = "sipRegDuration";
//
    // 利用友盟错误收集方法记录接口调用不成功的异常
    /** 接口调用失败:nps(获取配置地址接口) */
    public static String UMENG_KEY_ERROR_NPS = "nps";
    /** 接口调用失败:uc（用户中心） */
    public static String UMENG_KEY_ERROR_UC = "uc";
    /** 接口调用失败:passport（通行证） */
    public static String UMENG_KEY_ERROR_PASSPORT = "passport";
    /** 接口调用失败:contacts（通讯录） */
    public static String UMENG_KEY_ERROR_CONTACT = "contacts";
    /** 接口调用失败:order（订购管理） */
    public static String UMENG_KEY_ERROR_ORDER = "order";
    /** 接口调用失败:recharge（充值） */
    public static String UMENG_KEY_ERROR_RECHANGE = "recharge";
    /** 接口调用失败:activity（活动管理） */
    public static String UMENG_KEY_ERROR_ACTIVITY = "activity";
    /** 接口调用失败:num（号码管理） */
    public static String UMENG_KEY_ERROR_NUM = "num";
    /** 接口调用失败:OTA（OTA） */
    public static String UMENG_KEY_ERROR_OTA = "OTA";
    /** 接口调用失败:SIP 登陆注册 */
    public static String UMENG_KEY_ERROR_SIPREG = "sipreg";
//
//    /** 1.5版本中自定义事件 miaolk add 20140127 start */
//    /** 事件名称 流程（完成一次正确流程记一次） 触发点 */
//    /** 事件ID */
//    /** 注册开始 */
//    public static String UMENG_KEY_HOPPER_Registration_begin = "Registration_begin";
//    /** 注册成功 */
//    public static String UMENG_KEY_HOPPER_Registration_ok = "Registration_ok";
//    // 注册/登录统计点
//    /** 注册 用户通过点击注册按钮，并完成注册流程；初次启动客户端进入登录界面，点击注册按钮进行注册 */
//    public static String UMENG_KEY_Registration = "Registration";
//    /** 重置密码 用户通过所有登录入口点击“找回密码？”并完成找回密码流程； 登录入口 */
//    public static String UMENG_KEY_Retrieve_password = "Retrieve_password";
//    /** 退出注册 用户进入注册流程后，为进行注册，退出注册流程； 注册界面 */
//    public static String UMENG_KEY_Exit_Reg = "Exit_Reg";
//    /** 选号 用户点击“刷新”按钮，进行更换视讯号； 注册界面 */
//    public static String UMENG_KEY_Refresh_Number = "Refresh_Number";
//    /** 注册成功完善资料 用户注册成功后，弹出对话框“完善资料”点击完善资料的； 注册成功后弹出框 */
//    public static String UMENG_KEY_RegSuccess_information = "RegSuccess_information";
//    /** 立即使用 用户注册成功后，点击立即使用； 注册成功后弹出框 */
//    public static String UMENG_KEY_RegSuccess_use = "RegSuccess_use";
//
//    // 拨号
//    /** 拨号盘 用户通过菜单切换到拨号盘，输入视讯号后进行呼叫操作； 拨号界面 */
//    public static String UMENG_KEY_Dial = "Dial";
//    /** 通话记录 用户收起拨号盘，针对最近联系人列表进行回呼操作； 拨号界面，通话记录 */
//    public static String UMENG_KEY_Recently = "Recently";
//    /** 清空记录 点击清空按钮，删除通话记录； 通话记录界面 */
//    public static String UMENG_KEY_Empty = "Empty";
//
//    // 视频通话
//    /** 视频呼叫 统计所有视频呼叫次数； */
//    public static String UMENG_KEY_Call = "Call";
//    /** 视频接听 统计所有视频接听次数； */
//    public static String UMENG_KEY_Answer = "Answer";
//    /** 关闭镜头 视频通话过程中，关闭镜头的次数； */
//    public static String UMENG_KEY_Close_lens = "Close_lens";
//
//    // 消息
//    /** 分享照片：开始 */
//    public static String UMENG_KEY_N_share_photo_begin = "N_share_photo_begin";
//    /** 分享照片：选图 */
//    public static String UMENG_KEY_N_share_photo_pick = "N_share_photo_pick";
//    /** 分享照片：分享 */
//    public static String UMENG_KEY_N_share_photo_send = "N_share_photo_send";
//    /** 分享视频：开始 */
//    public static String UMENG_KEY_N_Share_Video_begin = "N_Share_Video_begin";
//    /** 分享视频：选图 */
//    public static String UMENG_KEY_N_Share_Video_pick = "N_Share_Video_pick";
//    /** 分享视频：分享 */
//    public static String UMENG_KEY_N_Share_Video_send = "N_Share_Video_send";
//    /** 分享名片：开始 */
//    public static String UMENG_KEY_N_Share_card_begin = "N_Share_card_begin";
//    /** 分享名片：选择接收人 */
//    public static String UMENG_KEY_N_Share_card_receiver = "N_Share_card_receiver";
//    /** 分享名片：分享 */
//    public static String UMENG_KEY_N_Share_card_send = "N_Share_card_send";
//    /** 删除消息 从消息界面，点击功能操作按钮，进行消息删除次数； 消息界面 */
//    public static String UMENG_KEY_Delete_message = "Delete_message";
//    /** 转发消息 从消息界面，点击功能操作按钮，进行转发次数； 消息界面 */
//    public static String UMENG_KEY_Forward = "Forward";
//    /** 查看照片 从消息界面，进行查看收到/分享的照片次数； 消息界面 */
//    public static String UMENG_KEY_View_Photos = "View_Photos";
//    /** 播放视频 从消息界面，进行播放收到/分享的视频次数 消息界面 */
//    public static String UMENG_KEY_Play_video = "Play_video";
//    /** 接收名片 从消息界面，点击名片信息，进行接收名片的次数及分享的次数； 消息界面 */
//    public static String UMENG_KEY_Receiving_card = "Receiving_card";
//
//    // 我
//    /** 修改资料 用户修改资料数量，完成修改流程； 设置（我）界面 */
//    public static String UMENG_KEY_Revised_data = "Revised_data";
//    /** 上传头像 用户上传头像数量，完成更新／上传流程； 设置（我）界面 */
//    public static String UMENG_KEY_Upload_picture = "Upload_picture";
//    /** 修改密码 用户修改密码，完成修改流程； 设置（我）界面 */
//    public static String UMENG_KEY_Change_password = "Change_password";
//    /** 自动下载内容设置 默认全部开启，用户手动设置开关，关闭此功能次数； 设置（我）界面 */
//    public static String UMENG_KEY_Download_Wifi = "Download_Wifi";
//    /** 禁止陌生人消息设置 默认全部开启，用户手动设置开关，关闭此功能次数； 设置（我）界面 */
//    public static String UMENG_KEY_Stop_stranger = "Stop_stranger";
//    /** 推荐通讯录朋友设置 默认全部开启，用户手动设置开关，关闭此功能次数； 设置（我）界面 */
//    public static String UMENG_KEY_Recommend_Friend = "Recommend_Friend";
//    /** 联系人上线通知设置 默认全部开启，用户手动设置开关，关闭此功能次数； 设置（我）界面 */
//    public static String UMENG_KEY_Online_notice = "Online_notice";
//    /** 在线状态设置 默认全部开启，用户手动设置开关，关闭此功能次数； 设置（我）界面 */
//    public static String UMENG_KEY_Online_state = "Online_state";
//    /** IP语音通话设置 默认全部开启，用户手动设置开关，关闭此功能次数； 设置（我）界面 */
//    public static String UMENG_KEY_Ip_Set = "Ip_Set";
//    /** 关于 用户查看关于ChannelFone的内容次数； 设置（我）界面 */
//    public static String UMENG_KEY_About = "About";
//
//	//友盟日志KEY定义
//	//WXZ 要求的新的全面的自定义事件
//	//=================================================================================================
//	//通话
//	/**视频呼叫    所有地方发起的视频通话此次数*/
//	public static String UMENG_KEY_W_Vedio_Call ="W_Vedio_Call";
//	/**语音呼叫    所有地方发起的语音通话此次数*/
//	public static String UMENG_KEY_W_Audio_Call ="W_Audio_Call";
//	/**呼叫      所有地方发起音视频通话次数*/
//	public static String UMENG_KEY_W_Call ="W_Call";
//	/**来电接听      所有来电点击“接听”按钮的次数*/
//	public static String UMENG_KEY_W_Answer_Call ="W_Answer_Call";
//	/**来电挂机    所有来电挂机的次数*/
//	public static String UMENG_KEY_W_Hangup_Call ="W_Hangup_Call";
//	/**发送消息     所有来电时发送消息回复的次数*/
//	public static String UMENG_KEY_W_Hangup_Call_bySms ="W_Hangup_Call_bySms";
//	/**呼叫挂断     所有发起呼叫方主动挂断的次数*/
//	public static String UMENG_KEY_W_Hangup_Call_ByCaller ="W_Hangup_Call_ByCaller";
//	
//	//联系人
//	/**新的联系人    查看次数*/
//	public static String UMENG_KEY_W_NewFriendItem_Click ="W_NewFriendItem_Click";
//	/**添加联系人    点击该按钮的次数*/
//	public static String UMENG_KEY_W_AddContacts_Click ="W_AddContacts_Click";
//	/**联系人名片    查看次数*/
//	public static String UMENG_KEY_W_ContactItem_Click ="W_ContactItem_Click";
//	/**立即邀请好友     点击该按钮的次数*/
//	public static String UMENG_KEY_W_NoContact_Invaite_Click ="W_NoContact_Invaite_Click";
//	/**可视客服     使用该功能的次数*/
//	public static String UMENG_KEY_W_KefuItem_Click ="W_KefuItem_Click";
//	/**下拉刷新页面     查看的次数*/
//	public static String UMENG_KEY_W_ContactList_Pulldown ="W_ContactList_Pulldown";
//	/**扫码关联设备     使用该功能的次数*/
//	public static String UMENG_KEY_W_ScanItem_Click ="W_ScanItem_Click";
//	/**打开设备界面     使用该功能的次数*/
//	public static String UMENG_KEY_W_X1Item_Click ="W_X1Item_Click";
//	
//	//X1设备主页
//	/**进入看家     使用该功能的次数*/
//	public static String UMENG_KEY_W_X1_MonitorBtn_Click ="W_X1_MonitorBtn_Click";
//	/**进入报警消息     使用该功能的次数*/
//	public static String UMENG_KEY_W_X1_WarningMsgBtn_Click ="W_X1_WarningMsgBtn_Click";
//	/**进入设置     使用该功能的次数*/
//	public static String UMENG_KEY_W_X1_SetBtn_Click ="W_X1_SetBtn_Click";
//	/**取消关联     使用该功能的次数*/
//	public static String UMENG_KEY_W_X1_CancelBtn_Click ="W_X1_CancelBtn_Click";
//	
//	//看家监控
//	/**看家     用户点击看家次数*/
//	public static String UMENG_KEY_W_Monitor_Start ="W_Monitor_Start";
//	/**双向通话     用户使用双向语音的次数*/
//	public static String UMENG_KEY_W_Monitor_OpenMute ="W_Monitor_OpenMute";
//	/**断开监控    使用该功能的次数*/
//	public static String UMENG_KEY_W_Monitor_End ="W_Monitor_End";
//	/**监控时间     界面停留时长 用户使用监控的时长*/
//	public static String UMENG_KEY_W_Monitor_Duration ="W_Monitor_Duration";
//	
//	//报警消息列表
//	/**收到报警消息      用户使用此功能的次数*/
//	public static String UMENG_KEY_W_WarningMsg_Count ="W_WarningMsg_Count";
//	/**打开报警消息      用户使用此功能的次数*/
//	public static String UMENG_KEY_W_WarningMsg_Open ="W_WarningMsg_Open";
//	/**删除单条消息      用户使用此功能的次数*/
//	public static String UMENG_KEY_W_WarningMsg_Delete ="W_WarningMsg_Delete";
//	/**删除全部      用户使用此功能的次数*/
//	public static String UMENG_KEY_W_WarningMsg_DelAll ="W_WarningMsg_DelAll";
//	
//	//查看报警图片
//	/**查看时间       界面停留时长      用户对照片的关注程度*/
//	public static String UMENG_KEY_W_WarningMsg_ImgView_Duration ="W_WarningMsg_ImgView_Duration";
//	
//	//X1设备设置
//	/**自动侦测关闭*/
//	public static String UMENG_KEY_W_X1Set_DetectBtn_Click ="W_X1Set_DetectBtn_Click";
//	/**接收报警信息关闭*/
//	public static String UMENG_KEY_W_X1Set_WarningMsgBtn_Click ="W_X1Set_WarningMsgBtn_Click";
//	
//	//扫码关联设备
//	/**操作时间      界面停留时长     停留时长，操作简易程度*/
//	public static String UMENG_KEY_W_X1Scan_Duration ="W_X1Scan_Duration";
//	
//	//新的联系人
//	/**添加联系人     点击该按钮的次数*/
//	public static String UMENG_KEY_W_NewFriend_Add ="W_NewFriend_Add";
//	/**查看联系人    使用该功能的次数*/
//	public static String UMENG_KEY_W_NewFriend_Item_Click ="W_NewFriend_Item_Click";
//	/**接受     点击该按钮的次数*/
//	public static String UMENG_KEY_W_NewFriend_AcceptBtn_Click ="W_NewFriend_AcceptBtn_Click";
//	/**添加     点击该按钮的次数*/
//	public static String UMENG_KEY_W_NewFriend_AddBtn_Click ="W_NewFriend_AddBtn_Click";
//	/**删除记录     使用该功能的次数*/
//	public static String UMENG_KEY_W_NewFriend_Item_Delete ="W_NewFriend_Item_Delete";
//	/**下拉刷新页面      查看的次数*/
//	public static String UMENG_KEY_W_NewFriend_List_Pulldown ="W_NewFriend_List_Pulldown";
//	
//	//添加联系人
//	/**搜索添加好友     使用该功能的次数*/
//	public static String UMENG_KEY_W_AddContacts_BySearch ="W_AddContacts_BySearch";
//	/**添加通讯录好友     使用该功能的次数*/
//	public static String UMENG_KEY_W_AddContacts_fromSystem ="W_AddContacts_fromSystem";
//	/**添加微信好友     使用该功能的次数*/
//	public static String UMENG_KEY_W_AddContacts_fromWeiXin ="W_AddContacts_fromWeiXin";
//	/**添加QQ好友      使用该功能的次数*/
//	public static String UMENG_KEY_W_AddContacts_fromQQ ="W_AddContacts_fromQQ";
//	/**扫一扫添加好友      使用该功能的次数*/
//	public static String UMENG_KEY_W_AddContacts_ByScan ="W_AddContacts_ByScan";
//	
//	//邀请手机通讯录
//	/**添加按钮     使用该功能的次数*/
//	public static String UMENG_KEY_W_SystemContact_AddBtn_Click ="W_SystemContact_AddBtn_Click";
//	/**邀请按钮     使用该功能的次数*/
//	public static String UMENG_KEY_W_SystemContact_InviteBtn_Click ="W_SystemContact_InviteBtn_Click";
//	
//	//拨号
//	/**拨号-视频呼叫     使用该功能的次数*/
//	public static String UMENG_KEY_W_Dial_Vedio ="W_Dial_Vedio";
//	/**拨号-语音呼叫     使用该功能的次数*/
//	public static String UMENG_KEY_W_Dial_Audio ="W_Dial_Audio";
//	/**通话记录呼叫      使用该功能的次数*/
//	public static String UMENG_KEY_W_Dial_CRItem_Click ="W_Dial_CRItem_Click";
//	/**清空通话记录     使用该功能的次数*/
//	public static String UMENG_KEY_W_Dial_Delete_AllCR ="W_Dial_Delete_AllCR";
//	/**更多-添加陌生人为联系人     使用该功能的次数*/
//	public static String UMENG_KEY_W_Dial_More_AddContact ="W_Dial_More_AddContact";
//	/**拨号-更多     使用该功能的次数*/
//	public static String UMENG_KEY_W_Dial_More ="W_Dial_More";
//	/**更多-发送消息     使用该功能的次数*/
//	public static String UMENG_KEY_W_Dial_More_meaasge ="W_Dial_More_meaasge";
//	/**更多-进入名片     使用该功能的次数*/
//	public static String UMENG_KEY_W_Dial_More_entryCard ="W_Dial_More_entryCard";
//	/**更多-删除     使用该功能的次数*/
//	public static String UMENG_KEY_W_Dial_More_delete ="W_Dial_More_delete";
//	/**更多-呼叫     使用该功能的次数*/
//	public static String UMENG_KEY_W_More_makecall ="W_More_makecall";
//	
//	//名片页
//	/**名片-视频呼叫     使用该功能的次数*/
//	public static String UMENG_KEY_W_Card_Vedio ="W_Card_Vedio";
//	/**名片-语音呼叫    使用该功能的次数*/
//	public static String UMENG_KEY_W_Card_Audio ="W_Card_Audio";
//	/**名片-发送消息    使用该功能的次数*/
//	public static String UMENG_KEY_W_Card_Message ="W_Card_Message";
//	/**PSTN呼叫    使用该功能的次数*/
//	public static String UMENG_KEY_W_Card_PSTN ="W_Card_PSTN";
//	/**名片-查看头像    使用该功能的次数*/
//	public static String UMENG_KEY_W_Card_PhotoImg_Click ="W_Card_PhotoImg_Click";
//	/**名片-添加为联系人      使用该功能的次数*/
//	public static String UMENG_KEY_W_Card_AddContact ="W_Card_AddContact";
//	/**编辑资料      使用该功能的次数*/
//	public static String UMENG_KEY_W_Card_Edit_Click ="W_Card_Edit_Click";
//	/**修改备注名      使用该功能的次数*/
//	public static String UMENG_KEY_W_Card_Edit_Rename ="W_Card_Edit_Rename";
//	/**删除联系人      使用该功能的次数*/
//	public static String UMENG_KEY_W_Card_Edit_Delete ="W_Card_Edit_Delete";
//	/**心标设置       使用该功能的次数*/
//	public static String UMENG_KEY_W_Card_XinBiao_Click ="W_Card_XinBiao_Click";
//	
//	//导航
//	/**拨号     使用该功能的次数*/
//	public static String UMENG_KEY_W_MainTab_Dial ="W_MainTab_Dial";
//	/**联系人	     使用该功能的次数*/
//	public static String UMENG_KEY_W_MainTab_Contact ="W_MainTab_Contact";
//	/**消息      使用该功能的次数*/
//	public static String UMENG_KEY_W_MainTab_Meassge ="W_MainTab_Meassge";
//	/**我       使用该功能的次数*/
//	public static String UMENG_KEY_W_MainTab_Setting ="W_MainTab_Setting";
//	
//	//会话列表
//	/**新建信息      使用该功能的次数*/
//	public static String UMENG_KEY_W_MessageList_NewBtn_Click ="W_MessageList_NewBtn_Click";
//	/**消息查看       查看会话列表消息的次数*/
//	public static String UMENG_KEY_W_MessageList_Item_Click ="W_MessageList_Item_Click";
//	/**官方消息的查看      查看的次数*/
//	public static String UMENG_KEY_W_MessageList_ButelItem_Click ="W_MessageList_ButelItem_Click";
//	/**下拉刷新页面       查看的次数*/
//	public static String UMENG_KEY_W_MessageList_Pulldown ="W_MessageList_Pulldown";
//	/**删除消息       使用该功能的次数*/
//	public static String UMENG_KEY_W_MessageList_Item_Delete ="W_MessageList_Item_Delete";
//	
//	//新建消息
//	/**退出新建消息      使用该功能的次数*/
//	public static String UMENG_KEY_W_NewMsg_Back ="W_NewMsg_Back";
//	/**选择收件人      使用该功能的次数*/
//	public static String UMENG_KEY_W_NewMsg_SelectContacts_Click ="W_NewMsg_SelectContacts_Click";
//	/**完成收件人选择      使用该功能的次数*/
//	public static String UMENG_KEY_W_NewMsg_SelectContacts_End ="W_NewMsg_SelectContacts_End";
//	/**成功发送文字       成功发送文字次数*/
//	public static String UMENG_KEY_W_NewMsg_Send_Txt ="W_NewMsg_Send_Txt";
//	/**发送图片-选择拍照       选择拍照的次数*/
//	public static String UMENG_KEY_W_NewMsg_Send_Img_ByCamera ="W_NewMsg_Send_Img_ByCamera";
//	/**发送图片-选择相册       选择相册的次数*/
//	public static String UMENG_KEY_W_Send_Img_fromAlbum ="W_Send_Img_fromAlbum";
//	/**成功发送图片       成功发送图片的比例*/
//	public static String UMENG_KEY_W_NewMsg_Send_Img ="W_NewMsg_Send_Img";
//	/**发送视频-选择拍照       选择拍照的次数*/
//	public static String UMENG_KEY_W_NewMsg_Send_Vedio_ByCamera ="W_NewMsg_Send_Vedio_ByCamera";
//	/**发送视频-选择相册      选择相册的次数*/
//	public static String UMENG_KEY_W_NewMsg_Send_Vedio_fromAlbum ="W_NewMsg_Send_Vedio_fromAlbum";
//	/**成功发送视频      成功发送视频的比例*/
//	public static String UMENG_KEY_W_NewMsg_Send_Vedio ="W_NewMsg_Send_Vedio";
//	/**成功发送音频      成功发送音频的比例*/
//	public static String UMENG_KEY_W_NewMsg_Send_Audio ="W_NewMsg_Send_Audio";
//	/**成功发送名片      成功发送名片的比例*/
//	public static String UMENG_KEY_W_NewMsg_Send_Posdcard ="W_NewMsg_Send_Posdcard";
//	
//	//查看消息
//	/**消息转发      使用该功能的次数*/
//	public static String UMENG_KEY_W_MsgPage_forword ="W_MsgPage_forword";
//	/**消息复制      使用该功能的次数*/
//	public static String UMENG_KEY_W_MsgPage_copy ="W_MsgPage_copy";
//	/**消息删除     使用该功能的次数*/
//	public static String UMENG_KEY_W_MsgPage_delete ="W_MsgPage_delete";
//	/**图片预览     使用该功能的次数*/
//	public static String UMENG_KEY_W_MsgPage_ImgView ="W_MsgPage_ImgView";
//	/**图片预览-转发给朋友      使用该功能的次数*/
//	public static String UMENG_KEY_W_MsgPage_ImgView_forword ="W_MsgPage_ImgView_forword";
//	/**图片预览-保存到手机      使用该功能的次数*/
//	public static String UMENG_KEY_W_MsgPage_ImgView_save ="W_MsgPage_ImgView_save";
//	/**视频播放       使用该功能的次数*/
//	public static String UMENG_KEY_W_MsgPage_Vedio_play ="W_MsgPage_Vedio_play";
//	/**音频播放       使用该功能的次数*/
//	public static String UMENG_KEY_W_MsgPage_Audio_play ="W_MsgPage_Audio_play";
//	/**查看收件人     使用该功能的次数*/
//	public static String UMENG_KEY_W_MsgPage_Recipients ="W_MsgPage_Recipients";
//	/**添加对方为好友      使用该功能的次数*/
//	public static String UMENG_KEY_W_MsgPage_AddContact ="W_MsgPage_AddContact";
//	
//	//我
//	/**编辑个人资料     使用该功能的次数*/
//	public static String UMENG_KEY_W_Settting_Edit_selfinfo ="W_Settting_Edit_selfinfo";
//	/**编辑头像       使用该功能的次数*/
//	public static String UMENG_KEY_W_Settting_PhotoImg_Click ="W_Settting_PhotoImg_Click";
//	/**编辑头像-拍照      使用该功能的次数*/
//	public static String UMENG_KEY_W_Settting_PhotoEdit_ByCamera ="W_Settting_PhotoEdit_ByCamera";
//	/**编辑头像-从相册选择     使用该功能的次数*/
//	public static String UMENG_KEY_W_Settting_PhotoEdit_fromAlbum ="W_Settting_PhotoEdit_fromAlbum";
//	/**设置      查看的次数*/
//	public static String UMENG_KEY_W_Settting_SetItem_Click ="W_Settting_SetItem_Click";
//	/**接收陌生人来电和消息       使用该功能的次数*/
//	public static String UMENG_KEY_W_Settting_Set_strangerAccept ="W_Settting_Set_strangerAccept";
//	/**向我推荐通信录好友       使用该功能的次数*/
//	public static String UMENG_KEY_W_Settting_Set_recommendFriend ="W_Settting_Set_recommendFriend";
//	/**设置-声音检测        使用该功能的次数*/
//	public static String UMENG_KEY_W_Settting_AudioDetect_click ="W_Settting_AudioDetect_click";
//	/**设置-开始检测        使用该功能的次数*/
//	public static String UMENG_KEY_W_Settting_AudioDetect_start ="W_Settting_AudioDetect_start";
//	/**设置-声音检测-下次再说         点击该按钮的次数*/
//	public static String UMENG_KEY_W_Settting_AudioDetect_lateragain ="W_Settting_AudioDetect_lateragain";
//	/**设置-声音检测-完成          点击该按钮的次数*/
//	public static String UMENG_KEY_W_Settting_AudioDetect_succ ="W_Settting_AudioDetect_succ";
//	/**修改密码           使用该功能的次数*/
//	public static String UMENG_KEY_W_Settting_ChangePWD_click ="W_Settting_ChangePWD_click";
//	/**密码修改成功      使用该功能的次数*/
//	public static String UMENG_KEY_W_Settting_ChangePWD_succ ="W_Settting_ChangePWD_succ";
//	/**退出登录       点击退出登录按钮的次数*/
//	public static String UMENG_KEY_W_Settting_Logout_click ="W_Settting_Logout_click";
//	/**成功退出登录     成功退出登录的次数*/
//	public static String UMENG_KEY_W_Settting_Logout_Dialog_Exit ="W_Settting_Logout_Dialog_Exit";
//	/**清空缓存       点击清空缓存按钮的次数*/
//	public static String UMENG_KEY_W_Settting_WipeCache_click ="W_Settting_WipeCache_click";
//	/**清空缓存       点击清空缓存的“确定”按钮的次数*/
//	public static String UMENG_KEY_W_Settting_WipeCacheSuccess_click ="W_Settting_WipeCacheSuccess_click";
//	/**关于可视     查看次数*/
//	public static String UMENG_KEY_W_Settting_AboutItem_Click ="W_Settting_AboutItem_Click";
//	/**关于可视     可视官网      查看次数*/
//	public static String UMENG_KEY_W_Settting_About_website ="W_Settting_About_website";
//	/**关于可视    检查更新      使用该功能的次数*/
//	public static String UMENG_KEY_W_Settting_About_update ="W_Settting_About_update";
//	/**意见反馈      查看及提交次数*/
//	public static String UMENG_KEY_W_Settting_FeedbackItem_Click ="W_Settting_FeedbackItem_Click";
//	/**提交意见反馈     查看及提交次数*/
//	public static String UMENG_KEY_W_Settting_FeedBack_submit ="W_Settting_FeedBack_submit";
//	
//	//视频通话中
//	/**切换镜头      使用该功能的次数*/
//	public static String UMENG_KEY_W_Vedio_switchCamera ="W_Vedio_switchCamera";
//	/**关闭/打开镜头       使用该功能的次数*/
//	public static String UMENG_KEY_W_Vedio_camera_open_close ="W_Vedio_camera_open_close";
//	/**视频通话-关闭/打开静音       使用该功能的次数*/
//	public static String UMENG_KEY_W_Vedio_mute_open_close ="W_Vedio_mute_open_close";
//	/**特效      使用该功能的次数*/
//	public static String UMENG_KEY_W_Vedio_effectBtn_click ="W_Vedio_effectBtn_click";
//	/**成功发送特效       成功发送特效的次数*/
//	public static String UMENG_KEY_W_Vedio_sendEffect_succ ="W_Vedio_sendEffect_succ";
//	/**音量调整      使用该功能的次数*/
//	public static String UMENG_KEY_W_Vedio_volume_change ="W_Vedio_volume_change";
//	
//	//音频通话中
//	/**打开/关闭免提        使用该功能的次数*/
//	public static String UMENG_KEY_W_Audio_speaker_open_close ="W_Audio_speaker_open_close";
//	/**语音通话-打开/关闭静音       使用该功能的次数*/
//	public static String UMENG_KEY_W_Audio_mute_open_close ="W_Audio_mute_open_close";
//	
//	//视频呼叫
//	/**视频呼叫挂断      视频呼叫主叫方主动挂断次数*/
//	public static String UMENG_KEY_W_Vedio_OutCall_End_ByCaller ="W_Vedio_OutCall_End_ByCaller";
//	//语音呼叫
//	/**语音呼叫挂断      语音呼叫主叫方主动挂断次数*/
//	public static String UMENG_KEY_W_Audio_OutCall_End_ByCaller ="W_Audio_OutCall_End_ByCaller";
//	
//	//视频来电等待
//	/**视频来电-接听       使用该功能的次数*/
//	public static String UMENG_KEY_W_VedioCalling_accept_forAV ="W_VedioCalling_accept_forAV";
//	/**视频来电-语音接听       使用该功能的次数*/
//	public static String UMENG_KEY_W_VedioCalling_accept_forAU ="W_VedioCalling_accept_forAU";
//	/**视频来电-发送消息       使用该功能的次数*/
//	public static String UMENG_KEY_W_VedioCalling_hangup_bySms ="W_VedioCalling_hangup_bySms";
//	/**视频来电-挂机       使用该功能的次数*/
//	public static String UMENG_KEY_W_VedioCalling_hangup ="W_VedioCalling_hangup";
//	
//	//语音来电等待
//	/**语音来电-接听       使用该功能的次数*/
//	public static String UMENG_KEY_W_AudioCalling_accept ="W_AudioCalling_accept";
//	/**语音来电-发送消息       使用该功能的次数*/
//	public static String UMENG_KEY_W_AudioCalling_hangup_bySms ="W_AudioCalling_hangup_bySms";
//	/**语音来电-挂机       使用该功能的次数*/
//	public static String UMENG_KEY_W_AudioCalling_hangup ="W_AudioCalling_hangup";
//	
//	//消息推送通知
//	/**消息推送条数         系统累计推送的消息条数，包括文字、图片、音频、视频、名片及好友邀请消息*/
//	public static String UMENG_KEY_W_Notification_message ="W_Notification_message";
//	/**推送消息查看       查看消息的次数*/
//	public static String UMENG_KEY_W_Notification_message_click ="W_Notification_message_click";
//	/**未接来电推送      系统推送的来电通知条数*/
//	public static String UMENG_KEY_W_Notification_missedcall ="W_Notification_missedcall";
//	/**未接来电通知回拨      未接来电的回拨次数*/
//	public static String UMENG_KEY_W_Notification_missedcall_click ="W_Notification_missedcall_click";

	//==================================================================================================
	
    public static final String FRIEND_IS_EMPTY = "friend_is_empty";

    /** 友盟在线参数 */
    /** 回音消除时延 */
    public static final String UMENG_ONLINE_PARAM_KEY_ECHO = "echoTail";
    /** 回音消除时延范围 */
    public static final String UMENG_ONLINE_PARAM_KEY_ECHO_REGION = "echoTailRegion";
    /** 回音消除时延默认值 */
    public static final int ECHO_TAIL_DEFAULT = 200;
    /** 回音消除时延范围与手机型号之间的分隔符,echoTail:MI 2SC */
    public static final String UMENG_ONLINE_PARAM_MODEL_SEPERATOR = ":";
    /** 回音消除时延范围分隔符,100-200 */
    public static final String UMENG_ONLINE_PARAM_ECHO_SEPERATOR = "-";
    /** 回音消除时延默认范围 */
    public static final int DEFAULT_ECHO_REGION_BEGIN = 0;
    public static final int DEFAULT_ECHO_REGION_END = 800;
    /** sdk 自适应参数negoInfoParam */
    public static final String UMENG_ONLINE_PARAM_KEY_NEGOINFO = "negoInfoParam";
    /** nps访问地址 */
    public static final String UMENG_ONLINE_PARAM_KEY_NPSADDRESS = "NpsAddress";
    /** 是否立即nps访问地址  1:每次获取Nps    0:默认间隔3天获取 或是 nps上设置的时间间隔（单位：小时）*/
    public static final String UMENG_ONLINE_PARAM_KEY_NPSINTERVAL = "NpsInterval:BASE";
    /** 每次获取Nps*/
    public static final String NPS_ACCESS_AT_ONCE = "1";

    /** 参数：是否拨打客服及客服号码 */
    public static final String IS_DIAL_CUSTOMER = "isDialCustomer";
    public static final String CUSTOMER_NUMBERS = "customerNumbers";

    /** 自定义手机性能值 */
    public static final float PHONE_PERFORMANCE_LOW = 1000;
    public static final float PHONE_PERFORMANCE_MIDDLE1 = 2000;
    public static final float PHONE_PERFORMANCE_MIDDLE2 = 3000;

    /** 加载错误日志Tag lihs **/
    public static final String CHANNELPHONE_TAG = "channelphone_tag:";

    /** 切换线上环境，开发环境，部署环境的参数 **/
    public static final String ACCESS_NET = "0";
    public static final String ACCESS_DEV_NET = "1";
    public static final String ACCESS_DEV_TEMP_NET = "2";

    // 二维码扫描登录内容标签
    public static final String INTENT_TAG_CODE_RESULT_DATA = "INTENT_TAG_CODE_RESULT_DATA";
    public static final String INTENT_TAG_CODE_RESULT_KEYCODE_DATA = "INTENT_TAG_CODE_RESULT_KEYCODE_DATA";
    public static final String INTENT_TAG_CODE_RESULT_DEVICE_TYPE = "INTENT_TAG_CODE_RESULT_DEVICE_TYPE";

    // 在Login页面视频号输入框中，输入下面指令+回车键后，弹出对话框
    // 可以用于切换nps的访问地址
    public static final String NPS_CHANGE_CMD = "00045692";

    /** 回复消息类型 */
    public static final String REPLY_MSG_TYPE_COMMENT = "1";
    public static final String REPLY_MSG_TYPE_REPLY = "2";

    /** 回复消息方向 */
    public static final String REPLY_MSG_DIRECTION_SEND = "1";
    public static final String REPLY_MSG_DIRECTION_RECEIVE = "2";

    /** 是否切换了登录帐号 */
    public static final String CHANGE_LOGINNUMBER_CHANGED = "1";
    public static final String CHANGE_LOGINNUMBER_UNCHANGED = "0";

    // 来电、去电、未接或者接通中通知栏按钮事件
    public static final String ACTION_BUTTON = "com.notifications.intent.action.ButtonClick";
    // 来电、去电、未接或者接通中通知栏按钮Id
    public static final String INTENT_BUTTONID_TAG = "ButtonId";
    public static final int BUTTON_ACCEPT = 1; // 接听
    public static final int BUTTON_REFUSE = 2; // 挂断
    public static final int BUTTON_CALLBACK = 3; // 回拨
    public static final int BUTTON_ICO = 4; // 通话过程中的应用图标
    public static final String NUMBER = "nubeNumber";
    public static final String NAME = "name";
    public static final String CALLTYPE = "callType";// 通话类型
    public static final String ISCALLING = "iscalling";// 是否正在通话中
    public static final String SOURCEID = "sourceId";// //0：语音电话状态 1：视频电话关闭摄像头状态
    public static final String BTNSTY = "btnsty";// 按钮上的内容，接听、静音、取消静音
    public static final String BTNCONTENT = "btnContent";// 静音 取消静音
    public static final String BTN_ID = "btn_id";

    /** 视频文件最大限制：30M */
    public static final long MAX_VIDEO_FILE_SIZE = 30 * 1024 * 1024;
    /** 图片文件最大限制：10M */
    public static final long MAX_IMAGE_FILE_SIZE = 10 * 1024 * 1024;
    // /** 图片文件压缩大小：2M */
    // public static final long MAX_IMAGE_SEND_SIZE = 2 * 1024 * 1024;
    /** 上传图片默认大小 */
    public static final long DEFAULT_IMAGE_SEND_SIZE = 300 * 1024;

    /** audio mode in communication */
    public static final int AUDIO_MODE_IN_COMMUNICATION = Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ? AudioManager.MODE_IN_CALL
            : AudioManager.MODE_IN_COMMUNICATION;

    /** 消息列表一页数据条数 */
    public static final int NOTICE_PAGE_CNT = 30;
    //关联我的设备类型
    public static final String DEVICE_QUERYTYPE_MOBILE = "1";
    public static final String DEVICE_QUERYTYPE_TV = "0";

    public static String ToAudioDetectKEY= "ToAudioDetectKEY";

	// @功能特殊字符
    public static final char SPECIAL_CHAR = 8197;

    /**公众号分页查询--每页条数**/
    public static final int PUBLIC_NO_PAGESIZE = 30;

    public static final String DEVICE_TYPE = "ANDROID_KESHI";
}
