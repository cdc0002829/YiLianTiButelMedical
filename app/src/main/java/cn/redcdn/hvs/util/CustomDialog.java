package cn.redcdn.hvs.util;

import android.app.Dialog;

import android.content.Context;

import android.graphics.Color;

import android.os.Bundle;

import android.view.Gravity;
import android.view.View;

import android.view.Window;

import android.widget.Button;

import android.widget.ImageView;

import android.widget.LinearLayout;

import android.widget.RelativeLayout;

import android.widget.TextView;


public class CustomDialog extends Dialog {

    // private CustomOperateListener mListener = null;

    private String mTitle = "提示"; // 对话框标题，若标题为null或空字符串，不显示标题栏

    private Context mContext;

    private String mBody = null; // 对话框内容

    public static final int INVALID_RES_ID = -1;

    private int imgResId = INVALID_RES_ID; // 背景图片Id

    private TextView bodyText = null;

    private Button leftBtn = null;

    private Button rightBtn = null;

    private Button centerBtn = null;

    private LinearLayout twoBtnLay = null;

    LinearLayout oneBtnLay = null;

    private boolean isWithCancelBtn = true;

    // private String[] mButtonItems = null;

    private String okBtnText = "确定";

    private String cancelBtnText = "取消";


    private OKBtnOnClickListener okBtnOnClickListener;

    private CancelBtnOnClickListener cancelBtnOnClickListener;


    private boolean useBlackTheme = false;

    private RelativeLayout customDialogBg;

    private TextView dialogTip;

    private ImageView vLine;

    private ImageView hLine;


    public interface OKBtnOnClickListener {

        public void onClick(CustomDialog customDialog);

    }


    public interface CancelBtnOnClickListener {

        public void onClick(CustomDialog customDialog);

    }


    /**
     * 构造方法，传入activity的上下文
     *
     * @param context
     */

    public CustomDialog(Context context) {

        this(context, MResource.getIdByName(context, MResource.STYLE, "jmetingsdk_dialog"));

    }


    public CustomDialog(Context context, int theme) {

        super(context, theme);

        mContext = context;

    }


    /**
     * 设置dialog的具体提示内容
     *
     * @param mBody
     */

    public void setTip(String mBody) {

        this.mBody = mBody;

    }


    /**
     * 设置dialog的标题
     *
     * @param mTitle 标题
     */

    public void setTitle(String mTitle) {

        this.mTitle = mTitle;

    }


    /**
     * 设置确认按钮的文字
     *
     * @param okBtnText
     */

    public void setOkBtnText(String okBtnText) {

        this.okBtnText = okBtnText;

    }


    /**
     * 设置取消按钮的文字
     *
     * @param cancelBtnText
     */

    public void setCancelBtnText(String cancelBtnText) {

        this.cancelBtnText = cancelBtnText;

    }


    public void removeCancelBtn() {

        isWithCancelBtn = false;

    }


    public void setBlackTheme() {

        useBlackTheme = true;

    }

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

        Window dialogWindow = this.getWindow();

        dialogWindow.setGravity(Gravity.CENTER);

        setCanceledOnTouchOutside(false);

        setContentView(MResource.getIdByName(mContext, MResource.LAYOUT, "jmeetingsdk_custom_operate_dialog"));


        customDialogBg = (RelativeLayout) findViewById(MResource.getIdByName(mContext, MResource.ID, "custom_dialog_bg"));

        dialogTip = (TextView) findViewById(MResource.getIdByName(mContext, MResource.ID, "qn_operate_dialog_body"));

        vLine = (ImageView) findViewById(MResource.getIdByName(mContext, MResource.ID, "custom_dialog_v_line"));

        hLine = (ImageView) findViewById(MResource.getIdByName(mContext, MResource.ID, "custom_dialog_h_line"));


        RelativeLayout layout = (RelativeLayout) findViewById(MResource.getIdByName(mContext, MResource.ID, "qn_operate_dialog_layout"));

        twoBtnLay = (LinearLayout) findViewById(MResource.getIdByName(mContext, MResource.ID, "two_button_layout"));

        oneBtnLay = (LinearLayout) findViewById(MResource.getIdByName(mContext, MResource.ID, "one_button_layout"));


        bodyText = (TextView) findViewById(MResource.getIdByName(mContext, MResource.ID, "qn_operate_dialog_body"));

        if (CustomDialog.INVALID_RES_ID != imgResId) {

            layout.setBackgroundResource(imgResId);

        }


        leftBtn = (Button) findViewById(MResource.getIdByName(mContext, MResource.ID, "qn_operae_dialog_left_button"));

        rightBtn = (Button) findViewById(MResource.getIdByName(mContext, MResource.ID, "qn_operae_dialog_right_button"));

        centerBtn = (Button) findViewById(MResource.getIdByName(mContext, MResource.ID, "qn_operae_dialog_center_button"));


        if (!isWithCancelBtn) {

            oneBtnLay.setVisibility(View.VISIBLE);

            twoBtnLay.setVisibility(View.GONE);

            centerBtn.setText(okBtnText);

            centerBtn.setOnClickListener(new View.OnClickListener() {

                @Override

                public void onClick(View v) {

                    if (okBtnOnClickListener != null) {

                        okBtnOnClickListener.onClick(CustomDialog.this);

                    }

                }

            });

        } else {

            oneBtnLay.setVisibility(View.GONE);

            twoBtnLay.setVisibility(View.VISIBLE);


            leftBtn.setText(cancelBtnText);


            rightBtn.setText(okBtnText);


            leftBtn.setOnClickListener(new View.OnClickListener() {

                @Override

                public void onClick(View v) {

                    if (cancelBtnOnClickListener != null) {

                        cancelBtnOnClickListener.onClick(CustomDialog.this);

                    }

                }

            });

            rightBtn.setOnClickListener(new View.OnClickListener() {

                @Override

                public void onClick(View v) {

                    if (okBtnOnClickListener != null) {

                        okBtnOnClickListener.onClick(CustomDialog.this);

                    }

                }

            });

        }


        if (useBlackTheme) {

            customDialogBg.setBackgroundDrawable(mContext.getResources().getDrawable(

                    MResource.getIdByName(mContext, MResource.DRAWABLE, "custom_dialog_bg_for_meeting_room")));

            customDialogBg.setPadding(0, 0, 0, 0);

            dialogTip.setTextColor(Color.argb(255, 200, 202, 204));

            vLine.setBackgroundColor(Color.argb(255, 37, 39, 41));

            hLine.setBackgroundColor(Color.argb(255, 37, 39, 41));

            leftBtn.setTextColor(Color.argb(255, 147, 149, 153));


            leftBtn.setBackgroundDrawable(mContext.getResources().getDrawable(

                    MResource.getIdByName(mContext, MResource.DRAWABLE, "custom_dialog_left_btn_selector_for_meeting_room")));

            rightBtn.setBackgroundDrawable(mContext.getResources().getDrawable(

                    MResource.getIdByName(mContext, MResource.DRAWABLE, "jmeetingsdk_custom_dialog_right_btn_selector_for_meeting_room")));

            centerBtn.setBackgroundDrawable(mContext.getResources().getDrawable(

                    MResource.getIdByName(mContext, MResource.DRAWABLE, "custom_dialog_center_btn_selector_for_meeting_room")));


        }


        if (null != mBody) {

            bodyText.setText(mBody);

        }


    }


    public void setOkBtnOnClickListener(OKBtnOnClickListener okBtnOnClickListener) {

        this.okBtnOnClickListener = okBtnOnClickListener;

    }


    public void setCancelBtnOnClickListener(

            CancelBtnOnClickListener cancelBtnOnClickListener) {

        this.cancelBtnOnClickListener = cancelBtnOnClickListener;

    }


    public void setCenterBtnText(String str) {

        okBtnText = str;

    }


}

