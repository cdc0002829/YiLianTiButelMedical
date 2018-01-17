package cn.redcdn.hvs.contacts.contact;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.redcdn.hvs.contacts.contact.interfaces.Contact;
/***
 * 
 * @author LB 通讯录界面和编辑界面父类，不同子类实现自己需要的方法
 *
 */
public class ContactPagerGridViewAdapterBase extends BaseAdapter{
  
  protected int currentPage;
  protected LayoutInflater inflater;
  protected List<Contact> contacts;
  protected int pageCapacity;
  protected ContactPagerGridViewAdapterBase(Context context, int currentPage,
      List<Contact> contacts, int pageCapacity) {
    this.contacts = contacts;
    this.currentPage = currentPage;
    inflater = LayoutInflater.from(context);
    this.pageCapacity = pageCapacity;
  }
  @Override
  public int getCount() {
    return 0;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    return null;
  }
  
  @Override
  public Object getItem(int position) {
    return contacts.get(currentPage * pageCapacity + position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }
  
  public class ContactHolder {
    public ImageView rightTop = null;
    public ImageView rightTopnor = null;
    public TextView photoId = null;
    public TextView name = null;
    public TextView videonumber = null;
  }
}
