package cn.redcdn.hvs.im.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.HeaderViewListAdapter;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import cn.redcdn.hvs.R;
import cn.redcdn.log.CustomLog;


/**
 * <dl>
 * <dt>SideBar.java</dt>
 * <dd>Description:快速定位条</dd>
 */
public class SideBar extends View {

	private SectionIndexer sectionIndexter = null;
	private ListView list;
	private View popView;
	private TextView mDialogText;
	public static boolean isCanRefreshListView = true;
//	private Rect bound = new Rect();
	private boolean showBkg = false;
	int choose = -1;// 选中
	Paint paint = new Paint();

	private boolean isShown = false;

	private WindowManager winManager = null;
	private WindowManager.LayoutParams layoutParam = new WindowManager.LayoutParams(
			LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
			WindowManager.LayoutParams.TYPE_APPLICATION,
			WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
					| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
			PixelFormat.TRANSLUCENT);

	public SideBar(Context context) {
		super(context);
		textSize = getResources().getDimensionPixelSize(
				R.dimen.x20);
	}

	public SideBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		textSize = getResources().getDimensionPixelSize(
				R.dimen.x20);
	}

	public SideBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		textSize = getResources().getDimensionPixelSize(
				R.dimen.x20);
	}

	/**
	 * @author: sunkai
	 * @Title: setListView
	 * @Description: 设置快速定位条关联的listView
	 * @param _list
	 * @date: 2013-8-9 下午2:41:23
	 */
	public void setListView(ListView _list) {
		list = _list;
		if (list.getHeaderViewsCount() > 0) {
			sectionIndexter = (SectionIndexer) ((HeaderViewListAdapter) _list
					.getAdapter()).getWrappedAdapter();
		} else {
			sectionIndexter = (SectionIndexer) _list.getAdapter();
		}

	}

	/**
	 * @author: sunkai
	 * @Title: setTextView
	 * @Description: 设置快速定位条滑动时，屏幕中央显示定位条选中的字母
	 * @param mDialogText
	 * @date: 2013-8-9 下午2:41:55
	 */
	public void setTextView(TextView mDialogText) {
		this.mDialogText = mDialogText;
		mDialogText.setTextSize(34);
	}

	public void setPopView(View popView) {
		this.popView = popView;
		winManager = (WindowManager) popView.getContext().getSystemService(
				Context.WINDOW_SERVICE);
	}

	/**
	 * @author: sunkai-PC
	 * @Title: onTouchEvent
	 * @Description: 滑动监听
	 * @param event
	 * @return
	 * @date 2013-8-9 下午2:43:13
	 */
	public boolean onTouchEvent(MotionEvent event) {
		String[] charArr = (String[]) sectionIndexter.getSections();
		if (charArr == null) {
			return true;
		}
		int length = charArr.length;
		float y = event.getY(); // 点击y坐标
		int oldChoose = choose;
		int c = (int) (y / getHeight() * length); // 获取每一个字母的高度
		super.onTouchEvent(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			showBkg = true;
			if (oldChoose != c) {
				if (c >= 0 && c < length) {
					if (popView != null) {
						handler.removeMessages(0);
						if (!isShown) {
							CustomLog.d("SideBar","ACTION_DOWN addView");
							winManager.addView(popView, layoutParam);
							isShown = true;
						}
					}
						mDialogText.setText(String.valueOf(charArr[c]));
					listViewSelection(c);
					choose = c;
				}
			}
			break;
		case MotionEvent.ACTION_MOVE:
			showBkg = true;
			if (oldChoose != c) {
				if (c >= 0 && c < length) {
					if (popView != null) {
						if (!isShown) {
							CustomLog.d("SideBar","ACTION_MOVE addView");
							winManager.addView(popView, layoutParam);
							isShown = true;
						}
					}
						mDialogText.setText(String.valueOf(charArr[c]));
					listViewSelection(c);
					choose = c;
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			CustomLog.d("SideBar","MotionEvent.ACTION_UP");
			showBkg = false;
			choose = -1;
			handler.removeMessages(0);
			handler.sendEmptyMessageDelayed(0, 300); // 延迟影藏提示框
			break;
		}
		invalidate();// 刷新
		return true;
	}

	public void hidePopWin() {
		if (popView != null) {
			if (isShown) {
				CustomLog.d("SideBar","handler removeView");
				winManager.removeView(popView);
				isShown = false;
				choose = -1;
				this.invalidate();
			}
		}
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			CustomLog.d("SideBar","handler handleMessage");
			if (popView != null) {
				if (isShown) {
					CustomLog.d("SideBar","handler removeView");
					winManager.removeView(popView);
					isShown = false;
				}
			}
		}

	};

	private void listViewSelection(int section) {
		if (sectionIndexter == null) {
			if (list.getHeaderViewsCount() > 0) {
				sectionIndexter = (SectionIndexer) ((HeaderViewListAdapter) list
						.getAdapter()).getWrappedAdapter();
			} else {
				sectionIndexter = (SectionIndexer) list.getAdapter();
			}
		}
		int position = sectionIndexter.getPositionForSection(section);
		CustomLog.d("SideBar","定位条位置：" + position);
		if (position == -1) {
			return;
		}
		list.setSelection(position);
	}

	private float textSize;

	/**
	 * @author: sunkai-PC
	 * @Title: onDraw
	 * @Description: 绘制导航字母
	 * @param canvas
	 * @date 2013-8-9 下午2:43:29
	 */
	protected void onDraw(Canvas canvas) {

		if (showBkg) {
			canvas.drawColor(Color.parseColor("#eeeefe"));
		}
		String[] charArr = (String[]) sectionIndexter.getSections();
		if (charArr != null && charArr.length > 0) {
			float height = getMeasuredHeight() / charArr.length;
			int size = charArr.length;
			for (int i = 0; i < size; i++) {
				String letter = charArr[i];
				if (i == choose) {
					paint.setColor(Color.parseColor("#646566"));
				} else {
					paint.setColor(Color.parseColor("#646566"));
				}
				paint.setTextSize(textSize);
				paint.setAntiAlias(true);
				
				/**bound带来的问题：bound将宽度统一了。但是由于字母的宽度各不相同，会导致误差--2015-12-9*/
				 float xPos = getMeasuredWidth() / 2 - paint.measureText(charArr[i]) / 2; 
				 float yPos = i * height  + (height + paint.measureText(charArr[i])) / 2;

				// x坐标等于中间-字符串宽度的一半.
//				paint.getTextBounds(letter, 0, letter.length(), bound);
//				float xPos = getMeasuredWidth() / 2 - bound.width() / 2;
				// 微调心标索引位置，使其与其他字母对齐
				// if(i == 0 && !charArr[i].equals("A")){
				// xPos -= 1;
				// }
				// 微调字母A位置，使其与其他字母对齐
//				if (charArr[i].equals("A")) {
//					xPos += 2;
//				}
//				float yPos = i * height + (height + bound.width()) / 2;
				if (charArr[i].equals("★")) {
					// 极会议中：给了星标图片

					BitmapDrawable bd = (BitmapDrawable) getResources()
							.getDrawable(R.drawable.xin_index);
					;
					Bitmap bitmap = bd.getBitmap();

					xPos = getMeasuredWidth() / 2 - bitmap.getWidth() / 2;
					yPos = (height + bitmap.getWidth()) / 2;
//					xPos += 1;
					yPos -= 6;
					canvas.drawBitmap(bitmap, xPos, yPos, paint);
				} else {
					canvas.drawText(letter, xPos, yPos, paint);
					
				}

				paint.reset();
			}
		}
		this.invalidate();
		super.onDraw(canvas);
	}
}
