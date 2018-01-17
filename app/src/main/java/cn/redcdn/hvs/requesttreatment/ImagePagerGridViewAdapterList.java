package cn.redcdn.hvs.requesttreatment;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.contacts.contact.ContactPagerAdapterList;
import cn.redcdn.hvs.contacts.contact.ContactPagerGridViewAdapterBase;
import cn.redcdn.hvs.contacts.contact.diaplayImageListener.DisplayImageListener;
import cn.redcdn.hvs.contacts.contact.interfaces.Contact;
import cn.redcdn.hvs.im.view.RoundImageView;
import cn.redcdn.log.CustomLog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.List;

/**
 * Created by Administrator on 2017/12/4 0004.
 */

public class ImagePagerGridViewAdapterList extends ContactPagerGridViewAdapterBase {

    private final String TAG = ContactPagerAdapterList.class.getSimpleName();
    private Context mContext;
    private DisplayImageListener mDisplayImageListener = null;
    private boolean mHasDefaultImage = true;
    public ImagePagerGridViewAdapterList(Context context, int currentPage,
                                           List<Contact> contacts, int pageCapacity,boolean hasDefaultImage) {
        super(context, currentPage, contacts, pageCapacity);
        this.mContext = context;
        mDisplayImageListener = new DisplayImageListener();
        mHasDefaultImage = hasDefaultImage;
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
        ViewHolder holder = null;

        if (convertView == null) {

            convertView = inflater.inflate(R.layout.layout_image_viewpager_item,
                null);
            holder = new ViewHolder();
            holder.rightTop = (RoundImageView) convertView
                .findViewById(R.id.image_viewpager_iamgehead);
            holder.rightTop.setVisibility(View.VISIBLE);
            holder.rightTop.bringToFront();

            CustomLog.d(TAG, "viewHolder.headImage="+holder.rightTop);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if(mHasDefaultImage&&(currentIndex==contacts.size()-1)){
            holder.rightTop.setBackgroundResource(R.drawable.patient_condition_add_picture);
        }else{

            if(!TextUtils.isEmpty(contacts.get(currentIndex).getNubeNumber())){
                Glide.with(mContext)
                    .load(contacts.get(currentIndex).getNubeNumber())
                    .placeholder(R.drawable.empty_photo)
                    .error(R.drawable.empty_photo)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .crossFade().into(holder.rightTop);
            }else {
                Glide.with(mContext)
                    .load(contacts.get(currentIndex).getHeadUrl())
                    .placeholder(R.drawable.empty_photo)
                    .error(R.drawable.empty_photo)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .crossFade().into(holder.rightTop);
            }

        }

        if(currentIndex==9){
            holder.rightTop.setVisibility(View.GONE);
        }else{
            holder.rightTop.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    public class ViewHolder {
        public RoundImageView rightTop = null;
    }

}

