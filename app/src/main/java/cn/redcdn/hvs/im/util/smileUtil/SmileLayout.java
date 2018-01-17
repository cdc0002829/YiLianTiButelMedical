package cn.redcdn.hvs.im.util.smileUtil;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RelativeLayout;

import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.log.CustomLog;


public class SmileLayout extends RelativeLayout implements EmojiconRecents {

	//	private EmojiconRecents mRecents;
	private OnEmojiconBackspaceClickedListener mOnEmojiconBackspaceClickedListener;
	// mOnEmojiconBackspaceClickedListener;
	private OnEmojiconClickedListener mOnEmojiconClickedListener;

	private int EMITION_BOARD_HEIGHT = getResources().getDimensionPixelOffset(
			R.dimen.smile_board_height);
	private List<Emojicon> mDataList;

	private int ROW_COUNT = 3;
	private int COLUMN_COUNT = 7;
	private int pageCount;

	private DotGroup dotGroup;
	private ViewPager viewPager;
//	private EmojiconEditText blogEditText;

	private LayoutInflater inflater;
//	private Activity mActivity;

	public SmileLayout(Context context) {
		super(context);
	}

	public SmileLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SmileLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public void setOnItemClickedListener(OnEmojiconClickedListener listener) {
		this.mOnEmojiconClickedListener = listener;
	}

	public void setOnBackspaceClickedListener(OnEmojiconBackspaceClickedListener listener) {
		this.mOnEmojiconBackspaceClickedListener = listener;
	}

	public void init(Emojicon[] data) {

//		mActivity = actvity;
		inflater = LayoutInflater.from(MedicalApplication.getContext());
//		mOnEmojiconClickedListener = (OnEmojiconClickedListener) actvity;
//		mOnEmojiconBackspaceClickedListener=(OnEmojiconBackspaceClickedListener)actvity;
		mDataList = Arrays.asList(data);

		// 行数*列数 - 删除按钮
		pageCount = mDataList.size() / (ROW_COUNT * COLUMN_COUNT - 1);
		if (mDataList.size() % (ROW_COUNT * COLUMN_COUNT - 1) > 0) {
			pageCount++;
		}

//		this.blogEditText = blogEditText;
//		this.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//			}
//		});

		LayoutInflater.from(getContext()).inflate(R.layout.cpnt_emotionl, this);
		viewPager = (ViewPager) findViewById(R.id.viewpager);
		dotGroup = (DotGroup) findViewById(R.id.dotGroup);
		viewPager.setId("vp".hashCode());
		viewPager.setAdapter(mPagerAdapter);
		viewPager.setOffscreenPageLimit(pageCount);
		dotGroup.init(pageCount);
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				dotGroup.setCurrentItem(arg0);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}

	public ViewPager getViewPager() {
		return viewPager;
	}

	AdapterView.OnItemClickListener onEmotionClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
								long id) {

			CustomLog.d("SmileLayout","onItemClick:position=" + position);

			if (position == parent.getCount() - 1) {
				if (mOnEmojiconBackspaceClickedListener != null) {
					mOnEmojiconBackspaceClickedListener.onEmojiconBackspaceClicked(view);
				}
			}else{
				Emojicon emojicon = (Emojicon) parent.getItemAtPosition(position);
				if (mOnEmojiconClickedListener != null) {
					mOnEmojiconClickedListener.onEmojiconClicked(emojicon);
				}
//				if (mRecents != null) {
//					mRecents.addRecentEmoji(view.getContext(),
//							((Emojicon) parent.getItemAtPosition(position)));
//				}
			}
		}
	};

	// ViewPager的适配器
	PagerAdapter mPagerAdapter = new PagerAdapter() {

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			CustomLog.d("SmileLayout","标签pageradapter,position=" + position);

			// 填充resList,作为图片数据;
			int startIdx = (ROW_COUNT * COLUMN_COUNT - 1) * position;
			int endIdx = (ROW_COUNT * COLUMN_COUNT - 1) * (position + 1);
			if (endIdx > mDataList.size()) {
				endIdx = mDataList.size();
			}
			List<Emojicon> pageSmileList = mDataList.subList(startIdx, endIdx);

			View convertView = inflater.inflate(R.layout.emojicon_grid,
					container, false);
			GridView gridview = (GridView) convertView
					.findViewById(R.id.Emoji_GridView);
			gridview.setNumColumns(COLUMN_COUNT);
			gridview.setAdapter(new EmojiAdapter(getContext(), pageSmileList));

			gridview.setOnItemClickListener(onEmotionClickListener);
			container.addView(gridview, LayoutParams.MATCH_PARENT,
					EMITION_BOARD_HEIGHT);
			return gridview;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public int getCount() {
			return pageCount;
		}
	};

	public interface OnEmojiconClickedListener {
		void onEmojiconClicked(Emojicon emojicon);
	}
	public interface OnEmojiconBackspaceClickedListener {
		void onEmojiconBackspaceClicked(View v);
	}

	public static void input(EditText editText, Emojicon emojicon) {
		CustomLog.d("SmileLayout","");
		if (editText == null || emojicon == null) {
			return;
		}

		CustomLog.d("SmileLayout","edittext input emoji=" + emojicon.getEmoji());
		int start = editText.getSelectionStart();
		int end = editText.getSelectionEnd();
		if ((end - start == 0)
				&& Math.max(start, end) == editText.getText().toString().length()) {
			editText.append(emojicon.getEmoji());
		} else {
			editText.getText().replace(Math.min(start, end),
					Math.max(start, end), emojicon.getEmoji(), 0,
					emojicon.getEmoji().length());
		}
	}

	@Override
	public void addRecentEmoji(Context context, Emojicon emojicon) {
		EmojiconRecentsManager recents = EmojiconRecentsManager
				.getInstance(context);
		recents.push(emojicon);

		// notify dataset changed
		// if (mAdapter != null)
		// mAdapter.notifyDataSetChanged();
	}

	public static void backspace(EditText editText) {
		KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0,
				0, KeyEvent.KEYCODE_ENDCALL);
		editText.dispatchKeyEvent(event);
	}

	/**
	 * A class, that can be used as a TouchListener on any view (e.g. a Button).
	 * It cyclically runs a clickListener, emulating keyboard-like behaviour.
	 * First click is fired immediately, next before initialInterval, and
	 * subsequent before normalInterval.
	 * <p/>
	 * <p>
	 * Interval is scheduled before the onClick completes, so it has to run
	 * fast. If it runs slow, it does not generate skipped onClicks.
	 */
	public static class RepeatListener implements View.OnTouchListener {

		private Handler handler = new Handler();

		private int initialInterval;
		private final int normalInterval;
		private final View.OnClickListener clickListener;

		private Runnable handlerRunnable = new Runnable() {
			@Override
			public void run() {
				if (downView == null) {
					return;
				}
				handler.removeCallbacksAndMessages(downView);
				handler.postAtTime(this, downView, SystemClock.uptimeMillis()
						+ normalInterval);
				clickListener.onClick(downView);
			}
		};

		private View downView;

		/**
		 * @param initialInterval
		 *            The interval before first click event
		 * @param normalInterval
		 *            The interval before second and subsequent click events
		 * @param clickListener
		 *            The OnClickListener, that will be called periodically
		 */
		public RepeatListener(int initialInterval, int normalInterval,
							  OnClickListener clickListener) {
			if (clickListener == null)
				throw new IllegalArgumentException("null runnable");
			if (initialInterval < 0 || normalInterval < 0)
				throw new IllegalArgumentException("negative interval");

			this.initialInterval = initialInterval;
			this.normalInterval = normalInterval;
			this.clickListener = clickListener;
		}

		public boolean onTouch(View view, MotionEvent motionEvent) {
			switch (motionEvent.getAction()) {
				case MotionEvent.ACTION_DOWN:
					downView = view;
					handler.removeCallbacks(handlerRunnable);
					handler.postAtTime(handlerRunnable, downView,
							SystemClock.uptimeMillis() + initialInterval);
					clickListener.onClick(view);
					return true;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_OUTSIDE:
					handler.removeCallbacksAndMessages(downView);
					downView = null;
					return true;
			}
			return false;
		}
	}

}
