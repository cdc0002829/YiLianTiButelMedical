package cn.redcdn.hvs.im.bean;

import android.text.TextUtils;
import com.butel.connectevent.utils.LogUtil;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 * @ClassName: ButelFileInfo.java
 * @Description: 文件信息
 * @author: gtzha
 * @date: 2014年11月27日
 */
public class ButelFileInfo {

    private String localPath = "";
    private String remoteUrl = "";
    private String thumbUrl = "";
    private String fileName="";
    private String fileType="";
    private long size=0;//文件大小
    private int width = 0;
    private int height = 0;
    private int duration = 0;

    public String getFileType() {
        return fileType;
    }
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getSize() {
        return size;
    }
    public void setSize(long size) {
        this.size = size;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public static ButelFileInfo parseJsonStr(String jsonStr, boolean isCollection) {
        List<ButelFileInfo> fileInfos = parseJsonArrary(jsonStr, isCollection);
        ButelFileInfo info=new ButelFileInfo();
        if (fileInfos!=null&&fileInfos.size()>0){
            info=fileInfos.get(0);
        }
        return info;
    }

    /**
     *
     * @param jsonStr
     * @return
     */
    public static List<ButelFileInfo> parseJsonArrary(String jsonStr, boolean isCollection) {
        List<ButelFileInfo> fileInfos = new ArrayList<ButelFileInfo>();
        if (TextUtils.isEmpty(jsonStr)) {
            return fileInfos;
        }
        try {
            JSONArray bodyArray = new JSONArray(jsonStr);
            if (bodyArray != null && bodyArray.length() > 0) {
                for (int i=0;i<bodyArray.length();i++){
                    ButelFileInfo info=new ButelFileInfo();
                    JSONObject bodyObj = bodyArray.optJSONObject(i);
                    info.setLocalPath(bodyObj.optString("localUrl"));
                    if (isCollection) {
                        info.setThumbUrl(bodyObj.optString("thumbnailRemoteUrl"));
                        info.setWidth(bodyObj.optInt("photoWidth"));
                        info.setHeight(bodyObj.optInt("photoHeight"));
                    } else {
                        info.setThumbUrl(bodyObj.optString("thumbnail"));
                        info.setWidth(bodyObj.optInt("width"));
                        info.setHeight(bodyObj.optInt("height"));
                    }
                    info.setRemoteUrl(bodyObj.optString("remoteUrl"));
                    info.setFileName(bodyObj.optString("fileName"));
                    info.setFileType(bodyObj.optString("fileType"));
                    info.setSize(bodyObj.optLong("size"));
                    info.setDuration(bodyObj.optInt("duration"));
                    fileInfos.add(info);
                }
            }
        } catch (Exception e) {
            LogUtil.e("JSONArray Exception", e);
        }
        return fileInfos;
    }

    public static String toCollectionRecordBody(ButelFileInfo info) {
        try {
            JSONArray bodyArray = new JSONArray();
            JSONObject bodyObj = new JSONObject();
            bodyObj.put("localUrl",info.getLocalPath());
            bodyObj.put("thumbnailRemoteUrl",info.getThumbUrl());
            bodyObj.put("remoteUrl",info.getRemoteUrl());
            bodyObj.put("fileName", info.getFileName());
            bodyObj.put("fileType",info.getFileType());
            bodyObj.put("size",info.getSize());
            bodyObj.put("photoWidth",info.getWidth());
            bodyObj.put("photoHeight",info.getHeight());
            bodyObj.put("duration",info.getDuration());
            bodyArray.put(bodyObj);
            return bodyArray.toString();
        } catch (Exception e) {
            LogUtil.e("JSONArray Exception", e);
            return"";
        }
    }
}
