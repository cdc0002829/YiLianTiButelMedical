/*
 * Copyright (c) 2013. wyouflf (wyouflf@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.redcdn.hvs.im.util.xutils.http;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.im.util.xutils.http.client.HttpRequest;
import cn.redcdn.hvs.im.util.xutils.http.client.callback.DefaultDownloadRedirectHandler;
import cn.redcdn.hvs.im.util.xutils.http.client.callback.DownloadRedirectHandler;
import cn.redcdn.hvs.im.util.xutils.http.client.callback.FileDownloadHandler;
import cn.redcdn.hvs.im.util.xutils.http.client.callback.RequestCallBackHandler;
import cn.redcdn.hvs.im.util.xutils.http.client.callback.StringDownloadHandler;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.StringUtil;
import cn.redcdn.log.CustomLog;

public class HttpHandler<T> extends AsyncTask<Object, Object, Object> implements
        RequestCallBackHandler {

    private final AbstractHttpClient client;
    private final HttpContext context;

    private final StringDownloadHandler mStringDownloadHandler = new StringDownloadHandler();
    private final FileDownloadHandler mFileDownloadHandler = new FileDownloadHandler();

    private DownloadRedirectHandler downloadRedirectHandler;

    public void setDownloadRedirectHandler(
            DownloadRedirectHandler downloadRedirectHandler) {
        this.downloadRedirectHandler = downloadRedirectHandler;
    }

    private final RequestCallBack callback;

    private int executionCount = 0;
    private String targetUrl = null; // 下载的路径
    private boolean isDownloadingFile;
    private boolean isResume = false; // 是否断点续传
    private String charset;
    private String misTokenCode;//token失效code

    private Context mContext = MedicalApplication.getContext();

    public HttpHandler(AbstractHttpClient client, HttpContext context,
            String charset, RequestCallBack callback ,String misTokenCode) {
        this.client = client;
        this.context = context;
        this.callback = callback;
        this.charset = charset;
        this.misTokenCode = misTokenCode;
    }

    // 执行请求
    private void execRequestWithRetries(HttpRequestBase request)
            throws IOException {
        if (isResume && targetUrl != null) {
            File downloadFile = new File(targetUrl);
            long fileLen = 0;
            if (downloadFile.isFile() && downloadFile.exists()) {
                fileLen = downloadFile.length();
            }
            if (fileLen > 0)
                request.setHeader("RANGE", "bytes=" + fileLen + "-");
        }

        boolean retry = true;
        IOException ioException = null;
        HttpRequestRetryHandler retryHandler = client
                .getHttpRequestRetryHandler();
        while (retry) {
            try {
                if (!isCancelled()) {

                    HttpResponse response = client.execute(request, context);
                    if (!isCancelled()) {
                        handleResponse(response);
                    }
                }
                return;
            } catch (UnknownHostException e) {
                publishProgress(UPDATE_FAILURE, e,
                        "unknownHostException：can't resolve host");
                return;
                // suanjian add
            } catch (AccessTokenException e) {
                // 授权令牌（accessToken）无效
                String tokenId = AccountManager.getInstance(mContext).getToken();
                ((HttpRequest) request).getUriBuilder().setParameter(
                        "accessToken", tokenId);
                retry = true;
            } catch (IOException e) {
                ioException = e;
                retry = retryHandler.retryRequest(ioException,
                        ++executionCount, context);
            } catch (NullPointerException e) {
                ioException = new IOException("NPE in HttpClient"
                        + e.getMessage());
                retry = retryHandler.retryRequest(ioException,
                        ++executionCount, context);
            } catch (Exception e) {
                ioException = new IOException("Exception" + e.getMessage());
                retry = retryHandler.retryRequest(ioException,
                        ++executionCount, context);
            }
        }
        if (ioException != null) {
            // sunjian add 20130829
            // 20140813 此处不需要抛送UPDATE_FAILURE，否则应用层会有两次异常的结果
            // publishProgress(UPDATE_FAILURE, ioException, "ioException");
            throw ioException;
        } else {
            throw new IOException("未知网络错误");
        }
    }

    @Override
    protected Object doInBackground(Object... params) {
        if (params != null && params.length == 3) {
            targetUrl = String.valueOf(params[1]);
            isDownloadingFile = targetUrl != null;
            isResume = (Boolean) params[2];
        }
        try {
            publishProgress(UPDATE_START);
            execRequestWithRetries((HttpRequestBase) params[0]);
        } catch (IOException e) {
            publishProgress(UPDATE_FAILURE, e, e.getMessage());
        }

        return null;
    }

    private final static int UPDATE_START = 1;
    private final static int UPDATE_LOADING = 2;
    private final static int UPDATE_FAILURE = 3;
    private final static int UPDATE_SUCCESS = 4;

    @SuppressWarnings("unchecked")
    @Override
    protected void onProgressUpdate(Object... values) {
        int update = Integer.valueOf(String.valueOf(values[0]));
        int length = values.length;
        switch (update) {
        case UPDATE_START:
            if (callback != null) {
                callback.onStart();
            }
            break;
        case UPDATE_LOADING:
            if (callback != null) {
                if (length >= 3) {
                    callback.onLoading(Long.valueOf(String.valueOf(values[1])),
                            Long.valueOf(String.valueOf(values[2])));
                }
            }
            break;
        case UPDATE_FAILURE:
            if (callback != null) {
                try {
                    if (length >= 3) {
                        callback.onFailure((Throwable) values[1],
                                (String) values[2]);
                    }
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
                if (length >= 2 && values[1] != null) {
                    CustomLog.d("HttpHandler","[异步访问结束  ==<] failure Throwable ="
                            + values[1].toString());
                }
                if (length >= 3 && values[2] != null) {
                    CustomLog.d("HttpHandler","[异步访问结束  ==<] failure values="
                            + values[2].toString());
                }
            }
            break;
        case UPDATE_SUCCESS:
            if (length >= 2 && callback != null) {
                try {
                    callback.onSuccess((T) values[1]);
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
                if (values[1] != null) {
                    CustomLog.d("HttpHandler"," [异步访问结束==<] success result="
                            + values[1].toString());
                }
            }
            break;
        default:
            break;
        }
        super.onProgressUpdate(values);
    }

    private void handleResponse(HttpResponse response) {
        StatusLine status = response.getStatusLine();
        if (status.getStatusCode() < 300) {
            try {
                HttpEntity entity = response.getEntity();
                Object responseBody = null;
                if (entity != null) {
                    lastUpdateTime = SystemClock.uptimeMillis();
                    if (isDownloadingFile) {
                        responseBody = mFileDownloadHandler.handleEntity(
                                entity, this, targetUrl, isResume);
                    } else {
                        responseBody = mStringDownloadHandler.handleEntity(
                                entity, this, charset);
                        // SUNJIAN ADD 令牌验证失败的时候进行登录处理
                        if (!StringUtil.isEmpty(responseBody)) {
                            String jsonStr = responseBody.toString();
                            try {
                                Object jsonType = (new JSONTokener(jsonStr)).nextValue();
                                if (jsonType instanceof JSONObject) {
                                    JSONObject json = (JSONObject) jsonType;
                                    
                                    if (!StringUtil.isEmpty(misTokenCode) && misTokenCode.equals(json.optString("status"))) {
                                        CustomLog.d("HttpHandler","授权令牌（accessToken）无效，需要重新登录");
                                        //此处直接调用AppAuthManager方法会出现IOException,need loop prepare()
                                        //故该成广播--》到Sip message Receiver中处理
//                                        Intent intent = new Intent(CallManageConstant.TOKEN_INVALID);
//                                        NetPhoneApplication.getContext().sendBroadcast(intent);
                                    }
                                } else {
                                    if (!CommonUtil.isViewWebPage(jsonStr)) {
                                        CustomLog.d("HttpHandler","登录接口异常," + jsonStr);
                                    }
                                }
                            } catch (JSONException jsonEx) {
                                CustomLog.d("HttpHandler","登录接口异常JSONException" + jsonEx.toString());
                            }
                        }
                    }

                }
                publishProgress(UPDATE_SUCCESS, responseBody);

            } catch (IOException e) {
                publishProgress(UPDATE_FAILURE, e, e.getMessage());
            }
        } else if (status.getStatusCode() == 302) {
            if (downloadRedirectHandler == null) {
                downloadRedirectHandler = new DefaultDownloadRedirectHandler();
            }
            HttpRequestBase request = downloadRedirectHandler
                    .getDirectRequest(response);
            if (request != null) {
                try {
                    response = client.execute(request, context);
                    if (!isCancelled()) {
                        handleResponse(response);
                    }
                } catch (IOException e) {
                    publishProgress(UPDATE_FAILURE, e, e.getMessage());
                }
            }

        } else {
            String errorMsg = "response status error code:"
                    + status.getStatusCode();
            if (status.getStatusCode() == 416 && isResume) {
                errorMsg += " \n maybe you have download complete.";
            }
            publishProgress(
                    UPDATE_FAILURE,
                    new HttpResponseException(status.getStatusCode(), status
                            .getReasonPhrase()), errorMsg);
        }
    }

    private boolean mStop = false;

    /**
     * 停止下载任务
     */
    @Override
    public void stop() {
        this.mStop = true;
    }

    public boolean isStop() {
        return mStop;
    }

    private long lastUpdateTime;

    @Override
    public boolean updateProgress(long total, long current,
            boolean forceUpdateUI) {
        if (mStop) {
            return !mStop;
        }
        if (callback != null && callback.isProgress()) {
            if (forceUpdateUI) {
                publishProgress(UPDATE_LOADING, total, current);
            } else {
                long currTime = SystemClock.uptimeMillis();
                if (currTime - lastUpdateTime >= callback.getRate()) {
                    lastUpdateTime = currTime;
                    publishProgress(UPDATE_LOADING, total, current);
                }
            }
        }
        return !mStop;
    }

}
