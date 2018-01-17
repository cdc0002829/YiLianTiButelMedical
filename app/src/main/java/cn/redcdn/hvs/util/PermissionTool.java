package cn.redcdn.hvs.util;

import android.hardware.Camera;

/**
 * Created by guoyx on 3/17/17.
 */

public class PermissionTool {
    /** 判断摄像头是否可用 * 主要针对6.0 之前的版本，现在主要是依靠try...catch... 报错信息，感觉不太好， * 以后有更好的方法的话可适当替换 * * @return */
    public static boolean isCameraPermission() {
        boolean canUse = true;
        Camera mCamera = null;
        try {
            mCamera = Camera.open(0);
            mCamera.setDisplayOrientation(90);
            Camera.Parameters mParameters = mCamera.getParameters();
            mCamera.setParameters(mParameters);
        } catch (Exception e) {
            canUse = false;
        }
        if (canUse) {
            mCamera.release();
            mCamera = null;
        }
        return canUse;
    }

}
