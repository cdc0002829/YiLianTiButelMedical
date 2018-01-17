package cn.redcdn.hvs.contacts.contact;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.contacts.contact.interfaces.Contact;
import cn.redcdn.hvs.contacts.contact.manager.RecommendManager;
import cn.redcdn.log.CustomLog;

public class RecommendActivity extends BaseActivity {

  private HashMap<String, Integer> selector;// 存放含有索引字母的位置
  private String[] indexStr = { "#", "A", "B", "C", "D", "E", "F", "G", "H",
      "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
      "W", "X", "Y", "Z" };
  private List<Contact> mRecommendList = null;
  private List<Contact> mNewlistRecommendList = null;
  private LinearLayout noRecommendLayout = null;
  private List<Contact> newRecommendContact = new ArrayList<Contact>();

  private ListView lvRecommend = null;
  private int firstTimeExecute = 0;
  private Button btnRecommendBack = null;
  /*** 定义消息类型 */
  private final int MSG_UPDATAUI = 0;
  private final int MSG_LOADINGDATA = 1;
  private RecommendListViewAdapter recommendAdapter;
  private int isDataChanged=0;
  private Button btnAddFriend = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.activity_recommend);

    RecommendActivity.this.showLoadingView(getString(R.string.loading_date),
        new DialogInterface.OnCancelListener() {

          @Override
          public void onCancel(DialogInterface dialog) {

          }
        });
    mHandler.sendEmptyMessage(MSG_LOADINGDATA);

  }

  @SuppressLint("HandlerLeak")
  private Handler mHandler = new Handler() {
    @Override
    public void handleMessage(android.os.Message msg) {
      switch (msg.what) {
      case MSG_LOADINGDATA:

        initRecommendData();
        break;
      case MSG_UPDATAUI:
        initRecommendPage();
        initAdapter();
        switchLayout();

        RecommendActivity.this.removeLoadingView();
      default:
        break;
      }

    };
  };

  private void switchLayout() {
    if (newRecommendContact.size() == 0) {

      noRecommendLayout.setVisibility(View.VISIBLE);
      lvRecommend.setVisibility(View.INVISIBLE);
    } else {

      noRecommendLayout.setVisibility(View.INVISIBLE);
      lvRecommend.setVisibility(View.VISIBLE);
    }
  }

  private void initRecommendPage() {
    noRecommendLayout = (LinearLayout) findViewById(R.id.norecommend_layout);
    btnRecommendBack = (Button) findViewById(R.id.btnrecommendback);
    btnAddFriend = (Button) findViewById(R.id.recommand_addfriend_btn);
    lvRecommend = (ListView) findViewById(R.id.lvrecommend);
    btnAddFriend.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
               finish();
      }
    });
    btnRecommendBack.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        // TODO Auto-generated method stub
        RecommendManager.getInstance(RecommendActivity.this)
            .doChangeRecommendState();
        Intent intent= getIntent();
        intent.putExtra("isDataChanged", isDataChanged);
        setResult(ContactTransmitConfig.RESULT_RECOMMEND_CODE, intent);
        finish();
      }
    });

    CustomLog.d(TAG, "112 ........" + newRecommendContact.size()
        + newRecommendContact.toString());

  }

  private void initAdapter() {
    recommendAdapter = new RecommendListViewAdapter(this, newRecommendContact/*,new bc()*/);
    lvRecommend.setAdapter(recommendAdapter);
  }

  private void initRecommendData() {
    new Thread() {
      public void run() {

        mRecommendList = new ArrayList<Contact>();
        List<Contact> contacts = RecommendManager.getInstance(
            RecommendActivity.this).getRecommendList();
        mNewlistRecommendList = new ArrayList<Contact>();
        mNewlistRecommendList.add(new Contact(getString(R.string.latest_recommend)));
        if (null != contacts) {
          for (Contact c : contacts) {
            CustomLog.d(TAG, c.getNubeNumber() + "test" + c.getNumber()
                + "c.beadded......" + c.getBeAdded());

            if(null!=RecommendManager.getInstance(getApplicationContext()).getRawIdByMobile(c.getNumber())){
              String id =RecommendManager.getInstance(getApplicationContext()).getRawIdByMobile(c.getNumber());
              String name= RecommendManager.getNameByRawId(getApplicationContext(), id);

              if (name!= null&&!name.isEmpty()) {
                c.setName(name);
              } else {
                c.setName("未命名");
              }
            }

            if (0 == c.getBeRecommended()) {
              mRecommendList.add(c);
            } else if (1 == c.getBeRecommended()) {
              mNewlistRecommendList.add(c);
            }

          }
        }
        CustomLog.i(TAG,
            "initData mRecommendList size=" + mRecommendList.size());
        for (int i = 0; i < mRecommendList.size(); i++) {
          CustomLog.i(TAG, "initData mRecommendList size="
              + mRecommendList.get(i).toString());
        }
        for (int i = 0; i < mNewlistRecommendList.size(); i++) {
          CustomLog.i(TAG, "initData mRecommendList size="
              + mNewlistRecommendList.get(i).toString());
        }

        updateData(mRecommendList, newRecommendContact);
        updataIndex(newRecommendContact);
        Contact.removeDuplicateWithOrder(newRecommendContact);
        mHandler.sendEmptyMessage(MSG_UPDATAUI);
      };
    }.start();
  }

  private void updateDataList() {
    mRecommendList = new ArrayList<Contact>();
    List<Contact> contacts = RecommendManager.getInstance(
        RecommendActivity.this).getRecommendList();
    mNewlistRecommendList = new ArrayList<Contact>();
    mNewlistRecommendList.add(new Contact("最新推荐"));
    if (null != contacts) {
      for (Contact c : contacts) {
        CustomLog.d(TAG, c.getNubeNumber() + "test" + c.getNumber()
            + "c.beadded......" + c.getBeAdded());
        if(null!=RecommendManager.getInstance(getApplicationContext()).getRawIdByMobile(c.getNumber())) {
          String id = RecommendManager.getInstance(getApplicationContext()).getRawIdByMobile(c.getNumber());
          String name = RecommendManager.getNameByRawId(getApplicationContext(), id);

          if (name != null && !name.isEmpty()) {
            c.setName(name);
          } else {
            c.setName("未命名");
          }
        }
        if (0 == c.getBeRecommended()) {
          mRecommendList.add(c);
        } else if (1 == c.getBeRecommended()) {
          mNewlistRecommendList.add(c);
        }

      }
    }
    CustomLog.i(TAG, "initData mRecommendList size=" + mRecommendList.size());
    for (int i = 0; i < mRecommendList.size(); i++) {
      CustomLog.i(TAG, "initData mRecommendList size="
          + mRecommendList.get(i).toString());
    }
    for (int i = 0; i < mNewlistRecommendList.size(); i++) {
      CustomLog.i(TAG, "initData mRecommendList size="
          + mNewlistRecommendList.get(i).toString());
    }
  }

  private void getNewList() {
    mRecommendList = new ArrayList<Contact>();
    List<Contact> contacts = RecommendManager.getInstance(
        RecommendActivity.this).getRecommendList();
    mNewlistRecommendList = new ArrayList<Contact>();
    mNewlistRecommendList.add(new Contact("最新推荐"));
    if (null != contacts) {
      for (Contact c : contacts) {
        CustomLog.d(TAG, c.getNubeNumber() + "test" + c.getNumber()
            + "c.beadded......" + c.getBeAdded());

        if (RecommendManager.getNameByRawId(RecommendActivity.this,
            c.getRawContactId()) != null
            && !RecommendManager.getNameByRawId(RecommendActivity.this,
                c.getRawContactId()).isEmpty()) {
          CustomLog.d(
              TAG,
              "1212121"
                  + RecommendManager.getNameByRawId(RecommendActivity.this,
                      c.getRawContactId()));
          c.setName(RecommendManager.getNameByRawId(RecommendActivity.this,
              c.getRawContactId()));
        } else {
          c.setName("未命名");
        }
        if (0 == c.getBeRecommended()) {
          mRecommendList.add(c);
        } else if (1 == c.getBeRecommended()) {
          mNewlistRecommendList.add(c);
        }

      }
    }
    CustomLog.i(TAG, "initData mRecommendList size=" + mRecommendList.size());
    for (int i = 0; i < mRecommendList.size(); i++) {
      CustomLog.i(TAG, "initData mRecommendList size="
          + mRecommendList.get(i).toString());
    }
    for (int i = 0; i < mNewlistRecommendList.size(); i++) {
      CustomLog.i(TAG, "initData mRecommendList size="
          + mNewlistRecommendList.get(i).toString());
    }
  }

  /**
   * 获取排序后的新数据
   * 
   * //@param persons
   * @return
   */
  public String[] sortIndex(List<Contact> mContacts) {
    TreeSet<String> set = new TreeSet<String>();
    // 获取初始化数据源中的首字母，添加到set中
    // set.add("最新推荐");
    for (Contact mContact : mContacts) {

      if (mContact.getName() != null && !mContact.getName().isEmpty()) {
        if (StringHelper.getPinYinHeadChar(mContact.getName()).substring(0, 1)
            .matches("[A-Z]|[a-z]")) {
          set.add(StringHelper.getPinYinHeadChar(mContact.getName()).substring(
              0, 1));
        } else {
          set.add(("#"));
        }

      }
    }
    // set.add("最新推荐");
    // 新数组的长度为原数据加上set的大小
    String[] names = new String[mContacts.size() + set.size()];
    int i = 0;
    for (String string : set) {

      if (string != "#") {
        names[i] = string.replace(string, string + '0');
      } else {
        names[i] = string;
      }
      CustomLog.d(TAG, ".....sss" + names[i]);
      i++;

    }
    String[] pinYinNames = new String[mContacts.size()];
    for (int j = 0; j < mContacts.size(); j++) {
      if (mContacts.get(j).getName() != null
          && !mContacts.get(j).getName().toString().isEmpty()) {
        pinYinNames[j] = (StringHelper.getPingYin(mContacts.get(j).getName()
            .toString()))
            .replace(
                StringHelper.getPingYin(mContacts.get(j).getName().toString()),
                StringHelper.getPingYin(mContacts.get(j).getName().toString()) + '1');
        CustomLog.d(TAG, "....." + pinYinNames[j]);
      }
    }

    // 将原数据拷贝到新数据中
    System.arraycopy(pinYinNames, 0, names, set.size(), pinYinNames.length);

    // 自动按照首字母排序
    Arrays.sort(names, String.CASE_INSENSITIVE_ORDER);
    return names;
  }

  private void updateData(List<Contact> srcContacts, List<Contact> dstContacts) {
    dstContacts.clear();
    CustomLog.d(TAG,
        "updateData...1...srcContacts.size()=" + srcContacts.size()
            + "......dstContacts.size()=" + dstContacts.size());
    String[] allNames = sortIndex(srcContacts);
    CustomLog.d(TAG, "srcContacts=" + srcContacts.toString());
    CustomLog.d(TAG,
        "updateData...2...srcContacts.size()=" + srcContacts.size()
            + "......dstContacts.size()=" + dstContacts.size()
            + "allNames.size" + allNames.length);

    sortList(allNames, srcContacts, dstContacts);
    removeSameWeiMingMing(dstContacts);
    if (mNewlistRecommendList.size() > 1) {
      List<Contact> temp = new ArrayList<Contact>();
      for (int i = 0; i < mNewlistRecommendList.size(); i++) {
        temp.add(mNewlistRecommendList.get(i));
      }
      for (int i = 0; i < dstContacts.size(); i++) {
        temp.add(dstContacts.get(i));
      }
      dstContacts.clear();
      for (int i = 0; i < temp.size(); i++) {
        dstContacts.add(temp.get(i));
      }
    }
    CustomLog.d(TAG,
        "updateData...3...srcContacts.size()=" + srcContacts.size()
            + "......dstContacts.size()=" + dstContacts.size()
            + "allNames.length" + allNames.length);
    updataIndex(dstContacts);
    CustomLog.d(TAG, "dstContacts=" + dstContacts.toString());
  }

  private void removeSameWeiMingMing(List<Contact> dstContacts) {
    for (int i = 0; i < dstContacts.size(); i++)
      for (int j = dstContacts.size() - 1; j > i; j--) {
        if (dstContacts.get(i).getName() != null
            && dstContacts.get(j).getName() != null) {
          if (dstContacts.get(i).getName().equals("未命名")
              && dstContacts.get(j).getName().equals("未命名")) {
            if (dstContacts.get(i).getNubeNumber()
                .equals(dstContacts.get(j).getNubeNumber())) {
              CustomLog.d(TAG, "......." + dstContacts.get(j).getNubeNumber());
              dstContacts.remove(dstContacts.get(j));
            }
          }
        }
      }
  }

  private void updataIndex(List<Contact> newContact) {
    selector = new HashMap<String, Integer>();
    for (int j = 0; j < indexStr.length; j++) {// 循环字母表，找出newPersons中对应字母的位置
      for (int i = 0; i < newContact.size(); i++) {
        if (newContact.get(i).getName().equals(indexStr[j])) {
          selector.put(indexStr[j], i);
        } else {
          selector.put(indexStr[26], i);
        }
      }

    }
  }

  /**
   * 重新排序获得一个新的List集合
   * 
   * @param allNames
   */
  private void sortList(String[] allNames, List<Contact> srcContacts,
      List<Contact> dstContacts) {
    for (int i = 0; i < allNames.length; i++) {
      CustomLog.d(TAG, allNames[i] + i);
      if (allNames[i].length() != 1) {

        if (allNames[i].length() == 2) {
          if (allNames[i].substring(1, 2).equals("0")) {

            allNames[i] = allNames[i].substring(0, 1);
            dstContacts.add(new Contact(allNames[i]));
          } else {
            allNames[i] = allNames[i].substring(0, 1);
            for (int j = 0; j < srcContacts.size(); j++) {
              if (allNames[i].equals(StringHelper.getPingYin(srcContacts.get(j)
                  .getName()))) {
                Contact p = new Contact();
                p.setName(srcContacts.get(j).getName());
                p.setFirstName(srcContacts.get(j).getFirstName());
                p.setNubeNumber(srcContacts.get(j).getNubeNumber());
                p.setNickname(srcContacts.get(j).getNickname());
                p.setNumber(srcContacts.get(j).getNumber());
                p.setContactId(srcContacts.get(j).getContactId());
                p.setAppType(srcContacts.get(j).getAppType());
                p.setBeAdded(srcContacts.get(j).getBeAdded());
                p.setBeRecommended(srcContacts.get(j).getBeRecommended());
                p.setContactUserId(srcContacts.get(j).getContactUserId());
                p.setIsDeleted(srcContacts.get(j).getIsDeleted());
                p.setFirstName(srcContacts.get(j).getFirstName());
                p.setLastTime(srcContacts.get(j).getLastTime());
                p.setPicUrl(srcContacts.get(j).getPicUrl());
                p.setUserFrom(srcContacts.get(j).getUserFrom());
                p.setUserType(srcContacts.get(j).getUserType());

                p.setWorkUnit(srcContacts.get(j).getWorkUnit());
                p.setWorkUnitType(srcContacts.get(j).getWorkUnitType());
                p.setDepartment(srcContacts.get(j).getDepartment());
                p.setProfessional(srcContacts.get(j).getProfessional());
                p.setOfficeTel(srcContacts.get(j).getOfficeTel());
                dstContacts.add(p);
              }
            }
          }
        } else {
          allNames[i] = allNames[i].substring(0, allNames[i].length() - 1);
          for (int j = 0; j < srcContacts.size(); j++) {
            if (allNames[i].equals(StringHelper.getPingYin(srcContacts.get(j)
                .getName()))) {
              Contact p = new Contact();
              p.setName(srcContacts.get(j).getName());
              p.setFirstName(srcContacts.get(j).getFirstName());
              p.setNubeNumber(srcContacts.get(j).getNubeNumber());
              p.setNickname(srcContacts.get(j).getNickname());
              p.setNumber(srcContacts.get(j).getNumber());
              p.setContactId(srcContacts.get(j).getContactId());
              p.setAppType(srcContacts.get(j).getAppType());
              p.setBeAdded(srcContacts.get(j).getBeAdded());
              p.setBeRecommended(srcContacts.get(j).getBeRecommended());
              p.setContactUserId(srcContacts.get(j).getContactUserId());
              p.setIsDeleted(srcContacts.get(j).getIsDeleted());
              p.setFirstName(srcContacts.get(j).getFirstName());
              p.setLastTime(srcContacts.get(j).getLastTime());
              p.setPicUrl(srcContacts.get(j).getPicUrl());
              p.setUserFrom(srcContacts.get(j).getUserFrom());
              p.setUserType(srcContacts.get(j).getUserType());

              p.setWorkUnit(srcContacts.get(j).getWorkUnit());
              p.setWorkUnitType(srcContacts.get(j).getWorkUnitType());
              p.setDepartment(srcContacts.get(j).getDepartment());
              p.setProfessional(srcContacts.get(j).getProfessional());
              p.setOfficeTel(srcContacts.get(j).getOfficeTel());
              dstContacts.add(p);
            }
          }
        }
      } else {
        dstContacts.add(new Contact(allNames[i]));
      }
    }
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    // 在oncreate里面执行下面的代码没反应，因为oncreate里面得到的getHeight=0
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
      RecommendManager.getInstance(RecommendActivity.this)
          .doChangeRecommendState();
      Intent intent= getIntent();
      intent.putExtra("isDataChanged", isDataChanged);
      setResult(ContactTransmitConfig.RESULT_RECOMMEND_CODE, intent);
      finish();
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (firstTimeExecute == 0) {
      firstTimeExecute = 1;
    } else {
      RecommendActivity.this.showLoadingView("加载数据中...",
          new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
              RecommendActivity.this.removeLoadingView();
            }
          });
      updateDataList();
      updateData(mRecommendList, newRecommendContact);
      Contact.removeDuplicateWithOrder(newRecommendContact);
      recommendAdapter.notifyDataSetChanged();
      CustomLog.d(TAG, "onResume 更新数据" + newRecommendContact.size());
      switchLayout();
      RecommendActivity.this.removeLoadingView();
    }

  }

  @Override
  protected void onStop() {
    super.onStop();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  @Override
  public void onBackPressed() {

    RecommendManager.getInstance(RecommendActivity.this)
        .doChangeRecommendState();
    Intent intent= getIntent();
    intent.putExtra("isDataChanged", isDataChanged);
    setResult(ContactTransmitConfig.RESULT_RECOMMEND_CODE, intent);
    super.onBackPressed();
    this.finish();
  }

}
