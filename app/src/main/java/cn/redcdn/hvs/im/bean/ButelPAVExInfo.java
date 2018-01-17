package cn.redcdn.hvs.im.bean;

/**
 * Desc
 * Created by wangkai on 2017/2/27.
 */

public class ButelPAVExInfo {

    /**
     * 图片的宽
     */
    private int width =0;
    /**
     * 图片的高
     */
    private int height =0;
    /**
     * 声音、视频的时长，单位秒
     */
    private int duration =0;

    public int getWide() {
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
}
