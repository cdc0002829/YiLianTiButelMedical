package cn.redcdn.hvs.contacts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import cn.redcdn.buteldataadapter.DataAdapter;
import cn.redcdn.datacenter.offaccscenter.data.OffAccdetailInfo;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.contacts.contact.diaplayImageListener.DisplayImageListener;
import cn.redcdn.hvs.im.view.RoundImageView;

public class ContactsPublicNumberListViewAdapter extends DataAdapter  {

    private Context context;
    private ViewHolder viewHolder;
    private String indexStr =  "ABCDEFGHIJKLMNOPQRSTUVWXYZ" ;

    private DisplayImageListener mDisplayImageListener = null;

    private List<OffAccdetailInfo> list = null;

    public ContactsPublicNumberListViewAdapter(Context context, List<OffAccdetailInfo> list) {
        super(context);
        this.context = context;
        this.list = list;
        mDisplayImageListener = new DisplayImageListener();

    }

  @Override
  public int getCount() {
    return list.size();
  }

  @Override
  public Object getItem(int position) {
    return list.get(position);
  }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        OffAccdetailInfo item = (OffAccdetailInfo) getItem(position);
        String firstString=item.getNameSpell();
        String preFirstString=null;
        String backFirstString=null;
        if(!indexStr.contains(firstString)){
            firstString="#";
        }
        if(position==0){
            preFirstString="#";
        }else{
            preFirstString=((OffAccdetailInfo) getItem(position-1)).getNameSpell();
            if(!indexStr.contains(preFirstString)){
                preFirstString="#";
            }
        }
        if(position+1<getCount()){
            backFirstString=((OffAccdetailInfo) getItem(position+1)).getNameSpell();
            if(!indexStr.contains(backFirstString)){
                backFirstString="#";
            }
        }

        viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.public_number_contactitemindex, null);
            viewHolder = new ViewHolder();
            viewHolder.indexTv = (TextView) convertView
                    .findViewById(R.id.indexTv);
            viewHolder.contactItemIndex=(LinearLayout)convertView.findViewById(R.id.indexitem);
            viewHolder.itemTv = (TextView) convertView.findViewById(R.id.itemTv);
            viewHolder.headImage = (RoundImageView) convertView
                    .findViewById(R.id.public_number_headimage);
            viewHolder.contactItemLine = (LinearLayout) convertView
                    .findViewById(R.id.contact_item_line);
            viewHolder.contactItem = (LinearLayout) convertView
                    .findViewById(R.id.ll_contact_item);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (position == 0) {
            viewHolder.indexTv.setVisibility(View.VISIBLE);
            viewHolder.itemTv.setVisibility(View.VISIBLE);
            viewHolder.contactItemIndex.setVisibility(View.VISIBLE);
            viewHolder.headImage.setVisibility(View.VISIBLE);
            viewHolder.contactItemLine.setVisibility(View.VISIBLE);
            viewHolder.indexTv.setText(firstString);
        }else if(!firstString.equals(preFirstString)){
            viewHolder.indexTv.setVisibility(View.VISIBLE);
            viewHolder.itemTv.setVisibility(View.VISIBLE);
            viewHolder.contactItemIndex.setVisibility(View.VISIBLE);
            viewHolder.headImage.setVisibility(View.VISIBLE);
            viewHolder.contactItemLine.setVisibility(View.VISIBLE);
            viewHolder.indexTv.setText(firstString);
        }else{
            viewHolder.indexTv.setVisibility(View.VISIBLE);
            viewHolder.itemTv.setVisibility(View.VISIBLE);
            viewHolder.headImage.setVisibility(View.VISIBLE);
            viewHolder.contactItemLine.setVisibility(View.VISIBLE);
            viewHolder.contactItemIndex.setVisibility(View.INVISIBLE);
            viewHolder.contactItemIndex.setVisibility(View.GONE);
        }

        viewHolder.contactItem.setVisibility(View.GONE);

        viewHolder.contactItemLine.setVisibility(View.VISIBLE);
        if(position+1<getCount()){
            if(!firstString.equals(backFirstString)){
                viewHolder.contactItemLine.setVisibility(View.VISIBLE);
            }
        }
        if (position + 1 == getCount()) {
            viewHolder.contactItemLine.setVisibility(View.VISIBLE);
        }

        ImageLoader imageLoader = ImageLoader.getInstance();

        imageLoader.displayImage(item.getLogoUrl(),
                viewHolder.headImage,
                MedicalApplication.shareInstance().options,
                mDisplayImageListener);

        viewHolder.itemTv.setText(item.getName());

        viewHolder.contactItem.setVisibility(View.VISIBLE);

        return convertView;
    }

    private class ViewHolder {
        private TextView indexTv;
        private TextView itemTv;
        private RoundImageView headImage;
        private LinearLayout contactItemLine;
        private LinearLayout contactItemIndex;
        private LinearLayout contactItem;

    }

}
