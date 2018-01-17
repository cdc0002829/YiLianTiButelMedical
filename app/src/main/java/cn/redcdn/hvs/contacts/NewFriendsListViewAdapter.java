package cn.redcdn.hvs.contacts;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.contacts.contact.ContactCardActivity;
import cn.redcdn.hvs.contacts.contact.diaplayImageListener.DisplayImageListener;
import cn.redcdn.hvs.im.bean.FriendInfo;
import cn.redcdn.hvs.im.column.StrangerMessageTable;
import cn.redcdn.hvs.im.manager.FriendsManager;
import cn.redcdn.hvs.im.view.BottomMenuWindow;
import cn.redcdn.hvs.im.view.MedicalAlertDialog;
import cn.redcdn.log.CustomLog;

/**
 * Created by Administrator on 2017/5/4 0004.
 */

public class NewFriendsListViewAdapter extends BaseAdapter {

    protected final String TAG = getClass().getName();
    private Context context;
    private NewFriendsListViewAdapter.ViewHolder viewHolder;
    private List<FriendInfo> list = null;
    private DisplayImageListener mDisplayImageListener = null;

    public interface buttonClick {
        void itemClicked(View v,int position);
    }
    private buttonClick bc;

    public interface buttonLongClick {
        void itemLongClicked(View v,int position);
    }
    private buttonLongClick blc;

    public NewFriendsListViewAdapter(Context context, List<FriendInfo> list, buttonClick bc, buttonLongClick blc){
        this.context = context;
        this.list = list;
        this.bc = bc;
        this.blc = blc;
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
        viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.new_friends_item, null);
            viewHolder = new ViewHolder();
            viewHolder.ivImage = (ImageView) convertView.findViewById(R.id.iv_new_friends_item_image);
            viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_new_friends_item_name);
            viewHolder.tvMessage = (TextView) convertView.findViewById(R.id.tv_new_friends_item_message);
            viewHolder.btnAccept = (Button) convertView.findViewById(R.id.btn_new_friends_item_accept);
            viewHolder.llItem = (LinearLayout) convertView.findViewById(R.id.ll_new_friends_item);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if(null!=list.get(position).getName()){

            String name = "";

            if(list.get(position).getName().length()>12){
                name = list.get(position).getName()
                        .replace(list.get(position).getName(),
                                list.get(position).getName()
                                        .substring(0,11)+"...");
            }else{
                name = list.get(position).getName();

            }
            viewHolder.tvName.setText(name);
        }else{
            viewHolder.tvName.setText("");
        }

        ImageLoader imageLoader = ImageLoader.getInstance();

        imageLoader.displayImage(list.get(position).getHeadUrl(),
                viewHolder.ivImage,
                MedicalApplication.shareInstance().options,
                mDisplayImageListener);

        Cursor c = FriendsManager.getInstance().getFriendValidationMsg(list.get(position).getNubeNumber());

        if(null != c && c.moveToFirst()){
            viewHolder.tvMessage.setText(c.getString(c.getColumnIndex(StrangerMessageTable.MSG_CONTENT)));

            String text;

            if(null!=c.getString(c.getColumnIndex(StrangerMessageTable.MSG_CONTENT))){
                if(c.getString(c.getColumnIndex(StrangerMessageTable.MSG_CONTENT)).length()>25){
                    text = c.getString(c.getColumnIndex(StrangerMessageTable.MSG_CONTENT))
                            .replace(c.getString(c.getColumnIndex(StrangerMessageTable.MSG_CONTENT)),
                                    c.getString(c.getColumnIndex(StrangerMessageTable.MSG_CONTENT))
                                            .substring(0,24)+"...");
                }else{
                    text = c.getString(c.getColumnIndex(StrangerMessageTable.MSG_CONTENT));
                }

                viewHolder.tvMessage.setText(text);

            }else{
                viewHolder.tvMessage.setText("");
            }

        }else{
            viewHolder.tvMessage.setText("");
        }


        viewHolder.btnAccept.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                if (MotionEvent.ACTION_DOWN == event.getAction()) {
                    if (bc != null) {
                        CustomLog.d(TAG, "新的朋友列表点击button");
                        bc.itemClicked(v, position);
                        return true;
                    }
                }
                return false;
            }
        });
        int result = FriendsManager.getInstance().getFriendRelationByNubeNumber(list.get(position).getNubeNumber());
        if(result==FriendInfo.RELATION_TYPE_POSITIVE){
            viewHolder.btnAccept.setText(R.string.to_verify);
            viewHolder.btnAccept.setTextColor(context.getResources().getColor(R.color.btn_color_gray));
        }else if(result==FriendInfo.RELATION_TYPE_BOTH){
            viewHolder.btnAccept.setText(R.string.newfriend_has_passed_status);
            viewHolder.btnAccept.setTextColor(context.getResources().getColor(R.color.btn_color_gray));
        }else if(result==FriendInfo.RELATION_TYPE_NONE){
            viewHolder.btnAccept.setText(R.string.btn_add);
            viewHolder.btnAccept.setTextColor(context.getResources().getColor(R.color.btn_color_blue));
        }else if(result==FriendInfo.RELATION_TYPE_NEGATIVE){
            viewHolder.btnAccept.setText(R.string.receive);
            viewHolder.btnAccept.setTextColor(context.getResources().getColor(R.color.btn_color_blue));
        }

        viewHolder.llItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomLog.d(TAG, "新的朋友列表点击item");
                if(null!=list&&list.size()>0){
                    Intent intent = new Intent();
                    intent.setClass(context, ContactCardActivity.class);
                    intent.putExtra("nubeNumber",list.get(position).getNubeNumber());
                    context.startActivity(intent);
                }
            }
        });

        viewHolder.llItem.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(final View v) {
                // 显示 删除 菜单
                MedicalAlertDialog menuDlg = new MedicalAlertDialog(v.getContext(),
                        Gravity.CENTER_VERTICAL);
                menuDlg.setShowFlag();
                menuDlg.addButtonSecond(new BottomMenuWindow.MenuClickedListener() {
                    @Override
                    public void onMenuClicked() {
                        //调friendmanager的删除好友记录（数据库记录）接口
                        FriendsManager.getInstance().deleteFriendRecord(list.get(position).getNubeNumber());
                        if (blc != null) {
                            CustomLog.d(TAG, "新的朋友列表点击button");
                            blc.itemLongClicked(v, position);
                        }
                    }
                }, context.getString(R.string.chat_delete));
                menuDlg.show();
                return true;
            }

        });

        return convertView;
    }

    private class ViewHolder {
        ImageView ivImage;
        TextView tvName;
        TextView tvMessage;
        Button btnAccept;
        LinearLayout llItem;
    }

}
