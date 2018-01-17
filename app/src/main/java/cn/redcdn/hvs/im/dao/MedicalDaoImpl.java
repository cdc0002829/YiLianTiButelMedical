package cn.redcdn.hvs.im.dao;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.net.Uri;
import cn.redcdn.hvs.im.bean.ContactBean;
import cn.redcdn.hvs.im.bean.ContactFriendBean;
import cn.redcdn.hvs.im.bean.NubeFriendBean;
import cn.redcdn.hvs.im.provider.ProviderConstant;
import cn.redcdn.log.CustomLog;
import com.butel.connectevent.utils.CommonUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Desc
 * Created by wangkai on 2017/2/25.
 */

public class MedicalDaoImpl extends ContextWrapper implements MedicalDao {

    public MedicalDaoImpl(Context base) {
        super(base);
    }

    @Override
    public long getUploadCount(String table) throws Exception {
        return 0;
    }

    @Override
    public Map<String, ContactBean> queryNubeDataList() throws Exception {
        return null;
    }

    @Override
    public void updateFriendsStatus(Set<String> list, int mark) throws Exception {

    }

    @Override
    public String getMaxTimestamp(String tabeName) throws Exception {
        return null;
    }

    @Override
    public Cursor getRawContactById(String id) throws Exception {
        return null;
    }

    @Override
    public void deleteNubeLinkman(String id) throws Exception {

    }

    @Override
    public void batchUpdateTimestemp(Map<String, String> map, Map<String, Integer> deleteMap) throws Exception {

    }

    @Override
    public void updateDownloadData(List<ContactBean> contacts) throws Exception {

    }

    @Override
    public void doUpdateNewfriendRecord() {

    }

    @Override
    public String getFindLinkmanCount() {
        return null;
    }

    @Override
    public boolean checkUpAccount(String accountName) {
        return false;
    }

    @Override
    public boolean authAccount(String accountName) {
        return false;
    }

    @Override
    public Cursor queryNubeFriends() {
        return null;
    }

    @Override
    public Cursor queryNubeFriendsByFullPym() {
        return null;
    }

    @Override
    public Cursor queryNubeContactUser() {
        return null;
    }

    @Override
    public void updateLinkmanByNubenumber(String nubeNumber, ContactFriendBean bean) {

    }

    @Override
    public Cursor queryFindFriends() {
        return null;
    }

    @Override
    public Cursor queryLocalNumberFilterAppNumber() {
        return null;
    }

    @Override
    public boolean importLocalContactToApp(Map<String, NubeFriendBean> map, Boolean mark) {
        return false;
    }

    @Override
    public List<ContactFriendBean> getAppLinkmanData() {
        List<ContactFriendBean> list = new ArrayList<ContactFriendBean>();
        ContactFriendBean info;
        Cursor cursor = null;
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/GET_APP_LINKMAN_DATA_NEW");
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    // contactId _id,name name,number number,nickname
                    // nickname,headUrl headUrl,nubeNumber nubeNumber
                    // ,contactUserId contactUserId
                    info = new ContactFriendBean();
                    info.setContactId(CommonUtil.trackValue(cursor.getString(0)));
                    info.setName(CommonUtil.trackValue(cursor.getString(1)));
                    info.setNumber(CommonUtil.trackValue(cursor.getString(2)));
                    info.setNickname(CommonUtil.trackValue(cursor.getString(3)));
                    info.setHeadUrl(CommonUtil.trackValue(cursor.getString(4)));
                    info.setNubeNumber(CommonUtil.trackValue(cursor
                        .getString(5)));
                    info.setSourcesId(CommonUtil.trackValue(cursor.getString(6)));
                    info.setPym(CommonUtil.trackValue(cursor.getString(7)));
                    info.setSex(CommonUtil.trackValue(cursor.getString(8)));
                    list.add(info);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            CustomLog.d("TAG","获取应用联系人   Exception");
            list = null;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return list;
    }

    @Override
    public Map<String, String> getAppLinkmanNumberData() {
        return null;
    }

    @Override
    public HashMap<String, ContactFriendBean> getLocationLinkmanDataHashMap() {
        return null;
    }

    @Override
    public List<ContactFriendBean> getLoacalFindNewFriendsBean() {
        return null;
    }

    @Override
    public List<ContactFriendBean> getLocationLinkmanData() {
        return null;
    }

    @Override
    public ContactFriendBean getLocationLinkmanDataByMobile(String mobile) {
        return null;
    }

    @Override
    public List<ContactFriendBean> getLocationLinkmanData(Set<String> idList) {
        return null;
    }

    @Override
    public boolean updateFriendsAet(String id, int authStatus) {
        return false;
    }

    @Override
    public boolean insertLinkman(ContactFriendBean info) {
        return false;
    }

    @Override
    public ContactFriendBean queryFriendInfo(String contactId) {
        return null;
    }

    @Override
    public ContactFriendBean getLoacalContactBean(String nubeNumber) {
        return null;
    }

    @Override
    public boolean updateOnlineByPhone(Map<String, String> map) {
        return false;
    }

    @Override
    public boolean updateOnlineByPhone(List<Map<String, String>> list) {
        return false;
    }

    @Override
    public boolean batchInsertContacts(List<ContactFriendBean> list) {
        return false;
    }

    @Override
    public boolean batchInsertContactsForVcard(List<ContactFriendBean> list) {
        return false;
    }

    @Override
    public void batchUpdateOnlineStatus() {

    }

    @Override
    public String matchNameByNumber(String number) {
        return null;
    }

    @Override
    public List<String> queryAppNumber() {
        return null;
    }

    @Override
    public void updateAuthByNumber(String number, int authStatus) {

    }

    @Override
    public int queryOnlineConunt() {
        return 0;
    }

    @Override
    public int queryNewFriendCount() {
        return 0;
    }

    @Override
    public String getNubeNumber(String number) {
        return null;
    }

    @Override
    public String getNumber(String nubenumber) {
        return null;
    }

    @Override
    public String getNameByNumber(String number) {
        return null;
    }

    @Override
    public String getContactNameByNumber(String phoneNumber) {
        return null;
    }

    @Override
    public String getNameByNubenumber(String nubeNumber) {
        return null;
    }

    @Override
    public String getExtraInfoByNubenumber(String nubeNumber) {
        return null;
    }

    @Override
    public String getExtraInfoByNubenumberNIsonline(String nubeNumber) {
        return null;
    }

    @Override
    public String getNicknameByNubenumber(String nubeNumber) {
        return null;
    }

    @Override
    public String getNicknameByNumber(String number) {
        return null;
    }

    @Override
    public String getSexByNumber(String nubeNumber) {
        return null;
    }

    @Override
    public int getAppdataCount() {
        return 0;
    }

    @Override
    public boolean insertRecordByNewId(String newFriendId) {
        return false;
    }

    @Override
    public Cursor getAppContacts() {
        return null;
    }

    @Override
    public Cursor getVisibleAppContacts() {
        return null;
    }

    @Override
    public List<String> checkUpOnlineLinkman(List<String> phoneList) {
        return null;
    }

    @Override
    public void updateLinkmanStatus(String phone) {

    }

    @Override
    public void deleteLinkman(String phone) {

    }

    @Override
    public void updateContactSort(String fromId, String toId, String dragContactId) {

    }

    @Override
    public void deleteNubeContactById(String contactId) {

    }

    @Override
    public void upDateNubeContactNameById(String contactId, String name, int nameType) {

    }

    @Override
    public void upDateNubeContactUserTypeById(String contactId, int userType) {

    }

    @Override
    public String getUserTypeByNumber(String contactId) {
        return null;
    }

    @Override
    public Map<String, String[]> getHeadUrlByNubenumber(List<String> nubeNumberList) {
        return null;
    }

    @Override
    public boolean matchContactByNumber(String number) {
        return false;
    }

    @Override
    public boolean matchContactBynubenumber(String nubenumber) {
        return false;
    }

    @Override
    public ContactFriendBean queryFriendInfoByNube(String nubeNumber) {
        return null;
    }

    @Override
    public boolean isNubeFriend(String nubeNumber) {
        return false;
    }

    @Override
    public ContactFriendBean queryFriendInfoByPhone(String phone) {
        return null;
    }

    @Override
    public boolean queryVideoFriendByNubePhone(String nubeNumber) {
        return false;
    }

    @Override
    public void clearNewFriendRecord() {

    }

    @Override
    public Cursor queryContactRecord(String nubenumber) {
        return null;
    }

    @Override
    public boolean checkContactRecord(String nubenumber) {
        return false;
    }

    @Override
    public boolean checkContactIsonline(String nubenumber) {
        return false;
    }

    @Override
    public boolean getNubeFriend(String nubeNumber, String isMutualtrust, String isMutualtrustTwo) {
        return false;
    }

    @Override
    public boolean updateNubeNumberStatus(String nubeNumber) {
        return false;
    }

    @Override
    public int insertContactIfNotExist(String tvNubeNumber) {
        return 0;
    }

    @Override
    public void doUpdatePym() {

    }

    @Override
    public String queryInfoByNumber(String nubenumber) {
        return null;
    }

    @Override
    public Map<String, String> getAppLinkmanNumberStatusData() {
        return null;
    }

    @Override
    public Map<String, ContactFriendBean> getAppLinkmanNubeNumberData() {
        return null;
    }

    @Override
    public Map<String, ContactFriendBean> getNubeLoacalFindNewFriendsBean() {
        return null;
    }

    @Override
    public Map<String, String> getLoacalNameByNubeList(List<String> nubeNumberList) {
        return null;
    }
}
