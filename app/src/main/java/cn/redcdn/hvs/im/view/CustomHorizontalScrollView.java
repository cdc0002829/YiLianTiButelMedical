package cn.redcdn.hvs.im.view;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import cn.redcdn.hvs.im.adapter.CustomHorizontalScrollViewAdapter;
import cn.redcdn.hvs.im.util.IMCommonUtil;

/**
 *
 * <dl>
 * <dt>CustomHorizontalScrollView.java</dt>
 * <dd>Description:自定义横向滚动条CustomHorizontalScrollView</dd>
 * </dl>
 */

public class CustomHorizontalScrollView extends HorizontalScrollView implements OnClickListener{

    /**
     * HorizontalListView中的LinearLayout
     */
    private LinearLayout mContainer;

    /**
     * 子元素的宽度
     */
    private int mChildWidth;
    /**
     * 子元素的高度
     */
    private int mChildHeight;
    /**
     * 当前最后一张图片的index
     */
    private int mCurrentIndex;
    /**
     * 当前第一张图片的下标
     */
    private int mFristIndex;
    /**
     * 数据适配器
     */
    private CustomHorizontalScrollViewAdapter mAdapter;
    /**
     * 每屏幕最多显示的个数
     */
    private int mCountOneScreen;
    /**
     * 屏幕的宽度
     */
    private int mScreenWitdh;

    /**
     * 保存View与位置的键值对
     */
    private Map<View, Integer> mViewPos = new HashMap<View, Integer>();
    /**
     * 图片个数
     */
    private int picCount = 0;
    /**
     * 是否删减，ture： 删减 false： 增添
     */
    private boolean isDelete = false;
    /**
     * 顶部横向滚动条是首联接还是尾连接， ture： 首联接 false：尾连接
     */
    private boolean isFrontInsert = false;

    /**极会议版本：为了省去measure childWidth计算任务，以及防止计算时出错，此处将xml文件中的宽度传值进来*/
    /** 头像之间间距 */
    private int space = 0;

    public void setSpace(int _space) {
        this.space = _space;
    }

    public int getSpace() {
        return space;
    }
    /** 头像宽度 */
    private int iconWidth=0;
    public void setIconWidth(int _iconWidth){
        this.iconWidth=_iconWidth;
    }
    public int getIconWidth(){
        return iconWidth;
    }

    /**
     * 图片滚动时的回调接口
     */
    public interface CurrentImageChangeListener {
        void onCurrentImgChanged(int position, View viewIndicator);
    }

    /**
     * 条目点击时的回调
     *
     */
    public interface OnItemClickListener {
        void onClick(View view, int pos);
    }

    private CurrentImageChangeListener mListener;

    private OnItemClickListener mOnClickListener;

    // private boolean lastisDelete = false;//记录上一次是否为删减操作和添加操作的分割点
    public CustomHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 获得屏幕宽度
        mScreenWitdh = IMCommonUtil.getScreenWidth(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mContainer = (LinearLayout) getChildAt(0);
    }

    public void setCount(int sum) {
        picCount = sum;
    }

    public void setIsDelete(Boolean isDelete2) {
        this.isDelete = isDelete2;
    }

    public void setIsFrontInsert(Boolean frontInsert) {
        this.isFrontInsert = frontInsert;
    }

    /**
     * 滑动时的回调
     */
    public void notifyCurrentImgChanged() {
        // 先清除所有的背景色，点击时会设置为相应颜色
        for (int i = 0; i < mContainer.getChildCount(); i++) {
            // mContainer.getChildAt(i).setBackgroundColor(Color.WHITE);
        }

        mListener.onCurrentImgChanged(mFristIndex, mContainer.getChildAt(0));

    }

    /**
     * 初始化数据，设置数据适配器
     *
     * @param mAdapter
     */
    public void initDatas(CustomHorizontalScrollViewAdapter mAdapter) {
        this.mAdapter = mAdapter;
        mContainer = (LinearLayout) getChildAt(0);
        // 获得适配器中第一个View
        final View view = mAdapter.getView(0, null, mContainer);
        mContainer.addView(view);

        // 计算当前View的宽和高
//		if (mChildWidth == 0 && mChildHeight == 0) {
//			int w = View.MeasureSpec.makeMeasureSpec(0,
//					View.MeasureSpec.UNSPECIFIED);
//			int h = View.MeasureSpec.makeMeasureSpec(0,
//					View.MeasureSpec.UNSPECIFIED);
//			view.measure(w, h);
//			mChildWidth = view.getMeasuredWidth();
//			// 计算每次加载多少个View
//			mCountOneScreen = mScreenWitdh / (mChildWidth + space);
//			// mCountOneScreen = mScreenWitdh / mChildWidth;
//		}
//		LogUtil.d("mChildWidth=" + mChildWidth + "|interval=" + space
//				+ "|mCountOneScreen=" + mCountOneScreen);
        // 初始化第一屏幕的元素
        initFirstScreenChildren(picCount);
    }

    /**
     * 加载第一屏的View
     *
     * @param
     */
    public void initFirstScreenChildren(final int count) {
        mContainer = (LinearLayout) getChildAt(0);
        mContainer.removeAllViews();
        mViewPos.clear();

        for (int i = 0; i < count; i++) {
            View view = mAdapter.getView(i, null, mContainer);
            view.setOnClickListener(this);
            if (isFrontInsert) {
                mContainer.addView(view, 0);
            } else {
                mContainer.addView(view);
            }
            mViewPos.put(view, i);
            mCurrentIndex = i;
        }
        // 当元素个数大于满屏时，左移显示出最后一条
//		if (count > mCountOneScreen && !isFrontInsert) {
//			// if (!isFrontInsert) {
        mContainer.post(new Runnable() {
            @Override
            public void run() {
                //留100px预额
                smoothScrollBy(((count + 1) * (space+iconWidth)+100), 0);
//					 scrollTo((count + 1) * mChildWidth+34, 0);
            }
        });
//		}
        if (mListener != null) {
            notifyCurrentImgChanged();
        }

    }

    // public void addPicView() {
    // View view = mAdapter.getView(, null, mContainer);
    // view.setOnClickListener(this);
    // if (isFrontInsert) {
    // mContainer.addView(view, 0);
    // } else {
    // mContainer.addView(view);
    // }
    // mViewPos.put(view, picCount);
    // // 当元素个数大于满屏时，左移显示出最后一条
    // if (picCount > mCountOneScreen && !isFrontInsert) {
    // mContainer.post(new Runnable() {
    // @Override
    // public void run() {
    // smoothScrollBy(getChildAt(0).getWidth(), 0);
    // // scrollTo((count + 1) * mChildWidth, 0);
    // }
    // });
    // }
    // if (mListener != null) {
    // notifyCurrentImgChanged();
    // }
    //
    // }
    // public void removePicView(int position) {
    // View view = mAdapter.getView(position, null, mContainer);
    // mViewPos.remove(view);
    // mContainer.removeView(view);
    // if (mListener != null) {
    // notifyCurrentImgChanged();
    // }
    //
    // }
    @Override
    public void onClick(View v) {
        if (mOnClickListener != null) {
            for (int i = 0; i < mContainer.getChildCount(); i++) {
                mContainer.getChildAt(i).setBackgroundColor(Color.WHITE);
            }
            mOnClickListener.onClick(v, mViewPos.get(v));
        }
    }

    public void setOnItemClickListener(OnItemClickListener mOnClickListener) {
        this.mOnClickListener = mOnClickListener;
    }

    public void setCurrentImageChangeListener(
            CurrentImageChangeListener mListener) {
        this.mListener = mListener;
    }
}
