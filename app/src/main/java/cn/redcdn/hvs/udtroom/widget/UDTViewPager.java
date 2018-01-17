package cn.redcdn.hvs.udtroom.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;


/**
 * @author guoyx
 */

public class UDTViewPager extends ViewPager {
    private boolean isScrollEnabled = false;


    public UDTViewPager(Context context) {
        super(context);
    }


    public UDTViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!this.isScrollEnabled) {
            return super.onInterceptTouchEvent(event);
        }
        return false;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!this.isScrollEnabled) {
            return super.onTouchEvent(event);
        }
        return false;
    }


    public void lockPaging(boolean isLocked) {
        this.isScrollEnabled = isLocked;
    }
}

