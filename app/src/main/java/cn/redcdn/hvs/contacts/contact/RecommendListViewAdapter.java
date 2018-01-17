package cn.redcdn.hvs.contacts.contact;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.contacts.contact.diaplayImageListener.DisplayImageListener;
import cn.redcdn.hvs.contacts.contact.interfaces.Contact;
import cn.redcdn.hvs.im.manager.FriendsManager;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.log.CustomLog;

public class RecommendListViewAdapter extends BaseAdapter {
  private final String TAG = RecommendListViewAdapter.this.getClass()
      .getSimpleName();
  private Context context;
  private List<Contact> list;
  private ViewHolder viewHolder;
  private DisplayImageListener mDisplayImageListener = null;

  public RecommendListViewAdapter(Context context, List<Contact> list) {
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
  public boolean isEnabled(int position) {
      return false;
  }

  @Override
  public View getView(final int position, View convertView, ViewGroup parent) {

    String item = list.get(position).getName();
    CustomLog.d(TAG, "item=" + item);
    viewHolder = new ViewHolder();
    if ((item.length() == 1 || item.equals(context.getString(R.string.latest_recommend)))
        && (list.get(position).getNubeNumber() == null)) {

      convertView = LayoutInflater.from(context).inflate(
          R.layout.recommendindex, null);
      viewHolder.indexTv = (TextView) convertView.findViewById(R.id.indexTv);
    } else {
        if (list.get(position).getNubeNumber() != null) {
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.recommenditem, null);
            viewHolder.itemTv = (TextView) convertView.findViewById(R.id.itemTv);
            viewHolder.attendmeetingname = (TextView) convertView
                    .findViewById(R.id.attendmeetingname);
            viewHolder.headImage = (ImageView) convertView
                    .findViewById(R.id.headimage);
            viewHolder.recommendaddTV = (TextView) convertView
                    .findViewById(R.id.tv_recommendadd);
            viewHolder.recommendItemLine = (LinearLayout) convertView
                    .findViewById(R.id.recommend_item_line);
            viewHolder.llItem = (LinearLayout) convertView.findViewById(R.id.llitem);

            if (position + 1 < list.size()) {
                if (list.get(position + 1).getNubeNumber() == null) {
                    CustomLog.d(TAG, "hello..........................................");
                    viewHolder.recommendItemLine.setVisibility(View.INVISIBLE);
                }
            }

            CustomLog.d(TAG, "list.get(position)=" + list.get(position).toString()
                    + "position=" + position);

            CustomLog.d(TAG, "list.get(position).getBeAdded()="
                    + list.get(position).getBeAdded());

            CustomLog.d(TAG, "viewHolder.headImage=" + viewHolder.headImage);
            if (list.get(position).getPicUrl() != null
                    && !list.get(position).getPicUrl().isEmpty()
                    && viewHolder.headImage != null) {

                ImageLoader imageLoader = ImageLoader.getInstance();
                imageLoader.displayImage(list.get(position).getPicUrl(),
                        viewHolder.headImage,
                        MedicalApplication.shareInstance().options,
                        mDisplayImageListener);
            }

             if(FriendsManager.getInstance().getFriendRelationByNubeNumber(list.get(position).getNubeNumber())
                     ==FriendsManager.RELATION_TYPE_BOTH){
                 viewHolder.recommendaddTV.setText(R.string.newfriend_has_passed_status);
                 viewHolder.recommendaddTV.setTextColor(context.getResources().getColor(R.color.btn_color_gray));
             }else if(FriendsManager.getInstance().getFriendRelationByNubeNumber(list.get(position).getNubeNumber())
                     ==FriendsManager.RELATION_TYPE_NEGATIVE){
                 viewHolder.recommendaddTV.setText(R.string.btn_add);
                 viewHolder.recommendaddTV.setTextColor(context.getResources().getColor(R.color.btn_color_black));
                              }else if(FriendsManager.getInstance().getFriendRelationByNubeNumber(list.get(position).getNubeNumber())
                     ==FriendsManager.RELATION_TYPE_NONE){
                 viewHolder.recommendaddTV.setText(R.string.btn_add);
                 viewHolder.recommendaddTV.setTextColor(context.getResources().getColor(R.color.btn_color_black));
             }else if(FriendsManager.getInstance().getFriendRelationByNubeNumber(list.get(position).getNubeNumber())
                     ==FriendsManager.RELATION_TYPE_POSITIVE){
                 viewHolder.recommendaddTV.setText(R.string.btn_add);
                 viewHolder.recommendaddTV.setTextColor(context.getResources().getColor(R.color.btn_color_black));
             }

            viewHolder.llItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(list!=null&&list.size()>0){
                        Intent intent = new Intent();
                        intent.setClass(context,ContactCardActivity.class);
                        intent.putExtra("searchType",String.valueOf(7));
                        intent.putExtra("contact", list.get(position));
                        intent.putExtra("REQUEST_CODE", ContactTransmitConfig.REQUEST_CONTACT_CODE);
                        context.startActivity(intent);
                    }else{
                        CustomToast.show(context,context.getString(R.string.user_info_null),1);
                    }

                }
            });


        }
    }
    if ((item.length() == 1 || item.equals(context.getString(R.string.latest_recommend)))
        && (list.get(position).getNubeNumber() == null)) {
      viewHolder.indexTv.setText(list.get(position).getName());
    } else {
      viewHolder.itemTv.setText(list.get(position).getNickname());
      viewHolder.attendmeetingname.setText(context.getString(R.string.mobile_linkman)+list.get(position).getName());
    }
    return convertView;
  }

  private class ViewHolder {
    private TextView indexTv;
    private TextView itemTv;
    private ImageView headImage;
    private LinearLayout recommendItemLine;
    private TextView attendmeetingname;
    private TextView recommendaddTV;
    private LinearLayout llItem;
  }

}
