/*
 * Copyright 2014 Hieu Rocker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.redcdn.hvs.im.util.smileUtil;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import cn.redcdn.hvs.R;
import cn.redcdn.log.CustomLog;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Hieu Rocker (rockerhieu@gmail.com)
 */
class EmojiAdapter extends ArrayAdapter<Emojicon> {
    private boolean mUseSystemDefault = false;

    List<Emojicon> data = new ArrayList<Emojicon>();


    public EmojiAdapter(Context context, List<Emojicon> data) {
        super(context, R.layout.emojicon_item, data);
        mUseSystemDefault = false;
        this.data = data;
    }


    public EmojiAdapter(Context context, List<Emojicon> data, boolean useSystemDefault) {
        super(context, R.layout.emojicon_item, data);
        mUseSystemDefault = useSystemDefault;
    }


    public EmojiAdapter(Context context, Emojicon[] data) {
        super(context, R.layout.emojicon_item, data);
        mUseSystemDefault = false;
    }


    public EmojiAdapter(Context context, Emojicon[] data, boolean useSystemDefault) {
        super(context, R.layout.emojicon_item, data);
        mUseSystemDefault = useSystemDefault;
    }


    // 防止getview position = 0重复执行很多次
    private int mCount = 0;


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (0 == position) {
            // 里面就是正常的position
            mCount++;
        } else {
            mCount = 0;
        }

        if (mCount > 1) {
            // 临时的position=0
            return convertView;
        }

//        CustomLog.d("EmojiAdapter", "表情gridview adapter,position=" + position);
        View v = convertView;
        if (v == null) {
            v = View.inflate(getContext(), R.layout.emojicon_item, null);
            ViewHolder holder = new ViewHolder();
            //            holder.icon = (EmojiconTextView) v.findViewById(R.id.emojicon_icon);
            holder.iconIv = (ImageView) v.findViewById(R.id.icon_iv);
            //            holder.delete=(ImageView) v.findViewById(R.id.emojicon_del);

            //            holder.icon.setUseSystemDefault(mUseSystemDefault);
            v.setTag(holder);
        }

        ViewHolder holder = (ViewHolder) v.getTag();
        if (position == data.size()) {
            //			holder.icon.setVisibility(View.GONE);
            //			holder.delete.setVisibility(View.VISIBLE);

            holder.iconIv.setImageResource(R.drawable.icon_delete_normal);
        } else {
            //			holder.icon.setVisibility(View.VISIBLE);
            //			holder.delete.setVisibility(View.GONE);

            Emojicon emoji = getItem(position);
            //			holder.icon.setText(emoji.getEmoji());
            int resId = getContext().getResources().getIdentifier(
                Emojicon.getHexResName(emoji.getEmoji()), "drawable",
                getContext().getApplicationContext().getPackageName());
            holder.iconIv.setImageResource(resId);
        }
        return v;
    }


    @Override
    public int getCount() {
        return data.size() + 1;
    }


    static class ViewHolder {
        //        EmojiconTextView icon;
        ImageView iconIv;
        //        ImageView delete;
    }

}