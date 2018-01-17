package cn.redcdn.hvs.im.view; /**
 * <dl>
 * <dt>CustomGridView.java</dt>
 * <dd>Description:GridView空白区域事件</dd>
 * <dd>Copyright: Copyright (C) 2011</dd>
 * <dd>Company: 北京红云融通技术有限公司</dd>
 * <dd>CreateDate: 2015-6-11 上午15:36:34</dd>
 * </dl>
 * @author niuben
 */
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.GridView;

public class CustomGridView extends GridView {

    private OnClickListener mOnClickBlankPosListener;

    private boolean isonBlackClick=false;

    public CustomGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CustomGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomGridView(Context context) {
        super(context);
    }

    @SuppressLint("ClickableViewAccessibility") @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mOnClickBlankPosListener != null) {
            if (!isEnabled()) {
                return isClickable() || isLongClickable();
            }
            int action = event.getActionMasked();
            float x = event.getX();
            float y = event.getY();
            final int motionPosition = pointToPosition((int) x, (int) y);
            if (motionPosition == INVALID_POSITION) {
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        isonBlackClick=true;
                        //                    mTouchBlankPosListener.onTouchBlank(event);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        //                    if (Math.abs(mTouchX - x) > 10
                        //                            || Math.abs(mTouchY - y) > 10) {
                        //                        mTouchBlankPosListener.onTouchBlank(event);
                        //                    }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (isonBlackClick){
                            mOnClickBlankPosListener.onClick(this);
                        }else {
                            isonBlackClick=false;
                        }

                        break;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onMeasure(int widthMeasureSpec,int heightMeasureSpec){
        //重写了GridView的onMeasure方法，ScrollView嵌套使用时，使其不会出现滚动条。
        int expandSpec=MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE>>2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

    /**
     * 设置GridView的空白区域的触摸事件
     *
     * @param listener
     */
    public void setOnClickListener(
        OnClickListener listener) {
        mOnClickBlankPosListener = listener;
    }

    //    public interface OnTouchBlankPositionListener {
    //        void onTouchBlank(MotionEvent event);
    //    }
}