package cn.redcdn.hvs.im.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.butel.connectevent.utils.LogUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.redcdn.hvs.R;
import cn.redcdn.hvs.im.IMConstant;
import cn.redcdn.hvs.im.bean.ImageItem;
import cn.redcdn.hvs.im.manager.FileManager;
import cn.redcdn.hvs.im.util.IMCommonUtil;
import cn.redcdn.hvs.im.view.ButelGridView;
import cn.redcdn.hvs.im.view.CustomDialog;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.TitleBar;

/**
 * <dl>
 * <dt>MultiImageChooserActivity.java</dt>
 * <dd>Description:图片选择界面（支持多选）</dd>
 * <dd>Copyright: Copyright (C) 2011</dd>
 * <dd>Company: 安徽青牛信息技术有限公司</dd>
 * <dd>CreateDate: 2013-9-10 下午3:18:17</dd>
 * </dl>
 *
 * @author zhaguitao
 */
public class MultiImageChooserActivity extends AbstractMediaChooserActivity {
    List<ImageItem> imageItems = new ArrayList<>();

    public static final String KEY_CHOOSER_TYPE = "chooser_type";
    public static final String KEY_CHOOSER_ALL_IMAGE = "chooser_all_image";
    public static final int CHOOSER_TYPE_IMAGE = 3;
    public static final int CHOOSER_TYPE_VIDEO = 4;
    public static final int CHOOSER_TYPE_VIDEO_IMAGE = 6;
    public static final int CHOOSE_ALL = 5;

    private static final int REQEUST_CODE_PREVIEW = 2102;

    // 照片墙grid
    private ButelGridView gridView;
    // 照片墙列数
    private static final int IMAGE_COLUMN = 4;
    private static final int IMAGE_COLUMN_LANDSCAPE = 5;
    // 图片适配器
    private ImageAdapter ia;
    private Cursor imageCursor;
    // 图片尺寸
    private int imgSize = 166;
    private int imgSizePortrait = imgSize;
    private int imgSizeLandscape = imgSize;
    // 选中的图片
    private ArrayList<String> imgPathList = new ArrayList<String>();

    // imageid列索引
    private int imageIdColumnIndex;
    // imagedata列索引
    private int imageDataColIdx;

    private int imageDuration;

    //	// image height列索引
    //	private int imageHeightColumnIndex = -1;
    //	// image width列索引
    //	private int imageWidthColumnIndex = -1;

    // 滚动速度很快的场合，先不加载图片，滚动速度较慢，才会加载图片，以保证性能
    private boolean shouldRequestThumb = true;

    // private LocalImageFetcher mImageFetcher = null;

    private LoaderCallbacks<Cursor> loaderCallbacks = null;

    private String nubenumber = "";
    private String bucketId = "";

    // 选择类型
    private int chooserType = CHOOSER_TYPE_IMAGE;
    private int chooserAll = 0;

    private Button mBntFinish = null;
    private TextView shareAccount;
    private Button mBntPreview = null;

    // 分享界面已选照片数
    private int shareSelectedCnt = 0;

    public static Activity mltiImageChooserActivity = null;

    //	private boolean isSupportImgWH = false;
    private TitleBar titleBar = null;

    @Override
    protected int getContentView() {
        return R.layout.select_multi_image;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        LogUtil.begin("");
        super.onCreate(savedInstanceState);

        mltiImageChooserActivity = this;

        //		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        //			// Android4.1开始支持图片width和height
        //			isSupportImgWH = true;
        //		}

        shareSelectedCnt = getIntent().getIntExtra(
                PreviewActivity.KEY_SELECTED_CNT, 0);
        nubenumber = getIntent().getStringExtra(
                MultiBucketChooserActivity.KEY_NUBENUMBER);
        bucketId = getIntent().getStringExtra(
                MultiBucketChooserActivity.KEY_BUCKETID);
        imgPathList.clear();

        chooserType = getIntent().getIntExtra(KEY_CHOOSER_TYPE,
                CHOOSER_TYPE_IMAGE);

        chooserAll = getIntent().getIntExtra(KEY_CHOOSER_ALL_IMAGE, 0);
        titleBar = getTitleBar();
        titleBar.enableBack();
        titleBar.enableRightBtn(this.getString(R.string.cancel), 0, new OnClickListener() {
            @Override
            public void onClick(View view) {
                MultiImageChooserActivity.this.finish();
            }
        });
        String title = "";
        if (chooserType == CHOOSER_TYPE_IMAGE) {
            title = FileManager.getImgBucketName(this, bucketId);

        } else if (chooserType == CHOOSER_TYPE_VIDEO) {
            title = FileManager.getVideoBucketName(this, bucketId);
        }
        titleBar.setTitle(title);

        int mImageThumbSpacing = getResources().getDimensionPixelSize(
                R.dimen.multi_image_chooser_spacing);
        int mImageThumbPadding = getResources().getDimensionPixelSize(
                R.dimen.multi_image_chooser_padding);

        int screenWidth = IMCommonUtil.getDeviceSize(this).x;
        int screenHeight = IMCommonUtil.getDeviceSize(this).y;

        shareAccount = (TextView) findViewById(R.id.share_account);
        mBntFinish = (Button) findViewById(R.id.bnt_share_photos);
        mBntPreview = (Button) findViewById(R.id.bnt_preview_photos);

        if (MultiBucketChooserActivity.fromType == MultiBucketChooserActivity.FROM_TYPE_SEND) {
            mBntFinish.setText(R.string.send_blank);
        } else if (MultiBucketChooserActivity.fromType ==
                MultiBucketChooserActivity.FROM_TYPE_COLLECT) {
            mBntFinish.setText(R.string.collect_str);
        }
        mBntFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.d("选择图片/视频 页面,点击 完成 按钮");
                if (CommonUtil.isFastDoubleClick()) {
                    return;
                }
                confirm();
            }
        });
        mBntPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.d("选择图片/视频 页面,点击 图片/视频 进入预览/播放 页面");
                if (CommonUtil.isFastDoubleClick()) {
                    return;
                }
                doPreview();
            }
        });

        setAccount(0);

        gridView = (ButelGridView) findViewById(R.id.gridview);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            gridView.setNumColumns(IMAGE_COLUMN);
            imgSizePortrait = (screenWidth - mImageThumbSpacing
                    * (IMAGE_COLUMN - 1) - mImageThumbPadding * 2)
                    / IMAGE_COLUMN;
            imgSizeLandscape = (screenHeight - mImageThumbSpacing
                    * (IMAGE_COLUMN_LANDSCAPE - 1) - mImageThumbPadding * 2)
                    / IMAGE_COLUMN_LANDSCAPE;
            imgSize = imgSizePortrait;
        } else {
            gridView.setNumColumns(IMAGE_COLUMN_LANDSCAPE);
            imgSizePortrait = (screenHeight - mImageThumbSpacing
                    * (IMAGE_COLUMN - 1) - mImageThumbPadding * 2)
                    / IMAGE_COLUMN;
            imgSizeLandscape = (screenWidth - mImageThumbSpacing
                    * (IMAGE_COLUMN_LANDSCAPE - 1) - mImageThumbPadding * 2)
                    / IMAGE_COLUMN_LANDSCAPE;
            imgSize = imgSizeLandscape;
        }

        //		gridView.setOnItemClickListener(this);
        gridView.setOnScrollListener(new OnScrollListener() {

            private int lastFirstItem = 0;
            private long timestamp = System.currentTimeMillis();


            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    // 停止滚动，刷新grid并加载图片
                    LogUtil.d("MultiImageChooserActivity IDLE - Reload!");
                    shouldRequestThumb = true;
                    ia.notifyDataSetChanged();
                } else if (scrollState == SCROLL_STATE_FLING) {
                    LogUtil.d("MultiBucketChooserActivity 列表正在滚动...");
                    // list列表滚动过程中，暂停图片上传下载
                } else {
                }
            }


            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                float dt = System.currentTimeMillis() - timestamp;
                if (firstVisibleItem != lastFirstItem) {
                    double speed = 1 / dt * 1000;
                    lastFirstItem = firstVisibleItem;
                    timestamp = System.currentTimeMillis();
                    LogUtil.d("MultiImageChooserActivity Speed: " + speed
                            + " elements/second");

                    // 滚动速度很快的场合，先不加载图片，滚动速度较慢，才会加载图片，以保证性能
                    shouldRequestThumb = speed < visibleItemCount;
                }
            }
        });

        ia = new ImageAdapter(this);
        gridView.setAdapter(ia);

        LoaderManager.enableDebugLogging(false);

        loaderCallbacks = new LoaderCallbacks<Cursor>() {
            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
//                imageCursor.close();
//                imageCursor = null;
//                imageItems.clear();
            }


            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                if (cursor == null) {
                    // NULL cursor. This usually means there's no image database
                    // yet....
                    imgPathList.clear();
                    setAccount(0);
                    imageCursor = null;
                    ia.notifyDataSetChanged();
                    return;
                }

                int boforeCnt = imgPathList.size();
                for (int i = imgPathList.size() - 1; i >= 0; i--) {
                    File imgFile = new File(imgPathList.get(i));
                    if (!imgFile.exists() || imgFile.length() == 0) {
                        // 排除不存在的文件
                        imgPathList.remove(i);
                    }
                }
                if (imgPathList.size() != boforeCnt) {
                    // 选中的图片，有被删除的场合
                    setAccount(imgPathList.size());
                }

                switch (loader.getId()) {
                    case CHOOSER_TYPE_IMAGE:
                        imageCursor = cursor;
                        imageIdColumnIndex = imageCursor
                                .getColumnIndex(MediaStore.Images.Media._ID);
                        imageDataColIdx = imageCursor
                                .getColumnIndex(MediaStore.Images.Media.DATA);
                        while (imageCursor.moveToNext()) {
                            ImageItem imageItem = new ImageItem();
                            imageItem.setImageId(imageCursor.getInt(imageIdColumnIndex));
                            imageItem.setImagePath(imageCursor.getString(imageDataColIdx));
                            imageItems.add(imageItem);
                        }
                        ia.setData(imageItems);
                        break;
                    case CHOOSER_TYPE_VIDEO:
                        imageCursor = cursor;
                        imageIdColumnIndex = imageCursor
                                .getColumnIndex(MediaStore.Video.Media._ID);
                        imageDataColIdx = imageCursor
                                .getColumnIndex(MediaStore.Video.Media.DATA);
                        imageDuration = imageCursor.getColumnIndex(MediaStore.Video.Media.DURATION);
                        while (imageCursor.moveToNext()) {
                            ImageItem imageItem = new ImageItem();
                            imageItem.setImageId(cursor.getInt(imageIdColumnIndex));
                            imageItem.setImagePath(cursor.getString(imageDataColIdx));
                            imageItem.setDuration(cursor.getLong(imageDuration));
                            imageItems.add(imageItem);
                        }

                        break;
                    default:
                        break;
                }
                ia.setData(imageItems);
            }


            @Override
            public Loader<Cursor> onCreateLoader(int cursorID, Bundle arg1) {
                CursorLoader cl = null;

                ArrayList<String> img = new ArrayList<String>();
                String order = null;
                //				String[] selectArgs = new String[] { bucketId };
                switch (cursorID) {

                    case CHOOSER_TYPE_IMAGE:
                        img.add(MediaStore.Images.Media._ID);
                        img.add(MediaStore.Images.Media.DATA);
                        img.add("count( distinct " + MediaStore.Images.Media.DATA
                                + ")");
                        order = MediaStore.Images.Media.DATE_MODIFIED + " desc ";
                        if (chooserAll == CHOOSE_ALL) {
                            cl = new CursorLoader(MultiImageChooserActivity.this,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    img.toArray(new String[img.size()]),
                                    MediaStore.Images.Media.SIZE
                                            + " > 0 ) GROUP BY ("
                                            + MediaStore.Images.Media.DATA, null, order);
                        } else {
                            cl = new CursorLoader(MultiImageChooserActivity.this,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    img.toArray(new String[img.size()]),
                                    MediaStore.Images.Media.BUCKET_ID + " = "
                                            + bucketId + " and "
                                            + MediaStore.Images.Media.SIZE
                                            + " > 0 ) GROUP BY ("
                                            + MediaStore.Images.Media.DATA, null, order);
                        }
                        break;
                    case CHOOSER_TYPE_VIDEO:
                        img.add(MediaStore.Video.Media._ID);
                        img.add(MediaStore.Video.Media.DATA);
                        img.add(MediaStore.Video.Media.DURATION);
                        img.add("count( distinct " + MediaStore.Video.Media.DATA
                                + ")");
                        order = MediaStore.Video.Media.DATE_MODIFIED + " desc ";

                        cl = new CursorLoader(MultiImageChooserActivity.this,
                                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                img.toArray(new String[img.size()]),
                                MediaStore.Images.Media.SIZE
                                        + " > 0 ) GROUP BY ("
                                        + MediaStore.Images.Media.DATA, null, order);
                        break;
                    default:
                        break;
                }
                return cl;
            }
        }

        ;

        getSupportLoaderManager()
                .

                        initLoader(chooserType, null, loaderCallbacks);
        LogUtil.end("");

    }


    private void setAccount(int account) {
        if (account <= 0) {
            shareAccount.setVisibility(View.GONE);
            mBntFinish.setClickable(false);
            mBntFinish.setTextColor(getResources().getColor(
                    R.color.img_choose_text_disable_color));
            mBntPreview.setClickable(false);
            mBntPreview.setTextColor(getResources().getColor(
                    R.color.img_choose_text_disable_color));
        } else {
            if (View.GONE == shareAccount.getVisibility()) {
                shareAccount.setVisibility(View.VISIBLE);
            }
            mBntFinish.setClickable(true);
            mBntFinish.setTextColor(getResources().getColor(
                    R.color.img_choose_text_enable_color));
            mBntPreview.setClickable(true);
            mBntPreview.setTextColor(getResources().getColor(
                    R.color.img_choose_text_enable_color));
            Animation scaleAnimation = AnimationUtils.loadAnimation(this,
                    R.anim.img_selected_cnt_anim);
            shareAccount.startAnimation(scaleAnimation);
            shareAccount.setText(String.valueOf(account));
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        LogUtil.begin("");
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            imgSize = imgSizeLandscape;
            gridView.setNumColumns(IMAGE_COLUMN_LANDSCAPE);
        } else {
            imgSize = imgSizePortrait;
            gridView.setNumColumns(IMAGE_COLUMN);
        }
        // mImageFetcher.setImageSize(imgSize);
        ia.notifyDataSetChanged();
        LogUtil.end("");
    }


    @Override
    protected void onResume() {
        LogUtil.begin("");
        super.onResume();

        LogUtil.end("");
    }


    @Override
    protected void onPause() {
        LogUtil.begin("");
        super.onPause();

        LogUtil.end("");
    }


    @Override
    protected void onDestroy() {
        LogUtil.begin("");
        super.onDestroy();
        getSupportLoaderManager().destroyLoader(chooserType);

        mltiImageChooserActivity = null;
        LogUtil.end("");
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        LogUtil.d("点击的照片 Id=" + position);
        clickPosition(position);
    }


    protected void doPreview() {
        LogUtil.d("进入预览图片页面");
        // 预览图片
        ArrayList<String> imagePaths = new ArrayList<String>();
        if (imgPathList.size() > getMaxCnt()) {
            imagePaths.addAll(imgPathList.subList(0, getMaxCnt()));
        } else {
            imagePaths = imgPathList;
        }

        // 检查视频文件是否超过30M
        if (checkVedioOversize(imagePaths)) {
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putStringArrayList(Intent.EXTRA_STREAM, imagePaths);
        bundle.putBoolean(PreviewActivity.KEY_ACTION_CUSTOM, true);
        if (chooserType == CHOOSER_TYPE_VIDEO) {
            bundle.putInt(PreviewActivity.KEY_FOR_SHARE_TYPE,
                    PreviewActivity.SHARE_TYPE_VIDEO);
        } else {
            bundle.putInt(PreviewActivity.KEY_FOR_SHARE_TYPE,
                    PreviewActivity.SHARE_TYPE_PIC);
        }
        bundle.putString(PreviewActivity.KEY_FOR_NUBE_NUMBER, nubenumber);

        Intent intent = new Intent(MultiImageChooserActivity.this,
                PreviewActivity.class);
        intent.putExtras(bundle);
        startActivityForResult(intent, REQEUST_CODE_PREVIEW);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.d("onActivityResult");
        if (resultCode == RESULT_OK) {
            if (requestCode == REQEUST_CODE_PREVIEW) {
                LogUtil.d("onActivityResult REQEUST_CODE_PREVIEW");
                String back_flag = data
                        .getStringExtra(PreviewActivity.BACK_CODE);
                if (back_flag.equals(PreviewActivity.RETURN_WITHOUT_SEND)) {
                    // TODO:backBtn返回，则带入PreviewActivity中编辑结果，刷新当前图片选中状态
                    imgPathList.clear();
                    imgPathList = data
                            .getStringArrayListExtra(Intent.EXTRA_STREAM);
                    ia.notifyDataSetChanged();
                    setAccount(imgPathList.size());
                } else {
                    //只有发送返回，才携带数据
                    setResult(RESULT_OK, data);
                    // 预览页面发送照片返回，直接finish
                    MultiImageChooserActivity.this.finish();
                }
            }
        }
    }


    // 分享文件大小限制
    private boolean checkFileOversize(String path) {
        if (path != null) {
            File file = new File(path);
            if (file != null && file.exists()) {
                long signal_size = file.length();
                if (chooserType == CHOOSER_TYPE_VIDEO) {
                    // 视频不支持30M以上
                    if (signal_size > IMConstant.MAX_VIDEO_FILE_SIZE) {
                        CustomDialog.Builder builder = new CustomDialog.Builder(this);
                        builder.setMessage(getString(R.string.send_vedio_most_30));
                        builder.setPositiveButton(getString(R.string.iknow), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                //设置你的操作事项
                            }
                        });
                        builder.create().show();
                        return true;
                    }
                } else {
                    if (signal_size > IMConstant.MAX_IMAGE_FILE_SIZE) {
                        CustomDialog.Builder builder = new CustomDialog.Builder(this);
                        builder.setMessage(getString(R.string.send_image_most_10));
                        builder.setPositiveButton(getString(R.string.iknow), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                //设置你的操作事项
                            }
                        });
                        builder.create().show();
                        return true;
                    }
                }
            }
        }
        return false;
    }


    private boolean checkVedioOversize(ArrayList<String> pathList) {
        if (pathList != null) {
            for (String path : pathList) {
                if (checkFileOversize(path)) {
                    return true;
                }
            }
        }
        return false;
    }


    protected void confirm() {
        // 选择图片
        ArrayList<String> imagePaths = new ArrayList<String>();
        if (imgPathList.size() > getMaxCnt()) {
            imagePaths.addAll(imgPathList.subList(0, getMaxCnt()));
        } else {
            imagePaths = imgPathList;
        }
        // 分享界面增加照片/视频的场合
        Intent intent = new Intent();
        intent.putStringArrayListExtra(Intent.EXTRA_STREAM, imagePaths);
        setResult(RESULT_OK, intent);
        MultiImageChooserActivity.this.finish();
    }


    @Override
    protected int getMaxCnt() {
        return MultiBucketChooserActivity.MAX_IMAGE_COUNT;
    }

    //	@Override
    //	protected boolean needRefresh() {
    //		return false;
    //	}


    @Override
    protected int getDataCnt() {
        if (imageItems == null) {
            return 0;
        } else {
            return imageItems.size();
        }
    }


    private void deselectAll() {
        imgPathList.clear();
        ia.notifyDataSetChanged();
    }


    @Override
    protected void selectPosition(View view, int position) {
    }


    @Override
    protected void clickPosition(int position) {
        LogUtil.d("pos:" + position);
        //		if (CommonUtil.isFastDoubleClick()) {
        //			return;
        //		}
        //
        //		if (chooserType == CHOOSER_TYPE_VIDEO) {
        //			// 视频的场合，点击开始播放
        //			LogUtil.d("chooserType == CHOOSER_TYPE_VIDEO,点击开始视频播放");
        //			Intent intent = new Intent(Intent.ACTION_VIEW,
        //					getImageUri(position));
        //			startActivity(intent);
        //		} else {
        //			// 查看原图
        //			LogUtil.d("chooserType == CHOOSER_TYPE_IMAGE,点击开始视频播放");
        //			ArrayList<String> oriImage = new ArrayList<String>();
        //			imageCursor.moveToPosition(position);
        //			oriImage.add(imageCursor.getString(imageDataColIdx));
        //			Intent i = new Intent(this, ViewPhotosActivity.class);
        //			i.putStringArrayListExtra(ViewPhotosActivity.KEY_PHOTOS_LIST,
        //					oriImage);
        //			startActivity(i);
        //		}
    }


    private Uri getImageUri(int position) {
        imageCursor.moveToPosition(position);

        try {
            int id = imageCursor.getInt(imageIdColumnIndex);
            return getImageUriByImgId(id);
        } catch (Exception e) {
            return null;
        }
    }


    private Uri getImageUriByImgId(int imgId) {

        try {
            if (chooserType == CHOOSER_TYPE_VIDEO) {
                return Uri
                        .withAppendedPath(
                                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, ""
                                        + imgId);
            } else {
                return Uri.withAppendedPath(FileManager.IMAGE_BASEURI, ""
                        + imgId);
            }
        } catch (Exception e) {
            return null;
        }
    }


    public class ComparatorImageItem implements Comparator<ImageItem> {

        public int compare(ImageItem o1, ImageItem o2) {

            return String.valueOf(o2.getTime()).compareTo(String.valueOf(o1.getTime()));
        }
    }

    public List removeDuplicate(List<ImageItem> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = list.size() - 1; j > i; j--) {
                if (list.get(j).getImagePath().equals(list.get(i).getImagePath())) {
                    list.remove(j);
                }
            }
        }
        return list;
    }

    public class ImageAdapter extends BaseAdapter {
        private LayoutInflater layoutInflater = null;
        private GalleryViewHolder viewHolder = null;
        private List<ImageItem> imgeItems;

        public void setData(List<ImageItem> imageItems) {
            if (null != imageItems && imageItems.size() > 0) {
                ComparatorImageItem comparator = new ComparatorImageItem();
                Collections.sort(removeDuplicate(imageItems), comparator);
            }

            this.imgeItems = imageItems;
            notifyDataSetChanged();
        }

        public ImageAdapter(Context c) {
            this.layoutInflater = (LayoutInflater) c
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }


        public int getCount() {
            if (imgeItems != null) {
                return imgeItems.size();
            } else {
                return 0;
            }
        }


        public Object getItem(int position) {
            return position;
        }


        public long getItemId(int position) {
            return position;
        }


        public View getView(final int pos, View convertView, ViewGroup parent) {

            if (gridView.isOnMeasure) {
                convertView = (View) layoutInflater.inflate(
                        R.layout.multi_image_chooser_pic, parent, false);
                return convertView;
            } else {
                if (convertView != null) {
                    viewHolder = (GalleryViewHolder) convertView.getTag();
                    if (viewHolder == null) {
                        convertView = null;
                    }
                }
            }
            LogUtil.d("getView:" + pos);

            if (convertView == null) {
                convertView = (View) layoutInflater.inflate(
                        R.layout.multi_image_chooser_pic, parent, false);
                viewHolder = new GalleryViewHolder();
                viewHolder.imgArea = (FrameLayout) convertView
                        .findViewById(R.id.img_area);
                viewHolder.imageTarget = (ImageView) convertView
                        .findViewById(R.id.image_target);
                viewHolder.videoIcon = (ImageView) convertView
                        .findViewById(R.id.video_icon);
                viewHolder.timeLong = (TextView) convertView.findViewById(R.id.time1_tv);


                viewHolder.checkboxImg = (ImageView) convertView
                        .findViewById(R.id.checkbox_img);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (GalleryViewHolder) convertView.getTag();
            }

            if (chooserType == CHOOSER_TYPE_VIDEO) {
                viewHolder.videoIcon.setVisibility(View.VISIBLE);
                viewHolder.timeLong.setVisibility(View.VISIBLE);

            }

            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) viewHolder.imgArea
                    .getLayoutParams();
            lp.width = imgSize;
            lp.height = imgSize;
            viewHolder.imgArea.setLayoutParams(lp);

            viewHolder.imageTarget.setImageResource(R.drawable.empty_photo);

            final int position = pos;
            if (!imageCursor.moveToPosition(position)) {
                return convertView;
            }
            if (imageIdColumnIndex == -1) {
                return convertView;
            }

            final int id = imageCursor.getInt(imageIdColumnIndex);
            final String path = imageCursor.getString(imageDataColIdx);

            if (shouldRequestThumb) {
                if (chooserType == CHOOSER_TYPE_VIDEO) {
                    Glide.with(MultiImageChooserActivity.this).load(imgeItems.get(pos).getImagePath())
                            .placeholder(R.drawable.empty_photo)
                            .error(R.drawable.empty_photo).centerCrop()
                            .crossFade()
                            .into(viewHolder.imageTarget);
                    viewHolder.timeLong.setText(timeParse(imageItems.get(pos).getDuration()));
                    viewHolder.timeLong.setTextColor(Color.WHITE);
                } else {
                    Glide.with(MultiImageChooserActivity.this).load(imgeItems.get(pos).getImagePath())
                            .placeholder(R.drawable.empty_photo)
                            .error(R.drawable.empty_photo).centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .dontAnimate()
                            .into(viewHolder.imageTarget);
                }
            }

            // 使用图片路径作为key来记选择状态
            if (imgPathList.contains(path)) {

                viewHolder.checkboxImg
                        .setBackgroundResource(R.drawable.m_notice_checkbox_sel);

            } else {
                viewHolder.checkboxImg
                        .setBackgroundResource(R.drawable.m_notice_checkbox_nor);

            }


            viewHolder.checkboxImg.setTag(R.id.image_tag_mutil, id + "_" + path);
            viewHolder.checkboxImg.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 已选中的场合，点击取消选中
                    String idPath = (String) v.getTag(R.id.image_tag_mutil);
                    int sIdx = idPath.indexOf("_");
                    int id = Integer.parseInt(idPath.substring(0, sIdx));
                    String path = idPath.substring(sIdx + 1);
                    selectItem(id, path);
                }
            });

            viewHolder.imageTarget.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ArrayList<String> imagePaths = new ArrayList<String>();
                    for (int i = 0; i < imgeItems.size(); i++) {
                        imagePaths.add(imageItems.get(i).getImagePath());
                    }
                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList(Intent.EXTRA_STREAM, imagePaths);
                    bundle.putStringArrayList("list", imgPathList);
                    bundle.putBoolean(PreviewActivity.KEY_ACTION_CUSTOM, true);
                    bundle.putInt(PreviewActivity.KEY_FOR_SHARE_TYPE,
                            PreviewActivity.SHARE_TYPE_PIC);
                    bundle.putInt(PreviewActivity.KEY_FOR_SHARE_TYPE1,
                            3);
                    bundle.putInt(PreviewActivity.SELECTED_IMG_INDEX,pos);
                    bundle.putString(PreviewActivity.KEY_FOR_NUBE_NUMBER, nubenumber);

                    Intent intent = new Intent(MultiImageChooserActivity.this,
                            PreviewActivity.class);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, REQEUST_CODE_PREVIEW);
                }
            });

            return convertView;
        }
    }


    private void selectItem(int imageId, String imagePath) {
        boolean isChecked = imgPathList.contains(imagePath);
//        if (chooserType == CHOOSER_TYPE_VIDEO && !isChecked) {
//            deselectAll();
//        }

        if (isChecked) {
            imgPathList.remove(imagePath);
        } else {
            // 判断是否达到选择最大个数
            if (imgPathList.size() >= getMaxCnt()) {
                CustomDialog.Builder builder = new CustomDialog.Builder(this);
                if (chooserType == CHOOSER_TYPE_VIDEO) {
                    builder.setMessage(getString(R.string.most_select) + getMaxCnt() + getString(R.string.vedio_number));
                } else {
                    builder.setMessage(getString(R.string.most_select) + getMaxCnt() + getString(R.string.photo_number));
                }
                builder.setPositiveButton(getString(R.string.iknow), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //设置你的操作事项
                    }
                });
                builder.create().show();
                return;
            }
            if (checkFileOversize(imagePath)) {
                return;
            }
            imgPathList.add(imagePath);
        }

        setAccount(imgPathList.size());

        ia.notifyDataSetChanged();
    }


    private static class GalleryViewHolder {
        FrameLayout imgArea;
        ImageView imageTarget;
        ImageView videoIcon;
        //		TextView selectedBg;
        ImageView checkboxImg;
        TextView timeLong;
    }


    @Override
    protected void registerContentObserver(MyContentObserver observer) {

//            getContentResolver()
//                    .registerContentObserver(
//                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true,
//                            observer);
//            getContentResolver().registerContentObserver(
//                    MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, true,
//                    observer);
//
//            getContentResolver().registerContentObserver(
//                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true,
//                    observer);
//            getContentResolver().registerContentObserver(
//                    MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, true,
//                    observer);
//
    }


    @Override
    protected boolean needContentObserver() {
        return false;
    }


    @Override
    protected int getCursorId() {
        return chooserType;
    }


    @Override
    protected LoaderCallbacks<Cursor> getLoaderCallbacks() {
        return loaderCallbacks;
    }

    public static String timeParse(long duration) {
        String time = "";

        long minute = duration / 60000;
        long seconds = duration % 60000;

        long second = Math.round((float) seconds / 1000);

        if (minute < 10) {
            time += "0";
        }
        time += minute + ":";

        if (second < 10) {
            time += "0";
        }
        time += second;

        return time;
    }
}
