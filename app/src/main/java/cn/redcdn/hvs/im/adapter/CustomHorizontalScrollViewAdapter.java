package cn.redcdn.hvs.im.adapter;


import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import cn.redcdn.hvs.R;
import cn.redcdn.hvs.im.bean.ContactFriendBean;
import cn.redcdn.hvs.im.bean.ShowNameUtil;
import cn.redcdn.hvs.im.util.IMCommonUtil;
import cn.redcdn.hvs.im.view.RoundCornerImageView;
import cn.redcdn.hvs.util.CommonUtil;

/**
 *
 * <dl>
 * <dt>CustomHorizontalScrollViewAdapter.java</dt>
 * <dd>Description:</dd>
 */

public class CustomHorizontalScrollViewAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<ContactFriendBean> mDatas = null;

    public CustomHorizontalScrollViewAdapter(Context context,
                                             List<ContactFriendBean> mDatas) {
        this.mContext = context;
        mInflater = LayoutInflater.from(context);
        this.mDatas = mDatas;
    }

    public void setData(List<ContactFriendBean> mDatas) {
        this.mDatas = mDatas;
    }

    public int getCount() {
        return mDatas.size();
    }

    public Object getItem(int position) {
        return mDatas.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(
                    R.layout.select_linkman_gallery_item, parent, false);
            viewHolder.mImg = (RoundCornerImageView) convertView
                    .findViewById(R.id.id_index_gallery_item_image);
            viewHolder.mText = (TextView) convertView
                    .findViewById(R.id.id_index_gallery_item_text);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (mDatas != null && getCount() > 0) {
            int headId= IMCommonUtil.getHeadIdBySex(mDatas.get(position).getSex());
            Glide.with(mContext)
                    .load(mDatas.get(position).getHeadUrl())
                    .placeholder(headId)
                    .error(headId).centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE).crossFade()
                    .into(viewHolder.mImg);

            // 显示名称：
            ShowNameUtil.NameElement element = ShowNameUtil.getNameElement(
                    mDatas.get(position).getName(), mDatas.get(position)
                            .getNickname(), mDatas.get(position).getNumber(),
                    mDatas.get(position).getNubeNumber());
            String MName = ShowNameUtil.getShowName(element);
            viewHolder.mText.setText(MName);
        }
        return convertView;
    }

    private class ViewHolder {
        RoundCornerImageView mImg;
        TextView mText;
    }
}
