package cn.redcdn.hvs.util;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.im.activity.ViewImages.PhotoView;
import cn.redcdn.hvs.im.activity.ViewImages.PhotoViewAttacher;
import cn.redcdn.hvs.im.util.IMCommonUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class OriginalContactIconActivity extends BaseActivity {
    public static String CURRENT_HEAD_URL="current_head_url";
    public static String CURRENT_SEX="current_sex";

    private PhotoView originalIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.original_contact_icon);
        originalIcon = (PhotoView) findViewById(R.id.original_icon);
        originalIcon.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {

            @Override
            public void onViewTap(View view, float x, float y) {
                OriginalContactIconActivity.this.finish();
            }
        });

        loadContactIcon();
    }

    private void loadContactIcon() {
        // 加载联系人头像
        Intent i = getIntent();
        String headUrl = i.getStringExtra(CURRENT_HEAD_URL);
        String sex = i.getStringExtra(CURRENT_SEX);

        if (!TextUtils.isEmpty(headUrl)) {
            Glide.with(this)
                .load(headUrl)
                .placeholder(IMCommonUtil.getHeadIdBySex(sex))
                .error(IMCommonUtil.getHeadIdBySex(sex))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE).crossFade()
                .into(originalIcon);
        } else {
            // 设置默认头像
            originalIcon.setImageResource(IMCommonUtil.getHeadIdBySex(sex));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
