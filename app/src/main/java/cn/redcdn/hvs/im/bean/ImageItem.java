package cn.redcdn.hvs.im.bean;

/**
 * Created by Administrator on 2017/5/16.
 */

public class ImageItem {
    int imageId;
    String ImagePath;
    int type;
    long duration;
    int time;

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public void setImagePath(String imagePath) {
        ImagePath = imagePath;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getImageId() {

        return imageId;
    }

    public String getImagePath() {
        return ImagePath;
    }

    public int getType() {
        return type;
    }
}
