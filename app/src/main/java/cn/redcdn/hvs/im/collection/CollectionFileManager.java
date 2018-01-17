package cn.redcdn.hvs.im.collection;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import cn.redcdn.datacenter.collectcenter.DataBodyInfo;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.im.activity.CollectionFileDetilActivity;
import cn.redcdn.hvs.im.activity.MultiBucketChooserActivity;
import cn.redcdn.hvs.im.activity.PlayVideoActivity;
import cn.redcdn.hvs.im.activity.RecordedVideoActivity;
import cn.redcdn.hvs.im.activity.ShareLocalActivity;
import cn.redcdn.hvs.im.activity.ViewPhotosActivity;
import cn.redcdn.hvs.im.bean.ButelFileInfo;
import cn.redcdn.hvs.im.bean.CollectionBean;
import cn.redcdn.hvs.im.bean.CollectionEntity;
import cn.redcdn.hvs.im.bean.NoticesBean;
import cn.redcdn.hvs.im.bean.PhotoBean;
import cn.redcdn.hvs.im.fileTask.FileTaskManager;
import cn.redcdn.hvs.im.manager.CollectionManager;
import cn.redcdn.hvs.im.preference.DaoPreference;
import cn.redcdn.hvs.im.util.IMCommonUtil;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.log.CustomLog;
import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static cn.redcdn.hvs.im.activity.CollectionFileDetilActivity.DOC;
import static cn.redcdn.hvs.im.activity.CollectionFileDetilActivity.DOCX;
import static cn.redcdn.hvs.im.activity.CollectionFileDetilActivity.PDF;

/**
 * Desc
 * Created by wangkai on 2017/3/8.
 */

public class CollectionFileManager {

    private final String TAG = "CollectionFileManager";

    /** 常用音频文件扩展名 */
    public static final List<String> AUDIO_FILE_EXTENSIONS = new ArrayList<String>();
    /** 常用视频文件扩展名 */
    public static final List<String> VEDIO_FILE_EXTENSIONS = new ArrayList<String>();


    static {
        AUDIO_FILE_EXTENSIONS.add("mp3");
        AUDIO_FILE_EXTENSIONS.add("aac");
        AUDIO_FILE_EXTENSIONS.add("wav");
        AUDIO_FILE_EXTENSIONS.add("wma");
        AUDIO_FILE_EXTENSIONS.add("mid");
        AUDIO_FILE_EXTENSIONS.add("ape");
        AUDIO_FILE_EXTENSIONS.add("ogg");

        VEDIO_FILE_EXTENSIONS.add("mp4");
        VEDIO_FILE_EXTENSIONS.add("avi");
        VEDIO_FILE_EXTENSIONS.add("mkv");
        VEDIO_FILE_EXTENSIONS.add("rmvb");
        VEDIO_FILE_EXTENSIONS.add("wmv");
        VEDIO_FILE_EXTENSIONS.add("mov");
        VEDIO_FILE_EXTENSIONS.add("3gp");
        VEDIO_FILE_EXTENSIONS.add("mpeg");
        VEDIO_FILE_EXTENSIONS.add("mpg");
        VEDIO_FILE_EXTENSIONS.add("vob");
        VEDIO_FILE_EXTENSIONS.add("flv");
    }


    private static CollectionFileManager manager;


    public static CollectionFileManager getInstance() {
        if (null == manager) {
            manager = new CollectionFileManager();
        }
        return manager;
    }


    // 判断文件是否是文件夹
    public boolean isFileFolder(File file) {

        return file.isDirectory();
    }


    @SuppressWarnings("unchecked")
    public List<ButeleCollectionFile> getFileList(File[] files) {
        List<ButeleCollectionFile> directoryEntries = new ArrayList<ButeleCollectionFile>();

        if (files == null) {
            return directoryEntries;
        }
        if (files != null && files.length == 0) {
            return directoryEntries;
        }

        for (File currentFile : files) {
            ButeleCollectionFile collectionFile = new ButeleCollectionFile();
            // 判断是一个文件夹还是一个文件
            if (currentFile.isDirectory()) {

                collectionFile.setMtext(currentFile.getName());
                collectionFile.setModifiedDate(currentFile.lastModified());
                collectionFile.setFileType("floder");
                collectionFile.setFileIsDir(currentFile.isDirectory());
            } else {
                // 取得文件名
                String fileName = currentFile.getName();
                collectionFile.setMtext(currentFile.getName());
                collectionFile.setFilePath(currentFile.getAbsolutePath());
                collectionFile.setFileSize(currentFile.length());
                collectionFile.setModifiedDate(currentFile.lastModified());
                String fileType;
                fileType = getFiletypeByFileName(fileName);
                // if (checkEndsWithInStringArray(fileName,
                // NetPhoneApplication.getContext().getResources()
                // .getStringArray(R.array.fileEndingImage))) {
                // fileType = "Image";
                // } else if (checkEndsWithInStringArray(fileName,
                // NetPhoneApplication.getContext().getResources()
                // .getStringArray(R.array.fileEndingWebText))) {
                // fileType = "WebText";
                // } else if (checkEndsWithInStringArray(fileName,
                // NetPhoneApplication.getContext().getResources()
                // .getStringArray(R.array.fileEndingPackage))) {
                // fileType = "Package";
                // } else if (checkEndsWithInStringArray(fileName,
                // NetPhoneApplication.getContext().getResources()
                // .getStringArray(R.array.fileEndingAudio))) {
                // fileType = "Audio";
                // } else if (checkEndsWithInStringArray(fileName,
                // NetPhoneApplication.getContext().getResources()
                // .getStringArray(R.array.fileEndingVideo))) {
                // fileType = "Video";
                // } else {
                // fileType = "Other";
                // }
                collectionFile.setFileType(fileType);
                collectionFile.setFileIsDir(currentFile.isDirectory());
            }
            directoryEntries.add(collectionFile);
        }
        FileSortHelper sort = new FileSortHelper();
        Collections.sort(directoryEntries, sort.getComparator());
        return directoryEntries;
    }


    public List<ButeleCollectionFile> fill(File[] files) {

        return null;
    }


    // 通过文件名判断是什么类型的文件
    @SuppressWarnings("unused")
    private boolean checkEndsWithInStringArray(String checkItsEnd,
                                               String[] fileEndings) {
        for (String aEnd : fileEndings) {
            if (checkItsEnd.endsWith(aEnd)) {
                return true;
            }
        }
        return false;
    }


    public String formatDateString(Context context, long time) {
        DateFormat dateFormat = android.text.format.DateFormat
            .getDateFormat(context);
        DateFormat timeFormat = android.text.format.DateFormat
            .getTimeFormat(context);
        Date date = new Date(time);
        return dateFormat.format(date) + " " + timeFormat.format(date);
    }


    // storage, G M K B
    @SuppressLint("DefaultLocale")
    public String convertStorage(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else {
            return String.format("%d B", size);
        }
    }


    public boolean isBigFile(long fileSize) {
        boolean flag = true;
        long bigSize = 1024 * 1024 * 30;
        if (fileSize > bigSize) {
            flag = true;
        } else {
            flag = false;
        }
        return flag;
    }


    public void OpenFile(File file, Activity activity) {
        if (file == null) {
            return;
        }
        if (file.exists()) {
            Uri path = Uri.fromFile(file);
            String type = getMIMEType(file);
            Log.d("chencj", type);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(path, type);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            try {
                activity.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                CustomToast.show(activity,
                    activity.getString(R.string.no_available_app_to_open_this_file),
                    Toast.LENGTH_LONG);
            }

        }
    }


    public void OpenFile1(File file, Activity activity) {
        if (file.exists()) {
            Uri path = Uri.fromFile(file);
            String type = getMIMEType(file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            //            intent.setDataAndType(path, type);
            intent.setType(type);
            // intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            List<ResolveInfo> resInfo = activity.getPackageManager()
                .queryIntentActivities(intent, 0);
            if (!resInfo.isEmpty()) {
                List<Intent> targetedShareIntents = new ArrayList<Intent>();
                for (ResolveInfo info : resInfo) {
                    Intent targeted = new Intent(Intent.ACTION_VIEW);
                    //                    intent.setDataAndType(path, type);
                    intent.setType(type);
                    ActivityInfo activityInfo = info.activityInfo;
                    // judgments : activityInfo.packageName, activityInfo.name,
                    // etc.
                    //                    if (activityInfo.packageName
                    //                            .contains("com.tencent.mobileqq")) {
                    //                        continue;
                    //                    }
                    targeted.setClassName(activityInfo.packageName, activityInfo.name);
                    targeted.setPackage(activityInfo.packageName);
                    targetedShareIntents.add(targeted);
                }

                if (targetedShareIntents.size() != 0) {
                    Intent chooserIntent = Intent.createChooser(
                        targetedShareIntents.remove(0),
                        "Select app to Open");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                        targetedShareIntents.toArray(new Parcelable[] {}));
                    activity.startActivity(chooserIntent);
                } else {
                    Toast.makeText(activity, R.string.no_select_program, Toast.LENGTH_SHORT)
                        .show();
                }

            }
            // try {
            // activity.startActivity(intent);
            // }
            // catch (ActivityNotFoundException e) {
            // Toast.makeText(activity,
            // "No Application Available to View PDF",
            // Toast.LENGTH_SHORT).show();
            // }

        }
    }


    @SuppressLint("DefaultLocale")
    private String getMIMEType(File file) {

        String type = "*/*";
        String fName = file.getName();
        // 获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex < 0) {
            return type;
        }
        /* 获取文件的后缀名 */
        String end = fName.substring(dotIndex, fName.length()).toLowerCase();
        if (end == "") {
            return type;
        }
        // 在MIME和文件类型的匹配表中找到对应的MIME类型。
        for (int i = 0;
             i < MIME_MapTable.length;
             i++) { // MIME_MapTable??在这里你一定有疑问，这个MIME_MapTable是什么？
            if (end.equals(MIME_MapTable[i][0])) {
                type = MIME_MapTable[i][1];
            }
        }
        return type;
    }


    private final String[][] MIME_MapTable = {
        // {后缀名，MIME类型}
        { ".3gp", "video/3gpp" },
        { ".apk", "application/vnd.android.package-archive" },
        { ".asf", "video/x-ms-asf" },
        { ".avi", "video/x-msvideo" },
        { ".bin", "application/octet-stream" },
        { ".bmp", "image/bmp" },
        { ".c", "text/plain" },
        { ".class", "application/octet-stream" },
        { ".conf", "text/plain" },
        { ".cpp", "text/plain" },
        { ".doc", "application/msword" },
        { ".docx",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" },
        { ".xls", "application/vnd.ms-excel" },
        { ".xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" },
        { ".exe", "application/octet-stream" },
        { ".gif", "image/gif" },
        { ".gtar", "application/x-gtar" },
        { ".gz", "application/x-gzip" },
        { ".h", "text/plain" },
        { ".htm", "text/html" },
        { ".html", "text/html" },
        { ".jar", "application/java-archive" },
        { ".java", "text/plain" },
        { ".jpeg", "image/jpeg" },
        { ".jpg", "image/jpeg" },
        { ".js", "application/x-javascript" },
        { ".log", "text/plain" },
        { ".m3u", "audio/x-mpegurl" },
        { ".m4a", "audio/mp4a-latm" },
        { ".m4b", "audio/mp4a-latm" },
        { ".m4p", "audio/mp4a-latm" },
        { ".m4u", "video/vnd.mpegurl" },
        { ".m4v", "video/x-m4v" },
        { ".mov", "video/quicktime" },
        { ".mp2", "audio/x-mpeg" },
        { ".mp3", "audio/x-mpeg" },
        { ".mp4", "video/mp4" },
        { ".mpc", "application/vnd.mpohun.certificate" },
        { ".mpe", "video/mpeg" },
        { ".mpeg", "video/mpeg" },
        { ".mpg", "video/mpeg" },
        { ".mpg4", "video/mp4" },
        { ".mpga", "audio/mpeg" },
        { ".msg", "application/vnd.ms-outlook" },
        { ".ogg", "audio/ogg" },
        { ".pdf", "application/pdf" },
        { ".png", "image/png" },
        { ".pps", "application/vnd.ms-powerpoint" },
        { ".ppt", "application/vnd.ms-powerpoint" },
        { ".pptx",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation" },
        { ".prop", "text/plain" }, { ".rc", "text/plain" },
        { ".rmvb", "audio/x-pn-realaudio" }, { ".rtf", "application/rtf" },
        { ".sh", "text/plain" }, { ".tar", "application/x-tar" },
        { ".tgz", "application/x-compressed" }, { ".txt", "text/plain" },
        { ".wav", "audio/x-wav" }, { ".wma", "audio/x-ms-wma" },
        { ".wmv", "audio/x-ms-wmv" },
        { ".wps", "application/vnd.ms-works" }, { ".xml", "text/plain" },
        { ".z", "application/x-compress" },
        { ".zip", "application/x-zip-compressed" }, { "", "*/*" } };


    public void gotoAddCollectionACtivity(Context context) {
        //        Intent i = new Intent(context, ButelAddCollectionActivity.class);
        //        context.startActivity(i);
    }


    public void gotoCollectionTextActivity(Context context, CollectionBean bean) {
        //        Intent intent = new Intent(context, CollectionTextFileActivity.class);
        //        intent.putExtra(CollectionTextFileActivity.COLLECTION_TEXT_DATA, bean);
        //        context.startActivity(intent);
    }


    public void gotoCollectionFileActivity(Context context, CollectionBean bean) {
        //        Intent intent = new Intent(context, CollectionFileDetilActivity.class);
        //        intent.putExtra(CollectionFileDetilActivity.COLLECTION_FILE_DATA, bean);
        //        context.startActivity(intent);
    }


    public void gotoCollectionFileForNoticeActivity(Context context, NoticesBean bean) {
        CustomLog.i(TAG, "gotoCollectionFileForNoticeActivity()");

        int fileType = -1;

        try {
            JSONArray bodyInfoJSONArray = new JSONArray(bean.getBody());
            JSONObject bodyInfoJSONObject = bodyInfoJSONArray.getJSONObject(0);
            String remoteUrl = bodyInfoJSONObject.optString("remoteUrl");

            if (remoteUrl.endsWith(".pdf")) {
                fileType = PDF;
            } else if (remoteUrl.endsWith(".doc")) {
                fileType = DOC;
            } else if (remoteUrl.endsWith(".docx")) {
                fileType = DOCX;
            } else {
                CustomToast.show(context, context.getString(R.string.not_allow_open),
                    CustomToast.LENGTH_LONG);
                return;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(context, CollectionFileDetilActivity.class);
        intent.putExtra(CollectionFileDetilActivity.COLLECTION_FILE_TYPE, 1);
        intent.putExtra(CollectionFileDetilActivity.COLLECTION_FILE_DATA, bean);
        intent.putExtra(CollectionFileDetilActivity.ATTACHMENT_FILE_TYPE, fileType);
        context.startActivity(intent);
    }


    public final static int COLLECTION_PIC_FROM_NATIVE = 1;
    public final static int COLLECTION_PIC_FROM_CAMERA = 2;


    public void sendPicFromNative(Activity activity) {
        CustomLog.d(TAG, "选择图片 begin");
        Intent i = new Intent(activity, MultiBucketChooserActivity.class);
        i.putExtra(MultiBucketChooserActivity.KEY_BUCKET_TYPE,
            MultiBucketChooserActivity.BUCKET_TYPE_IMAGE);
        i.putExtra(MultiBucketChooserActivity.KEY_FROM_TYPE,
            MultiBucketChooserActivity.FROM_TYPE_COLLECT);
        activity.startActivityForResult(i, COLLECTION_PIC_FROM_NATIVE);
        CustomLog.d(TAG, "选择图片 end");
    }


    /**
     * 调用系统相机拍照
     */
    private String cameraFilePath;


    public void sendPicFromCamera(Activity activity) {
        CustomLog.d(TAG, "用系统相机拍照 begin");
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            try {
                File mCurPhotoFile = new File(
                    FileTaskManager.getTakePhotoDir(),
                    IMCommonUtil.makeCusPhotoFileName());
                cameraFilePath = mCurPhotoFile.getAbsolutePath();
                final Intent intent = IMCommonUtil
                    .getTakePickIntent(mCurPhotoFile);
                activity.startActivityForResult(intent,
                    COLLECTION_PIC_FROM_CAMERA);
            } catch (Exception e) {
                CustomLog.e(TAG, "Exception" + e.toString());
                showToast(
                    activity,
                    activity.getResources().getString(
                        R.string.taker_not_found));
            }
        } else {
            showToast(activity,
                activity.getResources().getString(R.string.sd_unfound));
        }
        CustomLog.d(TAG, "用系统相机拍照 end");
    }


    public void onPicFromCameraBack(final Activity activity) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(cameraFilePath)) {
                    IMCommonUtil.scanFileAsync(activity, cameraFilePath);
                }
                List<String> files = new ArrayList<String>();
                files.add(cameraFilePath);
                CollectionManager.getInstance().addCollectionFromPic(activity,
                    files);
            }
        }).start();
    }


    public void onPicFromNativeBack(final Activity activity, final Intent data) {
        if (data == null) {
            CustomLog.d(TAG, "data==null");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                CollectionManager.getInstance().addCollectionFromPic(
                    activity,
                    data.getExtras()
                        .getStringArrayList(Intent.EXTRA_STREAM));
            }
        }).start();
    }


    protected static void showToast(Context context, String toast) {
        Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
        CustomLog.d("CollectionFIleManage", toast);
    }


    @SuppressLint("DefaultLocale")
    public String getFiletypeByFileName(String fName) {
        String type = "";
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex < 0) {
            return type;
        }
        return fName.substring(dotIndex + 1, fName.length()).toLowerCase();
    }


    /**
     * 根据文件类型获取文件详情图标
     *
     * @param fName 文件名
     * @return 文件详情图标资源ID
     */
    public int getCollectFileDetailDrawableId(String fName) {
        int type = R.drawable.collection_default;
        String end = getFiletypeByFileName(fName);
        if (end == "") {
            return type;
        }
        if (isDocFile(end)) {
            type = R.drawable.collection_word;
        } else if (isXlsFile(end)) {
            type = R.drawable.collection_execl;
        } else if (end.equalsIgnoreCase("pdf")) {
            type = R.drawable.collection_pdf;
        } else if (isPptFile(end)) {
            type = R.drawable.collection_ppt;
        } else if (end.equalsIgnoreCase("txt")) {
            type = R.drawable.collection_text;
        } else if (isAudioFile(end)) {
            type = R.drawable.collection_detail_audio;
        } else if (isVedioFile(end)) {
            type = R.drawable.collection_detail_video;
        } else if (end.equalsIgnoreCase("vsd")) {
            type = R.drawable.collection_visio;
        } else if (isFlaFile(end)) {
            type = R.drawable.collection_fla;
        }
        return type;
    }


    /**
     * 根据文件类型获取文件图标
     *
     * @param typeName 文件名
     * @return 文件图标资源ID
     */
    public int getCollectFileDrawableId(String typeName) {
        int type = R.drawable.collection_default2;
        if (typeName == "") {
            return type;
        }
        if (isDocFile(typeName)) {
            type = R.drawable.collection_word2;
        } else if (isXlsFile(typeName)) {
            type = R.drawable.collection_execl2;
        } else if (typeName.equalsIgnoreCase("pdf")) {
            type = R.drawable.collection_pdf2;
        } else if (isPptFile(typeName)) {
            type = R.drawable.collection_ppt2;
        } else if (typeName.equalsIgnoreCase("txt")) {
            type = R.drawable.collection_text2;
        } else if (isAudioFile(typeName)) {
            type = R.drawable.collection_audio;
        } else if (isVedioFile(typeName)) {
            type = R.drawable.collection_video;
        } else if (typeName.equalsIgnoreCase("vsd")) {
            type = R.drawable.collection_visio2;
        } else if (isFlaFile(typeName)) {
            type = R.drawable.collection_fla2;
        }
        return type;
    }


    /**
     * 根据文件类型获取消息列表文件图标
     *
     * @param typeName 文件名
     * @return 消息列表文件图标资源ID
     */
    public int getNoticeFileDrawableId(String typeName) {
        int type = R.drawable.notice_file_default;
        if (typeName == "") {
            return type;
        }
        if (isDocFile(typeName)) {
            type = R.drawable.notice_file_word;
        } else if (isXlsFile(typeName)) {
            type = R.drawable.notice_file_xls;
        } else if (typeName.equalsIgnoreCase("pdf")) {
            type = R.drawable.notice_file_pdf;
        } else if (isPptFile(typeName)) {
            type = R.drawable.notice_file_ppt;
        } else if (typeName.equalsIgnoreCase("txt")) {
            type = R.drawable.notice_file_txt;
        } else if (isAudioFile(typeName)) {
            type = R.drawable.notice_file_audio;
        } else if (isVedioFile(typeName)) {
            type = R.drawable.notice_file_video;
        } else if (typeName.equalsIgnoreCase("vsd")) {
            type = R.drawable.notice_file_vis;
        } else if (isFlaFile(typeName)) {
            type = R.drawable.notice_file_swf;
        }
        return type;
    }


    private boolean isDocFile(String extension) {
        if (extension.equalsIgnoreCase("doc")
            || extension.equalsIgnoreCase("docx")) {
            return true;
        }
        return false;
    }


    private boolean isXlsFile(String extension) {
        if (extension.equalsIgnoreCase("xls")
            || extension.equalsIgnoreCase("xlsx")) {
            return true;
        }
        return false;
    }


    private boolean isPptFile(String extension) {
        if (extension.equalsIgnoreCase("ppt")
            || extension.equalsIgnoreCase("pptx")) {
            return true;
        }
        return false;
    }


    private boolean isFlaFile(String extension) {
        if (extension.equalsIgnoreCase("swf")
            || extension.equalsIgnoreCase("fla")) {
            return true;
        }
        return false;
    }


    private boolean isAudioFile(String extension) {
        if (AUDIO_FILE_EXTENSIONS.contains(extension.toLowerCase())) {
            return true;
        }
        return false;
    }


    private boolean isVedioFile(String extension) {
        if (VEDIO_FILE_EXTENSIONS.contains(extension.toLowerCase())) {
            return true;
        }
        return false;
    }


    /**
     * @param bean 收藏记录的转发调用
     */
    public void onMsgForward(Context mcontext, CollectionBean bean) {

        Intent i = new Intent(mcontext, ShareLocalActivity.class);
        i.putExtra(ShareLocalActivity.KEY_ACTION_FORWARD, true);
        i.putExtra(ShareLocalActivity.MSG_ID, bean.getId());
        i.putExtra(ShareLocalActivity.KEY_COLLECTION_FORWARD, 1);
        mcontext.startActivity(i);
    }


    public void onCollectMsgForward(Context mcontext, DataBodyInfo bean) {

        Intent mIntent = new Intent(mcontext, ShareLocalActivity.class);
        Bundle mBundle = new Bundle();
        mBundle.putSerializable(ShareLocalActivity.KEY_COLLECTION_ITEM_INFO, bean);
        mIntent.putExtras(mBundle);
        mcontext.startActivity(mIntent);
    }

    /**
     */

    /**
     * @param bean 跳转到图片和视频浏览页面
     */
    public void gotoVieWPohto(Context mcontext, CollectionBean bean,
                              boolean isVedio) {
        Log.d("collectionccj",
            "body:" + bean.getBody() + "type:" + bean.getType() + "extinfo"
                + bean.getExtinfo());
        CustomLog.d(TAG, "collectionccj gotoVieWPohto body:" + bean.getBody()
            + "type:" + bean.getType() + "extinfo" + bean.getExtinfo());
        // 图片内容
        final ButelFileInfo fileInfo = ButelFileInfo.parseJsonStr(bean
            .getBody(), true);
        final boolean isSend = isCollectionBySelf(bean.getOperatorNube());
        // 查看图片/视频
        boolean exist = isValidFilePath(fileInfo.getLocalPath());
        if (isSend) {
            if (isVedio) {
                //                MobclickAgent.onEvent(mcontext,
                //                        UmengEventConstant.EVENT_PLAY_VEDIO);
                if (exist) {
                    // 本地视频存在，则直接播放
                    // 播放视频
                    Intent i = new Intent(mcontext, PlayVideoActivity.class);
                    i.putExtra(RecordedVideoActivity.KEY_VIDEO_FILE_PATH,
                        fileInfo.getLocalPath());
                    if (fileInfo.getDuration() == 0) {
                        i.putExtra(
                            RecordedVideoActivity.KEY_VIDEO_FILE_DURATION,
                            30);
                    } else {
                        i.putExtra(
                            RecordedVideoActivity.KEY_VIDEO_FILE_DURATION,
                            fileInfo.getDuration());
                    }
                    mcontext.startActivity(i);
                    return;
                } else {
                    if (TextUtils.isEmpty(fileInfo.getRemoteUrl())) {
                        // 本地不存在，且服务端也不存在的场合，无法查看
                        showToast(
                            mcontext,
                            mcontext.getResources().getString(
                                R.string.toast_no_video));
                        return;
                    }
                }
            } else {
                // MobclickAgent.onEvent(mContext,
                // UmengEventConstant.EVENT_PRE_PIC);
                // 图片的场合
                // 已没有数据源，提示已删除
                // if (!exist
                // && TextUtils.isEmpty(fileInfo.getRemoteUrl())) {
                // showToast(mcontext,mcontext.getResources().getString(
                // R.string.toast_no_pic));
                // return;
                // }
            }
        } else {
            // 友盟统计次数
            if (isVedio) {
                //                MobclickAgent.onEvent(mcontext,
                //                        UmengEventConstant.EVENT_PLAY_VEDIO);

            }
            // 接收的视频，本地已存在的场合，直接播放
            if (isVedio && exist) {
                // 本地视频存在，则直接播放
                Intent i = new Intent(mcontext, PlayVideoActivity.class);
                i.putExtra(RecordedVideoActivity.KEY_VIDEO_FILE_PATH,
                    fileInfo.getLocalPath());
                if (fileInfo.getDuration() == 0) {
                    i.putExtra(RecordedVideoActivity.KEY_VIDEO_FILE_DURATION,
                        30);
                } else {
                    i.putExtra(RecordedVideoActivity.KEY_VIDEO_FILE_DURATION,
                        fileInfo.getDuration());
                }
                mcontext.startActivity(i);
                return;
            }
        }
        int type = -1;
        int index = -1;
        ArrayList<PhotoBean> photos = null;
        if (isVedio) {
            type = FileTaskManager.NOTICE_TYPE_VEDIO_SEND;
        } else {
            type = FileTaskManager.NOTICE_TYPE_PHOTO_SEND;
        }
        //        MobclickAgent.onEvent(mcontext, UmengEventConstant.EVENT_PRE_PIC);
        photos = getAllPAVFileList(bean, type);
        index = 0;

        Intent i = new Intent(mcontext, ViewPhotosActivity.class);
        i.putParcelableArrayListExtra(ViewPhotosActivity.KEY_PHOTOS_LIST,
            photos);
        i.putExtra(ViewPhotosActivity.KEY_PHOTOS_SELECT_INDEX, index);
        i.putExtra(ViewPhotosActivity.KEY_REMOTE_FILE, true);
        i.putExtra(ViewPhotosActivity.KEY_VIDEO_FILE, isVedio);
        i.putExtra(ViewPhotosActivity.KEY_VIDEO_LEN, fileInfo.getDuration());
        i.putExtra(ViewPhotosActivity.KEY_COLLECTION_TYPE, 1);
        mcontext.startActivity(i);
    }


    private boolean isValidFilePath(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        }
        if (file.length() == 0) {
            file.delete();
            return false;
        }
        if (filePath.endsWith(".temp")) {
            return false;
        }
        return true;
    }


    /**
     * 是否是自己收藏
     */
    public boolean isCollectionBySelf(String opraterNube) {
        String selfNubeNumber = MedicalApplication.getPreference()
            .getKeyValue(DaoPreference.PrefType.LOGIN_NUBENUMBER, "");
        return selfNubeNumber.equals(opraterNube);
    }


    private ArrayList<PhotoBean> getAllPAVFileList(CollectionBean bean, int type) {
        ArrayList<PhotoBean> photolist = new ArrayList<PhotoBean>();
        String body = bean.getBody();
        String thumbUrl = "";
        String localUrl = "";
        String remoteUrl = "";
        PhotoBean photoBean = null;
        String id = bean.getId();
        boolean isfrom = isCollectionBySelf(bean.getOperatorNube());
        try {
            JSONArray array = new JSONArray(body);
            if (array != null && array.length() > 0) {
                for (int j = 0; j < array.length(); j++) {
                    JSONObject obj = array.getJSONObject(j);
                    CustomLog.d(TAG, "collectionccj obj" + obj.toString());
                    thumbUrl = obj.optString("thumbnailRemoteUrl");
                    localUrl = obj.optString("localUrl");
                    remoteUrl = obj.optString("remoteUrl");

                    photoBean = new PhotoBean();
                    photoBean.setLittlePicUrl(thumbUrl);
                    photoBean.setLocalPath(localUrl);
                    photoBean.setRemoteUrl(remoteUrl);
                    photoBean.setTaskId(id);
                    photoBean.setType(type);
                    photoBean.setFrom(isfrom);

                    photolist.add(photoBean);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return photolist;
    }


    public ArrayList<PhotoBean> getAllPFileList(CollectionEntity bean, int type) {
        ArrayList<PhotoBean> photolist = new ArrayList<PhotoBean>();
        String body = bean.getBody();
        String thumbUrl = "";
        String localUrl = "";
        String remoteUrl = "";
        PhotoBean photoBean = null;
        String id = bean.getId();
        boolean isfrom = isCollectionBySelf(bean.getOperatorNube());
        try {
            JSONArray array = new JSONArray(body);
            if (array != null && array.length() > 0) {
                for (int j = 0; j < array.length(); j++) {
                    JSONObject obj = array.getJSONObject(j);
                    CustomLog.d(TAG, "collectionccj" + "obj" + obj.toString());
                    thumbUrl = obj.optString("thumbnailRemoteUrl");
                    localUrl = obj.optString("localUrl");
                    remoteUrl = obj.optString("remoteUrl");

                    photoBean = new PhotoBean();
                    photoBean.setLittlePicUrl(thumbUrl);
                    photoBean.setLocalPath(localUrl);
                    photoBean.setRemoteUrl(remoteUrl);
                    photoBean.setTaskId(id);
                    photoBean.setType(type);
                    photoBean.setFrom(isfrom);

                    photolist.add(photoBean);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return photolist;
    }
}
