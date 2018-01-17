package cn.redcdn.hvs.im.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;

import android.util.Log;

import cn.redcdn.hvs.im.bean.FileTaskBean;
import cn.redcdn.hvs.im.fileTask.DownFileRequestCallBack;
import cn.redcdn.log.CustomLog;


public class DownloadTask implements Runnable {
    public static final String TAG = "DownloadTask";
    private HttpURLConnection conn = null;

    private String uuid;
    private DownFileRequestCallBack callback;
    private FileTaskBean bean;
    private boolean isStop = false;

    public DownloadTask(String uuid, DownFileRequestCallBack callback) {
        this.uuid = uuid;
        this.callback = callback;
        this.bean = callback.getTaskbean();
    }

    @Override
    public void run() {
        // TODO:执行下载任务
        download(bean.getSrcUrl(), bean.getResultUrl());
        // Thread.sleep(1500);
        System.out.println(bean.getSrcUrl() + " executed OK!");
    }

    public String getFileId() {
        return uuid;
    }

    /**
     *
     * @param url
     *            源文件地址
     * @param destPath
     *            文件在本地存储路径
     */
    public void download(String url, String destPath) {

        InputStream is = null;
        OutputStream fos = null;
//		boolean hasSend = false;
        try {

            URL downloadAddress = new URL(url);
            conn = (HttpURLConnection) downloadAddress.openConnection();
            conn.setConnectTimeout(30 * 1000);
            conn.setReadTimeout(30 * 1000);
            conn.connect();
            int code = conn.getResponseCode();
            Log.d(TAG, "HttpURLConnection  return code:" + code);
            // 源文件大小
            int length = (int) conn.getContentLength();
            CustomLog.d("DownloadTask","file conn.getContentLength:" + length);
            // 创建输入流
            is = conn.getInputStream();

            File file = new File(destPath);

            if (!file.exists()) {
                boolean succ = file.createNewFile();
                Log.d(TAG, "创建文件 succ=" + succ);
            }
            fos = new FileOutputStream(file);

            // 缓存
            byte buf[] = new byte[1024];
            // 下载进度
            int preProgress=0;
            int currentProgress=0;
            int count = 0;
            callback.onStart();

            // 写入到文件中
            do {
                int numread = is.read(buf);
                count += numread;
                if(isStop){
                    conn.disconnect();
                    break;
                }
                if (numread <= 0) {
                    // 下载完成,关闭文件
                    if (fos != null) {
                        fos.flush();
                        fos.close();
                        fos = null;
                    }

                    callback.onSuccess(file);
//					hasSend = true;
                    break;
                }

                // 计算进度条位置
                currentProgress = (int) (((float) count / length) * 100);
                //对进度消息的发送做一个控制
                if(currentProgress-preProgress>1){
                    // 更新进度:传递的是count而不是currentProgress.因为viewphotoActivity做了
                    //count/total处理
                    callback.onLoading(length, count);
                    preProgress=currentProgress;
                }


                // 写入文件
                fos.write(buf, 0, numread);
            } while (true);// 点击取消就停止下载.
        } catch (SocketTimeoutException se) {

            CustomLog.e("DownloadTask","软件下载 网络连接超时 异常:" + se.toString());
            se.printStackTrace();
            callback.onFailure(se, "");

        } catch (SocketException e) {

            CustomLog.e("DownloadTask","文件下载 网络连接 异常：" + e.toString());
//			if (!hasSend) {
            callback.onFailure(e, "");
//				hasSend = true;
//			}
        } catch (IOException e) {
            CustomLog.e("DownloadTask","下载 IO异常：" + e.toString());
            callback.onFailure(e, "");
        } catch (Exception e) {
            CustomLog.e("DownloadTask", "下载 未知异常：" + e.toString());
            callback.onFailure(e, "");
        } finally {

            //先关输出流，再关输入流，最后disconnect
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    CustomLog.e("DownloadTask","软件下载异常：finally 中 fos 关闭异常:" + e.toString());
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    CustomLog.e("DownloadTask","软件下载异常：finally 中is 关闭流异常:" + e.toString());
                }
                is = null;
            }

            if (conn != null) {
                conn.disconnect();
                conn = null;
            }

        }
    }

    public void stopDownload() {
        isStop = true;
//		new Thread() {
//			@Override
//			public void run() {
//				super.run();
//				conn.disconnect();
//			}
//		}.start();
    }
}