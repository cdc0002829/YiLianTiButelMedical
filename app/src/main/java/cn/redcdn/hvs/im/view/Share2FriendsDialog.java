package cn.redcdn.hvs.im.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import cn.redcdn.hvs.R;
import cn.redcdn.log.CustomLog;

/**
 * 我页签中分享给朋友功能Dialog界面
 * Created by caiguo on 2017/5/26.
 */

public class Share2FriendsDialog {
    private String TAG = getClass().getName();
    private Context context;
    private Dialog dialog;
    private Window window;
    private View contentView;
    private int viewPosition = Gravity.BOTTOM;
    private LinearLayout share2weChat,share2qq,share2msg,dialogDismiss;

    public Share2FriendsDialog(Context context) {
        this.context = context;
        initView();
        initWidget();
    }

    public Share2FriendsDialog(Context context, int position){
        this.context = context;
        this.viewPosition = position;
        initView();
        initWidget();
    }

    private void initView(){
        if(viewPosition == Gravity.BOTTOM){
            dialog =  new Dialog(context, R.style.ActionSheetDialogStyle);
        }else {
            dialog = new android.app.AlertDialog.Builder(context).create();
        }
        if (null != dialog) {
            dialog.show();
            contentView = ((Activity) context).getLayoutInflater().inflate(
                    R.layout.share2friends_dalog_layout, null);
            dialog.setContentView(contentView, new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            window = dialog.getWindow();
            window.setGravity(viewPosition);
            dialog.setCanceledOnTouchOutside(true);
            // 设置窗口大小和位置
            WindowManager windowManager = ((Activity)context).getWindowManager();
            Display display = windowManager.getDefaultDisplay();
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width =display.getWidth(); // 设置宽度
            dialog.onWindowAttributesChanged(lp);
        }else{
            CustomLog.i(TAG,"Dialog==NULL");
        }
    }
    private void initWidget(){
        share2weChat= (LinearLayout) contentView.findViewById(R.id.share2wechat_ll);
        share2qq= (LinearLayout) contentView.findViewById(R.id.share2qq_ll);
        share2msg= (LinearLayout) contentView.findViewById(R.id.share2msg_ll);
        dialogDismiss= (LinearLayout) contentView.findViewById(R.id.share_to_friends_dismiss);
        dialogDismiss.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {

                dismiss();
            }
        });
    }
    public void setShare2weChatListener(final MenuClickedListener listener) {
        share2weChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (listener != null) {
                    listener.onMenuClicked();
                }
            }
        });
    }
    public void setShare2QQListener(final MenuClickedListener listener) {
        share2qq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (listener != null) {
                    listener.onMenuClicked();
                }
            }
        });
    }
    public void setShare2MsgListener(final MenuClickedListener listener) {
        share2msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (listener != null) {
                    listener.onMenuClicked();
                }
            }
        });
    }


    private void dismiss() {
        dialog.dismiss();
        context=null;
        dialog=null;
        window=null;
        contentView=null;
    }


    public interface MenuClickedListener {
         void onMenuClicked();
    }
}
