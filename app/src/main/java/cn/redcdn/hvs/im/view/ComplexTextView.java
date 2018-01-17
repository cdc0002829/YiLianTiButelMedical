package cn.redcdn.hvs.im.view;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

/**
 * 
 * <dl>
 * <dt>ComplexTextView.java</dt>
 * <dd>Description: 自定义文本框，用于嵌入多个超链接</dd>
 * <dd>Copyright: Copyright (C) 2011</dd>
 * <dd>Company: 安徽青牛信息技术有限公司</dd>
 * <dd>CreateDate: 2014年9月25日</dd>
 * </dl>
 * 
 * @author 10
 */
public class ComplexTextView extends EditText implements OnClickListener {

    Context mContext;
    private boolean preventClick;
    private OnClickListener clickListener;
    private boolean ignoreSpannableClick;

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public ComplexTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public ComplexTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ComplexTextView(Context context) {
        super(context);
        init(context);
    }

    public void init(Context context) {
        this.mContext = context;
    }

    /**
     * 
     * Description: 增加超链接部分
     * 
     * @param str
     *            显示文字
     * @param link
     *            链接内容
     * @return
     */
    public SpannableStringBuilder addLinkPart(final String str,
            final String link) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        ssb.append(str);
        TouchableSpan span = new TouchableSpan(mContext, str, link);
        ssb.setSpan(span, 0, str.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return ssb;
    }

//    public SpannableStringBuilder addIcon(int res, final String str) {
//        SpannableStringBuilder ssb = new SpannableStringBuilder();
//        ssb.append(str);
//        ImageSpan is =  new ImageSpan(mContext, res, );
//        ssb.setSpan(is, 0, str.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
//        return ssb;
//    }

    public boolean onTouchEvent(MotionEvent event) {
        if (getMovementMethod() != null)
            getMovementMethod()
                    .onTouchEvent(this, (Spannable) getText(), event);
        this.ignoreSpannableClick = true;
        boolean ret = super.onTouchEvent(event);
        this.ignoreSpannableClick = false;
        return ret;
    }

    /**
     * Returns true if click event for a clickable span should be ignored
     * 
     * @return true if click event should be ignored
     */
    public boolean ignoreSpannableClick() {
        return ignoreSpannableClick;
    }

    /**
     * Call after handling click event for clickable span
     */
    public void preventNextClick() {
        preventClick = true;
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        this.clickListener = listener;
        super.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (preventClick) {
            preventClick = false;
        } else if (clickListener != null)
            clickListener.onClick(v);
    }
}

