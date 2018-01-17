package cn.redcdn.hvs.contacts.contact.interfaces;

public interface ContactOperation {
  public void getAllContacts(ContactCallback callback,
                             boolean isNeedCustomerService);

  public boolean isContactExist(String nubeNumber);

  public void addContact(Contact contact, ContactCallback callback);
  
  public String getHeadUrlByNube(String nubeNumber);
  
  public boolean checkNubeIsCustomService(String nubeNumber);
}
