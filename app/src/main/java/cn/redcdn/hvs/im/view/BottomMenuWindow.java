package cn.redcdn.hvs.im.view;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import cn.redcdn.hvs.R;
import cn.redcdn.hvs.im.util.IMCommonUtil;

/**
 * <dl>
 * <dt>BottomMenuWindow.java</dt>
 * <dd>Description:底部弹出式菜单窗口</dd>
 * <dd>Copyright: Copyright (C) 2013</dd>
 * <dd>Company: 安徽青牛信息技术有限公司</dd>
 * <dd>CreateDate: 2013-12-17 上午10:22:19</dd>
 * </dl>
 *
 * @author zhaguitao
 */
public class BottomMenuWindow {

    private Activity mActivity = null;

    private Dialog mDialog = null;

    private View contentView = null;

    public BottomMenuWindow(Activity activity) {
        mActivity = activity;
        mDialog = new Dialog(mActivity, R.style.transparentFrameWindowStyle);
    }

    public BottomMenuWindow(Activity activity, int res) {
        this(activity);
        setContentView(res);
    }

    public void setContentView(int res) {
        if(null == mDialog){
            return;
        }
        contentView = mActivity.getLayoutInflater().inflate(res, null);
        mDialog.setContentView(contentView, new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        Window window = mDialog.getWindow();
        // 设置显示动画
        window.setWindowAnimations(R.style.bottom_menu_windown_animstyle);

        // 设置窗口大小和位置
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.x = 0;
        wl.y = IMCommonUtil.getDeviceSize(mActivity).y;
        wl.width = IMCommonUtil.getDeviceSize(mActivity).x;
        mDialog.onWindowAttributesChanged(wl);
        // 点击窗口以外区域，关闭窗口
        mDialog.setCanceledOnTouchOutside(true);
    }

    public View getContentView() {
        return contentView;
    }

    public void show() {
        if(mDialog!=null){
            mDialog.show();
        }
    }

    public void dismiss() {
        if(mDialog!=null){
            mDialog.dismiss();
        }
    }

    public interface MenuClickedListener {
        public void onMenuClicked();
    }
}