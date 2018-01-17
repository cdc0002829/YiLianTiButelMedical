package cn.redcdn.hvs.im.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.redcdn.hvs.R;

/**
 * Desc
 * Created by wangkai on 2017/2/27.
 */

public class SharePressableImageView extends LinearLayout {

    public ImageView shareImageview;
    public TextView pressableTextview;
    private LinearLayout PressableImageView;

    public SharePressableImageView(Context context) {
        this(context, null);
    }

    public SharePressableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 导入布局
        PressableImageView = (LinearLayout) LayoutInflater.from(context)
                .inflate(R.layout.notice_list_pic_imageview, this, true);
        shareImageview = (ImageView) findViewById(R.id.iv);
        pressableTextview = (TextView) findViewById(R.id.tv);
    }

    /**
     * 设置图片资源
     */
    public void setImageResource(int resId) {
        shareImageview.setImageResource(resId);
    }

    /**
     * 设置显示的文字
     */
    public void setTextViewText(String text) {
        pressableTextview.setText(text);
    }
}
