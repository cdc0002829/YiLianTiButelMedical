package cn.redcdn.hvs.im.collection;

/**
 * Desc
 * Created by wangkai on 2017/3/8.
 */

public class ButeleCollectionFile {

    /* 文件名 */
    private String mText = "";
    /* 文件大小 */
    private long fileSize;
    /* 文件路径 */
    private String filePath;
    /* 文件最后修改时间 */
    private long ModifiedDate = 0;
    /* 文件类型 */
    private String fileType;
    /* 是否是文件夹 */
    public boolean IsDir = false;

    public void setMtext(String name) {
        mText = name;
    }

    public String getMtext() {
        return mText;
    }

    public void setFileSize(long size) {
        fileSize = size;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFilePath(String Path) {
        filePath = Path;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setModifiedDate(long data) {
        ModifiedDate = data;
    }

    public long getModifiedDate() {
        return ModifiedDate;
    }

    public void setFileType(String type) {
        fileType = type;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileIsDir(boolean f) {
        IsDir = f;
    }

    public boolean getFileIsDir() {
        return IsDir;
    }
}
