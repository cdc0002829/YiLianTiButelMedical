package cn.redcdn.hvs.im.bean;

import android.text.TextUtils;
import cn.redcdn.hvs.im.fileTask.ChangeUIInterface;
import cn.redcdn.hvs.im.fileTask.FileTaskManager;
import cn.redcdn.log.CustomLog;
import java.io.File;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Desc
 * Created by wangkai on 2017/2/24.
 */

public class FileTaskBean {
    private static final String TAG = FileTaskBean.class.getSimpleName();

    /**
     * 保存上传文件的本地地址或是下载文件的网络链接
     */
    private String srcUrl = "";
    /**
     * 保存上传文件后返回的网络地址或下载文件的本地路径
     */
    private String resultUrl = "";
    /**
     * 消息的本地记录的uuid
     */
    private String uuid = "";
    /**
     * 此任务的类型（图片上传、图片下载、录音上传、录音下载、名片上传、名片下载）
     */
    private int type = -1;

    /**
     * 该文件在本次任务中（同一uuid）的位置
     */
    private int index = 0;
    /**
     * 本次任务中共有几个文件需要上传或下载
     */
    private int total_count = 0;
    /**
     * 文件上传或下载状态 0：准备中 1：进行中 2：成功 3：失败
     */
    private int status = 0;
    /**
     * 文件的大小记录，在下载时保存在表中，在下次断点续传时做简单的判断文件是否下载完成
     */
    private long filesize = 0;
    /**
     * 在overSized==true的情况下，大图片文件被压缩成小文件，此字段保存小文件的绝对路径
     */
    private String compressedPath = "";
    //    /**
    //     * 标识是否需要压缩图片文件
    //     */
    //    private boolean overSized = false;
    /***
     * 1.5版中增加 缩略图的连接
     */
    private String thumbnailUrl = "";
    /***
     * 1.5版中增加 是否是用户主动点击的单个文件下载
     */
    private boolean singleDownload = false;
    /***
     * 1.5版中增加 用于返回服务器端的错误提示
     */
    private String errorTip = null;
    /**
     * 20141114 add 表示消息的来源
     * true:接收到的消息
     * false:发送的消息
     */
    private boolean from = false;
    /**
     * 20141114 add
     * 数据库表中body字段Json数组中每个Item的原始数据数据
     * 为方便修改其中的对象值
     */
    private String rawBodyItemData = "";


    private String id = "";

    private int pos = -1;




    public void setPos(int pos) {
        this.pos = pos;
    }


    public int getPos() {
        return pos;
    }


    public void setId(String uid) {
        id = uid;
    }


    public String getId() {
        return id;
    }


    public String getRawBodyItemData() {
        return rawBodyItemData;
    }


    public void setRawBodyItemData(String rawBodyItemData) {
        this.rawBodyItemData = rawBodyItemData;
    }


    public boolean isFrom() {
        return from;
    }


    public void setFrom(boolean from) {
        this.from = from;
    }


    public String getErrorTip() {
        return errorTip;
    }


    public void setErrorTip(String errorTip) {
        this.errorTip = errorTip;
    }


    public boolean isSingleDownload() {
        return singleDownload;
    }


    public void setSingleDownload(boolean singleDownload) {
        this.singleDownload = singleDownload;
    }


    public String getThumbnailUrl() {
        return thumbnailUrl;
    }


    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }


    public String getCompressedPath() {
        return compressedPath;
    }


    public void setCompressedPath(String compressedPath) {
        this.compressedPath = compressedPath;
    }

    //    public boolean isOverSized() {
    //        return overSized;
    //    }
    //
    //    public void setOverSized(boolean overSized) {
    //        this.overSized = overSized;
    //    }

    // =======以上为保存下载或上传的基本信息和结果================
    // =======以下为上传或下载过程中的实时信息===================
    /**
     * 待上传或下载的文件大小
     */
    private long total = 0;
    /**
     * 已下载或上传的部分size
     */
    private long current = 0;
    /**
     * 上个进度的值，避免同一进度上报多次，引起页面的无效刷新
     */
    private long pre = 0;
    /**
     * 成功时返回的response 内原始string， 从中可以解析得到resultUrl结果
     */
    private String success_result = "";
    /**
     * 失败的原因
     */
    private String fial_reason = "";

    /**
     * 实时的变化UI信息，展示进度或结果呈现
     */
    private ChangeUIInterface changui = null;
    /**
     * 暂停更新页面的进度，默认值为true
     */
    private boolean pauseUiChange = true;


    public String getSrcUrl() {
        return srcUrl;
    }


    public void setSrcUrl(String srcUrl) {
        this.srcUrl = srcUrl;
    }


    public String getResultUrl() {
        return resultUrl;
    }


    public void setResultUrl(String resultUrl) {
        this.resultUrl = resultUrl;
    }


    public String getUuid() {
        return uuid;
    }


    public void setUuid(String uuid) {
        this.uuid = uuid;
    }


    public int getIndex() {
        return index;
    }


    public void setIndex(int index) {
        this.index = index;
    }


    public int getTotal_count() {
        return total_count;
    }


    public void setTotal_count(int total_count) {
        this.total_count = total_count;
    }


    public int getStatus() {
        return status;
    }


    public void setStatus(int status) {
        this.status = status;
    }


    public int getType() {
        return type;
    }


    public void setType(int type) {
        this.type = type;
    }


    public long getFilesize() {
        return filesize;
    }


    public void setFilesize(long filesize) {
        this.filesize = filesize;
    }


    public boolean isPauseUiChange() {
        return pauseUiChange;
    }


    public void setPauseUiChange(boolean pauseUiChange) {
        this.pauseUiChange = pauseUiChange;
    }


    public long getTotal() {
        return total;
    }


    public void setTotal(long total) {
        this.total = total;
    }


    public long getCurrent() {
        return current;
    }


    public void setCurrent(long current) {
        this.current = current;
        setStatus(FileTaskManager.TASK_STATUS_RUNNING);
        if (changui != null && !pauseUiChange) {

            if (current == 0) {
                changui.onStart(this);
            }

            changui.onProcessing(this, current, total);
        }
    }


    public void setCurrentSCIM(long mCurrent) {
        this.current = mCurrent;
        this.total = 100;
        setStatus(FileTaskManager.TASK_STATUS_RUNNING);
        if (changui != null && !pauseUiChange) {
            if (mCurrent == 0) {
                changui.onStart(this);
                return;
            }
            if (mCurrent == 100) {
                changui.onSuccess(this, "sdk connect upload file complete");
                return;
            }
            changui.onProcessing(this, current, total);
        }
    }


    public String getSuccess_result() {
        return success_result;
    }


    public void setSuccess_result(String success_result) {
        this.success_result = success_result;
        setStatus(FileTaskManager.TASK_STATUS_SUCCESS);
        if (current != total && current < total) {
            current = total;
        }
        // TODO:在下载文件DownFileRequestCallBack-〉onSuccess方法中
        // 先调用setSuccess_result(String
        // success_result)，再handler通知FileTaskManager修改.temp文件后缀
        // 可能会出现因changui回调方法中访问该文件，以致修改失败，故在此处先修改文件名
        renameDownloadFile();
        if (changui != null && !pauseUiChange) {
            if (success_result.endsWith(".temp")) {
                changui.onSuccess(this, success_result.replace(".temp", ""));
            } else {
                changui.onSuccess(this, success_result);
            }
        }
    }


    public String getFial_reason() {
        return fial_reason;
    }


    public void setFial_reason(String fial_reason) {
        this.fial_reason = fial_reason;
        setStatus(FileTaskManager.TASK_STATUS_FAIL);
        if (changui != null && !pauseUiChange) {
            changui.onFailure(this, null, fial_reason);
        }
    }


    public ChangeUIInterface getChangui() {
        return changui;
    }


    public void setChangui(ChangeUIInterface changui) {
        this.changui = changui;
    }


    private void renameDownloadFile() {
        CustomLog.i(TAG, "renameDownloadFile()");

        switch (this.type) {
            case FileTaskManager.NOTICE_TYPE_VEDIO_SEND:
            case FileTaskManager.NOTICE_TYPE_AUDIO_SEND:
            case FileTaskManager.NOTICE_TYPE_VCARD_SEND:
            case FileTaskManager.NOTICE_TYPE_PHOTO_SEND:
                if (this.from) {
                    String tempfilepath = success_result;
                    if (!TextUtils.isEmpty(tempfilepath)) {
                        if (tempfilepath.endsWith(".temp")) {
                            tempfilepath = tempfilepath.replace(".temp", "");
                            File newFile = new File(tempfilepath);
                            if (newFile != null && newFile.exists()) {
                                this.resultUrl = tempfilepath;
                                this.success_result = tempfilepath;
                                break;
                            } else {
                                File old = new File(success_result);
                                if (old != null && old.exists()) {
                                    if (old.renameTo(newFile)) {
                                        this.resultUrl = tempfilepath;
                                        this.success_result = tempfilepath;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                break;
        }
    }


    public void convertSuccessStringToResultUrl() {
        convertSuccessStringToResultUrl(this.success_result);
    }


    /***
     * 根据不同的type，把success_result解析成目标结果
     */
    public void convertSuccessStringToResultUrl(String success_result) {
        CustomLog.d("FiletaskBean", "convertSuccessStringToResultUrl begin");
        if (TextUtils.isEmpty(success_result)) {
            return;
        }
        switch (this.type) {
            case FileTaskManager.NOTICE_TYPE_PHOTO_SEND: {
                if (this.from) {
                    renameDownloadFile();
                    this.resultUrl = success_result;
                    this.status = FileTaskManager.TASK_STATUS_SUCCESS;
                    break;
                }
                JSONObject resp;
                try {
                    resp = new JSONObject(success_result);
                    if (!resp.isNull("ok")) {
                        if (!resp.getString("ok").equals("1")) {
                            // miaolk add 20140314 仅对服务器端返回的不正确的结果
                            // Toast 给用户知道
                            CustomLog.d("FiletaskBean", "发送失败:" + resp.optString("error"));
                            errorTip = "发送失败";

                            String reason = "发送相片失败(S)，网络异常("
                                + resp.getString("ok") + ")，请稍后重试。";
                            setFial_reason(reason);
                            break;
                        }
                        // 部分图片在服务器端转储后，手机端无法显示，故此处读取源图连接
                        // 游 要求使用源图连接 20130917
                        if (!resp.isNull("originalImagePath")) {
                            this.resultUrl = resp.optString("originalImagePath");
                            if (!resp.isNull("littleImagePath")) {
                                this.thumbnailUrl = resp
                                    .optString("littleImagePath");
                            }
                            if (!TextUtils.isEmpty(this.resultUrl)) {
                                this.status = FileTaskManager.TASK_STATUS_SUCCESS;
                            } else {
                                String reason = "发送失败,后台返回URL为空";
                                setFial_reason(reason);
                            }
                        } else {
                            String reason = "发送失败,后台返回URL JSON KEY为空";
                            setFial_reason(reason);
                        }
                    } else {
                        String reason = "发送相片失败(S)，网络异常(无响应)，请稍后重试。";
                        setFial_reason(reason);
                        break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    String reason = "发送相片失败，JSON解析异常";
                    setFial_reason(reason);
                }

            }
            break;
            case FileTaskManager.NOTICE_TYPE_AUDIO_SEND:
            case FileTaskManager.NOTICE_TYPE_VEDIO_SEND:
            case FileTaskManager.NOTICE_TYPE_VCARD_SEND: {

                if (this.from) {
                    renameDownloadFile();
                    this.resultUrl = success_result;
                    this.status = FileTaskManager.TASK_STATUS_SUCCESS;
                    break;
                }

                JSONObject resp;
                try {
                    resp = new JSONObject(success_result);
                    if (!resp.isNull("ok")) {
                        if (!resp.getString("ok").equals("1")) {
                            // miaolk add 20140314 仅对服务器端返回的不正确的结果
                            // Toast 给用户知道
                            CustomLog.d("fileTaskBean", "发送失败:" + resp.optString("error"));
                            errorTip = "发送失败";

                            String reason = "发送名片失败(S)，网络异常("
                                + resp.getString("ok") + ")，请稍后重试。";
                            setFial_reason(reason);
                            break;
                        }
                        if (!resp.isNull("originalFilePath")) {
                            this.resultUrl = resp.optString("originalFilePath");
                            // 20141119 服务端产生了缩略图
                            if (!resp.isNull("picFilePath")) {
                                this.thumbnailUrl = resp.optString("picFilePath");
                            }

                            if (!TextUtils.isEmpty(this.resultUrl)) {
                                this.status = FileTaskManager.TASK_STATUS_SUCCESS;
                            } else {
                                String reason = "发送失败,后台返回URL为空";
                                setFial_reason(reason);
                            }
                        } else {
                            String reason = "发送失败,后台返回URL JSON KEY为空";
                            setFial_reason(reason);
                        }
                    } else {
                        String reason = "发送名片失败(S)，网络异常(无响应)，请稍后重试。";
                        setFial_reason(reason);
                        break;
                    }
                } catch (JSONException e) {
                    CustomLog.e("FileTaskBean", "JSONException" + e.toString());
                    e.printStackTrace();
                    String reason = "发送名片失败，JSON解析异常";
                    setFial_reason(reason);
                }

            }
            break;
            default:
        }
        CustomLog.d("FiletaskBean", "convertSuccessStringToResultUrl end");
    }


    public void convertSuccessStringToResultUrl(String filePath, boolean isVertical) {
        CustomLog.i(TAG, "convertSuccessStringToResultUrl()");

        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        switch (this.type) {
            case FileTaskManager.NOTICE_TYPE_PHOTO_SEND: {
                if (this.from) {
                    renameDownloadFile();
                    this.resultUrl = filePath;
                    this.status = FileTaskManager.TASK_STATUS_SUCCESS;
                    break;
                }
                this.resultUrl = filePath;
                if (isVertical) {
                    this.thumbnailUrl = filePath +
                        "?cmd=imageprocess/format/jpg/processtype/1/width/240/height/427";
                } else {
                    this.thumbnailUrl = filePath +
                        "?cmd=imageprocess/format/jpg/processtype/1/width/263/height/148";
                }
                this.status = FileTaskManager.TASK_STATUS_SUCCESS;
            }
            break;
            case FileTaskManager.NOTICE_TYPE_AUDIO_SEND:
            case FileTaskManager.NOTICE_TYPE_VCARD_SEND: {

                if (this.from) {
                    renameDownloadFile();
                    this.resultUrl = filePath;
                    this.status = FileTaskManager.TASK_STATUS_SUCCESS;
                    break;
                }
                this.resultUrl = filePath;
                this.thumbnailUrl = filePath;
                this.status = FileTaskManager.TASK_STATUS_SUCCESS;
            }
            break;
            default:
        }
    }


    /**
     * 填充 图片类型 IM bean ，相关字段
     */
    public void convertSuccessStringToResultUrlOfVideo(String success_result, String thumbnailUrl) {
        CustomLog.d("FiletaskBean", "convertSuccessStringToResultUrlOfPic begin");
        if (TextUtils.isEmpty(success_result)) {
            return;
        }
        if (this.from) {
            renameDownloadFile();
            this.resultUrl = success_result;
            this.status = FileTaskManager.TASK_STATUS_SUCCESS;
            return;
        }
        this.resultUrl = success_result;
        this.thumbnailUrl = thumbnailUrl;
        this.status = FileTaskManager.TASK_STATUS_SUCCESS;
    }

}
