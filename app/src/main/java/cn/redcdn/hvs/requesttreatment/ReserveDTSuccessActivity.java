package cn.redcdn.hvs.requesttreatment;

/**
 * Created by Administrator on 2017/11/18 0018.
 */

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.udtcenter.activity.UDTRoomActivity;
import cn.redcdn.hvs.util.TitleBar;

public class ReserveDTSuccessActivity extends BaseActivity {

    private Context mContext;
    private TitleBar titleBar;
    private TextView completeView;
    private TextView closeView;
    private TextView reserveNumberView;
    private TextView reserveTimeView;
    private NewCurInfo newCurInfo = new NewCurInfo();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve_treatment_success);
        initWidget();
    }

    private void initWidget() {
        mContext = this;
        initTitleBar();
        completeView = (TextView) findViewById(R.id.tv_complete);
        closeView = (TextView) findViewById(R.id.tv_close);
        reserveNumberView = (TextView) findViewById(R.id.riv_reserve_number);
        reserveTimeView = (TextView) findViewById(R.id.tv_reserve_time);
        completeView.setOnClickListener(mbtnHandleEventListener);
        closeView.setOnClickListener(mbtnHandleEventListener);
        newCurInfo = (NewCurInfo)getIntent().getSerializableExtra("newCurInfo");
        reserveNumberView.setText(newCurInfo.getCurNum()+mContext.getString(R.string.reserve_treatment_success_number_number));

        String year = newCurInfo.getSchedulDate().substring(0,4);
        String month = (newCurInfo.getSchedulDate().substring(4).equals("0"))
                       ? newCurInfo.getSchedulDate().substring(5,6)
                       : newCurInfo.getSchedulDate().substring(4,6);
        String day = (newCurInfo.getSchedulDate().substring(6).equals("0"))
                       ? newCurInfo.getSchedulDate().substring(7,8)
                       : newCurInfo.getSchedulDate().substring(6,8);
        reserveTimeView.setText(month+mContext.getString(R.string.reserve_treatment_month)
            +day+mContext.getString(R.string.reserve_treatment_day)+" "+newCurInfo.getTime());
    }

    private void initTitleBar() {
        titleBar = getTitleBar();
        titleBar.enableBack();
        titleBar.setTitle(mContext.getString(R.string.reserve_treatment_success_complete));
    }

    @Override
    public void todoClick(int i) {
        super.todoClick(i);
        switch (i) {
            case R.id.tv_complete:
                // Intent intent = new Intent(mContext, UDTChatRoomActivity.class);
                // intent.putExtra(UDTDataConstant.UDT_ROOM_DT_ID_FLAG, newCurInfo.getId());
                // mContext.startActivity(intent);
                Intent intent = new Intent(mContext, UDTRoomActivity.class);
                mContext.startActivity(intent);
                finish();
                break;
            case R.id.tv_close:
                finish();
                break;
            default:
                break;
        }
    }

}
