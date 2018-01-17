package cn.redcdn.hvs.cdnmanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.select.Evaluator;

import java.io.File;
import java.util.HashMap;

import cn.redcdn.datacenter.cdnuploadimg.CdnGetConfig;
import cn.redcdn.datacenter.cdnuploadimg.CdnGetToken;
import cn.redcdn.datacenter.cdnuploadimg.CdnUploadDataInfo;
import cn.redcdn.datacenter.cdnuploadimg.CdnUploadImage;
import cn.redcdn.datacenter.config.ConstConfig;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.config.SettingData;
import cn.redcdn.log.CustomLog;


public class UploadManager {

    private static UploadManager instance;
    private  String  token;
    private  String  uploadUrl;
    private  String appId = "";
    private  File tmpfile;
    private  CdnGetToken getToken;
    private CdnGetConfig getConfig;
    private CdnUploadImage uploadImage;
    private  GetTokenStaus tokenStaus = GetTokenStaus.GET_TOKEN_NONE;
    private GetConfigStaus configStaus = GetConfigStaus.GET_CONFIG_NONE;
    private  uploadImageStaus uploadStaus;
    private  UploadImageListener listener;
    private  static String SAMLL_PIC_STR = "?cmd=imageprocess/processtype/2/width/";

    private HashMap<CdnUploadImage,UploadImageListener> listenerHasmap = new HashMap();
    private HashMap<CdnGetToken,UploadImageListener> gettokenHasmap = new HashMap();
    private HashMap<CdnGetConfig,UploadImageListener> getConfigHasmap = new HashMap();

    private enum GetTokenStaus{
        GET_TOKEN_NONE ,    //获取token未开始
        GET_TOKEN_ING,      //获取token开始
        GET_TOKEN_SUCCESS,  //获取token成功
        GET_TOKEN_FAILED;   //获取token失败
    }

    private enum GetConfigStaus{
        GET_CONFIG_NONE,    //获取配置未开始
        GET_CONFIG_ING,     //获取配置开始
        GET_CONFIG_SUCCESS, //获取配置成功
        GET_CONFIG_FAILED;  //获取配置失败
    }
    private enum uploadImageStaus{

        UPLOAD_NONE,        //上传图片未调用
        UPLOAD_ING,         //上传图片开始
        UPLOAD_SUCCESS,     //上传图片成功
        UPLOAD_FAILED;      //上传图片失败
    }

    /*
     * 获取实例
     */
    public static synchronized UploadManager getInstance(){
        if (instance == null) {
            instance = new UploadManager();
        }
        return instance;
    }

    private UploadManager(){

    }

    public interface UploadImageListener {
        public void onSuccess(CdnUploadDataInfo dataInfo);
        public void onFailed(int statusCode, String msg);
        public void onProgress(int persent);
    }


    //启动时调用
    public void init(String appid) {
        //TODO 1. 获取token
        //TODO 2. 获取配置信息

        //先从内存中取
        appId = appid;
        ReadSharedPreferences();
        listener = new UploadImageListener() {
            @Override
            public void onSuccess(CdnUploadDataInfo dataInfo) {

            }
            @Override
            public void onFailed(int statusCode, String msg) {

            }
            @Override
            public void onProgress(int persent) {

            }
        };
        if (TextUtils.isEmpty(token)) {
            getToken(appId,listener);
        }else {
            getConfig(token,listener);
        }
    }




    private void ReadSharedPreferences(){

        SharedPreferences.Editor edit = getCdnSharePre().edit();

        token = getCdnSharePre().getString("cdntoken", "");

    }

    private void WriteSharedPreferences(){

        SharedPreferences.Editor edit = getCdnSharePre().edit();
        edit.putString("cdntoken", token);
        edit.commit();


    }

    private SharedPreferences getCdnSharePre() {
        SharedPreferences sharedPreferences = MedicalApplication.shareInstance()
                .getSharedPreferences("CdnToken", Context.MODE_PRIVATE);
        return sharedPreferences;
    }

    public void release(){



    }

    public void cancel(){
        if (tokenStaus == GetTokenStaus.GET_TOKEN_ING) {
            getToken.cancel();
        }
        if (configStaus == GetConfigStaus.GET_CONFIG_ING) {
            getConfig.cancel();
        }
        if (uploadStaus == uploadImageStaus.UPLOAD_ING&&uploadImage!=null) {
            uploadImage.cancel();
        }
    }
    /*
     * 回调方法
     */


    public int uploadImage(File file, final UploadImageListener listener){
        final String Tag = UploadManager.class.getName();
        CustomLog.d(Tag,"uploadImage 被调用");
        tmpfile = file;
        this.listener = listener;
        ReadSharedPreferences();
        uploadStaus = uploadImageStaus.UPLOAD_ING;
        if (configStaus == GetConfigStaus.GET_CONFIG_NONE&&!TextUtils.isEmpty(token)) {
            CustomLog.d(Tag,"GetCongigStaus.GET_CONFIG_NONE&& TOKEN != NULL");
            getConfig(token,listener);

        }else  if (configStaus == GetConfigStaus.GET_CONFIG_NONE&&TextUtils.isEmpty(token)&&tokenStaus!=GetTokenStaus.GET_TOKEN_ING) {
            CustomLog.d(Tag,"GetCongigStaus.GET_CONFIG_NONE&& TOKEN == NULL tokenStaus!=GetTokenStaus.GET_TOKEN_ING");
            if (TextUtils.isEmpty(appId)){
                appId = SettingData.getInstance().CDN_AppId;
            }
            getToken(appId,listener);

        }else  if(configStaus == GetConfigStaus.GET_CONFIG_FAILED&&!TextUtils.isEmpty(token)){

            CustomLog.d(Tag,"configStaus == GetConfigStaus.GET_CONFIG_FAILED&& TOKEN != NULL");
            getConfig(token,listener);

        }else if(configStaus == GetConfigStaus.GET_CONFIG_FAILED&&TextUtils.isEmpty(token)){

            CustomLog.d(Tag,"configStaus == GetConfigStaus.GET_CONFIG_FAILED&&token == null");
            if (TextUtils.isEmpty(appId)){
                appId = SettingData.getInstance().CDN_AppId;
            }
            getToken(appId,listener);


        }else  if (configStaus == GetConfigStaus.GET_CONFIG_SUCCESS&&!TextUtils.isEmpty(uploadUrl)){

            CustomLog.d(Tag,"configStaus == GetConfigStaus.GET_CONFIG_SUCCESS&&upLoadUrl != null");
            upload(listener);


        }else  if (configStaus == GetConfigStaus.GET_CONFIG_SUCCESS&&TextUtils.isEmpty(uploadUrl)){

            CustomLog.d(Tag,"configStaus == GetConfigStaus.GET_CONFIG_SUCCESS&&upLoadUrl == null");
            getConfig(token,listener);

        }
        return 0;
    }
    private void getToken(String appid, final UploadImageListener listen){

        tokenStaus = GetTokenStaus.GET_TOKEN_ING;

        CdnGetToken mtoken = new CdnGetToken() {
            @Override
            protected void onSuccess(JSONObject responseContent) {
                try {
                    tokenStaus = GetTokenStaus.GET_TOKEN_SUCCESS;
                    if (responseContent != null){
                        token = responseContent.getString("token");
                        WriteSharedPreferences();
                        if (configStaus == GetConfigStaus.GET_CONFIG_NONE||configStaus == GetConfigStaus.GET_CONFIG_FAILED) {
                            getConfig(token,gettokenHasmap.get(this));
                        }
                    }else{
                        tokenStaus = GetTokenStaus.GET_TOKEN_FAILED;
                        gettokenHasmap.get(this).onFailed(-1,"获取token失败!"+"返回结果为null");
                    }
                } catch (JSONException e) {

                    e.printStackTrace();
                }
                super.onSuccess(responseContent);
            }
            @Override
            protected void onFail(int statusCode, String statusInfo) {
                tokenStaus = GetTokenStaus.GET_TOKEN_FAILED;
                gettokenHasmap.get(this).onFailed(statusCode,"获取token失败!"+statusInfo);
//                listener.onFailed(statusCode,"获取token失败!"+statusInfo);
                super.onFail(statusCode, statusInfo);
            }
        };
        gettokenHasmap.put(mtoken,listen);
        mtoken.getToken(appid);

    }


    private void getConfig(String token, final UploadImageListener listen){
        final String Tag = UploadManager.class.getName();
        configStaus = GetConfigStaus.GET_CONFIG_ING;
        CdnGetConfig mConfig = new CdnGetConfig() {
            @Override
            protected void onSuccess(JSONObject responseContent) {
                try {
                    configStaus = GetConfigStaus.GET_CONFIG_SUCCESS;
                    if (responseContent!=null && responseContent.has("fileuploadurl")) {
                        uploadUrl = responseContent.getString("fileuploadurl");
                        ConstConfig.CDN_VIDEO_URL = responseContent.optString("fileprocessurl");
                        if (uploadStaus == uploadImageStaus.UPLOAD_ING) {
                            upload(listen);
                        }
                    }else{
                        //返回数据为空
                        configStaus = GetConfigStaus.GET_CONFIG_NONE;
                        if (TextUtils.isEmpty(appId)){
                            appId = SettingData.getInstance().CDN_AppId;
                        }
                        getToken(appId,listen);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                super.onSuccess(responseContent);
            }
            @Override
            protected void onFail(int statusCode, String statusInfo) {
                configStaus = GetConfigStaus.GET_CONFIG_FAILED;
                if (statusCode == -998999)
                {
                    CustomLog.d(Tag,"statusCode = -998999 msg 在获取config时token失效，重新获取token");
                    if (TextUtils.isEmpty(appId)){
                        appId = SettingData.getInstance().CDN_AppId;
                    }
                    getToken(appId,getConfigHasmap.get(this));
                }else {
                    getConfigHasmap.get(this).onFailed(statusCode, "获取config失败!" + statusInfo);
//                    listen.onFailed(statusCode, "获取config失败!" + statusInfo);
                }
                super.onFail(statusCode, statusInfo);
            }
        };
        getConfigHasmap.put(mConfig,listen);
        mConfig.GetConfig(token, false);
    }

    private void upload(final UploadImageListener listen){
        final String Tag = UploadManager.class.getName();

        CdnUploadImage upload = new CdnUploadImage() {
            @Override
            protected void onSuccess(CdnUploadDataInfo responseContent) {
                listenerHasmap.get(this).onSuccess(responseContent);

//                listener.onSuccess(responseContent);
                super.onSuccess(responseContent);
            }

            @Override
            protected void onUploadProgress(int percent) {
                super.onUploadProgress(percent);
                listenerHasmap.get(this).onProgress(percent);
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                if (statusCode == -41002)
                {
                    CustomLog.d(Tag,"statusCode = -41002 msg 在上传图片时token失效，重新获取token");
                    configStaus = GetConfigStaus.GET_CONFIG_NONE;
                    if (TextUtils.isEmpty(appId)){
                        appId = SettingData.getInstance().CDN_AppId;
                    }
                    getToken(appId,listenerHasmap.get(this));
                }else {
                    listenerHasmap.get(this).onFailed(statusCode, "upload失败!" + statusInfo);
                }
                super.onFail(statusCode, statusInfo);
            }
        };
        uploadImage = upload;
        listenerHasmap.put(upload,listen);
        upload.uploadImage(tmpfile, token, "", uploadUrl);
    }

    public String getSmallPic(String picurl,int width,int height){
        String sPicUrl = "";
        if(picurl.isEmpty()){
            return sPicUrl;
        }
        sPicUrl = picurl + SAMLL_PIC_STR + width+"/height/"+height;
        CustomLog.d("uploadManager","smallUrl = "+ sPicUrl);
        return  sPicUrl;
    }

}
