package cn.redcdn.hvs.im.dao;

import android.database.Cursor;
import cn.redcdn.hvs.im.bean.CallRecordBean;
import cn.redcdn.hvs.im.bean.ContactBean;
import cn.redcdn.hvs.im.bean.ContactFriendBean;
import cn.redcdn.hvs.im.bean.NubeFriendBean;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;


public interface NetPhoneDao{

    /**    TODO:同步开始          */
    // 获取同步上传数量
    public long getUploadCount(String table) throws Exception;
    // 获取需要同步的佰酷好友数据
    public Map<String, ContactBean> queryNubeDataList() throws Exception;
    // 更新佰酷好友表同步状态
    public void updateFriendsStatus(Set<String> list,int mark)throws Exception;
    // 获取最大时间戳
    public String getMaxTimestamp(String tabeName) throws Exception;
    // 根据id获取好友数据id
    public Cursor getRawContactById(String id) throws Exception;
    // 根据物理删除好友数据
    public void deleteNubeLinkman(String id) throws Exception;
    //	// 根据id更新好友信息
    //	public void updateLinkmanByContactBean(String id,ContactBean bean) throws Exception;
    //	// 插入一条好友记录[目前废弃不用]
    //	public void insertLinkmanByContactBean(ContactBean bean) throws Exception;
    // 批量更新时间戳
    public void batchUpdateTimestemp(Map<String,String> map,Map<String,Integer> deleteMap) throws Exception;
    // 批量处理下载数据
    public void updateDownloadData(List<ContactBean> contacts)throws Exception;
    // 更新新朋友记录
    public void doUpdateNewfriendRecord();
    /**   TODO:同步结束           */

    // 获取发现好友数量
    public String getFindLinkmanCount();
    public boolean checkUpAccount(String accountName);
    public boolean authAccount(String accountName);
    // 查询视频联系人
    public Cursor queryNubeFriends();
    // 查询视频联系人（按照姓名升序排列）
    public Cursor queryNubeFriendsByFullPym();
    /**
     * 查询视频联系人，一键回家表和纳贝好友表
     */
    public Cursor queryNubeContactUser();
    // 根据nubeNumber更新好友信息
    public void updateLinkmanByNubenumber(String nubeNumber,ContactFriendBean bean);
    // 查询发现好友
    public Cursor queryFindFriends();
    // 查询android本地联系人号码(过滤掉应用号码)
    public Cursor queryLocalNumberFilterAppNumber();
    // 根据android raw_ContactId将本地联系人导入到应用数据库
    public boolean importLocalContactToApp(Map<String,NubeFriendBean> map, Boolean mark);
    // 获取应用联系人
    public List<ContactFriendBean> getAppLinkmanData();
    // 获取应用联系人手机号码
    public Map<String,String> getAppLinkmanNumberData();
    // 获取应用联系人手机号码
    public HashMap<String,ContactFriendBean> getLocationLinkmanDataHashMap();
    // 获取通话记录联系人显示数据
    public CopyOnWriteArrayList<CallRecordBean> getCallLogContactData();
    // 获取好友表中状态为5的数据
    public List<ContactFriendBean> getLoacalFindNewFriendsBean();
    // 获取本地联系人
    public List<ContactFriendBean> getLocationLinkmanData();
    // 根据手机号码获取本地联系人
    public ContactFriendBean getLocationLinkmanDataByMobile(String mobile);
    // 根据id获取联系人详细信息
    public List<ContactFriendBean> getLocationLinkmanData(Set<String> idList);
    // 更新好友认证状态
    public boolean updateFriendsAet(String id,int authStatus);
    // 插入好友信息
    public boolean 	insertLinkman(ContactFriendBean info);
    // 查询nube联系人信息
    public ContactFriendBean queryFriendInfo(String contactId);
    // 根据纳贝号获取好友表中状态为5的数据
    public ContactFriendBean getLoacalContactBean(String nubeNumber);
    // 根据号码更新好友在线状态
    public boolean updateOnlineByPhone(Map<String, String> map);

    public boolean updateOnlineByPhone(List<Map<String, String>> list);
    // 批量插入联系人到应用数据库
    public boolean batchInsertContacts(List<ContactFriendBean> list);
    //@lihs 根据名片分享 发现好友的现象
    // 批量插入联系人到应用数据库
    public boolean batchInsertContactsForVcard(List<ContactFriendBean> list);
    // 批量更新好友状态
    public void batchUpdateOnlineStatus();
    // 根据号码匹配联系人姓名
    public String matchNameByNumber(String number);
    // 查询本地号码
    public List<String> queryAppNumber();
    // 根据号码修改认证状态
    public void updateAuthByNumber(String number,int authStatus);
    // 查询在线状态好友数量
    public int queryOnlineConunt();
    // 查询新朋友数量
    public int queryNewFriendCount();
    // 根据手机号码获取nubenumber
    public String getNubeNumber(String number);
    // 根据nubenumber获取手机号码
    public String getNumber(String nubenumber);
    // 根据手机号码获取姓名
    public String getNameByNumber(String number);
    //根据号码获取系统通讯录中联系人的姓名
    public String getContactNameByNumber(String phoneNumber);
    // 根据nubeNumber获取姓名
    public String getNameByNubenumber(String nubeNumber);
    // 根据nubeNumber获取在场状态扩展信息
    public String getExtraInfoByNubenumber(String nubeNumber);

    // 根据nubeNumber获取在场状态扩展信息(不区别是否在线)
    public String getExtraInfoByNubenumberNIsonline(String nubeNumber);

    // 根据nubeNumber获取昵称
    public String getNicknameByNubenumber(String nubeNumber);
    // 根据Number获取昵称
    public String getNicknameByNumber(String number);

    // 根据nubeNumber获取性别
    public String getSexByNumber(String nubeNumber);
    // 获取应用数据库数据
    public int getAppdataCount();
    // 根据新好友记录id新增一条好友记录
    public boolean insertRecordByNewId(String newFriendId);
    // 查询好友联系人
    public Cursor getAppContacts();
    //TODO:查询可见好友--同步之后有一个上传好友nube号再发现的过程。以前是上传所有的nube号。现修改为：上传可见的nube号
    public Cursor getVisibleAppContacts();
    // 鉴别上线好友
    public List<String> checkUpOnlineLinkman(List<String> phoneList);
    // 根据视频号更新好友状态
    public void updateLinkmanStatus(String phone);
    // 根据视频号，物理删除视频好友
    public void deleteLinkman(String phone);
    /**
     * @Title: updateContactSort
     * @Description: 更新联系人的排序值
     * @param fromId 起始位置
     * @param toId 目标位置
     * @param dragContactId 拖动的联系人id
     * @return: void
     */
    public void updateContactSort(String fromId, String toId, String dragContactId);

    /**
     * @Title: deleteNubeContactById
     * @Description: 根据联系人id删除佰库联系人
     * @param contactId
     * @return: void
     */
    public void deleteNubeContactById(String contactId);

    /**
     * @Title: upDateNubeContactNameById
     * @Description: 更新NUBE联系人姓名
     * @param ContactId
     * @param name
     * @param nameType
     * @return: void
     */
    public void upDateNubeContactNameById(String contactId, String name, int nameType);

    /**
     * @Title: upDateNubeContactUserTypeById
     * @Description: 更新NUBE联系人用户类型
     * @param ContactId
     * @param userType
     * @return: void
     */
    public void upDateNubeContactUserTypeById(String contactId,  int userType);
    // 根据contactId获取用户类型
    public String getUserTypeByNumber(String contactId);
    //根据纳贝号查询对应联系人的头像
    public Map<String, String[]>  getHeadUrlByNubenumber(List<String> nubeNumberList);

    /**
     * @Title: matchContactByNumber
     * @Description: 根据号码查询是否存在匹配的联系人
     * @param number
     * @return: boolean
     */
    public boolean matchContactByNumber(String number);

    /**
     * @Title: matchContactBynubenumber
     * @Description: 根据nube号码查询是否存在匹配的联系人
     * @param nubenumber
     * @return: boolean
     */
    public boolean matchContactBynubenumber(String nubenumber);

    /**
     * @Title: queryFriendInfoByNube
     * @Description: 根据视频号查询nube联系人信息
     * @param contactId
     * @return: ContactFriendBean
     */
    public ContactFriendBean queryFriendInfoByNube(String nubeNumber);

    /**
     * 判断nube号码是否为好友
     */
    public boolean isNubeFriend(String nubeNumber);

    /**
     * @Title: queryFriendInfoByNube
     * @Description: 根据视频号查询nube联系人信息
     * @param contactId
     * @return: ContactFriendBean
     */
    public ContactFriendBean queryFriendInfoByPhone(String phone);

    public boolean queryVideoFriendByNubePhone(String nubeNumber);

    /**12.24 新需求*/
    public void clearNewFriendRecord();// 清空新朋友记录表

    // 查询好友联系人
    public Cursor queryContactRecord(String nubenumber); // 查询联系人通话记录

    public boolean checkContactRecord(String nubenumber); // 查询联系人是否有通话记录

    public boolean checkContactIsonline(String nubenumber); // 检测是否在线

    // 获取增量订阅联系人在线状态需要的视频号
    //    public ArrayList<String> getNubenumberToSub(String isMutualtrust);

    // *****************add by wangyf start *****************
    // 获取好友的联系人的信息
    public boolean getNubeFriend(String nubeNumber,String isMutualtrust,String isMutualtrustTwo);
    // 将联系人列表里好友的不可见状态改为可见状态
    public boolean updateNubeNumberStatus(String nubeNumber);
    public int insertContactIfNotExist(String tvNubeNumber);
    // *****************add by wangyf end *****************

    public void doUpdatePym();
    /**
     *
     * Description:根据nube号，查询好友表和i回家表的 用户名称
     *
     * @param nubenumber  nuber号
     * @return
     */
    public String queryInfoByNumber(String nubenumber);

    /**
     *
     * Description:查询纳贝表中所有数据中的手机号以及认证状态
     *
     * @return
     */
    public Map<String, String> getAppLinkmanNumberStatusData();
    /**
     *
     * Description:查询纳贝表中所有数据中的手机号以及对应联系人信息
     *
     * @return
     */
    public Map<String, ContactFriendBean> getAppLinkmanNubeNumberData();
    // 获取好友表中状态为5的数据
    public Map<String, ContactFriendBean> getNubeLoacalFindNewFriendsBean();
    /**
     *  成员nube和name的键值对
     *
     * @param nubeNumberList
     * @return
     */
    public Map<String, String> getLoacalNameByNubeList(List<String> nubeNumberList);
}
