package cn.redcdn.hvs.profiles.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.log.CustomLog;

/**
 * Created by Administrator on 2017/2/24.
 */

public class PersonDataActivity extends BaseActivity{
    private Button personDataBackBtn = null;
    private TextView personDataNumTV = null;
    private TextView personDataPermTV = null;
//    private TextView personDataTimeTV = null;
//    private RelativeLayout rlDateLine = null;
//    private View viewDataTime = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CustomLog.d(TAG, "onCreate:" + this.toString());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_persondata);
        personDataBackBtn = (Button) this.findViewById(R.id.persondata_back);
        personDataNumTV = (TextView) this.findViewById(R.id.data_num);
        personDataPermTV = (TextView) this.findViewById(R.id.data_permissen);
//        personDataTimeTV = (TextView) this.findViewById(R.id.data_time);
//        rlDateLine = (RelativeLayout) this.findViewById(R.id.rldateline);
//        viewDataTime = (View) this.findViewById(R.id.view_data_time);

        initstatus();
        personDataBackBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                PersonDataActivity.this.finish();
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();

    }

    private void initstatus() {
        String nickName = AccountManager.getInstance(getApplicationContext())
                .getAccountInfo().nube;
//        String useEndTime = AccountManager.getInstance(getApplicationContext())
//                .getAccountInfo().useEndTime;
        personDataNumTV.setText(nickName);
//        personDataTimeTV.setText(useEndTime);
//        int serviceType = AccountManager.getInstance(getApplicationContext())
//                .getAccountInfo().serviceType;
//        if (serviceType == 0) {
//            personDataPermTV.setText("VIP用户");
//            rlDateLine.setVisibility(View.VISIBLE);
//            viewDataTime.setVisibility(View.VISIBLE);
//        } else if (serviceType == 1) {
//            personDataPermTV.setText("免费用户");
//            rlDateLine.setVisibility(View.INVISIBLE);
//            viewDataTime.setVisibility(View.INVISIBLE);
//        }
    }
}
