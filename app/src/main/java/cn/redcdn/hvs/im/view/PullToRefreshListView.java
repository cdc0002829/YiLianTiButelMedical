package cn.redcdn.hvs.im.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.DateUtil;
import cn.redcdn.log.CustomLog;

import static cn.redcdn.hvs.MedicalApplication.context;

public class PullToRefreshListView extends ListView implements OnScrollListener {
	
	private static final String TAG = "PullToRefreshContactListView";

    private final static int RELEASE_To_REFRESH = 0;
    private final static int PULL_To_REFRESH = 1;
    private final static int REFRESHING = 2;
    private final static int DONE = 3;
    private final static int LOADING = 4;

    // 实际的padding的距离与界面上偏移距离的比例
    private final static int RATIO = 3;

    private LayoutInflater inflater;

    private LinearLayout headView;

    private TextView tipsTextview;
    private TextView lastUpdatedTextView;
    private ImageView arrowImageView;
    private ProgressBar progressBar;

    private RotateAnimation animation;
    private RotateAnimation reverseAnimation;

    // 用于保证startY的值在一个完整的touch事件中只被记录一次
    private boolean isRecored;

    private int headContentWidth;
    private int headContentHeight;

    private int startY;
    private int firstItemIndex;

    private int state;

    private boolean isBack;

    private OnRefreshListener refreshListener;

    private boolean isRefreshable;
    
    private final static int MESSAGE_TIME_OUT=1000;
    private int mTimeOut=0;//定义TimeOut <=0无超时时间，>0表示超时时间的ms数
    
    private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MESSAGE_TIME_OUT:
				CustomLog.d(TAG,"超时"+mTimeOut+"ms,取消显示");
				onRefreshComplete();
				break;
			default:
				break;
			}
		}
	};	
    
	public PullToRefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
        setCacheColorHint(context.getResources().getColor(
                android.R.color.transparent));
        inflater = LayoutInflater.from(context);

        headView = (LinearLayout) inflater.inflate(
                R.layout.pull_to_refresh_header, null);

        arrowImageView = (ImageView) headView
                .findViewById(R.id.head_arrowImageView);
        arrowImageView.setMinimumWidth(70);
        arrowImageView.setMinimumHeight(50);
        progressBar = (ProgressBar) headView
                .findViewById(R.id.head_progressBar);
        tipsTextview = (TextView) headView.findViewById(R.id.head_tipsTextView);
        lastUpdatedTextView = (TextView) headView
                .findViewById(R.id.head_lastUpdatedTextView);

        measureView(headView);
        headContentHeight = headView.getMeasuredHeight();
        headContentWidth = headView.getMeasuredWidth();

        headView.setPadding(0, -1 * headContentHeight, 0, 0);
        headView.invalidate();

        Log.v("size", "width:" + headContentWidth + " height:"
                + headContentHeight);

        addHeaderView(headView, null, false);
        setOnScrollListener(this);

        animation = new RotateAnimation(0, -180,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(250);
        animation.setFillAfter(true);

        reverseAnimation = new RotateAnimation(-180, 0,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        reverseAnimation.setInterpolator(new LinearInterpolator());
        reverseAnimation.setDuration(200);
        reverseAnimation.setFillAfter(true);

        

        state = DONE;
        isRefreshable = false;
    }
	
	/**
	 * 设置超时时间 ms数（>0:超时时间，<=0不做超时处理）
	 * @param timeOut ms数
	 */
	public void setTimeOut(int timeOut) {
		CustomLog.d(TAG,"set timeOut="+timeOut);
		this.mTimeOut = timeOut;
	}

    public void onScroll(AbsListView arg0, int firstVisiableItem, int arg2,
            int arg3) {
        firstItemIndex = firstVisiableItem;
    }

    public void onScrollStateChanged(AbsListView arg0, int arg1) {
    }

    public boolean onTouchEvent(MotionEvent event) {

        firstItemIndex = getFirstVisiblePosition();

        if (isRefreshable) {
            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (firstItemIndex == 0 && !isRecored) {
                    isRecored = true;
                    startY = (int) event.getY();
                    CustomLog.d(TAG, "在down时候记录当前位置");
                }
                break;

            case MotionEvent.ACTION_UP:

                if (state != REFRESHING && state != LOADING) {
                    if (state == DONE) {
                        // 什么都不做
                    }
                    if (state == PULL_To_REFRESH) {
                        state = DONE;
                        changeHeaderViewByState();

                        CustomLog.d(TAG, "由下拉刷新状态，到done状态");
                    }
                    if (state == RELEASE_To_REFRESH) {
                        state = REFRESHING;
                        changeHeaderViewByState();
                        onRefresh();

                        CustomLog.d(TAG, "由松开刷新状态，到done状态");
                    }
                }

                isRecored = false;
                isBack = false;

                break;

            case MotionEvent.ACTION_MOVE:
                int tempY = (int) event.getY();

                if (!isRecored && firstItemIndex == 0) {
                    CustomLog.d(TAG, "在move时候记录下位置");
                    isRecored = true;
                    startY = tempY;
                }

                if (state != REFRESHING && isRecored && state != LOADING) {

                    // 保证在设置padding的过程中，当前的位置一直是在head，否则如果当列表超出屏幕的话，当在上推的时候，列表会同时进行滚动

                    // 可以松手去刷新了
                    if (state == RELEASE_To_REFRESH) {

                        setSelection(0);

                        // 往上推了，推到了屏幕足够掩盖head的程度，但是还没有推到全部掩盖的地步
                        if (((tempY - startY) / RATIO < headContentHeight)
                                && (tempY - startY) > 0) {
                            state = PULL_To_REFRESH;
                            changeHeaderViewByState();

                            CustomLog.d(TAG, "由松开刷新状态转变到下拉刷新状态");
                        }
                        // 一下子推到顶了
                        else if (tempY - startY <= 0) {
                            state = DONE;
                            changeHeaderViewByState();

                            CustomLog.d(TAG, "由松开刷新状态转变到done状态");
                        }
                        // 往下拉了，或者还没有上推到屏幕顶部掩盖head的地步
                        else {
                            // 不用进行特别的操作，只用更新paddingTop的值就行了
                        }
                    }
                    // 还没有到达显示松开刷新的时候,DONE或者是PULL_To_REFRESH状态
                    if (state == PULL_To_REFRESH) {

                        setSelection(0);

                        // 下拉到可以进入RELEASE_TO_REFRESH的状态
                        if ((tempY - startY) / RATIO >= headContentHeight) {
                            state = RELEASE_To_REFRESH;
                            isBack = true;
                            changeHeaderViewByState();

                            CustomLog.d(TAG, "由done或者下拉刷新状态转变到松开刷新");
                        }
                        // 上推到顶了
                        else if (tempY - startY <= 0) {
                            state = DONE;
                            changeHeaderViewByState();

                            CustomLog.d(TAG, "由DOne或者下拉刷新状态转变到done状态");
                        }
                    }

                    // done状态下
                    if (state == DONE) {
                        if (tempY - startY > 0) {
                            state = PULL_To_REFRESH;
                            changeHeaderViewByState();
                        }
                    }

                    // 更新headView的size
                    if (state == PULL_To_REFRESH) {
                        headView.setPadding(0, -1 * headContentHeight
                                + (tempY - startY) / RATIO, 0, 0);

                    }

                    // 更新headView的paddingTop
                    if (state == RELEASE_To_REFRESH) {
                        headView.setPadding(0, (tempY - startY) / RATIO
                                - headContentHeight, 0, 0);
                    }

                }

                break;
            }
        }

        return super.onTouchEvent(event);
    }

    // 当状态改变时候，调用该方法，以更新界面
    private void changeHeaderViewByState() {
        switch (state) {
        case RELEASE_To_REFRESH:
            arrowImageView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            tipsTextview.setVisibility(View.GONE);
            lastUpdatedTextView.setVisibility(View.GONE);

            arrowImageView.clearAnimation();
            arrowImageView.startAnimation(animation);

            tipsTextview.setText(CommonUtil.getString(R.string.pull_to_refresh_for_release));

            CustomLog.d(TAG, "当前状态，松开刷新");
            break;
        case PULL_To_REFRESH:
            progressBar.setVisibility(View.GONE);
            tipsTextview.setVisibility(View.GONE);
            lastUpdatedTextView.setVisibility(View.GONE);
            arrowImageView.clearAnimation();
            arrowImageView.setVisibility(View.GONE);
            // 是由RELEASE_To_REFRESH状态转变来的
            if (isBack) {
                isBack = false;
                arrowImageView.clearAnimation();
                arrowImageView.startAnimation(reverseAnimation);

                tipsTextview.setText(CommonUtil.getString(R.string.pull_to_refresh_for_drop_down));
            } else {
                tipsTextview.setText(CommonUtil.getString(R.string.pull_to_refresh_for_drop_down));
            }
            CustomLog.d(TAG, context.getResources().getString(R.string.with));
            break;

        case REFRESHING:

            headView.setPadding(0, 0, 0, 0);

            progressBar.setVisibility(View.GONE);
            arrowImageView.clearAnimation();
            arrowImageView.setVisibility(View.GONE);
            tipsTextview.setText(CommonUtil.getString(R.string.pull_to_refresh_for_now));
            lastUpdatedTextView.setVisibility(View.GONE);

            CustomLog.d(TAG, "当前状态,正在刷新...");
            break;
        case DONE:
            headView.setPadding(0, -1 * headContentHeight, 0, 0);

            progressBar.setVisibility(View.GONE);
            arrowImageView.clearAnimation();
            arrowImageView.setImageResource(R.drawable.pull_arrow_icon);
            tipsTextview.setText(CommonUtil.getString(R.string.pull_to_refresh_for_drop_down));
            lastUpdatedTextView.setVisibility(View.GONE);

            CustomLog.d(TAG, "当前状态，done");
            break;
        }
    }

    public void setOnRefreshListener(OnRefreshListener refreshListener) {
        this.refreshListener = refreshListener;
        isRefreshable = true;
    }

    public interface OnRefreshListener {
        public void onRefresh();
    }

    public void onRefreshComplete() {
    	mHandler.removeMessages(MESSAGE_TIME_OUT);
    	state = DONE;
        lastUpdatedTextView
                .setText(CommonUtil.getString(R.string.pull_to_refresh_for_time_prefix)
                        + DateUtil
                                .getCurrentTimeSpecifyFormat(DateUtil.FORMAT_HH_MM_SS));
        changeHeaderViewByState();
    }

    public void onUnRefreshComplete() {
    	mHandler.removeMessages(MESSAGE_TIME_OUT);
        state = DONE;
        changeHeaderViewByState();
    }

    private void onRefresh() {
        if (refreshListener != null) {
            refreshListener.onRefresh();
        }
        
        if (mTimeOut>0){
        	CustomLog.d(TAG,"发"+mTimeOut+"超时");
        	mHandler.sendEmptyMessageDelayed(MESSAGE_TIME_OUT, mTimeOut);
        }
    }

    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
                    MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0,
                    MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    public void setAdapter(BaseAdapter adapter) {
        lastUpdatedTextView
                .setText(CommonUtil.getString(R.string.pull_to_refresh_for_time_prefix)
                        + DateUtil
                                .getCurrentTimeSpecifyFormat(DateUtil.FORMAT_HH_MM_SS));
        super.setAdapter(adapter);
    }
}
