package cn.redcdn.hvs.im.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.ViewGroup;
import cn.redcdn.hvs.im.work.MessageReceiveAsyncTask;
import cn.redcdn.hvs.im.work.MessageReceiveAsyncTask.MessageReceiverListener;
import cn.redcdn.hvs.util.TitleBar;
import com.butel.connectevent.component.CommonDialog;
import com.butel.connectevent.utils.LogUtil;
import com.umeng.analytics.MobclickAgent;
/**
 * Created by guoyx on 2017/2/28.
 */
public class BaseFragmentActivity extends FragmentActivity {

    private MyBroadCast instance;
    private CommonDialog dialog;

    private TitleBar titleBar = null;

    // 是否异地登陆弹出对话框 true弹出；false 不弹出
    // TODO:因当前广播的监听在onCreate,注销监听在onDestroy
    // 在页面切换因为时机巧合，会出现两次谈对话框的现象
    // 此处暂用变量控制，理想的做法是将注销监听放在onStop;
    // 待讨论后再做处理
    private boolean isForceLogin = true;

    private IntentFilter filter = null;

    private MessageReceiverListener listener = null;

    public void setReceiverListener(MessageReceiveAsyncTask.MessageReceiverListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        MobclickAgent.updateOnlineConfig(this);

        // instance = new MyBroadCast();
        // filter = new IntentFilter();
        // filter.addAction(CallManageConstant.OFFSITE_KICK);
        // filter.addAction(CallManageConstant.OFFSITE_LOGIN);
        // filter.addAction(CallManageConstant.USER_CHANGE_ACTION);
        // filter.addAction(BizConstant.TOKEN_INVALID_LOGIN_ACTION);
        // filter.addAction(BizConstant.APK_DOWNLOAD_FAIL_IO);
        // filter.addAction(BizConstant.APK_DOWNLOAD_FAIL_NET);
        // filter.addAction(BizConstant.APK_DOWNLOAD_SUCC);
        // filter.addAction(BizConstant.APK_ULTRA_STORAGE_SPACE);
        // filter.addAction(BizConstant.CANCEL_DOWNLOAD_BY_CALL);

        // 清空dialog栈
        CommonDialog.clearStkByActivityName(getLocalClassName());
    }

    public TitleBar getTitleBar() {
        if (titleBar == null) {
            titleBar = new TitleBar(this,
                ((ViewGroup) findViewById(android.R.id.content))
                    .getChildAt(0));
        }
        return titleBar;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 统计时长
        MobclickAgent.onPause(this);

        if (instance != null) {
            this.unregisterReceiver(instance);
        }

        if (listener != null) {
            listener.onFinished();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 统计时长
        MobclickAgent.onResume(this);

        if(instance!=null && filter!=null){
            this.registerReceiver(instance, filter);
        }

        isForceLogin = true;

        // 0013942:
        // String offsite = NetPhoneApplication.getPreference().getKeyValue(PrefType.KEY_OFFSITE_TIP, "");
        // if("true".equals(offsite)){
        //     LogUtil.d("onResume 账号在异地登录，您已被迫下线，是否重新登录");
        //     tipLogin(getString(R.string.dialog_offsite_kick));
        // }
    }

    @Override
    protected void onStop() {
        super.onStop();
        isForceLogin = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        instance = null;

        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }

    private class MyBroadCast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            LogUtil.d("接收到广播统一号码登录：" + action);
            // if (CallManageConstant.OFFSITE_LOGIN.equals(action)) {
            //     removeStickyBroadcast(intent);
            //     // 尝试登录失败时 弹出对话框，提示是否强制登陆
            //     if (!isFinishing()) {
            //         LogUtil.d(" 您的账号已在异地登录，是否重新登录");
            //         tipLogin(getString(R.string.dialog_offsite_login));
            //         NetPhoneApplication.getPreference().setKeyValue(PrefType.KEY_OFFSITE_TIP, "true");
            //     }
            //
            // } else if (CallManageConstant.OFFSITE_KICK.equals(action)) {
            //     removeStickyBroadcast(intent);
            //     // 异地登录被踢出时 弹出对话框
            //     if (!isFinishing()) {
            //         LogUtil.d("账号在异地登录，您已被迫下线，是否重新登录");
            //         tipLogin(getString(R.string.dialog_offsite_kick));
            //         NetPhoneApplication.getPreference().setKeyValue(PrefType.KEY_OFFSITE_TIP, "true");
            //     }
            // } else if (CallManageConstant.USER_CHANGE_ACTION.equals(action)) {
            //     NetPhoneApplication.getPreference().setKeyValue(PrefType.KEY_OFFSITE_TIP, "false");
            //     if (dialog != null && dialog.isShowing()) {
            //         dialog.dismiss();
            //         dialog = null;
            //     }
            // } else if(BizConstant.TOKEN_INVALID_LOGIN_ACTION.equals(action)){
            //     NetPhoneApplication.getPreference().setKeyValue(PrefType.KEY_OFFSITE_TIP, "false");
            //     if (dialog != null && dialog.isShowing()) {
            //         dialog.dismiss();
            //         dialog = null;
            //     }
            //     CommonUtil.showToast(getString(R.string.token_invalid_login_action));
            //     stopServiceAndLogin();
            // } else if(BizConstant.APK_DOWNLOAD_FAIL_IO.equals(action)){
            //     String force = NetPhoneApplication.getPreference()
            //         .getKeyValue(PrefType.FORCE_UPGRADE,CheckNewVersionManager.UN_FORCE_PROGRADE);
            //     //            	if(CheckNewVersionManager.FORCE_PROGRADE.equals(force)){
            //     String txt = getString(R.string.apk_download_fail_io);
            //     CheckNewVersionManager.downAPKFailDialog(txt, force, BaseFragmentActivity.this);
            //     LogUtil.d("弹出 下载失败IO 对话框");
            //     //            	}else{
            //     //            		NotificationUtil.sendNotifacation("软件下载失败", "写文件失败，下载失败，请到‘我’中重新尝试下载", 0, null);
            //     //            		LogUtil.d("弹出 下载失败IO 非强制升级 通知栏");
            //     //            	}
            // } else if(BizConstant.APK_DOWNLOAD_FAIL_NET.equals(action)){
            //     String force = NetPhoneApplication.getPreference()
            //         .getKeyValue(PrefType.FORCE_UPGRADE,CheckNewVersionManager.UN_FORCE_PROGRADE);
            //     //            	if(CheckNewVersionManager.FORCE_PROGRADE.equals(force)){
            //     String txt = getString(R.string.update_version_internet_exception);
            //     CheckNewVersionManager.downAPKFailDialog(txt, force, BaseFragmentActivity.this);
            //     LogUtil.d("弹出 下载失败NET 对话框");
            //     //            	}else{
            //     //            		NotificationUtil.sendNotifacation("软件下载失败", "网络连接异常，请检查网络后重新下载", 0, null);
            //     //            		LogUtil.d("弹出 下载失败NET 非强制升级 通知栏");
            //     //            	}
            // } else if(BizConstant.APK_DOWNLOAD_SUCC.equals(action)){
            //     // do nothing
            //     LogUtil.d("下载完成并SHA1校验成功");
            // } else if(BizConstant.APK_ULTRA_STORAGE_SPACE.equals(action)){
            //     String txt = getString(R.string.update_version_no_space);
            //     CheckNewVersionManager.downAPKUltraSpaceDialog(txt,BaseFragmentActivity.this);
            //     LogUtil.d("弹出 APK下载空间不足 对话框");
            // } else if(BizConstant.CANCEL_DOWNLOAD_BY_CALL.equals(action)){
            //     LogUtil.d("取消下载");
            //     ApkUpdateManager apkupdateManager = ApkUpdateManager.getInstance(BaseFragmentActivity.this);
            //     if(apkupdateManager!=null){
            //         apkupdateManager.cancelDownloadApk();
            //     }
            // }
        }
    }

    /**
     * @author: lihs
     * @Title: tipLogin
     * @Description: 强制登陆
     * @date: 2013-9-11 下午6:25:31
     */
    // private void tipLogin(String text_context) {
    //
    //     if (!isForceLogin) {
    //         return;
    //     }
    //     dialog = new CommonDialog(this, getLocalClassName(),
    //         CommonDialog.DIALOG_TYPE_FORCE_LOGIN);
    //     dialog.setCancelable(false);
    //     //dialog.setTitle("提醒");
    //     dialog.setMessage(text_context);
    //     dialog.setCancleButton(new CommonDialog.BtnClickedListener() {
    //         @Override
    //         public void onBtnClicked() {
    //             if (CommonUtil.isFastDoubleClick()){
    //                 return;
    //             }
    //             stopServiceAndLogin();
    //             dialog.dismiss();
    //             dialog = null;
    //             NetPhoneApplication.getPreference().setKeyValue(PrefType.KEY_OFFSITE_TIP, "false");
    //         }
    //     }, getString(R.string.login_tip_force_cancel));
    //     dialog.setPositiveButton(new CommonDialog.BtnClickedListener() {
    //         @Override
    //         public void onBtnClicked() {
    //             if (CommonUtil.isFastDoubleClick()){
    //                 return;
    //             }
    //             dialog.dismiss();
    //             dialog = null;
    //             // 发广播强制登陆广播
    //             Intent intent = new Intent();
    //             intent.setAction(CallManageConstant.FORCE_LOGIN_SIP_ACTION);
    //             intent.putExtra(CallManageConstant.KEY_BROADCAST_INTENT_DATA,
    //                 true);
    //             NetPhoneApplication.getPreference().setKeyValue(PrefType.KEY_OFFSITE_TIP, "false");
    //             sendBroadcast(intent);
    //         }
    //     }, getString(R.string.login_tip_force_positive));
    //     if (dialog != null && dialog.isShowing()) {
    //
    //     } else {
    //         if (!isFinishing()) {
    //             dialog.showDialog();
    //         }
    //     }
    // }

    // private void stopServiceAndLogin(){
    //     // 退出应用
    //     LogUtil.d("注销登录发送停掉sip结束广播");
    //     /*
    //      * niuben modify
    //      * CallManageService在onDestroy有30S定时启动发送广播无法停止。
    //      * 走注销流程（SettingFragement点击注销时流程一致）。
    //      */
    //     //发送退出广播，避免在退出过程中还有信号来临造成的问题。
    //     //Intent broadcast_exit = new Intent(
    //     //		CallManageConstant.ACTION_EXIT_APP_ING);
    //     //sendBroadcast(broadcast_exit);
    //     LogUtil.d("点击知道了，退出登陆或token失效自动获取失败");
    //     NetPhoneApplication.logOff();
    //     skipToLoginActivity();
    // }

    // protected void skipToLoginActivity(){
    //     // TODO:先跳到MainFragmentActivity,再在onNewIntent方法中跳到LoginActivity
    //     Intent login_intent = new Intent();
    //     login_intent.setClass(BaseFragmentActivity.this,
    //         MainFragmentActivity.class);
    //     login_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    //     login_intent.putExtra(
    //         MainFragmentActivity.FLAG_SKIP_LOGOACTIVITY, true);
    //     startActivity(login_intent);
    // }
}
