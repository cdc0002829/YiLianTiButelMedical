package cn.redcdn.hvs.contacts.contact.interfaces;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import cn.redcdn.hvs.contacts.contact.StringHelper;

public class Contact implements Comparable<Contact>, Serializable {
    private static final long serialVersionUID = -4964117461177959453L;
    private String contactId; // 好友ID
    private String name; // 姓名、群名称
    private String nickname; // 昵称
    private String firstName; // 姓
    private long lastTime; // 时间戳
    private String number; // 号码
    private String nubeNumber; // Nube号码、群id号码
    private String picUrl; // 头像、群头像
    private String note;// 备注
    private String title;// 标题
    private String contactUserId;// nubeID
    private String appType;// mobile,n8
    private String pinYin;

    // ///////下面几个字段创建的时候要注意

    private int userType = 0;// 用户类型 0：全能用户 1：普通用户
    private int beAdded;// 推荐联系人是否添加 0:未添加 1：已添加
    private int beRecommended;// 0：普通推荐 1：最新推荐
    private int isDeleted;// 0：未删除 1：已删除
    private int syncStat;// 0:未同步，1：已同步
    private int userFrom; // 用户来源 0:视讯号搜索，1:手机通讯录好友推荐，2:手机号搜索， 3:邮箱搜索,  4:二维码扫描,  5:群内添加,  6:陌生人聊天添加, 7:发起或接受加好友请求, 8:同步新的朋友添加

    public static final int USER_FROM_SEND_OR_ACCEPT = 7;//7:发起或接受加好友请求

    public static final int USER_FROM_SYNC = 8;//8:同步新的朋友添加

    private String rawContactId;

    // 以下字段为JMeetingSDK 中需要使用
    private String headUrl = ""; // 头像
    private String fullPym = "";// 全拼
    private int deviceType;// 设备类型 1->M1, 2->N8J, 3->X1, 4->N7/N8, 5->MOBILE
    private int sex;// 性别 1男，2女
    private int juserType = 0;// 用户类型，0：普通用户；1：星标用户

    // 以下字段为医疗云平台项目新增字段
    private String email; // 邮箱
    private int accountType; //账号类型 0: 个人， 1：群
    private int workUnitType; //单位类型 1：医院, 2：公司
    private String workUnit;  //公司、医院名称
    private String department;  //科室、部门
    private String professional;  //职称、职位
    private String officeTel;  //科室电话、公司电话
    private String saveToContactsTime;  //保存到通讯录时间

    //以下是医联体共享通讯录添加的信息
    private String phuId;
    private String phuName;
    private long updateTime;

    public String getPhuId() {
        return phuId;
    }

    public String getPhuName() {
        return phuName;
    }

    public void setPhuId(String phuId) {
        this.phuId = phuId;
    }

    public void setPhuName(String phuName) {
        this.phuName = phuName;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setSaveToContactsTime(String saveToContactsTime) {
        this.saveToContactsTime = saveToContactsTime;
    }

    public String getSaveToContactsTime() {
        return this.saveToContactsTime == null ? "" : this.saveToContactsTime;
    }

    public void setOfficeTel(String officeTel) {
        this.officeTel = officeTel;
    }

    public String getOfficeTel() {
        return this.officeTel == null ? "" : this.officeTel;
    }

    public void setProfessional(String professional) {
        this.professional = professional;
    }

    public String getProfessional() {
        return this.professional == null ? "" : this.professional;
    }

    public void setWorkUnitType(int type) {
        this.workUnitType = type;
    }

    public int getWorkUnitType() {
        return this.workUnitType;
    }

    public void setWorkUnit(String workUnit) {
        this.workUnit = workUnit;
    }

    public String getWorkUnit() {
        return this.workUnit == null ? "" : this.workUnit;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDepartment() {
        return this.department == null ? "" : this.department;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return this.email == null ? "" : this.email;
    }

    public void setAccountType(int type) {
        this.accountType = type;
    }

    public int getAccountType() {
        return this.accountType;
    }


    public Contact() {
        super();
    }

    public Contact(String name) {
        super();
        this.name = name;
    }

    public Contact(String name, String firstName) {
        super();
        this.name = name;
        this.firstName = firstName;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getName() {
        if(null!=name){
            return name;
        }else if(null!=nickname){
            return nickname;
        }else{
            return "";
        }

    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getFirstName() {

        if(TextUtils.isEmpty(firstName)){
            return StringHelper.getHeadChar(fullPym);
        }else{
            return firstName;
        }

    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getNubeNumber() {
        return nubeNumber;
    }

    public void setNubeNumber(String nubeNumber) {
        this.nubeNumber = nubeNumber;
    }

    public String getPicUrl() {
        return headUrl;
    }

    public void setPicUrl(String picUrl) {
        this.headUrl = picUrl;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public int getBeAdded() {
        return beAdded;
    }

    public void setBeAdded(int beAdded) {
        this.beAdded = beAdded;
    }

    public int getBeRecommended() {
        return beRecommended;
    }

    public void setBeRecommended(int beRecommended) {
        this.beRecommended = beRecommended;
    }

    public int getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(int isDeleted) {
        this.isDeleted = isDeleted;
    }

    public int getSyncStat() {
        return syncStat;
    }

    public void setSyncStat(int syncStat) {
        this.syncStat = syncStat;
    }

    public String getContactUserId() {
        return contactUserId;
    }

    public void setContactUserId(String contactUserId) {
        this.contactUserId = contactUserId;
    }

    public int getUserFrom() {
        return userFrom;
    }

    public void setUserFrom(int userFrom) {
        this.userFrom = userFrom;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getRawContactId() {
        return rawContactId;
    }

    public void setRawContactId(String rawContactId) {
        this.rawContactId = rawContactId;
    }

    public String getPinYin() {
        return pinYin;
    }

    public void setPinYin(String pinYin) {
        this.pinYin = pinYin;
    }

    /***********
     * 以下方法为jmeetingSDK中使用
     ***************/
    public String getHeadUrl() {
        return headUrl;
    }

    public void setHeadUrl(String headUrl) {
        this.headUrl = headUrl;
    }

    public String getFullPym() {
        return fullPym;
    }

    public void setFullPym(String fullPym) {
        this.fullPym = fullPym;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public int getJmeetingUserType() {
        return juserType;
    }

    public void setJmeetingUserType(int userType) {
        this.juserType = userType;
    }

    /***************/

    @Override
    public String toString() {
        return "Contact [contactId=" + contactId + ", name=" + name + ", nickname"
                + nickname + ", firstName" + firstName + ", lastTime" + lastTime
                + ", number" + number + ", nubeNumber" + nubeNumber + ", picUrl"
                + picUrl + ", userType" + userType + ", beAdded" + beAdded
                + ", beRecommended" + beRecommended + ", isDeleted" + isDeleted
                + ", contactUserId" + contactUserId + ", userFrom" + userFrom
                + ", appType" + appType + "]";
    }

    @Override
    public int compareTo(Contact another) {
        // TODO Auto-generated method stub
        return 0;
    }

    public static void removeDuplicateWithOrder(List list) {
        Set set = new HashSet();
        List newList = new ArrayList();
        for (Iterator iter = list.iterator(); iter.hasNext(); ) {
            Object element = iter.next();
            if (set.add(element) && !newList.contains(element)) {
                newList.add(element);
            }
        }
        list.clear();
        list.addAll(newList);
        // for (int i = 0; i < list.size() - 1; i++) {
        // for (int j = list.size() - 1; j > i; j--) {
        // if (list.get(j).equals(list.get(i))) {
        // list.remove(j);
        // }
        // }
        // }
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        // result = PRIME * result + getId();
        return result;
    }

    public boolean equals(Object o) {

        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (getClass() != o.getClass()) {
            return false;
        }
        // CustomLog.d("contact.........", o.toString());
        Contact e = (Contact) o;
        if (this.getNumber() != null && e.getNubeNumber() != null) {
            if (this.getNubeNumber().isEmpty() && e.getNubeNumber().isEmpty()) {
                return false;
            } else {
                return (this.getNubeNumber().equals(e.getNubeNumber()));
            }
        } else {
            return false;
        }

    }
}
