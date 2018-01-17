package cn.redcdn.hvs.profiles;

import android.content.Context;
import android.webkit.JavascriptInterface;

/**
 * Designed by guoyx on 2017/9/14.
 */

public class SignUpWebInterface {

    private Context mContext;

    SignUpWebInterface(Context context){
        mContext = context;
    }


    /**
     * 确认支付
     * @param payInfo
     */
    @JavascriptInterface
    public void confirmPayment(String payInfo){

    }


    /**
     * 进入筹备群组
     * @param groupId 组id
     */
    @JavascriptInterface
    public void enterPreparingGroup(String groupId){}


    /**
     * 完成
     * @param result 报名失败还是成功
     */
    @JavascriptInterface
    public void signUpCompleted(int result){}


    /**
     * Activity 标题
     * @param title 标题内容
     */
    @JavascriptInterface
    public void payProgressTitle(String title) {}





}
