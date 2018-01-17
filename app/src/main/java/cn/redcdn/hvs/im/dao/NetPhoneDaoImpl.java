package cn.redcdn.hvs.im.dao;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Settings;
import android.text.TextUtils;
import cn.redcdn.hvs.database.DBConstant;
import cn.redcdn.hvs.im.bean.CallRecordBean;
import cn.redcdn.hvs.im.bean.ContactBean;
import cn.redcdn.hvs.im.bean.ContactBean.PhoneUidPo;
import cn.redcdn.hvs.im.bean.ContactFriendBean;
import cn.redcdn.hvs.im.bean.NewFriendBean;
import cn.redcdn.hvs.im.bean.NubeFriendBean;
import cn.redcdn.hvs.im.bean.NubeInfoBean;
import cn.redcdn.hvs.im.bean.ShowNameUtil;
import cn.redcdn.hvs.im.column.NewFriendTable;
import cn.redcdn.hvs.im.column.NubeFriendColumn;
import cn.redcdn.hvs.im.provider.ProviderConstant;
import cn.redcdn.hvs.util.DateUtil;
import cn.redcdn.hvs.util.StringUtil;

import com.butel.connectevent.utils.CommonUtil;
import com.butel.connectevent.utils.LogUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class NetPhoneDaoImpl extends ContextWrapper implements NetPhoneDao {

    public NetPhoneDaoImpl(Context base) {
        super(base);
    }

    /**
     * @author: chuwx
     * @Title: getUploadCount
     * @Description:根据表名获取同步数据量
     * @param table
     * @return
     * @throws Exception
     * @date 2013-8-3 上午11:21:34
     */
    @Override
    public long getUploadCount(String table) throws Exception {
        long count = 0;
        Cursor cursor = null;
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/UPLOAD_GET_COUNT/" + table);

        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    count = cursor.getInt(0);
                    LogUtil.d("TableName: " + table + " count: " + count);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("根据表名获取同步数据量   Exception");
            throw e;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return count;
    }

    @Override
    public boolean authAccount(String accountName) {
        LogUtil.d("authAccount  start");
        Cursor localCursor = null;
        boolean mark = false;
        try {
            localCursor = getContentResolver()
                .query(Settings.CONTENT_URI,
                    new String[] { Settings.ACCOUNT_NAME,
                        Settings.ACCOUNT_TYPE },
                    Settings.ACCOUNT_NAME + " = ? ",
                    new String[] { accountName }, null);
            if (localCursor != null && localCursor.getCount() > 0) {
                localCursor.moveToFirst();
                LogUtil.d("app have account , account_name : "
                    + localCursor.getString(0));
            } else {
                LogUtil.d("authAccount  insert start");
                ContentValues values = new ContentValues();
                // values.put(Settings.ACCOUNT_TYPE,
                //     BizConstant.NET_PHONE_ACCOUNT_TYPE);
                values.put(Settings.ACCOUNT_NAME, accountName);
                getContentResolver().insert(Settings.CONTENT_URI, values);
            }
            mark = true;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("authAccount exception");
        } finally {
            if (localCursor != null) {
                localCursor.close();
                localCursor = null;
            }
        }
        return mark;
    }

    @Override
    public boolean checkUpAccount(String accountName) {
        LogUtil.d("checkUpAccount  start");
        Cursor localCursor = null;
        boolean mark = false;
        try {
            localCursor = getContentResolver()
                .query(Settings.CONTENT_URI,
                    new String[] { Settings.ACCOUNT_NAME,
                        Settings.ACCOUNT_TYPE },
                    Settings.ACCOUNT_NAME + " = ? ",
                    new String[] { accountName }, null);
            if (localCursor != null && localCursor.getCount() > 0) {

                localCursor.moveToFirst();
                LogUtil.d("checkUpAccount  account_name : "
                    + localCursor.getString(0));

                String type = localCursor.getString(1);

                if ("channelsoft_netphone".equals(type)) {
                    LogUtil.d("authAccount  update start");
                    ContentValues values = new ContentValues();
                    // values.put(Settings.ACCOUNT_TYPE,
                    //     BizConstant.NET_PHONE_ACCOUNT_TYPE);
                    getContentResolver().update(Settings.CONTENT_URI, values,
                        Settings.ACCOUNT_NAME + " = ? ",
                        new String[] { accountName });
                }

                // if (BizConstant.NET_PHONE_ACCOUNT_TYPE.equals(type)
                //     || "channelsoft_netphone".equals(type)) {
                //     mark = true;
                // }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("checkUpAccount exception");
        } finally {
            if (localCursor != null) {
                localCursor.close();
                localCursor = null;
            }
        }
        return mark;
    }

    /**
     * @author: chuwx
     * @Title: queryNubeDataList
     * @Description:获取需要同步的佰酷好友数据
     * @return
     * @throws Exception
     * @date 2013-8-5 下午1:50:05
     */
    @Override
    public Map<String, ContactBean> queryNubeDataList() throws Exception {
        Map<String, ContactBean> map = new HashMap<String, ContactBean>();
        Cursor cursor = null;
        Uri queryUri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/QUERY_NUBE_FRIENDS_LIST");
        try {
            cursor = getContentResolver().query(queryUri, null, null, null,
                null);
            if (cursor != null && cursor.getCount() > 0) {
                ContactBean bean;

                // PhoneUidBean phoneUidPo;
                List<Map<String, List<PhoneUidPo>>> phones;
                Map<String, List<PhoneUidPo>> mobile;
                List<PhoneUidPo> mobileList;

                NubeInfoBean nubeInfo;
                List<NubeInfoBean> nubeList;

                List<Map<String, List<String>>> urls;
                Map<String, List<String>> urlMap;
                List<String> urlList;
                String name;
                while (cursor.moveToNext()) {
                    bean = new ContactBean();
                    bean.setContactId(CommonUtil.trackValue(cursor.getString(0)));
                    bean.setIsDeleted(Integer.parseInt(CommonUtil
                        .trackValue(cursor.getInt(6))));
                    bean.setTimestamp(Long.valueOf(CommonUtil.trackValue(cursor
                        .getLong(5))));
                    bean.setName(CommonUtil.trackValue(cursor.getString(1)));
                    bean.setNickName(CommonUtil.trackValue(cursor.getString(2)));
                    bean.setFirstName(CommonUtil.trackValue(cursor.getString(3)));
                    bean.setLastName(CommonUtil.trackValue(cursor.getString(4)));

                    // 用户类型 保存
                    List<Map<String, List<String>>> extendProperties = new ArrayList<Map<String, List<String>>>();
                    Map<String, List<String>> extendPropertieMap = new HashMap<String, List<String>>();
                    List<String> extendPropertie = new ArrayList<String>();
                    extendPropertie.add(CommonUtil.trackValue(cursor
                        .getString(14)));
                    extendPropertieMap.put("userType", extendPropertie);
                    extendProperties.add(extendPropertieMap);

                    // 增加性别字段保存
                    extendPropertieMap = null;
                    extendPropertieMap = new HashMap<String, List<String>>();
                    List<String> extendPropertie2 = new ArrayList<String>();
                    extendPropertie2.add(CommonUtil.trackValue(cursor
                        .getString(15)));
                    extendPropertieMap.put("sex", extendPropertie2);
                    extendProperties.add(extendPropertieMap);

                    // 增加 个人名片 显示手机号字段
                    extendPropertieMap = null;
                    extendPropertieMap = new HashMap<String, List<String>>();
                    List<String> extendPropertie3 = new ArrayList<String>();
                    extendPropertie3.add(CommonUtil.trackValue(cursor
                        .getString(16)));
                    extendPropertieMap.put("showMobile", extendPropertie3);
                    extendProperties.add(extendPropertieMap);
                    bean.setExtendProperties(extendProperties);

                    // phone de 处理
                    // phoneUidPo = new PhoneUidPo();
                    phones = new ArrayList<Map<String, List<PhoneUidPo>>>();
                    mobile = new HashMap<String, List<PhoneUidPo>>();
                    mobileList = new ArrayList<PhoneUidPo>();
                    mobile.put("mobile", mobileList);
                    phones.add(mobile);
                    // phoneUidPo.setNumber(CommonUtil.trackValue(cursor
                    //     .getString(8)));
                    // phones.get(0).get("mobile").add(phoneUidPo);
                    // bean.setPhones(phones);

                    // nubeNumber处理
                    nubeInfo = new NubeInfoBean();
                    nubeInfo.setContactUserId(cursor.getString(12));
                    nubeInfo.setNubeNumber(CommonUtil.trackValue(cursor
                        .getString(9)));
                    // nubeList = new ArrayList<NubeInfoPo>();
                    // nubeList.add(nubeInfo);
                    // bean.setNubeInfos(nubeList);

                    // 头像的处理
                    String head = CommonUtil.trackValue(cursor.getString(13));
                    if (!StringUtil.isEmpty(head)) {
                        urlList = new ArrayList<String>();
                        urlList.add(head);
                        urlMap = new HashMap<String, List<String>>();
                        urlMap.put("HeadUrl", urlList);
                        urls = new ArrayList<Map<String, List<String>>>();
                        urls.add(urlMap);
                        bean.setUrls(urls);
                    }

                    // 确保姓名字段不为空
                    if (StringUtil.isEmpty(bean.getName())) {
                        name = bean.getLastName() + bean.getFirstName();
                        if (StringUtil.isEmpty(name)) {
                            // name = bean.getNickName();
                            // if (StringUtil.isEmpty(name)) {
                            // name = (bean.getNubeInfos().get(0))
                            // .getNubeNumber();
                            // }
                            // // 名字段填充
                            // bean.setFirstName(name);

                            // 4.16号修改，终端匹配
                            // name = DBConstant.NAME_PLACEHOLDER;
                            bean.setName(name);
                        }
                    }

                    map.put(CommonUtil.trackValue(cursor.getString(0)), bean);

                }
            }
            LogUtil.d("获取需要同步的佰酷好友数据 success");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("获取需要同步的佰酷好友数据   Exception");
            throw e;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return map;
    }

    /**
     * @author: chuwx
     * @Title: updateFriendsStatus
     * @Description:更新佰酷好友表同步状态
     * @param list
     * @throws Exception
     * @date 2013-8-5 下午2:33:36
     */
    @Override
    public void updateFriendsStatus(Set<String> list, int mark)
        throws Exception {
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/UPDATE_SYNC_STATUS");
        String ids = "";
        if (list != null) {
            Iterator<String> it = list.iterator();
            StringBuilder builder = new StringBuilder();
            while (it.hasNext()) {
                builder.append("'").append(it.next()).append("'").append(",");
            }
            ids = builder.toString();
            ids = ids.substring(0, ids.length() - 1);
        }
        try {
            ContentValues values = new ContentValues();
            values.put("syncStat", mark);
            getContentResolver().update(uri, values,
                "contactId in (" + ids + " )", null);
            LogUtil.d("更新佰酷好友表同步状态 success");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("更新佰酷好友表同步状态   Exception");
            throw e;
        }
    }

    /**
     * @author: chuwx
     * @Title: getMaxTimestamp
     * @Description:获取最大时间戳
     * @param tabeName
     * @return
     * @throws Exception
     * @date 2013-8-5 下午2:56:34
     */
    @Override
    public String getMaxTimestamp(String tabeName) throws Exception {
        String timestamp = "";
        Cursor cursor = null;
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/GET_MAX_TIMESTAMP/" + tabeName);
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);

            if (cursor != null && cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    timestamp = CommonUtil.trackValue(cursor.getString(0));
                    LogUtil.d("TableName: " + tabeName + " timestamp: "
                        + timestamp);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("获取最大时间戳   Exception");
            throw e;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return timestamp;
    }

    /**
     * @author: chuwx
     * @Title: getRawContactById
     * @Description:根据id获取好友数据id
     * @param id
     * @return
     * @throws Exception
     * @date 2013-8-5 下午3:21:51
     */
    @Override
    public Cursor getRawContactById(String id) throws Exception {
        Cursor cursor = null;
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/QUERY_LINKMAN_ID");
        try {
            cursor = getContentResolver().query(uri,
                new String[] { NubeFriendColumn.CONTACTID },
                "contactId='" + id + "'", null, null);
            LogUtil.d("根据id获取好友数据id success");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("根据id获取好友数据id Exception");
            throw e;
        }
        return cursor;
    }

    /**
     * @author: chuwx
     * @Title: deleteNubeLinkman
     * @Description:根据物理删除好友数据
     * @param id
     * @throws Exception
     * @date 2013-8-5 下午3:29:10
     */
    @Override
    public void deleteNubeLinkman(String id) throws Exception {
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/DELETE_LINKMAN");
        try {
            getContentResolver().delete(uri, "contactId='" + id + "'", null);
            LogUtil.d("根据物理删除好友数据  success");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("根据物理删除好友数据  Exception");
            throw e;
        }
    }

    //	/**
    //	 * @author: chuwx
    //	 * @Title: updateLinkmanByContactBean
    //	 * @Description:根据id更新好友信息
    //	 * @param id
    //	 * @param bean
    //	 * @throws Exception
    //	 * @date 2013-8-5 下午3:40:56
    //	 */
    //	@Override
    //	public void updateLinkmanByContactBean(String id, ContactBean bean)
    //			throws Exception {
    //		Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
    //				+ "/UPDATE_LINKMAN_ITEM");
    //		if (bean != null) {
    //			ContentValues values = resetConentValuesByPo(bean);
    //			try {
    //				getContentResolver().update(uri, values,
    //						"contactId='" + id + "'", null);
    //				LogUtil.d("根据id更新好友信息  success");
    //			} catch (Exception e) {
    //				e.printStackTrace();
    //				LogUtil.d("根据id更新好友信息  Exception");
    //				throw e;
    //			}
    //		}
    //	}

    /**
     * @author: chuwx
     * @Title: resetConentValuesByPo
     * @Description:拼装参数
     * @param bean
     * @return
     * @date: 2013-8-11 下午6:36:58
     */
    private ContentValues resetConentValuesByPo(ContactBean bean) {
        ContentValues values = new ContentValues();
        // values.put("contactId", bean.getContactId());
        String name;
        if (bean != null) {
            name = bean.getName();
            // if (DBConstant.NAME_PLACEHOLDER.equals(name)) {
            //     // 4.16号修改，case下重置name字段为空
            //     values.put("name", "");
            // } else {
            //     values.put("name", name);
            // }
            values.put("nickname", bean.getNickName());
            values.put("firstName", bean.getFirstName());
            values.put("lastName", bean.getLastName());
            values.put("lastTime", String.valueOf(bean.getTimestamp()));
            values.put("isDeleted", bean.getIsDeleted());
            values.put("isMutualTrust", 1);
            // String pym = CommonUtil.getPymByParams(values.getAsString("name"),
            //     bean.getNickName(), bean.getNubeInfos().get(0)
            //         .getNubeNumber());
            // values.put("fullPym", pym);

            try {
                if (bean.getPhones() != null) {
                    List<PhoneUidPo> list = bean.getPhones().get(0)
                        .get("mobile");
                    // added by zhaguitao on 20160406 begin
                    // 级会议在同步联系人时 电话号码信息内容是：
                    // "phones":[{"移动电话":[{"contactUserId":"1","isMutualTrust":0,"number":"18919623028","trueNumber":"18919623028"}]}],
                    // 而可视级会议在同步联系人时，电话号码信息内容是：
                    // "phones":[{"mobile":[{"contactUserId":"1","isMutualTrust":0,"number":"18919623028","trueNumber":"18919623028"}]}]
                    // 因此需要把“移动电话”作为key也解析出来
                    if (list == null || list.size() == 0) {
                        list = bean.getPhones().get(0).get("移动电话");
                        if (list != null && list.size() > 0) {
                            LogUtil.d("没取到 mobile ，取到 移动电话 了");
                        }
                    }
                    // added by zhaguitao on 20160406 end

                    if (list != null && list.size() > 0) {
                        LogUtil.d("同步下载完成插入app db 数据库的手机号： "
                            + list.get(0).getNumber());
                        values.put("number", list.get(0).getNumber());
                    }
                }
                if (bean.getNubeInfos() != null) {
                    values.put("nubeNumber", bean.getNubeInfos().get(0)
                        .getNubeNumber());
                    values.put("contactUserId", bean.getNubeInfos().get(0)
                        .getContactUserId());
                }
                if (bean.getUrls() != null) {
                    List<String> list = bean.getUrls().get(0).get("HeadUrl");
                    if (list != null && list.size() > 0) {
                        values.put("headUrl", list.get(0));
                    }
                }
                if (bean.getExtendProperties() != null) {
                    List<String> list = bean.getExtendProperties().get(0)
                        .get("userType");
                    if (list != null && list.size() > 0) {
                        LogUtil.d("同步下载完成插入app db 数据库的用户类型： " + list.get(0));
                        values.put("userType", list.get(0));
                    }
                    // 同步下载时，更新性别字段
                    if (bean.getExtendProperties().size() > 1) {
                        List<String> list2 = bean.getExtendProperties().get(1)
                            .get("sex");
                        if (list2 != null && list2.size() > 0) {
                            LogUtil.d("同步下载完成插入app db 数据库的性别： " + list2.get(0));
                            values.put("sex", list2.get(0));
                        }
                    }
                    // 同步下载，更新个人名片 显示手机号
                    if (bean.getExtendProperties().size() > 2) {
                        List<String> list2 = bean.getExtendProperties().get(2)
                            .get("showMobile");
                        if (list2 != null && list2.size() > 0) {
                            LogUtil.d("同步下载完成插入app db 数据库的  个人名片显示手机号字符： "
                                + list2.get(0));
                            values.put("reserveStr1", list2.get(0));// 利用第一个扩展字段
                        }
                    }
                }
            } catch (Exception e) {
                LogUtil.e("resetConentValuesByPo 组转联系人信息出错", e);
            }
            values.put("syncStat", 2);

        }
        return values;
    }

    //	/**
    //	 * @author: chuwx
    //	 * @Title: insertLinkmanByContactBean
    //	 * @Description:插入一条好友记录
    //	 * @param bean
    //	 * @throws Exception
    //	 * @date 2013-8-5 下午4:00:48
    //	 */
    //	@Override
    //	public void insertLinkmanByContactBean(ContactBean bean) throws Exception {
    //		Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
    //				+ "/INSERT_LINKMAN_ITEM");
    //		if (bean != null) {
    //			ContentValues values = new ContentValues();
    //			values.put("contactId", bean.getContactId());
    //			LogUtil.d("insertLinkmanByContactBean 下载数据插入一条联系人号码："
    //					+ bean.getName());
    //			values.put("name", bean.getName());
    //			values.put("nickname", bean.getNickName());
    //			values.put("firstName", bean.getFirstName());
    //			values.put("lastName", bean.getLastName());
    //			values.put("lastTime", String.valueOf(bean.getTimestamp()));
    //			values.put("isDeleted", bean.getIsDeleted());
    //			values.put("isOnline", 0);
    //			try {
    //				if (bean.getPhones().get(0).get("mobile") != null) {
    //					List<PhoneUidPo> list = bean.getPhones().get(0)
    //							.get("mobile");
    //					if (list.size() > 0) {
    //						values.put("number", list.get(0).getNumber());
    //						values.put("isMutualTrust", list.get(0)
    //								.getIsMutualTrust());
    //						values.put("nubeNumber", list.get(0).getTrueNumber());
    //					}
    //				}
    //			} catch (Exception e) {
    //				e.printStackTrace();
    //				LogUtil.d("insertLinkmanByContactBean 组转联系人信息出错");
    //			}
    //			values.put("syncStat", 2);
    //			try {
    //				getContentResolver().insert(uri, values);
    //				LogUtil.d("插入一条好友记录  success");
    //			} catch (Exception e) {
    //				e.printStackTrace();
    //				LogUtil.d("插入一条好友记录  Exception");
    //				throw e;
    //			}
    //		}
    //	}

    /**
     * @author: chuwx
     * @Title: batchUpdateTimestemp
     * @Description:批量更新时间戳
     * @param map
     * @throws Exception
     * @date 2013-8-5 下午4:53:21
     */
    @Override
    public void batchUpdateTimestemp(Map<String, String> map,
                                     Map<String, Integer> deleteMap) throws Exception {

        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/UPDATE_LINKMAN_TIME");
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ContentValues addCvs;
        if (map != null && map.size() > 0) {
            Set<String> list = map.keySet();
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                String key = it.next();
                addCvs = new ContentValues();
                addCvs.put("lastTime", map.get(key));
                if (deleteMap.get(key) == 1) {
                    // 3.4号服务端优先》》》》》服务端已经删除数据
                    addCvs.put("isDeleted", deleteMap.get(key));
                }

                ops.add(ContentProviderOperation.newUpdate(uri)
                    .withValues(addCvs)
                    .withSelection("contactId = ?", new String[] { key })
                    .build());
            }
        }
        try {
            if (ops.size() > 0) {
                getContentResolver()
                    .applyBatch(ProviderConstant.AUTHORITY, ops);
                LogUtil.d("批量更新时间戳   success");
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("批量更新时间戳   Exception", e);
            throw e;
        }

    }

    /**
     * @author: chuwx
     * @Title: getFindLinkmanCount
     * @Description:获取发现好友数量
     * @return
     * @date 2013-8-7 下午3:25:19
     */
    @Override
    public String getFindLinkmanCount() {
        String count = "";
        Cursor cursor = null;
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/QUERY_FIND_LINKMAN_COUNT");
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                count = CommonUtil.trackValue(cursor.getString(0));
            }
            LogUtil.d("获取发现好友数量 success");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("获取发现好友数量 Exception");
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return count;
    }

    /**
     * @author: chuwx
     * @Title: queryNubeFriends
     * @Description:查询视频联系人
     * @return
     * @date 2013-8-7 下午4:47:57
     */
    @Override
    public Cursor queryNubeFriends() {
        Cursor cursor = null;
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/QUERY_VIDEO_LINKMAN");
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            LogUtil.d("查询视频联系人 success");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("查询视频联系人 Exception", e);
            return null;
        }
        return cursor;
    }

    /**
     * @author: wangyf
     * @Title: queryNubeFriendsByFullPym
     * @Description:查询视频联系人(根据姓名的升序排序)
     * @return
     * @date 2013-8-7 下午4:47:57
     */
    @Override
    public Cursor queryNubeFriendsByFullPym() {
        Cursor cursor = null;
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/QUERY_VIDEO_LINKMAN_BY_FULLPYM");
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            LogUtil.d("查询视频联系人success");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("查询视频联系人 Exception");
            return null;
        }
        return cursor;
    }

    public Cursor queryNubeContactUser() {

        Cursor cursor = null;
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/QUERY_VIDEO_LINKMAN_BY_FULLPYM_FOR_XIN");
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            LogUtil.d("查询视频联系人success");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("查询视频联系人 Exception");
            return null;
        }
        return cursor;

    }

    public String queryInfoByNumber(String nubenumber) {
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/QUERY_INFO_ROR_FAMILYE_BY_NUBENUMBER/" + nubenumber);
        Cursor cursor = null;

        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                String nameString = cursor.getString(0);
                return nameString;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("queryFriendInfo Exception");
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

        return null;
    }

    /**
     * @author: chuwx
     * @Title: queryFindFriends
     * @Description:查询发现好友
     * @return
     * @date 2013-8-7 下午4:48:03
     */
    @Override
    public Cursor queryFindFriends() {
        Cursor cursor = null;
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/QUERY_FIND_LINKMAN");
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            LogUtil.d("查询发现好友 success");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("查询发现好友 Exception", e);
            return null;
        }
        return cursor;
    }

    /**
     * @author: chuwx
     * @Title: getMaxSortkey
     * @Description:查询最大sortkey
     * @return
     * @date: 2013-8-16 下午2:28:25
     */
    private int getMaxSortkey() {
        Uri queryUri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/QUERY_MAX_SORT");
        Cursor sortCursor = null;
        int maxSort = 10000;
        try {
            sortCursor = getContentResolver().query(queryUri, null, null, null,
                null);
            if (sortCursor != null && sortCursor.getCount() > 0) {
                sortCursor.moveToFirst();
                maxSort = sortCursor.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("查询最大排序值");
        } finally {
            if (sortCursor != null) {
                sortCursor.close();
            }
        }
        LogUtil.d("当前最大排序值 :" + maxSort);
        return maxSort;
    }

    /**
     * @author: chuwx
     * @Title: updateDownloadData
     * @Description:批量处理下载数据
     * @param contacts
     * @throws Exception
     * @date 2013-8-11 下午6:26:26
     */
    @Override
    public void updateDownloadData(List<ContactBean> contacts) throws Exception {
        LogUtil.d("批量处理下载数据     start");
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        // 物理删除数据url
        Uri deleteUri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/DELETE_LINKMAN");
        // 修改数据url
        Uri updateUri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/UPDATE_LINKMAN_ITEM");
        // 新增数据Url
        Uri insertUri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/INSERT_LINKMAN_ITEM");

        // 【存储app内联系人的主键】key：nubenumber，value：contactId
        Map<String, String> appKeyMap = new HashMap<String, String>();
        // 【存储app内联系人的对应系统id】key：nubenumber，value：sourceId
        Map<String, String> sourceKeyMap = new HashMap<String, String>();
        // 【针对app内含有相同视频号，保存对应未删除的那条记录】
        List<String> idList = new ArrayList<String>();

        // 查询视频表数据，【处理冗余数据】
        queryNubefirendsTable(ops, appKeyMap, sourceKeyMap, idList);

        // 存储同步下载接口返回联系人中新增的数据
        Map<String, String> remoteKeyMap = new HashMap<String, String>();

        int maxSortkey = getMaxSortkey();
        String nubeNumber;
        // String ownNumber = MedicalApplication.getPreference().getKeyValue(
        //     PrefType.LOGIN_NUBENUMBER, "");
        // 8.6号，后台存在与本地相同nubeNumber时，后续删除此号码，UI表现同{本地导入》添加为好友》删除}效果一致，即不能再次添加为好友；【之前逻辑没这么处理，开发认为是两个case，分类处理】
        boolean isFirstQuery = true;
        Map<String, String> invisibleSourceMap = new HashMap<String, String>();

        if (contacts != null && contacts.size() > 0) {
            for (ContactBean temp : contacts) {

                // 是否是新增数据
                boolean isNewData = true;

                try {
                    nubeNumber = temp.getNubeInfos().get(0).getNubeNumber();
                } catch (Exception e) {
                    LogUtil.e(
                        "temp.getNubeInfos() Exception:" + temp.toString(),
                        e);
                    continue;
                }

                String selection = "contactId = ?";// selection条件

                if (appKeyMap.containsKey(nubeNumber)) {
                    // 记录存在，且记录的同步状态为已同步，则检查服务器数据的删除标记，若为1，则物理删除本地数据，否则更新本地数据（包括版本号，但是同步状态还是置为已同步）
                    // 记录存在，且记录的同步状态为未同步，此时表明出现了数据冲突。此时采用服务器数据优先原则来处理。检查服务器数据的删除标记，若为true，则物理删除本地数据，否则更新本地数据（包括版本号，删除标记），同时将同步状态置为已同步。
                    if (1 == temp.getIsDeleted()) {
                        // 12.18号，若删除数据为本地导入数据，则只需要模糊[客户端不可见]删除此条数据；若此条数据为服务端数据，则物理删除词条数据
                        if (temp.getContactId().equals(
                            appKeyMap.get(nubeNumber))) {
                            LogUtil.d("deleteNubeLinkman start");
                            if (StringUtil
                                .isEmpty(sourceKeyMap.get(nubeNumber))) {
                                ops.add(ContentProviderOperation
                                    .newDelete(deleteUri)
                                    .withSelection(
                                        selection,
                                        new String[] { appKeyMap
                                            .get(nubeNumber) })
                                    .build());
                                LogUtil.d("该好友非本地发现记录，直接物理删除");
                            } else {
                                LogUtil.d("该好友为本地发现记录， 开始模糊删除联系人，即将ISMUTUALTRUST置为5，ISDELETED置为0,SYNCSTAT置为2");
                                ContentValues values = new ContentValues();
                                values.put(NubeFriendColumn.ISDELETED, 0);
                                values.put(NubeFriendColumn.SYNCSTAT, 2);
                                values.put(NubeFriendColumn.ISMUTUALTRUST,
                                    NubeFriendColumn.KEY_NO_VISIBLE);
                                ops.add(ContentProviderOperation
                                    .newUpdate(updateUri)
                                    .withValues(values)
                                    .withSelection(
                                        selection,
                                        new String[] { appKeyMap
                                            .get(nubeNumber) })
                                    .build());
                            }
                        } else {
                            // 3.6号针对【至少有一条数据是未删除的，其他为已经删除数据】...@目前这个case由于在同步上传的处理很少遇到
                            if (idList.contains(temp.getContactId())) {
                                String sourceId = getSourceId(temp
                                    .getContactId());
                                if (sourceId == null) {
                                    continue;
                                }
                                if (StringUtil.isEmpty(sourceId)) {// 联系人非本地导入的case
                                    LogUtil.d("deleteNubeLinkman : idList start");
                                    ops.add(ContentProviderOperation
                                        .newDelete(deleteUri)
                                        .withSelection(
                                            selection,
                                            new String[] { temp
                                                .getContactId() })
                                        .build());
                                } else {// 联系人是本地导入的case
                                    LogUtil.d("deleteNubeLinkman : idList 开始模糊删除联系人");
                                    ContentValues values = new ContentValues();
                                    values.put(NubeFriendColumn.ISDELETED, 0);
                                    values.put(NubeFriendColumn.SYNCSTAT, 2);
                                    values.put(NubeFriendColumn.ISMUTUALTRUST,
                                        NubeFriendColumn.KEY_NO_VISIBLE);
                                    ops.add(ContentProviderOperation
                                        .newUpdate(updateUri)
                                        .withValues(values)
                                        .withSelection(
                                            selection,
                                            new String[] { temp
                                                .getContactId() })
                                        .build());
                                }
                            }
                        }
                    } else {
                        // 目前以本地数据为主;本地联系人id为主，信息更新为服务端信息........[至少有一条数据是未删除的，其他为已经删除数据case补充]
                        if (temp.getContactId().equals(
                            appKeyMap.get(nubeNumber))
                            || idList.contains(temp.getContactId())) {
                            LogUtil.d("updateLinkmanByContactBean");
                            ContentValues values = resetConentValuesByPo(temp);
                            ops.add(ContentProviderOperation
                                .newUpdate(updateUri)
                                .withValues(values)
                                .withSelection(
                                    selection,
                                    new String[] { temp.getContactId() })
                                .build());
                        }
                    }
                    isNewData = false;
                }

                if (isNewData && 0 == temp.getIsDeleted()) { // 记录不存在， 且未删除状态,
                    if (isFirstQuery) {
                        isFirstQuery = false;
                        invisibleSourceMap = getSourceId();
                    }
                    // 针对纳贝号为空、纳贝号为登陆账号时不插入本地数据库
                    if (!remoteKeyMap.containsKey(nubeNumber)
                        && !TextUtils.isEmpty(nubeNumber)) {
                        // if (ownNumber.equals(nubeNumber)) {
                        //     LogUtil.d("排除登陆账号:" + ownNumber);
                        //     continue;
                        // }
                        LogUtil.d("insertLinkmanByContactBean");
                        ++maxSortkey;
                        remoteKeyMap.put(nubeNumber, nubeNumber);
                        ContentValues values = resetConentValuesByPo(temp);
                        values.put(NubeFriendColumn.SORTKEY, maxSortkey);
                        values.put(NubeFriendColumn.CONTACTID,
                            temp.getContactId());
                        values.put(NubeFriendColumn.ISONLINE, 0);

                        if (invisibleSourceMap.containsKey(nubeNumber)) {
                            values.put(NubeFriendColumn.SOURCESID,
                                invisibleSourceMap.get(nubeNumber));
                        }

                        ops.add(ContentProviderOperation.newInsert(insertUri)
                            .withValues(values).build());
                        LogUtil.d("为新增数据");
                    } else {
                        // 10.24号，逐步删除服务端重复数据
                        ++maxSortkey;
                        ContentValues values = resetConentValuesByPo(temp);
                        values.put(NubeFriendColumn.SORTKEY, maxSortkey);
                        values.put(NubeFriendColumn.CONTACTID,
                            temp.getContactId());
                        values.put(NubeFriendColumn.ISONLINE, 0);
                        values.put(NubeFriendColumn.ISDELETED, 1);
                        values.put(NubeFriendColumn.SYNCSTAT, 0);
                        ops.add(ContentProviderOperation.newInsert(insertUri)
                            .withValues(values).build());
                        LogUtil.d("remoteKeyMap containsKey:" + nubeNumber);
                    }
                }

                try {
                    if (ops != null
                        && ops.size() >= DBConstant.UPLOAD_FATCH_UNIT_SIZE) {
                        getContentResolver().applyBatch(
                            ProviderConstant.AUTHORITY, ops);
                        LogUtil.d("批量处理下载数据  success");
                        ops.clear();
                        ops = new ArrayList<ContentProviderOperation>();
                    }
                } catch (Exception e) {
                    LogUtil.e("批量处理下载数据[ops数量超过500]   Exception:", e);
                    e.printStackTrace();
                    appKeyMap.clear();
                    remoteKeyMap.clear();
                    sourceKeyMap.clear();
                    idList.clear();
                    throw e;
                }
            }
        }
        try {
            if (ops.size() > 0) {
                getContentResolver()
                    .applyBatch(ProviderConstant.AUTHORITY, ops);
                LogUtil.d("批量处理下载数据  success");
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("批量处理下载数据   Exception", e);
            throw e;
        } finally {
            appKeyMap.clear();
            remoteKeyMap.clear();
            sourceKeyMap.clear();
            idList.clear();
        }
    }

    private Map<String, String> getSourceId() {
        // 查询不可见数据 系统id Url
        Uri querySUri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/QUERY_SOURCE_ITEM");

        Map<String, String> invisibleMap = new HashMap<String, String>();
        Cursor cursor = null;

        try {
            cursor = getContentResolver().query(querySUri, null, null, null,
                null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    invisibleMap.put(cursor.getString(0), cursor.getString(2));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("getSourceId  Exception");
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return invisibleMap;
    }

    /**
     * @author: chuwx
     * @Title: queryNubefirendsTable
     * @Description:同步下载处理前，查询视频表数据，【处理冗余数据】
     * @param ops
     * @param appKeyMap
     * @param sourceKeyMap
     * @param idList
     * @throws Exception
     * @date: 2014-3-19 上午10:43:35
     */
    private void queryNubefirendsTable(ArrayList<ContentProviderOperation> ops,
                                       Map<String, String> appKeyMap, Map<String, String> sourceKeyMap,
                                       List<String> idList) throws Exception {
        // 修改数据url
        Uri updateUri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/UPDATE_LINKMAN_ITEM");
        // 查询数据url
        Uri queryUri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/QUERY_LINKMAN_KEY");

        Cursor cursor = null;

        // key：nubenumber，value：contactId[isDeleted=1]
        Map<String, String> sameMap = new HashMap<String, String>();
        try {
            cursor = getContentResolver().query(queryUri, null, null, null,
                null);
            if (cursor != null && cursor.getCount() > 0) {

                while (cursor.moveToNext()) {
                    if (appKeyMap.containsKey(cursor.getString(0))) {

                        if (cursor.getInt(2) == 1) {
                            // 标示该条数据删除的情况下
                            if (sameMap.containsKey(cursor.getString(0))) {
                                // 3.6号 验证，后台会返回最新时间戳；不可见数据可以分批次处理，UI上不会引起混乱
                                LogUtil.d("同步下载处理数据前，重置已经本地逻辑删除数据的同步状态为0");
                                ContentValues values = new ContentValues();
                                values.put("syncStat", 0);
                                ops.add(ContentProviderOperation
                                    .newUpdate(updateUri)
                                    .withValues(values)
                                    .withSelection(
                                        "contactId = ?",
                                        new String[] { cursor
                                            .getString(1) })
                                    .build());
                            } else {
                                // 3.6号： 至少有一条数据是未删除的，其他为已经删除数据[此时这种数据应该在一个批次处理]
                                idList.add(appKeyMap.get(cursor.getString(0)));// 获取未删除数据的id

                                LogUtil.d("同步下载处理数据前，替换所选择的重复数据[上一条数据未删除数据，保留状态]");
                                sameMap.put(cursor.getString(0),
                                    cursor.getString(1));
                                appKeyMap.put(cursor.getString(0),
                                    cursor.getString(1));
                                sourceKeyMap.put(cursor.getString(0),
                                    cursor.getString(3));
                            }
                        } else {
                            if (!sameMap.containsKey(cursor.getString(0))) {
                                // 3.6号：这个场景主要出现几率基本上为0；
                                LogUtil.d("同步下载处理数据前，上一条数据为未删除数据，删除其中一条数据");
                                ContentValues values = new ContentValues();
                                values.put("isDeleted", 1);
                                values.put("syncStat", 0);
                                ops.add(ContentProviderOperation
                                    .newUpdate(updateUri)
                                    .withValues(values)
                                    .withSelection(
                                        "contactId = ?",
                                        new String[] { cursor
                                            .getString(1) })
                                    .build());
                            } else {
                                // 3.6号： 至少有一条数据是未删除的，其他为已经删除数据
                                idList.add(cursor.getString(1));// 获取未删除数据的id
                            }
                        }
                    } else {
                        appKeyMap.put(cursor.getString(0), cursor.getString(1));
                        sourceKeyMap.put(cursor.getString(0),
                            cursor.getString(3));
                        if (cursor.getInt(2) == 1) {
                            sameMap.put(cursor.getString(0),
                                cursor.getString(1));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("查询同步key值  Exception");
            throw e;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
            sameMap.clear();
        }
    }

    private String getSourceId(String id) {
        if (StringUtil.isEmpty(id))
            return null;
        String result = "";
        Cursor cursor = null;
        Uri queryUri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/MATCH_NAME_BYNUMBER_ITEM");
        try {
            cursor = getContentResolver().query(queryUri,
                new String[] { "sourcesId" }, "contactId = ?",
                new String[] { id }, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                result = cursor.getString(0);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("getSourceId Exception :" + e.getMessage());
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return StringUtil.isEmpty(result) ? "" : result;
    }

    /**
     * @author: chuwx
     * @Title: queryLocalNumberFilterAppNumber
     * @Description:查询android本地联系人号码
     * @return
     * @date 2013-8-12 下午4:39:14
     */
    @Override
    public Cursor queryLocalNumberFilterAppNumber() {
        LogUtil.d("查询android本地联系人号码  start");
        Cursor localCursor;
        try {
            // 取得手机终端联系人，且在旺铺联系人中未重复
            localCursor = getContentResolver().query(Phone.CONTENT_URI,
                new String[] { Phone.RAW_CONTACT_ID, Phone.NUMBER }, null,
                null, Phone.RAW_CONTACT_ID);
            LogUtil.d("Import[query] ----- 查询LOCAL数据   success");
            if (localCursor != null)
                LogUtil.d("Import -----本地联系人数据量：" + localCursor.getCount());
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("Import[query] ----- 查询LOCAL数据   faile", e);
            return null;
        }
        LogUtil.d("查询android本地联系人号码  end");
        return localCursor;
    }

    /**
     * @author: chuwx
     * @Title: importLocalContactToApp
     * @Description:根据android raw_ContactId将本地联系人导入到应用数据库
     * @param remoteMap
     * @param _mark
     *            标示是否插入新朋友表
     * @return
     * @date 2013-8-13 上午11:19:50
     */
    @Override
    public boolean importLocalContactToApp(Map<String, NubeFriendBean> remoteMap,
                                           Boolean _mark) {
        LogUtil.d("importLocalContactToApp  start");

        // 返回结果
        boolean result = false;
        // 系统联系人查询cursor
        Cursor localCursor = null;

        Uri insertUri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/INSERT_LINKMAN_ITEM");
        Uri emptyDataUri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/QUERY_EMPTY_LINKMAN_ITEM");
        Uri updateUri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/UPDATE_LINKMAN_ITEM");

        if (remoteMap != null && remoteMap.size() > 0) {

            ArrayList<String> contactIdList = new ArrayList<String>();

            for (String key : remoteMap.keySet()) {
                contactIdList.add(key);
            }

            try {
                // 取得手机终端联系人所有信息
                String selection = Data.RAW_CONTACT_ID + " in("
                    + makePlaceholders(contactIdList.size()) + ") AND "
                    + Data.MIMETYPE + " in(?,?)";

                contactIdList.add(StructuredName.CONTENT_ITEM_TYPE);
                contactIdList.add(Phone.CONTENT_ITEM_TYPE);

                LogUtil.d("条件：" + selection + "----" + contactIdList);

                localCursor = getContentResolver()
                    .query(Data.CONTENT_URI,
                        new String[] { Data.RAW_CONTACT_ID,
                            Data.MIMETYPE, Data.DATA1, Data.DATA2 },
                        selection,
                        contactIdList.toArray(new String[contactIdList
                            .size()]), null);
                LogUtil.d("Import TO APP----查询LOCAL数据   success");
                if (localCursor != null) {
                    LogUtil.d("Import TO APP----- LOCAL数据量："
                        + localCursor.getCount());
                }
                // 系统联系人信息，结合remoteMap【后台联系人信息】，组装需要插入app数据库的ContentValues
                if (localCursor != null && localCursor.getCount() > 0) {
                    // key:系统raw_contact_id ,value:组装后的联系人信息
                    Map<String, ContentValues> requestMap = new HashMap<String, ContentValues>();
                    // 当前最大排序值
                    int maxSort = getMaxSortkey();

                    ContentValues values;
                    ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
                    NubeFriendBean info;
                    while (localCursor.moveToNext()) {
                        String id = localCursor.getString(0);// 系统raw_contact_id
                        info = remoteMap.get(id);
                        values = requestMap.get(id);
                        if (values == null) {
                            values = new ContentValues();
                            ++maxSort;
                            values.put(NubeFriendColumn.SOURCESID, id);
                            values.put(NubeFriendColumn.ISDELETED, 0);
                            values.put(NubeFriendColumn.SYNCSTAT, 0);
                            values.put(NubeFriendColumn.ISONLINE, 0);
                            values.put(NubeFriendColumn.ISMUTUALTRUST,
                                NubeFriendColumn.KEY_VISIBLE);// 默认数据可见
                            values.put(NubeFriendColumn.HEADURL,
                                info.getHeadUrl());
                            values.put(NubeFriendColumn.NUBENUMBER,
                                info.getNubeNumber());
                            values.put(NubeFriendColumn.CONTACTUSERID,
                                info.getUid());
                            values.put(NubeFriendColumn.CONTACTID,
                                CommonUtil.getUUID());
                            values.put(NubeFriendColumn.SORTKEY, maxSort);
                            // values.put(NubeFriendColumn.SEX,
                                // CommonUtil.getSex(info.getSex()));
                            // 昵称>手机号->视讯号
                            // String nickname = info.getNickname();
                            // if (TextUtils.isEmpty(nickname)){
                            // nickname=info.getNumber();
                            // if (TextUtils.isEmpty(nickname)){
                            // nickname=info.getNubeNumber();
                            // }
                            // }
                            // values.put(NubeFriendColumn.NICKNAME, nickname);
                        }
                        String mimeType = CommonUtil.trackValue(localCursor
                            .getString(1));
                        String nameOrNum = CommonUtil.trackValue(localCursor
                            .getString(2));
                        if (StructuredName.CONTENT_ITEM_TYPE.equals(mimeType)) {
                            // mimeType为name[以本地联系人姓名为依据,如果为空，则以服务端为准]
                            if (!TextUtils.isEmpty(nameOrNum)) {
                                nameOrNum = nameOrNum.trim();
                            }
                            if (StringUtil.isEmpty(nameOrNum)) {

                                nameOrNum = queryLocalLinkmanName(id);
                            }
                            if (StringUtil.isEmpty(nameOrNum.trim())) {
                                if (!TextUtils.isEmpty(info.getName())) {
                                    nameOrNum = info.getName();
                                } else {
                                    // 同步时此字段不为空
                                    // nameOrNum = AndroidUtil
                                    //     .getString(R.string.importLocalContactToApp_name);
                                }
                            }
                            // nameOrNum = CommonUtil.fliteIllegalChar(nameOrNum);
                            values.put(NubeFriendColumn.NAME, nameOrNum);

                            // if
                            // (values.containsKey(NubeFriendColumn.NICKNAME)) {
                            // String nick = (String) (values
                            // .get(NubeFriendColumn.NICKNAME));
                            // if (StringUtil.isEmpty(nick)) {
                            // values.put(NubeFriendColumn.NICKNAME,
                            // nameOrNum);
                            // }
                            // } else {
                            // values.put(NubeFriendColumn.NICKNAME, nameOrNum);
                            // }
                        } else if (Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
                            // mimeType为phone
                            if (values.containsKey(NubeFriendColumn.NUMBER)) {

                            } else {
                                if (!StringUtil.isEmpty(info.getMobile())) {
                                    nameOrNum = info.getNumber();
                                    values.put(NubeFriendColumn.NUMBER,
                                        nameOrNum);
                                } else if (!StringUtil.isEmpty(nameOrNum)) {
                                    // 格式化手机号码
                                    // nameOrNum = CommonUtil
                                    //     .simpleFormatMoPhone(nameOrNum);
                                    // TODO:
                                    LogUtil.d("本地号码:格式化之后：nameOrNum="
                                        + nameOrNum);
                                    LogUtil.d("localCursor.getInt(30)="
                                        + localCursor.getInt(3));
                                    if (Phone.TYPE_MOBILE == localCursor
                                        .getInt(3)) {
                                        values.put(NubeFriendColumn.NUMBER,
                                            nameOrNum);
                                    }
                                }
                            }
                        }
                        requestMap.put(id, values);
                    }

                    if (localCursor != null) {
                        localCursor.close();
                        localCursor = null;
                    }

                    Cursor emptyCursor = null;
                    Map<String, String> emptyMap = new HashMap<String, String>();
                    try {
                        // contactId，number，nubeNumber
                        emptyCursor = getContentResolver().query(emptyDataUri,
                            null, null, null, null);
                        if (emptyCursor != null && emptyCursor.getCount() > 0) {
                            while (emptyCursor.moveToNext()) {
                                // <nubeNumber,contactId>
                                emptyMap.put(emptyCursor.getString(2),
                                    emptyCursor.getString(0));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogUtil.e("Exception", e);
                    } finally {
                        if (emptyCursor != null) {
                            emptyCursor.close();
                            emptyCursor = null;
                        }
                    }

                    ContentValues newValues;
                    if (requestMap != null && requestMap.size() > 0) {
                        for (Entry<String, ContentValues> entry : requestMap
                            .entrySet()) {
                            if (emptyMap.containsKey(entry.getValue()
                                .getAsString("nubeNumber"))) {
                                // 合并联系人
                                newValues = new ContentValues();
                                newValues.put("syncStat", 0);
                                newValues.put("number", entry.getValue()
                                    .getAsString("number"));
                                // newValues.put("sex", entry.getValue()
                                // .getAsString("sex"));
                                ops.add(ContentProviderOperation
                                    .newUpdate(updateUri)
                                    .withValues(newValues)
                                    .withSelection(
                                        "contactId = ?",
                                        new String[] { emptyMap
                                            .get(entry
                                            .getValue()
                                            .getAsString(
                                                "nubeNumber")) })
                                    .build());
                                LogUtil.d("Import TO APP----- 合并好友nubenumber："
                                    + entry.getValue().getAsString(
                                    "nubeNumber"));
                                continue;
                            }

                            newValues = entry.getValue();
                            if (_mark) {
                                // 在视频联系人中标示新朋友，但是不在视频联系人中显示
                                newValues
                                    .put(NubeFriendColumn.ISMUTUALTRUST, 5);
                            }
                            ops.add(ContentProviderOperation
                                .newInsert(insertUri).withValues(newValues)
                                .build());
                            if (_mark) {
                                // 插入新朋友
                                newValues = new ContentValues();
                                newValues.put(
                                    NewFriendTable.NEWFRIEND_COLUMN_ID,
                                    CommonUtil.getUUID());
                                newValues
                                    .put(NewFriendTable.NEWFRIEND_COLUMN_LASTTIME,
                                        DateUtil.getDBOperateTime());
                                newValues.put(
                                    NewFriendTable.NEWFRIEND_COLUMN_NAME,
                                    (String) entry.getValue().get(
                                        "nickname"));
                                newValues
                                    .put(NewFriendTable.NEWFRIEND_COLUMN_NUMBER,
                                        (String) entry.getValue().get(
                                            "number"));
                                newValues
                                    .put(NewFriendTable.NEWFRIEND_COLUMN_NUBENUMBER,
                                        (String) entry.getValue().get(
                                            "nubeNumber"));
                                newValues.put(
                                    NewFriendTable.NEWFRIEND_COLUMN_STATUS,
                                    NewFriendBean.LOCAL_FIND_STATUS);
                                newValues.put(
                                    NewFriendTable.NEWFRIEND_COLUMN_ISNEW,
                                    1);
                                newValues
                                    .put(NewFriendTable.NEWFRIEND_COLUMN_CONTACTUSERID,
                                        (String) entry.getValue().get(
                                            "contactUserId"));
                                newValues
                                    .put(NewFriendTable.NEWFRIEND_COLUMN_HEADURL,
                                        (String) entry.getValue().get(
                                            "headUrl"));
                                newValues
                                    .put(NewFriendTable.NEWFRIEND_COLUMN_REALNAME,
                                        (String) entry.getValue().get(
                                            "name"));
                                newValues.put(
                                    NewFriendTable.NEWFRIEND_COLUMN_SEX,
                                    String.valueOf(entry.getValue().get(
                                        "sex")));
                                newValues
                                    .put(NewFriendTable.NEWFRIEND_COLUMN_VISIBLE,
                                        1);
                                LogUtil.d("Import TO APP-----新朋友:"
                                    + entry.getValue().get("name"));
                                ops.add(ContentProviderOperation
                                    .newInsert(
                                        ProviderConstant.NETPHONE_NEWFRIEND_URI)
                                    .withValues(newValues).build());
                            }

                            if (ops != null
                                && ops.size() >= DBConstant.UPLOAD_FATCH_UNIT_SIZE) {
                                try {
                                    getContentResolver().applyBatch(
                                        ProviderConstant.AUTHORITY, ops);
                                    LogUtil.d("Import TO APP-----  根据android raw_ContactId将本地联系人导入到应用数据库  分批次插入成功");
                                    ops.clear();
                                    ops = new ArrayList<ContentProviderOperation>();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    LogUtil.e(
                                        "Import TO APP-----  根据android raw_ContactId将本地联系人导入到应用数据库   Exception",
                                        e);
                                    return false;
                                }
                            }
                        }
                    }
                    try {
                        if (ops.size() > 0) {
                            getContentResolver().applyBatch(
                                ProviderConstant.AUTHORITY, ops);
                            result = true;
                            LogUtil.d("Import TO APP-----  根据android raw_ContactId将本地联系人导入到应用数据库(包括新朋友)  success");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogUtil.e(
                            "Import TO APP-----  根据android raw_ContactId将本地联系人导入到应用数据库   Exception",
                            e);
                    }
                    // 清空后台返回数据 map
                    remoteMap.clear();
                } else {
                    if (localCursor != null && localCursor.getCount() == 0)
                        result = true;
                }

            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.e("查询本地联系人异常", e);
                return result;
            } finally {
                if (localCursor != null) {
                    localCursor.close();
                    localCursor = null;
                }
            }

        }
        LogUtil.d("importLocalContactToApp  end");
        return result;
    }

    /**
     * 查找本地联系人姓名
     *
     * @param id
     * @return
     */
    private String queryLocalLinkmanName(String id) {
        LogUtil.d("queryLocalLinkmanName  START ");
        String name = "";
        Cursor localCursor = null;
        try {
            localCursor = getContentResolver().query(
                Data.CONTENT_URI,
                new String[] { Data.RAW_CONTACT_ID, Data.MIMETYPE,
                    Data.DATA1, Data.DATA3, Data.DATA2 },
                Data.MIMETYPE + " in (?) and " + Data.RAW_CONTACT_ID
                    + " = '" + id + "'",
                new String[] { StructuredName.CONTENT_ITEM_TYPE }, null);
            if (localCursor != null && localCursor.getCount() > 0) {
                localCursor.moveToFirst();
                String firstName = localCursor.getString(3);
                String lastName = localCursor.getString(4);
                if (StringUtil.isEmpty(firstName)) {
                    firstName = "";
                }
                if (StringUtil.isEmpty(lastName)) {
                    lastName = "";
                }
                name = firstName + lastName;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("queryLocalLinkmanName  Exception");
        } finally {
            if (localCursor != null) {
                localCursor.close();
                localCursor = null;
            }
        }
        LogUtil.d("queryLocalLinkmanName[组装] name:" + name);
        return name;
    }

    /**
     * @author: chuwx
     * @Title: getAppLinkmanData
     * @Description:获取应用联系人
     * @return
     * @date 2013-8-14 下午5:12:25
     */
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
            LogUtil.d("获取应用联系人   Exception");
            list = null;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return list;
    }

    /**
     * @author: liujc
     * @Title: getAppLinkmanNumberData
     * @Description:获取应用联系人手机号码
     * @return
     * @date 2015-03-13 下午4:13:25
     */
    @Override
    public Map<String, String> getAppLinkmanNumberData() {
        Map<String, String> map = new HashMap<String, String>();
        Cursor cursor = null;
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/GET_APP_LINKMAN_NUMBER_DATA_NEW");
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    map.put(CommonUtil.trackValue(cursor.getString(0)),
                        CommonUtil.trackValue(cursor.getString(0)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("获取应用联系人手机号码   Exception");
            map = null;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return map;
    }

    /**
     * @author: niuben
     * @Title: getLocationLinkmanDataHashMap
     * @Description: 获取本地联系人
     * @return
     * @date 2015-12-18 下午5:31:08
     */
    @Override
    public HashMap<String, ContactFriendBean> getLocationLinkmanDataHashMap() {
        Cursor localCursor = null;
        HashMap<String, ContactFriendBean> map = new HashMap<String, ContactFriendBean>();
        ContactFriendBean info;
        try {
            localCursor = getContentResolver().query(
                Phone.CONTENT_URI,
                new String[] { Phone._ID, Phone.NUMBER, Phone.DISPLAY_NAME,
                    Phone.SORT_KEY_PRIMARY }, null, null,
                Phone.SORT_KEY_PRIMARY + "," + Phone._ID);
            if (localCursor != null) {
                LogUtil.d("LOCAL数据量：" + localCursor.getCount());
            }
            String isPhone = null;
            if (localCursor != null && localCursor.getCount() > 0) {
                while (localCursor.moveToNext()) {
                    info = new ContactFriendBean();
                    info.setSourcesId(CommonUtil.trackValue(localCursor
                        .getString(0)));
                    // String phoneNum = CommonUtil.simpleFormatMoPhone(CommonUtil
                    //     .trackValue(localCursor.getString(1)));
                    // if (TextUtils.isEmpty(phoneNum)) {
                    //     continue;
                    // }
                    // info.setNumber(phoneNum);
                    // info.setName(CommonUtil.trackValue(localCursor.getString(2)));
                    // if (CommonUtil.trackValue(localCursor.getString(3)).equals(
                    //     CommonUtil.trackValue(localCursor.getString(2)))) {
                    //     info.setPym(PinyinUtil.getPinYin(CommonUtil
                    //         .trackValue(localCursor.getString(3))));
                    // } else {
                    //     info.setPym(CommonUtil.trackValue(localCursor
                    //         .getString(3)));
                    // }
                    // map.put(phoneNum, info);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("获取本地联系人   Exception");
        } finally {
            if (localCursor != null) {
                localCursor.close();
                localCursor = null;
            }
        }
        return map;
    }

    /**
     * @author: chuwx
     * @Title: getLocationLinkmanData
     * @Description: 获取本地联系人
     * @return
     * @date 2013-8-14 下午5:31:08
     */
    @Override
    public List<ContactFriendBean> getLocationLinkmanData() {
        Cursor localCursor = null;
        List<ContactFriendBean> list = new ArrayList<ContactFriendBean>();
        ContactFriendBean info;
        try {
            localCursor = getContentResolver().query(
                Phone.CONTENT_URI,
                new String[] { Phone._ID, Phone.NUMBER, Phone.DISPLAY_NAME,
                    Phone.SORT_KEY_PRIMARY }, null, null,
                Phone.SORT_KEY_PRIMARY + "," + Phone._ID);
            if (localCursor != null) {
                LogUtil.d("LOCAL数据量：" + localCursor.getCount());
            }
            Map<String, String> map = new HashMap<String, String>();
            // Map<String, ContactFriendBean> map = new HashMap<String,
            // ContactFriendBean>();
            String isPhone = null;
            if (localCursor != null && localCursor.getCount() > 0) {
                // String telphoneString = MedicalApplication.getPreference()
                //     .getKeyValue(PrefType.LOGIN_MOBILE, "");
                while (localCursor.moveToNext()) {

                    // if (map.containsKey(CommonUtil.trackValue(localCursor
                    // .getString(0)))) {
                    // info = map.get(CommonUtil.trackValue(localCursor
                    // .getString(0)));
                    // isPhone = info.getNumber();
                    // isPhone = isPhone.replace(" ", "").replace("-", "");
                    // if (!TextUtils.isEmpty(isPhone)
                    // && isPhone.startsWith("+86")) {
                    // isPhone = isPhone.substring(3);
                    // }
                    // if (!isPhone.matches("^[1][0-9]{10}$")) {
                    // // @李红生 2014.3.13 针对一个联系人有多个号码优先显示手机号，如果没有手机号则显示非手机号
                    // info.setNumber(CommonUtil.trackValue(localCursor
                    // .getString(1)));
                    // }
                    // continue;
                    // }
                    info = new ContactFriendBean();
                    info.setSourcesId(CommonUtil.trackValue(localCursor
                        .getString(0)));
                    String phoneNum = CommonUtil
                        .trackValue(localCursor.getString(1))
                        .replace(" ", "").replace("-", "");

                    if (!TextUtils.isEmpty(phoneNum)
                        && phoneNum.startsWith("+86")) {
                        phoneNum = phoneNum.substring(3);
                    }

                    /** cheakPhoneNumber方法只将手机号取出来，新需求修改为：取出手机号和固话--2015/8/14-- */
                    // if (!CommonUtil.cheakPhoneNumber(phoneNum)) {
                    // LogUtil.d(phoneNum + "号码不合法");
                    // continue;
                    // }
                    // if (!CommonUtil.checkMobile(phoneNum)
                    //     && !CommonUtil.checkPhone(phoneNum)) {
                    //     LogUtil.d(phoneNum + "号码不合法:既不是手机号也不是固话");
                    //     continue;
                    // }
                    /****/
                    // if (phoneNum.equals(telphoneString)) {
                    //     LogUtil.d("号码为登陆账号对应的手机号码");
                    //     continue;
                    // }
                    if (TextUtils.isEmpty(map.get(phoneNum))) {
                        map.put(phoneNum, phoneNum);
                    } else {
                        if (TextUtils.isEmpty(phoneNum)) {
                            LogUtil.d("号码为空");
                        } else {
                            LogUtil.d(phoneNum + "为本地重复手机号码");
                        }
                        continue;
                    }
                    info.setNumber(phoneNum);
                    info.setName(CommonUtil.trackValue(localCursor.getString(2)));
                    info.setInviteType("2");// 本地通讯录用户
                    // if (CommonUtil.trackValue(localCursor.getString(3)).equals(
                    //     CommonUtil.trackValue(localCursor.getString(2)))) {
                    //     info.setPym(PinyinUtil.getPinYin(CommonUtil
                    //         .trackValue(localCursor.getString(3))));
                    // } else {
                    //     info.setPym(CommonUtil.trackValue(localCursor
                    //         .getString(3)));
                    // }

                    // map.put(info.getSourcesId(), info);
                    list.add(info);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("获取本地联系人   Exception");
            list = null;
        } finally {
            if (localCursor != null) {
                localCursor.close();
                localCursor = null;
            }
        }
        return list;
    }

    /**
     * 可能获取到数据，尽量不要这样使用
     */
    @Override
    public ContactFriendBean getLocationLinkmanDataByMobile(String mobile) {
        Cursor localCursor = null;
        ContactFriendBean info;
        try {
            localCursor = getContentResolver().query(
                Phone.CONTENT_URI,
                new String[] { Phone._ID, Phone.NUMBER, Phone.DISPLAY_NAME,
                    Phone.SORT_KEY_PRIMARY },
                Phone.NUMBER + "= '" + mobile + "'", null, null);
            if (localCursor != null) {
                LogUtil.d("LOCAL数据量：" + localCursor.getCount());
            }
            // Map<String, String> map = new HashMap<String, String>();
            String isPhone = null;
            if (localCursor != null && localCursor.getCount() > 0) {
                localCursor.moveToFirst();
                // String telphoneString = NetPhoneApplication.getPreference()
                //     .getKeyValue(PrefType.LOGIN_MOBILE, "");

                info = new ContactFriendBean();
                // info.setSourcesId(CommonUtil.trackValue(localCursor
                // .getString(0)));
                String phoneNum = CommonUtil
                    .trackValue(localCursor.getString(1)).replace(" ", "")
                    .replace("-", "");

                if (!TextUtils.isEmpty(phoneNum) && phoneNum.startsWith("+86")) {
                    phoneNum = phoneNum.substring(3);
                }

                // if (!CommonUtil.checkMobile(phoneNum)
                //     && !CommonUtil.checkPhone(phoneNum)) {
                //     LogUtil.d(phoneNum + "号码不合法:既不是手机号也不是固话");
                //     return null;
                // }
                // if (phoneNum.equals(telphoneString)) {
                //     LogUtil.d("号码为登陆账号对应的手机号码");
                //     return null;
                // }
                // if (TextUtils.isEmpty(map.get(phoneNum))) {
                // map.put(phoneNum, phoneNum);
                // } else {
                // if (TextUtils.isEmpty(phoneNum)) {
                // LogUtil.d("号码为空");
                // } else {
                // LogUtil.d(phoneNum + "为本地重复手机号码");
                // }
                // return null;
                // }
                info.setNumber(phoneNum);
                info.setName(CommonUtil.trackValue(localCursor.getString(2)));
                LogUtil.d("displayName="
                    + CommonUtil.trackValue(localCursor.getString(2)));
                // info.setInviteType("2");// 本地通讯录用户
                // if (CommonUtil.trackValue(localCursor.getString(3)).equals(
                // CommonUtil.trackValue(localCursor.getString(2)))) {
                // info.setPym(PinyinUtil.getPinYin(CommonUtil
                // .trackValue(localCursor.getString(3))));
                // } else {
                // info.setPym(CommonUtil.trackValue(localCursor.getString(3)));
                // }

                return info;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("获取本地联系人   Exception");
        } finally {
            if (localCursor != null) {
                localCursor.close();
                localCursor = null;
            }
        }
        return null;
    }

    /**
     * @author: chuwx
     * @Title: simpleFormatMoPhone
     * @Description: 简单格式化手机号码
     * @param phone
     * @return
     * @date: 2014-3-18 下午3:19:07
     */
    public static String simpleFormatMoPhone(String phone) {
        if (TextUtils.isEmpty(phone)) {
            return "";
        }
        String oldPhone = phone;
        phone = phone.replace("-", "").replace(" ", "");
        if (phone.startsWith("+86") && phone.length() == 14) {
            phone = phone.substring(3);
        }
        LogUtil.d("简单格式化手机号码:" + oldPhone + "---->" + phone);
        return phone;
    }

    /**
     * @author: chuwx
     * @Title: getLocationLinkmanData
     * @Description:根据id获取联系人详细信息
     * @param idList
     * @return
     * @date 2013-8-14 下午5:31:11
     */
    @Override
    public List<ContactFriendBean> getLocationLinkmanData(Set<String> idList) {
        Cursor localCursor = null;
        List<ContactFriendBean> list = new ArrayList<ContactFriendBean>();
        Map<String, ContactFriendBean> map = new HashMap<String, ContactFriendBean>();
        ContactFriendBean info;

        ArrayList<String> contactIdList = new ArrayList<String>();
        for (String key : idList) {
            contactIdList.add(key);
        }
        if (idList != null && contactIdList.size() > 0) {
            try {
                // 取得手机终端联系人所有信息
                String selection = Data.RAW_CONTACT_ID + " in("
                    + makePlaceholders(idList.size()) + ") AND "
                    + Data.MIMETYPE + " in(?,?)";
                contactIdList.add(StructuredName.CONTENT_ITEM_TYPE);
                contactIdList.add(Phone.CONTENT_ITEM_TYPE);

                LogUtil.d("条件：" + selection + "----" + contactIdList);
                localCursor = getContentResolver()
                    .query(Data.CONTENT_URI,
                        new String[] { Data.RAW_CONTACT_ID,
                            Data.MIMETYPE, Data.DATA1, Data.DATA2 },
                        selection,
                        contactIdList.toArray(new String[contactIdList
                            .size()]), null);
                if (localCursor != null)
                    LogUtil.d("LOCAL数据量：" + localCursor.getCount());

                if (localCursor != null && localCursor.getCount() > 0) {

                    while (localCursor.moveToNext()) {
                        String id = CommonUtil.trackValue(localCursor
                            .getString(0));
                        info = map.get(id);
                        if (info == null) {
                            info = new ContactFriendBean();
                            info.setSourcesId(id);
                        }
                        String mimeType = localCursor.getString(1);
                        String nameOrNum = localCursor.getString(2);
                        LogUtil.d(mimeType + "-----" + nameOrNum + "---");
                        if (StructuredName.CONTENT_ITEM_TYPE.equals(mimeType)) {
                            if (!TextUtils.isEmpty(nameOrNum)) {
                                nameOrNum = nameOrNum.replace(" ", "");
                            }
                            info.setName(nameOrNum);
                        } else if (Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
                            // if (!TextUtils.isEmpty(nameOrNum)) {
                            //     nameOrNum = CommonUtil
                            //         .simpleFormatMoPhone(nameOrNum);
                            //     if (Phone.TYPE_MOBILE == localCursor.getInt(3)) {
                            //         info.setNumber(nameOrNum);
                            //     }
                            // }
                        }
                        map.put(id, info);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.d("根据id获取联系人详细信息   Exception");
                list = null;

            } finally {
                if (localCursor != null) {
                    localCursor.close();
                    localCursor = null;
                }
            }
            if (map != null) {
                for (Entry<String, ContactFriendBean> entry : map.entrySet()) {
                    list.add(entry.getValue());
                }
            }
        } else {
            list = null;
        }
        return list;
    }

    private String makePlaceholders(int len) {
        if (len < 1) {
            // It will lead to an invalid query anyway ..
            throw new RuntimeException("No placeholders");
        } else {
            StringBuilder sb = new StringBuilder(len * 2 - 1);
            sb.append("?");
            for (int i = 1; i < len; i++) {
                sb.append(",?");
            }
            return sb.toString();
        }
    }

    @Override
    public void updateContactSort(String fromId, String toId,
                                  String dragContactId) {
        LogUtil.d("更新联系人排序信息：updateContactSort from " + fromId + " to " + toId);
        // 注意：

        Uri multUpdateUri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/UPDATE_MULT_CONTACT_SORTKEY_INFO/" + fromId + "/" + toId);
        try {
            getContentResolver().update(multUpdateUri, null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("更新位置   Exception " + e.getMessage());
        }

        Uri updateUri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/UPDATE_CONTACT_SORTKEY_INFO");
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ContentValues values = new ContentValues();
        values.put("sortKey", toId);
        ContentProviderOperation op = ContentProviderOperation
            .newUpdate(updateUri)
            .withSelection(" sortKey = ? AND contactId = ?",
                new String[] { fromId, dragContactId })
            .withValues(values).build();
        ops.add(op);
        try {
            if (ops.size() > 0) {
                getContentResolver()
                    .applyBatch(ProviderConstant.AUTHORITY, ops);
                LogUtil.d("更新位置 success");
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("更新位置   Exception " + e.getMessage());
        }
    }

    /**
     * @author: chuwx
     * @Title: updateFriendsAet
     * @param id
     * @param authStatus
     * @return
     * @date 2013-8-15 上午9:37:56
     */
    @Override
    public boolean updateFriendsAet(String id, int authStatus) {
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/UPDATE_AUTH_STATUS");
        try {
            ContentValues values = new ContentValues();
            values.put("isMutualTrust", authStatus);
            if (authStatus == 0 || authStatus == 1) {
                values.put("syncStat", 0);
            }
            getContentResolver().update(uri, values, "contactId='" + id + "'",
                null);
            LogUtil.d("更新好友认证状态 success");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("更新好友认证状态   Exception");
            return false;
        }
        return true;
    }

    @Override
    public boolean insertLinkman(ContactFriendBean info) {
        if (matchContactBynubenumber(info.getNubeNumber())) {
            // 已经添加过，直接返回
            LogUtil.d("已经添加过，直接返回：" + info.getNubeNumber());
            return true;
        }

        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/INSERT_LINKMAN_ITEM");
        if (info != null) {
            ContentValues values = new ContentValues();
            int maxSort = getMaxSortkey();
            if (StringUtil.isEmpty(info.getContactId())) {
                info.setContactId(CommonUtil.getUUID());
            }
            values.put("contactId", info.getContactId());
            values.put("name", info.getName());
            values.put("nickname", info.getNickname());
            values.put("firstName", info.getFirstName());
            values.put("lastName", info.getLastName());
            values.put("headUrl", info.getHeadUrl());
            values.put("contactUserId", info.getUid());
            values.put("isDeleted", 0);
            values.put("isOnline", 0);
            values.put("number", info.getNumber());
            // values.put("isMutualTrust", info.getIsMutualTrust());
            values.put("isMutualTrust", 1);
            values.put("nubeNumber", info.getNubeNumber());
            values.put("syncStat", 0);
            values.put("sortKey", ++maxSort);
            values.put("userType", 0);
            // values.put("sex", CommonUtil.getSex(info.getSex()));
            // values.put("reserveStr1", info.getShowMoblie());
            //
            // String pym = CommonUtil.getPymByParams(info.getName(),
            //     info.getNickname(), info.getNubeNumber());
            // values.put(NubeFriendColumn.FULLPYM, pym);

            try {
                getContentResolver().insert(uri, values);
                LogUtil.d("插入一条好友记录  success");
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.d("插入一条好友记录  Exception");
            }
        }
        return false;
    }

    /**
     * @author: chuwx
     * @Title: queryFriendInfo
     * @Description:查询nube联系人信息
     * @param contactId
     * @return
     * @date 2013-8-15 上午11:06:23
     */
    @Override
    public ContactFriendBean queryFriendInfo(String contactId) {
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/QUERY_LINKMAN_ITEM/" + contactId);
        Cursor cursor = null;
        ContactFriendBean bean = null;
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                bean = new ContactFriendBean();
                bean.setContactId(CommonUtil.trackValue(cursor.getString(0)));
                bean.setName(CommonUtil.trackValue(cursor.getString(1)));
                bean.setNickname(CommonUtil.trackValue(cursor.getString(2)));
                bean.setFirstName(CommonUtil.trackValue(cursor.getString(3)));
                bean.setLastName(CommonUtil.trackValue(cursor.getString(4)));

                bean.setNumber(CommonUtil.trackValue(cursor.getString(8)));
                bean.setNubeNumber(CommonUtil.trackValue(cursor.getString(9)));
                bean.setIsMutualTrust(cursor.getInt(10));
                bean.setHeadUrl(CommonUtil.trackValue(cursor.getString(11)));
                return bean;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("queryFriendInfo Exception");
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return bean;
    }

    /**
     * @author: chuwx
     * @Title: updateOnlineByPhone
     * @Description:根据号码更新好友在线状态
     * @param map
     * @return
     * @date 2013-8-16 下午1:18:39
     */
    @Override
    public boolean updateOnlineByPhone(Map<String, String> map) {
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/UPDATE_ONLINE_STATUS_TIME");
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ContentValues addCvs;
        try {
            if (map != null && map.size() > 0) {
                for (Entry<String, String> temp : map.entrySet()) {
                    if (StringUtil.isEmpty(temp.getValue()))
                        continue;
                    addCvs = new ContentValues();
                    if ("1".equals(temp.getValue())) {
                        // 不在线
                        addCvs.put("isOnline", 0);
                    } else {
                        // 在线
                        addCvs.put("isOnline", 1);
                    }
                    ops.add(ContentProviderOperation
                        .newUpdate(uri)
                        .withValues(addCvs)
                        .withSelection(
                            " isMutualTrust in (0,1) and nubeNumber = ? ",
                            new String[] { temp.getKey() }).build());

                }
            }
            if (ops.size() > 0) {
                getContentResolver()
                    .applyBatch(ProviderConstant.AUTHORITY, ops);
                LogUtil.d("根据号码更新好友在线状态   success");
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("根据号码更新好友在线状态   Exception");
        }
        return false;
    }

    public boolean updateOnlineByPhone(List<Map<String, String>> list) {
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/UPDATE_ONLINE_STATUS_TIME");
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ContentValues addCvs;
        try {
            if (list != null && list.size() > 0) {
                for (Map<String, String> temp : list) {
                    if (StringUtil.isEmpty(temp.get("phone")))
                        continue;
                    addCvs = new ContentValues();
                    // if (("" + OutCallUtil.presence_offline).equals(temp
                    //     .get("status"))) {
                    //     // 离线
                    //     addCvs.put("isOnline", 0);
                    //     addCvs.put("extraInfo", "");
                    // } else {
                    //     // 在线
                    //     addCvs.put("isOnline", 1);
                    //     addCvs.put("extraInfo", temp.get("extra"));
                    // }
                    ops.add(ContentProviderOperation
                        .newUpdate(uri)
                        .withValues(addCvs)
                        .withSelection(
                            " isMutualTrust in (0,1) and nubeNumber = ? ",
                            new String[] { temp.get("phone") }).build());

                }
            }
            if (ops.size() > 0) {
                getContentResolver()
                    .applyBatch(ProviderConstant.AUTHORITY, ops);
                LogUtil.d("根据号码更新好友在线状态   success");
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("根据号码更新好友在线状态   Exception", e);
        }
        return false;
    }

    /*
	 * (non-Javadoc) 批量插入联系人到应用数据库[只针对加为好友场景]
	 *
	 * @see
	 * com.channelsoft.netphone.dao.NetPhoneDao#batchInsertContacts(java.util
	 * .List)
	 */
    @Override
    public boolean batchInsertContacts(List<ContactFriendBean> list) {
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/INSERT_LINKMAN_ITEM");
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ContentValues values;
        ContactFriendBean info;
        if (list != null && list.size() > 0) {
            Iterator<ContactFriendBean> it = list.iterator();
            int maxSort = getMaxSortkey();
            while (it.hasNext()) {
                info = it.next();
                values = new ContentValues();
                if (StringUtil.isEmpty(info.getContactId())) {
                    info.setContactId(CommonUtil.getUUID());
                }
                values.put("contactId", info.getContactId());
                values.put("name", info.getName());
                values.put("nickname", info.getNickname());
                values.put("firstName", info.getFirstName());
                values.put("lastName", info.getLastName());
                values.put("isDeleted", 0);
                values.put("isOnline", 0);
                values.put("number", info.getNumber());
                values.put("isMutualTrust", 3);
                values.put("nubeNumber", info.getNubeNumber());
                values.put("syncStat", 0);
                values.put("sortKey", ++maxSort);
                ops.add(ContentProviderOperation.newInsert(uri)
                    .withValues(values).build());
            }
        }
        try {
            if (ops.size() > 0) {
                getContentResolver()
                    .applyBatch(ProviderConstant.AUTHORITY, ops);
                LogUtil.d("批量插入联系人到应用数据库   success");
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("批量插入联系人到应用数据库  Exception");
        }
        return false;
    }

    @Override
    public boolean batchInsertContactsForVcard(List<ContactFriendBean> list) {
        // 插入视频好友表和新朋友表
        Uri uriVidieo = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/INSERT_LINKMAN_ITEM");
        Uri uriNewFriend = ProviderConstant.NETPHONE_NEWFRIEND_URI;
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ContentValues values;
        ContactFriendBean info;
        NetPhoneDaoImpl netDaoImpl = new NetPhoneDaoImpl(
            getApplicationContext());
        NewFriendDao newFriendDao = new NewFriendDao(getApplicationContext());
        String temp[] = null;
        Map<String, String> map = null;
        if (list != null && list.size() > 0) {
            Iterator<ContactFriendBean> it = list.iterator();
            map = new HashMap<String, String>();
            while (it.hasNext()) {
                info = it.next();
                values = new ContentValues();
                if (StringUtil.isEmpty(info.getContactId())) {
                    info.setContactId(CommonUtil.getUUID());
                }
                if (!StringUtil.isEmpty(info.getNubeNumber())) {
                    // 名片分享 插入视频好友表 格式:poItem.getNubeNumber()+ "_" +
                    // poItem.getSourcesId()
                    if (!StringUtil.isEmpty(map.get(info.getNubeNumber()))) {
                        continue;
                    } else {
                        map.put(info.getNubeNumber(), info.getNubeNumber());
                    }
                    temp = info.getNubeNumber().split("\\_");
                    values.put("nubeNumber", temp[0]);
                    values.put("contactUserId", temp[1]);
                }
                // 根据视频好查询 视频好友表和新朋友表 如果已经存在就不在插入 解决自己和自己分享名片的情况
                values.put("contactId", info.getContactId());
                values.put("name", info.getName());
                values.put("nickname", info.getNickname());
                values.put("number", info.getNumber());
                values.put("isMutualTrust", 5);
                values.put("isDeleted", 0);
                values.put("headUrl", info.getHeadUrl());
                if (temp == null) {
                    continue;
                }
                if (!StringUtil.isEmpty(temp[0])
                    && !netDaoImpl.queryVideoFriendByNubePhone(temp[0])) {
                    // 视频好友表不存在该视频联系人，自己对自己
                    ops.add(ContentProviderOperation.newInsert(uriVidieo)
                        .withValues(values).build());
                }
                if (!StringUtil.isEmpty(temp[0])
                    && newFriendDao.queryNewFriendByNubeNumber(temp[0])
                    && !netDaoImpl.queryVideoFriendByNubePhone(temp[0])) {
                    // 插入新朋友表时：判断一下 新朋友表和视频好友表都没有该视频号码时才插入新朋友表
                    ops.add(ContentProviderOperation.newInsert(uriNewFriend)
                        .withValues(getNewFriendsValues(temp, info))
                        .build());
                }
                temp = null;
            }
            map.clear();
            map = null;
        }
        try {
            if (ops.size() > 0) {
                getContentResolver()
                    .applyBatch(ProviderConstant.AUTHORITY, ops);
                LogUtil.d("发送名片 ，批量插入联系人到应用数据库   success");
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("发送名片 ，批量插入联系人到应用数据库  Exception");
        }
        return false;
    }

    public boolean batchInsertContactsForVcards(List<ContactFriendBean> list,
                                                String excludingNumber) {
        LogUtil.begin("list size:" + list.size() + "|excludingNumber:"
            + excludingNumber);
        // 插入视频好友表和新朋友表
        Uri uriVidieo = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/INSERT_LINKMAN_ITEM");
        Uri uriNewFriend = ProviderConstant.NETPHONE_NEWFRIEND_URI;
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ContentValues values;
        ContactFriendBean info;
        NetPhoneDaoImpl netDaoImpl = new NetPhoneDaoImpl(
            getApplicationContext());
        NewFriendDao newFriendDao = new NewFriendDao(getApplicationContext());
        List<String> videoList = null;
        List<String> headUrlList = null;
        List<String> nickNameList = null;
        List<ContactFriendBean.PhoneInfo> listPhones = null;
        String temp[] = null;
        Map<String, String> map = null;
        try {
            if (list != null && list.size() > 0) {
                Iterator<ContactFriendBean> it = list.iterator();
                map = new HashMap<String, String>();
                while (it.hasNext()) {

                    info = it.next();
                    videoList = info.getVideoList();
                    headUrlList = info.getHeadUrlList();
                    nickNameList = info.getNickNameList();
                    listPhones = info.getPhoneList();

                    if (videoList == null || videoList.size() == 0) {
                        continue;
                    }
                    for (int i = 0; i < videoList.size(); i++) {
                        String videoItem = videoList.get(i);
                        values = new ContentValues();
                        if (!StringUtil.isEmpty(videoItem)) {
                            // 名片分享 插入视频好友表 格式:poItem.getNubeNumber()+ "_" +
                            // poItem.getSourcesId()
                            if (!StringUtil.isEmpty(map.get(videoItem))) {
                                continue;
                            } else {
                                map.put(videoItem, videoItem);
                            }
                            temp = videoItem.split("\\_");
                            if (temp == null) {
                                continue;
                            }
                            if (temp.length == 2) {
                                // 排除一些特殊的号码，一般情况下是自身的视频号
                                if (!TextUtils.isEmpty(excludingNumber)
                                    && excludingNumber.equals(temp[0])) {
                                    continue;
                                }
                                values.put("nubeNumber", temp[0]);
                                values.put("contactUserId", temp[1]);
                            } else {
                                continue;
                            }
                            if (!StringUtil.isEmpty(temp[0])
                                && !netDaoImpl
                                .queryVideoFriendByNubePhone(temp[0])) {
                                // 视频好友表不存在该视频联系人
                                values.put("isOnline", 0);
                                values.put("isMutualTrust", 5);
                                // 根据视频好查询 视频好友表和新朋友表 如果已经存在就不在插入 解决自己和自己分享名片的情况
                                values.put("contactId", CommonUtil.getUUID());
                                values.put("name", info.getName());
                                if (nickNameList != null
                                    && i < nickNameList.size()) {
                                    values.put("nickname", nickNameList.get(i));
                                }
                                if (listPhones != null && i < listPhones.size()) {
                                    values.put("number",
                                        listPhones.get(i).number);
                                }
                                values.put("number", listPhones.get(i).number);
                                values.put("isDeleted", 0);
                                if (headUrlList != null
                                    && i < headUrlList.size()) {
                                    values.put("headUrl", headUrlList.get(i));
                                }
                                ops.add(ContentProviderOperation
                                    .newInsert(uriVidieo)
                                    .withValues(values).build());
                            }
                            if (!StringUtil.isEmpty(temp[0])
                                && newFriendDao
                                .queryNewFriendByNubeNumber(temp[0])
                                && !netDaoImpl
                                .queryVideoFriendByNubePhone(temp[0])) {
                                // 插入新朋友表时：判断一下 新朋友表和视频好友表都没有该视频号码时才插入新朋友表
                                values.clear();
                                values.put(NewFriendTable.NEWFRIEND_COLUMN_ID,
                                    CommonUtil.getUUID());
                                values.put(
                                    NewFriendTable.NEWFRIEND_COLUMN_LASTTIME,
                                    DateUtil.getDBOperateTime());
                                values.put(
                                    NewFriendTable.NEWFRIEND_COLUMN_NAME,
                                    info.getName());
                                if (listPhones != null && i < listPhones.size()) {
                                    values.put(
                                        NewFriendTable.NEWFRIEND_COLUMN_NUMBER,
                                        listPhones.get(i).number);
                                }
                                values.put(
                                    NewFriendTable.NEWFRIEND_COLUMN_NUBENUMBER,
                                    temp[0]);
                                values.put(
                                    NewFriendTable.NEWFRIEND_COLUMN_STATUS,
                                    5);
                                values.put(
                                    NewFriendTable.NEWFRIEND_COLUMN_ISNEW,
                                    1);
                                values.put(
                                    NewFriendTable.NEWFRIEND_COLUMN_VISIBLE,
                                    0);
                                values.put(
                                    NewFriendTable.NEWFRIEND_COLUMN_CONTACTUSERID,
                                    temp[1]);

                                if (headUrlList != null
                                    && i < headUrlList.size()) {
                                    values.put(
                                        NewFriendTable.NEWFRIEND_COLUMN_HEADURL,
                                        headUrlList.get(i));
                                }

                                if (nickNameList != null
                                    && i < nickNameList.size()) {
                                    values.put(
                                        NewFriendTable.NEWFRIEND_COLUMN_REALNAME,
                                        nickNameList.get(i));
                                }

                                ops.add(ContentProviderOperation
                                    .newInsert(uriNewFriend)
                                    .withValues(values).build());
                            }
                            temp = null;
                            values = null;
                        }
                    }
                }
                map.clear();
                map = null;
            }

            if (ops.size() > 0) {
                getContentResolver()
                    .applyBatch(ProviderConstant.AUTHORITY, ops);
                LogUtil.d("发送名片 ，批量插入联系人到应用数据库   success");
                return true;
            }

        } catch (Exception e) {
            LogUtil.e("发送名片 ，批量插入联系人到应用数据库  Exception", e);
        }
        return false;
    }

    private ContentValues getNewFriendsValues(String[] temp,
                                              ContactFriendBean info) {
        ContentValues values = new ContentValues();
        // 插入新朋友表
        values.put(NewFriendTable.NEWFRIEND_COLUMN_ID, CommonUtil.getUUID());
        values.put(NewFriendTable.NEWFRIEND_COLUMN_LASTTIME,
            DateUtil.getDBOperateTime());
        values.put(NewFriendTable.NEWFRIEND_COLUMN_NAME, info.getNickname());
        values.put(NewFriendTable.NEWFRIEND_COLUMN_NUMBER, info.getNumber());
        values.put(NewFriendTable.NEWFRIEND_COLUMN_NUBENUMBER, temp[0]);
        values.put(NewFriendTable.NEWFRIEND_COLUMN_STATUS, 5);
        values.put(NewFriendTable.NEWFRIEND_COLUMN_ISNEW, 1);
        values.put(NewFriendTable.NEWFRIEND_COLUMN_VISIBLE, 0);
        values.put(NewFriendTable.NEWFRIEND_COLUMN_CONTACTUSERID, temp[1]);
        values.put(NewFriendTable.NEWFRIEND_COLUMN_HEADURL, info.getHeadUrl());
        values.put(NewFriendTable.NEWFRIEND_COLUMN_REALNAME, info.getName());
        return values;
    }

    /*
	 * (non-Javadoc) 批量更新好友状态
	 *
	 * @see com.channelsoft.netphone.dao.NetPhoneDao#batchUpdateOnlineStatus()
	 */
    @Override
    public void batchUpdateOnlineStatus() {
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/BATCH_UPDATE_ONLINE_ITEM");
        try {
            ContentValues values = new ContentValues();
            getContentResolver().update(uri, values, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("批量更新好友状态     Exception");
        }
        return;
    }

    /*
	 * (non-Javadoc) 根据号码匹配联系人姓名
	 *
	 * @see com.channelsoft.netphone.dao.NetPhoneDao#matchNameByNumber()
	 */
    @Override
    public String matchNameByNumber(String number) {
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/MATCH_NAME_BYNUMBER_ITEM");
        Cursor cursor;
        String name = "";
        try {
            cursor = getContentResolver().query(
                uri,
                new String[] { "name" },
                "number='" + number
                    + "' and isDeleted=0 and isMutualTrust in (0,1) ",
                null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                name = cursor.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(" 根据号码匹配联系人姓名  Exception");
        }
        return name;
    }

    @Override
    public void deleteNubeContactById(String contactId) {
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/DEL_NUBE_CONTACT_BY_ID");
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ContentValues values;
        values = new ContentValues();
        values.put("isDeleted", 1);
        values.put("syncStat", 0);
        ops.add(ContentProviderOperation
            .newUpdate(uri)
            .withValues(values)
            .withSelection(NubeFriendColumn.CONTACTID + " =? ",
                new String[] { contactId }).build());
        try {
            if (ops.size() > 0) {
                getContentResolver()
                    .applyBatch(ProviderConstant.AUTHORITY, ops);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("deleteNubeContactById  Exception" + e.getMessage());
        }
    }

    /*
	 * (non-Javadoc) 查询本地号码
	 *
	 * @see com.channelsoft.netphone.dao.NetPhoneDao#queryAppNumber()
	 */
    @Override
    public List<String> queryAppNumber() {
        List<String> list = new ArrayList<String>();
        Cursor appCursor = null;
        Uri queryApp = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/QUERY_APP_LINKMAN_NUMBER");
        try {
            appCursor = getContentResolver().query(queryApp, null, null, null,
                null);
            if (appCursor != null)
                LogUtil.d("app数据量：" + appCursor.getCount());
            if (appCursor != null && appCursor.getCount() > 0) {
                while (appCursor.moveToNext()) {
                    list.add(appCursor.getString(0));
                }
            }
            LogUtil.d("查询本地号码   success");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("查询本地号码   faile");
        } finally {
            if (appCursor != null) {
                appCursor.close();
                appCursor = null;
            }
        }
        return list;
    }

    /**
     * @Title: upDateNubeContactNameById
     * @Description: 更新nube联系人姓名
     * @param contactId
     * @param name
     * @param nameType
     *            修改的名称类型：0 姓名 1 昵称
     * @return: void
     */
    public void upDateNubeContactNameById(String contactId, String name,
                                          int nameType) {
        LogUtil.d("更新nube联系人姓名");
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/UPDATE_CONTACT_INFO");
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ContentValues values;
        values = new ContentValues();
        if (nameType == 0) {
            values.put(NubeFriendColumn.NAME, name);
            if (!StringUtil.isEmpty(name)) {
                // String pym = CommonUtil.getPymByParams(name, "", "");
                // values.put(NubeFriendColumn.FULLPYM, pym);
            }
        } else {
            values.put(NubeFriendColumn.NICKNAME, name);
        }
        values.put(NubeFriendColumn.SYNCSTAT, 0);
        ops.add(ContentProviderOperation
            .newUpdate(uri)
            .withValues(values)
            .withSelection(NubeFriendColumn.CONTACTID + " =? ",
                new String[] { contactId }).build());
        try {
            if (ops.size() > 0) {
                getContentResolver()
                    .applyBatch(ProviderConstant.AUTHORITY, ops);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("deleteNubeContactById  Exception", e);
        }
    }

    /**
     * @Title: upDateNubeContactUserTypeById
     * @Description: 更新NUBE联系人用户类型
     * @param contactId
     * @param userType
     *            0：普通用户 1：❤型用户
     * @return: void
     */
    public void upDateNubeContactUserTypeById(String contactId, int userType) {
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/UPDATE_CONTACT_INFO");
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ContentValues values;
        values = new ContentValues();
        values.put(NubeFriendColumn.USERTYPE, userType);
        values.put(NubeFriendColumn.SYNCSTAT, 0);
        ops.add(ContentProviderOperation
            .newUpdate(uri)
            .withValues(values)
            .withSelection(NubeFriendColumn.CONTACTID + " =? ",
                new String[] { contactId }).build());
        try {
            if (ops.size() > 0) {
                getContentResolver()
                    .applyBatch(ProviderConstant.AUTHORITY, ops);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("deleteNubeContactById  Exception" + e.getMessage());
        }

    }

    public String getUserTypeByNumber(String contactId) {
        String result = "";
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/MATCH_NAME_BYNUMBER_ITEM");
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(
                uri,
                new String[] { "userType" },
                "contactId='" + contactId
                    + "' and isDeleted=0 and isMutualTrust in (0,1)",
                null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                result = CommonUtil.trackValue(cursor.getString(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(" 根据手机号码获取用户类型    Exception");
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return result;
    }

    /*
	 * (non-Javadoc) 根据号码修改认证状态
	 *
	 * @see
	 * com.channelsoft.netphone.dao.NetPhoneDao#updateAuthByNumber(java.lang
	 * .String, int)
	 */
    @Override
    public void updateAuthByNumber(String number, int authStatus) {
        Uri updateUri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/UPDATE_CONTACT_INFO");
        try {
            ContentValues values = new ContentValues();
            values.put("isMutualTrust", authStatus);
            getContentResolver()
                .update(updateUri,
                    values,
                    "number='" + number
                        + "' and isMutualTrust in (0,1) ", null);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("根据号码修改认证状态   faile");
        }
    }

    /*
	 * (non-Javadoc) 查询在线状态好友数量
	 *
	 * @see com.channelsoft.netphone.dao.NetPhoneDao#queryOnlineConunt()
	 */
    @Override
    public int queryOnlineConunt() {
        int count = 0;
        LogUtil.d("queryOnlineConunt  count:" + count);
        return count;
    }

    /*
	 * (non-Javadoc) 根据新好友记录id新增一条好友记录
	 *
	 * @see
	 * com.channelsoft.netphone.dao.NetPhoneDao#insertRecordByNewId(java.lang
	 * .String)
	 */
    @Override
    public boolean insertRecordByNewId(String newFriendId) {
        Uri queryUri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/QUERY_NEW_FRIEND_INFO");
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/INSERT_LINKMAN_ITEM");
        boolean mark = false;
        Cursor queryCursor;
        try {
            queryCursor = getContentResolver().query(
                queryUri,
                NewFriendDao.select_columns,
                NewFriendTable.NEWFRIEND_COLUMN_ID + " = '" + newFriendId
                    + "'", null, null);
            if (queryCursor != null && queryCursor.getCount() > 0) {
                queryCursor.moveToFirst();
                int maxSort = getMaxSortkey();
                ContentValues values = new ContentValues();
                values.put("contactId", CommonUtil.getUUID());
                // 通过拨打电话时，传入的值不需要作为备注名niuben
                // values.put("name", queryCursor.getString(9));
                values.put("nickName", queryCursor.getString(2));
                values.put("number", queryCursor.getString(3));
                values.put("nubeNumber", queryCursor.getString(4));
                values.put("contactUserId", queryCursor.getString(7));
                values.put("headUrl", queryCursor.getString(8));
                values.put("sex", queryCursor.getString(11));
                values.put("isDeleted", 0);
                values.put("isOnline", 0);
                values.put("isMutualTrust", 1);
                values.put("syncStat", 0);
                values.put("sortKey", ++maxSort);

                // String pym = CommonUtil.getPymByParams(
                //     queryCursor.getString(9), queryCursor.getString(2),
                //     queryCursor.getString(4));
                // values.put(NubeFriendColumn.FULLPYM, pym);

                getContentResolver().insert(uri, values);
                mark = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("根据新好友记录id新增一条好友记录  Exception");
        }
        return mark;
    }

    @Override
    public int queryNewFriendCount() {
        int count = -99;
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/QUERY_NEW_FRIEND_COUNT_INFO");
        Cursor cursor;
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("查询新朋友数量   faile");
        }
        LogUtil.d("queryNewFriendCount  count:" + count);
        return count;
    }

    @Override
    public boolean matchContactByNumber(String number) {
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/MATCH_CONTACT_BYNUMBER_ITEM");
        Cursor cursor = null;
        ;
        try {
            cursor = getContentResolver().query(
                uri,
                null,
                "number='" + number
                    + "' and isDeleted=0 and isMutualTrust in(0,1)",
                null, null);
            if (cursor != null && cursor.getCount() > 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(" 根据号码匹配联系人 Exception");
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return false;
    }

    @Override
    public String getNubeNumber(String number) {
        String result = "";
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/MATCH_NAME_BYNUMBER_ITEM");
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(
                uri,
                new String[] { "nubeNumber" },
                "number='" + number
                    + "' and isDeleted=0 and isMutualTrust in (0,1)",
                null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                result = CommonUtil.trackValue(cursor.getString(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(" 根据手机号码获取nubenumber  Exception");
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return result;
    }

    @Override
    public ContactFriendBean queryFriendInfoByPhone(String phone) {

        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/QUERY_LINKMAN_BY_PHONENUMBER/" + phone);
        Cursor cursor = null;
        ContactFriendBean bean = null;
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                bean = new ContactFriendBean();
                bean.setSourcesId(cursor.getString(0));// contactuserid
                bean.setNubeNumber(cursor.getString(1));
                bean.setNickname(cursor.getString(2));
                bean.setHeadUrl(cursor.getString(3));
                return bean;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("queryFriendInfo Exception");
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

        return null;

    }

    /**
     * add by liujc 2014-09-26 下午：10:06:30 判断该视讯号是不是本地好友
     */
    @Override
    public boolean queryVideoFriendByNubePhone(String nubeNumber) {
        NetPhoneDao dao = new NetPhoneDaoImpl(getBaseContext());
        Boolean ischeakBoolean = dao.matchContactBynubenumber(nubeNumber);
        return ischeakBoolean;
    }

    // @Override
    // public boolean queryVideoFriendByNubePhone(String nubeNumber) {
    // Cursor cursor = null;
    // try {
    // cursor = getContentResolver().query(
    // ProviderConstant.NETPHONE_NUBEFRIEND_URI,
    // null,
    // NubeFriendColumn.NUBENUMBER + "=?",
    // new String[] {nubeNumber,"5"}, null);
    // if (cursor != null && cursor.getCount() > 0) {
    // LogUtil.d("接收名片分享时，判断视频号存在");
    // return false;
    // }
    // } catch (Exception e) {
    // } finally {
    // if (cursor != null) {
    // cursor.close();
    // cursor = null;
    // }
    // }
    // return false;
    // }

    @Override
    public String getNumber(String nubenumber) {
        String result = "";
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/MATCH_NAME_BYNUMBER_ITEM");
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(
                uri,
                new String[] { "number" },
                "nubeNumber='" + nubenumber
                    + "' and isDeleted=0 and isMutualTrust in (0,1)",
                null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                result = CommonUtil.trackValue(cursor.getString(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(" 根据nubenumber获取手机号码  Exception");
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return result;
    }

    @Override
    public String getNameByNumber(String number) {
        String result = "";
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/MATCH_NAME_BYNUMBER_ITEM");
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(
                uri,
                new String[] { "name" },
                "number='" + number
                    + "' and isDeleted=0 and isMutualTrust in (0,1)",
                null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                result = CommonUtil.trackValue(cursor.getString(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(" 根据手机号码获取姓名    Exception");
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return result;
    }

    @Override
    public String getNameByNubenumber(String nubeNumber) {
        String result = "";
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/MATCH_NAME_BYNUMBER_ITEM");
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(
                uri,
                new String[] { "name" },
                "nubeNumber='" + nubeNumber
                    + "' and isDeleted=0 and isMutualTrust in (0,1)",
                null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                result = CommonUtil.trackValue(cursor.getString(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(" 根据nubeNumber获取姓名    Exception");
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return result;
    }

    @Override
    public String getExtraInfoByNubenumber(String nubeNumber) {
        String result = "";
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/MATCH_NAME_BYNUMBER_ITEM");
        Cursor cursor = null;
        try {
            cursor = getContentResolver()
                .query(uri,
                    new String[] { "extraInfo" },
                    "nubeNumber='"
                        + nubeNumber
                        + "' and isOnline=1 and isDeleted=0 and isMutualTrust in (0,1)",
                    null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                result = CommonUtil.trackValue(cursor.getString(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(" 根据nubeNumber获取在场状态    Exception");
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return result;
    }

    public String getExtraInfoByNubenumberNIsonline(String nubeNumber) {
        String result = "";
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/MATCH_NAME_BYNUMBER_ITEM");
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(
                uri,
                new String[] { "extraInfo" },
                "nubeNumber='" + nubeNumber
                    + "' and isDeleted=0 and isMutualTrust in (0,1)",
                null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                result = CommonUtil.trackValue(cursor.getString(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(" 根据nubeNumber获取在场状态    Exception");
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return result;
    }

    /*
	 * (非 Javadoc) <p>Title: getNicknameByNubenumber</p> <p>Description: </p>
	 *
	 * @param nubeNumber
	 *
	 * @return
	 *
	 * @see
	 * com.channelsoft.netphone.dao.NetPhoneDao#getNicknameByNubenumber(java
	 * .lang.String)
	 */
    @Override
    public String getNicknameByNubenumber(String nubeNumber) {
        String result = "";
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/MATCH_NAME_BYNUMBER_ITEM");
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(
                uri,
                new String[] { "nickname" },
                "nubeNumber='" + nubeNumber
                    + "' and isDeleted=0 and isMutualTrust in (0,1)",
                null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                result = CommonUtil.trackValue(cursor.getString(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(" 根据nubeNumber获取昵称    Exception");
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return result;
    }

    @Override
    public String getSexByNumber(String nubeNumber) {
        String result = "";
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/MATCH_NAME_BYNUMBER_ITEM");
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(
                uri,
                new String[] { "sex" },
                "nubeNumber='" + nubeNumber
                    + "' and isDeleted=0 and isMutualTrust in (0,1)",
                null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                result = CommonUtil.trackValue(cursor.getString(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(" 根据nubeNumber获取性别   Exception");
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return result;
    }

    /*
	 * (非 Javadoc) <p>Title: getNicknameNameByNumber</p> <p>Description: </p>
	 *
	 * @param number
	 *
	 * @return
	 *
	 * @see
	 * com.channelsoft.netphone.dao.NetPhoneDao#getNicknameNameByNumber(java
	 * .lang.String)
	 */
    @Override
    public String getNicknameByNumber(String number) {
        String result = "";
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/MATCH_NAME_BYNUMBER_ITEM");
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(
                uri,
                new String[] { "nickname" },
                "number='" + number
                    + "' and isDeleted=0 and isMutualTrust in (0,1)",
                null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                result = CommonUtil.trackValue(cursor.getString(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(" 根据手机号码获取姓名    Exception");
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return result;
    }

    public String getDisplaynameByNubenumber(String nubeNumber) {
        String result = "";
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/MATCH_NAME_BYNUMBER_ITEM");
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(
                uri,
                new String[] { "name", "nickname" },
                "nubeNumber='" + nubeNumber
                    + "' and isDeleted=0 and isMutualTrust in (0,1)",
                null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                result = CommonUtil.trackValue(cursor.getString(0));
                if (TextUtils.isEmpty(result)) {
                    result = CommonUtil.trackValue(cursor.getString(1));
                }
                if (TextUtils.isEmpty(result)) {
                    result = nubeNumber;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(" 根据nubeNumber获取昵称    Exception");
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        if (TextUtils.isEmpty(result)) {
            // 当cursor为null时，也可以有正常的数据返回--wxy
            result = nubeNumber;
        }
        return result;
    }

    @Override
    public boolean matchContactBynubenumber(String nubenumber) {
        boolean matched = false;
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/MATCH_CONTACT_BYNUBENUMBER_ITEM");
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(
                uri,
                null,
                "nubeNumber='" + nubenumber
                    + "' and isDeleted=0 and isMutualTrust in(0,1)",
                null, null);
            if (cursor != null && cursor.getCount() > 0) {
                matched = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(" 根据号码匹配联系人 Exception");
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return matched;
    }

    /*
	 * (non-Javadoc) 获取应用数据库数据
	 *
	 * @see com.channelsoft.netphone.dao.NetPhoneDao#getAppdataCount()
	 */
    @Override
    public int getAppdataCount() {
        int count = 0;
        Cursor cursor = null;
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/QUERY_APP_LINKMAN_COUNT");
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                count = cursor.getInt(0);
            }
            LogUtil.d("获取应用数据库数量  success");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("获取应用数据库数量  Exception");
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return count;
    }

    @Override
    public Cursor getAppContacts() {
        // select nubeNumber,contactId,name,nickname NubeFriendColumn.TABLENAME
        Cursor cursor = null;
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/QUERY_NUBE_FRIEND_INFO");
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            LogUtil.d("查询好友联系人  success");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("查询好友联系人  Exception", e);
            return null;
        }
        return cursor;
    }

    @Override
    public Cursor getVisibleAppContacts() {
        Cursor cursor = null;
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/QUERY_VISIBLE_NUBE_FRIEND_INFO");
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            LogUtil.d("查询可见的好友联系人");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("查询可见的好友联系人  Exception", e);
            return null;
        }
        return cursor;
    }

    @Override
    public List<String> checkUpOnlineLinkman(List<String> phoneList) {
        LogUtil.d("checkUpOnlineLinkman start");
        List<String> list = new ArrayList<String>();
        Cursor cursor = null;
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/MATCH_NAME_BYNUMBER_ITEM");
        try {
            StringBuilder builder = new StringBuilder("");
            if (phoneList != null && phoneList.size() > 0) {
                for (String phone : phoneList) {
                    builder.append(phone).append(",");
                }
            }
            String phone = builder.toString();
            if (!StringUtil.isEmpty(phone))
                phone = phone.substring(0, phone.length() - 1);

            cursor = getContentResolver()
                .query(uri,
                    new String[] { "nubeNumber" },
                    " nubeNumber in (?) and isDeleted=0 and isOnline=0 and isMutualTrust in(0,1)",
                    new String[] { phone }, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    list.add(cursor.getString(0));
                }
            }
            LogUtil.d("鉴别上线好友  success");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("鉴别上线好友  Exception");
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        LogUtil.d("checkUpOnlineLinkman list.size:" + list.size());
        return list;
    }

    @Override
    public void updateLinkmanStatus(String nubeNumber) {
        LogUtil.d("updateLinkmanStatus start");
        if (StringUtil.isEmpty(nubeNumber))
            return;
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/UPDATE_LINKMAN_STATUS_ITEM");
        try {
            ContentValues values = new ContentValues();
            values.put("isMutualTrust", 1);
            getContentResolver().update(
                uri,
                values,
                " nubeNumber='" + nubeNumber
                    + "' and isDeleted=0 and isMutualTrust=5", null);
            LogUtil.d("updateLinkmanStatus  success");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("updateLinkmanStatus  Exception");
        }
    }

    public void deleteLinkman(String phone) {
        LogUtil.d("deleteLinkman start");
        if (StringUtil.isEmpty(phone))
            return;
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/DELETE_LINKMAN_STATUS_ITEM");
        try {
            getContentResolver().delete(
                uri,
                " nubeNumber='" + phone
                    + "' and isDeleted=0 and isMutualTrust=5", null);
            LogUtil.d("deleteLinkman  success");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("deleteLinkman  Exception");
        }
    }

    @Override
    public ContactFriendBean queryFriendInfoByNube(String nubeNumber) {

        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/QUERY_LINKMAN_BY_NUBENUMBER/" + nubeNumber);
        Cursor cursor = null;
        ContactFriendBean bean = new ContactFriendBean();
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                bean.setContactId(CommonUtil.trackValue(cursor.getString(0)));
                bean.setName(CommonUtil.trackValue(cursor.getString(1)));
                bean.setNickname(CommonUtil.trackValue(cursor.getString(2)));
                bean.setFirstName(CommonUtil.trackValue(cursor.getString(3)));
                bean.setLastName(CommonUtil.trackValue(cursor.getString(4)));

                bean.setNumber(CommonUtil.trackValue(cursor.getString(8)));
                bean.setNubeNumber(CommonUtil.trackValue(cursor.getString(9)));
                bean.setIsMutualTrust(cursor.getInt(10));
                bean.setHeadUrl(CommonUtil.trackValue(cursor.getString(11)));
                bean.setLocalName(CommonUtil.trackValue(cursor.getString(12)));
                bean.setUid(CommonUtil.trackValue(cursor.getString(13)));
                bean.setSex(CommonUtil.trackValue(cursor.getString(14)));
                bean.setShowMoblie(TextUtils.isEmpty(CommonUtil
                    .trackValue(cursor.getString(15))) ? NubeFriendColumn.MOBILE_INVISIBLE
                                                       : CommonUtil.trackValue(cursor.getString(15)));
                return bean;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("queryFriendInfo Exception", e);
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return null;
    }

    /**
     * 判断nube号码是否为好友
     */
    public boolean isNubeFriend(String nubeNumber) {
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/QUERY_LINKMAN_BY_NUBENUMBER/" + nubeNumber);
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                return true;
            }
        } catch (Exception e) {
            LogUtil.e("isNubeFriend Exception", e);
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return false;
    }

    @Override
    public void clearNewFriendRecord() {
        Uri queryUri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/QUERY_NEW_FRIEND_INFO");
        Uri deleteUri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/DELETE_NEWFRIEND_TABLE");
        Uri updateUri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/DELETE_LINKMAN");
        Cursor queryCursor = null;
        List<String> markList = new ArrayList<String>();
        try {
            queryCursor = getContentResolver()
                .query(queryUri,
                    new String[] { NewFriendTable.NEWFRIEND_COLUMN_NUBENUMBER },
                    NewFriendTable.NEWFRIEND_COLUMN_STATUS + "=5",
                    null, null);
            if (queryCursor != null && queryCursor.getCount() > 0) {
                while (queryCursor.moveToNext()) {
                    markList.add(queryCursor.getString(0));
                }
            }

            if (markList.size() > 0) {
                ArrayList<ContentProviderOperation> opp = new ArrayList<ContentProviderOperation>();
                for (String nubeNumber : markList) {
                    if (TextUtils.isEmpty(nubeNumber))
                        continue;
                    opp.add(ContentProviderOperation
                        .newDelete(updateUri)
                        .withSelection(
                            NubeFriendColumn.NUBENUMBER + " = ? and "
                                + NubeFriendColumn.ISMUTUALTRUST
                                + "=5", new String[] { nubeNumber })
                        .build());
                }
                if (opp.size() > 0) {
                    getContentResolver().applyBatch(ProviderConstant.AUTHORITY,
                        opp);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("clearNewFriendRecord Exception:" + e.getMessage());
        } finally {
            getContentResolver().delete(deleteUri, null, null);
            if (queryCursor != null) {
                queryCursor.close();
                queryCursor = null;
            }
        }
        LogUtil.d("clearNewFriendRecord end");
    }

    /**
     * @author: chuwx
     * @Title: queryContactRecord
     * @return
     * @date 2013-12-26 下午4:50:37
     */
    @Override
    public Cursor queryContactRecord(String nubenumber) {
        Cursor cursor = null;
        if (TextUtils.isEmpty(nubenumber))
            return cursor;
        Uri queryUri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/QUERY_CONTACT_RECORD/" + nubenumber);
        try {
            cursor = getContentResolver().query(queryUri, null, null, null,
                null);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("queryContactRecord Exception:" + e.getMessage());
        }
        return cursor;
    }

    /**
     * @author: chuwx
     * @Title: checkContactRecord
     * @return
     * @date 2013-12-26 下午5:08:51
     */
    @Override
    public boolean checkContactRecord(String nubenumber) {
        boolean mark = false;
        if (TextUtils.isEmpty(nubenumber))
            return mark;
        Uri queryUri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/CHECK_CONTACT_RECORD/" + nubenumber);
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(queryUri, null, null, null,
                null);
            if (cursor != null && cursor.getCount() > 0) {
                mark = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("checkContactRecord Exception:" + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return mark;
    }

    @Override
    public boolean checkContactIsonline(String nubenumber) {
        boolean mark = false;
        if (TextUtils.isEmpty(nubenumber))
            return mark;
        Uri queryUri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/CHECK_CONTACT_ISONLINE/" + nubenumber);
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(queryUri, null, null, null,
                null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                mark = 1 == cursor.getInt(0) ? true : false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("checkContactIsonline Exception:" + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return mark;
    }

    /**
     * @author: majj
     * @Title: getCallLogContactData
     * @Description:获取应用联系人数据，用于显示在通话记录表
     * @return
     * @date 2013-8-14 下午5:12:25
     */
    @Override
    public CopyOnWriteArrayList<CallRecordBean> getCallLogContactData() {
        CopyOnWriteArrayList<CallRecordBean> list = new CopyOnWriteArrayList<CallRecordBean>();
        CallRecordBean info;
        Cursor cursor = null;
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/GET_APP_LINKMAN_DATA");
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    info = new CallRecordBean();
                    String name = CommonUtil.trackValue(cursor.getString(1));
                    String nickname = CommonUtil
                        .trackValue(cursor.getString(3));
                    String mobile = CommonUtil.trackValue(cursor.getString(2));
                    String nube = CommonUtil.trackValue(cursor.getString(5));
                    info.setName(ShowNameUtil.getShowName(ShowNameUtil
                        .getNameElement(name, nickname, mobile, nube)));
                    info.setNumber(mobile);
                    info.setHeadUrl(CommonUtil.trackValue(cursor.getString(4)));
                    info.setNubeNumber(nube);
                    info.setSex(CommonUtil.trackValue(cursor.getString(7)));
                    // info.setCallType(String.valueOf(OutCallUtil.CT_SIP_AV));
                    // info.setDataType(DialpadFragment.TYPE_CALLLOG_DATA_CONTACT);
                    list.add(info);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("获取应用联系人   Exception");
            list = null;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return list;
    }

    /**
     * @author: chuwx
     * @Title: doUpdateNewfriendRecord
     * @date 2014-2-23 下午2:55:24
     */
    @Override
    public void doUpdateNewfriendRecord() {
        LogUtil.d("doUpdateNewfriendRecord START");
        Uri queryUri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/QUERY_NUBE_FRIEND_RECORD");
        Uri queryRecordUri = Uri.parse("content://"
            + ProviderConstant.AUTHORITY + "/QUERY_NEW_FRIEND_RECORD");
        Uri deleteUri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/DELETE_LINKMAN_STATUS_ITEM");
        Cursor cursor = null;
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        try {
            /** 清理视频表与新朋友表重复记录 处理 */
            cursor = getContentResolver().query(queryUri, null, null, null,
                null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    // 删除新朋友对应记录
                    ops.add(ContentProviderOperation
                        .newDelete(ProviderConstant.NETPHONE_NEWFRIEND_URI)
                        .withSelection(
                            NewFriendTable.NEWFRIEND_COLUMN_ID + "=?",
                            new String[] { cursor.getString(1) })
                        .build());
                    LogUtil.d("清理视频表与新朋友表重复记录 处理");
                    // 删除视频表对应记录【针对导入是成对出现的不可见记录】
                    ops.add(ContentProviderOperation
                        .newDelete(deleteUri)
                        .withSelection(
                            NubeFriendColumn.NUBENUMBER + "=? and "
                                + NubeFriendColumn.ISMUTUALTRUST
                                + "=5 and "
                                + NubeFriendColumn.SYNCSTAT + "=0",
                            new String[] { cursor.getString(0) })
                        .build());
                }
            }
            if (ops.size() > 0) {
                getContentResolver()
                    .applyBatch(ProviderConstant.AUTHORITY, ops);
                ops.clear();
            }
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }

            /** 重置新朋友不可见记录为可见》》》》》》》 visible = 0 */
            cursor = getContentResolver().query(queryRecordUri, null, null,
                null, null);
            if (cursor != null && cursor.getCount() > 0) {
                ContentValues values = new ContentValues();
                values.put(NewFriendTable.NEWFRIEND_COLUMN_VISIBLE, 0);
                while (cursor.moveToNext()) {
                    ops.add(ContentProviderOperation
                        .newUpdate(ProviderConstant.NETPHONE_NEWFRIEND_URI)
                        .withValues(values)
                        .withSelection(
                            NewFriendTable.NEWFRIEND_COLUMN_ID + "=?",
                            new String[] { cursor.getString(0) })
                        .build());
                }
            }
            if (ops.size() > 0) {
                getContentResolver()
                    .applyBatch(ProviderConstant.AUTHORITY, ops);
                ops.clear();
            }

        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("doUpdateNewfriendRecord Exception：" + e.getMessage(), e);
            return;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        LogUtil.d("doUpdateNewfriendRecord SUCCESS");
    }

    // *****************add by wangyf start *****************
    // ISMUTUALTRUST = 1 可见 ISMUTUALTRUST= 5 不可见
    public boolean getNubeFriend(String nubeNumber, String isMutualtrust,
                                 String isMutualtrustTwo) {
        Uri queryUri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/MATCH_NAME_BYNUMBER_ITEM");
        Cursor cursor = null;
        try {
            cursor = getContentResolver()
                .query(queryUri,
                    null,
                    NubeFriendColumn.NUBENUMBER + "= ? and "
                        + NubeFriendColumn.ISDELETED + "=0 and "
                        + NubeFriendColumn.ISMUTUALTRUST
                        + " in (?,?) ",
                    new String[] { nubeNumber, isMutualtrust,
                        isMutualtrustTwo }, null);
            if (cursor != null && cursor.getCount() > 0) {
                return true;
            }
        } catch (Exception e) {
            LogUtil.e("Exception", e);
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return false;
    }

    @Override
    public boolean updateNubeNumberStatus(String nubeNumber) {
        LogUtil.d("updateLinkmanStatus start");
        if (StringUtil.isEmpty(nubeNumber))
            return false;
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/UPDATE_LINKMAN_STATUS_ITEM");
        try {
            ContentValues values = new ContentValues();
            values.put("isMutualTrust", 1);
            values.put("SYNCSTAT", 0);
            getContentResolver().update(
                uri,
                values,
                " nubeNumber='" + nubeNumber
                    + "' and isDeleted=0 and isMutualTrust=?",
                new String[] { "5" });
            LogUtil.d("updateLinkmanStatus  success");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("updateLinkmanStatus  Exception");
            return false;
        }
        return true;
    }

    // ****************add by wangyf start
    // 将扫码获取到的TV视讯号与本地视讯好友进行查找*****************
	/*
	 * 先查询本地是否有此视频号码对应的可见的联系人， 若有，则不做处理；若无，则查询本地是否有此视频号码对应的不可见的联系人，
	 * 若有，则将此不可见的联系人状态改为可见；若无，新增一条只有视频号码的联系人
	 */
    @Override
    public int insertContactIfNotExist(String tvNubeNumber) {
        // 首先查询联系人列表里状态为可见的好友
        if (getNubeFriend(tvNubeNumber, "0", "1")) {
            // 已经存在联系人
            return 1;
        } else {
            NewFriendDao newFriendDao = new NewFriendDao(
                getApplicationContext());
            NewFriendBean friendBean = newFriendDao
                .getNewFriendByNubeNumber(tvNubeNumber);
            // 不存在联系人，查询联系人列表里状态为不可见的好友
            // if (getNubeFriend(tvNubeNumber, "5", "5")) {
            if (friendBean != null) {
                // 将好友的不可见状态变为可见状态
                updateNubeNumberStatus(tvNubeNumber);
                return 2;
            } else {
                // 将此联系人加入到联系人好友里
                ContactFriendBean info = new ContactFriendBean();
                info.setNubeNumber(tvNubeNumber);
                insertLinkman(info);
                return 3;
            }
        }
    }

    // *****************add by wangyf end *****************

    /**
     * @author: chuwx
     * @Title: doUpdatePym
     * @Description: TODO
     * @date 2014-6-6 下午3:57:20
     */
    @Override
    public void doUpdatePym() {
        Cursor cursor = null;
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/QUERY_NUBE_FRIEND_INFO");
        Uri updateUri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/UPDATE_CONTACT_INFO");

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            ContentValues values;
            while (cursor.moveToNext()) {
                values = new ContentValues();
                // String pym = CommonUtil.getPymByParams(cursor.getString(2),
                //     cursor.getString(3), cursor.getString(0));
                // values.put(NubeFriendColumn.FULLPYM, pym);

                ops.add(ContentProviderOperation
                    .newUpdate(updateUri)
                    .withValues(values)
                    .withSelection(NubeFriendColumn.CONTACTID + " =? ",
                        new String[] { cursor.getString(1) }).build());
            }
            if (ops.size() > 0) {
                getContentResolver()
                    .applyBatch(ProviderConstant.AUTHORITY, ops);
                ops.clear();
            }

            LogUtil.d("更新拼音字段doUpdatePym success");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("更新拼音字段doUpdatePym  Exception", e);
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

    @Override
    public Map<String, String[]> getHeadUrlByNubenumber(
        List<String> nubeNumberList) {
        LogUtil.d("getHeadUrlByNubenumber start");
        String tempnubeNumber = "";
        Map<String, String[]> map = new HashMap<String, String[]>();
        Cursor cursor = null;
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/MATCH_NAME_BYNUMBER_ITEM");
        try {
            StringBuilder builder = new StringBuilder("");
            if (nubeNumberList != null && nubeNumberList.size() > 0) {
                for (String nubeNumber : nubeNumberList) {
                    builder.append(nubeNumber).append(",");
                }
            }
            String nubenumber = builder.toString();
            if (!StringUtil.isEmpty(nubenumber))
                nubenumber = nubenumber.substring(0, nubenumber.length() - 1);

            cursor = getContentResolver().query(
                uri,
                new String[] { NubeFriendColumn.NUBENUMBER,
                    NubeFriendColumn.NAME, NubeFriendColumn.NICKNAME,
                    NubeFriendColumn.HEADURL, NubeFriendColumn.SEX,
                    NubeFriendColumn.NUMBER },
                NubeFriendColumn.NUBENUMBER + " in (" + nubenumber
                    + ")  and isDeleted=0 and isMutualTrust in(0,1)",
                null, null);
            if (cursor != null && cursor.getCount() > 0) {
                String[] item = null;
                while (cursor.moveToNext()) {
                    tempnubeNumber = CommonUtil.trackValue(cursor.getString(0));
                    item = new String[] {
                        CommonUtil.trackValue(cursor.getString(0)),
                        CommonUtil.trackValue(cursor.getString(1)),
                        CommonUtil.trackValue(cursor.getString(2)),
                        CommonUtil.trackValue(cursor.getString(3)),
                        CommonUtil.trackValue(cursor.getString(4)),
                        CommonUtil.trackValue(cursor.getString(5)) };
                    map.put(tempnubeNumber, item);
                }
            }
            LogUtil.d("查询联系人头像  success");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("查询联系人头像  Exception");
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        LogUtil.d("getHeadUrlByNubenumber list.size:" + map.size());
        return map;
    }

    /**
     * @author: liujc
     * @Title: updateLinkmanByContactBean
     * @Description:根据id更新好友信息
     * @param nubeNumber
     * @param bean
     * @date 2014-11-19 下午9:40:56
     */
    @Override
    public void updateLinkmanByNubenumber(String nubeNumber,
                                          ContactFriendBean bean) {
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/UPDATE_LINKMAN_ITEM");
        if (bean != null) {
            ContentValues values = new ContentValues();
            LogUtil.d("insertLinkmanByContactBean 下载数据插入一条联系人号码："
                + bean.getName());
            values.put("name", bean.getName());
            values.put("nickname", bean.getNickname());
            values.put("number", bean.getNumber());
            values.put("headUrl", bean.getHeadUrl());
            values.put("contactUserId", bean.getUid());
            values.put("syncStat", 0);
            try {
                getContentResolver().update(uri, values,
                    "nubeNumber='" + nubeNumber + "'", null);
                LogUtil.d("根据nubeNumber更新好友信息  success");
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.d("根据nubeNumber更新好友信息  Exception");
            }
        }
    }

    /**
     * @author: liujc
     * @Title: getLoacalFindNewFriendsBean
     * @Description:获取好友表中状态为5的联系人，即被本地发现的联系人
     * @return
     * @date 2015-3-12上午11:41
     */
    @Override
    public List<ContactFriendBean> getLoacalFindNewFriendsBean() {

        List<ContactFriendBean> list = new ArrayList<ContactFriendBean>();
        ContactFriendBean info;
        Cursor cursor = null;
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/GET_APP_LINKMAN_LOCAL_FIND_DATA_NEW");
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
                    info.setSourcesId(CommonUtil.trackValue(cursor.getString(9)));
                    info.setPym(CommonUtil.trackValue(cursor.getString(7)));
                    info.setSex(CommonUtil.trackValue(cursor.getString(8)));
                    list.add(info);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("获取应用联系人   Exception");
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
    public String getContactNameByNumber(String phoneNumber) {
        Uri uri = Uri
            .parse("content://com.android.contacts/data/phones/filter/"
                + phoneNumber);
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver
            .query(uri,
                new String[] { android.provider.ContactsContract.Data.DISPLAY_NAME },
                null, null, null);
        if (cursor.moveToFirst()) {
            String name = cursor.getString(0);
            // name = CommonUtil.fliteIllegalChar(name);
            return name;
        }
        cursor.close();
        return phoneNumber;
    }

    /**
     * @author: liujc
     * @Title: getAppLinkmanNumberStatusData
     * @Description:获取应用联系人手机号码以及认证状态
     * @return
     * @date 2015-04-09 上午10:04
     */
    @Override
    public Map<String, String> getAppLinkmanNumberStatusData() {
        Map<String, String> map = new HashMap<String, String>();
        Cursor cursor = null;
        String tempPhoneNumber = null;
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/QUERY_APP_LINKMAN_NUMBER");
        try {
            // 查询：number、isMutualTrust、nubeNumber from nube表
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    tempPhoneNumber = CommonUtil
                        .trackValue(cursor.getString(0));
                    // 针对本地存在两条同一手机号场景，即一条为本地发现数据，一条为好友数据，此场景仅保存状态非5的数据
                    if (map.get(tempPhoneNumber) != null
                        && ("1".equals(map.get(tempPhoneNumber)) || "0"
                        .equals(map.get(tempPhoneNumber)))) {
                        continue;
                    }
                    map.put(tempPhoneNumber,
                        CommonUtil.trackValue(cursor.getString(1)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("获取应用联系人手机号码   Exception");
            map = null;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return map;
    }

    @Override
    public Map<String, ContactFriendBean> getAppLinkmanNubeNumberData() {
        Map<String, ContactFriendBean> map = new HashMap<String, ContactFriendBean>();
        Cursor cursor = null;
        String tempPhoneNumber = null;
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/QUERY_APP_LINKMAN_NUMBER");
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    tempPhoneNumber = CommonUtil
                        .trackValue(cursor.getString(0));
                    // 针对本地存在两条同一手机号场景，即一条为本地发现数据，一条为好友数据，此场景仅保存状态非5的数据
                    if (map.get(tempPhoneNumber) != null
                        && ("1".equals(map.get(tempPhoneNumber)
                        .getIsMutualTrust()) || "0".equals(map.get(
                        tempPhoneNumber).getIsMutualTrust()))) {
                        continue;
                    }
                    map.put(tempPhoneNumber, getLoacalContactBean(CommonUtil
                        .trackValue(cursor.getString(2))));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("获取应用联系人手机号码以及详情   Exception");
            map = null;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return map;
    }

    @Override
    public ContactFriendBean getLoacalContactBean(String nubeNumber) {
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/GET_APP_LINKMAN_NEW/" + nubeNumber);
        Cursor cursor = null;
        ContactFriendBean bean = null;
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                bean = new ContactFriendBean();
                bean.setName(CommonUtil.trackValue(cursor.getString(0)));
                bean.setNickname(CommonUtil.trackValue(cursor.getString(2)));
                bean.setNumber(CommonUtil.trackValue(cursor.getString(1)));
                bean.setNubeNumber(CommonUtil.trackValue(cursor.getString(4)));
                bean.setUid(CommonUtil.trackValue(cursor.getString(5)));
                bean.setHeadUrl(CommonUtil.trackValue(cursor.getString(3)));
                bean.setSex(CommonUtil.trackValue(cursor.getString(7)));
                return bean;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("queryFriendInfo Exception");
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return bean;
    }

    /**
     * 成员nube和name的键值对
     *
     * @param nubeNumberList
     * @return
     */
    public Map<String, String> getLoacalNameByNubeList(
        List<String> nubeNumberList) {
        LogUtil.d("getLoacalNameByNubeList start");
        Map<String, String> map = new HashMap<String, String>();
        Cursor cursor = null;
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/MATCH_NAME_BYNUMBER_ITEM");
        try {
            StringBuilder builder = new StringBuilder("");
            if (nubeNumberList != null && nubeNumberList.size() > 0) {
                for (String nubeNumber : nubeNumberList) {
                    builder.append(nubeNumber).append(",");
                }
            }
            String nubenumber = builder.toString();
            if (!StringUtil.isEmpty(nubenumber)) {
                nubenumber = nubenumber.substring(0, nubenumber.length() - 1);
            }
            cursor = getContentResolver().query(
                uri,
                new String[] { NubeFriendColumn.NAME,
                    NubeFriendColumn.NICKNAME, NubeFriendColumn.NUMBER,
                    NubeFriendColumn.NUBENUMBER },
                NubeFriendColumn.NUBENUMBER + " in (" + nubenumber
                    + ")  and isDeleted=0 and isMutualTrust in(0,1)",
                null, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String name = CommonUtil.trackValue(cursor.getString(0));
                    String nick = CommonUtil.trackValue(cursor.getString(1));
                    String mobile = CommonUtil.trackValue(cursor.getString(2));
                    String nube = CommonUtil.trackValue(cursor.getString(3));
                    if (!TextUtils.isEmpty(nube)) {// 备注名->昵称->手机号->视讯号
                        map.put(nube,
                            TextUtils.isEmpty(name) ? (TextUtils
                                                           .isEmpty(nick) ? (TextUtils
                                                                                 .isEmpty(mobile) ? nube : mobile)
                                                                          : nick) : name);
                    }
                }
            }
            LogUtil.d("查询联系人名称  success");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("查询联系人名称  Exception");
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

        // 将自己的数据也插入进去
        // String nube = MedicalApplication.getPreference().getKeyValue(
        //     PrefType.LOGIN_NUBENUMBER, "");
        // String nick = MedicalApplication.getPreference().getKeyValue(
        //     PrefType.USER_NICKNAME, "");
        // String mobile = MedicalApplication.getPreference().getKeyValue(
        //     PrefType.LOGIN_MOBILE, "");
        // if (!map.containsKey(nube)) {// 昵称->手机号->视讯号
        //     map.put(nube,
        //         TextUtils.isEmpty(nick) ? (TextUtils.isEmpty(mobile) ? nube
        //                                                              : mobile) : nick);
        // }
        LogUtil.d("getLoacalNameByNubeList list.size:" + map.size());
        return map;
    }

    @Override
    public Map<String, ContactFriendBean> getNubeLoacalFindNewFriendsBean() {

        Map<String, ContactFriendBean> map = new HashMap<String, ContactFriendBean>();
        ContactFriendBean info;
        Cursor cursor = null;
        Uri uri = Uri.parse("content://" + ProviderConstant.AUTHORITY
            + "/GET_APP_LINKMAN_LOCAL_FIND_DATA_NEW");
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    info = new ContactFriendBean();
                    info.setName(CommonUtil.trackValue(cursor.getString(1)));
                    info.setNumber(CommonUtil.trackValue(cursor.getString(2)));
                    info.setNickname(CommonUtil.trackValue(cursor.getString(3)));
                    info.setHeadUrl(CommonUtil.trackValue(cursor.getString(4)));
                    info.setNubeNumber(CommonUtil.trackValue(cursor
                        .getString(5)));
                    info.setUid(CommonUtil.trackValue(cursor.getString(6)));
                    info.setSex(CommonUtil.trackValue(cursor.getString(8)));
                    map.put(CommonUtil.trackValue(cursor.getString(2)), info);
                    LogUtil.d("从纳贝表中取出状态为5的数据对应手机号="
                        + CommonUtil.trackValue(cursor.getString(2)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d("获取应用联系人   Exception");
            map = null;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return map;

    }
}
