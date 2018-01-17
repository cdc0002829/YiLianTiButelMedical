package cn.redcdn.hvs.contacts.contact.butelDataAdapter;

import android.database.Cursor;

import cn.redcdn.buteldataadapter.DataSet;
import cn.redcdn.hvs.contacts.contact.database.DBConf;
import cn.redcdn.log.CustomLog;
import cn.redcdn.hvs.contacts.contact.interfaces.Contact;

public class ContactSetImp implements DataSet {
    private final String TAG = this.getClass().getName();
    private Cursor mCursor;

    public void setSrcData(Cursor data) {
        mCursor = data;
    }

    public Cursor getSrcData() {
        return mCursor;
    }

    @Override
    public <T> Object getItem(int index) {
        if (index < 0 || mCursor == null || mCursor.getCount() <= index) {
            CustomLog.e(TAG, "Illegal params, return null !");
            return null;
        }
        int pos = mCursor.getPosition();

        mCursor.moveToPosition(index);
        Contact contact = getDataFromCursor(mCursor);
        mCursor.moveToPosition(pos);

        return contact;
    }

    @Override
    public int getCount() {
        if (mCursor != null) {
            return mCursor.getCount();
        }

        return 0;
    }

    @Override
    public void release() {
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
    }

    private Contact getDataFromCursor(Cursor cursor) {
        Contact c = new Contact();
        c.setContactId(cursor.getString(cursor.getColumnIndex(DBConf.CONTACTID)));
        c.setName(cursor.getString(cursor.getColumnIndex(DBConf.NAME)));
        c.setNubeNumber(cursor.getString(cursor.getColumnIndex(DBConf.NUBENUMBER)));
        c.setNickname(cursor.getString(cursor.getColumnIndex(DBConf.NICKNAME)));
        c.setAppType(cursor.getString(cursor.getColumnIndex(DBConf.APPTYPE)));
        c.setPicUrl(cursor.getString(cursor.getColumnIndex(DBConf.PICURL)));
        c.setIsDeleted(cursor.getInt(cursor.getColumnIndex(DBConf.ISDELETED)));
        c.setLastTime(cursor.getLong(cursor.getColumnIndex(DBConf.LASTTIME)));
        c.setNumber(cursor.getString(cursor.getColumnIndex(DBConf.PHONENUMBER)));
        c.setContactUserId(cursor.getString(cursor
                .getColumnIndex(DBConf.CONTACTUSERID)));
        c.setUserType(cursor.getInt(cursor.getColumnIndex(DBConf.USERTYPE)));
        c.setUserFrom(cursor.getInt(cursor.getColumnIndex(DBConf.USERFROM)));
        c.setFirstName(cursor.getString(cursor.getColumnIndex(DBConf.FIRSTNAME)));

        c.setEmail(cursor.getString(cursor.getColumnIndex(DBConf.EMAIL)));

        c.setWorkUnitType(cursor.getInt(cursor.getColumnIndex(DBConf.WORKUNIT_TYPE)));
        c.setWorkUnit(cursor.getString(cursor.getColumnIndex(DBConf.WORK_UNIT)));
        c.setProfessional(cursor.getString(cursor.getColumnIndex(DBConf.PROFESSIONAL)));
        c.setDepartment(cursor.getString(cursor.getColumnIndex(DBConf.DEPARTMENT)));
        c.setOfficeTel(cursor.getString(cursor.getColumnIndex(DBConf.OFFICETEL)));
        c.setSaveToContactsTime(cursor.getString(cursor.getColumnIndex(DBConf.SAVE_TO_CONTACTS_TIME)));

        //CustomLog.d(TAG, "getDataFromCursor"+c.toString());
        return c;
    }
}
