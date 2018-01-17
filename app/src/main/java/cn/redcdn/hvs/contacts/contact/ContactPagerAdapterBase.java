package cn.redcdn.hvs.contacts.contact;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.redcdn.hvs.contacts.contact.interfaces.Contact;

public class ContactPagerAdapterBase extends PagerAdapter {
  protected List<ContactPagerGridViewAdapterBase> mAdapter;
  protected List<Contact> mContacts;
  protected ContactPagerListener mListener;
  protected GridView mCurrentView;
  protected int mPageCounts;
  protected int mPageCapacity;
  protected int mColumn;
  protected View lastView = null;
  protected Context mContext = null;


  public ContactPagerAdapterBase(Context context, List<Contact> contacts,
      int columns, int rows, boolean status) {
    this.mColumn = columns;
    this.mContext = context;
    this.mContacts = contacts;
    this.mPageCapacity = rows * columns;
    this.mAdapter = new ArrayList<ContactPagerGridViewAdapterBase>();
  }

  @Override
  public int getCount() {
    return 0;
  }

  public void setListener(ContactPagerListener listener) {
    this.mListener = listener;
  }

  public void setFocusChange(final GridView gridView,
      final Map<View, Contact> map) {
    gridView.setOnFocusChangeListener(new View.OnFocusChangeListener() {

      @Override
      public void onFocusChange(View v, boolean hasFocus) {

      }
    });
  }

  public GridView getCurrentView() {
    return mCurrentView;
  }

  @Override
  public boolean isViewFromObject(View arg0, Object arg1) {
    return arg0 == arg1;
  }

  @Override
  public void destroyItem(ViewGroup container, int position, Object view) {
    ((ViewPager) container).removeView((GridView) view);
  }

  @Override
  public int getItemPosition(Object object) {
    return PagerAdapter.POSITION_NONE;
  }

  @Override
  public void setPrimaryItem(ViewGroup container, int position, Object object) {
    mCurrentView = (GridView) object;
  }

  public interface ContactPagerListener {
    /**
     * 点击通讯录联系人卡片上的回调函数
     * 
     * @param contact
     *          当前点击的联系人数据信息
     * @param gridView
     *          当前点击的联系人�?��的grid
     * @param position
     *          当前点击的联系人在gridview上的位置
     */

    public void onItemClick(Contact contact, GridView gridView,
                            int position, View lastView, Contact lastSelectBO, int page,
                            int absolutePosition);

    public void onNoItemSelected();

  }
}
