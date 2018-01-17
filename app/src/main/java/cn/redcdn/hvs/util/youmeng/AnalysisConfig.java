package cn.redcdn.hvs.util.youmeng;
/*
统计计数事件

在您希望跟踪的代码部分，调用如下方法：

MobclickAgent.onEvent(Context context, String eventId);

context指当前的Activity，eventId为当前统计的事件ID。

示例：统计微博应用中"转发"事件发生的次数，那么在转发的函数里调用

MobclickAgent.onEvent(mContext,"Forward");

 */

public final class AnalysisConfig {
	//注册登录
//	public final static String ACCESS_DESKTOP = "1001";//桌面启动
	public final static String ACCESS_REGIST = "1002";//引导页注册
	public final static String ACCESS_HOME= "1003";//引导页登陆成功，进入首页
	
	//首页操作
	public final static String CLICK_CONVENE_MEETING= "1004";//首页点击召开,成功进入会诊室
	public final static String CLICK_JOIN_MEETING= "1005";//首页页点击加入，进入加入会诊界面
	public final static String CLICK_CONTACTS= "1006";//首页点击联系人，进入联系人界面
	public final static String CLICK_CONTACT_PHOTO= "1007";//首页点击头像，进入“我”界面
	
	//会控操作
	public final static String CLICK_SPEAK= "1008";//点击发言按钮
	public final static String CLICK_PARTICIPANTS= "1009";//点击参会方按钮
	public final static String CLICK_INVITE= "1010";//点击邀请参会按钮
	public final static String CLICK_SWITCH_CAMERA= "1011";//点击切换摄像头按钮
	public final static String CLICK_CANCEL_TALK= "1012";//点击取消发言
	public final static String TRANSFER_MIC= "1013";//选择参会人员，传麦成功
	public final static String ADD_PARTICIPANTS_TO_CONTACT= "1014";//参会方添加至联系人
	public final static String INVITE_BY_VEDIONUMBER= "1015";//视讯号邀请
	public final static String INVITE_BY_CONTACTS= "1016";//已有联系人列表邀请
	public final static String LOCK_MEETING= "1017";//会诊加锁
	public final static String UNLOCK_MEETING= "1018";//会诊解锁
	public final static String SWITCH_WINDOW= "1019";//切换窗口
	public final static String CLICK_INTERPOSE= "1028";//点击插话按钮 
	public final static String CLICK_INTERPOSE_END= "1029";//点击说完了按钮 
	public final static String CLICK_OPEN_CLOSE_VIDEO= "1030";//点击“开启/关闭视频”按钮
	public final static String CLICK_CLOSE_CAMERA= "1031";//点击“关闭摄像头”按钮
	public final static String SWITCH_FREEMODE= "1032";//开关切换“自由模式”
	public final static String SWITCH_COMPEREMODE= "1033";//开关切换“主讲模式”
	public final static String CLICK_ASSIGN_SPEAK= "1034";//会控面板点击“指定发言”按钮
	public final static String CLICK_SPEAK_IN_ASSIGN_SPEAK_LIST= "1035";//指定发言列表，指定参会方发言，点击“发言”按钮
	public final static String CLICK_CANCEL_SPEAK_IN_ASSIGN_SPEAK_LIST= "1036";//指定发言列表，取消参会方发言，点击“取消发言”按钮
	public final static String CLICK_LIVE_TELECAST= "1037";//会控面板点击“直播”按钮
	public final static String CLICK_START_LIVE_TELECAST= "1038";//直播弹框中点击“开启直播”按钮
	public final static String CLICK_WECHAT_IN_LIVE_TELECAST= "1039";//直播点击“微信好友”
	public final static String CLICK_WECHATMOMENTS_IN_LIVE_TELECAST= "1040";//直播点击“朋友圈”
	public final static String CLICK_SMS_IN_LIVE_TELECAST= "1041";//直播点击“短信”
	public final static String CLICK_COPYLINK_IN_LIVE_TELECAST= "1042";//直播点击“复制链接”
	public final static String CLICK_RAISE_HANDS= "1043";//参会方会控面板点击“举手”按钮
	public final static String CLICK_ORDER_MEETING= "1044";//首页点击“预订会诊室”按钮
	public final static String CLICK_ORDER_IN_MEETING= "1045";//预约会诊室界面点击“预约”按钮
	public final static String CLICK_WECHAT_IN_ORDER= "1046";//预约成功界面点击 “微信好友”按钮
	public final static String CLICK_WECHATMOMENTS_IN_ORDER= "1047";//预约成功界面点击 “朋友圈”按钮
	public final static String CLICK_COPYLINK_IN_ORDER= "1048";//预约成功界面点击 “复制链接”按钮
    
	//加入会诊
	public final static String JOINMEETING_BY_NUMBER= "1020";//会诊号加入会诊
	public final static String JOINMEETING_BY_INVITELINK= "1021";//邀请链接加入会诊
	public final static String JOINMEETING_BY_MESSAGE= "1022";//短信邀请进入会诊
	
	//联系人
	public final static String  HOLD_MULTIPERSON_MEETING= "1023";//联系人界面点击“发起多人会诊”按钮
	public final static String  CLICK_MEETING_IN_CONTACTCARD= "1024";//名片页中点击“视频会诊”
	public final static String  ADDCONTACT= "1025";//手机号/视讯号添加联系人
	public final static String  MULTIPERSON_MEETING_BY_CONTACT= "1026";//选择联系人发起多人会诊
	public final static String  ACCESS_CONTACT_RECOMMEND= "1027";//进入通讯录推荐界面
}
