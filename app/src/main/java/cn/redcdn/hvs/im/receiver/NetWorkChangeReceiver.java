package cn.redcdn.hvs.im.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import cn.redcdn.log.CustomLog;

/**
 * Desc
 * Created by wangkai on 2017/8/29.
 */

public class NetWorkChangeReceiver extends BroadcastReceiver {

    public static final String NET_CHANGE = "net_change";
    //标记当前网络状态，0为无可用网络状态，1表示有。
    public static final String NET_TYPE = "net_type";

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager=(ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        Intent netIntent = new Intent(NET_CHANGE);
        if(networkInfo != null && networkInfo.isAvailable()){
            CustomLog.d("NetWorkChangeReceiver","network change，network type is "
                    + networkInfo.getTypeName());
            netIntent.putExtra(NET_TYPE,1);
        }else{
            CustomLog.d("NetWorkChangeReceiver","network change,no network");
            netIntent.putExtra(NET_TYPE,0);
        }
        context.sendBroadcast(netIntent);
    }
}
