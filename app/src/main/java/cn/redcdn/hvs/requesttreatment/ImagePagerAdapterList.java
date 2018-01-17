package cn.redcdn.hvs.requesttreatment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.contacts.contact.ContactPagerAdapterBase;
import cn.redcdn.hvs.contacts.contact.interfaces.Contact;
import cn.redcdn.hvs.im.activity.ViewPhotosActivity;
import cn.redcdn.hvs.im.bean.PhotoBean;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.log.CustomLog;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/12/5 0005.
 */

public class ImagePagerAdapterList extends ContactPagerAdapterBase {
    private List<ImagePagerGridViewAdapterList> mAdapterList;
    private PatientConditionActivity.ItemClickListener mConditionLintener;
    private boolean canLongClick = true;

    public ImagePagerAdapterList(Context context, List<Contact> contacts,
                                 int columns, int rows, boolean status, PatientConditionActivity.ItemClickListener listener) {
        super(context, contacts, columns, rows, status);
        this.mColumn = columns;
        this.mContext = context;
        this.mContacts = contacts;
        this.mPageCapacity = rows * columns;
        this.mAdapterList = new ArrayList<ImagePagerGridViewAdapterList>();
        this.mConditionLintener = listener;
    }

    @Override
    public int getCount() {
        mPageCounts = (mContacts.size() ) / mPageCapacity;
        if ((mContacts.size() ) % mPageCapacity != 0) {
            mPageCounts += 1;
        }
        return mPageCounts;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int index) {
        final GridView gridView = (GridView) LayoutInflater.from(mContext).inflate(
            R.layout.layout_image_viewpager, null);
        ImagePagerGridViewAdapterList adapter = new ImagePagerGridViewAdapterList(
            mContext, index, mContacts, mPageCapacity,canLongClick);
        if (!mAdapterList.contains(adapter)) {
            mAdapterList.add(adapter);
        }
        gridView.setFocusable(true);
        gridView.requestFocus();
        gridView.setNumColumns(mColumn);
        gridView.setAdapter(adapter);
        gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));

        gridView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if (mListener != null) {
                    mListener.onNoItemSelected();
                }
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int itemPosition, long id) {
                int absolutePosition = index * mPageCapacity + itemPosition;
                CustomLog.d("ImagePagerAdapterList onItemClick", "absolutePosition"+absolutePosition);
                if(!canLongClick){
                    String url = null;
                    if(!TextUtils.isEmpty(mContacts.get(absolutePosition).getNubeNumber())){
                        url = mContacts.get(absolutePosition).getNubeNumber();
                    }else if(!TextUtils.isEmpty(mContacts.get(absolutePosition).getHeadUrl())){
                        url = mContacts.get(absolutePosition).getHeadUrl();
                    }
                    if (!TextUtils.isEmpty(url)) {

                        ArrayList<PhotoBean> imagePaths = new ArrayList<PhotoBean>();
                        for (int i = 0; i < mContacts.size(); i++) {
                            PhotoBean bean = new PhotoBean();
                            if(!TextUtils.isEmpty(mContacts.get(absolutePosition).getNubeNumber())){
                                bean.setLittlePicUrl(mContacts.get(i).getNubeNumber());
                                bean.setLocalPath(mContacts.get(i).getNubeNumber());
                                bean.setRemoteUrl(mContacts.get(i).getNubeNumber());
                            }else if(!TextUtils.isEmpty(mContacts.get(absolutePosition).getHeadUrl())){
                                bean.setLittlePicUrl(mContacts.get(i).getHeadUrl());
                                bean.setLocalPath(mContacts.get(i).getHeadUrl());
                                bean.setRemoteUrl(mContacts.get(i).getHeadUrl());
                            }
                            imagePaths.add(bean);
                        }

                        Intent i = new Intent(mContext, ViewPhotosActivity.class);
                        i.putParcelableArrayListExtra(
                            ViewPhotosActivity.KEY_PHOTOS_LIST, imagePaths);
                        i.putExtra(ViewPhotosActivity.KEY_PHOTOS_SELECT_INDEX, absolutePosition);
                        i.putExtra(ViewPhotosActivity.KEY_REMOTE_FILE, true);
                        i.putExtra(ViewPhotosActivity.KEY_VIDEO_FILE, false);
                        i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        i.putExtra(ViewPhotosActivity.KEY_COLLECTION_SCAN, true);
                        i.putExtra(ViewPhotosActivity.KEY_DISABLE_LONG_CLICK, true);
                        mContext.startActivity(i);

                    } else {
                        CustomToast.show(mContext, mContext.getString(R.string.pic_address_null), 1);
                    }
                }else {
                    if((absolutePosition+1==mContacts.size())){
                        if(mContacts.size()==10){
                            CustomToast.show(mContext, mContext.getString(R.string.patient_condition_at_most_nine_pictures), 1);
                        }else{
                            if(mConditionLintener!=null){
                                mConditionLintener.onClick();
                            }
                        }

                    }else{
                        String url = null;
                        if(!TextUtils.isEmpty(mContacts.get(absolutePosition).getNubeNumber())){
                            url = mContacts.get(absolutePosition).getNubeNumber();
                        }else if(!TextUtils.isEmpty(mContacts.get(absolutePosition).getHeadUrl())){
                            url = mContacts.get(absolutePosition).getHeadUrl();
                        }
                        if (!TextUtils.isEmpty(url)) {

                            ArrayList<PhotoBean> imagePaths = new ArrayList<PhotoBean>();
                            for (int i = 0; i < mContacts.size()-1; i++) {
                                PhotoBean bean = new PhotoBean();
                                if(!TextUtils.isEmpty(mContacts.get(absolutePosition).getNubeNumber())){
                                    bean.setLittlePicUrl(mContacts.get(i).getNubeNumber());
                                    bean.setLocalPath(mContacts.get(i).getNubeNumber());
                                    bean.setRemoteUrl(mContacts.get(i).getNubeNumber());
                                }else if(!TextUtils.isEmpty(mContacts.get(absolutePosition).getHeadUrl())){
                                    bean.setLittlePicUrl(mContacts.get(i).getHeadUrl());
                                    bean.setLocalPath(mContacts.get(i).getHeadUrl());
                                    bean.setRemoteUrl(mContacts.get(i).getHeadUrl());
                                }
                                imagePaths.add(bean);
                            }

                            Intent i = new Intent(mContext, ViewPhotosActivity.class);
                            i.putParcelableArrayListExtra(
                                ViewPhotosActivity.KEY_PHOTOS_LIST, imagePaths);
                            i.putExtra(ViewPhotosActivity.KEY_PHOTOS_SELECT_INDEX, absolutePosition);
                            i.putExtra(ViewPhotosActivity.KEY_REMOTE_FILE, true);
                            i.putExtra(ViewPhotosActivity.KEY_VIDEO_FILE, false);
                            i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            i.putExtra(ViewPhotosActivity.KEY_COLLECTION_SCAN, true);
                            i.putExtra(ViewPhotosActivity.KEY_DISABLE_LONG_CLICK, true);
                            mContext.startActivity(i);

                        } else {
                            CustomToast.show(mContext, mContext.getString(R.string.pic_address_null), 1);
                        }
                    }
                }
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int absolutePosition = index * mPageCapacity + position;
                CustomLog.d("ImagePagerAdapterList setOnItemLongClickListener", "absolutePosition"+absolutePosition);
                if(canLongClick){
                    if(absolutePosition+1!=mContacts.size()){
                        if(mConditionLintener!=null){
                            mConditionLintener.onLongClick(view,absolutePosition);
                        }
                    }
                }
                return true;
            }
        });

        container.addView(gridView, 0);
        return gridView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();

        while (mPageCounts < mAdapterList.size()) {
            mAdapterList.remove(mAdapterList.size() - 1);
        }
        for (int i = 0; i < mAdapterList.size(); ++i) {
            ImagePagerGridViewAdapterList adapter = mAdapterList.get(i);
            adapter.notifyDataSetChanged();
        }
    }

    public void resetData(List<Contact> list){
        this.mContacts = list;
    }

    public void disableLongClick(){
        this.canLongClick = false;
        notifyDataSetChanged();
    }


}
