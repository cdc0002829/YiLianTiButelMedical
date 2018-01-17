package cn.redcdn.hvs.im;

import android.text.TextUtils;

import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.config.SettingData;
import cn.redcdn.hvs.util.StringUtil;
import cn.redcdn.log.CustomLog;
import cn.redcdn.hvs.im.preference.DaoPreference.PrefType;

/**
 * <dl>
 * <dt>UrlConstant.java</dt>
 * <dd>Description:访问接口url</dd>
 * <dd>Copyright: Copyright (C) 2011</dd>
 * <dd>Company: 安徽青牛信息技术有限公司</dd>
 * <dd>CreateDate: 2013-8-11 上午10:42:44</dd>
 * </dl>
 * 《佰库通行证接口.docx 》中接口的域名是：passport.baikuceshi.com:81/BaikuPassportV2
 * 《授权服务端接口.docx 》和《用户中心接口.docx 》中接口的域名是：accountapi.baikuceshi.com:81/BaikuUserCenterV2
 *
 * @author lihs
 */
public class UrlConstant {

    private static final String TAG = "UrlConstant";

    // 回声消除时延服务器地址
    public static final String NETPHONE_ECHO_URL = "http://220.178.7.254:8083/netPhoneEcho/netPhoneEchoService";

    /**文件存储服务器的url*/
    public static final String FILE_SERVER_ADDRESS = "/NubePhotoUpload";
    /**获取接口地址的url**/
    //miaolk modify 20140826 NPS的参数访问接口由ip+服务名 或 域名+服务名的形式组合而成
    // 现网地址210.51.12.100(割接前)/103.25.23.99(割接后)   http://nps.butel.com/nps2
    // TODO:域名+服务名组合,会通过友盟参数配制的形式修改；参数获取优先使用该组合
    public static String NPS_ADDRESS_DNS="http://nps.butel.com/nps2";
    //	public static String NPS_ADDRESS_DNS="http://tms.baikuceshi.com:81/nps2";
    //add by zzwang: 用于解决测试环境域名失效问题，暂改成ip地址
    //	public static String NPS_ADDRESS_DNS="http://219.142.74.32:81/nps2";

    // TODO：IP+服务名组合（备用），当访问域名+服务名组合没有获取成功时，用该组合再次访问一次  此ip地址需要与上面域名配对修改
    public static final String NPS_ADDRESS_IP="http://103.25.23.99/nps2";

    //测试网的IP地址（备用）sunjian 添加2015/12/11  目的是解决 测试网使用域名访问3次失败，使用测试网的IP地址进行访问。
    //	public static final String NPS_ADDRESS_IP="http://219.142.74.32:81/nps2";

    // 开发网地址
    public static final String NPS_ADDRESS_DEV="http://219.142.74.32:81/nps2";
    //	 public static final String NPS_ADDRESS="http://219.142.74.44/nps2";
    // 开发网地址：211.103.182.57  tms.baikuceshi.com:81(该域名已暂停使用)
    // 合肥环境：220.178.7.254:8082
    // 北京开发环境：http://211.103.182.57/nps2
    public static final String NPS_INTERFACE_URL = "/parameter/getServiceParameters";
    public static final String NPS_ADDRESS_HTTP="http://";

    // authorize接口
    public static final String MENTHOD_AUTHORIZE_SERVICE = "authorize";

    /** 一键回家接口 */
    public static final String MENTHOD_ONEKEYVISIT_SERVICE = "tvFamilyNumber";
    /** 关联我的设备 */
    public static final String MENTHOD_HOUSEKEEPING_SERVICE = "houseKeeping";


    /******************************START************************/
    public static final String TEST_PASSPORT = "http://accountapi.baikuceshi.com:81/BaikuUserCenterV2/passportService";
    public static final String TEST_LOGIN = "http://accountapi.baikuceshi.com:81/BaikuUserCenterV2/auth";
    /**将头像地址和账号进行绑定接口**/
    public static final String MENTHOD_BIND_HEAD_ADDRESS_SERVICE = "setAttrApp";
    /**修改用户资料的url**/
    public static final String MENTHOD_MODIFY_USER_INFO = "setAccountAttr";
    /** 修改密码**/
    public static final String MENTHOD_MODIFIED_PASSWORD_SERVICE = "changePassword";

    /**注册第一步调用接口url lihs **/
    public static final String MENTHOD_REGISTER_SERVICE = "registerAccount";

    /**注册第一步获取佰库号码service lihs**/
    public static final String MENTHOD_BAIKU_NUMBER_SERVICE = "getNubeNumberList";

    /** 注册第二步激活注册账号 手机号或者nube号码 lihs **/
    public static final String MENTHOD_ACTIVE_REGISTER_SERVICE ="activateAccount";

    /**注册重新发送激活码接口 lihs **/
    public static final String MENTHOD_REPEAT_SEND_ACTIVE_CODE = "reSendActivateCode";

    /**重置密码第一步获取验证码**/
    public static final String MENTHOD_RESET_PASSWORD_CODE_SERVICE = "sendCodeForResetPwd";

    /**重置密码第二步 **/
    public static final String MENTHOD_RESET_PASSWORD_SERVICE = "resetPassword";

    /**查询账户信息接口**/
    public static final String  MENTHOD_QUERY_USER_INFO_SERVICE = "getAllUserInfo";

    /**获取接口地址的url 的service 名称**/
    public static final String MENTHOD_SERVICE_PARAM = "getServiceParameters";
    /**绑定视频号service**/
    public static final String  BINDNUBENUMBER  ="bindNubeNumber";
    /**发现好友接口service 名称**/
    public static final String  FIND_FRIEND_SERVICE = "searchAccount";
    /**发现好友接口service 名称,针对400 、 800号码**/
    public static final String  FIND_FRIEND_SERVICE_FOR_800 = "SearchAccount";
    public static final String DEFAULT_POST_SEARCHACCOUNT_URL="http://baikuceshi.com:81/UnifiedService/callService";
    /**查询活动service**/
    public static final String QUERY_ACTIVITY_SERVICE = "queryActivityList";
    public static final String PARTICIPATION_ACTIVITY_SERVICE = "participateActivity";
    public static final String USER_ORDER_MSG_SERVICE = "queryOrderList";
    /** 提交日志文件链接接口 */
    public static final String COMMIT_LOGFILE_URL_SERVICE = "Interface/saveFileURL.html";
    /**修改密码**/
    public static final String KEY_UPDATE_PASSWORD = "changePasswordByOldPwd";
    /**发送设置登录帐号的验证码*/
    public static final String KEY_SEND_CODE_FOR_SET_ACCOUNT= "sendCodeForSetAccount";
    /**设置账号接口*/
    public static final String SET_ACCOUNT_FOR_EXPERIENCE = "setAccount";

    /**ACD center接口*/
    public static final String GET_ACDCENTER_SERVICE = "getACDInfo";
    /*******************************END************************/


    /**I看家接口定义*/
    // 解除关联关系
    public static final String TV_REMOVE_HK_RELATION = "removeHKRelation";
    // 获取关联关系
    public static final String TV_QUERY_HK_RELATION = "queryHKRelation";
    // 设置自动侦测开关
    public static final String TV_SETUP_AUTO_DETECT = "setupAutoDetect";
    // 获取自动侦测开关
    public static final String TV_GET_AUTO_DETECT = "getAutoDetect";
    // 建立、维护关联关系
    public static final String TV_SAVE_HK_RELATION = "saveHKRelation";


    //========================群组管理=========================
    // 新建群
    public static final String METHOD_CREATE_GROUP = "CreateGroup";

    public static final String METHOD_CREATE_DT_GROUP = "CreateDTGroup";
    // 修改群信息
    public static final String METHOD_EDIT_GROUP = "EditGroupInfo";
    // 群增加成员（邀请）
    public static final String METHOD_ADD_USERS = "GroupAddUsers";
    // 删除群成员（踢人）
    public static final String METHOD_DEL_USERS = "GroupDelUsers";
    // 退出群
    public static final String METHOD_QUITE_GROUP = "QuitGroup";
    // 解散群
    public static final String METHOD_DEL_GROUP = "DeleteGroup";
    // 查询群详情
    public static final String METHOD_QUERY_GROUP_DETAIL = "QueryGroupDetail";
    // 备注用户名
    public static final String METHOD_TAG_USERNAME = "TagGroupUserName";
    // 获取与某人相关群的列表
    public static final String METHOD_GET_ALL_GROUP = "GetAllGroup";

    public static final String METHOD_ADD_ONESELF = "GroupAddOneself";
    //=======================================================

    //========================公众号管理=========================
    //获取推荐公众号列表
    public static final String METHOD_EPG_GET_RECOMMEND_PUBLIC_NO = "getrecommendpublicno";
    //	根据关键字查询公众号列表
    public static final String METHOD_EPG_SEARCH_PUBLIC_NO = "searchpublicno";
    //	根据公众号唯一标识查询公众号详情
    public static final String METHOD_EPG_GET_PUBLIC_NO_DETAILS = "getpublicnodetails";
    //	根据父id获取其下子栏目集合
    public static final String METHOD_EPG_GET_CATALOGS = "getcatalogs";
    //	获取编目下包含的内容集合
    public static final String METHOD_EPG_GET_CONTENTS = "getcontents";
    //	查询某个公众号下包关键的内容集合
    public static final String METHOD_EPG_SEARCH_CONTENTS = "searchcontents";
    //	获取内容详情
    public static final String METHOD_EPG_GET_CONTENT_DETAILS = "getcontentdetails";
    //	订阅公众号
    public static final String METHOD_EPG_SUBSCRIBE_PUBLIC_NO = "subscribepublicno";
    //	取消订阅公众号
    public static final String METHOD_EPG_CANCEL_SUBSCRIBE_PUBLIC_NO = "cancelsubscribepublicno";
    //	获取订阅公众号
    public static final String METHOD_EPG_GET_SUBSCRIBE_PUBLIC_NO = "getsubscribepublicno";
    //	获取推荐内容
    public static final String METHOD_EPG_GET_FEATURED_CONTENT = "getfeaturedcontent";
    //	获取预览内容集合
    public static final String METHOD_EPG_GET_PREVIEW_CONTENT = "getpreviewcontents";
    //=======================================================

    //TODO 测试活动相关域名
    //	public static final String ACTIVITY_URL = "http://baikuvoip.com/NubeNumberService/activity";
    //	/**查询用户订购信息urle**/
    //	public static final String USER_ORDER_MSG_URL = "http://baikuvoip.com/NubeNumberService/order";
    /******************************参数类常量********************************************/
    public static int timeoutSocket = 60 * 1000;

    /**
     * @author: lihs
     * @Title: getCommUrl
     * @Description: 根据key值取，对应的接口名称
     * @param key 放在 @CommonPreferce 下面
     * @return KEY_BAIKU_PASSPORT_URL
     * @date: 2013-8-31 下午12:51:36
     *
     */
    //TODO 接口测试
    public static String getCommUrl(PrefType key) {
        String url = MedicalApplication.getPreference().getKeyValue(key,"");
        if (!StringUtil.isEmpty(key.name()) && key.name().equals(PrefType.FILE_UPLOAD_SERVER_URL.name())) {
            if(url.endsWith("/")){
                url = url + FILE_SERVER_ADDRESS.substring(1);
            }else{
                url = url + FILE_SERVER_ADDRESS;
            }
        }else if(key.name().equals(PrefType.KEY_GROUP_MANAGER_URL.name())){
        }else if(key.name().equals(PrefType.KEY_FAVORITE_SERVER_URL.name())){
            //收藏url 直接从配置文件中读取
            url = SettingData.getInstance().Favorite_URL;
            if(url.endsWith("/")){
                url = url+"messageV2/favoriteapi?";
            }else{
                url = url + "/" + "messageV2/favoriteapi?";;
            }
        }
        CustomLog.d(TAG,"获取接口的域名:key= "+key.name()+";;;reslut= "+url);
        return url;
    }

    public static String getCommUrl(PrefType key,String method) {
        String url = MedicalApplication.getPreference().getKeyValue(key, "");
        if (key.name().equals(PrefType.KEY_GROUP_MANAGER_URL.name())) {
            url = url+method+".html";
        }else if(key.name().equals(PrefType.KEY_V_CHANNEL_URL.name())){
            if (TextUtils.isEmpty(url)){
                url="http://justtv.cn/epg";
            }
//			url="http://210.51.168.119:8080/epg";
            if(!url.endsWith("/")){
                url = url + "/";
            }
            url=url+method;
        }
        CustomLog.d(TAG,"获取接口的域名:key= " + key.name() + ";;;reslut= " + url);
        return url;
    }
}

