package cn.redcdn.hvs.head.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.util.TitleBar;

public class ConfirmActivity extends BaseActivity {

    private Button confirmBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tfconfirm);
        initTitleBar();
        confirmBtn = (Button)findViewById(R.id.verify_btn);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //完成按钮点击事件 跳转
                finish();

            }
        });
    }

    private void initTitleBar() {

        TitleBar title = getTitleBar();
        title.enableBack();
        title.setTitle("审核中");
    }
}
