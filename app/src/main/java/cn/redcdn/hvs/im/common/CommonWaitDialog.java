package cn.redcdn.hvs.im.common;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.ViewGroup.LayoutParams;

import cn.redcdn.hvs.R;

/**
 * Desc  异步加载等待对话框
 * Created by wangkai on 2017/2/24.
 */

public class CommonWaitDialog {

    private View aniView;

    private ImageView btn;

    private RotateAnimation mRotateAnimation;

    private Dialog dialog = null;

    private TextView tvShowContent;

    public CommonWaitDialog(Context context , String alertMsg) {
        super();

        aniView = LayoutInflater.from(context).inflate(
                R.layout.wait_dialog_layout, null);
        btn = (ImageView) aniView.findViewById(R.id.btn_animation);
        tvShowContent = (TextView) aniView.findViewById(R.id.tv_animation);
        tvShowContent.setText(alertMsg);
        mRotateAnimation = new RotateAnimation(0.0F, 360.0F, 1, 0.5F, 1, 0.5F);
        mRotateAnimation.setFillAfter(false);
        mRotateAnimation.setRepeatCount(Animation.INFINITE);
        mRotateAnimation.setDuration(800);
        mRotateAnimation.setInterpolator(new LinearInterpolator());

        dialog = new Dialog(context, R.style.transparentFrameWindowStyle);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        dialog.addContentView(aniView, params);
        dialog.setCancelable(false);
        if (dialog != null) {
            dialog.show();

            btn.post(new Runnable() {

                @Override
                public void run() {

                    btn.startAnimation(mRotateAnimation);

                }
            });
        }
    }

    public void startAnimation() {
    }

    public boolean isShowing() {
        if (dialog != null) {
            return dialog.isShowing();
        }
        return false;
    }

    public void clearAnimation() {

        if (dialog != null && dialog.isShowing()) {
            btn.clearAnimation();
            dialog.dismiss();
            dialog = null;
        }
    }
}
