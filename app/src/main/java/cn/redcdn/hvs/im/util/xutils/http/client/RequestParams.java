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
package cn.redcdn.hvs.im.util.xutils.http.client;

import cn.redcdn.hvs.im.util.xutils.http.client.multipart.MultipartEntity;
import cn.redcdn.hvs.im.util.xutils.http.client.multipart.content.ContentBody;
import cn.redcdn.hvs.im.util.xutils.http.client.multipart.content.FileBody;
import cn.redcdn.hvs.im.util.xutils.http.client.multipart.content.InputStreamBody;
import cn.redcdn.hvs.im.util.xutils.http.client.multipart.content.StringBody;
import cn.redcdn.log.CustomLog;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class RequestParams {

    private static String URL_ENCODING = HTTP.UTF_8;

    private List<Header> headers;
    private List<NameValuePair> queryStringParams;
    private HttpEntity bodyEntity;
    private List<NameValuePair> bodyParams;
    private HashMap<String, ContentBody> fileParams;

    public RequestParams() {
    }

    public void addHeader(Header header) {
        if (this.headers == null) {
            this.headers = new ArrayList<Header>();
        }
        this.headers.add(header);
    }

    public void addHeader(String name, String value) {
        if (this.headers == null) {
            this.headers = new ArrayList<Header>();
        }
        this.headers.add(new BasicHeader(name, value));
    }

    public void addHeaders(List<Header> headers) {
        if (this.headers == null) {
            this.headers = new ArrayList<Header>();
        }
        this.headers.addAll(headers);
    }

//    public void addQueryStringParameter(String name, String value) {
//        if (queryStringParams == null) {
//            queryStringParams = new ArrayList<NameValuePair>();
//        }
//        queryStringParams.add(new BasicNameValuePair(name, value));
//    }
//
//    public void addQueryStringParameter(NameValuePair nameValuePair) {
//        if (queryStringParams == null) {
//            queryStringParams = new ArrayList<NameValuePair>();
//        }
//        queryStringParams.add(nameValuePair);
//    }
//
//    public void addQueryStringParameter(List<NameValuePair> nameValuePairs) {
//        if (queryStringParams == null) {
//            queryStringParams = new ArrayList<NameValuePair>();
//        }
//        queryStringParams.addAll(nameValuePairs);
//    }

    public void addBodyParameter(String name, String value) {
        if (bodyParams == null) {
            bodyParams = new ArrayList<NameValuePair>();
        }
        bodyParams.add(new BasicNameValuePair(name, value));
    }

    public void addBodyParameter(NameValuePair nameValuePair) {
        if (bodyParams == null) {
            bodyParams = new ArrayList<NameValuePair>();
        }
        bodyParams.add(nameValuePair);
    }

    public void addBodyParameter(List<NameValuePair> nameValuePairs) {
        if (bodyParams == null) {
            bodyParams = new ArrayList<NameValuePair>();
        }
        bodyParams.addAll(nameValuePairs);
    }

    public void addBodyParameter(String key, File file) {
        if (fileParams == null) {
            fileParams = new HashMap<String, ContentBody>();
        }
        fileParams.put(key, new FileBody(file));
    }

    public void addBodyParameter(String key, File file, String mimeType) {
        if (fileParams == null) {
            fileParams = new HashMap<String, ContentBody>();
        }
        fileParams.put(key, new FileBody(file, mimeType));
    }

    public void addBodyParameter(String key, File file, String mimeType, String charset) {
        if (fileParams == null) {
            fileParams = new HashMap<String, ContentBody>();
        }
        fileParams.put(key, new FileBody(file, mimeType, charset));
    }

    public void addBodyParameter(String key, InputStream stream, long length, String fileName) {
        if (fileParams == null) {
            fileParams = new HashMap<String, ContentBody>();
        }
        fileParams.put(key, new InputStreamBody(stream, length, fileName));
    }

    public void addBodyParameter(String key, InputStream stream, long length, String mimeType, String fileName) {
        if (fileParams == null) {
            fileParams = new HashMap<String, ContentBody>();
        }
        fileParams.put(key, new InputStreamBody(stream, length, mimeType, fileName));
    }

    public void setBodyEntity(HttpEntity bodyEntity) {
        this.bodyEntity = bodyEntity;
        if (bodyParams != null) {
            bodyParams.clear();
            bodyParams = null;
        }
        if (fileParams != null) {
            fileParams.clear();
            fileParams = null;
        }
    }

    /**
     * Returns an HttpEntity containing all request parameters
     */
    public HttpEntity getEntity() {

        if (bodyEntity != null) {
            return bodyEntity;
        }

        HttpEntity result = null;

        if (fileParams != null && !fileParams.isEmpty()) {

            MultipartEntity multipartEntity = new MultipartEntity();

            if (bodyParams != null) {
                for (NameValuePair param : bodyParams) {
                    try {
                        multipartEntity.addPart(param.getName(), new StringBody(param.getValue()));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }

            for (ConcurrentHashMap.Entry<String, ContentBody> entry : fileParams.entrySet()) {
                ContentBody file = entry.getValue();
                multipartEntity.addPart(entry.getKey(), entry.getValue());
            }

            result = multipartEntity;
        } else {
        	
        	if (bodyParams == null) {
        		return null;
        	}
            result = new BodyParamsEntity(bodyParams, URL_ENCODING);
        }

        return result;
    }

    public List<NameValuePair> getQueryStringParams() {
        return this.queryStringParams;
    }

    public List<Header> getHeaders() {
        return headers;
    }
    
    public void printAllParams(){
    	if(headers!=null&&headers.size()>0){
    		for(Header item:headers){
    			CustomLog.d("RequestParams","header parameter :"+item.getName()+" "+item.getValue());
    		}  		
    	}else{
            CustomLog.d("RequestParams","headers params is empty");
    	}
    	
    	if(queryStringParams!=null&&queryStringParams.size()>0){
    		for(NameValuePair item:queryStringParams){
                CustomLog.d("RequestParams","queryStringParams parameter :"+item.getName()+" "+item.getValue());
    		}  		
    	}else{
    		CustomLog.d("RequestParams","queryStringParams params is empty");
    	}
    	
    	if(bodyParams!=null&&bodyParams.size()>0){
    		for(NameValuePair item:bodyParams){
                CustomLog.d("RequestParams","bodyParams parameter :"+item.getName()+" "+item.getValue());
    		}  		
    	}else{
            CustomLog.d("RequestParams","bodyParams params is empty");
    	}
    	if(fileParams!=null&&fileParams.size()>0){
	    	for (ConcurrentHashMap.Entry<String, ContentBody> entry : fileParams.entrySet()) {
                CustomLog.d("RequestParams","fileParams parameter :"+entry.getKey()+" "+entry.getValue());
	        }
    	}else{
            CustomLog.d("RequestParams","fileParams params is empty");
    	}
    }
}