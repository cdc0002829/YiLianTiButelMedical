package cn.redcdn.hvs.im.bean;

import cn.redcdn.hvs.im.column.GroupMemberTable;
import java.io.Serializable;

/**
 * Created by gtzha on 2015/6/16.
 */
public class GroupMemberBean implements Serializable {

    // 主键
    private int id = -1;
    // 群聊id
    private String gid = "";
    // 该用户在个人用户中心的UID
    private String uid = "";
    // 每个用户在每个群中的ID
    private String mid = "";
    // nube号码
    private String nubeNum = "";
    // 手机号码
    private String phoneNum = "";
    // 用户昵称
    private String nickName = "";
    // 群内备注名
    private String groupNick = "";
    // 用户头像
    private String headUrl = "";
    // 显示名称
    private String showName = "";
    // 联系人表备注名
    private String name = "";
    // 是否被移出
    private int removed = GroupMemberTable.REMOVED_FALSE;
    // 性别
    private int gender = GroupMemberTable.GENDER_MALE;

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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getNubeNum() {
        return nubeNum;
    }

    public void setNubeNum(String nubeNum) {
        this.nubeNum = nubeNum;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getGroupNick() {
        return groupNick;
    }

    public void setGroupNick(String groupNick) {
        this.groupNick = groupNick;
    }

    public String getShowName() {
        return showName;
    }

    public void setShowName(String showName) {
        this.showName = showName;
    }

    public String getHeadUrl() {
        return headUrl;
    }

    public void setHeadUrl(String headUrl) {
        this.headUrl = headUrl;
    }

    public int getRemoved() {
        return removed;
    }

    public void setRemoved(int removed) {
        this.removed = removed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getDispName() {
        return ShowNameUtil.getShowName(ShowNameUtil.getNameElement(name, nickName, phoneNum, nubeNum));
    }

    public boolean isMember() {
        if (removed == GroupMemberTable.REMOVED_FALSE) {
            return true;
        } else {
            return false;
        }
    }
}
