package cn.redcdn.hvs.contacts.contact.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.contacts.contact.butelDataAdapter.ContactSetImp;
import cn.redcdn.hvs.contacts.contact.database.DBConf;
import cn.redcdn.hvs.contacts.contact.interfaces.Contact;
import cn.redcdn.hvs.contacts.contact.interfaces.ContactCallback;
import cn.redcdn.hvs.contacts.contact.interfaces.ResponseEntry;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.log.CustomLog;

public class CustomAsyncTask extends AsyncTask<String, Integer, ResponseEntry> {
    private static final String TAG = CustomAsyncTask.class.getSimpleName();
    private List<ContactCallback> mCallback;
    private String mTable;
    private ContentValues contentValues;
    private List<ContentValues> mContentValuesList;
    private String whereClause;
    private Context context;
    private String[] whereArgs;
    private int mOpertionStatus = -1;
    private boolean isNeedCustomerService = false;
    // private int mDetailedOpertion = -1;
    public static final int OPERATE_RAWQUERY = 0;
    public static final int OPERATE_INSERT = 1;
    public static final int OPERATE_DELETE = 2;
    public static final int OPERATE_UPDATE = 3;
    public static final int RAWQUERY_PHONE_NUM = 4;
    public static final int RAWQUERY_CONTACT_BY_ID = 5;
    private String indexString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public void setContentValues(ContentValues contentValues) {
        this.contentValues = contentValues;
    }

    public void setContentValuesList(List<ContentValues> mContentValuesList) {
        this.mContentValuesList = mContentValuesList;
    }

    public void setCallback(ContactCallback mCallback) {
        if (this.mCallback == null) {
            this.mCallback = new ArrayList<ContactCallback>();
        }
        this.mCallback.add(mCallback);
    }

    public void setTable(String mTable) {
        this.mTable = mTable;
    }

    public void setWhereClause(String whereClause) {
        this.whereClause = whereClause;
    }

    public void setWhereArgs(String[] whereArgs) {
        this.whereArgs = whereArgs;
    }

    public void setOpertionStatus(int mOpertionStatus) {
        this.mOpertionStatus = mOpertionStatus;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setCustomerServiceType(boolean isNeed) {
        this.isNeedCustomerService = isNeed;
    }

    @Override
    protected void onPostExecute(ResponseEntry result) {
        super.onPostExecute(result);
        CustomLog.d(TAG, "async task onPostExecute");
        if (mCallback != null) {
            for (int i = 0; i < mCallback.size(); i++) {
                mCallback.get(i).onFinished(result);
            }
        }
    }

    @Override
    protected ResponseEntry doInBackground(String... params) {
        CustomLog.d(TAG, "async task background thread run");
        String param = params[0];
        ResponseEntry result = new ResponseEntry();
        Cursor cursor;
        if(AccountManager.getInstance(context).getLoginState()== AccountManager.LoginState.ONLINE){
            switch (mOpertionStatus) {

                case OPERATE_RAWQUERY: // 查询
                    cursor = ContactDBOperater.getInstance(context).rawQuery(param,
                            mTable);
                    if (cursor == null) {
                        result.status = -1;
                    } else {
                        result.status = 0;
                        CustomLog.d(TAG, "cursor size " + cursor.getCount());
                    }
                    // 排序修改
                    MatrixCursor m1 = new MatrixCursor(DBConf.contacTableColumn);
                    MatrixCursor m2 = new MatrixCursor(DBConf.contacTableColumn);
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            String headChar = cursor.getString(cursor
                                    .getColumnIndex(DBConf.FIRSTNAME));
                            if (!indexString.contains(headChar)) {
                                headChar = "#";
                                m1.addRow(new Object[]{
                                        cursor.getString(cursor
                                                .getColumnIndex(DBConf.CONTACTID)),
                                        cursor.getString(cursor
                                                .getColumnIndex(DBConf.NAME)),
                                        cursor.getString(cursor
                                                .getColumnIndex(DBConf.NICKNAME)),
                                        headChar,
                                        cursor.getLong(cursor
                                                .getColumnIndex(DBConf.LASTTIME)),
                                        cursor.getInt(cursor
                                                .getColumnIndex(DBConf.ISDELETED)),
                                        cursor.getString(cursor
                                                .getColumnIndex(DBConf.PHONENUMBER)),
                                        cursor.getString(cursor
                                                .getColumnIndex(DBConf.PICURL)),
                                        cursor.getString(cursor
                                                .getColumnIndex(DBConf.NUBENUMBER)),
                                        cursor.getString(cursor
                                                .getColumnIndex(DBConf.CONTACTUSERID)),
                                        cursor.getInt(cursor
                                                .getColumnIndex(DBConf.SYNCSTAT)),
                                        cursor.getInt(cursor
                                                .getColumnIndex(DBConf.USERTYPE)),
                                        cursor.getInt(cursor
                                                .getColumnIndex(DBConf.USERFROM)),
                                        cursor.getString(cursor
                                                .getColumnIndex(DBConf.APPTYPE)),
                                        cursor.getString(cursor
                                                .getColumnIndex(DBConf.PINYIN)),
                                        cursor.getString(cursor
                                                .getColumnIndex(DBConf.RESERVESTR1)),
                                        cursor.getString(cursor
                                                .getColumnIndex(DBConf.RESERVESTR2)),
                                        cursor.getString(cursor
                                                .getColumnIndex(DBConf.RESERVESTR3)),
                                        cursor.getString(cursor
                                                .getColumnIndex(DBConf.RESERVESTR4)),
                                        cursor.getString(cursor
                                                .getColumnIndex(DBConf.RESERVESTR5)),
                                        cursor.getString(cursor.getColumnIndex(DBConf.EMAIL)),
                                        cursor.getInt(cursor.getColumnIndex(DBConf.ACCOUNT_TYPE)),
                                        cursor.getInt(cursor.getColumnIndex(DBConf.WORKUNIT_TYPE)),
                                        cursor.getString(cursor.getColumnIndex(DBConf.WORK_UNIT)),
                                        cursor.getString(cursor.getColumnIndex(DBConf.DEPARTMENT)),
                                        cursor.getString(cursor.getColumnIndex(DBConf.PROFESSIONAL)),
                                        cursor.getString(cursor.getColumnIndex(DBConf.OFFICETEL)),
                                        cursor.getString(cursor.getColumnIndex(DBConf.SAVE_TO_CONTACTS_TIME))});
                            } else {
                                m2.addRow(new Object[]{
                                        cursor.getString(cursor
                                                .getColumnIndex(DBConf.CONTACTID)),
                                        cursor.getString(cursor
                                                .getColumnIndex(DBConf.NAME)),
                                        cursor.getString(cursor
                                                .getColumnIndex(DBConf.NICKNAME)),
                                        headChar,
                                        cursor.getLong(cursor
                                                .getColumnIndex(DBConf.LASTTIME)),
                                        cursor.getInt(cursor
                                                .getColumnIndex(DBConf.ISDELETED)),
                                        cursor.getString(cursor
                                                .getColumnIndex(DBConf.PHONENUMBER)),
                                        cursor.getString(cursor
                                                .getColumnIndex(DBConf.PICURL)),
                                        cursor.getString(cursor
                                                .getColumnIndex(DBConf.NUBENUMBER)),
                                        cursor.getString(cursor
                                                .getColumnIndex(DBConf.CONTACTUSERID)),
                                        cursor.getInt(cursor
                                                .getColumnIndex(DBConf.SYNCSTAT)),
                                        cursor.getInt(cursor
                                                .getColumnIndex(DBConf.USERTYPE)),
                                        cursor.getInt(cursor
                                                .getColumnIndex(DBConf.USERFROM)),
                                        cursor.getString(cursor
                                                .getColumnIndex(DBConf.APPTYPE)),
                                        cursor.getString(cursor
                                                .getColumnIndex(DBConf.PINYIN)),
                                        cursor.getString(cursor
                                                .getColumnIndex(DBConf.RESERVESTR1)),
                                        cursor.getString(cursor
                                                .getColumnIndex(DBConf.RESERVESTR2)),
                                        cursor.getString(cursor
                                                .getColumnIndex(DBConf.RESERVESTR3)),
                                        cursor.getString(cursor
                                                .getColumnIndex(DBConf.RESERVESTR4)),
                                        cursor.getString(cursor
                                                .getColumnIndex(DBConf.RESERVESTR5)),
                                        cursor.getString(cursor.getColumnIndex(DBConf.EMAIL)),
                                        cursor.getInt(cursor.getColumnIndex(DBConf.ACCOUNT_TYPE)),
                                        cursor.getInt(cursor.getColumnIndex(DBConf.WORKUNIT_TYPE)),
                                        cursor.getString(cursor.getColumnIndex(DBConf.WORK_UNIT)),
                                        cursor.getString(cursor.getColumnIndex(DBConf.DEPARTMENT)),
                                        cursor.getString(cursor.getColumnIndex(DBConf.PROFESSIONAL)),
                                        cursor.getString(cursor.getColumnIndex(DBConf.OFFICETEL)),
                                        cursor.getString(cursor.getColumnIndex(DBConf.SAVE_TO_CONTACTS_TIME))});
                            }
                        }
                    }
                    Cursor mCursor;
                    if (isNeedCustomerService) {
                        MatrixCursor mm = new MatrixCursor(DBConf.contacTableColumn);
                        MatrixCursor mc = new MatrixCursor(DBConf.contacTableColumn);

//                    new String[]{CONTACTID, NAME, NICKNAME, FIRSTNAME, LASTTIME,
//                            ISDELETED, PHONENUMBER, PICURL, NUBENUMBER,CONTACTUSERID,
//                            SYNCSTAT, USERTYPE, USERFROM, APPTYPE, PINYIN,
//                            RESERVESTR1, RESERVESTR2, RESERVESTR3, RESERVESTR4, RESERVESTR5,
//                            EMAIL, ACCOUNT_TYPE, WORKUNIT_TYPE, WORK_UNIT, DEPARTMENT,
//                            PROFESSIONAL, OFFICETEL, SAVE_TO_CONTACTS_TIME

                        mc.addRow(new Object[]{CommonUtil.getUUID(), context.getString(R.string.new_friends_title_string), context.getString(R.string.new_friends_title_string), "", 0,
                                0, "", "", "2", "",
                                1, 1, 0, "", "",
                                "", "", "", "", "",
                                "", 0, 1, "", "",
                                "", "", ""});

                        mc.addRow(new Object[]{CommonUtil.getUUID(), context.getString(R.string.groupChat), context.getString(R.string.groupChat), "", 0,
                                0, "", "", "0", "",
                                1, 1, 0, "", "",
                                "", "", "", "", "",
                                "", 0, 1, "", "",
                                "", "", ""});
                        mc.addRow(new Object[]{CommonUtil.getUUID(), context.getString(R.string.publicNumber), context.getString(R.string.publicNumber), "", 0,
                                0, "", "", "1", "",
                                1, 1, 0, "", "",
                                "", "", "", "", "",
                                "", 0, 1, "", "",
                                "", "", ""});
                        if (AccountManager.getInstance(context).hpuList.size() == 0) {
                            AccountManager.getInstance(context).hpuList = ContactManager.getInstance(context).getDTListFromData();
                        }
                        if (AccountManager.getInstance(context).hpuList.size()>0){
                            for (int i = 0;i<AccountManager.getInstance(context).hpuList.size();i++){
                                String dtName = AccountManager.getInstance(context).hpuList.get(i).getName();
                                String dtId = AccountManager.getInstance(context).hpuList.get(i).getId();
                                String tag = String.valueOf(2+i+1);
                                mc.addRow(new Object[]{CommonUtil.getUUID(),dtName ,dtName,"", 0,
                                        0, dtId, "",tag , "",
                                        1, 1, 0, "", "",
                                        "", "", "", "", "",
                                        "", 0, 1, "", "",
                                        "", "", ""});
                            }
                        }
//                        mm.addRow(new Object[]{CommonUtil.getUUID(), context.getString(R.string.customerServiceName), context.getString(R.string.customerServiceName), "#", 0,
//                                0, "", "", ContactManager.customerServiceNum1, "",
//                                1, 1, 0, "", "",
//                                "", "", "", "", "",
//                                "", 0, 1, "", "",
//                                "", "", ""});
//                        mm.addRow(new Object[]{CommonUtil.getUUID(), context.getString(R.string.customerServiceName), context.getString(R.string.customerServiceName), "#", 0,
//                                0, "", "", ContactManager.customerServiceNum2, "",
//                                1, 1, 0, "", "",
//                                "", "", "", "", "",
//                                "", 0, 1, "", "",
//                                "", "", ""});
//				Cursor[] a = new Cursor[] { m2, m1, mm };
//                    Cursor[] a = new Cursor[]{mc, mm, m2, m1};
                        Cursor[] a = new Cursor[]{mc, m2, m1, mm};
                        mCursor = new MergeCursor(a);
                    } else {
                        Cursor[] a = new Cursor[]{m2, m1};
                        mCursor = new MergeCursor(a);
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    ContactSetImp imp = new ContactSetImp();
                    imp.setSrcData(mCursor);
                    result.content = imp;
                    break;
                case RAWQUERY_PHONE_NUM:
                    cursor = ContactDBOperater.getInstance(context).rawQuery(param,
                            mTable);
                    if (cursor == null) {
                        result.status = -1;
                    } else {
                        result.status = 0;
                    }
                    if (cursor != null) {
                        List<String> list = new ArrayList<String>();
                        while (cursor.moveToNext()) {
                            list.add(cursor.getString(cursor
                                    .getColumnIndex(DBConf.PHONENUMBER)));
                        }
                        CustomLog.d(TAG, "list size " + list.size());
                        result.content = list;
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    break;
                case RAWQUERY_CONTACT_BY_ID:
                    cursor = ContactDBOperater.getInstance(context).rawQuery(param,
                            mTable);
                    if (cursor == null) {
                        result.status = -1;
                    } else {
                        result.status = 0;
                    }
                    if (cursor != null && cursor.moveToNext()) {
                        Contact c = getDataFromCursor(cursor);
                        result.content = c;
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    break;
                case OPERATE_INSERT: // 插入
                    long insert = ContactDBOperater.getInstance(context).insert(mTable,
                            contentValues);
                    if (insert > 0) {
                        result.status = 0;
                    } else {
                        result.status = (int) insert;
                    }
                    CustomLog.d(TAG, "OPERATE_INSERT result " + result.status);
                    break;
                case OPERATE_DELETE: // 删除 TODO
                    long delete = ContactDBOperater.getInstance(context).delete(mTable,
                            whereClause, whereArgs);
                    if (delete > 0) {
                        result.status = 0;
                    } else {
                        result.status = -1;
                    }
                    CustomLog.d(TAG, "OPERATE_DELETE result " + result.status);
                    break;
                case OPERATE_UPDATE: // 更新 TODO
                    long update = ContactDBOperater.getInstance(context).update(mTable,
                            contentValues, whereClause, whereArgs);
                    if (update > 0) {
                        result.status = 0;
                    } else {
                        result.status = -1;
                    }
                    CustomLog.d(TAG, "OPERATE_UPDATE result " + update);
                    break;
                default:
                    break;
            }
        }

        return result;
    }

    @Override
    protected void onCancelled(ResponseEntry entry) {
        CustomLog.d(TAG, "async task onCancelled");
    }

    private Contact getDataFromCursor(Cursor cursor) {
        Contact c = new Contact();
        c.setContactId(cursor.getString(cursor.getColumnIndex(DBConf.CONTACTID)));
        c.setName(cursor.getString(cursor.getColumnIndex(DBConf.NAME)));
        c.setNubeNumber(cursor.getString(cursor
                .getColumnIndex(DBConf.NUBENUMBER)));
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

        c.setAccountType(cursor.getInt(cursor.getColumnIndex(DBConf.ACCOUNT_TYPE)));
        c.setEmail(cursor.getString(cursor.getColumnIndex(DBConf.EMAIL)));
        c.setWorkUnitType(cursor.getInt(cursor.getColumnIndex(DBConf.WORKUNIT_TYPE)));
        c.setWorkUnit(cursor.getString(cursor.getColumnIndex(DBConf.WORK_UNIT)));
        c.setProfessional(cursor.getString(cursor.getColumnIndex(DBConf.PROFESSIONAL)));
        c.setDepartment(cursor.getString(cursor.getColumnIndex(DBConf.DEPARTMENT)));
        c.setOfficeTel(cursor.getString(cursor.getColumnIndex(DBConf.OFFICETEL)));
        c.setSaveToContactsTime(cursor.getString(cursor.getColumnIndex(DBConf.SAVE_TO_CONTACTS_TIME)));
        return c;
    }
}
