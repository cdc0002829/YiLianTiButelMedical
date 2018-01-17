package cn.redcdn.hvs.contacts.contact;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.contacts.contact.diaplayImageListener.DisplayImageListener;
import cn.redcdn.hvs.contacts.contact.interfaces.Contact;
import cn.redcdn.hvs.contacts.contact.manager.ContactManager;
import cn.redcdn.log.CustomLog;
import com.nostra13.universalimageloader.core.ImageLoader;
import java.util.List;


/***
 * 
 * @author LB   通讯录界面GridViewAdapter
 *
 */ 
public class ContactPagerGridViewAdapterList extends ContactPagerGridViewAdapterBase {

  private final String TAG = ContactPagerAdapterList.class.getSimpleName();
  private Context mContext;
  private DisplayImageListener mDisplayImageListener = null;
  public ContactPagerGridViewAdapterList(Context context, int currentPage,
                                         List<Contact> contacts, int pageCapacity) {
    super(context, currentPage, contacts, pageCapacity);
    this.mContext = context;
    mDisplayImageListener = new DisplayImageListener();
  }
  
  @Override
  public int getCount() {
    if (contacts.size() < pageCapacity) {
      return contacts.size() ;
    }
    if (pageCapacity * (currentPage + 1) <= (contacts.size() )) {
      return pageCapacity;
    } else {
      return contacts.size() - pageCapacity * (currentPage);
    }
  }
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    final int currentIndex = currentPage * pageCapacity + position;
    ContactHolder holder = null;
    
      if (convertView == null) {
        
        convertView = inflater.inflate(R.layout.layout_contact_viewpager_item,
            null);
        holder = new ContactHolder();
        holder.rightTop = (ImageView) convertView
            .findViewById(R.id.contact_viewpager_iamgehead);
        holder.rightTop.setVisibility(View.VISIBLE);
        holder.rightTop.bringToFront();

        CustomLog.d(TAG, "viewHolder.headImage="+holder.rightTop);
        
        convertView.setTag(holder);
      } else {
        holder = (ContactHolder) convertView.getTag();
      }

//    if(currentIndex==contacts.size()-1){
//      holder.rightTop.setImageResource(R.drawable.contact_customservice);
//    }else{
      ImageLoader imageLoader = ImageLoader.getInstance();
      imageLoader.displayImage(contacts.get(currentIndex).getPicUrl(),
          holder.rightTop,
          MedicalApplication.shareInstance().options,
          mDisplayImageListener);
//    }

    if(contacts.get(currentIndex).getNubeNumber()!=null){
      if(contacts.get(currentIndex).getNubeNumber().equals(ContactManager.getInstance(mContext).customerServiceNum1)
          ||contacts.get(currentIndex).getNubeNumber().equals(ContactManager.getInstance(mContext).customerServiceNum2)){
        holder.rightTop.setImageResource(R.drawable.contact_customservice);
      }else if(contacts.get(currentIndex).getNubeNumber().equals(ContactManager.getInstance(mContext).customerServiceNum1)
          ||contacts.get(currentIndex).getNubeNumber().equals(ContactManager.getInstance(mContext).customerServiceNum2)){
        holder.rightTop.setImageResource(R.drawable.contact_customservice);
      }
    }

    return convertView;
  }
}
