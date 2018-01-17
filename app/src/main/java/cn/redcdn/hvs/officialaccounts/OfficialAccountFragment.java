package cn.redcdn.hvs.officialaccounts;

import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseFragment;
import cn.redcdn.hvs.officialaccounts.fragment.OrderFragment;
import cn.redcdn.hvs.officialaccounts.fragment.RecommondFragment;
import cn.redcdn.hvs.officialaccounts.widget.ChildViewPager;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.log.CustomLog;


/**
 * Created by thinkpad on 2017/2/14.
 */
public class OfficialAccountFragment extends BaseFragment implements View.OnClickListener, ViewPager.OnPageChangeListener {
    private final String TAG = "OfficialAccountFragment";
    private Button button_onOrder, button_recommond;
    private Fragment[] fragments;
    private int mIndex;
    private ChildViewPager official_vp;
    private List<Fragment> mFragments = new ArrayList<Fragment>();
    private View order_view, recommond_view;
    private MyPageAdapter myPageAdapter;
    OrderFragment orderFragment;
    RecommondFragment recommondFragment;
    private View view;


    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.officialaccount_fragment, null);
        CustomLog.d(TAG, "view被创建了才会走这个方法");
        button_onOrder = (Button) view.findViewById(R.id.button_onOrder);
        button_recommond = (Button) view.findViewById(R.id.button_recommond);
        button_recommond.setTextColor(getResources().getColor(R.color.press_color));
        order_view = view.findViewById(R.id.order_view);
        recommond_view = view.findViewById(R.id.recommond_view);
        order_view.setVisibility(View.INVISIBLE);
        recommond_view.setVisibility(View.VISIBLE);
        button_onOrder.setOnClickListener(this);
        button_recommond.setOnClickListener(this);

        official_vp = (ChildViewPager) view.findViewById(R.id.official_vp);
        //推荐页面
        recommondFragment = new RecommondFragment();
        //已订页面
        orderFragment = new OrderFragment();
        mFragments.add(recommondFragment);
        mFragments.add(orderFragment);
        myPageAdapter = new MyPageAdapter(getChildFragmentManager(), mFragments);
        official_vp.setAdapter(myPageAdapter);
        official_vp.addOnPageChangeListener(this);

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            //父已显示
            //CustomToast.show(MedicalApplication.context,"WODE",1);
            if (official_vp.getCurrentItem() == 0) { //推荐
                recommondFragment.updateState(true);
            } else if (official_vp.getCurrentItem() == 1) { //已订
                orderFragment.updateState(true);
            }
        } else {
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void initData() {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_recommond:
                order_view.setVisibility(View.VISIBLE);
                recommond_view.setVisibility(View.INVISIBLE);
                button_onOrder.setTextColor(getResources().getColor(R.color.press_color));
                button_recommond.setTextColor(getResources().getColor(R.color.defalt_color));
                official_vp.setCurrentItem(0);
                recommondFragment.updateState(true);
                break;
            case R.id.button_onOrder:
                order_view.setVisibility(View.INVISIBLE);
                recommond_view.setVisibility(View.VISIBLE);
                button_onOrder.setTextColor(getResources().getColor(R.color.defalt_color));
                button_recommond.setTextColor(getResources().getColor(R.color.press_color));
                official_vp.setCurrentItem(1);
                orderFragment.updateState(true);
                break;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (position == 0) {
            order_view.setVisibility(View.INVISIBLE);
            recommond_view.setVisibility(View.VISIBLE);
            button_onOrder.setTextColor(getResources().getColor(R.color.defalt_color));
            button_recommond.setTextColor(getResources().getColor(R.color.press_color));
            recommondFragment.updateState(true);
        } else if (position == 1) {
            order_view.setVisibility(View.VISIBLE);
            recommond_view.setVisibility(View.INVISIBLE);
            button_onOrder.setTextColor(getResources().getColor(R.color.press_color));
            button_recommond.setTextColor(getResources().getColor(R.color.defalt_color));
            orderFragment.updateState(true);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /**
     * ViewPager适配器
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
            if (fragment.getView().getParent() == null) {
                //添加布局
                container.addView(fragment.getView());
            }

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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
