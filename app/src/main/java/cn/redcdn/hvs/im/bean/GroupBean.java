package cn.redcdn.hvs.im.bean;

import java.io.Serializable;

/**
 * Desc
 * Created by wangkai on 2017/2/24.
 */

public class GroupBean implements Serializable {
    // 主键
    private int id = -1;
    // 群聊id
    private String gid = "";
    // 群聊名称
    private String gName = "";
    // 群聊头像
    private String headUrl = "";
    // 管理员nube号码
    private String mgrNube = "";
    // 群聊创建时间
    private String createTime = "";

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public String getgName() {
        return gName;
    }

    public void setgName(String gName) {
        this.gName = gName;
    }

    public String getHeadUrl() {
        return headUrl;
    }

    public void setHeadUrl(String headUrl) {
        this.headUrl = headUrl;
    }

    public String getMgrNube() {
        return mgrNube;
    }

    public void setMgrNube(String mgrNube) {
        this.mgrNube = mgrNube;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
