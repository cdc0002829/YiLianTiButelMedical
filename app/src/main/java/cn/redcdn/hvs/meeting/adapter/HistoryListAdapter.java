/*===================================================================
 * 南京青牛通讯技术有限公司
 * 日期：2015-9-28 下午5:33:49
 * 作者：zl
 * 版本：1.0.0
 * 版权：All rights reserved.
 *===================================================================
 * 修订日期           修订人               描述
 * 2015-9-28     zl      创建
 */
package cn.redcdn.hvs.meeting.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cn.redcdn.hvs.R;
import java.util.List;

public class HistoryListAdapter extends BaseAdapter {

  private List<String> list = null;
  private Context context;
  private String tag = MeetingListAdapter.class.getName();

  public HistoryListAdapter(List<String> meetingList, Context c) {
    list = meetingList;
    context = c;
  }

  @Override
  public int getCount() {
    return list.size();
  }

  @Override
  public Object getItem(int arg0) {
    // TODO Auto-generated method stub
    return list.get(arg0);
  }

  @Override
  public long getItemId(int arg0) {

    return arg0;
  }

  @Override
  public View getView(int arg0, View view, ViewGroup arg2) {
    ViewHolder holder = null;

    if (view == null) {
      holder = new ViewHolder();
      view = LayoutInflater.from(context).inflate(
          R.layout.history_item_meeting, null);
    
      holder.number = (TextView) view.findViewById(R.id.meetingid);
    
      view.setTag(holder);
    } else {
      holder = (ViewHolder) view.getTag();
    }
    Log.e("adapter", list.get(arg0));
    holder.number.setText(list.get(arg0));
    return view;
  }

  final static class ViewHolder {
    public TextView number;
  }

}
