package cn.redcdn.hvs.im.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.redcdn.hvs.im.column.NubeFriendColumn;

/**
 * Desc    好友信息
 * Created by wangkai on 2017/2/25.
 */

public class ContactFriendBean implements Serializable{
    private static final long serialVersionUID = 1L;

    private String contactId; // 好友ID
    private String name; // 姓名
    private String nickname; // 别名
    private String firstName; // 姓
    private String lastName; // 名
    private String lastTime; // 时间戳
    private int isOnline; // 是否在线（0:不在线,1:在线）
    private String number; // 号码
    private int isMutualTrust; // 表示该用户是否已认证 （默认为0，表示未认证,单项关注1：表示认证双向关注，2：已邀请，3：被邀请）
    private String nubeNumber; // Nube号码
    private String uid;
    private String sourcesId;// 系统联系人id
    private String headUrl; // 头像
    private String pym;
    private String localName;

    private String note;// 备注
    private String title;//标题
    private String orgNisation;//组织

    private String userType;//用户类型     0：普通用户    1：心型用户
    private String groupType;//1是星标用户列表        2是普通用户列表
    private String sex;//性别      0：未知，1：男，2：女
    private String inviteType;//邀请通讯录联系人类型     1：本地发现用户，2：本地系统通讯录
    private String showMoblie= NubeFriendColumn.MOBILE_INVISIBLE;//MOBILE_VISIBLE与MOBILE_INVISIBLE

    public String getShowMoblie() {
        return showMoblie;
    }

    public void setShowMoblie(String showMoblie) {
        this.showMoblie = showMoblie;
    }

    public String getInviteType() {
        return inviteType;
    }
    public void setInviteType(String inviteType) {
        this.inviteType = inviteType;
    }
    public String getSex() {
        return sex;
    }
    public void setSex(String sex) {
        this.sex = sex;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getOrgNisation() {
        return orgNisation;
    }
    public void setOrgNisation(String orgNisation) {
        this.orgNisation = orgNisation;
    }

    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }
    public String getLocalName() {
        return localName;
    }
    public void setLocalName(String localName) {
        this.localName = localName;
    }
    public String getPym() {
        return pym;
    }
    public void setPym(String pym) {
        this.pym = pym;
    }
    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
    public String getHeadUrl() {
        return headUrl;
    }
    public void setHeadUrl(String headUrl) {
        this.headUrl = headUrl;
    }


    public String getSourcesId() {
        return sourcesId;
    }
    public void setSourcesId(String sourcesId) {
        this.sourcesId = sourcesId;
    }
    public String getContactId() {
        return contactId;
    }
    public void setContactId(String contactId) {
        this.contactId = contactId;
    }
    public String getName() {
        return name;
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
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getLastTime() {
        return lastTime;
    }
    public void setLastTime(String lastTime) {
        this.lastTime = lastTime;
    }
    public int getIsOnline() {
        return isOnline;
    }
    public void setIsOnline(int isOnline) {
        this.isOnline = isOnline;
    }
    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }
    public int getIsMutualTrust() {
        return isMutualTrust;
    }
    public void setIsMutualTrust(int isMutualTrust) {
        this.isMutualTrust = isMutualTrust;
    }
    public String getNubeNumber() {
        return nubeNumber;
    }
    public void setNubeNumber(String nubeNumber) {
        this.nubeNumber = nubeNumber;
    }
    public String getUserType() {
        return userType;
    }
    public void setUserType(String userType) {
        this.userType = userType;
    }
    public String getGroupType() {
        return groupType;
    }
    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }



    /** 联系人电话信息 */
    public  static class PhoneInfo {
        /** 联系电话类型 */
        public int type;
        /** 联系电话 */
        public String number;
    }

    /** 联系人邮箱信息 */
    public static  class EmailInfo {
        /** 邮箱类型 */
        public int type;
        /** 邮箱 */
        public String email;
    }

    public List<ContactAddressInfo> getAddressInfo() {
        if (addressInfo == null) {
            addressInfo = new ArrayList<ContactFriendBean.ContactAddressInfo>();
        }
        return addressInfo;
    }

    public List<ContactAddressInfo> addressInfo = null;
    /** 联系人地址 */
    public static  class ContactAddressInfo {
        /** 地址类型 */
        public int type;
        /** 邮箱 */
        public String address;
    }

    public List<ContactBirthdayInfo> getBirthdayInfo() {
        if (birthdayInfo == null) {
            birthdayInfo = new ArrayList<ContactFriendBean.ContactBirthdayInfo>();
        }
        return birthdayInfo;
    }

    public List<ContactBirthdayInfo> birthdayInfo = null;
    /** 联系人生日 */
    public static  class ContactBirthdayInfo {

        public int type;

        public String birthday;
    }

    public List<ContactIMInfo> getcontactImInfo() {
        if (contactImInfo == null) {
            contactImInfo = new ArrayList<ContactFriendBean.ContactIMInfo>();
        }
        return contactImInfo;
    }

    public List<ContactIMInfo> contactImInfo = null;
    /** 及时消息 */
    public static  class ContactIMInfo {

        public int type;

        public String contactIm;
    }


    public List<WebSiteInfo> getWebsiteInfo() {
        if (webSiteList == null) {
            webSiteList = new ArrayList<WebSiteInfo>();
        }
        return webSiteList;
    }
    public List<WebSiteInfo> webSiteList = null;
    public static  class WebSiteInfo {

        public int type;

        public String webSiteItem;
    }
    public List<SystemContactNickname> getSystemContactNickname() {
        if (systemContactNickname == null) {
            systemContactNickname = new ArrayList<ContactFriendBean.SystemContactNickname>();
        }
        return systemContactNickname;
    }
    public List<SystemContactNickname> systemContactNickname = null;
    public static  class SystemContactNickname {

        public int type;

        public String nickNameItem;
    }
    public List<String> getHeadUrlList() {
        if (headUrlList == null) {
            headUrlList = new ArrayList<String>();
        }
        return headUrlList;
    }

    public List<String> getNickNameList() {
        if (nickNameList == null) {
            nickNameList = new ArrayList<String>();
        }
        return nickNameList;
    }

    List<String> videoList = null;
    public List<String> getVideoList() {
        if (videoList == null) {
            videoList = new ArrayList<String>();
        }
        return videoList;
    }
    List<String> headUrlList = null;
    List<String> nickNameList = null;

    public List<PhoneInfo> phoneList = null; // 联系号码
    public List<PhoneInfo> getPhoneList() {
        if (phoneList == null) {
            phoneList = new ArrayList<PhoneInfo>();
        }
        return phoneList;
    }

    public List<EmailInfo> getEmailList() {
        if (emailList == null) {
            emailList = new ArrayList<EmailInfo>();
        }
        return emailList;
    }

    public List<EmailInfo> emailList = null; // Email
}
