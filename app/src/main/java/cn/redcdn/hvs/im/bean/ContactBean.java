package cn.redcdn.hvs.im.bean;

import android.text.TextUtils;

import java.util.List;
import java.util.Map;

/**
 * Desc  联系人
 * Created by wangkai on 2017/2/25.
 */

public class ContactBean {
    /**
     * 联系人Id
     */
    private String contactId;

    /**
     * 地址节点
     */
    private List<Map<String, List<String>>> addresses;

    //nubeNumber

    private List<NubeInfoBean> nubeInfos;

    /**
     * 生日
     */
    private String birthday;

    /**
     * 公司
     */
    private String company;

    /**
     * 备注
     */
    private String description;

    /**
     * 邮箱节点
     */
    private List<Map<String, List<MailUidBean>>> emails;

    /**
     * 扩展属性
     */
    private List<Map<String, List<String>>> extendProperties;

    /**
     * 名
     */
    private String firstName;

    /**
     * 姓
     */
    private String lastName;

    /**
     * 姓名
     */
    private String name;

    /**
     * 所在分组
     */
    private List<String> groups;

    /**
     * 即时消息节点
     */
    private List<Map<String, List<String>>> ims;

    /**
     * 逻辑删除
     */
    private int isDeleted;

    /**
     * 客户端行号
     */
    private long clientLineNumber;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 电话节点
     */
    private List<Map<String, List<PhoneUidPo>>> phones;

    /**
     * 职务
     */
    private String position;

    /**
     * 创建时间
     */
    private long createTime;

    /**
     * 时间戳
     */
    private long timestamp;

    /**
     * 网站地址节点
     */
    private List<Map<String, List<String>>> urls;

    private String userType;

    private String sex;

    private String showMobile;

    public void setShowMobile(String showMobile) {
        this.showMobile = showMobile;
    }

    public String getShowMobile() {
        return showMobile;
    }


    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getContactId()
    {
        return contactId;
    }

    public void setContactId(String contactId)
    {
        this.contactId = contactId;
    }

    public List<Map<String, List<String>>> getAddresses()
    {
        return addresses;
    }

    public void setAddresses(List<Map<String, List<String>>> addresses)
    {
        this.addresses = addresses;
    }

    public String getBirthday()
    {
        return birthday;
    }

    public void setBirthday(String birthday)
    {
        this.birthday = birthday;
    }

    public String getCompany()
    {
        return company;
    }

    public void setCompany(String company)
    {
        this.company = company;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public List<Map<String, List<String>>> getExtendProperties()
    {
        return extendProperties;
    }

    public void setExtendProperties(
            List<Map<String, List<String>>> extendProperties)
    {
        this.extendProperties = extendProperties;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<String> getGroups()
    {
        return groups;
    }

    public void setGroups(List<String> groups)
    {
        this.groups = groups;
    }

    public List<Map<String, List<String>>> getIms()
    {
        return ims;
    }

    public void setIms(List<Map<String, List<String>>> ims)
    {
        this.ims = ims;
    }

    public int getIsDeleted()
    {
        return isDeleted;
    }

    public void setIsDeleted(int isDeleted)
    {
        this.isDeleted = isDeleted;
    }

    public long getClientLineNumber()
    {
        return clientLineNumber;
    }

    public void setClientLineNumber(long clientLineNumber)
    {
        this.clientLineNumber = clientLineNumber;
    }

    public String getNickName()
    {
        return nickName;
    }

    public void setNickName(String nickName)
    {
        this.nickName = nickName;
    }

    public List<Map<String, List<MailUidBean>>> getEmails() {
        return emails;
    }

    public void setEmails(List<Map<String, List<MailUidBean>>> emails) {
        this.emails = emails;
    }

    public List<Map<String, List<PhoneUidPo>>> getPhones() {
        return phones;
    }

    public void setPhones(List<Map<String, List<PhoneUidPo>>> phones) {
        this.phones = phones;
    }

    public String getPosition()
    {
        return position;
    }

    public void setPosition(String position)
    {
        this.position = position;
    }

    public long getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(long createTime)
    {
        this.createTime = createTime;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(long timestamp)
    {
        this.timestamp = timestamp;
    }

    public List<Map<String, List<String>>> getUrls()
    {
        return urls;
    }

    public void setUrls(List<Map<String, List<String>>> urls)
    {
        this.urls = urls;
    }
    public List<NubeInfoBean> getNubeInfos() {
        return nubeInfos;
    }
    public void setNubeInfos(List<NubeInfoBean> nubeInfos) {
        this.nubeInfos = nubeInfos;
    }

    //如果用户类型为空，默认为0-普通用户
    public String getUserType() {
        if(TextUtils.isEmpty(userType)){
            return  "0";
        }
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    @Override
    public String toString() {
        return "ContactPo [contactId=" + contactId
                + ", emails=" + emails + ", extendProperties="
                + extendProperties + ", name=" + name + ", groups=" + groups
                + ", isDeleted=" + isDeleted + ", phones=" + phones + ", nubeInfos="+nubeInfos+"]";
    }

    public static class PhoneUidPo
    {

        private String number;

        private String trueNumber;

        private String contactUserId;

        private int isMutualTrust;

        public String getNumber()
        {
            return number;
        }

        public void setNumber(String number)
        {
            this.number = number;
        }

        public String getTrueNumber()
        {
            return trueNumber;
        }

        public void setTrueNumber(String trueNumber)
        {
            this.trueNumber = trueNumber;
        }

        public String getContactUserId()
        {
            return contactUserId;
        }

        public void setContactUserId(String contactUserId)
        {
            this.contactUserId = contactUserId;
        }

        public int getIsMutualTrust()
        {
            return isMutualTrust;
        }

        public void setIsMutualTrust(int isMutualTrust)
        {
            this.isMutualTrust = isMutualTrust;
        }

    }
}
