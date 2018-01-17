package cn.redcdn.hvs.contacts.contact.hpucontact;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.youth.banner.loader.ImageLoader;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.contacts.contact.interfaces.Contact;

/**
 * Created by caizx on 2017/11/22.
 */

public class HpuAdapter extends BaseAdapter {

    private int resourceId;
    private Context mContext;
    private List<Contact> mContacts;
    public HpuAdapter(Context context, int resourceid, List<Contact> objs){
        super();
        mContext = context;
        resourceId = resourceid;
        mContacts = objs;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public int getCount() {
        return mContacts.size();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Contact contact = mContacts.get(i);
        ViewHolder holder ;
        if (view == null){
            view = LayoutInflater.from(mContext).inflate(resourceId,null);
            holder = new ViewHolder();
            holder.headIV  = (ImageView) view.findViewById(R.id.headimage);
            holder.nameTV = (TextView)view.findViewById(R.id.itemTv);
            holder.hospTV = (TextView)view.findViewById(R.id.attendmeetingname);
            holder.depTV = (TextView)view.findViewById(R.id.showdepname) ;
            view.setTag(holder);
        }else {
             holder = (ViewHolder) view.getTag();
        }
        com.nostra13.universalimageloader.core.ImageLoader imageLoader = com.nostra13.universalimageloader.core.ImageLoader.getInstance();
        imageLoader.displayImage(contact.getPicUrl(),
                holder.headIV,
                MedicalApplication.shareInstance().options,
                null);
        holder.nameTV.setText(contact.getNickname());
        holder.hospTV.setText(contact.getWorkUnit().trim());
        holder.depTV.setText(contact.getDepartment());
        return view;
    }
    class  ViewHolder{
        ImageView headIV;
        TextView  nameTV;
        TextView  hospTV;
        TextView depTV;
    }
}
