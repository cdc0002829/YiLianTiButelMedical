package cn.redcdn.hvs;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.support.multidex.MultiDex;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.redcdn.keyeventwrite.KeyEventFileManager;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cn.redcdn.hvs.accountoperate.activity.LoginActivity;
import cn.redcdn.hvs.appinstall.ForcedInstallActivity;
import cn.redcdn.hvs.appinstall.MeetingVersionManager;
import cn.redcdn.hvs.config.SettingData;
import cn.redcdn.hvs.contacts.contact.butelDataAdapter.ContactSetImp;
import cn.redcdn.hvs.im.fileTask.FileTaskManager;
import cn.redcdn.hvs.im.preference.DaoPreference;
import cn.redcdn.log.CustomLog;
import cn.redcdn.log.LogMonitor;
import cn.redcdn.log.LogcatFileManager;
import cn.redcdn.push.utils.SelectPlatformUtil;

public class MedicalApplication extends Application {
    private final String TAG = getClass().getSimpleName();
    private static Map<String, Activity> destoryMap = new HashMap<>();
    public final String SMS_HVS = "HVS";
    private static MedicalApplication mApplication = null;
    private ArrayList<Activity> activityList = new ArrayList<Activity>();
    private boolean isInit = false;
    private boolean isMeetingRoomRunning = false;
    private boolean isFromMessageLink = false; // 标记是否通过从短信链接过来
    //    private ContactSetImp contactSetImp=null;
    public DisplayImageOptions options;
    public DisplayImageOptions photoOptions;
    private ImageLoaderConfiguration config;
    public static Context context;
    private ContactSetImp contactSetImp = null;

    private static FileTaskManager filetaskManager = null;

    private static DaoPreference mDaoPreference = null;
    private String Approve_inputImagePath = "";


    public void setApprove_inputImagePath(String approve_inputImagePath) {
        Approve_inputImagePath = approve_inputImagePath;
    }


    public String getApprove_inputImagePath() {

        return Approve_inputImagePath;
    }


    // 标示数据库账号
    public static String LOGIN_NUMBER_CHANGE = "-1";


    public static DaoPreference getPreference() {
        return mDaoPreference;
    }


    @Override
    public void onCreate() {
        CustomLog.i(TAG,"onCreate()");

        // Normal app init code..
        MultiDex.install(this);
        super.onCreate();
        context = this;
        imageLoaderConfig();
        displayImageOpt();
        mApplication = this;
        mDaoPreference = DaoPreference.getInstance(context);
//        if (!BuildConfig.DEBUG) {
            CrashReport.initCrashReport(getApplicationContext(), "78f26ee75e", false);
//        }
        SelectPlatformUtil.getPlatform(this,getPackageName());
    }


    /**
     * @param mfileName 文件名字
     * @param write_str 内容
     * @throws IOException
     */
    public static void writeFileSdcardFile(String mfileName, String write_str) throws IOException {
        File file = new File(mfileName);
        File fileParent = file.getParentFile();
        if (!file.exists()) {
            boolean b = fileParent.mkdirs();
        }
        try {
            FileOutputStream fout = new FileOutputStream(mfileName);
            byte[] bytes = write_str.getBytes();
            fout.write(bytes);
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void imageLoaderConfig() {
        config = new ImageLoaderConfiguration.Builder(getApplicationContext())
            .threadPriority(Thread.NORM_PRIORITY - 2)//设置线程的优先级，比ui稍低优先级
            .threadPoolSize(5) //线程池内加载的数量，建议1~5
            //		.denyCacheImageMultipleSizesInMemory()//当同一个Uri获取不同大小的图片，缓存到内存时，只缓存一个。默认会缓存多个不同的大小的相同图片
            .discCacheFileNameGenerator(new Md5FileNameGenerator())//设置缓存文件的名字,通过Md5将url生产文件的唯一名字
            .memoryCache(new LruMemoryCache(5 * 1024 *
                1024)) //可以通过自己的内存缓存实现。LruMemoryCache：缓存只使用强引用. (缓存大小超过指定值时，删除最近最少使用的bitmap) --默认情况下使用
            //		.memoryCacheSize(2 * 1024 * 1024)  // 内存缓存的最大值
            .discCacheFileCount(1000)//缓存文件的最大个数
            .tasksProcessingOrder(QueueProcessingType.FIFO)// 设置图片下载和显示的工作队列排序
            //		.enableLogging() //是否打印日志用于检查错误
            .build();
        ImageLoader.getInstance().init(config);
    }


    private void displayImageOpt() {
        options = new DisplayImageOptions.Builder()
            .showStubImage(R.drawable.doctor_default)//设置图片在下载期间显示的图片
            .showImageForEmptyUri(R.drawable.doctor_default)//设置图片Uri为空或是错误的时候显示的图片
            .showImageOnFail(R.drawable.doctor_default)//设置图片加载/解码过程中错误时候显示的图片
            .cacheInMemory(true)//是否緩存都內存中
            .cacheOnDisc(true)//是否緩存到sd卡上
            .displayer(new RoundedBitmapDisplayer(0))//设置图片的显示方式 : 设置圆角图片  int roundPixels
            .bitmapConfig(Config.RGB_565)//设置为RGB565比起默认的ARGB_8888要节省大量的内存
            .delayBeforeLoading(100)//载入图片前稍做延时可以提高整体滑动的流畅度
            .build();

        photoOptions = new DisplayImageOptions.Builder()
            .showStubImage(R.drawable.chat_empty_img)//设置图片在下载期间显示的图片
            .showImageForEmptyUri(R.drawable.chat_empty_img)//设置图片Uri为空或是错误的时候显示的图片
            .showImageOnFail(R.drawable.chat_empty_img)//设置图片加载/解码过程中错误时候显示的图片
            .cacheInMemory(true)//是否緩存都內存中
            .cacheOnDisc(true)//是否緩存到sd卡上
            .displayer(new RoundedBitmapDisplayer(0))//设置图片的显示方式 : 设置圆角图片  int roundPixels
            .bitmapConfig(Bitmap.Config.RGB_565)//设置为RGB565比起默认的ARGB_8888要节省大量的内存
            .delayBeforeLoading(100)//载入图片前稍做延时可以提高整体滑动的流畅度
            .build();
    }


    public static MedicalApplication shareInstance() {
        return mApplication;
    }


    /**
     * 恢复应用程序
     */
    public void recoverApplication() {
        CustomLog.i(TAG,"recoverApplication()");
        SettingData.getInstance()
            .disposeNpsData(SettingData.getInstance().readNpsConfigFromSharePre());
        AccountManager.getInstance(getApplicationContext()).recoverLoginInfo();
        MeetingVersionManager.getInstance().init(getApplicationContext());
        setInit(true);
    }


    /**
     * 退出程序
     */
    public void exit() {
        CustomLog.i(TAG,"exit()");
        //        MeetingManager.getInstance().release();
        clearTaskStack();
        LogcatFileManager.getInstance().stop();
        LogMonitor.getInstance().release();
        KeyEventFileManager.getInstance().stop();
        System.exit(0);
    }


    public void setInit(boolean flag) {
        isInit = flag;
        CustomLog.i(TAG, "设置启动状态为: " + flag);
    }


    public boolean getInitStatus() {
        CustomLog.i(TAG, "获取启动状态: " + isInit);
        return isInit;

    }


    /**
     * 清除Activity堆栈信息
     */
    public void clearTaskStack() {
        CustomLog.i(TAG,"clearTaskStack()");
        for (Activity activity : activityList) {
            CustomLog.i(TAG,
                "MeetingApplication::clearTaskStack() " + activity.toString());
            activity.finish();
        }
    }


    public void clearExtraActivityAsLogout() {
        for (Activity activity : activityList) {
            if (!activity.getClass().getName().contains(LoginActivity.class.getName())) {
                CustomLog.i(TAG,
                    "MeetingApplication::clearExtraActivityAsLogout() " + activity.toString());
                activity.finish();
            }
        }
    }


    public void insertActivity(Activity activity) {
        if (!activityList.contains(activity)) {
            CustomLog.i(TAG,"insertActivity() | " + activity.getClass().getSimpleName() + " will be insert");
            activityList.add(activity);
        }
    }


    public void deleteActivity(Activity activity) {
        CustomLog.i(TAG,"deleteActivity() | " + activity.getClass().getSimpleName() + " will be destroyed");
        activityList.remove(activity);
    }


    public boolean isFirstRun() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("share",
            MODE_PRIVATE);

        return sharedPreferences.getBoolean("isFirstRun", true);
    }


    /**
     * 设置是否第一次启动
     */
    public void setFirstRun(boolean flag) {
        SharedPreferences sharedPreferences = this.getSharedPreferences("share",
            Context.MODE_PRIVATE);
        Editor edit = sharedPreferences.edit();
        edit.putBoolean("isFirstRun", flag);
        edit.commit();
    }


    public boolean getAutoSpeakSetting() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("setting",
            MODE_PRIVATE);
        return sharedPreferences.getBoolean("autoSpeakSetting", true);
    }

    public boolean getMeetingSetting() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("setting",
                MODE_PRIVATE);
        return sharedPreferences.getBoolean("meetingSetting", true);
    }

    public boolean getWebSetting() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("setting",
            MODE_PRIVATE);
        return sharedPreferences.getBoolean("webSetting", false);

    }


    public boolean getDownloadSetting() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("setting",
            MODE_PRIVATE);
        return sharedPreferences.getBoolean("downloadSetting", false);

    }


    public void startInstallActivity(String changelist) {
        Intent intent = new Intent(this, ForcedInstallActivity.class);
        intent.putExtra("changelist", changelist);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    /**
     * 设置会议室页面是否正在运行
     *
     * @param state true: 会议室页面正在运行; false：会议室页面未运行
     */
    public void setMeetingRoomRunningState(boolean state) {
        CustomLog.i(TAG, "setMeetingRoomRunningState :" + state);
        isMeetingRoomRunning = state;
    }


    /**
     * 获取会议室页面运行状态
     *
     * @return true: 会议室页面正在运行; false：会议室页面未运行
     */
    public boolean getMeetingRoomRunningState() {
        CustomLog.i(TAG, "getMeetingRoomRunningState :" + isMeetingRoomRunning);

        return isMeetingRoomRunning;
    }


    /**
     * 设置是否通过短信链接启动
     */
    public void setIsFromMessageLink(boolean flag) {
        CustomLog.i(TAG, "setIsFromMessageLink:: " + flag);
        isFromMessageLink = flag;
    }


    public static Context getContext() {
        return context;

        //    public ContactSetImp getContactSetImp() {
        //        return contactSetImp;
        //    }
        //
        //    public void setContactSetImp(ContactSetImp contactSetImp) {
        //        this.contactSetImp = contactSetImp;
        //    }
        //
        //    public void removeContactSetImp(){
        //        contactSetImp=null;
        //    }

    }


    /**
     * 获取是否通过短信链接启动
     *
     * @return true: 会议室页面正在运行; false：会议室页面未运行
     */
    public boolean getIsFromMessageLink() {
        return isFromMessageLink;
    }


    public ContactSetImp getContactSetImp() {
        return contactSetImp;
    }


    public void setContactSetImp(ContactSetImp contactSetImp) {
        this.contactSetImp = contactSetImp;
    }


    public void removeContactSetImp() {
        contactSetImp = null;
    }


    public static FileTaskManager getFileTaskManager() {

        if (filetaskManager == null) {
            filetaskManager = new FileTaskManager(context);
        }
        return filetaskManager;
    }


    /**
     * 添加到销毁队列
     *
     * @param activity 要销毁的activity
     */

    public static void addDestoryActivity(Activity activity, String activityName) {
        CustomLog.i("MedicalApplication","addDestoryActivity()");
        destoryMap.put(activityName, activity);
    }


    /**
     * 销毁指定Activity
     */
    public static void destoryActivity(String activityName) {
        CustomLog.i("MedicalApplication","destoryActivity()");
        Set<String> keySet = destoryMap.keySet();
        for (String key : keySet) {
            destoryMap.get(key).finish();
        }
    }
}