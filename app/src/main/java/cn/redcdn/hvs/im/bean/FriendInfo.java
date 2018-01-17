package cn.redcdn.hvs.im.bean;

/**
 * Desc  好友信息
 * Created by caiguo on 2017/2/25.
 */

public class FriendInfo {
    public static final int RELATION_TYPE_BOTH = 0;//已经是好友
    public static final int RELATION_TYPE_NEGATIVE = 2;//被请求加为好友状态
    public static final int RELATION_TYPE_NONE = -1;//无关系状态
    public static final int RELATION_TYPE_POSITIVE = 1;//请求加为好友状态
    public static final int HAS_DELETE = 1;//已经删除
    public static final int NOT_DELETE = 0;//没有删除


    private String nubeNumber;
    private String name;
    private String headUrl;
    private int relationType;
    private String email;
    private String workUnitType;
    private String workUnit;
    private String department;
    private String professional;
    private String officeTel;
    private int userFrom;// 用于判断显示手机号还是邮箱   // 用户来源 0:视讯号搜索，1:手机通讯录好友推荐，2:手机号搜索， 3:邮箱搜索,  4:二维码扫描,  5:群内添加,  6:陌生人聊天添加, 7:发起或接受加好友请求, 8:同步新的朋友添加
    private int isDeleted;
    private String number;//手机号码


    public FriendInfo() {
    }

    public FriendInfo(String nubeNumber, String name, int userFrom, int isDeleted) {
        this.nubeNumber = nubeNumber;
        this.name = name;
        this.userFrom = userFrom;
        this.isDeleted = isDeleted;
    }

    public FriendInfo(String nubeNumber, String name, String headUrl, int relationType, int userFrom, int isDeleted) {
        this.nubeNumber = nubeNumber;
        this.name = name;
        this.headUrl = headUrl;
        this.relationType = relationType;
        this.userFrom = userFrom;
        this.isDeleted = isDeleted;
    }


    public FriendInfo(String nubeNumber, String name, String headUrl, int relationType, int isDeleted) {
        this.nubeNumber = nubeNumber;
        this.name = name;
        this.headUrl = headUrl;
        this.relationType = relationType;
        this.isDeleted = isDeleted;
    }


    //收到陌生人消失的时候只有  视讯号
    public FriendInfo(String nubeNumber, String name, String headUrl, int relationType,
                      String email, String workUnitType, String workUnit, String department,
                      String professional, String officeTel, int userFrom, int isDeleted, String number) {
        this.nubeNumber = nubeNumber;
        this.name = name;
        this.headUrl = headUrl;
        this.relationType = relationType;
        this.email = email;
        this.workUnitType = workUnitType;
        this.workUnit = workUnit;
        this.department = department;
        this.professional = professional;
        this.officeTel = officeTel;
        this.userFrom = userFrom;
        this.isDeleted = isDeleted;
        this.number = number;
    }


    @Override
    public String toString() {
        return "FriendInfo{" +
            "department='" + department + '\'' +
            ", nubeNumber='" + nubeNumber + '\'' +
            ", name='" + name + '\'' +

            ", headUrl='" + headUrl + '\'' +
            ", relationType=" + relationType +
            ", email='" + email + '\'' +
            ", workUnitType='" + workUnitType + '\'' +
            ", workUnit='" + workUnit + '\'' +
            ", professional='" + professional + '\'' +
            ", officeTel='" + officeTel + '\'' +
            ", userFrom=" + userFrom +
            ", isDeleted=" + isDeleted +
            ", number='" + number + '\'' +
            '}';
    }


    public String getNubeNumber() {
        return nubeNumber;
    }


    public void setNubeNumber(String nubeNumber) {
        this.nubeNumber = nubeNumber;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getHeadUrl() {
        return headUrl;
    }


    public void setHeadUrl(String headUrl) {
        this.headUrl = headUrl;
    }


    public int getRelationType() {
        return relationType;
    }


    public void setRelationType(int relationType) {
        this.relationType = relationType;
    }


    public String getEmail() {
        return email;
    }


    public void setEmail(String email) {
        this.email = email;
    }


    public String getWorkUnitType() {
        return workUnitType;
    }


    public void setWorkUnitType(String workUnitType) {
        this.workUnitType = workUnitType;
    }


    public String getWorkUnit() {
        return workUnit;
    }


    public void setWorkUnit(String workUnit) {
        this.workUnit = workUnit;
    }


    public String getDepartment() {
        return department;
    }


    public void setDepartment(String department) {
        this.department = department;
    }


    public String getProfessional() {
        return professional;
    }


    public void setProfessional(String professional) {
        this.professional = professional;
    }


    public String getOfficeTel() {
        return officeTel;
    }


    public void setOfficeTel(String officeTel) {
        this.officeTel = officeTel;
    }


    public int getUserFrom() {
        return userFrom;
    }


    public void setUserFrom(int userFrom) {
        this.userFrom = userFrom;
    }


    public int getIsDeleted() {
        return isDeleted;
    }


    public void setIsDeleted(int isDeleted) {
        this.isDeleted = isDeleted;
    }


    public String getNumber() { return number; }


    public void setNumber(String number) { this.number = number; }

}
