package cn.redcdn.hvs.profiles.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.WindowManager;

/**
 * Created by Administrator on 2017/2/27.
 */

public abstract class SoundCheckDialog extends Dialog implements
        DialogInterface.OnDismissListener {

    public SoundCheckDialog(Context context) {
        super(context);
    }

    public SoundCheckDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backPressed();
    }

    public abstract void backPressed();

    @Override
    public void show() {
        super.show();
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = 696;
        params.height = 466;
        getWindow().setAttributes(params);
    }
}
