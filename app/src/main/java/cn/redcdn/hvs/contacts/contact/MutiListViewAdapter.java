package cn.redcdn.hvs.contacts.contact;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import cn.redcdn.buteldataadapter.DataAdapter;
import cn.redcdn.datacenter.hpucenter.data.DTInfo;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.contacts.contact.diaplayImageListener.DisplayImageListener;
import cn.redcdn.hvs.contacts.contact.interfaces.Contact;
import cn.redcdn.hvs.contacts.contact.manager.ContactManager;


public class MutiListViewAdapter extends DataAdapter {

  private Context context;
  private ViewHolder viewHolder;
  private String indexStr =  "ABCDEFGHIJKLMNOPQRSTUVWXYZ" ;
  private DisplayImageListener mDisplayImageListener = null;

  public MutiListViewAdapter(Context context) {
	super(context);
    this.context = context;
    mDisplayImageListener = new DisplayImageListener();
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(final int position, View convertView, ViewGroup parent) {
	Contact item = (Contact) getItem(position);
	String firstString = item.getFirstName();
	String preFirstString = null;
	String backFirstString = null;
	if (!indexStr.contains(firstString)) {
		firstString = "#";
	}
	if (position == 0) {
		preFirstString = "#";
	} else {
		preFirstString = ((Contact) getItem(position - 1)).getFirstName();
		if (!indexStr.contains(preFirstString)) {
			preFirstString = "#";
		}
	}
	if (position + 1 < getCount()) {
		backFirstString = ((Contact) getItem(position + 1)).getFirstName();
		if (!indexStr.contains(backFirstString)) {
			backFirstString = "#";
		}
	}
	viewHolder = null;
	if (convertView == null) {
		convertView = LayoutInflater.from(context).inflate(
				R.layout.holdmeetingindex, null);
		viewHolder = new ViewHolder();
		viewHolder.indexTv = (TextView) convertView
				.findViewById(R.id.indexTv);
		viewHolder.contactItemIndex = (LinearLayout) convertView
				.findViewById(R.id.indexItem);
		viewHolder.itemTv = (TextView) convertView
				.findViewById(R.id.itemTv);
		viewHolder.headImage = (ImageView) convertView
				.findViewById(R.id.hold_meeting_headimage);
		viewHolder.holdmeetinItemLine = (LinearLayout) convertView
				.findViewById(R.id.holdmeeting_item_line);
		viewHolder.cbCheck=(ImageView)convertView.findViewById(R.id.cbselect);
		viewHolder.cbUnCheck=(ImageView)convertView.findViewById(R.id.cbunselect);
		viewHolder.holdMeetingIndex = (LinearLayout)convertView.findViewById(R.id.ll_hold_meeting_index);
		convertView.setTag(viewHolder);
	} else {
		viewHolder = (ViewHolder) convertView.getTag();
	}
	if (position == 0) {
		viewHolder.indexTv.setVisibility(View.VISIBLE);
		viewHolder.itemTv.setVisibility(View.VISIBLE);
		viewHolder.contactItemIndex.setVisibility(View.VISIBLE);
		viewHolder.headImage.setVisibility(View.VISIBLE);
		viewHolder.holdmeetinItemLine.setVisibility(View.VISIBLE);
		viewHolder.indexTv.setText(firstString);
	} else if (!firstString.equals(preFirstString)) {
		viewHolder.indexTv.setVisibility(View.VISIBLE);
		viewHolder.itemTv.setVisibility(View.VISIBLE);
		viewHolder.contactItemIndex.setVisibility(View.VISIBLE);
		viewHolder.headImage.setVisibility(View.VISIBLE);
		viewHolder.holdmeetinItemLine.setVisibility(View.VISIBLE);
		viewHolder.indexTv.setText(firstString);
	} else {
		viewHolder.indexTv.setVisibility(View.VISIBLE);
		viewHolder.itemTv.setVisibility(View.VISIBLE);
		viewHolder.headImage.setVisibility(View.VISIBLE);
		viewHolder.holdmeetinItemLine.setVisibility(View.VISIBLE);
		viewHolder.contactItemIndex.setVisibility(View.INVISIBLE);
		viewHolder.contactItemIndex.setVisibility(View.GONE);
	}

	if(position>0&&((Contact) getItem(position)).getNubeNumber()
			.equals(((Contact) getItem(position-1)).getNubeNumber())){
		viewHolder.holdMeetingIndex.setVisibility(View.INVISIBLE);
		viewHolder.holdMeetingIndex.setVisibility(View.GONE);
	} else {
		viewHolder.holdMeetingIndex.setVisibility(View.VISIBLE);
	}

	viewHolder.holdmeetinItemLine.setVisibility(View.VISIBLE);
	if (position + 1 < getCount()) {
		if (!firstString.equals(backFirstString)) {
			viewHolder.holdmeetinItemLine.setVisibility(View.INVISIBLE);
		}
	}
	if (position + 1 == getCount()) {
		viewHolder.holdmeetinItemLine.setVisibility(View.INVISIBLE);
	}

	ImageLoader imageLoader = ImageLoader.getInstance();
	imageLoader.displayImage(item.getPicUrl(), 
			viewHolder.headImage,
			MedicalApplication.shareInstance().options,
			mDisplayImageListener);
	
	if ((mExpandMap.get(item.getNubeNumber())).isSelected) {
		viewHolder.cbCheck.setVisibility(View.INVISIBLE);
		viewHolder.cbUnCheck.setVisibility(View.VISIBLE);
	} else {
		viewHolder.cbCheck.setVisibility(View.VISIBLE);
		viewHolder.cbUnCheck.setVisibility(View.INVISIBLE);
	}

	viewHolder.itemTv.setText(item.getNickname());
      List<DTInfo> mLsit  = ContactManager.getInstance(context).getDTListFromData();
	  int count = mLsit.size();
	  if(position==0||position==1||position==2||position<=2+count){
		  viewHolder.holdMeetingIndex.setVisibility(View.GONE);
	  }

	  if(item.getNubeNumber().equals(ContactManager.getInstance(context).customerServiceNum1)
			  ||item.getNubeNumber().equals(ContactManager.getInstance(context).customerServiceNum2)){
		  viewHolder.headImage.setImageResource(R.drawable.contact_customservice);
	  }else if(item.getNubeNumber().equals(ContactManager.getInstance(context).customerServiceNum1)
			  ||item.getNubeNumber().equals(ContactManager.getInstance(context).customerServiceNum2)){
		  viewHolder.headImage.setImageResource(R.drawable.contact_customservice);
	  }

	return convertView;
}

  private class ViewHolder {
    private TextView indexTv;
    private TextView itemTv;
    private ImageView cbCheck;
    private ImageView cbUnCheck;
    private ImageView headImage;
    private LinearLayout holdmeetinItemLine;
    private LinearLayout contactItemIndex;
    private LinearLayout holdMeetingIndex;
  }

}
