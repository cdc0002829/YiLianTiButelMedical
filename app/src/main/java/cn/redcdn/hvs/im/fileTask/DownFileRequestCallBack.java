package cn.redcdn.hvs.im.fileTask;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.apache.http.client.HttpResponseException;

import java.io.File;

import cn.redcdn.hvs.im.bean.FileTaskBean;
import cn.redcdn.hvs.im.util.xutils.http.RequestCallBack;
import cn.redcdn.log.CustomLog;

/**
 * Desc
 * Created by wangkai on 2017/2/27.
 */

public class DownFileRequestCallBack extends RequestCallBack<File> {

    private static final String TAG = "DownFileRequestCallBack";

    private FileTaskBean taskbean = null;
    private Handler syncHandler = null;

    public DownFileRequestCallBack(FileTaskBean taskbean,Handler syncHandler){
        this.taskbean = taskbean;
        this.syncHandler = syncHandler;
    }

    public Handler getSyncHandler() {
        return syncHandler;
    }

    public void setSyncHandler(Handler syncHandler) {
        this.syncHandler = syncHandler;
    }

    public FileTaskBean getTaskbean() {
        return taskbean;
    }

    public void setTaskbean(FileTaskBean taskbean) {
        this.taskbean = taskbean;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(taskbean!=null){
            taskbean.setCurrent(0);
        }
    }

    @Override
    public void onLoading(long total, long current) {
        super.onLoading(total, current);
        if(taskbean!=null){
            taskbean.setTotal(total);
            taskbean.setCurrent(current);
            taskbean.setFilesize(total);
        }
    }

    @Override
    public void onSuccess(File result) {
        // TODO Auto-generated method stub
        super.onSuccess(result);
        if(taskbean!=null){
            taskbean.setSuccess_result(result.getPath());

            if(syncHandler!=null){
                Message msg = syncHandler.obtainMessage();

                msg.what= FileTaskManager.MSG_SUCCESS;
                Bundle data = new Bundle();
                data.putString("uuid", taskbean.getUuid());
                data.putString("srcUrl", taskbean.getSrcUrl());
                msg.setData(data);
                syncHandler.sendMessage(msg);
            }
        }
    }

    @Override
    public void onFailure(Throwable error, String msg) {
        // TODO Auto-generated method stub
        super.onFailure(error, msg);
        if(taskbean!=null){

            if(error instanceof HttpResponseException){
                if(416==((HttpResponseException) error).getStatusCode()){

                    if(syncHandler!=null){
                        Message fail_msg = syncHandler.obtainMessage();

                        fail_msg.what= FileTaskManager.MSG_SUCCESS;
                        taskbean.setSuccess_result(taskbean.getResultUrl());
                        CustomLog.d(TAG, "onFailure" + "DFileCallBack in onFailure 416,force reture success");
                        Bundle data = new Bundle();
                        data.putString("uuid", taskbean.getUuid());
                        data.putString("srcUrl", taskbean.getSrcUrl());
                        fail_msg.setData(data);
                        syncHandler.sendMessage(fail_msg);
                        return;
                    }

                }
            }
            taskbean.setFial_reason(msg);
            if(syncHandler!=null){
                Message fail_msg = syncHandler.obtainMessage();
                fail_msg.what= FileTaskManager.MSG_FAILURE;
                Bundle data = new Bundle();
                data.putString("uuid", taskbean.getUuid());
                data.putString("srcUrl", taskbean.getSrcUrl());
                fail_msg.setData(data);
                syncHandler.sendMessage(fail_msg);
            }
        }

    }
}
