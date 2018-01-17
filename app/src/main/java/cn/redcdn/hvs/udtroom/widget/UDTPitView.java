package cn.redcdn.hvs.udtroom.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * @author guoyx
 *
 * 用于显示界面的 loading、错误信息提示等状态。
 */

public class UDTPitView extends FrameLayout {

    public UDTPitView(
        @NonNull Context context,
        @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    public UDTPitView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    /**
     * 加载布局，初始化控件
     */
    private void init() {}


    public void showLoadingView() {}


    public void showEmptyView() {}
}
