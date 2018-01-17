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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import android.os.Build;
import android.os.CpuUsageInfo;
import android.text.TextUtils;

import cn.redcdn.hvs.im.util.xutils.http.client.HttpRequest;
import cn.redcdn.hvs.im.util.xutils.http.client.RequestParams;
import cn.redcdn.hvs.im.util.xutils.http.client.callback.DownloadRedirectHandler;
import cn.redcdn.log.CustomLog;

public class HttpUtils {

    private final DefaultHttpClient httpClient = new DefaultHttpClient();
    private final HttpContext httpContext = new BasicHttpContext();

    public HttpUtils() {
        httpClient.setHttpRequestRetryHandler(new RetryHandler(DEFAULT_RETRY_TIMES));
    }

    // ************************************    default settings & fields ****************************

    private String charset = HTTP.UTF_8;

    private final static int DEFAULT_RETRY_TIMES = 3;
    // 5个线程并发上传，提高上传效率
    private static int httpThreadCount = 5;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            Thread tread = new Thread(r, "HttpUtils #" + mCount.getAndIncrement());
            tread.setPriority(Thread.NORM_PRIORITY - 1);
            return tread;
        }
    };

    private static final Executor executor = Executors.newFixedThreadPool(httpThreadCount, sThreadFactory);

    public HttpClient getHttpClient() {
        return this.httpClient;
    }

    // ***************************************** config *******************************************

    public void configCharset(String charSet) {
        if (charSet != null && charSet.trim().length() != 0) {
            this.charset = charSet;
        }
    }

    public void configCookieStore(CookieStore cookieStore) {
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
    }

    public void configUserAgent(String userAgent) {
        HttpProtocolParams.setUserAgent(this.httpClient.getParams(), userAgent);
    }

    public void configTimeout(int timeout) {
        final HttpParams httpParams = this.httpClient.getParams();
        ConnManagerParams.setTimeout(httpParams, timeout);
        HttpConnectionParams.setSoTimeout(httpParams, timeout);
        HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
    }

    public void configSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
        Scheme scheme = new Scheme("https", sslSocketFactory, 443);
        this.httpClient.getConnectionManager().getSchemeRegistry().register(scheme);
    }

    public void configRequestExecutionRetryCount(int count) {
        this.httpClient.setHttpRequestRetryHandler(new RetryHandler(count));
    }

    // ***************************************** send request *******************************************

    public HttpHandler send(HttpRequest.HttpMethod method, String url,
                            RequestCallBack<? extends Object> callBack) {
        return send(method, url, null, callBack,null);
    }

    public HttpHandler send(HttpRequest.HttpMethod method, String url, RequestParams params,
                            RequestCallBack<? extends Object> callBack) {
        return send(method, url, params, null, callBack,null);
    }
    
    public HttpHandler send(HttpRequest.HttpMethod method, String url, RequestParams params,
            RequestCallBack<? extends Object> callBack,String misTokenCode) {
    	return send(method, url, params, null, callBack,misTokenCode);
    }

    public HttpHandler send(HttpRequest.HttpMethod method, String url, RequestParams params, String contentType,
                            RequestCallBack<? extends Object> callBack,String misTokenCode) {
    	if(TextUtils.isEmpty(url)){
        	return null;
        }
        HttpRequest request = new HttpRequest(method, url);
        CustomLog.d("HttpUtils"," [异步访问开始  ==>] send url="+url);
        if(params!=null){
        	params.printAllParams();
        }
        return sendRequest(request, params, contentType, callBack,misTokenCode);
    }


    public SyncResult sendSync(HttpRequest.HttpMethod method, String url) {
        return sendSync(method, url, null);
    }

    public SyncResult sendSync(HttpRequest.HttpMethod method, String url, RequestParams params) {
        return sendSync(method, url, params, null);
    }
    
    public SyncResult sendSync(HttpRequest.HttpMethod method, String url, RequestParams params,String misTokenCode) {
        return sendSync(method, url, params, null,misTokenCode);
    }

    public SyncResult sendSync(HttpRequest.HttpMethod method, String url, RequestParams params, String contentType,String misTokenCode) {
    	SyncResult mSyncResult = new SyncResult();
        if(TextUtils.isEmpty(url)){
        	mSyncResult.setOK(false);
        	mSyncResult.setErrorCode(-100);
        	return mSyncResult;
        }
        HttpRequest request = new HttpRequest(method, url);
        CustomLog.d("HttpUtils","[同步访问开始  ==>]sendSync url="+url);
        if(params!=null){
        	params.printAllParams();
        }
        SyncResult result = sendSyncRequest(request, params, contentType,misTokenCode);
        if(result!=null){
        	if (result.isOK()){
                CustomLog.d("HttpUtils","[同步访问结束  ==<]sendSync result ="+result.getResult());
        	} else {
                CustomLog.d("HttpUtils","[同步访问结束  ==<]sendSync result failure  : "+result.getErrorMsg());
        	}
        }
        return  result;
    }

    // ***************************************** download *******************************************

    public HttpHandler<File> download(String url, String target,
                                      RequestCallBack<File> callback) {
        return download(url, null, target, false, null, callback,null);
    }

    public HttpHandler<File> download(String url, String target,
                                      DownloadRedirectHandler downloadRedirectHandler, RequestCallBack<File> callback) {
        return download(url, null, target, false, downloadRedirectHandler, callback,null);
    }

    public HttpHandler<File> download(String url, RequestParams params, String target,
                                      RequestCallBack<File> callback) {
        return download(url, params, target, false, null, callback,null);
    }

    public HttpHandler<File> download(String url, RequestParams params, String target,
                                      DownloadRedirectHandler downloadRedirectHandler, RequestCallBack<File> callback) {
        return download(url, params, target, false, downloadRedirectHandler, callback,null);
    }

    public HttpHandler<File> download(String url, String target, boolean isResume,
                                      RequestCallBack<File> callback) {
        return download(url, null, target, isResume, null, callback,null);
    }

    public HttpHandler<File> download(String url, String target, boolean isResume,
                                      DownloadRedirectHandler downloadRedirectHandler, RequestCallBack<File> callback) {
        return download(url, null, target, isResume, downloadRedirectHandler, callback,null);
    }

    public HttpHandler<File> download(String url, RequestParams params, String target, boolean isResume,
                                      RequestCallBack<File> callback) {
        return download(url, params, target, isResume, null, callback,null);
    }
    
    public HttpHandler<File> download(String url, RequestParams params, String target, boolean isResume,
            RequestCallBack<File> callback,String misTokenCode) {
    	return download(url, params, target, isResume, null, callback,misTokenCode);
   }

    public HttpHandler<File> download(String url, RequestParams params, String target, boolean isResume,
                                      DownloadRedirectHandler downloadRedirectHandler, RequestCallBack<File> callback,String misTokenCode) {
    	if(TextUtils.isEmpty(url)){
        	return null;
        }
        HttpRequest request = new HttpRequest(HttpRequest.HttpMethod.GET, url);
        HttpHandler<File> handler = new HttpHandler<File>(httpClient, httpContext, charset, callback,misTokenCode);
        request.setRequestParams(params, handler);
        handler.setDownloadRedirectHandler(downloadRedirectHandler);
        CustomLog.d("HttpUtils","download url="+url);
        if(params!=null){
        	params.printAllParams();
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            handler.executeOnExecutor(executor, request, target, isResume);
        } else {
        	handler.execute(request, target, isResume);
        }

        return handler;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    private <T> HttpHandler<T> sendRequest(HttpRequest request, RequestParams params, String contentType, RequestCallBack<T> callBack,String misTokenCode) {
        if (contentType != null) {
            request.addHeader("Content-Type", contentType);
        }

        HttpHandler<T> handler = new HttpHandler<T>(httpClient, httpContext, charset, callBack,misTokenCode);

        request.setRequestParams(params, handler);
        
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
        handler.executeOnExecutor(executor, request);
        } else {
        	handler.execute(request);
        }

        return handler;
    }

    private SyncResult sendSyncRequest(HttpRequest request, RequestParams params, String contentType,String misTokenCode) {
        if (contentType != null) {
            request.addHeader("Content-Type", contentType);
        }
        request.setRequestParams(params);
        return new SyncHttpHandler(httpClient, httpContext, charset,misTokenCode).sendRequest(request);
    }
}
