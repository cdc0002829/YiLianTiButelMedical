package cn.redcdn.hvs.im.util;

import android.content.Context;
import android.os.PowerManager;
import cn.redcdn.hvs.MedicalApplication;

import static android.os.PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK;

/**
 * Designed by guoyx on 4/9/17.
 */

public class WakeLockHelper {
    Context context;
    PowerManager manager;
    PowerManager.WakeLock wakeLock;


    public WakeLockHelper() {
        context = MedicalApplication.getContext();
        manager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    }


    /**
     * 唤醒屏幕
     */
    public void setScreenOn() {

        if (wakeLock != null){
            wakeLock.setReferenceCounted(false);
            wakeLock.release();
            wakeLock = null;
        }

    }


    /**
     * 熄灭屏幕
     */
    public void setScreenOff() {
        if (wakeLock == null){
            wakeLock = manager.newWakeLock(PROXIMITY_SCREEN_OFF_WAKE_LOCK, "WakeAndLock");
        }

        wakeLock.acquire();
    }


}
