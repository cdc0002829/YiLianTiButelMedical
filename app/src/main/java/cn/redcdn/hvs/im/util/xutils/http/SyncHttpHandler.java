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

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Intent;

import cn.redcdn.hvs.im.util.xutils.http.client.HttpRequest;
import cn.redcdn.hvs.im.util.xutils.http.client.callback.StringDownloadHandler;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.StringUtil;
import cn.redcdn.log.CustomLog;


public class SyncHttpHandler {

    private final String TAG = "SyncHttpHandler";

    private final AbstractHttpClient client;
    private final HttpContext context;
    private final StringDownloadHandler mStringDownloadHandler = new StringDownloadHandler();

    private int executionCount = 0;
    private String charset;
    private String misTokenCode;//token失效code

    public SyncHttpHandler(AbstractHttpClient client, HttpContext context,
            String charset,String misTokenCode) {
        this.client = client;
        this.context = context;
        this.charset = charset;
        this.misTokenCode = misTokenCode;
    }

    private SyncResult makeRequestWithRetries(HttpRequestBase request)
            throws IOException {

        boolean retry = true;
        IOException ioException = null;
        HttpRequestRetryHandler retryHandler = client
                .getHttpRequestRetryHandler();
        SyncResult mSyncResult = new SyncResult();
        while (retry) {
            try {
                HttpResponse response = client.execute(request, context);
                Object responseBody = mStringDownloadHandler.handleEntity(
                        response.getEntity(), null, charset);
                // SUNJIAN ADD 令牌验证失败的时候进行登录处理
                if (!StringUtil.isEmpty(responseBody)) {
                    JSONObject json = null;
                    String jsonStr = responseBody.toString();
                    try {
                        Object jsonType = (new JSONTokener(jsonStr))
                                .nextValue();
                        if (jsonType instanceof JSONObject) {
                            json = (JSONObject) jsonType;
                        } else if (!CommonUtil.isViewWebPage(responseBody
                                .toString())) {
                            CustomLog.d(TAG,"登录接口异常," + jsonStr);
                        }
                    } catch (JSONException e) {
                        CustomLog.e(TAG,"登录接口异常JSONException" + e.toString());
                    }
                    if (json != null) {
                        if (!StringUtil.isEmpty(misTokenCode) && misTokenCode.equals(json.optString("status"))) {
                            CustomLog.d(TAG,"授权令牌（accessToken）无效，需要重新登录");
                            //此处直接调用AppAuthManager方法会出现IOException,need loop prepare()
                            //故该成广播--》到Sip message Receiver中处理
//                            Intent intent = new Intent(CallManageConstant.TOKEN_INVALID);
//                            NetPhoneApplication.getContext().sendBroadcast(intent);
                        }
                    }
                }
                mSyncResult.setOK(true);
                mSyncResult.setResult(responseBody + "");
                return mSyncResult;
            } catch (UnknownHostException e) {
                ioException = e;
                retry = retryHandler.retryRequest(ioException,
                        ++executionCount, context);
                // suanjian add
            } catch (AccessTokenException e) {
                // 授权令牌（accessToken）无效
                // retry = retryHandler.retryRequest(e, ++executionCount,
                // context);
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
            throw ioException;
        } else {
            throw new IOException("未知网络错误");
        }

    }

    public SyncResult sendRequest(HttpRequestBase... params) {

        try {
            return makeRequestWithRetries(params[0]);

        } catch (IOException e) {
            e.printStackTrace();
            SyncResult mSyncResult = new SyncResult();
            if (e instanceof ConnectTimeoutException
                    || e instanceof SocketTimeoutException
                    || e instanceof SocketException) {
                mSyncResult.setOK(false);
                mSyncResult.setErrorCode(-200);
            }

            if (e instanceof ClientProtocolException) {
                mSyncResult.setOK(false);
                mSyncResult.setErrorCode(-300);
            }

            return mSyncResult;

        }
    }

}
