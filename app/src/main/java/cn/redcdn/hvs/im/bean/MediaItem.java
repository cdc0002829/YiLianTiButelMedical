package cn.redcdn.hvs.im.bean;

/**
 * Created by Administrator on 2017/5/15.
 */

public class MediaItem {
    public String name;
    public int bucketId;
    public  String num;

    public MediaItem() {
    }

    public MediaItem(String name, String num) {
        this.name = name;
        this.num = num;
    }

    public MediaItem(String name, int bucketId, String num) {

        this.name = name;
        this.bucketId = bucketId;
        this.num = num;
    }

    public String getName() {
        return name;

    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBucketId() {
        return bucketId;
    }

    public void setBucketId(int bucketId) {
        this.bucketId = bucketId;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }
}
