package cn.redcdn.hvs.im.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * Desc
 * Created by wangkai on 2017/2/27.
 */

public class DeviceLinearLayout extends LinearLayout {

    public boolean clickFlag = false;
    public DeviceLinearLayout(Context context, AttributeSet attrs) {

        super(context, attrs);
        clickFlag = false;
    }
    /**
     * 重写该方法，目的是为了能响应点击事件:仅模拟点击回调，并不能正真使用OnClick事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        if(action == MotionEvent.ACTION_DOWN){
            clickFlag = true;
        }
        return super.onTouchEvent(event);
    }
}
