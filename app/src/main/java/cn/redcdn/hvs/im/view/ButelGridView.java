package cn.redcdn.hvs.im.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.GridView;

/**
 * @ClassName: ButelGridView.java
 * @Description: 本GridView用于解决getView方法频繁执行的问题
 * @author: gtzha
 * @date: 2014年12月3日
 */
public class ButelGridView extends GridView {
    public boolean isOnMeasure;

    public ButelGridView(Context context) {
        super(context);
    }

    public ButelGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ButelGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d("onMeasure", "onMeasure");
        isOnMeasure = true;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d("onLayout", "onLayout");
        isOnMeasure = false;
        super.onLayout(changed, l, t, r, b);
    }
}
