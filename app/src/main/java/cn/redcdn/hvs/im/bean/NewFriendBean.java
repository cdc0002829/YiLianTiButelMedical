package cn.redcdn.hvs.im.bean; /**
 * @Title: NewFriendBean.java
 * @Package com.channelsoft.netphone.bean
 * @author miaolikui
 * @date 2013-8-27 上午10:50:37
 * @version V1.0
 */

import java.io.Serializable;


/**
 * @ClassName: NewFriendBean
 * @author miaolikui
 * @date 2013-8-27 上午10:50:37
 *
 */
public class NewFriendBean implements Serializable{

    private static final long serialVersionUID = 6831107704848964820L;

    private String id="";
    private String lastTime="";
    private String name="";
    private String number="";
    private String nubeNumber="";
    private String headUrl = "";
    private String sex = "1";//性别 0：未知，1：男，2：女
    /***
     * 状态说明
     * 发送方：0  发出邀请，待对方同意
     * 发送方：1  发出邀请，对方已经同意
     * 接收方：2  收到邀请，待处理
     * 接收方：3  收到邀请，同意，给对方发生SIP回执
     * 接收方：4  收到邀请，拒绝，不给对方发生SIP回执
     * 本地导入：5 已导入待‘添加’确认
     * 本地导入：6 已导入并确认添加
     */
    private int status=0;

    //1：表示不可见；0：可见；
    private int visible=0;
    /***
     * 0:否；1:是
     */
    private int isNews=0;

    private String contactUserId="";
    private String realName ="";//通过拨打电话消息体传递的值  niuben modify 20140926

    public int getVisible() {
        return visible;
    }
    public void setVisible(int visible) {
        this.visible = visible;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getLastTime() {
        return lastTime;
    }
    public void setLastTime(String lastTime) {
        this.lastTime = lastTime;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
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
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public int getIsNews() {
        return isNews;
    }
    public void setIsNews(int isNews) {
        this.isNews = isNews;
    }
    public String getContactUserId() {
        return contactUserId;
    }
    public void setContactUserId(String contactUserId) {
        this.contactUserId = contactUserId;
    }

    public String getHeadUrl() {
        return headUrl;
    }

    public void setHeadUrl(String headUrl) {
        this.headUrl = headUrl;
    }
    public String getRealName() {
        return realName;
    }
    public void setRealName(String realName) {
        this.realName = realName;
    }
    public String getSex() {
        return sex;
    }
    public void setSex(String sex) {
        this.sex = sex;
    }

    /**
     * 针对新朋友表的各种状态定义常量
     * 235670
     */

    /**等待验证**/
    //public static final int WAIT_AUTHORIZATION_STATUS = 0;
    /**已经验证**/
    //public static final int ALREADY_AUTHENTICATED_STATUS = 1;
    /**加为好友**/
    public static final int ADD_FRIEND_STATUS= 2;
    /**加为已经通过**/
    public static final int HAS_PASSED_STATUS = 3;
    /**本地发现未添加**/
    public static final int LOCAL_FIND_STATUS = 5;
    /**本地已经添加**/
    public static final int ALREADY_ADDED_STATUS = 6;
    /**回执消息**/
    public static final int GREETING_MESSAGE_STATUS = 7;
    /**回执消息无本地联系人，点击添加后的状态*/
    public static final int GREETED_MESSAGE_STATUS = 8;


}
