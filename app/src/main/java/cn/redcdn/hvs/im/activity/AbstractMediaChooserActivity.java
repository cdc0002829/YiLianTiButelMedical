package cn.redcdn.hvs.im.activity;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;

/**
 * <dl>
 * <dt>AbstractMediaChooserActivity.java</dt>
 * <dd>Description:媒体文件选择，支持点击选中</dd>
 * <dd>Copyright: Copyright (C) 2011</dd>
 * <dd>Company: 安徽青牛信息技术有限公司</dd>
 * <dd>CreateDate: 2013-10-9 下午1:43:33</dd>
 * </dl>
 *
 * @author zhaguitao
 */
public abstract class AbstractMediaChooserActivity extends
    AbstractMediaFolderChooserActivity implements OnItemLongClickListener {

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
                                   int position, long id) {
        clickPosition(position);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        selectPosition(view, position);
    }

    protected abstract void selectPosition(View view, int position);
}
