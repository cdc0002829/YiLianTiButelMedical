package cn.redcdn.hvs.im.task;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.Toast;

import com.butel.connectevent.utils.NetWorkUtil;

import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.im.UrlConstant;
import cn.redcdn.hvs.im.common.CommonWaitDialog;
import cn.redcdn.hvs.im.util.xutils.http.client.RequestParams;

/**
 * Desc  通用接口调用
 * Created by wangkai on 2017/2/24.
 */

public class AsyncTasks {

    public static final String TIMEOUT = "timeout";
    public static final String UNKNOWHOST = "unknowhost";
    public static final String INTERNET_INTERRUPT = "internet_interrupt";

    // 是否提示错误信息
    private boolean isShowAlertMsg = true;

    public boolean isShowAlertMsg() {
        return isShowAlertMsg;
    }

    public void setShowAlertMsg(boolean isShowAlertMsg) {
        this.isShowAlertMsg = isShowAlertMsg;
    }

    private Activity activity;

    private boolean isPopWaitingDialog = false;

    private boolean isAlertInternet = false;

    public void setAlertInternet(boolean isAlertInternet) {
        this.isAlertInternet = isAlertInternet;
    }

    public boolean isPopWaitingDialog() {
        return isPopWaitingDialog;
    }

    public void setPopWaitingDialog(boolean isPopWaitingDialog) {
        this.isPopWaitingDialog = isPopWaitingDialog;
    }

    private CommonWaitDialog dialog = null;
    private String url = null;
    private RequestParams params = null;
    private String message = null;
    private String misTokenCode = "";
    // 标记当前正在访问nps,
    // 若访问nps失败时会判断需要再次使用IP地址访问；
    private boolean npsAccessing=false;

    public AsyncTasks(String url, RequestParams params, String message,
                      Activity activity, boolean isAlertInternet,
                      boolean isPopWaitingDialog,String misTokenCode) {

        this.activity = activity;
        this.isAlertInternet = isAlertInternet;
        this.isPopWaitingDialog = isPopWaitingDialog;
        this.url = url;
        this.message = message;
        this.params = params;
        this.misTokenCode = misTokenCode;

//        if (this.url.endsWith("getServiceParameters")) {
//            String accessDevnet = NetPhoneApplication.getPreference()
//                    .getKeyValue(PrefType.ACCESS_DEVNET,"");
//            if (TextUtils.isEmpty(accessDevnet)
//                    ||CommonConstant.ACCESS_NET.equals(accessDevnet)
//                    ||CommonConstant.ACCESS_DEV_NET.equals(accessDevnet)
//                    ||CommonConstant.ACCESS_DEV_TEMP_NET.equals(accessDevnet)) {
//                npsAccessing = true;
//                this.url = UrlConstant.NPS_ADDRESS_DNS + UrlConstant.NPS_INTERFACE_URL;
//            } else {
//                this.url = accessDevnet + UrlConstant.NPS_INTERFACE_URL;
//            }
//            LogUtil.d("nps : "+this.url);
//        }
    }

    public AsyncTasks(String url, RequestParams params, String message,
                      Activity activity, boolean isAlertInternet,
                      boolean isPopWaitingDialog) {

        this.activity = activity;
        this.isAlertInternet = isAlertInternet;
        this.isPopWaitingDialog = isPopWaitingDialog;
        this.url = url;
        this.message = message;
        this.params = params;
        this.misTokenCode = "";

//        if (this.url.endsWith("getServiceParameters")) {
//            String accessDevnet = NetPhoneApplication.getPreference()
//                    .getKeyValue(PrefType.ACCESS_DEVNET,"");
//            if (TextUtils.isEmpty(accessDevnet)
//                    ||CommonConstant.ACCESS_NET.equals(accessDevnet)
//                    ||CommonConstant.ACCESS_DEV_NET.equals(accessDevnet)
//                    ||CommonConstant.ACCESS_DEV_TEMP_NET.equals(accessDevnet)) {
//                npsAccessing = true;
//                this.url = UrlConstant.NPS_ADDRESS_DNS + UrlConstant.NPS_INTERFACE_URL;
//            } else {
//                LogUtil.d("使用友盟配置的地址进行访问:" + accessDevnet);
//                this.url = accessDevnet + UrlConstant.NPS_INTERFACE_URL;
//            }
//            LogUtil.d("nps : "+this.url);
//        }
    }


    public boolean exeuteTask() {
        return getReslut(url, params, message);
    }

    public void onDestroy() {
        if (dialog != null && dialog.isShowing()) {
            dialog.clearAnimation();
            dialog = null;
        }
    }

    public void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.clearAnimation();
            dialog = null;
        }
    }

    private boolean isDnsAccessNps(){
        if(this.url.equals(UrlConstant.NPS_ADDRESS_DNS + UrlConstant.NPS_INTERFACE_URL)){
            return true;
        }
        return false;
    }

    /**
     * @author: lihs
     * @Title: getReslut
     * @Description:
     * @return
     * @date: 2013-8-10 下午5:05:01
     */
    private boolean getReslut(String httpurl, RequestParams params,
                              final String message) {

//        if (isAlertInternet) {
//            if (!NetWorkUtil.isNetworkConnected(MedicalApplication
//                    .getContext())) {
//                NetWorkUtil.networkError(activity);
//                return false;
//            }
//        }

//        if (isPopWaitingDialog) {
//            if (dialog == null) {
//                if (activity != null) {
//                    if (!((Activity) activity).isFinishing()) {
//                        dialog = new CommonWaitDialog(activity,message);
//                        dialog.startAnimation();
//                    }
//                }
//            }
//        }
//
//        HttpUtils http = new HttpUtils();
//        http.configTimeout(30 * 1000);
//        http.send(HttpMethod.POST, httpurl, params, new RequestCallBack<Object>() {
//
//            @Override
//            public void onStart() {
//                super.onStart();
//            }
//
//            @Override
//            public void onSuccess(Object result) {
//                super.onSuccess(result);
//
//                if(npsAccessing){
//                    String temp = result + "";
//                    if(TextUtils.isEmpty(temp) && isDnsAccessNps()){
//                        LogUtil.d("nps ip retry 1 使用IP地址进行访问");
//                        url = UrlConstant.NPS_ADDRESS_IP + UrlConstant.NPS_INTERFACE_URL;
//                        exeuteTask();
//                        npsAccessing = false;
//                        return;
//                    }else{
//                        try {
//                            JSONObject json = new JSONObject(temp);
//                            if (!CommonConstant.SUCCESS_RESLUT
//                                    .equals(json.optString("status"))
//                                    && isDnsAccessNps()) {
//                                LogUtil.d("nps ip retry 2 使用IP地址进行访问");
//                                url = UrlConstant.NPS_ADDRESS_IP
//                                        + UrlConstant.NPS_INTERFACE_URL;
//                                exeuteTask();
//                                npsAccessing = false;
//                                return;
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//
//
//                if (dialog != null && activity != null) {
//                    dialog.clearAnimation();
//                }
//                if (listenerResult != null) {
//                    listenerResult.getResluts(result + "");
//                }
//            }
//
//            @Override
//            public void onFailure(Throwable error, String msg) {
//                super.onFailure(error, msg);
//
//                if(npsAccessing && isDnsAccessNps()){
//                    LogUtil.d("nps ip retry 3 使用IP地址进行访问");
//                    url = UrlConstant.NPS_ADDRESS_IP + UrlConstant.NPS_INTERFACE_URL;
//                    exeuteTask();
//                    npsAccessing = false;
//                    return;
//                }
//
//                if (dialog != null) {
//                    dialog.clearAnimation();
//                }
//                boolean alerted = false;
//                if (error instanceof ConnectTimeoutException
//                        || error instanceof SocketTimeoutException
//                        || error instanceof SocketException) {
//                    if (!AndroidUtil.getString(R.string.login_msg).equals(message)) {
//                        if (isShowAlertMsg) {
//                            alertMassage(R.string.network_timeout);
//                            alerted = true;
//                        }
//
//                    } else {
//                        msg = TIMEOUT;
//                    }
//
//                } else if (error instanceof UnknownHostException) {
//                    if (!AndroidUtil.getString(R.string.login_msg).equals(message)) {
//                        if (isShowAlertMsg) {
//                            alertMassage(R.string.network_unknown_host);
//                            alerted = true;
//                        }
//                    } else {
//                        msg = UNKNOWHOST;
//                    }
//                } else if (error instanceof ClientProtocolException) {
//                    if (!AndroidUtil.getString(R.string.login_msg).equals(message)) {
//                        if (isShowAlertMsg) {
//                            alertMassage(R.string.interface_failure);
//                            alerted = true;
//                        }
//                    } else {
//                        msg = INTERNET_INTERRUPT;
//                    }
//                }
//
//                if (listenerFaliureResult != null) {
//                    listenerFaliureResult.getResluts(msg,alerted);
//                }
//            }
//        }, this.misTokenCode);
        return true;
    }

    private ListenerResult listenerResult;

    public interface ListenerResult {
        public void getResluts(String reslut);
    }

    private ListenerFaliureResult listenerFaliureResult;

    public interface ListenerFaliureResult {
        public void getResluts(String msg, boolean alerted);
    }

    public void setListenerFaliureResult(ListenerFaliureResult listenerResult) {
        this.listenerFaliureResult = listenerResult;
    }

    public void setListenerResult(ListenerResult listenerResult) {
        this.listenerResult = listenerResult;
    }

    private void alertMassage(int msgRes) {
        if (activity != null) {
            Toast.makeText(activity, msgRes, Toast.LENGTH_LONG).show();
        }
    }
}
