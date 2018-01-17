package cn.redcdn.hvs.head.javabean;

/**
 * Created by Teacher on 2016/7/5.
 */
public class RollItem {
    String picPath;
    String title;
    String id;
    int ArticalType;

    public RollItem(String picPath, String title, String id, int articalType) {
        this.picPath = picPath;
        this.title = title;
        this.id = id;
        ArticalType = articalType;
    }

    public int getArticalType() {
        return ArticalType;
    }

    public void setArticalType(int articalType) {
        ArticalType = articalType;
    }

    public RollItem(String picPath, String title, String id) {
        this.picPath = picPath;
        this.title = title;
        this.id = id;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }



    public RollItem(String picPath, String title) {
        this.picPath = picPath;
        this.title = title;
    }

    public String getPicPath() {
        return picPath;
    }

    public String getTitle() {
        return title;
    }
}
