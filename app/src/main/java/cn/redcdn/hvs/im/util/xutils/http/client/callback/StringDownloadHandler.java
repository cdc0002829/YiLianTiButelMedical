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
package cn.redcdn.hvs.im.util.xutils.http.client.callback;

import cn.redcdn.hvs.im.util.xutils.http.client.callback.RequestCallBackHandler;
import cn.redcdn.hvs.util.StringUtil;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class StringDownloadHandler {

    public Object handleEntity(HttpEntity entity, RequestCallBackHandler callback, String charset) throws IOException {
        if (entity == null)
            return null;

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        long count = entity.getContentLength();
        long curCount = 0;
        int len = -1;
        InputStream is = entity.getContent();
        //sunjian add gzip  2013/11/6 add
        Header encodString = entity.getContentEncoding();
        if (!StringUtil.isEmpty(encodString) && encodString.getValue().contains("gzip")) {
            is= new GZIPInputStream(is);
        }
        //add end
        while ((len = is.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
            curCount += len;
            if (callback != null) {
                callback.updateProgress(count, curCount, false);
            }
        }
        if (callback != null) {
            callback.updateProgress(count, curCount, true);
        }
        byte[] data = outStream.toByteArray();
        outStream.close();
        is.close();
        return new String(data, charset);
    }

}
