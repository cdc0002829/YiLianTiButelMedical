package cn.redcdn.hvs.udtcenter.activity;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.officialaccounts.widget.ChildViewPager;
import cn.redcdn.hvs.responsedt.adapter.IndicatorExpandableListAdapter;
import cn.redcdn.hvs.udtcenter.fragment.RequestDtFragment;
import cn.redcdn.hvs.udtcenter.fragment.ResponseDtFragment;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.log.CustomLog;

public class UDTRoomActivity extends BaseActivity implements ViewPager.OnPageChangeListener {
    private Button requestDtBut;
    private Button responseDtBut;
    private Button backBtn;
    private View requestDtView;
    private View responseDtView;
    private List<Fragment> mFragments = new ArrayList<Fragment>();
    private MyPageAdapter myPageAdapter;
    private ChildViewPager udtRoomChildViewPager;
    private RequestDtFragment requestDtFragment;
    private ResponseDtFragment responseDtFragemnt;
    private onShowResponseDtFragmentListen mOnShowResponseDtFragmentListen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_udtroom);
        initView();
    }

    /**
     * 初始化控件
     */
    private void initView() {
        CustomLog.d(TAG, "initView");
        requestDtBut = (Button) findViewById(R.id.button_request_dt);
        responseDtBut = (Button) findViewById(R.id.button_response_dt);
        backBtn = (Button) findViewById(R.id.udtroom_back_btn);
        requestDtBut.setTextColor(getResources().getColor(R.color.press_color));
        requestDtBut.setOnClickListener(mbtnHandleEventListener);
        responseDtBut.setOnClickListener(mbtnHandleEventListener);
        backBtn.setOnClickListener(mbtnHandleEventListener);
        requestDtView = findViewById(R.id.request_dt_view);
        responseDtView = findViewById(R.id.response_dt_view);
        responseDtView.setVisibility(View.INVISIBLE);
        udtRoomChildViewPager = (ChildViewPager) findViewById(R.id.udt_room_view_pager);
        requestDtFragment = new RequestDtFragment();
        responseDtFragemnt = new ResponseDtFragment();
        mFragments.add(requestDtFragment);
        mFragments.add(responseDtFragemnt);
        myPageAdapter = new MyPageAdapter(UDTRoomActivity.this.getSupportFragmentManager(), mFragments);
        udtRoomChildViewPager.setAdapter(myPageAdapter);
        udtRoomChildViewPager.addOnPageChangeListener(this);

    }

    /**
     * 按钮的点击事件
     */
    @Override
    public void todoClick(int i) {
        super.todoClick(i);
        switch (i) {
            case R.id.button_request_dt:
                CustomLog.d(TAG, "点击request按钮，切换到requestDtFragement");
//                responseDtView.setVisibility(View.VISIBLE);
//                requestDtView.setVisibility(View.INVISIBLE);
//                responseDtBut.setTextColor(getResources().getColor(R.color.press_color));
//                requestDtBut.setTextColor(getResources().getColor(R.color.defalt_color));
                udtRoomChildViewPager.setCurrentItem(0);
                break;
            case R.id.button_response_dt:
                CustomLog.d(TAG, "点击response按钮，切换到responseDtFragement");
//                responseDtView.setVisibility(View.INVISIBLE);
//                requestDtView.setVisibility(View.VISIBLE);
//                responseDtBut.setTextColor(getResources().getColor(R.color.defalt_color));
//                requestDtBut.setTextColor(getResources().getColor(R.color.press_color));
                udtRoomChildViewPager.setCurrentItem(1);
                break;
            case R.id.udtroom_back_btn:
                CustomLog.d(TAG, "点击返回按钮");
                if (CommonUtil.isFastDoubleClick()) {
                    return;
                }
                UDTRoomActivity.this.finish();

            default:
                break;

        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    /**
     * 根据不同的select显示不同的ui
     *
     * @param position
     */
    @Override
    public void onPageSelected(int position) {
        if (position == 0) {
            CustomLog.d(TAG, "onPageSelected position == 0");
            requestDtView.setVisibility(View.VISIBLE);
            responseDtView.setVisibility(View.INVISIBLE);
            requestDtBut.setTextColor(getResources().getColor(R.color.press_color));
            responseDtBut.setTextColor(getResources().getColor(R.color.defalt_color));

        } else if (position == 1) {
            CustomLog.d(TAG, "onPageSelected position == 1");
            requestDtView.setVisibility(View.INVISIBLE);
            responseDtView.setVisibility(View.VISIBLE);
            requestDtBut.setTextColor(getResources().getColor(R.color.defalt_color));
            responseDtBut.setTextColor(getResources().getColor(R.color.press_color));
            mOnShowResponseDtFragmentListen.ShowResponseDtFragmentListen();
        }

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /**
     * viewpager的适配器
     */
    public class MyPageAdapter extends PagerAdapter {
        private List<Fragment> fragments;
        private FragmentManager manager;

        public MyPageAdapter(FragmentManager fm, List<Fragment> fragments) {
            super();
            manager = fm;
            this.fragments = fragments;
        }


        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = fragments.get(position);
            //判断当前的fragment是否已经被添加进入Fragmentanager管理器中
            if (!fragment.isAdded()) {
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.add(fragment, fragment.getClass().getSimpleName());
                //不保存系统参数，自己控制加载的参数
                transaction.commitAllowingStateLoss();
                //手动调用,立刻加载Fragment片段
                manager.executePendingTransactions();
            }
//            if (fragment.getView().getParent() == null) {
//                //添加布局
            container.addView(fragment.getView());
//            }

            return fragment.getView();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            //移除布局
            container.removeView(fragments.get(position).getView());
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    public void setOnShowResponseDtFragmentListen(onShowResponseDtFragmentListen showResponseDtFragmentListen) {
        mOnShowResponseDtFragmentListen = showResponseDtFragmentListen;
    }

    public interface onShowResponseDtFragmentListen {
        void ShowResponseDtFragmentListen();
    }
}
