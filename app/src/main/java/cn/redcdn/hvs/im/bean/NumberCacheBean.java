package cn.redcdn.hvs.im.bean;

import java.io.Serializable;

public class NumberCacheBean implements Serializable{

    private static final long serialVersionUID = -4564932643117993992L;

    /**
     * 系统生成唯一ID，uuid
     */
    private String id="";
    /**
     * 手机号码
     */
    private String phonenumber ="";
    /**
     * 视频号码
     */
    private String nebunumber = "";

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getPhonenumber() {
        return phonenumber;
    }
    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }
    public String getNebunumber() {
        return nebunumber;
    }
    public void setNebunumber(String nebunumber) {
        this.nebunumber = nebunumber;
    }


}
