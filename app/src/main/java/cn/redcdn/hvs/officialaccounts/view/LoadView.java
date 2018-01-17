package cn.redcdn.hvs.officialaccounts.view;


import cn.redcdn.hvs.R;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class LoadView
{
	public static Dialog createLoadingDialog(Context context, String msg)
	{
	    LayoutInflater inflater = LayoutInflater.from(context);
	    View v = inflater.inflate(R.layout.loading_dialog, null);
	    RelativeLayout layout = (RelativeLayout) v.findViewById(R.id.dialog_view);
	    ImageView spaceshipImage = (ImageView) v.findViewById(R.id.img);
    
	    // TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);

	    Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(context,R.anim.loading_animation);

	    spaceshipImage.startAnimation(hyperspaceJumpAnimation);
/*
		if (msg == null || msg.equals("")) {
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.CENTER_IN_PARENT);
			spaceshipImage.setLayoutParams(params);
		}
*/
		//tipTextView.setText(msg);

	    Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);
	    loadingDialog.setCanceledOnTouchOutside(false);
	    loadingDialog.setCancelable(true);
	    loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));
	    // loadingDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR);
	    return loadingDialog;
	}

/*
	public static Dialog createLoadingDialog(Context context, String msg,DialogInterface.OnCancelListener listener)
	{
	    LayoutInflater inflater = LayoutInflater.from(context);
	    View v = inflater.inflate(R.layout.loading_dialog, null);
	    RelativeLayout layout = (RelativeLayout) v.findViewById(R.id.dialog_view);
	
	    ImageView spaceshipImage = (ImageView) v.findViewById(R.id.img);
	    TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);
	
	    Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(context,R.anim.loading_animation);
	
	    if (msg == null || msg.equals("")) {
	      RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	      params.addRule(RelativeLayout.CENTER_IN_PARENT);
	      spaceshipImage.setLayoutParams(params);
	    }
	
	    spaceshipImage.startAnimation(hyperspaceJumpAnimation);
	    tipTextView.setText(msg);
	
	    Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);
	
	    loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));
	
	    loadingDialog.setCanceledOnTouchOutside(false);
	
	    if (listener != null) {
	      loadingDialog.setOnCancelListener(listener);
	    }
	
	    return loadingDialog;
	}
*/
}
