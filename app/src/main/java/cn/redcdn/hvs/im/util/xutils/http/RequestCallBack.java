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


public abstract class RequestCallBack<T> {

    private boolean progress = true;
    private int rate = 1000 * 1;//每秒


    public boolean isProgress() {
        return progress;
    }

    public int getRate() {
        return rate;
    }

    /**
     * 设置进度,而且只有设置了这个了以后，onLoading才能有效。
     *
     * @param progress 是否启用进度显示
     * @param rate     进度更新频率
     */
    public RequestCallBack<T> progress(boolean progress, int rate) {
        this.progress = progress;
        this.rate = rate;
        return this;
    }

    public void onStart() {
    }

    /**
     * onLoading方法有效progress
     *
     * @param total
     * @param current
     */
    public void onLoading(long total, long current) {
    }

    public void onSuccess(T result) {
    }

    public void onFailure(Throwable error, String msg) {
    }
}
