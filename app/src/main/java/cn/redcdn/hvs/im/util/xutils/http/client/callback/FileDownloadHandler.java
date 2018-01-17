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

import android.text.TextUtils;

import cn.redcdn.hvs.im.util.xutils.http.client.callback.RequestCallBackHandler;
import cn.redcdn.log.CustomLog;

import org.apache.http.HttpEntity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileDownloadHandler {

    public Object handleEntity(HttpEntity entity, RequestCallBackHandler callback, String target, boolean isResume) throws IOException {
        if (TextUtils.isEmpty(target) || target.trim().length() == 0) {
            return null;
        }

        File targetFile = new File(target);

        if (!targetFile.exists()) {
            targetFile.createNewFile();
        }

        long current = 0;
        FileOutputStream fileOutputStream = null;
        if (isResume) {
            current = targetFile.length();
            fileOutputStream = new FileOutputStream(target, true);
        } else {
            fileOutputStream = new FileOutputStream(target);
        }

        InputStream inputStream = entity.getContent();
        long fileLong = entity.getContentLength();
        CustomLog.d("FileDownloadHandler","返回的文件长度："+ fileLong +"   已经下载的文件长度："+ current);
        long total = fileLong + current;

        if (current >= total) {
            return targetFile;
        }

        if (callback != null && !callback.updateProgress(total, current, true)) {
            return null;
        }

        try {
            byte[] tmp = new byte[4096];
            int len;
            while ((len = inputStream.read(tmp)) != -1) {
                fileOutputStream.write(tmp, 0, len);
                current += len;
                if (callback != null) {
                    if (!callback.updateProgress(total, current, false)) {
                        throw new IOException("stop");
                    }
                }
            }
            fileOutputStream.flush();
            if (callback != null) {
                callback.updateProgress(total, current, true);
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }

        return targetFile;
    }

}
