package cn.redcdn.hvs.im.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.im.bean.ContactFriendBean;
import cn.redcdn.hvs.im.dao.GroupDao;
import cn.redcdn.hvs.im.view.RoundImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.List;

/**
 * Desc
 * Created by wangkai on 2017/3/4.
 */

public class SelectGroupAdapter extends BaseAdapter {

    private final String TAG = "SelectGroupAdapter";
    public static final int TYPE_ROUND = 1;
    private Context mContext;
    private List<ContactFriendBean> allGroupList;
    private LayoutInflater inflater;
    private GroupDao mgroupDao;


    public SelectGroupAdapter(Context context, List<ContactFriendBean> groupList, LayoutInflater inflater, GroupDao groupDao) {
        this.mContext = context;
        this.allGroupList = groupList;
        this.inflater = inflater;
        this.mgroupDao = groupDao;
    }


    @Override
    public int getCount() {
        return allGroupList.size();
    }


    @Override
    public Object getItem(int position) {
        return position;
    }


    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View groupView;
        ViewHolder viewHolder;
        if (convertView == null) {
            groupView = inflater.inflate(R.layout.activity_select_group_item, null);
            viewHolder = new ViewHolder();
            viewHolder.groupHeadView = (RoundImageView) groupView.findViewById(
                R.id.group_headurl_iv);
            viewHolder.groupName = (TextView) groupView.findViewById(R.id.group_name_tv);
            viewHolder.groupNumber = (TextView) groupView.findViewById(R.id.group_memeber_size_tv);
            groupView.setTag(viewHolder);
        } else {
            groupView = convertView;
            viewHolder = (ViewHolder) groupView.getTag();
        }
        ContactFriendBean bean = allGroupList.get(position);
        Glide.with(mContext)
            .load(bean.getHeadUrl())
            .centerCrop()
            .placeholder(R.drawable.group_icon)
            .diskCacheStrategy(DiskCacheStrategy.SOURCE).crossFade()
            .into(viewHolder.groupHeadView);

        int tempMemeberSize = mgroupDao.queryGroupMemberCnt(bean.getNubeNumber());
        String tmpStr = bean.getName();


        viewHolder.groupName.setText(ellipsisWords(tmpStr));
        viewHolder.groupNumber.setText("(" + tempMemeberSize + "人" + ")");

        return groupView;
    }

    //省略15字后面的内容并加省略号
    private String ellipsisWords(String tmpStr) {
        String ellipsisGroupName = "";
        if (tmpStr.length() > 15){
            ellipsisGroupName = tmpStr.substring(0,15);
            return ellipsisGroupName;
        }
        return tmpStr;
    }


    class ViewHolder {
        RoundImageView groupHeadView;
        TextView groupName;
        TextView groupNumber;
    }
}
