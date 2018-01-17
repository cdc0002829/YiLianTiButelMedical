package cn.redcdn.hvs.udtroom.task;

import android.support.annotation.NonNull;

import org.json.JSONObject;

import cn.redcdn.datacenter.hpucenter.HPUAcceptCsl;
import cn.redcdn.datacenter.hpucenter.HPUCanelCsl;
import cn.redcdn.datacenter.hpucenter.HPUStopCsl;
import cn.redcdn.datacenter.hpucenter.HPUSubmitAdvice;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.log.CustomLog;

/**
 * @author guoyx
 */

public class RemoteTask {
    private static final String TAG = RemoteTask.class.getSimpleName();

    private static RemoteTask mInstance;


    private RemoteTask() {}


    public static RemoteTask getInstance() {
        if (null == mInstance) {
            mInstance = new RemoteTask();
        }

        return mInstance;
    }


    /**
     * 接诊医生, 接受诊疗
     *
     * @param dtID 诊疗室 ID
     * @param taskCallback 操作回调
     */
    public void acceptDT(
        @NonNull String token, @NonNull String dtID, @NonNull final TaskCallback taskCallback) {
        CustomLog.i(TAG, "acceptDT()");

        HPUAcceptCsl hpuAcceptCsl = new HPUAcceptCsl() {
            @Override protected void onSuccess(JSONObject responseContent) {
                super.onSuccess(responseContent);
                taskCallback.onSuccess(responseContent);
                CustomLog.i(TAG, "onSuccess()");
                CustomToast.show(MedicalApplication.shareInstance(), "接受会诊成功",
                    CustomToast.LENGTH_SHORT);

            }


            @Override protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                taskCallback.onFailed(statusCode, statusInfo);
                CustomLog.e(TAG, "onFail()");
                CustomLog.e(TAG, "statusCode = " + statusCode + "statusInfo = " + statusInfo);
                CustomToast.show(MedicalApplication.shareInstance(),
                    "接受会诊失败  " + "statusCode = " + statusCode + "statusInfo = " + statusInfo,
                    CustomToast.LENGTH_SHORT);

            }
        };

        hpuAcceptCsl.accept(token, dtID);
    }


    /**
     * 接诊医生, 终止诊疗
     *
     * @param dtID 诊疗室 ID
     * @param taskCallback 操作回调
     */
    public void finishDT(
        @NonNull String token, @NonNull String dtID, @NonNull final TaskCallback taskCallback) {
        CustomLog.i(TAG, "finishDT()");

        HPUStopCsl hpuStopCsl = new HPUStopCsl() {
            @Override protected void onSuccess(JSONObject responseContent) {
                super.onSuccess(responseContent);
                CustomLog.i(TAG, "onSuccess()");
                CustomToast.show(MedicalApplication.shareInstance(), "结束会诊成功",
                    CustomToast.LENGTH_SHORT);
                taskCallback.onSuccess(responseContent);

            }


            @Override protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                CustomLog.e(TAG, "onFail()，statusCode = " + statusCode + "statusInfo = " + statusInfo);
                CustomToast.show(MedicalApplication.shareInstance(),
                    "结束会诊失败  " + "statusCode = " + statusCode + "statusInfo = " + statusInfo,
                    CustomToast.LENGTH_SHORT);
                taskCallback.onFailed(statusCode, statusInfo);
            }
        };

        hpuStopCsl.stop(token, dtID);
    }


    /**
     * 接诊医生，提交会诊室意见
     *
     * @param dtID 会诊ID
     * @param dtAdvice 会诊意见
     */
    public void submitDTDetails(
        @NonNull String token,
        @NonNull String dtID,
        @NonNull String dtAdvice, @NonNull final TaskCallback callback) {
        CustomLog.i(TAG, "submitDTDetails()");

        HPUSubmitAdvice hpuSubmitAdvice = new HPUSubmitAdvice() {
            @Override protected void onSuccess(JSONObject responseContent) {
                super.onSuccess(responseContent);
                CustomLog.i(TAG, "onSuccess()");
                callback.onSuccess(responseContent);
            }


            @Override protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                CustomLog.e(TAG, "onFail()");
                callback.onFailed(statusCode, statusInfo);
            }
        };

        hpuSubmitAdvice.submit(token, dtID, dtAdvice);
    }


    /**
     * 求诊医生, 撤销会诊
     *
     * @param dtID 会诊 ID
     */
    public void undoDT(
        @NonNull String token, @NonNull String dtID, @NonNull final TaskCallback callback) {
        CustomLog.i(TAG, "undoDT()");

        HPUCanelCsl hpuCanelCsl = new HPUCanelCsl() {
            @Override protected void onSuccess(JSONObject responseContent) {
                super.onSuccess(responseContent);
                CustomLog.i(TAG, "onSuccess()");
                callback.onSuccess(responseContent);
            }


            @Override protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                CustomLog.e(TAG, "onFail()");
                callback.onFailed(statusCode, statusInfo);
            }
        };

        hpuCanelCsl.canel(token, dtID);
    }


    public interface TaskCallback<T> {
        void onSuccess(T data);
        void onFailed(int statusCode, String statusInfo);
    }
}
