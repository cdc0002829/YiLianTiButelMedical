package cn.redcdn.hvs.config;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import com.redcdn.keyeventwrite.KeyEventConfig;
import com.redcdn.keyeventwrite.KeyEventWrite;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import cn.redcdn.datacenter.config.ConstConfig;
import cn.redcdn.hvs.BuildConfig;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.log.CustomLog;

public class SettingData {
    public final String TAG = getClass().getName();
    public static String PlayUrl = "http://ztv.butel.com";
    public static String VIRTUAL_DEVICE_NUM = "GDD511111111145"; //虚拟串号，用于nps获取
    public static String NpsDeviceType = "MobileMedical"; //设备类型，用于hvs医疗云平台获取nps地址

    public static String AUTH_IMEI = "mobinp_JIHY"; //imei，固定为mobinp_JIHY
    public static String AUTH_PRODUCT_ID = "prod002"; //产品标识，固定为prod002
    public static String AUTH_APPTYPE = "mobile"; //终端类型，固定为mobile
    public static String AUTH_DEVICETYPE = "ANDROID_JIHY"; //登录设备类型,固定为ANDROID_JIHY
    public static String Article_Preview_Url = "";
    public static String LogRootDir; //日志、配置文件根目录

    public String MASTER_MS_URL = "103.25.23.103:20001"; // 主后台接口服务器地址
    public String SLAVE_MS_URL = "103.25.23.103:20001"; // 从后台接口服务器地址

    //nps地址
    public String NPS_URL = BuildConfig.API_SERVER_URL;
    //双服务器第二个nps地址
    public String SLAVE_NPS_URL = BuildConfig.API_SERVER_URL;
////    nps地址
//    public String NPS_URL = "http://xmeeting.butel.com/nps_x1/";
//    //双服务器第二个nps地址
//    public String SLAVE_NPS_URL = "http://xmeeting.jihuiyi.cn/nps_x1/";

//    public String NPS_URL = "http://10.160.71.137:8018/nps_x1/";
//    public String SLAVE_NPS_URL = "http://10.160.71.137:8018/nps_x1/";

//public String NPS_URL = BuildConfig.Nps_MasterUrl;
//    //双服务器第二个nps地址
//    public String SLAVE_NPS_URL = BuildConfig .NPS_SlaverUrl;
    //juyuwang
//    public String NPS_URL = "http://10.160.71.137:8018/nps_x1/";
//    //双服务器第二个nps地址
//    public String SLAVE_NPS_URL = "http://10.160.71.137:8018/nps_x1/";


    public String PERSONAL_CENTER_URL = ""; // 个人用户中心地址
    public String SLAVE_PERSONAL_CENTER_URL = "";//从个人用户中心地址

    public String PERSONAL_CONTACT_URL = "";// 个人通讯录地址

    public String ENTERPRISE_CENTER_URL = "";// 企业用户中心 TODO
    public String SLAVE_ENTERPRISE_CENTER_URL = "";//从企业用户中心地址

    public String Favorite_URL = ""; //收藏服务器地址
    public String MDS_URL = ""; //医疗数据服务器地址
    public String Slave_MDS_URL = "";//从医疗数据服务器地址

    public String CDN_Url = ""; //cdn服务器地址
    public String Meeting_Url = ""; //会议nps地址
    public String Slave_Meeting_Url = "";//从会议nps地址
    public String Friend_Url = "";//好友关系服务器地址

    public String AppKey = ""; //appkey值，注册使用
    public String CDN_AppId = ""; //appId 值，访问cdn服务器使用

    public String GROUP_MANAGER_URL = ""; //群组服务器地址

    public String DOWNLAOD_LINK = "http://jihuiyi.cn/hvs/"; // 推荐中的下载地址
    public String JMEETING_WEBSITE = "http://www.jmeeting.cn/"; // 极会诊官网地址
    public String CUSTEMER_TEL1 = ""; // 客服电话1
    public String CUSTEMER_TEL2 = "";// 客服电话2


    public String HELP_URL;
    public String REVIEW_NUM = "";
    public String SERVICEAGREEMENT_URL = "";
    public String ShareUrl = "";
    public String adminNubeNum = "";
    public String adminHeadUrl = "";
    public String adminNickName = "";

    public String UPLOADIMAGEURL = ""; // 头像上传地址
    private String UPLOAD_IMAGE_URL_SUFFIX = "/dfs_upload/NubePhotoUpload"; // 头像上传后缀
    public String UPLOADIMGNAME = "file";
    public int tokenUnExist = -902;
    public int tokenInvalid = -903;

    public enum DeviceCategory {
        X1, N8, Mobile, N7
    }

    public enum RunDevice {
        Mobile, TV
    }

    /**
     * 运行目标设备，默认运行在手机设备上，如果打包运行在TV上的安装包，需要调整该值
     */
    public static final RunDevice runDevice = RunDevice.Mobile;

    public final String DeviceType = "Mobile";
    public String rootPath = "";
    public String CfgPath = Environment.getExternalStorageDirectory().getPath() + "/" + LogRootDir
            + "/config"; // 配置文件路径
    public String LogFileOutPath = ""; // 日志输出的路径
    public String CachePath; // 缓存目录路径
    public String logUploadPath = ""; // 上传数据压缩包临时放置目录

    public AppUpdateData AppUpdateConfig; // 应用升级配置
    public HostAgentData HostAgentConfig; // Host Agent 配置
    public final String customerServicephone = "400-668-2396";// 客服电话
    public final String serviceDeadLine = "2015-06-08";// 服务截至日期
    public LogUploadData LogUploadConfig; // 日志上传配置

    private static SettingData mInstance;
    public final String AppRestorePath; // app下载保存路径

    public static synchronized SettingData getInstance() {
        if (mInstance == null) {
            mInstance = new SettingData();

        }

        return mInstance;
    }

    public String readNpsUrlFromLocal() {
        File npsfile = new File(Environment.getExternalStorageDirectory().getPath() + "/" + LogRootDir
                + "/config/mobile_nps_address.txt");
        if (!npsfile.exists()) {
            CustomLog.i(TAG, "存放nps地址的本地文件[[[" + npsfile.getAbsolutePath()
                    + "]]]不存在!使用默认的nps地址--->" + NPS_URL);
            return NPS_URL;
        }
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(npsfile)));
            String line = br.readLine();
            if (!TextUtils.isEmpty(line)) {
                line = line.trim();
                if (TextUtils.isEmpty(line)) {
                    CustomLog.i(TAG, "读取文件[[[" + npsfile.getAbsolutePath()
                            + "]]]内容为空!使用默认的nps地址--->" + NPS_URL);
                } else {
                    NPS_URL = line;
                }
            } else {
                CustomLog.i(TAG, "读取文件[[[" + npsfile.getAbsolutePath()
                        + "]]]内容为空!使用默认的nps地址--->" + NPS_URL);
            }
        } catch (Exception e) {
            CustomLog.e(TAG, "文件 [[[" + npsfile.getAbsolutePath()
                    + "]]] 读取失败!使用默认的nps地址--->" + NPS_URL);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    CustomLog.e(TAG, "关闭文件流失败!!!");
                }
            }
        }
        return NPS_URL;
    }

    public String readSecondNpsUrlFromLocal() {
        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/" + LogRootDir
                + "/config/mobile_second_nps_address.txt");
        if (!file.exists()) {
            CustomLog.i(TAG, "存放second nps地址的本地文件[[[" + file.getAbsolutePath()
                    + "]]]不存在!使用默认的second nps地址--->" + SLAVE_NPS_URL);
            return SLAVE_NPS_URL;
        }
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line = br.readLine();
            if (!TextUtils.isEmpty(line)) {
                line = line.trim();
                if (TextUtils.isEmpty(line)) {
                    CustomLog.i(TAG, "读取文件[[[" + file.getAbsolutePath()
                            + "]]]内容为空!使用默认的second nps地址--->" + SLAVE_NPS_URL);
                } else {
                    SLAVE_NPS_URL = line;
                }
            } else {
                CustomLog.i(TAG, "读取文件[[[" + file.getAbsolutePath()
                        + "]]]内容为空!使用默认的second nps地址--->" + SLAVE_NPS_URL);
            }
        } catch (Exception e) {
            CustomLog.e(TAG, "文件 [[[" + file.getAbsolutePath()
                    + "]]] 读取失败!使用默认的sdecond nps地址--->" + SLAVE_NPS_URL);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    CustomLog.e(TAG, "关闭文件流失败!!!");
                }
            }
        }

        return SLAVE_NPS_URL;
    }

    private SettingData() {
        AppUpdateConfig = new AppUpdateData();
        HostAgentConfig = new HostAgentData();
        LogUploadConfig = new LogUploadData();
        LogRootDir = MedicalApplication.context.getPackageName() + "/main";
        rootPath = MedicalApplication.shareInstance()
                .getDir(MedicalApplication.context.getPackageName(), Context.MODE_PRIVATE).getAbsolutePath();
        CfgPath = Environment.getExternalStorageDirectory().getPath() + "/" + LogRootDir
                + "/config"; // 配置文件路径
        LogFileOutPath = Environment.getExternalStorageDirectory().getPath() + "/" + LogRootDir
                + "/log/logwriter";
        CachePath = rootPath + "/cache"; // 缓存目录
        AppRestorePath = Environment.getExternalStorageDirectory().getPath() + "/" + LogRootDir
                + "/appdownload";
        logUploadPath = Environment.getExternalStorageDirectory().getPath() + "/" + LogRootDir;// 上传数据压缩包临时放置目录
    }

    // 获取设备分类
    public DeviceCategory getDeivceCategory() {
        DeviceCategory category = DeviceCategory.N8;

        if (Build.MODEL.toString().equalsIgnoreCase("N7")) { // the big boss need
            // mobile APP run on N7
            // devices
            category = DeviceCategory.N7;
        } else {
            if (DeviceType.endsWith("JM1") || DeviceType.endsWith("M1")) {
                category = DeviceCategory.X1;
            } else if (DeviceType.endsWith("Mobile")) {
                category = DeviceCategory.Mobile;
            }
        }

        return category;
    }

    // 应用升级相关
    public class AppUpdateData {
        public String Master_ServerUrl = ""; // 主服务器地址
        public String Slave_ServerUrl = ""; // 从服务器地址
        public String ProjectName = ""; // 项目名称
        public String DeviceType = "MOBILE"; // 手机版固定升级使用的设备类型为MOBILE,不从nps进行获取
        public String CheckInterval = "7200"; // 检查版本间隔，单位秒
    }

    // Host Agent相关
    public class HostAgentData {
        public int LocalPort = 10000;
        public String LocalCmdIp = "127.0.0.1";
        public int LocalCmdPort = 10001;
        public String UICmdIp = "127.0.0.1";
        public int UICmdPort = 10002;
    }

    // 日志上传
    public class LogUploadData {
        public String ServerIP = "103.25.23.83";
        public int ServerPort = 10101;

    }

    // 初始化 DataCenter 数据
    public void initHttpRequestConfig() {
        CustomLog.i(TAG, "SettingData::initHttpRequestConfig() 初始化 DataCenter 中配置");

        ConstConfig.masterBmsWebDomain = SettingData.getInstance().MASTER_MS_URL;
        ConstConfig.slaveBmsWebDomain = SettingData.getInstance().SLAVE_MS_URL;
        ConstConfig.enterPriseUserCenterWebDomain = SettingData.getInstance().ENTERPRISE_CENTER_URL;
        ConstConfig.personalUserCenterWebDomain = SettingData.getInstance().PERSONAL_CENTER_URL;
        ConstConfig.slavePersonalUserCenterWebDomain = SettingData.getInstance().SLAVE_PERSONAL_CENTER_URL;
        ConstConfig.slaveEnterPriseUserCenterWebDomain = SettingData.getInstance().SLAVE_ENTERPRISE_CENTER_URL;
        ConstConfig.personalContactWebDomain = SettingData.getInstance().PERSONAL_CONTACT_URL;
        ConstConfig.masterAppUpdateServerWebDomain = SettingData.getInstance().AppUpdateConfig.Master_ServerUrl;
        ConstConfig.slaveAppUpdateServerWebDomain = SettingData.getInstance().AppUpdateConfig.Slave_ServerUrl;
        ConstConfig.groupManagerWebDomain = SettingData.getInstance().GROUP_MANAGER_URL;
        ConstConfig.MDS_URL = SettingData.getInstance().MDS_URL;
        ConstConfig.CDN_Url = SettingData.getInstance().CDN_Url;
        ConstConfig.Favorite_URL = SettingData.getInstance().Favorite_URL;
        ConstConfig.Friend_Url = SettingData.getInstance().Friend_Url;


        LogHttpRequestConfig();
    }

    public boolean disposeNpsData(String npsString) {
        try {
            JSONObject bodyObject = new JSONObject(npsString);
            JSONObject paramList = (JSONObject) bodyObject.get("paramList");
            if (null != paramList) {
                // 获取公共参数
                JSONObject commonObj = new JSONObject(paramList.get(
                        NpsParamConfig.Medical_COMMON).toString());
                if (null != commonObj) {
                    SettingData.getInstance().PERSONAL_CENTER_URL = commonObj
                            .getString(NpsParamConfig.Medical_COMMON_PUC_URL);
                    if (commonObj.has(NpsParamConfig.Medical_COMMON_SLAVE_PUC_URL)){
                        SettingData.getInstance().SLAVE_PERSONAL_CENTER_URL = commonObj.
                                getString(NpsParamConfig.Medical_COMMON_SLAVE_PUC_URL);
                    }else{
                        SettingData.getInstance().SLAVE_PERSONAL_CENTER_URL = SettingData.getInstance().PERSONAL_CENTER_URL;
                    }
                    SettingData.getInstance().ENTERPRISE_CENTER_URL = commonObj
                            .getString(NpsParamConfig.Medical_COMMON_PUC_URL);

                    SettingData.getInstance().PERSONAL_CONTACT_URL = commonObj
                            .getString(NpsParamConfig.Medical_COMMON_Persion_Contact_URL);
                    SettingData.getInstance().CDN_Url = commonObj
                            .getString(NpsParamConfig.Medical_COMMON_CDN_URL);
                    SettingData.getInstance().Favorite_URL = commonObj
                            .getString(NpsParamConfig.Medical_COMMON_Favorite_Server_Url);

                    SettingData.getInstance().MDS_URL = commonObj
                            .getString(NpsParamConfig.Medical_COMMON_MDS_URL);
                    int range = SettingData.getInstance().MDS_URL.indexOf("externalservice");
                    if (range != -1){
                        SettingData.getInstance().MDS_URL = SettingData.getInstance().MDS_URL.substring(0,range);
                    }
                    SettingData.getInstance().Slave_MDS_URL = commonObj
                            .getString(NpsParamConfig.Medical_COMMON_SLAVE_MDS_URL);
                    int range1= SettingData.getInstance().MDS_URL.indexOf("externalservice");
                    if (range1 != -1){
                        SettingData.getInstance().Slave_MDS_URL = SettingData.getInstance().Slave_MDS_URL.substring(0,range);
                    }

                        SettingData.getInstance().Meeting_Url = commonObj
                            .getString(NpsParamConfig.Medical_COMMON_MEETING_NPS_URL);
                    SettingData.getInstance().Slave_Meeting_Url = commonObj
                            .getString(NpsParamConfig.Medical_COMMON_SLAVE_MEETING_NPS_URL);

                    SettingData.getInstance().AppKey = commonObj
                            .getString(NpsParamConfig.Medical_COMMON_APPKEY);
                    ConstConfig.setAppkey(SettingData.getInstance().AppKey);
                    SettingData.getInstance().CDN_AppId = commonObj
                            .getString(NpsParamConfig.Medical_COMMON_CDN_APPID);
                    SettingData.getInstance().GROUP_MANAGER_URL = commonObj.getString(NpsParamConfig.Medical_COMMON_GROUP);
                    SettingData.getInstance().Friend_Url = commonObj.getString(NpsParamConfig.COMMON_Friendship_Service_URL);
                    if (commonObj.has(NpsParamConfig.COMMON_ArticlePreview)){
                        SettingData.getInstance().Article_Preview_Url = commonObj.getString(NpsParamConfig.COMMON_ArticlePreview);
                    }
                }

                // 获取APP升级参数
                JSONObject appUpdateObj = new JSONObject(paramList.get(
                        NpsParamConfig.APP_UPDATE).toString());
                if (null != appUpdateObj) {
                    // TODO 升级地址nps配置
                    SettingData.getInstance().AppUpdateConfig.Master_ServerUrl = appUpdateObj.getString(NpsParamConfig.APP_UPDATE_MASTER_ServerUrl);
                    String slave_serverUrl = appUpdateObj.optString(NpsParamConfig.APP_UPDATE_SLAVE_ServerUrl);

                    if (!slave_serverUrl.equals("")) {
                        SettingData.getInstance().AppUpdateConfig.Slave_ServerUrl = slave_serverUrl;
                    }

                    SettingData.getInstance().AppUpdateConfig.ProjectName = appUpdateObj
                            .getString(NpsParamConfig.APP_UPDATE_ProjectName);
                    SettingData.getInstance().AppUpdateConfig.DeviceType = appUpdateObj
                            .getString(NpsParamConfig.APP_UPDATE_DeviceType);
                }

                // 获取日志上传参数
                JSONObject logUploadObj = new JSONObject(paramList.get(
                        NpsParamConfig.LogUpload).toString());
                if (null != logUploadObj) {
                    SettingData.getInstance().LogUploadConfig.ServerIP = logUploadObj
                            .getString(NpsParamConfig.LogUpload_serverIp);
                    SettingData.getInstance().LogUploadConfig.ServerPort = logUploadObj
                            .getInt(NpsParamConfig.LogUpload_serverPort);
                }

                // 获取帮助参数
                JSONObject helpObj = new JSONObject(paramList.get(
                        NpsParamConfig.Medical_HELP).toString());
                if (null != helpObj) {
                    SettingData.getInstance().REVIEW_NUM = helpObj
                            .getString(NpsParamConfig.Medical_HELP_ReviewNum);
                    SettingData.getInstance().SERVICEAGREEMENT_URL = helpObj
                            .getString(NpsParamConfig.Medical_HELP_DisclaimerUrl);
                    if (helpObj.has(NpsParamConfig.Medical_HELP_DownloadUrl)){
                        SettingData.getInstance().DOWNLAOD_LINK = helpObj
                                .getString(NpsParamConfig.Medical_HELP_DownloadUrl);
                    }

                    if (helpObj.has(NpsParamConfig.Medical_HELP_CustomerTel)) {
                        String CustomerTel = helpObj
                                .getString(NpsParamConfig.Medical_HELP_CustomerTel);
                        String[] customerByte = CustomerTel.split("\\|");
                        if (customerByte.length > 1) {
                            SettingData.getInstance().CUSTEMER_TEL1 = customerByte[0];
                            SettingData.getInstance().CUSTEMER_TEL2 = customerByte[1];
                        }
                    }
                    if (helpObj.has(NpsParamConfig.Medical_HELP_ShareUrl)){
                        SettingData.getInstance().ShareUrl = helpObj.getString(NpsParamConfig.Medical_HELP_ShareUrl);
                    }
                    if (helpObj.has(NpsParamConfig.Medical_HELP_AdminNubeNum)){
                        SettingData.getInstance().adminNubeNum = helpObj.getString(NpsParamConfig.Medical_HELP_AdminNubeNum);
                    }
                    if (helpObj.has(NpsParamConfig.Medical_HELP_AdminHeadUrl)){
                        SettingData.getInstance().adminHeadUrl = helpObj.getString(NpsParamConfig.Medical_HELP_AdminHeadUrl);
                    }
                    if (helpObj.has(NpsParamConfig.Medical_HELP_AdminNickname)){
                        SettingData.getInstance().adminNickName = helpObj.getString(NpsParamConfig.Medical_HELP_AdminNickname);
                    }
                }
            } else { // paramList == null
                CustomLog.e(TAG,
                        "BootManager::acquireMeetingParameter() 获取NPS参数失败: paramList == null");
                return false;
            }
            initHttpRequestConfig();
            SettingData.getInstance().LogConfig();
            KeyEventWrite.write(KeyEventConfig.GET_NPS_INFO
                    + "_ok_Mobile");
        } catch (JSONException e) {
            CustomLog.e(TAG, e.getMessage());

            KeyEventWrite.write(KeyEventConfig.GET_NPS_INFO
                    + "_fail_"
                    + "Mobile" + "_paramList==null");
            return false;
        } catch (ClassCastException e) {
            CustomLog.e(TAG, e.getMessage());
            KeyEventWrite.write(KeyEventConfig.GET_NPS_INFO
                    + "_fail_"
                    + "Mobile" + "_paramList==null");
            return false;
        }

        return true;
    }

    public void useTestData() {
        SettingData.getInstance().PERSONAL_CENTER_URL = "http://124.205.216.253:81";
        SettingData.getInstance().ENTERPRISE_CENTER_URL = "http://124.205.216.253:81";

        SettingData.getInstance().PERSONAL_CONTACT_URL = "http://124.205.216.253:81";
        SettingData.getInstance().CDN_Url = "tv.butel.com";
        SettingData.getInstance().Favorite_URL = "http://124.205.216.253:81";

        SettingData.getInstance().MDS_URL = "http://175.102.21.35:10080";
        SettingData.getInstance().Slave_MDS_URL = "http://175.102.21.35:10080";

        SettingData.getInstance().Meeting_Url = "http://175.102.132.82:8018/nps_x1";
        SettingData.getInstance().Slave_Meeting_Url = "http://103.25.23.83:8018/nps_x1/";

        SettingData.getInstance().AppKey = "e16557a20eae4093874c3faff893a62e";
        SettingData.getInstance().CDN_AppId = "9320c1faa4334eff";

        initHttpRequestConfig();
        SettingData.getInstance().LogConfig();
    }

    public void restoreNpsConfigToSharePre(String npsString) {
        CustomLog.d(TAG, "SettingData::restoreNpsConfigToSharePre 保存 nps配置 数据到SharePreference");

        SharedPreferences sharedPreferences = MedicalApplication.shareInstance().getSharedPreferences("CACHE_NPS_CONFIG", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("CACHE_NPS_CONFIG", npsString);
        editor.commit();
    }

    /**
     * 从SharePrefence 从读取数据，用于在某些特殊场景下应用程序被杀死，使用缓存数据重建
     *
     * @return
     */
    public String readNpsConfigFromSharePre() {
        CustomLog.d(TAG, "SettingData::readHttpRequestConfigFromSharePre 从SharePreference 中读取 nps缓存数据");

        SharedPreferences sharedPreferences = MedicalApplication
                .shareInstance().getSharedPreferences("CACHE_NPS_CONFIG", Activity.MODE_PRIVATE);
        String cacheNpsConfig = sharedPreferences.getString("CACHE_NPS_CONFIG", null);

        return cacheNpsConfig;
    }

    private void LogHttpRequestConfig() {
        CustomLog.i(TAG, "ConstConfig 配置参数");
        CustomLog.i(TAG, "ConstConfig.masterBmsWebDomain：" + ConstConfig.masterBmsWebDomain);
        CustomLog.i(TAG, "ConstConfig.slaveBmsWebDomain：" + ConstConfig.slaveBmsWebDomain);
        CustomLog.i(TAG, "ConstConfig.enterPriseUserCenterWebDomain：" + ConstConfig.enterPriseUserCenterWebDomain);
        CustomLog.i(TAG, "ConstConfig.personalUserCenterWebDomain：" + ConstConfig.personalUserCenterWebDomain);
        CustomLog.i(TAG, "ConstConfig.personalContactWebDomain：" + ConstConfig.personalContactWebDomain);
        CustomLog.i(TAG, "ConstConfig.masterAppUpdateServerWebDomain：" + ConstConfig.masterAppUpdateServerWebDomain);
        CustomLog.i(TAG, "ConstConfig.slaveAppUpdateServerWebDomain：" + ConstConfig.slaveAppUpdateServerWebDomain);

        CustomLog.i(TAG, "ConstConfig.Mds_Url：" + ConstConfig.getMdsUrl());
        CustomLog.i(TAG, "ConstConfig.Cdn_Url：" + ConstConfig.getCdnUrl());
        CustomLog.i(TAG, "ConstConfig.Favorite_Url：" + ConstConfig.getFavoriteUrl());
        CustomLog.i(TAG, "ConstConfig.Friend_Url：" + ConstConfig.getFriend_Url());
    }

    // 打印配置信息
    public void LogConfig() {
        CustomLog.i(TAG, "SettingData 配置参数");
        CustomLog.i(TAG, "设备类型： " + DeviceType + " | 类别：" + getDeivceCategory());
        CustomLog.i(TAG, "运行目标设备： " + runDevice);
        CustomLog.i(TAG, "NPS 参数服务器地址：" + NPS_URL);
        CustomLog.i(TAG, "从NPS 参数服务器地址：" + SLAVE_NPS_URL);

        CustomLog.i(TAG, "统一认证服务器地址：" + PERSONAL_CENTER_URL);
        CustomLog.i(TAG, "个人通讯录服务器地址：" + PERSONAL_CONTACT_URL);

        CustomLog.i(TAG, "医疗数据服务器地址：" + MDS_URL);
        CustomLog.i(TAG, "从医疗数据服务器地址：" + Slave_MDS_URL);
        CustomLog.i(TAG, "会议nps地址：" + Meeting_Url);
        CustomLog.i(TAG, "从会议nps地址：" + Slave_Meeting_Url);
        CustomLog.i(TAG, "收藏服务器地址：" + Favorite_URL);
        CustomLog.i(TAG, "cdn服务器地址：" + CDN_Url);
        CustomLog.i(TAG, "群组服务器地址：" + GROUP_MANAGER_URL);
        CustomLog.i(TAG, "好友关系服务器：" + Friend_Url);
        CustomLog.i(TAG, "appkey值：" + AppKey);
        CustomLog.i(TAG, "appId：" + CDN_AppId);
        CustomLog.i(TAG, "文章页面地址：" + Article_Preview_Url);

        CustomLog.i(TAG, "App升级配置参数");
        CustomLog.i(TAG, "主App升级 服务器地址： " + AppUpdateConfig.Master_ServerUrl);
        CustomLog.i(TAG, "从App升级 服务器地址： " + AppUpdateConfig.Slave_ServerUrl);
        CustomLog.i(TAG, "App升级 项目名称： " + AppUpdateConfig.ProjectName);
        CustomLog.i(TAG, "App升级 设备类型： " + AppUpdateConfig.DeviceType);
        CustomLog.i(TAG, "App升级检测时间间隔： " + AppUpdateConfig.CheckInterval);

        CustomLog.i(TAG, "日志上传配置参数");
        CustomLog.i(TAG, "上传服务器地址： " + LogUploadConfig.ServerIP);
        CustomLog.i(TAG, "上传服务器端口： " + LogUploadConfig.ServerPort);

        CustomLog.i(TAG, "Help配置参数");
        CustomLog.i(TAG, "审核专线: " + REVIEW_NUM);
        CustomLog.i(TAG, "免责声明：" + SERVICEAGREEMENT_URL);
        CustomLog.i(TAG, "引导地址" + DOWNLAOD_LINK);
        CustomLog.i(TAG, "客服电话1:" + CUSTEMER_TEL1);
        CustomLog.i(TAG, "客服电话2:" + CUSTEMER_TEL2);
        CustomLog.i(TAG, "分享地址:" + ShareUrl);
        CustomLog.i(TAG, "系统服务视讯号:" + adminNubeNum);


    }
}
