package cn.redcdn.hvs.contacts.contact;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import cn.redcdn.buteldataadapter.DataAdapter;
import cn.redcdn.datacenter.enterprisecenter.data.AccountInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.contacts.contact.diaplayImageListener.DisplayImageListener;
import cn.redcdn.hvs.contacts.contact.interfaces.Contact;
import cn.redcdn.hvs.contacts.contact.manager.ContactManager;
import cn.redcdn.log.CustomLog;

public class ListViewAdapter extends DataAdapter  {

	private Context context;
	private ViewHolder viewHolder;
	private String indexStr =  "ABCDEFGHIJKLMNOPQRSTUVWXYZ" ;
	private DisplayImageListener mDisplayImageListener = null;
	private int msgCount;

	public ListViewAdapter(Context context,int msgCount) {
		super(context);
		this.context = context;
		this.msgCount = msgCount;
		mDisplayImageListener = new DisplayImageListener();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		int dtcount = AccountManager.getInstance(context).hpuList.size();
		Contact item = (Contact) getItem(position);
		String firstString=item.getFirstName();
		String preFirstString=null;
		if(!indexStr.contains(firstString)){
			firstString="#";
		}
		if(position==0||position==1||position==2|| position<=2+dtcount){
			preFirstString="";
		}else{
			preFirstString=((Contact) getItem(position-1)).getFirstName();
			if(!indexStr.contains(preFirstString)){
				preFirstString="#";
			}
		}

		viewHolder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.contactitemindex, null);
			viewHolder = new ViewHolder();
			viewHolder.indexTv = (TextView) convertView
					.findViewById(R.id.indexTv);
			viewHolder.contactItemIndex=(LinearLayout)convertView.findViewById(R.id.indexitem);
			viewHolder.itemTv = (TextView) convertView.findViewById(R.id.itemTv);
			viewHolder.headImage = (ImageView) convertView
					.findViewById(R.id.headimage);
			viewHolder.contactItemLine = (LinearLayout) convertView
					.findViewById(R.id.contact_item_line);
			viewHolder.contactItem = (LinearLayout) convertView
					.findViewById(R.id.ll_contact_item);
			viewHolder.newFriendsCountTv = (TextView) convertView
					.findViewById(R.id.tv_new_friends_count);
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

		if(position>0&&((Contact) getItem(position)).getNubeNumber()
				.equals(((Contact) getItem(position-1)).getNubeNumber())){
			viewHolder.contactItem.setVisibility(View.INVISIBLE);
			viewHolder.contactItem.setVisibility(View.GONE);
		} else {
			viewHolder.contactItem.setVisibility(View.VISIBLE);
		}

		viewHolder.contactItemLine.setVisibility(View.VISIBLE);
		if(position+1<getCount()){
			if(!firstString.equals(preFirstString)){
				viewHolder.contactItemLine.setVisibility(View.VISIBLE);
			}
		}
		if (position + 1 == getCount()) {
			viewHolder.contactItemLine.setVisibility(View.VISIBLE);
		}

		ImageLoader imageLoader = ImageLoader.getInstance();

		imageLoader.displayImage(item.getPicUrl(),
				viewHolder.headImage,
				MedicalApplication.shareInstance().options,
				mDisplayImageListener);

		viewHolder.itemTv.setText(item.getNickname());

		if(position==0){
			viewHolder.contactItemIndex.setVisibility(View.GONE);
		}

		if(item.getNubeNumber().equals("0")){
			viewHolder.headImage.setImageResource(R.drawable.contact_groupchat);
		}else if(item.getNubeNumber().equals("1")){
			viewHolder.headImage.setImageResource(R.drawable.contact_publicnumber);
		}else if(item.getNubeNumber().equals("2")){
			viewHolder.headImage.setImageResource(R.drawable.contact_new_friend);
		}
//		else if(item.getNubeNumber().equals(ContactManager.getInstance(context).customerServiceNum1)
//				||item.getNubeNumber().equals(ContactManager.getInstance(context).customerServiceNum2)){
//			viewHolder.headImage.setImageResource(R.drawable.contact_customservice);
//		}else if(item.getNubeNumber().equals(ContactManager.getInstance(context).customerServiceNum1)
//				||item.getNubeNumber().equals(ContactManager.getInstance(context).customerServiceNum2)){
//			viewHolder.headImage.setImageResource(R.drawable.contact_customservice);
//		}
		else if(dtcount>0){
			for (int i = 0;i<dtcount;i++){
				String str = String.valueOf(2+i+1);
				String str1 = item.getNubeNumber();
				if (item.getNubeNumber().equals(String.valueOf(2+i+1))){
					viewHolder.headImage.setImageResource(R.drawable.hpu_dt_icon);
				}
			}
		}


        CustomLog.d(TAG,"ListViewAdapter, position:"+String.valueOf(position));
		if(item.getNubeNumber().equals("2")){
			CustomLog.d(TAG,"getNotReadMsgSize, position:"+ String.valueOf(position));
			if(null==String.valueOf(msgCount)){
				CustomLog.d(TAG,"null==String.valueOf(FriendsManager.getInstance().getNotReadMsgSize())");
				msgCount = 0;
			}else if(msgCount<0){
				CustomLog.d(TAG,"count<0,count:"+msgCount);
				msgCount = 0;
			}else{
				CustomLog.d(TAG,"count:"+msgCount);
			}

			if(msgCount==0){
				viewHolder.newFriendsCountTv.setVisibility(View.INVISIBLE);
			}else{
				if(msgCount>99){
					viewHolder.newFriendsCountTv.setText("99+");
				}else{
					viewHolder.newFriendsCountTv.setText(String.valueOf(msgCount));
				}
				viewHolder.newFriendsCountTv.setVisibility(View.VISIBLE);
			}

		}else{
			viewHolder.newFriendsCountTv.setVisibility(View.INVISIBLE);
		}

		return convertView;
	}

	private class ViewHolder {
		private TextView indexTv;
		private TextView itemTv;
		private ImageView headImage;
		private LinearLayout contactItemLine;
		private LinearLayout contactItemIndex;
		private LinearLayout contactItem;
		private TextView newFriendsCountTv;
	}

}
