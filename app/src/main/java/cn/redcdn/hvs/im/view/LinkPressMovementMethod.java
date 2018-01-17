package cn.redcdn.hvs.im.view;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.MotionEvent;
import android.widget.TextView;
/**
 * 用在消息页面：消息中包含链接时，处理链接字符串的点击事件
 * @author 2015-9-14
 *
 */
public class LinkPressMovementMethod extends LinkMovementMethod {
	private TouchableSpan mPressedSpan;

	private long startPress;
	private long endPress;
	private long pressMills;

	@Override
	public boolean onTouchEvent(TextView textView, Spannable spannable,
			MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			startPress = System.currentTimeMillis();
			mPressedSpan = getPressedSpan(textView, spannable, event);
			if (mPressedSpan != null) {
				mPressedSpan.setPressed(true);
				Selection.setSelection(spannable,
						spannable.getSpanStart(mPressedSpan),
						spannable.getSpanEnd(mPressedSpan));
			}
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			TouchableSpan touchedSpan = getPressedSpan(textView, spannable,
					event);
			if (mPressedSpan != null && touchedSpan != mPressedSpan) {
				mPressedSpan.setPressed(false);
				mPressedSpan = null;
				Selection.removeSelection(spannable);
			}
		} else {
			if (mPressedSpan != null) {
				mPressedSpan.setPressed(false);
				//长按事件结束，不响应短按事件
				endPress = System.currentTimeMillis();
				pressMills = endPress - startPress;
				if (pressMills > 500) {
					//判定是长按事件
				}else{
					super.onTouchEvent(textView, spannable, event);
				}
			}
			mPressedSpan = null;
			Selection.removeSelection(spannable);
		}
		return true;
	}

	private TouchableSpan getPressedSpan(TextView textView,
			Spannable spannable, MotionEvent event) {

		int x = (int) event.getX();
		int y = (int) event.getY();

		x -= textView.getTotalPaddingLeft();
		y -= textView.getTotalPaddingTop();

		x += textView.getScrollX();
		y += textView.getScrollY();

		Layout layout = textView.getLayout();
		int line = layout.getLineForVertical(y);
		int off = layout.getOffsetForHorizontal(line, x);

		TouchableSpan[] link = spannable
				.getSpans(off, off, TouchableSpan.class);
		TouchableSpan touchedSpan = null;
		if (link.length > 0) {
			touchedSpan = link[0];
		}
		return touchedSpan;
	}

}
