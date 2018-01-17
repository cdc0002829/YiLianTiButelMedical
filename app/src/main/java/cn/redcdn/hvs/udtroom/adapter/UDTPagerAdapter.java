package cn.redcdn.hvs.udtroom.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;

/**
 * @author guoyx
 */

public class UDTPagerAdapter extends PagerAdapter {
    private List<Fragment> mFragmentList;
    private FragmentManager fragmentManager;


    public UDTPagerAdapter(FragmentManager fragmentManager, List<Fragment> mFragmentList) {
        super();
        this.fragmentManager = fragmentManager;
        this.mFragmentList = mFragmentList;
    }


    @Override
    public int getCount() {
        return mFragmentList.size();
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment currentFragment = mFragmentList.get(position);
        if (!currentFragment.isAdded()) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(currentFragment, currentFragment.getClass().getSimpleName());
            transaction.commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        }
        if (currentFragment.getView().getParent() == null) {
            container.addView(currentFragment.getView());
        }

        return currentFragment.getView();
    }


    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(mFragmentList.get(position).getView());
    }

}
