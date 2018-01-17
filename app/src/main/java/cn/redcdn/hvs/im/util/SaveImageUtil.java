package cn.redcdn.hvs.im.util;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.log.CustomLog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import static cn.redcdn.hvs.util.CommonUtil.getString;
import static com.butel.connectevent.utils.FileService.getFilePath;

/**
 * Created by guoyx on 3/10/17.
 */

public class SaveImageUtil {

    public static String TAG = "SavaImageUtil";
    private Context context;
    private String filePath;
    private File PHOTO_DIR;

    public SaveImageUtil(Context context){
        this.context = context;
    }

    public void savaPicToSDCard() {

        String postfix = getString(R.string.photos);

        PHOTO_DIR = new File(Environment.getExternalStorageDirectory() + postfix);

        try {
            final String fromFilePath = getFilePath();
            filePath = getDesFilePath(fromFilePath);
            if (!TextUtils.isEmpty(filePath)) {
                if (new File(filePath).exists()) {
                    CustomToast.show(context, R.string.picture_exist
                        + "\"" + PHOTO_DIR.getAbsolutePath() + "\"", 1);
                    return;
                }
            } else {
                CustomToast.show(context, getString(R.string.copy_fail), 1);
                return;
            }
            new Thread(new Runnable() {

                @Override
                public void run() {
                    boolean isSuccess = saveLocalFile(fromFilePath);
                    Message msg = saveHandler.obtainMessage();
                    msg.obj = isSuccess;
                    msg.what = 1;
                    saveHandler.sendMessage(msg);
                }
            }).start();
        } catch (Exception e) {
            CustomLog.e(TAG, e.toString());
            Message msg = saveHandler.obtainMessage();
            msg.obj = false;
            msg.what = 0;
            saveHandler.sendMessage(msg);
        }
    }


    private boolean saveLocalFile(String copyFilePath) {

        if (TextUtils.isEmpty(copyFilePath)) {
            return false;
        } else {
            CustomLog.d(TAG, "saveLocalFile 源文件路径：" + copyFilePath + "|" + "目标文件路径："
                + filePath);
            FileOutputStream os = null;
            FileInputStream in = null;
            try {
                os = new FileOutputStream(filePath);
                in = new FileInputStream(copyFilePath);
                byte[] buffer = new byte[8 * 1024];
                int c = -1;
                if (in != null) {
                    while ((c = in.read(buffer)) > 0) {
                        os.write(buffer, 0, c);
                    }
                }
                os.flush();
                return true;
            } catch (OutOfMemoryError e) {
                CustomLog.d(TAG, "os.write and os.flush出现异常" + e.toString());
            } catch (Exception e) {
            } finally {
                try {
                    if (in != null) {
                        in.close();
                        os = null;
                    }
                    if (os != null) {
                        os.close();
                        os = null;
                    }
                } catch (Exception e2) {
                    CustomLog.d(TAG, "in.close and os.close出现异常" + e2.toString());
                }
            }
        }
        return false;
    }


    private String getDesFilePath(String fromPhotoPath) {

        if (TextUtils.isEmpty(fromPhotoPath)) {
            return "";
        }
        try {
            if (!PHOTO_DIR.exists()) {
                PHOTO_DIR.mkdirs();
            }
            String sourceFileName = fromPhotoPath.substring(fromPhotoPath
                .lastIndexOf("/") + 1);

            return PHOTO_DIR.getAbsolutePath() + "/" + sourceFileName;
        } catch (Exception e) {

        }
        return "";
    }


    private Handler saveHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what){
                case 0:
                    CustomToast.show(context,"保存图片失败",1);
                case 1:
                    CustomToast.show(context,"保存图片成功",1);

            }
        }
    };
}
