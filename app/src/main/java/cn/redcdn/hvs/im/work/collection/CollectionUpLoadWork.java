package cn.redcdn.hvs.im.work.collection;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.butel.connectevent.utils.NetWorkUtil;

import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.im.IMConstant;
import cn.redcdn.hvs.im.UrlConstant;
import cn.redcdn.hvs.im.bean.CollectionEntity;
import cn.redcdn.hvs.im.fileTask.FileTaskManager;
import cn.redcdn.hvs.im.util.xutils.http.SyncResult;
import cn.redcdn.hvs.im.util.xutils.http.client.RequestParams;
import cn.redcdn.hvs.im.util.xutils.http.client.multipart.MultipartEntity;
import cn.redcdn.hvs.im.util.xutils.http.client.multipart.content.ContentBody;
import cn.redcdn.hvs.im.util.xutils.http.client.multipart.content.FileBody;
import cn.redcdn.hvs.im.util.xutils.http.client.HttpRequest.HttpMethod;
import cn.redcdn.log.CustomLog;
import cn.redcdn.hvs.im.preference.DaoPreference.PrefType;

/**
 * <dl>
 * <dt>CollectionUpLoadWork.java</dt>
 * <dd>Description:上传 收藏数据线程</dd>
 * <dd>Copyright: Copyright (C) 2011</dd>
 * <dd>Company: 北京红云融通技术有限公司</dd>
 * <dd>CreateDate: 2016-5-13 下午2:44:34</dd>
 * </dl>
 *
 * @author 牛犇
 */

public class CollectionUpLoadWork extends CollectionBaseLoadWork{

    private final String TAG = "CollectionUpLoadWork";

    protected CollectionEntity mDataEntity;

    public CollectionUpLoadWork(Context context,
                                CollectionLoadCallBack callBack, CollectionEntity entity) {
        super(context, callBack);
        mDataEntity = entity;
    }

    @Override
    public void syncLoadCollection() {
        if (CollectionEntity.STATUS_EFFECTIVE == mDataEntity.getStatus()) {
            syncAddCollection();
            return;
        }

        if (CollectionEntity.STATUS_INVALID == mDataEntity.getStatus()) {
            syncDeleteCollection();
            return;
        }
    }

    /*--------------------------------------------------------------------添加收藏数据时，先上传本地数据   开始-----------------------------------------------------------------------------------*/

    /**
     * 上传body里的附件，全部上传成功才算成功
     *
     * @return
     */
    private boolean DoUpLoadFile() {
        CustomLog.d(TAG,"mDataEntity.getBody()=" + mDataEntity.getBody()
                + "mDataEntity.getID()=" + mDataEntity.getId());
        if (TextUtils.isEmpty(mDataEntity.getBody())) {
            return false;
        }
        boolean success = true;
        try {
            JSONArray bodyArray = new JSONArray(mDataEntity.getBody());
            JSONArray upDataBodyArray = new JSONArray();
            if (bodyArray != null && bodyArray.length() > 0) {
                for (int i = 0; i < bodyArray.length(); i++) {
                    JSONObject object = bodyArray.getJSONObject(i);

                    if (object != null &&
                            (mDataEntity.getType() == FileTaskManager.NOTICE_TYPE_FILE
                                    || mDataEntity.getType() == FileTaskManager.NOTICE_TYPE_PHOTO_SEND
                                    || mDataEntity.getType() == FileTaskManager.NOTICE_TYPE_VEDIO_SEND
                                    || mDataEntity.getType() == FileTaskManager.NOTICE_TYPE_AUDIO_SEND)) {
                        // 需要上传文件

                        String remoteUrl = object.optString("remoteUrl");
                        if (TextUtils.isEmpty(remoteUrl)) {
                            // 还未上传文件，先上传文件
                            String local = object.optString("localUrl");
                            Map<String, String> resultUrls = uploadFile(local);
                            if (resultUrls == null) {
                                CustomLog.d(TAG,"上传文件失败");
                                success = false;
                            } else {
                                CustomLog.d(TAG,"上传文件成功");
                                object.put("remoteUrl", resultUrls.get("remoteUrl"));
                                object.put("thumbnailRemoteUrl", resultUrls.get("thumbUrl"));
                            }
                        } else {
                            CustomLog.d(TAG,"文件已上传，无需再次上传");
                            // 已上传文件
                        }
                    }
                    upDataBodyArray.put(object);
                }
                mDataEntity.setBody(upDataBodyArray.toString());
                mCollectionDao.upDateBodyById(mDataEntity.getId(),
                        upDataBodyArray.toString());
            }
        } catch (JSONException e) {
            CustomLog.e(TAG,"JSONException" + e.toString());
        }
        return success;
    }

    /**
     * 上传本地文件
     *
     * @param filePath
     * @return
     */
    private Map<String, String> uploadFile(String filePath) {
        Map<String, String> resultUrlMap = new HashMap<String, String>();
        CustomLog.d(TAG,"filePath=" + filePath);
        if (!NetWorkUtil.isNetworkConnected(MedicalApplication.getContext())) {
            CustomLog.d(TAG,"网络连接不可用");
            return null;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            CustomLog.d(TAG,"本地文件不存在");
            // 本地文件不存在，将数据更新为不需要同步状态（无需再次上传同步了）
            mCollectionDao.upDateSyncStatusById(
                    mDataEntity.getId(), CollectionEntity.SYNCSTATUS_UNEED);
            return null;
        }

        if (isFinish) {
            CustomLog.d(TAG,"任务已取消，不上传文件");
            return null;
        }

        String postResult = postFile(file,
                UrlConstant.getCommUrl(PrefType.FILE_UPLOAD_SERVER_URL));

        CustomLog.d(TAG,"upload  file result:" + postResult);
        if (TextUtils.isEmpty(postResult)) {
            return null;
        }

        try {
            JSONObject resp = new JSONObject(postResult);

            String okStr = resp.optString("ok");

            if ("1".equals(okStr)) {
                // 图片的原图
                String remoteUrl = resp.optString("originalImagePath");
                if (TextUtils.isEmpty(remoteUrl)) {
                    // 视频的原文件
                    remoteUrl = resp.optString("originalFilePath");
                }
                resultUrlMap.put("remoteUrl", remoteUrl);

                // 图片的缩略图
                String thumbUrl = resp.optString("littleImagePath");
                if (TextUtils.isEmpty(thumbUrl)) {
                    // 视频的缩略图
                    thumbUrl = resp.optString("picFilePath");
                }

                resultUrlMap.put("thumbUrl", thumbUrl);

                return resultUrlMap;
            } else {
                return null;
            }
        } catch (JSONException e) {
            CustomLog.e(TAG,"JSONException" + e.toString());
            return null;
        }
    }

    /**
     * @Title: postFile
     * @Description: 上传文件
     * @param file
     * @param urlServer
     */
    private String postFile(File file, String urlServer) {
        String result = null;
        HttpClient httpclient = new DefaultHttpClient();
        // 设置通信协议版本
        httpclient.getParams().setParameter(
                CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        HttpPost httppost = new HttpPost(urlServer);
        MultipartEntity mpEntity = new MultipartEntity();
        ContentBody cbFile = new FileBody(file);
        mpEntity.addPart("logZipFile", cbFile);
        httppost.setEntity(mpEntity);
        try {
            HttpResponse response = httpclient.execute(httppost);
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = EntityUtils.toString(resEntity, "utf-8");
                    resEntity.consumeContent();
                }
            }
        } catch (ClientProtocolException e) {
            CustomLog.e("upload file error", e.toString());
        } catch (IOException e) {
            CustomLog.e("upload file error", e.toString());
        } catch (Exception e) {
            CustomLog.e("upload file error", e.toString());
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
        return result;
    }

    /*--------------------------------------------------------------------添加收藏数据时，先上传本地数据   结束-----------------------------------------------------------------------------------*/

    /*--------------------------------------------------------------------添加收藏数据 开始-----------------------------------------------------------------------------------*/

    private void syncAddCollection() {
        CustomLog.d(TAG,"syncAddCollection begin collectId:" + mDataEntity.getId()
                + " body:" + mDataEntity.getBody());
        if (isFinish) {
            CustomLog.d(TAG,"任务已经被取消，不再调用接口");
            return;
        }

        if (!DoUpLoadFile()) {
            CustomLog.d(TAG,"有附件，且附件上传失败，等待下次同步");
            return;
        }

        if (isFinish) {
            CustomLog.d(TAG,"该任务已经被取消的场合，不做处理");
            return;
        }

        SyncResult syncResult = addItem();

        if (isFinish) {
            CustomLog.d(TAG,"接口返回，该任务已经被取消的场合，不做处理");
            return;
        }

        if (syncResult != null && syncResult.isOK()) {
            CustomLog.d(TAG,"接口调用成功，同步到本地操作");
            parseAddSyncResult(syncResult.getResult());
        } else {
            CustomLog.d(TAG,"接口调用失败");
        }
        CustomLog.d(TAG,"syncAddCollection end collectId:" + mDataEntity.getId());

    }

    private void parseAddSyncResult(String result) {
        CustomLog.d(TAG,"parseAddSyncResult begin");
        try {
            JSONObject object = new JSONObject(result);
            String status = object.optString("status");
            CustomLog.d(TAG,"status=" + status);
            if ("0".equals(status)) {
                // 添加收藏成功后，不更新本地时间为服务端时间
//                mCollectionDao.upDateDateAfterSyncSuccessById(
//                        mDataEntity.getId(), CollectionEntity.SYNCSTATUS_UNEED,
//                        object.optLong("time"));
                mCollectionDao.upDateSyncStatusById(
                        mDataEntity.getId(), CollectionEntity.SYNCSTATUS_UNEED);
            }
        } catch (JSONException e) {
            CustomLog.e("JSONException", e.toString());
        }
        CustomLog.d(TAG,"parseAddSyncResult end");
    }

    private SyncResult addItem() {
        CustomLog.d(TAG,"addItem begin");
        RequestParams params = GetInterfaceParams.getAddCollectionItemParams(
                getSelfNube(), getToken(), mDataEntity.getId(), getData());
        SyncResult result = httpUtils.sendSync(HttpMethod.POST, getUrl(),
                params, IMConstant.ACTIVITY_ACCESSTOKEN_INVALID);
        CustomLog.d(TAG,result.isOK() + "|" + result.getResult());
        return result;
    }

    private String getData() {
        JSONObject data = new JSONObject();
        try {
            data.put("operatorNube", mDataEntity.getOperatorNube());
            data.put("type", mDataEntity.getType());
            data.put("body", new JSONArray(mDataEntity.getBody()));
            if (TextUtils.isEmpty(mDataEntity.getExtinfo())) {
                data.put("extinfo", new JSONObject());
            } else {
                data.put("extinfo", new JSONObject(mDataEntity.getExtinfo()));
            }
        } catch (JSONException e) {
            CustomLog.e("JSONException", e.toString());
        }
        CustomLog.d(TAG,data.toString());
        return data.toString();
    }

    /*--------------------------------------------------------------------添加收藏数据 结束-----------------------------------------------------------------------------------*/

    /*--------------------------------------------------------------------删除收藏数据 开始-----------------------------------------------------------------------------------*/
    private void syncDeleteCollection() {
        if (isFinish) {
            CustomLog.d(TAG,"任务已经被取消，不再调用接口");
            return;
        }

        SyncResult syncResult = deleteItem();
        if (isFinish) {
            CustomLog.d(TAG,"接口返回，该任务已经被取消的场合，不做处理");
            return;
        }

        if (syncResult != null && syncResult.isOK()) {
            CustomLog.d(TAG,"接口调用成功，同步到本地操作");
            parseDeleteSyncResult(syncResult.getResult());
        } else {
            CustomLog.d(TAG,"接口调用失败");
        }
    }

    private void parseDeleteSyncResult(String result) {
        try {
            JSONObject object = new JSONObject(result);
            String status = object.optString("status");
            CustomLog.d(TAG,"服务端删除收藏结果status=" + status);
            if ("0".equals(status)) {
                CustomLog.d(TAG,"服务端删除收藏成功，删除本地数据库收藏数据：" + mDataEntity.getId());
                mCollectionDao.deleteRecordById(mDataEntity.getId());
            } else if ("-2".equals(status)) {
                CustomLog.d(TAG,"服务端已无此收藏，删除本地数据库收藏数据：" + mDataEntity.getId());
                mCollectionDao.deleteRecordById(mDataEntity.getId());
            } else {
                CustomLog.d(TAG,"服务端删除收藏失败:" + object.optString("message"));
            }
        } catch (JSONException e) {
            CustomLog.e("JSONException", e.toString());
        }
    }

    private SyncResult deleteItem() {
        CustomLog.d(TAG,"deleteItem begin");
        RequestParams params = GetInterfaceParams
                .getDeleteCollectionItemParams(getSelfNube(), getToken(),
                        mDataEntity.getId());
        SyncResult result = httpUtils.sendSync(HttpMethod.POST, getUrl(),
                params, IMConstant.ACTIVITY_ACCESSTOKEN_INVALID);
        CustomLog.d(TAG,result.isOK() + "|" + result.getResult());
        return result;
    }
}
