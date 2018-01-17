package cn.redcdn.hvs.im.util;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import cn.redcdn.log.CustomLog;

/**
 * Hacky fix for Issue #4 and
 * http://code.google.com/p/android/issues/detail?id=18990
 *
 * ScaleGestureDetector seems to mess up the touch events, which means that
 * ViewGroups which make use of onInterceptTouchEvent throw a lot of
 * IllegalArgumentException: pointerIndex out of range.
 *
 * There's not much I can do in my code for now, but we can mask the result by
 * just catching the problem and ignoring it.
 *
 * @author Chris Banes
 */
public class ViewPages extends ViewPager {

    public ViewPages(Context context) {
        super(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (Exception e) {
            CustomLog.d("TAG",e.getLocalizedMessage());
        }
        return false;
    }

}
