package cn.redcdn.hvs.boot;

import android.Manifest;
import android.app.Activity;
import android.support.v4.app.ActivityCompat;
import cn.redcdn.log.CustomLog;

import static cn.redcdn.hvs.boot.SplashActivity.PERMISSIONS_REQUEST_CODE;

/**
 * @author guoyx
 * 申请应用初始化所需运行时权限权限, 目前需要三种权限，排列组合共有七种情况，待优化
 * 为了避免一次申请多个权限时，授权成功的权限依然会添加提示框
 */

public class BootPermissionManager {
    private static final String TAG = BootPermissionManager.class.getSimpleName();
    private final Activity context;


    public BootPermissionManager(Activity context) {
        this.context = context;
    }


    public void requestPermissionA() {
        CustomLog.i(TAG, "requestPermissionA()");

        ActivityCompat.requestPermissions(context,
            new String[] {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE },
            PERMISSIONS_REQUEST_CODE);
    }


    public void requestPermissionB() {
        CustomLog.i(TAG, "requestPermissionB()");

        ActivityCompat.requestPermissions(context,
            new String[] {
                Manifest.permission.READ_PHONE_STATE },
            PERMISSIONS_REQUEST_CODE);
    }


    public void requestPermissionC() {
        CustomLog.i(TAG, "requestPermissionC()");

        ActivityCompat.requestPermissions(context,
            new String[] { Manifest.permission.GET_ACCOUNTS },
            PERMISSIONS_REQUEST_CODE);
    }


    public void requestPermissionAB() {
        CustomLog.i(TAG, "requestPermissionAB()");

        ActivityCompat.requestPermissions(context,
            new String[] {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE },
            PERMISSIONS_REQUEST_CODE);
    }


    public void requestPermissionAC() {
        CustomLog.i(TAG, "requestPermissionAC()");

        ActivityCompat.requestPermissions(context,
            new String[] {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.GET_ACCOUNTS },
            PERMISSIONS_REQUEST_CODE);
    }


    public void requestPermissionBC() {
        CustomLog.i(TAG, "requestPermissionBC()");

        ActivityCompat.requestPermissions(context,
            new String[] {
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.GET_ACCOUNTS },
            PERMISSIONS_REQUEST_CODE);
    }


    public void requestPermissionABC() {
        CustomLog.i(TAG, "requestPermissionABC()");

        ActivityCompat.requestPermissions(context,
            new String[] {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.GET_ACCOUNTS },
            PERMISSIONS_REQUEST_CODE);

    }
}
