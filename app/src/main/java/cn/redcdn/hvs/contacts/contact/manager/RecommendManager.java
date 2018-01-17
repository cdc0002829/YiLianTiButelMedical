package cn.redcdn.hvs.contacts.contact.manager;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.redcdn.datacenter.medicalcenter.MDSAppSearchUsers;
import cn.redcdn.datacenter.medicalcenter.data.MDSDetailInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.contacts.contact.StringHelper;
import cn.redcdn.hvs.contacts.contact.database.DBConf;
import cn.redcdn.hvs.contacts.contact.interfaces.Contact;
import cn.redcdn.hvs.contacts.contact.interfaces.ContactCallback;
import cn.redcdn.hvs.contacts.contact.interfaces.ResponseEntry;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.log.CustomLog;

public class RecommendManager {
  private static final String TAG = RecommendManager.class.getSimpleName();
  private HandlerThread dataHandlerThread;
  private Handler dataHandler;
  private List<List<String>> allList = new ArrayList<List<String>>();
  private static RecommendManager mInstance = null;
  private List<IRecommendListChanged> listChangedListener;
  private static Context mContext = null;
  private String fromWhere;
  private Map<String, Contact> recommendList = new HashMap<String, Contact>();
  private Map<String, String> nameList = new HashMap<String, String>();
  private int state = INIT_STATE;
  private static final int INIT_STATE = 0x00000301;
  private static final int DO_FILTER_DATA = 0x00000302;
  private static final int DO_SEARCH_DATA = 0x00000303;
  private static final int DO_COMPARA_DATA = 0x00000304;
  private static final int DO_STOP = 0x00000305;
  private static final int HAS_NEW = 0x00000306;
  private static final int GET_RECOMMEND_ING = 0x00000307;
  public static final String FROM_BOOT = "fromBoot";
  public static final String FROM_SYS = "fromSys";
  private int numCount = 0;
  private MDSAppSearchUsers searchUsers;
  private OnRecommendtLoadedListener mOnRecommendtLoadedListener = null;
  private ContactObserver observer;
  // 启动是否调用过获取取推荐列表
  public boolean isHasGetList = false;
  private Handler mainHandler = new Handler() {
    @SuppressWarnings("unchecked")
    public void handleMessage(Message msg) {
      if (state == DO_STOP) {
        state = INIT_STATE;
        mainHandler.removeCallbacksAndMessages(null);
        dataHandler.removeCallbacksAndMessages(null);
      } else {
        switch (msg.what) {
        case DO_FILTER_DATA:
          // 分批调接口
          allList = (List<List<String>>) msg.obj;
          doSearchContactList();
          break;
        case DO_SEARCH_DATA:
          newDoSearchAccountByMobile(1,(List<String>) msg.obj,fromWhere);
          break;
        case DO_COMPARA_DATA:
          doSearchContactList();
          break;
        case HAS_NEW:
          notifyListener((Integer) msg.obj);
          break;
        default:
          break;
        }
      }
      super.handleMessage(msg);
    }
  };

  public void setState(int state) {
    this.state = state;
  }

  // 返回时，最新变为普通推荐
  public void doChangeRecommendState() {
    for (String key : recommendList.keySet()) {
      if (recommendList.get(key).getBeRecommended() == 1) {
        recommendList.get(key).setBeRecommended(0);
      }
    }
  }

  private void doSearchContactList() {
    if (allList != null && !allList.isEmpty() && allList.get(0) != null) {
      Message msg = new Message();
      msg.obj = allList.get(0);
      msg.what = DO_SEARCH_DATA;
      msg.setTarget(mainHandler);
      msg.sendToTarget();
    } else {
      CustomLog.d(TAG, "doSearchContactList list .... " + recommendList.toString());
      state = INIT_STATE;
      Runnable runnable = new Runnable() {
        @Override
        public void run() {
          int num = getNewRecommentCount();
          Message msg = new Message();
          msg.obj = num;
          msg.what = HAS_NEW;
          msg.setTarget(mainHandler);
          msg.sendToTarget();
        }
      };
      dataHandler.post(runnable);
      if (mOnRecommendtLoadedListener != null && fromWhere.equals(FROM_SYS)) {
        mOnRecommendtLoadedListener.onRecommendLoaded(numCount);
      }
      CustomLog.d(TAG, "没有需要检测的的 ContactList ");
    }
  }




  private void newDoSearchAccountByMobile(final int searchType,List<String> list,final String fromWhere){

    searchUsers = new MDSAppSearchUsers(){
      @Override
      protected void onSuccess(List<MDSDetailInfo> responseContent) {
        CustomLog.d(TAG, "appSearchUsers onSuccess");
        if (allList != null && !allList.isEmpty() && allList.get(0) != null) {
          allList.remove(0);
        }
        if (responseContent != null && !responseContent.isEmpty()) {
          compareList(NewChangeBeans(responseContent), fromWhere);
        } else {
          Message msg = new Message();
          msg.what = DO_COMPARA_DATA;
          msg.setTarget(mainHandler);
          msg.sendToTarget();
        }
      }

      @Override
      protected void onFail(int statusCode, String statusInfo) {
        CustomLog.d(TAG, "appSearchUsers onFail"+"statusCode:"+statusCode+" statusInfo:"+statusInfo);
        if (allList != null && !allList.isEmpty() && allList.get(0) != null) {
          allList.remove(0);
        }
        Message msg = new Message();
        msg.what = DO_COMPARA_DATA;
        msg.setTarget(mainHandler);
        msg.sendToTarget();
      }

    };

    String[] array = new String[list.size()];
    searchUsers.appSearchUsers(AccountManager.getInstance(mContext).getToken(),searchType,list.toArray(array));
  }

  private List<Contact> NewChangeBeans(List<MDSDetailInfo> responseContent) {
    List<Contact> ccs = new ArrayList<Contact>();
    if (responseContent != null && !responseContent.isEmpty()) {
      Contact c = null;
      for (MDSDetailInfo u : responseContent) {
        c = new Contact();
        c.setNubeNumber(u.nubeNumber);
        c.setAppType("mobile");
        c.setNickname(u.nickName);
        c.setName(u.nickName);
        c.setPicUrl(u.headThumUrl);
        c.setHeadUrl(u.headThumUrl);
        c.setContactUserId(u.uid);
        c.setContactId(CommonUtil.getUUID());
        c.setUserType(1);
        c.setUserFrom(1);
        c.setRawContactId(nameList.get(u.account));

        c.setWorkUnit(u.workUnit);
        c.setWorkUnitType(Integer.valueOf(u.workUnitType));
        c.setDepartment(u.department);
        c.setProfessional(u.professional);
        c.setOfficeTel(u.officTel);

        if(null!=u.getMobile()&&!u.getMobile().isEmpty()){//手机号
          c.setNumber(u.getMobile());
        }else if(null!=u.getMail()&&!u.getMail().isEmpty()){//邮箱号
          c.setEmail(u.getMail());
        }
        ccs.add(c);
      }
    }
    return ccs;
  }

  private void compareList(final List<Contact> list, final String fromWhere) {
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        if (list != null && !list.isEmpty()) {
          if (fromWhere.equals(FROM_BOOT)) {
            for (Contact contact : list) {
              if (!TextUtils.isEmpty(contact.getNubeNumber())
                  && !recommendList.containsKey(contact.getNubeNumber())) {
                recommendList.put(contact.getNubeNumber(), contact);
                recommendList.get(contact.getNubeNumber()).setBeRecommended(1);
                if (!TextUtils.isEmpty(contact.getNickname())) {

                  recommendList.get(contact.getNubeNumber()).setFirstName(
                      StringHelper.getPinYinHeadChar(contact.getNickname()));
                }
                CustomLog.d(
                    TAG,
                    "FROM_BOOT contact "
                        + recommendList.get(contact.getNubeNumber()));

              }
            }
          } else {
            for (Contact contact : list) {
              if (!TextUtils.isEmpty(contact.getNubeNumber())
                  && !recommendList.containsKey(contact.getNubeNumber())) {
                recommendList.put(contact.getNubeNumber(), contact);
                recommendList.get(contact.getNubeNumber()).setBeRecommended(1);
                if (!TextUtils.isEmpty(contact.getNickname())) {
                  recommendList.get(contact.getNubeNumber()).setFirstName(
                      StringHelper.getPinYinHeadChar(contact.getNickname()));
                }
                CustomLog.d(
                    TAG,
                    "FROM_s contact "
                        + recommendList.get(contact.getNubeNumber()));

              }
            }
          }
        }
        Message msg = new Message();
        msg.what = DO_COMPARA_DATA;
        msg.setTarget(mainHandler);
        msg.sendToTarget();
      }
    };
    dataHandler.post(runnable);
  }

  public void registerListener(IRecommendListChanged listener) {
    CustomLog.i(TAG, "registerVersionListener");
    if (listChangedListener == null) {
      listChangedListener = new ArrayList<IRecommendListChanged>();
    }
    if (listener != null && !listChangedListener.contains(listener)) {
      listChangedListener.add(listener);
    }
  }

  private void notifyListener(int num) {
    if (listChangedListener != null && listChangedListener.size() > 0) {
      for (IRecommendListChanged listener : listChangedListener) {
        listener.onListChange(num);
      }
    }
  }

  // 推荐页面和主页面销毁时要调用这个接口，否则一直增加，内存泄露
  public void unRegisterVersionListener(IRecommendListChanged listener) {
    if (listChangedListener != null && listChangedListener.size() > 0
        && listener != null && listChangedListener.contains(listener)) {
      listChangedListener.remove(listener);
    }
  }

  public int getNewRecommentCount() {
    int num = 0;
    List<Contact> list = getRecommendList();
    if (!list.isEmpty()) {
      for (Contact c : list) {
        if (c.getBeRecommended() == 1/*c.getBeAdded()==0*/) {
          num++;
        }
      }
    }
    return num;
  }

  /**
   * <pre>
   * CAUTION :
   * 实例化 给外界调用
   * </pre>
   */
  public synchronized static RecommendManager getInstance(Context context) {
    CustomLog.i(TAG, "getInstance 初始化");
    if (mInstance == null) {
      CustomLog.d(TAG, "getInstance init...");
      mInstance = new RecommendManager();
      mInstance.dataHandlerThread = new HandlerThread("recommendHandlerThread");
      mInstance.dataHandlerThread.start();
      mInstance.dataHandler = new Handler(
          mInstance.dataHandlerThread.getLooper());
      mInstance.mContext = context;
      mInstance.observer = mInstance.new ContactObserver(mInstance.mainHandler);
      mInstance.mContext.getContentResolver().registerContentObserver(
          Data.CONTENT_URI, true, mInstance.observer);
      mInstance.addRecommendLoadedCallback(mInstance.observer);
      mInstance.state = INIT_STATE;
    }
    return mInstance;
  }

  public List<Contact> getRecommendList() {
    CustomLog.d(TAG, "testtttttttttt" + recommendList.values().toString());
    return new ArrayList<Contact>(recommendList.values());
  }

  public void changeBeAdded(Contact c) {
    if (c != null && recommendList.containsKey(c.getNubeNumber())) {
      CustomLog.d(TAG, "111recommendList.get(c.getNubeNumber()).getBeAdded"
          + recommendList.get(c.getNubeNumber()).getBeAdded());
      recommendList.get(c.getNubeNumber()).setBeAdded(1);
      CustomLog.d(TAG, recommendList.toString());
      CustomLog.d(TAG, "22recommendList.get(c.getNubeNumber()).getBeAdded"
          + recommendList.get(c.getNubeNumber()).getBeAdded());
    }
  }

  public void changeIdAndBeAdded(String id) {
    getContactById(id, new ContactCallback() {

      @Override
      public void onFinished(ResponseEntry result) {
        if (result != null && result.status == 0) {
          Contact c = (Contact) result.content;
          if (c != null) {
            if (recommendList.containsKey(c.getNubeNumber())) {
              recommendList.get(c.getNubeNumber()).setBeAdded(0);
              recommendList.get(c.getNubeNumber()).setLastTime(0);
              recommendList.get(c.getNubeNumber()).setContactId(
                  CommonUtil.getUUID());
            } else {
              if (nameList.containsKey(c.getNumber())) {
                Contact con = new Contact();
                con.setContactId(CommonUtil.getUUID());
                con.setNubeNumber(c.getNubeNumber());
                con.setAppType(c.getAppType());
                con.setNickname(c.getNickname());
                con.setPicUrl(c.getPicUrl());
                con.setContactUserId(c.getContactUserId());
                con.setNumber(c.getNumber());
                con.setUserType(c.getUserType());
                con.setUserFrom(1);
                con.setLastTime(0);
                con.setRawContactId(nameList.get(c.getNumber()));

                con.setHeadUrl(c.getPicUrl());
                con.setName(c.getNickname());
                con.setWorkUnit(c.getWorkUnit());
                con.setWorkUnitType(c.getWorkUnitType());
                con.setDepartment(c.getDepartment());
                con.setProfessional(c.getProfessional());
                con.setOfficeTel(c.getOfficeTel());

                if(null!=c.getNumber()&&!c.getNumber().isEmpty()){//手机号
                  con.setNumber(c.getNumber());
                }else if(null!=c.getEmail()&&!c.getEmail().isEmpty()){//邮箱号
                  con.setEmail(c.getEmail());
                }
                recommendList.put(con.getNubeNumber(), con);
              }
            }
          }

        }
      }
    });
  }

  private void getContactById(String id, ContactCallback callback) {
    String sql = "select * from "
        + ContactManager.getInstance(mContext).getMyTable()
        + " where contactId = '" + id + "' ";
    CustomAsyncTask task = new CustomAsyncTask();
    task.setCallback(callback);
    task.setTable(ContactManager.getInstance(mContext).getMyTable());
    task.setOpertionStatus(CustomAsyncTask.RAWQUERY_CONTACT_BY_ID);
    task.setContext(mContext);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, sql);
    } else {
      task.execute(sql);
    }
  }

  /**
   * doGetRecommendList 总入口
   * 
   * @param num
   *          此处是为了obserber逻辑设置的，普通调用请填0
   * @param from
   *          此次是为了区分调用的源头 启动调用填写FROM_BOOT ，手机联系人变化FROM_SYS
   */
  public void doGetRecommendList(int num, String from) {
    if (state != INIT_STATE) {
      CustomLog.d(TAG, "state is " + state + " 此次请求被忽略...");
    } else {
      state = GET_RECOMMEND_ING;
      fromWhere = from;
      numCount = num;
      if (from.equals(FROM_BOOT)) {
        recommendList.clear();
      }
      CustomLog
          .d(TAG, "fromWhere is " + fromWhere + " numCount is " + numCount);
      Runnable runnable = new Runnable() {
        @Override
        public void run() {
          List<List<String>> list = queryLocalMobileFilterNumber();
          Message msg = new Message();
          msg.what = DO_FILTER_DATA;
          msg.obj = list;
          msg.setTarget(mainHandler);
          msg.sendToTarget();
        }
      };
      dataHandler.post(runnable);
    }
  }

  // 获取推荐需要上传的数据 手机号码列表
  private List<List<String>> queryLocalMobileFilterNumber() {
    CustomLog.d(TAG, "查询android本地联系人号码并过滤  start");
    Cursor localCursor = null;
    List<String> phoneModeList = new ArrayList<String>();
    List<String> item = new ArrayList<String>();
    List<List<String>> allList = new ArrayList<List<String>>();
    try {
      // 取得手机终端联系人，且在应用联系人中未重复
      localCursor = mContext.getContentResolver().query(Phone.CONTENT_URI,
          new String[] { Phone.RAW_CONTACT_ID, Phone.NUMBER }, null, null,
          Phone.RAW_CONTACT_ID);
      if (localCursor != null && localCursor.getCount() > 0) {
        CustomLog.d(TAG, "本地联系人数据量：" + localCursor.getCount());
        String number;
        // TODO 从数据库里面读已有联系人
        List<String> phoneList = getAllPhone();
        nameList.clear();
        while (localCursor.moveToNext()) {
          nameList.put(
              CommonUtil.simpleFormatMoPhone(localCursor.getString(1)),
              localCursor.getString(0));
          // 过滤已有
          number = checkNumberMode(localCursor.getString(1), phoneList);
          if (TextUtils.isEmpty(number)) {
            continue;
          }
          // 去重复
          if (phoneModeList.contains(number)) {
            continue;
          }
          // 去自己
          if (AccountManager.getInstance(mContext).getAccountInfo() != null
              && number.equals(AccountManager.getInstance(mContext)
                  .getAccountInfo().mobile)) {
            continue;
          }
          phoneModeList.add(number);
          item.add(number);
          if (item.size() == 50) {
            allList.add(new ArrayList<String>(item));
            item.clear();
          }
        }
        if (item != null && item.size() > 0) {
          allList.add(new ArrayList<String>(item));
          item.clear();
        }
        item = null;
        phoneModeList.clear();
        phoneModeList = null;
      }
    } catch (Exception e) {
      CustomLog.d(TAG, "查询android本地联系人号码并过滤   faile" + e);
    } finally {
      if (localCursor != null) {
        localCursor.close();
        localCursor = null;
      }
    }
    CustomLog.d(TAG,
        "查询android本地联系人号码并过滤  end the list size is " + allList.size());
    return allList;
  }

  private List<String> getAllPhone() {
    String sql = "select number number from "
        + ContactManager.getInstance(mContext).getMyTable()
        + " where isDeleted = 0";
    List<String> list = new ArrayList<String>();
    Cursor cursor = ContactDBOperater.getInstance(mContext).rawQuery(sql,
        ContactManager.getInstance(mContext).getMyTable());
    if (cursor != null) {
      while (cursor.moveToNext()) {
        list.add(cursor.getString(cursor.getColumnIndex(DBConf.PHONENUMBER)));
      }
     // CustomLog.d(TAG, "list size " + list.size());
    }
    if (cursor != null) {
      cursor.close();
      cursor = null;
    }
    return list;
  }

  /*
   * 根据电话号码取得联系人姓名
   */
  public static String getNameByNum(String mNumber) {
    String name = "";
    String[] projection = { ContactsContract.PhoneLookup.DISPLAY_NAME,
        Phone.NUMBER };
    Cursor cursor = mContext.getContentResolver().query(
        Phone.CONTENT_URI, projection,
        Phone.NUMBER + " = '" + mNumber + "'",
        null, null);
    if (cursor == null) {
      return name;
    }
    for (int i = 0; i < cursor.getCount(); i++) {
      cursor.moveToPosition(i);
      // 取得联系人名字
      int nameFieldColumnIndex = cursor
          .getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
      name = cursor.getString(nameFieldColumnIndex);
    }
    if (cursor != null) {
      cursor.close();
      cursor = null;
    }
    return name;
  }


  public static String getContactNameFromPhoneBook(Context context, String phoneNum) {
    String contactName = "";
    ContentResolver cr = context.getContentResolver();
    Cursor pCur = cr.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
            ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?",
            new String[] { phoneNum }, null);
    if (pCur != null) {
      if (pCur.moveToFirst()) {
        contactName = pCur
                .getString(pCur
                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
        pCur.close();
      }
    }

    return contactName;
  }


  /**
   * 查询姓名
   * */
  public static String getNameByRawId(Context context, String rawContactId) {
    String name = "";
    if (rawContactId == null) {
      return name;
    }
    Cursor cursorOfName = context.getContentResolver().query(Data.CONTENT_URI,
        new String[] { Data.DISPLAY_NAME,// 姓名
        }, Data.RAW_CONTACT_ID + " = ? ", new String[] { rawContactId }, null);
    if (cursorOfName == null) {
      return name;
    }
    while (cursorOfName.moveToNext()) {
      name = cursorOfName.getString(cursorOfName
          .getColumnIndex(Data.DISPLAY_NAME));

    }
    if (cursorOfName != null) {
      cursorOfName.close();
      cursorOfName = null;
    }
    return name;
  }
  public String getRawIdByMobile(String number){
	  String num ="";
	  if(nameList!=null&&number!=null){
	     num = nameList.get(number);
	  }
	  return num;
  }
  
  private String checkNumberMode(String phone, List<String> list) {
    if (TextUtils.isEmpty(phone))
      return "";
    String temp = CommonUtil.simpleFormatMoPhone(phone);
    if (list.contains(temp)) {
      CustomLog.d("checkNumberMode", "排除号码：" + temp);
      return "";
    }
    return temp;
  }

  // ////////////////////////////

  private class ContactObserver extends ContentObserver implements
      OnRecommendtLoadedListener {
    private int changeCount = 0;

    @Override
    public void onChange(boolean selfChange) {
      changeCount++;
      responsdeSystemData();
    }

    private void responsdeSystemData() {
      if (state == INIT_STATE) {
        final int triggerCount = changeCount;
        doGetRecommendList(triggerCount, FROM_SYS);
      }
    }

    public ContactObserver(Handler handler) {
      super(handler);

    }

    @Override
    public void onRecommendLoaded(int num) {
      if (changeCount != 0) {
        if (num == changeCount) {
          changeCount = 0;
          numCount = 0;
        } else {
          responsdeSystemData();
        }
      }
    }
  }

  public interface OnRecommendtLoadedListener {
    public void onRecommendLoaded(int num);
  }

  public void addRecommendLoadedCallback(OnRecommendtLoadedListener listener) {
    mInstance.mOnRecommendtLoadedListener = listener;
  }

  public void clearInfos() {
    if (state != INIT_STATE) {
      state = DO_STOP;
    }
    if (searchUsers != null) {
      searchUsers.cancel();
      searchUsers = null;
    }
    numCount = 0;
    isHasGetList = false;
    allList.clear();
    recommendList.clear();
  }
}
