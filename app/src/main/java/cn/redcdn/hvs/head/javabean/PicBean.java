package cn.redcdn.hvs.head.javabean;

/**
 * Created by Administrator on 2017/7/26.
 */

public class PicBean {
    public PicBean(String title, int type,String officialId) {
        this.title = title;
        this.type = type;
        this.officialId=officialId;
    }

    public PicBean(String title, int type) {
        this.title = title;
        this.type = type;
    }

    public PicBean(String picUrl, String information, int type) {
        this.picUrl = picUrl;
        this.information = information;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    private String picUrl;
    private String title;
    private int type;
    private String information;

    public String getOfficialId() {
        return officialId;
    }

    public void setOfficialId(String officialId) {
        this.officialId = officialId;
    }

    private  String officialId;

    public String getArticleId() {
        return ArticleId;
    }

    public void setArticleId(String articleId) {
        ArticleId = articleId;
    }

    public int getArticleType() {
        return ArticleType;
    }

    public void setArticleType(int articleType) {
        ArticleType = articleType;
    }

    String ArticleId;

    public PicBean(String picUrl, int type, String information, String articleId, int articleType) {
        this.picUrl = picUrl;
        this.type = type;
        this.information = information;
        ArticleId = articleId;
        ArticleType = articleType;
    }


    int ArticleType;
}
