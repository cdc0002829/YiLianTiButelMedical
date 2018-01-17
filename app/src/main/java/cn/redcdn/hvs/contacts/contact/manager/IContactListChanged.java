package cn.redcdn.hvs.contacts.contact.manager;

import cn.redcdn.hvs.contacts.contact.butelDataAdapter.ContactSetImp;
//通讯录页面调用，数据刷新时更新列表
public interface IContactListChanged {
  public void onListChange(ContactSetImp set);
}