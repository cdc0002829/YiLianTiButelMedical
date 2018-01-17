package cn.redcdn.hvs.im.util;

import cn.redcdn.log.CustomLog;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Build;
import android.view.Surface;


/**
 * <dl>
 * <dt>CameraHelper.java</dt>
 * <dd>Description:手机摄像头相关处理</dd>
 * <dd>Copyright: Copyright (C) 2014</dd>
 * <dd>Company: 安徽青牛信息技术有限公司</dd>
 * <dd>CreateDate: 2014-3-20 上午11:11:47</dd>
 * </dl>
 *
 * @author zhaguitao
 */
public class CameraHelper {

    private static final String TAG = CameraHelper.class.getSimpleName();

    /** 应用支持的标准的视频预览宽度 */
    public static final int STANDARD_PREVIEW_WIDTH = 352;
    /** 应用支持的标准的视频预览高度 */
    public static final int STANDARD_PREVIEW_HEIGHT = 288;

    /** camera可用 */
    public static final int CAMERA_ENABLED_MASK = 0x0011;
    /** 前置camera可用 */
    public static final int CAMERA_FRONT_ENABLED = 0x0001;
    /** 后置camera可用 */
    public static final int CAMERA_BACK_ENABLED = 0x0010;
    /** camera不可用 */
    public static final int CAMERA_DISABLED = 0x0;

    /**
     * 部分手机摄像头所支持的previewsize虽然包含352*288，但设为352*288却不能正常预览和发送视频信息，
     * 因此需要特别列出来，以让此类手机的特定摄像头不能使用
     */
    public static final Map<String, Integer> UNSUPPORTED_PHONE_MAP = new HashMap<String, Integer>();
    static {
        // 三星GT-I9508，前置摄像头正常支持，后置摄像头视频图像显示彩条
        UNSUPPORTED_PHONE_MAP.put("GT-I9508", CAMERA_BACK_ENABLED);
        //		UNSUPPORTED_PHONE_MAP.put("M040", CAMERA_FRONT_ENABLED
        //				| CAMERA_BACK_ENABLED);
        //		UNSUPPORTED_PHONE_MAP.put("GT-I8268", CAMERA_FRONT_ENABLED
        //				| CAMERA_BACK_ENABLED);
    }

    /**
     * 部分手机摄像头所支持的previewsize虽然不包含352*288，但设为352*288却能正常预览和发送视频信息，
     * 因此需要特别列出来，以让此类手机的特定摄像头能使用
     */
    public static final Map<String, Integer> SUPPORTED_PHONE_MAP = new HashMap<String, Integer>();
    static {
        // 三星GT-I9508，前置摄像头正常支持，后置摄像头视频图像显示彩条
        SUPPORTED_PHONE_MAP.put("GT-I9508", CAMERA_FRONT_ENABLED);
    }

    /**
     * 前置摄像头竖屏拍摄时，MediaRecorder需要setOrientationHint为270度，
     * 部分手机比较特殊，只需要设置90度，否则图像就倒过来了，
     * 因此需要特别列出来，以便做排除
     */
    public static final Map<String, Integer> FRONT_CAMERA_HINT_DEGREE_MAP = new HashMap<String, Integer>();
    static {
        // MX2
        FRONT_CAMERA_HINT_DEGREE_MAP.put("M040", 90);
    }

    /**
     * @author: zhaguitao
     * @Title: initCameraInfo
     * @Description: 初始化摄像头信息
     * @return
     * @date: 2014-3-20 下午2:08:53
     */
    public static int initCameraInfo() {
        CustomLog.d(TAG,"initCameraInfo begin");
        int cameraResult = CAMERA_DISABLED;

        Camera camera = null;
        CameraInfo cameraInfo = new CameraInfo();

        // 特定手机不支持前置或后置摄像头
        Integer cameraSupport = UNSUPPORTED_PHONE_MAP.get(Build.MODEL
            .toUpperCase());
        boolean isSupportFront = true;
        boolean isSupportBack = true;
        if (cameraSupport != null) {
            if ((cameraSupport & CAMERA_FRONT_ENABLED) == CAMERA_FRONT_ENABLED) {
                isSupportFront = false;
                CustomLog.d(TAG,"initCameraInfo 强制不支持前置摄像头");
            }
            if ((cameraSupport & CAMERA_BACK_ENABLED) == CAMERA_BACK_ENABLED) {
                isSupportBack = false;
                CustomLog.d(TAG,"initCameraInfo 强制不支持后置摄像头");
            }
        }

        // 特定手机强制支持前置或后置摄像头
        cameraSupport = SUPPORTED_PHONE_MAP.get(Build.MODEL.toUpperCase());
        if (cameraSupport != null) {
            if (isSupportFront
                && (cameraSupport & CAMERA_FRONT_ENABLED) == CAMERA_FRONT_ENABLED) {
                isSupportFront = false;
                cameraResult = cameraResult | CAMERA_FRONT_ENABLED;
                CustomLog.d(TAG,"initCameraInfo 强制支持前置摄像头");
            }
            if (isSupportBack
                && (cameraSupport & CAMERA_BACK_ENABLED) == CAMERA_BACK_ENABLED) {
                isSupportBack = false;
                cameraResult = cameraResult | CAMERA_BACK_ENABLED;
                CustomLog.d(TAG,"initCameraInfo 强制支持后置摄像头");
            }
        }

        try {
            // 得到摄像头的个数
            int cameraCount = Camera.getNumberOfCameras();
            CustomLog.d(TAG,"initCameraInfo cameraCount:" + cameraCount);

            for (int i = 0; i < cameraCount; i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (isSupportFront
                    && cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    // 前置摄像头
                    if (isCameraEnabled(camera, i)) {
                        // 前置摄像头支持标准分辨率
                        cameraResult = cameraResult | CAMERA_FRONT_ENABLED;
                        CustomLog.d(TAG,"initCameraInfo 支持前置摄像头");
                    }
                } else if (isSupportBack
                    && cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
                    // 后置摄像头
                    if (isCameraEnabled(camera, i)) {
                        // 后置摄像头支持标准分辨率
                        cameraResult = cameraResult | CAMERA_BACK_ENABLED;
                        CustomLog.d(TAG,"initCameraInfo 支持后置摄像头");
                    }
                }
            }
        } catch (Exception e) {
            CustomLog.e("Exception", String.valueOf(e));
        } finally {
            if (camera != null) {
                camera.release();
                camera = null;
            }
        }

        return cameraResult;
    }

    /**
     * @author: zhaguitao
     * @Title: isCameraEnabled
     * @Description: 指定摄像头是否可用，即摄像头previewsize是否支持标准分辨率
     * @param camera
     * @param cameraIdx
     * @return
     * @throws InterruptedException
     * @date: 2014-3-20 下午2:09:51
     */
    private static boolean isCameraEnabled(Camera camera, int cameraIdx)
        throws InterruptedException {
        boolean enabled = false;

        int retryCnt = 3;
        while (retryCnt > 0) {
            try {
                CustomLog.d(TAG,"isCameraEnabled Camera.open:" + cameraIdx);
                camera = Camera.open(cameraIdx);
                break;
            } catch (Exception e) {
                CustomLog.d(TAG," isCameraEnabled 打开摄像头失败，可能由于摄像头打开过于频繁，隔4s再次尝试。"
                    + e.getLocalizedMessage());
                camera = null;
                retryCnt--;
                if (retryCnt > 0) {
                    // 隔4s再次尝试
                    Thread.sleep(4000);
                    continue;
                } else {
                    break;
                }
            }
        }

        if (camera == null) {
            return enabled;
        }

        try {
            Camera.Parameters parameters = camera.getParameters();
            CustomLog.d(TAG,"isCameraEnabled Camera Parameters:"
                + parameters.flatten());
            parameters.setPreviewSize(STANDARD_PREVIEW_WIDTH,
                STANDARD_PREVIEW_HEIGHT);
            camera.setParameters(parameters);

            // 判断设置的previewsize是否有效
            parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();
            if (size.width == STANDARD_PREVIEW_WIDTH
                && size.height == STANDARD_PREVIEW_HEIGHT) {
                // 设置成功，表示摄像头支持标准分辨率
                enabled = true;
            }
        } catch (Exception e) {
            CustomLog.d(TAG,"isCameraEnabled camera.setParameters 错误，摄像头不支持标准分辨率。"
                + e.getLocalizedMessage());
            enabled = false;
        }

        if (camera != null) {
            camera.release();
            camera = null;
        }

        return enabled;
    }

    /***
     * 获得可使用（可支持352*288视频或SUPPORTED_PHONE_MAP中明确支持的）的摄像头数目
     *
     * @param mask
     *            保存在本地的已知的摄像头
     * @return 0：不支持视频通话，1：不可切换前后摄像头，2：可切换前后摄像头
     */
    public static int getNumberOfEnableCameras(String mask) {

        int value = 0;
        try {
            value = Integer.parseInt(mask);
            if (CAMERA_ENABLED_MASK == value) {
                return 2;
            }
            if (CAMERA_FRONT_ENABLED == value) {
                return 1;
            }
            if (CAMERA_BACK_ENABLED == value) {
                return 1;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * @author: zhaguitao
     * @Title: setCameraDisplayOrientation
     * @Description: activity中，camera显示需要旋转的角度
     * @param activity
     * @param cameraId
     * @param camera
     * @date: 2014-4-2 下午5:47:21
     */
    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, Camera camera) {
        // See android.hardware.Camera.setCameraDisplayOrientation for
        // documentation.
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int degrees = getDisplayRotation(activity);
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        CustomLog.d(TAG,"setCameraDisplayOrientation:" + result);
        camera.setDisplayOrientation(result);
    }

    /**
     * @author: zhaguitao
     * @Title: getDisplayRotation
     * @Description: activity显示的角度
     * @param activity
     * @return
     * @date: 2014-4-2 下午5:47:06
     */
    public static int getDisplayRotation(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay()
            .getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }
}
