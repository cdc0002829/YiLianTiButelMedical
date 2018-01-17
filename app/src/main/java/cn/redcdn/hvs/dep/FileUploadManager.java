package cn.redcdn.hvs.dep;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ServerSocket;

import cn.redcdn.commonutil.NetConnectHelper;
import cn.redcdn.dep.FileUploadClientJNI;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.config.SettingData;
import cn.redcdn.log.CustomLog;


public class FileUploadManager {
    private String tag = FileUploadManager.class.getName();
    private Context context;
    private static FileUploadManager mInstance;
    private String mAccountId;
    private static final String DEVICE = "HVS";

    private JSONArray ja = null;
    private JSONObject jo1 = null;
    private JSONObject jo2 = null;
    private JSONObject jo3 = null;
    private JSONObject jo4 = null;
    private JSONObject jo6 = null;
    private JSONObject jo7 = null;
    private JSONObject jo8 = null;
    private JSONObject jo9 = null;
    private JSONObject jo10 = null;

    private JSONObject LogPathJson = null;

    private FileUploadManager() {

    }

    // meetingroom页面发送的该广播
    public static final String START_MEETING_BROADCAST = "cn.redcdn.jmeetingsdk.meetingroom.startmeeting";
    public static final String END_MEETING_BROADCAST = "cn.redcdn.jmeetingsdk.meetingroom.endmeeting";

    public static synchronized FileUploadManager getInstance() {
        if (mInstance == null) {
            mInstance = new FileUploadManager();
        }
        return mInstance;
    }

    public void init(Context context, String account) {
        CustomLog.d(tag, "FileUploadManager::init() device: " + DEVICE + " | account: " + account);
        this.context = context;
        this.mAccountId = account;
        initLogUpdatePath();
        registerReceiver();
    }
    public void updateAcountID(String account){
        this.mAccountId = account;
    }


    /**
     * 开始日志上传
     */
    public void startLogUpload() {
        CustomLog.d(tag, "startLogUpload() accountId: " + this.mAccountId);
        CustomLog.d(tag, "startLogUpload() DEVICE: " + DEVICE);
        CustomLog.d(tag, "startLogUpload() localIP: " + NetConnectHelper.getLocalIp(MedicalApplication.shareInstance()));
        CustomLog.d(tag, "startLogUpload() uploadPath: " + SettingData.getInstance().logUploadPath);
        CustomLog.d(tag, "startLogUpload() pathjson: " + LogPathJson.toString());
        CustomLog.d(tag,"startLogUpload() cfgPath: " + SettingData.getInstance().CfgPath);
        CustomLog.d(tag, "startLogUpload() outPath: " + SettingData.getInstance().LogFileOutPath);

        int ret = FileUploadClientJNI.StartLogUploadManager(this.mAccountId, DEVICE,
                NetConnectHelper.getLocalIp(MedicalApplication.shareInstance()),
                getFreePort(),
                SettingData.getInstance().LogUploadConfig.ServerIP,
                SettingData.getInstance().LogUploadConfig.ServerPort,
                SettingData.getInstance().logUploadPath,
                LogPathJson.toString(),
                SettingData.getInstance().CfgPath,
                SettingData.getInstance().LogFileOutPath);
        CustomLog.d(tag, "startLogUpload() ret: " + ret);
    }

    /**
     * 停止日志上传
     */
    public void stopLogUpload() {
        CustomLog.d(tag, "stopLogUpload()");
        FileUploadClientJNI.StopLogUploadManager();
    }

    //需要上传的日志文件路径
    private void initLogUpdatePath() {
        ja = new JSONArray();
        jo1 = new JSONObject();
        jo2 = new JSONObject();
        jo3 = new JSONObject();
        jo4 = new JSONObject();

        jo6 = new JSONObject();
        jo7 = new JSONObject();
        jo8 = new JSONObject();
        jo9 = new JSONObject();
        jo10 = new JSONObject();
        LogPathJson = new JSONObject();
        try {
            jo1.put("path", "/mnt/sdcard/" + MedicalApplication.getContext().getPackageName() + "/main");
            ja.put(jo1);
            jo3.put("path", "/mnt/sdcard/" + MedicalApplication.getContext().getPackageName() + "/auth");
            ja.put(jo3);
            jo4.put("path", "/mnt/sdcard/" + MedicalApplication.getContext().getPackageName() + "/dump");
            ja.put(jo4);
            jo6.put("path","/mnt/sdcard/" + MedicalApplication.getContext().getPackageName() + "/sdkLog");
            ja.put(jo6);
            jo7.put("path","/mnt/sdcard/" + MedicalApplication.getContext().getPackageName() + "/jmeetingsdk");
            ja.put(jo7);
            jo8.put("path","/mnt/sdcard/" + MedicalApplication.getContext().getPackageName() + "/eventlogcat");
            ja.put(jo8);
            jo9.put("path","/mnt/sdcard/" + MedicalApplication.getContext().getPackageName() + "/EventConnect");
            ja.put(jo9);
            jo10.put("path","/mnt/sdcard/" + MedicalApplication.getContext().getPackageName() + "/InternalConnect");
            ja.put(jo10);
            LogPathJson.put("uploadpaths", ja);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    public void unregisterReceiver() {

        context.unregisterReceiver(receiver);

    }

    private void registerReceiver() {
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mFilter.addAction(START_MEETING_BROADCAST);
        mFilter.addAction(END_MEETING_BROADCAST);
        context.registerReceiver(receiver, mFilter);
    }

    /**
     * 获取系统分配的空闲的端口号。
     *
     * @return -1 : 系统分配空闲端口号失败 ; 其他: 系统分配的空闲端口号
     */
    public int getFreePort() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(0);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            CustomLog.e(tag, "系统分配空闲端口号 error");
            return -1;
        }
        int port = serverSocket.getLocalPort();
        try {
            serverSocket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(tag + "  系统分配空闲端口号  Port = " + port);
        return port;
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            System.out.println(tag + "  Receive Broadcast: " + action);
            if (action.equals(END_MEETING_BROADCAST)) {
                startLogUpload();
            } else if (action.equals(START_MEETING_BROADCAST)) {
                stopLogUpload();
            } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                stopLogUpload();
                startLogUpload();
            }
        }
    };
}
