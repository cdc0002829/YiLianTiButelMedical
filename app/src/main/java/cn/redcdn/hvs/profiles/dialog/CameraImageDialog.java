package cn.redcdn.hvs.profiles.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.redcdn.hvs.R;

/**
 * Created by Administrator on 2017/2/24.
 */

public class CameraImageDialog extends Dialog {
    private CameraClickListener cameraListener = null;
    private NoClickListener noListener = null;
    private PhoneClickListener phoneListener = null;
    private RelativeLayout camemaRl = null;
    private RelativeLayout phoneRl = null;
    private RelativeLayout noRl = null;
    private String mFirstName = null;
    private String mSecondName = null;
    private TextView firstTv;
    private TextView secondTv;
    private int mNumber = 0;
    private RelativeLayout titleLayout;
    private boolean mHasTitle = false;
    private String mTitle;
    private TextView titleView;

    public CameraImageDialog(Context context) {
        super(context, R.style.dialog);
    }

    public interface CameraClickListener {
        public void clickListener();
    }

    public CameraImageDialog(Context context, int theme) {
        super(context, theme);
    }

    public CameraImageDialog(Context context, int theme, String firstTitle, String secondTitle,int number,boolean hasTitle, String title) {
        super(context, theme);
        mFirstName = firstTitle;
        mSecondName = secondTitle;
        mNumber = number;
        mHasTitle = hasTitle;
        mTitle = title;
    }

    public interface NoClickListener {
        public void clickListener();
    }

    public interface PhoneClickListener {
        public void clickListener();
    }

    public void setCameraClickListener(CameraClickListener ok) {
        this.cameraListener = ok;
    }

    public void setNoClickListener(NoClickListener no) {
        this.noListener = no;
    }

    public void setPhoneClickListener(PhoneClickListener phone) {
        this.phoneListener = phone;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.cameraimagedialog);
        //par.setAlpha(40);
        camemaRl = (RelativeLayout) findViewById(R.id.carema_rl);
        camemaRl.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (cameraListener != null) {
                    cameraListener.clickListener();
                }
            }
        });
        phoneRl = (RelativeLayout) findViewById(R.id.photo_rl);
        phoneRl.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (phoneListener != null)
                    phoneListener.clickListener();
            }
        });
        noRl = (RelativeLayout) findViewById(R.id.cancel_rl);
        noRl.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (noListener != null)
                    noListener.clickListener();
            }
        });
        firstTv = (TextView) findViewById(R.id.tv_first);
        secondTv = (TextView) findViewById(R.id.tv_second);
        if(mFirstName!=null){
            firstTv.setText(mFirstName);
        }
        if(mSecondName!=null){
            secondTv.setText(mSecondName);
        }
        if(mNumber==1){
            camemaRl.setVisibility(View.GONE);
        }

        titleLayout = (RelativeLayout) findViewById(R.id.rl_title);
        titleView = (TextView) findViewById(R.id.tv_title);
        if(mHasTitle){
            titleLayout.setVisibility(View.VISIBLE);
            if(mTitle!=null){
                titleView.setText(mTitle);
            }
        }

    }
}
