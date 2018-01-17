package cn.redcdn.hvs.contacts.contact;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.GridView;
import android.widget.Toast;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.contacts.contact.interfaces.Contact;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.log.CustomLog;
import java.util.ArrayList;
import java.util.List;

public class ContactPagerAdapterList extends ContactPagerAdapterBase {
  private List<ContactPagerGridViewAdapterList> mAdapterList;
 
  public ContactPagerAdapterList(Context context, List<Contact> contacts,
      int columns, int rows, boolean status) {
    super(context, contacts, columns, rows, status);
    this.mColumn = columns;
    this.mContext = context;
//    Contact c = new Contact();
//    contacts.add(c);
    this.mContacts = contacts;

    this.mPageCapacity = rows * columns;
    this.mAdapterList = new ArrayList<ContactPagerGridViewAdapterList>();
  }

  @Override
  public int getCount() {
    mPageCounts = (mContacts.size() ) / mPageCapacity;
    if ((mContacts.size() ) % mPageCapacity != 0) {
      mPageCounts += 1;
    }
    return mPageCounts;
  }

  @Override
  public Object instantiateItem(ViewGroup container, final int index) {
    final GridView gridView = (GridView) LayoutInflater.from(mContext).inflate(
        R.layout.layout_contact_viewpager, null);
    ContactPagerGridViewAdapterList adapter = new ContactPagerGridViewAdapterList(
        mContext, index, mContacts, mPageCapacity);
    if (!mAdapterList.contains(adapter)) {
      mAdapterList.add(adapter);
    }
    gridView.setFocusable(true);
    gridView.requestFocus();
    gridView.setNumColumns(mColumn);
    gridView.setAdapter(adapter);
    gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));

    gridView.setOnItemSelectedListener(new OnItemSelectedListener() {

      @Override
      public void onItemSelected(AdapterView<?> parent, View view,
          int position, long id) {
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
        if (mListener != null) {
          mListener.onNoItemSelected();
        }
      }
    });

    gridView.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view,
          int itemPosition, long id) {
      int absolutePosition = index * mPageCapacity + itemPosition;
        CustomLog.d("ContactPagerAdapterList onItemClick", "absolutePosition"+absolutePosition);
      }
    });

    gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
      @Override public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if(((mAdapterList.size()-1)*4+position+1)==mContacts.size()){
          CustomToast.show(mContext,"LAST:"+String.valueOf(position), Toast.LENGTH_LONG);
        }else{
          CustomToast.show(mContext,String.valueOf(position), Toast.LENGTH_LONG);
          mContacts.remove(position);
          notifyDataSetChanged();
        }

        return false;
      }
    });

    container.addView(gridView, 0);
    return gridView;
  }

  @Override
  public void notifyDataSetChanged() {
    super.notifyDataSetChanged();

    while (mPageCounts < mAdapterList.size()) {
      mAdapterList.remove(mAdapterList.size() - 1);
    }
    for (int i = 0; i < mAdapterList.size(); ++i) {
      ContactPagerGridViewAdapterList adapter = mAdapterList.get(i);
      adapter.notifyDataSetChanged();
    }
  }
}
