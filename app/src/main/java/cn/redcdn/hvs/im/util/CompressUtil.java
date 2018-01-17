package cn.redcdn.hvs.im.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import com.butel.connectevent.utils.LogUtil;

import java.awt.font.TextAttribute;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.im.fileTask.FileTaskManager;
import cn.redcdn.log.CustomLog;

/**
 * Desc
 * Created by wangkai on 2017/2/27.
 */

interface TaskListener {
    void noitfy(String body);
}

public class CompressUtil implements TaskListener {

    // private static int maxSize = 1000;
    private static final String TAG = "CompressUtil";
    private ExecutorService executeSvc = Executors.newFixedThreadPool(1);
    private Map<String, Future<?>> requestQueue = new HashMap<String, Future<?>>();
    private static CompressUtil instance;

    private static CompressUtil getInstance() {
        if (instance == null) {
            instance = new CompressUtil();
        }
        return instance;
    }

    public static <T> void addTask(Map<T, T> map) {
        getInstance().addCompressTask(map);
    }

    public static boolean isRuning() {
        return getInstance().isRuningTask();
    }

    @Override
    public void noitfy(String body) {
        String src = body;
        if (requestQueue.containsKey(src)) {
            requestQueue.remove(src);
        }
        CustomLog.d(TAG,"noitfy body：" + src);
    }

    public static boolean compressImage(Bitmap image, String outPath) {

        CustomLog.d(TAG,"outPath:" + outPath);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos);

            int options = 80;
            while (baos.toByteArray().length > FileTaskManager.PICTURE_COMPRESSION) {
                baos.reset();
                if(options<=0){
                    options = 5;
                }
                CustomLog.d(TAG,"options:" + options);
                image.compress(Bitmap.CompressFormat.JPEG, options, baos);
                if(options == 5){
                    break;
                }else{
                    options -= 20;
                }
            }
            if (image != null && !image.isRecycled()) {
                image.recycle();
                System.gc();// 内存回收
            }
            // ByteArrayInputStream isBm = new
            // ByteArrayInputStream(baos.toByteArray());
            // Bitmap bitmap = BitmapFactory.decodeStream(isBm, null,
            // null);
            File file = new File(outPath);
            if (!file.exists()) {
                File parentFolder = file.getParentFile();
                if (!parentFolder.exists()) {
                    parentFolder.mkdirs();
                }
                file.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(file);
            out.write(baos.toByteArray());

            out.flush();
            out.close();

            CustomLog.d(TAG,"compressImage end");
            return true;

        } catch (FileNotFoundException e) {
            LogUtil.e("FileNotFoundException", e);
        } catch (IOException e) {
            LogUtil.e("IOException", e);
        } catch (Exception e) {
            LogUtil.e("Exception", e);
        } catch (OutOfMemoryError e) {
            LogUtil.e("OutOfMemoryError", e);
        }
        return false;
    }

    public static boolean compressImage(String srcPath, String outPath) {
        CustomLog.d(TAG,"compressImage srcPath:" + srcPath + "|outPath:" + outPath);
        try {
            File resourceFile = new File(srcPath);
            if (resourceFile.exists()) {
                long length = resourceFile.length();
                // long size = length/1024;
                CustomLog.d(TAG,"file size:" + length);
                if (length < FileTaskManager.PICTURE_COMPRESSION) {
                    return false;
                }
            } else {
                return false;
            }
            BitmapFactory.Options newOpts = new BitmapFactory.Options();
            newOpts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(srcPath, newOpts);

            newOpts.inJustDecodeBounds = false;
            int w = newOpts.outWidth;
            int h = newOpts.outHeight;
//            float hh = 2048f;// 1024f
//            float ww = 1200f;// 600f
            float hh = 1280f;// 1280f
            float ww = 720f;// 720f
            int be = 1;//
            // if (w > h && w > ww) {
            // be = (int) (newOpts.outWidth / ww);
            // } else if (w < h && h > hh) {
            // be = (int) (newOpts.outHeight / hh);
            // }
            be = (int) ((w / ww + h / hh) / 2);
            if (be <= 0)
                be = 1;

            CustomLog.d(TAG,"压缩比为：" + be);

            newOpts.inSampleSize = be;// 压缩比率
            newOpts.inPurgeable = true;
            newOpts.inInputShareable = true;
            newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
            if (bitmap != null) {
                // TODO:摆正图片的显示方向
                int rotation = IMCommonUtil.getImageRotationByPath(
                        MedicalApplication.getContext(), srcPath);
                CustomLog.d(TAG,"图片旋转方向：" + rotation);
                if (rotation != 0) {
                    Matrix m = new Matrix();
                    m.setRotate(rotation);
                    Bitmap transformed = Bitmap.createBitmap(bitmap, 0, 0,
                            bitmap.getWidth(), bitmap.getHeight(), m, true);
                    if (transformed != null) {
                        bitmap = null;
                        return compressImage(transformed, outPath);
                    } else {
                        return compressImage(bitmap, outPath);
                    }
                } else {
                    return compressImage(bitmap, outPath);// 质量压缩
                }
            }
        } catch (Exception e) {
            LogUtil.e("Exception", e);
        } catch (OutOfMemoryError e) {
            LogUtil.e("OutOfMemoryError", e);
        }
        return false;
    }

    private <T> void addCompressTask(Map<T, T> map) {
        if (map != null && map.size() > 0) {
            CompressTask task;
            for (Map.Entry<T, T> temp : map.entrySet()) {
                task = new CompressTask((String) temp.getKey(),
                        (String) temp.getValue(), instance);
                CustomLog.d(TAG,"key:" + (String) temp.getKey() + " value:"
                        + (String) temp.getValue());
                Future<?> request = executeSvc.submit(task);
                requestQueue.put((String) temp.getKey(), request);
            }
        }
    }

    private boolean isRuningTask() {
        if (requestQueue != null && requestQueue.size() > 0) {
            return true;
        }
        return false;
    }

    private class CompressTask implements Runnable {

        private String srcPath;
        private String outPath;
        private TaskListener handle;

        CompressTask(String _srcPath, String _outPath, TaskListener listener) {
            this.srcPath = _srcPath;
            this.outPath = _outPath;
            this.handle = listener;
        }

        @Override
        public void run() {
            compressImage(srcPath, outPath);
            if (handle != null)
                handle.noitfy(srcPath);
        }
    }
}
