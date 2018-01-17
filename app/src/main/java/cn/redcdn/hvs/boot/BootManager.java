package cn.redcdn.hvs.boot;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;

import com.redcdn.keyeventwrite.KeyEventConfig;
import com.redcdn.keyeventwrite.KeyEventFileManager;
import com.redcdn.keyeventwrite.KeyEventWrite;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import cn.redcdn.crash.Crash;
import cn.redcdn.datacenter.config.ConstConfig;
import cn.redcdn.datacenter.meetingmanage.AcquireParameter;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.appinstall.InstallCallBackListerner;
import cn.redcdn.hvs.appinstall.MeetingVersionManager;
import cn.redcdn.hvs.config.NpsParamConfig;
import cn.redcdn.hvs.config.SettingData;
import cn.redcdn.hvs.dep.FileUploadManager;
import cn.redcdn.hvs.util.CustomDialog;
import cn.redcdn.hvs.util.CustomDialog.OKBtnOnClickListener;
import cn.redcdn.log.CustomLog;
import cn.redcdn.log.LogMonitor;
import cn.redcdn.log.LogcatFileManager;

/**
 * 启动流程：启动日志模块 -> 拷贝配置文件 -> 判断是否第一次启动 -> 显示引导页面 -> 显示欢迎界面 ->
 * 访问nps服务器获取配置信息 -> 初始化DataCenter数据 -> 检查是否有新版本 -> 登录 ->
 * 启动HostAgent -> initContactManager -> 显示主页面
 */
public abstract class BootManager {
    private final String TAG = getClass().getName();
    private Context mContext;

    private HandlerThread mStartupThread;
    private Handler mStartupHandler;

    private String sysVersionInfo = "";
    private String appVersionInfo = "";

    public static final int MSG_START_BOOT = 0x00000001; // 开始启动
    public static final int MSG_COPY_CFG_FILE = 0x00000002; // 拷贝配置文件
    public static final int MSG_ACQUIRE_NPS_CFG = 0x00000006; // 获取nps配置信息
    public static final int MSG_CHECK_APP_VERSION = 0x00000013; // 检测应用版本
    public static final int MSG_START_HOST_AGENT = 0x00000009; // 启动Hostagent

    public static final int MSG_BOOT_SUCCESS = 0x00000011; // 启动成功
    public static final int MSG_BOOT_FAILED = 0x00000012; // 启动失败

    //测试网
    private  static  String logAppkey = "0fc3b5c43f8d44fdbbad7f7e233f6e9e";
    private  static  String logUrl = "http://testmedical.butel.com:8189/MedicalLogMonitor/external/";

    //现网
//    private  static  String logAppkey = "0ea8689329a446a5b67fc360ef195fc4";
//    private  static  String logUrl = "http://218.94.66.98:8980/MedicalLogMonitor/external/";

    private int mCurrentStep = -1;

    private InstallCallBackListerner appVersionCheckListener;
    private boolean isPkgChanged = false;

    public abstract void onBootSuccess();

    public abstract void onBootFailed(int step, int errorCode, String errorMsg);

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            mCurrentStep = msg.what;
            switch (msg.what) {
                case MSG_START_BOOT:
                    startBoot();
                    break;
                case MSG_COPY_CFG_FILE:

                    break;
                case MSG_ACQUIRE_NPS_CFG:
                    acquireMeetingParameter();
                    break;
                case MSG_CHECK_APP_VERSION:
                    checkAppVersion();
                    break;
                case MSG_START_HOST_AGENT:
                    initHostAgent();
                    break;
                case MSG_BOOT_SUCCESS:
                    appVersionCheckListener = null;
                    if(isPkgChanged){
                        //包有更新 退出登录状态
                        AccountManager.getInstance(mContext).exitLoginState();
                        AccountManager.getInstance(mContext).clearLoginCache();
                    }
                    onBootSuccess();
                    break;
                case MSG_BOOT_FAILED:
                    onBootFailed(msg.arg1, msg.arg2, (String) msg.obj);
                    break;
            }
        }
    };

    public BootManager(Context context) {
        mContext = context;
    }

    public void start() {
        mStartupThread = new HandlerThread("StartUpThread");
        mStartupThread.start();

        mStartupHandler = new Handler(mStartupThread.getLooper());
        mHandler.sendEmptyMessage(MSG_START_BOOT);
    }

    public void release() {
        CustomLog.i(TAG, "BootManager::release()");
        mHandler = null;
        mContext = null;
        mStartupThread.quit();
        mStartupHandler = null;
    }

    public int getCurrentStep() {
        return mCurrentStep;
    }

    private void startBoot() {
        SettingData.getInstance();

        mHandler.post(new Runnable() {

            @Override
            public void run() {
                try {
                    LogcatFileManager.getInstance().setLogDir(SettingData.LogRootDir);
                    LogcatFileManager.getInstance().start(mContext.getPackageName());
                    LogMonitor.getInstance().init(logAppkey,logUrl);
                    KeyEventFileManager.getInstance().start(mContext.getPackageName());

                    Crash crash = new Crash();
                    crash.setDir(SettingData.LogRootDir);
                    crash.init(mContext, mContext.getPackageName());

                    String state = Environment.getExternalStorageState();
                    if (Environment.MEDIA_MOUNTED.equals(state)) {
                        CustomLog.d(mContext.getPackageName(), "sdcard mount");
                    } else {
                        CustomLog.e(mContext.getPackageName(), "sdcard unmount");
                    }

                    String[] s = Build.DISPLAY.split("@");
                    if (s != null && s.length == 2) {
                        sysVersionInfo = s[1];
                    }
                    appVersionInfo = mContext.getPackageManager().getPackageInfo(
                            mContext.getPackageName(), 0).versionName;
                    CustomLog.i(
                            mContext.getPackageName(),
                            "package: "
                                    + mContext.getPackageName()
                                    + " | version: "
                                    + appVersionInfo + " | Device: "
                                    + Build.MODEL + " | sdk version: " + Build.VERSION.SDK_INT
                                    + " | system version: " + Build.VERSION.RELEASE);

                    CustomLog.i(TAG, "BootManager::startBoot() 开始执行启动逻辑");

                    copyConfigfile();

                    mHandler.sendEmptyMessage(MSG_ACQUIRE_NPS_CFG);
                    KeyEventWrite.write(KeyEventConfig.BASIC_INFO
                            + "_ok_"
                            + "Mobile_"
                            + appVersionInfo + "-" + sysVersionInfo + "-" + sysVersionInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

//    mStartupHandler.postDelayed((new Runnable() {
//
//      @Override
//      public void run() {
//        copyConfigfile();
//      }
//    }), 300);
    }

    public void retry(int step) {
        if (mHandler.hasMessages(step)) {
            mHandler.removeMessages(step);
        }
        if (step == MSG_CHECK_APP_VERSION) {
            CustomLog.i(TAG, "BootManager::retry 继续检测应用版本信息");
            MeetingVersionManager.getInstance().checkVersion(mContext);
        } else {
            mHandler.sendEmptyMessage(step);
        }
    }

    // 获取nps信息
    //包没有变化：读取缓存，成功（使用缓存，触发服务器访问）、失败（触发服务器访问、存缓存）
    //包有变化：触发服务器访问，失败（使用缓存）、成功（存缓存）
    private void acquireMeetingParameter() {
        CustomLog.i(TAG, "BootManager::acquireMeetingParameter() 获取NPS参数");

        ConstConfig.npsWebDomain = SettingData.getInstance().readNpsUrlFromLocal();
        ConstConfig.slaveNpsWebDomain = SettingData.getInstance().readSecondNpsUrlFromLocal();

        CustomLog.i(TAG, "BootManager::acquireMeetingParameter() NPS_URL: "
                + ConstConfig.npsWebDomain + " |Slave_NPS:" + ConstConfig.slaveNpsWebDomain + " | serialNum: " + SettingData.VIRTUAL_DEVICE_NUM + " | type: "
                + SettingData.NpsDeviceType);
        AcquireParameter ap = new AcquireParameter() {
            @Override
            public void onSuccess(JSONObject bodyObject) {
                if (bodyObject == null) {
                    CustomLog.e(TAG,
                            "BootManager::acquireMeetingParameter() 获取NPS参数失败，返回为空");
                    Message msg = new Message();
                    msg.what = MSG_BOOT_FAILED;
                    msg.obj = "获取配置信息失败";
                    msg.arg1 = MSG_ACQUIRE_NPS_CFG;
                    mHandler.sendMessage(msg);
                    KeyEventWrite.write(KeyEventConfig.GET_NPS_INFO + "_fail_" + "Mobile" + "_return==null");
                    return;
                }

                if (SettingData.getInstance().disposeNpsData(bodyObject.toString())) {
                    //写缓存
                    SettingData.getInstance().restoreNpsConfigToSharePre(bodyObject.toString());
                    mHandler.sendEmptyMessage(MSG_CHECK_APP_VERSION);
                } else {
                    Message msg = new Message();
                    msg.what = MSG_BOOT_FAILED;
                    msg.obj = "获取配置信息失败";
                    msg.arg1 = MSG_ACQUIRE_NPS_CFG;
                    mHandler.sendMessage(msg);
                }
            }

            @Override
            public void onFail(int statusCode, String statusInfo) {
                CustomLog.e(TAG, "获取nps信息失败. statusCode: " + statusCode + " |statusInfo: " + statusInfo);
                //读缓存
                SharedPreferences sharedPreferences = mContext.getSharedPreferences("CACHE_NPS_CONFIG", Activity.MODE_PRIVATE);
                String cacheNpsConfig = sharedPreferences.getString("CACHE_NPS_CONFIG", null);
                if (!TextUtils.isEmpty(cacheNpsConfig)) {
                    CustomLog.d(TAG, "cacheNpsConfig: " + cacheNpsConfig);
                    if (SettingData.getInstance().disposeNpsData(cacheNpsConfig)) {
                        mHandler.sendEmptyMessage(MSG_CHECK_APP_VERSION);
                    } else {
                        Message msg = new Message();
                        msg.what = MSG_BOOT_FAILED;
                        msg.obj = "获取配置信息失败";
                        msg.arg1 = MSG_ACQUIRE_NPS_CFG;
                        mHandler.sendMessage(msg);
                    }
                } else {
                    final CustomDialog dialog = new CustomDialog(mContext);
                    dialog.setOkBtnOnClickListener(new OKBtnOnClickListener() {
                        @Override
                        public void onClick(CustomDialog customDialog) {
                            dialog.cancel();
                            System.exit(0);
                        }
                    });
                    dialog.setTip("网络异常");
                    dialog.removeCancelBtn();
                    dialog.setCancelable(false);
                    dialog.setOkBtnText("退出");
                    dialog.show();
                }
            }
        };

        //从服务器获取NPS地址 成功之后写入缓存
        AcquireParameter ac = new AcquireParameter() {
            @Override
            protected void onSuccess(JSONObject responseContent) {

                if (responseContent == null) {
                    CustomLog.e(TAG,
                            "BootManager::acquireMeetingParameter ac 获取NPS参数失败，返回为空");
                    return;
                }else{
                    //从服务器读到数据 写缓存
                    SettingData.getInstance().restoreNpsConfigToSharePre(responseContent.toString());
                }
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                //获取服务器nps数据失败
                CustomLog.d(TAG,"BootManager AcquireParameter ac 从服务器获取NPS信息失败!");
                CustomLog.d(TAG,"BootManager AcquireParameter ac failed stausCode = "+statusCode+"statusInfo = "+statusInfo);
            }
        };


        ArrayList<String> requestList = new ArrayList<>();

        requestList.add(NpsParamConfig.Medical_COMMON);
        requestList.add(NpsParamConfig.APP_UPDATE);
        requestList.add(NpsParamConfig.LogUpload);
        requestList.add(NpsParamConfig.Medical_HELP);

        if (isPkgChanged) { //包有更新
            CustomLog.d(TAG, "BootManager::acquireMeetingParameter() 包有更新，访问服务器获取新的nps信息");
            ap.acquire(requestList, SettingData.NpsDeviceType, SettingData.VIRTUAL_DEVICE_NUM);
        } else  { //包未更新
            CustomLog.d(TAG, "BootManager::acquireMeetingParameter() 包未更新，优先从本地获取nps缓存信息");
            SharedPreferences sharedPreferences = mContext.getSharedPreferences("CACHE_NPS_CONFIG", Activity.MODE_PRIVATE);
            String cacheNpsConfig = sharedPreferences.getString("CACHE_NPS_CONFIG", null);
            if (!TextUtils.isEmpty(cacheNpsConfig)) {
                CustomLog.d(TAG, "cacheNpsConfig: " + cacheNpsConfig);
                if (SettingData.getInstance().disposeNpsData(cacheNpsConfig)) {
                    mHandler.sendEmptyMessage(MSG_CHECK_APP_VERSION);
                    CustomLog.d(TAG, "BootManager::acquireMeetingParameter() 包未更新，从本地解析nps缓存信息成功，访问服务器获取新的配置信息存入缓存");
                    ac.acquire(requestList, SettingData.NpsDeviceType, SettingData.VIRTUAL_DEVICE_NUM);

                } else {
                    CustomLog.d(TAG, "BootManager::acquireMeetingParameter() 包未更新，从本地解析nps缓存信息失败，访问服务器获取新的配置信息");
                    ap.acquire(requestList, SettingData.NpsDeviceType, SettingData.VIRTUAL_DEVICE_NUM);
                }
            } else {
                CustomLog.d(TAG, "BootManager::acquireMeetingParameter() 包未更新，从本地获取nps缓存信息失败，访问服务器获取新的配置信息");
                ap.acquire(requestList, SettingData.NpsDeviceType, SettingData.VIRTUAL_DEVICE_NUM);
            }
        }

    }

    public void checkAppVersion() {
        CustomLog.i(TAG,"checkAppVersion");
        //获取缓存视讯号，如果有，则触发日志上传，没有则以手机设备类型作为视讯号触发
        String nubeNum = AccountManager.getInstance(mContext.getApplicationContext()).getCacheNube();
        if (TextUtils.isEmpty(nubeNum)) {
            nubeNum = Build.MODEL;
        }
        FileUploadManager.getInstance().init(mContext.getApplicationContext(), nubeNum);
        FileUploadManager.getInstance().startLogUpload();
        MeetingVersionManager.getInstance().init(mContext.getApplicationContext());
        appVersionCheckListener = new InstallCallBackListerner() {

            @Override
            public void needOptimizationInstall() {
                CustomLog.i(TAG, "优化升级，继续执行启动逻辑！");
                MeetingVersionManager.getInstance().setCallBackNull();
                mHandler.sendEmptyMessage(MSG_START_HOST_AGENT);
            }

            @Override
            public void needForcedInstall() {
                CustomLog.i(TAG, "强制升级，终止启动逻辑，等待升级！");
                MeetingVersionManager.getInstance().setCallBackNull();
            }

            @Override
            public void noNeedInstall() {
                CustomLog.i(TAG, "无需升级，继续执行启动逻辑！");
                MeetingVersionManager.getInstance().setCallBackNull();
                mHandler.sendEmptyMessage(MSG_START_HOST_AGENT);
            }

            @Override
            public void errorCondition(int error) {
                CustomLog.i(TAG, "升级出错，继续执行启动逻辑！");
                MeetingVersionManager.getInstance().setCallBackNull();
                mHandler.sendEmptyMessage(MSG_START_HOST_AGENT);
            }
        };
        MeetingVersionManager.getInstance().checkOrInstall(mContext,
                appVersionCheckListener);

    }

    // 初始化Agent
    private void initHostAgent() {
        CustomLog.i(TAG, "BootManager::initAgent() 初始化 Agent");
        initMeetingInvite();
        mHandler.sendEmptyMessage(MSG_BOOT_SUCCESS);
        // FileUploadManager.getInstance().init(mContext);

    }

    // 拷贝配置文件
    private void copyConfigfile() {
        PackageManager pm = mContext.getPackageManager();
        PackageInfo pi;
        boolean diff = false;
        try {
            pi = pm.getPackageInfo(mContext.getPackageName(), 0);
            int currVersion = pi.versionCode;

            SharedPreferences sharedPreferences = mContext.getSharedPreferences(
                    "share", Context.MODE_PRIVATE);

            int oldVersion = sharedPreferences.getInt("versionCode", 0);

            if (oldVersion != currVersion) {
                diff = true;
                Editor editor = sharedPreferences.edit();
                editor.putInt("versionCode", currVersion);
                editor.commit();
            }
            CustomLog.i(TAG,
                    "BootManager::copyConfigfile() 判断版本号，是否存在升级情况 oldVersion: "
                            + oldVersion + " | currVersion: " + currVersion + " diff: "
                            + diff);
        } catch (NameNotFoundException e) {
            diff = false;
            e.printStackTrace();
        }

        isPkgChanged = diff;

        CustomLog.i(TAG, "BootManager::copyConfigfile() 开始拷贝配置文件");
        File rootPath = new File(SettingData.getInstance().rootPath);
        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }

        File cfgFile = new File(SettingData.getInstance().CfgPath);
        if (!cfgFile.exists()) {
            cfgFile.mkdirs();
        }
        deleteIMCfgFile();
        copyCfgFileToPath("Log.xml", SettingData.getInstance().CfgPath + "/Log.xml");
    }

    private void deleteIMCfgFile(){
        if(isPkgChanged){
            String filePath = "/mnt/sdcard/" + mContext.getPackageName() + "/butelconnect.ini";
            File file = new File(filePath);
            if(file.exists()){
                file.delete();
                CustomLog.d(TAG,"删除IM的配置文件");
            }
        }else {
            CustomLog.d(TAG,"包没有变，无需删除IM配置文件");
        }
    }

    private void initMeetingInvite() {
        CustomLog.i(TAG, "init MeetingInvite object by application context");
        // MeetingInvite.getInstance().init(mContext.getApplicationContext());
    }

    private void copyCfgFileToPath(String fileName, String filePath) {
        CustomLog.i(this.getClass().getName(), " copyCfgFileToPath fileName: "
                + fileName + " filePath : " + filePath);

        try {
            InputStream inputStream = mContext.getResources().getAssets()
                    .open(fileName);

            FileOutputStream fileOutputStream;

            File file = new File(filePath);
            if (file.exists() && !isPkgChanged) {
                CustomLog.i(TAG, "配置文件已存在，且包没有更新，不copy!");
                return;
            }

            if (file.exists() && isPkgChanged) {
                CustomLog.i(TAG, "配置文件已存在，但包有更新，删除之前的配置文件，重新copy!");
                file.delete();
            }

            fileOutputStream = new FileOutputStream(file);

            byte[] buf = new byte[1024];
            int ch;
            while ((ch = inputStream.read(buf)) != -1) {
                fileOutputStream.write(buf, 0, ch);
            }
            file.setReadable(true, false);

            fileOutputStream.flush();
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            CustomLog.e(TAG, e.getMessage());
        }
    }
}
