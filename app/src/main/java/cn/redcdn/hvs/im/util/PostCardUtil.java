package cn.redcdn.hvs.im.util;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContacts.Data;
import android.text.TextUtils;
import a_vcard.android.provider.Contacts;
import a_vcard.android.syncml.pim.VDataBuilder;
import a_vcard.android.syncml.pim.VNode;
import a_vcard.android.syncml.pim.vcard.ContactStruct;
import a_vcard.android.syncml.pim.vcard.ContactStruct.ContactMethod;
import a_vcard.android.syncml.pim.vcard.ContactStruct.PhoneData;
import a_vcard.android.syncml.pim.vcard.VCardComposer;
import a_vcard.android.syncml.pim.vcard.VCardException;
import a_vcard.android.syncml.pim.vcard.VCardParser;

import cn.redcdn.hvs.im.bean.ButelVcardBean;
import cn.redcdn.hvs.im.bean.ContactFriendBean;
import cn.redcdn.hvs.im.dao.MedicalDaoImpl;
import cn.redcdn.hvs.im.fileTask.FileTaskManager;
import cn.redcdn.hvs.util.StringUtil;
import cn.redcdn.log.CustomLog;

/**
 * 联系人信息包装类
 *
 * @author LW
 *
 */
public class PostCardUtil {

    /** MUST exist */
    private String name; // 姓名

    /** 联系人电话信息 */
    public static class PhoneInfo {
        /** 联系电话类型 */
        public int type;
        /** 联系电话 */
        public String number;
    }

    /** 联系人邮箱信息 */
    public static class EmailInfo {
        /** 邮箱类型 */
        public int type;
        /** 邮箱 */
        public String email;
    }

    private List<PhoneInfo> phoneList = new ArrayList<PhoneInfo>(); // 联系号码
    private List<EmailInfo> emailList = new ArrayList<EmailInfo>(); // Email

    /**
     * 构造联系人信息
     *
     * @param name
     *            联系人姓名
     */
    public PostCardUtil(String name) {
        this.name = name;
    }

    /** 姓名 */
    public String getName() {
        return name;
    }

    /** 姓名 */
    public PostCardUtil setName(String name) {
        this.name = name;
        return this;
    }

    /** 联系电话信息 */
    public List<PhoneInfo> getPhoneList() {
        return phoneList;
    }

    /** 联系电话信息 */
    public PostCardUtil setPhoneList(List<PhoneInfo> phoneList) {
        this.phoneList = phoneList;
        return this;
    }

    public PostCardUtil setPhone(PhoneInfo phone) {
        if (this.phoneList == null) {
            this.phoneList = new ArrayList<PhoneInfo>();
        }
        this.phoneList.add(phone);
        return this;
    }

    /** 邮箱信息 */
    public List<EmailInfo> getEmailList() {
        return emailList;
    }

    /** 邮箱信息 */
    public PostCardUtil setEmailList(List<EmailInfo> emailList) {
        this.emailList = emailList;
        return this;
    }

    public PostCardUtil setEmail(EmailInfo email) {
        if (this.emailList == null) {
            this.emailList = new ArrayList<EmailInfo>();
        }
        this.emailList.add(email);
        return this;
    }

    @Override
    public String toString() {
        return "{name: " + name + ", number: " + phoneList + ", email: "
                + emailList + "}";
    }

    public static String idListToString(List<String> list, char splitchar) {
        StringBuffer buf = null;
        if (list != null && list.size() > 0) {
            buf = new StringBuffer();

            int length = list.size();
            for (int i = 0; i < length - 1; i++) {
                buf.append(list.get(i));
                buf.append(splitchar);
            }
            buf.append(list.get(length - 1));
            return buf.toString();
        }

        return "";
    }

    /**
     * 联系人 备份/还原操作
     *
     * @author LW
     *
     */
    public static class ContactHandler {

        private static ContactHandler instance_ = new ContactHandler();

        /** 获取实例 */
        public static ContactHandler getInstance() {
            return instance_;
        }

        /**
         * 获取联系人指定信息
         *
         * @param projection
         *            指定要获取的列数组, 获取全部列则设置为null
         * @param idlist
         *            指定要获取的联系人的id, 获取全部列则设置为null
         * @return
         * @throws Exception
         */
        public Cursor queryContact(Activity context, String[] projection,
                                   List<String> idlist) {
            // 获取联系人的所需信息
            Cursor cur = null;
            if (idlist == null || idlist.size() == 0) {
                cur = context.getContentResolver().query(
                        ContactsContract.Contacts.CONTENT_URI, projection,
                        null, null, null);
            } else {
                cur = context.getContentResolver()
                        .query(ContactsContract.Contacts.CONTENT_URI,
                                projection, // NAME_RAW_CONTACT_ID
                                ContactsContract.Contacts._ID + " in("
                                        + idListToString(idlist, ',') + ")",
                                null, null);
            }
            return cur;
        }

        public Cursor queryContactRetry(Activity context, String[] projection,
                                        List<String> idlist) {
            // 获取联系人的所需信息
            Cursor cur = null;
            if (idlist == null || idlist.size() == 0) {
                cur = context.getContentResolver().query(
                        ContactsContract.Contacts.CONTENT_URI, projection,
                        null, null, null);
            } else {
                cur = context.getContentResolver()
                        .query(ContactsContract.Contacts.CONTENT_URI,
                                projection, // NAME_RAW_CONTACT_ID
                                "name_raw_contact_id" + " in("
                                        + idListToString(idlist, ',') + ")",
                                null, null);
            }
            return cur;
        }

        public static final String[] projectionContacts = {
                ContactsContract.Data.RAW_CONTACT_ID,
                ContactsContract.Data.MIMETYPE, ContactsContract.Data.DATA1,
                ContactsContract.Data.DATA2, ContactsContract.Data.DATA15 };
        public static final String selectionContacts = ContactsContract.Data.MIMETYPE
                + " in (?, ?, ?)";
        public static final String[] selectionArgsContacts = new String[] {
                StructuredName.CONTENT_ITEM_TYPE, Phone.CONTENT_ITEM_TYPE,
                Email.CONTENT_ITEM_TYPE };

        // FromData
        public List<PostCardUtil> getContactInfo(Activity context,
                                                 List<String> idlist) {
            List<PostCardUtil> infoList = new ArrayList<PostCardUtil>();

            Cursor cur = context.getContentResolver().query(
                    ContactsContract.Data.CONTENT_URI,
                    projectionContacts,
                    "raw_contact_id" + " in(" + idListToString(idlist, ',')
                            + ") AND " + selectionContacts,
                    selectionArgsContacts, "raw_contact_id DESC");
            if (cur != null) {
                int preContactId = -1;
                int curContactId = -1;
                PostCardUtil info = null;

                while (cur.moveToNext()) {
                    curContactId = (int) cur.getInt(0);
                    if (preContactId == -1 || curContactId != preContactId) {
                        if (info != null) {
                            infoList.add(info);
                        }
                        info = null;
                        info = new PostCardUtil("");
                    }
                    preContactId = curContactId;
                    String mimeType = cur.getString(1);
                    if (StructuredName.CONTENT_ITEM_TYPE.equals(mimeType)) {
                        info.name = cur.getString(2);
                    } else if (Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
                        PostCardUtil.PhoneInfo phoneInfo = new PostCardUtil.PhoneInfo();
                        phoneInfo.type = cur.getInt(3);
                        phoneInfo.number = cur.getString(2);
                        info.setPhone(phoneInfo);
                    } else if (Email.CONTENT_ITEM_TYPE.equals(mimeType)) {
                        PostCardUtil.EmailInfo emailInfo = new PostCardUtil.EmailInfo();
                        emailInfo.type = cur.getInt(3);
                        emailInfo.email = cur.getString(2);
                        info.setEmail(emailInfo);
                    }
                }
                if (info != null) {
                    infoList.add(info);
                }

                cur.close();
                cur = null;
            }

            return infoList;
        }

        /**
         * 获取联系人信息
         *
         * @param context
         * @param idlist
         *            指定要获取的联系人的id, 获取全部列则设置为null
         * @return
         */
        public List<PostCardUtil> getContactInfoOld(Activity context,
                                                    List<String> idlist) {
            List<PostCardUtil> infoList = new ArrayList<PostCardUtil>();

            Cursor cur = queryContactRetry(context, null, idlist);

            int idlength = idlist == null ? 0 : idlist.size();
            if (cur == null || cur.getCount() == 0 || cur.getCount() < idlength) {
                if (cur != null) {
                    cur.close();
                    cur = null;
                }
                cur = queryContact(context, null, idlist);
            }
            if (cur == null) {
                return null;
            }
            while (cur.moveToNext()) {
                // 获取联系人id号
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                // 获取联系人姓名
                String displayName = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                PostCardUtil info = new PostCardUtil(displayName);// 初始化联系人信息

                // 查看联系人有多少电话号码, 如果没有返回0
                int phoneCount = cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                if (phoneCount > 0) {

                    Cursor phonesCursor = context.getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                    + "=" + id, null, null);

                    if (phonesCursor.moveToFirst()) {
                        List<PostCardUtil.PhoneInfo> phoneNumberList = new ArrayList<PostCardUtil.PhoneInfo>();
                        do {
                            // 遍历所有电话号码
                            String phoneNumber = phonesCursor
                                    .getString(phonesCursor
                                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            // 对应的联系人类型
                            int type = phonesCursor
                                    .getInt(phonesCursor
                                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));

                            // 初始化联系人电话信息
                            PostCardUtil.PhoneInfo phoneInfo = new PostCardUtil.PhoneInfo();
                            phoneInfo.type = type;
                            phoneInfo.number = phoneNumber;
                            phoneNumberList.add(phoneInfo);

                        } while (phonesCursor.moveToNext());
                        // 设置联系人电话信息
                        info.setPhoneList(phoneNumberList);
                    }
                }

                // 获得联系人的EMAIL
                Cursor emailCur = context.getContentResolver().query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + "="
                                + id, null, null);

                if (emailCur.moveToFirst()) {
                    List<PostCardUtil.EmailInfo> emailList = new ArrayList<PostCardUtil.EmailInfo>();
                    do {
                        // 遍历所有的email
                        String email = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA1));
                        int type = emailCur.getInt(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));

                        // 初始化联系人邮箱信息
                        PostCardUtil.EmailInfo emailInfo = new PostCardUtil.EmailInfo();
                        emailInfo.type = type; // 设置邮箱类型
                        emailInfo.email = email; // 设置邮箱地址

                        emailList.add(emailInfo);
                    } while (emailCur.moveToNext());

                    info.setEmailList(emailList);
                }

                infoList.add(info);
            }

            cur.close();
            cur = null;
            return infoList;
        }

        /**
         * 备份联系人
         */
        public String backupContacts(Activity context, List<PostCardUtil> infos) {

            try {
                MedicalDaoImpl dao = new MedicalDaoImpl(context);
                String path = Environment.getExternalStorageDirectory()
                        + "/contacts.vcf";

                OutputStreamWriter writer = new OutputStreamWriter(
                        new FileOutputStream(path), "UTF-8");

                VCardComposer composer = new VCardComposer();
                List<ContactFriendBean> poList = new ArrayList<ContactFriendBean>();
                ContactFriendBean po;
                for (PostCardUtil info : infos) {
                    String phone = "";
                    poList.clear();
                    ContactStruct contact = new ContactStruct();
                    contact.name = info.getName();
                    if (TextUtils.isEmpty(contact.name)) {
                        contact.name = "unknown";
                    }
                    // 获取联系人Email信息, 添加至 ContactStruct
                    List<PostCardUtil.EmailInfo> emailList = info
                            .getEmailList();
                    for (PostCardUtil.EmailInfo emailInfo : emailList) {
                        // Log.v("miaolk", "emailInfo.type="+emailInfo.type);
                        // Log.v("miaolk", "emailInfo.email="+emailInfo.email);
                        if (emailInfo.type == 0) {
                            emailInfo.type = 2;
                        }
                        contact.addContactmethod(Contacts.KIND_EMAIL,
                                emailInfo.type, emailInfo.email, null, true);
                    }
                    List<PostCardUtil.PhoneInfo> numberList = info.getPhoneList();
                    for (PostCardUtil.PhoneInfo phoneInfo : numberList) {
                        if (phoneInfo.type == 0) {
                            phoneInfo.type = 2;
                        }
                        // 根据手机号码到应用数据库查询对应的视频号码
                        phone = phoneInfo.number;
                        phone = phone.replace(" ", "").replace("-", "");
                        if (phone.length() > 11) {
                            phone = phone.substring(phone.length() - 11);
                        }
                        po = dao.queryFriendInfoByPhone(phone);
                        if (po != null) {
                            poList.add(po);
                        }
                        contact.addPhone(phoneInfo.type, phone, null, true);
                    }
                    String vcardString = composer.createVCard(contact, VCardComposer.VERSION_VCARD30_INT);
                    if (!StringUtil.isEmpty(vcardString)) {
                        if (vcardString.contains("END:VCARD")) {
                            String resluts = "";
                            for (ContactFriendBean poItem : poList) {
                                resluts += ("X-VIDEO:" + poItem.getNubeNumber()
                                        + "_" + poItem.getSourcesId() + "\n");
                            }
                            if (!StringUtil.isEmpty(resluts)) {
                                resluts = resluts.substring(0,
                                        resluts.length() - 2);
                                vcardString = vcardString.replace("END:VCARD",
                                        resluts);
                                vcardString = vcardString + "END:VCARD";
                            }
                        }
                    }
                    writer.write(vcardString);
                    writer.write("\n");

                    writer.flush();
                }
                writer.close();

                return path;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (VCardException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "";
        }
        /**
         *
         * @author: qn-lihs
         * @Title: backupContacts
         * @Description:  vcard 文件路径
         * @param context
         * @param vcardMap
         * @return
         * @date: 2013-11-15 下午3:47:47
         */
        public String backupContacts(Context context,Map<String, ContactFriendBean> vcardMap){

            if (vcardMap == null) {
                return "";
            }
            OutputStreamWriter writer = null;
            String phone = "";
            String name= "";
            String nickName = "";
            String headUrl = "";
            try {
                MedicalDaoImpl dao = new MedicalDaoImpl(context);
                String path = Environment.getExternalStorageDirectory() + "/contacts.vcf";
                writer =  new OutputStreamWriter( new FileOutputStream(path), "UTF-8");
                ContactFriendBean po;
                Set<Map.Entry<String, ContactFriendBean>> set = vcardMap.entrySet();
                for (Iterator<Map.Entry<String, ContactFriendBean>> it = set.iterator(); it.hasNext();) {
                    Map.Entry<String, ContactFriendBean> entry = (Map.Entry<String, ContactFriendBean>) it.next();
                    po = entry.getValue();
                    StringBuffer vcard = new StringBuffer("BEGIN:VCARD\nVERSION:3.0");
                    // 姓名
                    name = po.getName();
                    vcard.append("\nN:").append(name);
                    name = name.replace(";", "");
                    vcard.append("\nFN:").append(name);
                    // 号码
                    phone = po.getNumber();
                    phone = phone.replace(" ", "").replace("-", "");
                    if (phone.length() > 0 && phone.startsWith("+86")) {
                        phone = phone.substring(3);
                    }
                    // 手机号
                    vcard.append("\nTEL:").append(phone);
                    //TODO 根据号码查询时候是视频好友(是否限制手机号)
                    po = dao.queryFriendInfoByPhone(phone);
                    if (po != null) {
                        // 传递视频号和 服务端联系人id
                        vcard.append("\nX-VIDEO:").append(po.getNubeNumber()+ "_" + po.getSourcesId());
                        // 传递 头像
                        headUrl = po.getHeadUrl();
                        if (!StringUtil.isEmpty(headUrl)) {
                            vcard.append("\nX-HEADURL:").append(headUrl);
                        }
                        // 传递昵称
                        nickName = po.getNickname();
                        if (!StringUtil.isEmpty(nickName)) {
                            vcard.append("\nX-NICKNAME:").append(nickName);
                        }
                    }
                    vcard.append("\nEND:VCARD\n");
                    String tempvcard = vcard.toString().replace("null", "");
                    writer.write(tempvcard);
                    writer.flush();
                }
                return path;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }  catch (IOException e) {
                e.printStackTrace();
            }finally{
                try {
                    if (writer != null) {
                        writer.close();
                    }
                } catch (Exception e2) {
                }
            }

            return "";

        }

        /**
         * 在应用的data\data\file目录下，以视频号建立vcf文件
         * @param context
         * @param nubeInfo  nube信息 （视频号必须要有）
         * @return
         */
        public static String createNubeVcf(Context context,
                                           ButelVcardBean nubeInfo) {

            if (context == null) {
                CustomLog.d("PostCardUtil"," context == null");
                return "";
            }

            if (nubeInfo == null) {
                CustomLog.d("PostCardUtil"," nubeInfo == null");
                return "";
            }

            if (TextUtils.isEmpty(nubeInfo.getNubeNumber())) {
                CustomLog.d("PostCardUtil"," number == null");
                return "";
            }

            OutputStreamWriter writer = null;
            String phone = "";
            String name = "";
            String nickName = "";
            String headUrl = "";
            String nubeNumber = nubeInfo.getNubeNumber();
            try {

                String path = "";
//				path = context.getFilesDir().getAbsolutePath()
//						+ File.separator + nubeNumber + ".vcf";
                path = FileTaskManager.getVCFDir()
                        + File.separator + nubeNumber + ".vcf";
                CustomLog.d("PostCardUtil","path="+path);
                writer = new OutputStreamWriter(new FileOutputStream(path),
                        "UTF-8");

                StringBuffer vcard = new StringBuffer(
                        "BEGIN:VCARD\nVERSION:3.0");
                // 姓名
                nickName = nubeInfo.getNickname();
                name = TextUtils.isEmpty(nickName)?nubeInfo.getNubeNumber():nickName;

                vcard.append("\nN:").append(name);
                name = name.replace(";", "");
                vcard.append("\nFN:").append(name);
                // 号码
                phone = nubeInfo.getPhoneNumber();
                phone = phone.replace(" ", "").replace("-", "");
                if (phone.length() > 0 && phone.startsWith("+86")) {
                    phone = phone.substring(3);
                }
                // 手机号
                vcard.append("\nTEL:").append(phone);

                // 传递视频号和 服务端联系人id
                vcard.append("\nX-VIDEO:").append(
                        nubeInfo.getNubeNumber() + "_" + nubeInfo.getUserId());
                // 传递 头像
                headUrl = nubeInfo.getHeadUrl();
                if (!StringUtil.isEmpty(headUrl)) {
                    vcard.append("\nX-HEADURL:").append(headUrl);
                }
                // 传递昵称
                nickName = nubeInfo.getNickname();
                if (!StringUtil.isEmpty(nickName)) {
                    vcard.append("\nX-NICKNAME:").append(nickName);
                }

                vcard.append("\nEND:VCARD\n");
                String tempvcard = vcard.toString().replace("null", "");
                writer.write(tempvcard);
                writer.flush();
                return path;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                CustomLog.d("PostCardUtil","exception="+e.getMessage());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                CustomLog.d("PostCardUtil","exception="+e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                CustomLog.d("PostCardUtil","exception="+e.getMessage());
            } finally {
                try {
                    if (writer != null) {
                        writer.close();
                        writer = null;
                    }
                } catch (Exception e2) {

                }
            }
            return "";
        }


        /**

         * 获取vCard文件中的联系人信息
         *
         * @return
         */
        public List<PostCardUtil> restoreContacts(String filepath) {
            List<PostCardUtil> contactInfoList = new ArrayList<PostCardUtil>();

            VCardParser parse = new VCardParser();
            VDataBuilder builder = new VDataBuilder();

            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(filepath), "UTF-8"));

                String vcardString = "";
                String line;
                while ((line = reader.readLine()) != null) {
                    vcardString += line + "\n";
                }
                reader.close();

                boolean parsed = parse.parse(vcardString, "UTF-8", builder);

                if (!parsed) {
                    throw new VCardException("Could not parse vCard file: "
                            + filepath);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (VCardException e) {
                e.printStackTrace();
            }

            List<VNode> pimContacts = builder.vNodeList;

            for (VNode contact : pimContacts) {

                ContactStruct contactStruct = ContactStruct
                        .constructContactFromVNode(contact, 1);
                // 获取备份文件中的联系人电话信息
                List<PhoneData> phoneDataList = contactStruct.phoneList;
                List<PostCardUtil.PhoneInfo> phoneInfoList = new ArrayList<PostCardUtil.PhoneInfo>();
                if (phoneDataList != null && phoneDataList.size() > 0) {
                    for (PhoneData phoneData : phoneDataList) {
                        PostCardUtil.PhoneInfo phoneInfo = new PostCardUtil.PhoneInfo();
                        phoneInfo.number = phoneData.data;
                        phoneInfo.type = phoneData.type;
                        phoneInfoList.add(phoneInfo);
                    }
                }


                // 获取备份文件中的联系人邮箱信息
                List<ContactMethod> emailList = contactStruct.contactmethodList;
                List<PostCardUtil.EmailInfo> emailInfoList = new ArrayList<PostCardUtil.EmailInfo>();
                // 存在 Email 信息
                if (null != emailList) {
                    for (ContactMethod contactMethod : emailList) {
                        if (Contacts.KIND_EMAIL == contactMethod.kind) {
                            PostCardUtil.EmailInfo emailInfo = new PostCardUtil.EmailInfo();
                            emailInfo.email = contactMethod.data;
                            emailInfo.type = contactMethod.type;
                            emailInfoList.add(emailInfo);
                        }
                    }
                }
                PostCardUtil info = new PostCardUtil(contactStruct.name)
                        .setPhoneList(phoneInfoList)
                        .setEmailList(emailInfoList);
                contactInfoList.add(info);
            }

            return contactInfoList;
        }


        /**
         * 向手机中录入联系人信息
         *
         * @param info
         *            要录入的联系人信息
         */
        public boolean  addContacts(Context context, PostCardUtil info) {
            ContentValues values = new ContentValues();
            try {


                // 首先向RawContacts.CONTENT_URI执行一个空值插入，目的是获取系统返回的rawContactId
                Uri rawContactUri = context.getContentResolver().insert(
                        RawContacts.CONTENT_URI, values);
                long rawContactId = ContentUris.parseId(rawContactUri);

                // 往data表入姓名数据
                values.clear();
                values.put(Data.RAW_CONTACT_ID, rawContactId);
                values.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
                values.put(StructuredName.GIVEN_NAME, info.getName());
                context.getContentResolver().insert(
                        android.provider.ContactsContract.Data.CONTENT_URI, values);

                // 获取联系人电话信息
                List<PostCardUtil.PhoneInfo> phoneList = info.getPhoneList();
                /** 录入联系电话 */
                for (PostCardUtil.PhoneInfo phoneInfo : phoneList) {
                    values.clear();
                    values.put(
                            android.provider.ContactsContract.Contacts.Data.RAW_CONTACT_ID,
                            rawContactId);
                    values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
                    // 设置录入联系人电话信息
                    values.put(Phone.NUMBER, phoneInfo.number);
                    values.put(Phone.TYPE, Phone.TYPE_MOBILE);
                    // 往data表入电话数据
                    context.getContentResolver().insert(
                            android.provider.ContactsContract.Data.CONTENT_URI,
                            values);
                }

                // 获取联系人邮箱信息
                List<PostCardUtil.EmailInfo> emailList = info.getEmailList();

                /** 录入联系人邮箱信息 */
                for (PostCardUtil.EmailInfo email : emailList) {
                    values.clear();
                    values.put(
                            android.provider.ContactsContract.Contacts.Data.RAW_CONTACT_ID,
                            rawContactId);
                    values.put(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
                    // 设置录入的邮箱信息
                    values.put(Email.DATA, email.email);
                    values.put(Email.TYPE, email.type);
                    // 往data表入Email数据
                    context.getContentResolver().insert(
                            android.provider.ContactsContract.Data.CONTENT_URI,
                            values);
                }
                return true;
            } catch (Exception e) {

            }
            return false;
        }

    }

    //TODO
    public boolean  addContactForvcard(Context context, PostCardUtil info) {
        ContentValues values = new ContentValues();
        try {


            // 首先向RawContacts.CONTENT_URI执行一个空值插入，目的是获取系统返回的rawContactId
            Uri rawContactUri = context.getContentResolver().insert(
                    RawContacts.CONTENT_URI, values);
            long rawContactId = ContentUris.parseId(rawContactUri);

            // 往data表入姓名数据
            values.clear();
            values.put(Data.RAW_CONTACT_ID, rawContactId);
            values.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
            values.put(StructuredName.GIVEN_NAME, info.getName());
            context.getContentResolver().insert(
                    android.provider.ContactsContract.Data.CONTENT_URI, values);

            // 获取联系人电话信息
            List<PostCardUtil.PhoneInfo> phoneList = info.getPhoneList();
            /** 录入联系电话 */
            for (PostCardUtil.PhoneInfo phoneInfo : phoneList) {
                values.clear();
                values.put(
                        android.provider.ContactsContract.Contacts.Data.RAW_CONTACT_ID,
                        rawContactId);
                values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
                // 设置录入联系人电话信息
                values.put(Phone.NUMBER, phoneInfo.number);
                values.put(Phone.TYPE, Phone.TYPE_MOBILE);
                // 往data表入电话数据
                context.getContentResolver().insert(
                        android.provider.ContactsContract.Data.CONTENT_URI,
                        values);
            }

            // 获取联系人邮箱信息
            List<PostCardUtil.EmailInfo> emailList = info.getEmailList();

            /** 录入联系人邮箱信息 */
            for (PostCardUtil.EmailInfo email : emailList) {
                values.clear();
                values.put(
                        android.provider.ContactsContract.Contacts.Data.RAW_CONTACT_ID,
                        rawContactId);
                values.put(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
                // 设置录入的邮箱信息
                values.put(Email.DATA, email.email);
                values.put(Email.TYPE, email.type);
                // 往data表入Email数据
                context.getContentResolver().insert(
                        android.provider.ContactsContract.Data.CONTENT_URI,
                        values);
            }
            return true;
        } catch (Exception e) {

        }
        return false;
    }

    /**
     * @author: lihs
     * @Title: createDetailVcardFile
     * @Description: Vcard file 挟带更多的联系人信息（手机号，姓名，邮箱）
     * @param context
     * @param vcardMap
     * @return
     * @date: 2014-1-8 下午7:57:44
     */
    private static String[] selColumn_data = { ContactsContract.Data._ID,
            ContactsContract.Data.MIMETYPE,
            ContactsContract.Data.RAW_CONTACT_ID, ContactsContract.Data.DATA1,
            ContactsContract.Data.DATA2, ContactsContract.Data.DATA3,
            ContactsContract.Data.DATA4, ContactsContract.Data.DATA5,
            ContactsContract.Data.DATA6, ContactsContract.Data.DATA7,
            ContactsContract.Data.DATA8, ContactsContract.Data.DATA9,
            ContactsContract.Data.DATA10, ContactsContract.Data.DATA11,
            ContactsContract.Data.DATA12, ContactsContract.Data.DATA13,
            ContactsContract.Data.DATA14, ContactsContract.Data.DATA15,
            ContactsContract.RawContacts.ACCOUNT_TYPE,
            ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY };

    private static final String VCARD_TAG = "vcard_tag";

    public static String createDetailVcardFile(Activity context,Map<String, ContactFriendBean> vcardMap){

        if ( vcardMap == null || vcardMap.size() == 0) {
            return "";
        }
        OutputStreamWriter writer = null;
        String name= "";
        Cursor cursorData = null;
        StringBuffer sb = null;
        ContactFriendBean po = null;
        boolean writename = true;
        try {
            MedicalDaoImpl dao = new MedicalDaoImpl(context);
            String path = VCardUtil.getSDPath()+ "/contacts.vcf";
            writer =  new OutputStreamWriter( new FileOutputStream(path), "UTF-8");
            Set<String> rawContactedSet = vcardMap.keySet();
            for (String contactedId : rawContactedSet) {
                CustomLog.d("PostCardUtil","contactedId=" + contactedId);
                cursorData = context.getContentResolver().query(
                        ContactsContract.Data.CONTENT_URI, selColumn_data,
                        ContactsContract.Data.RAW_CONTACT_ID + " = ?",
                        new String[] { contactedId }, null);

                sb = new StringBuffer("BEGIN:VCARD\nVERSION:3.0");
                writename = true;
                while (cursorData.moveToNext()) {
                    // 添加姓名
                    name = cursorData.getString(19);
                    if (writename) {
                        // 只写一次名字对象
                        writename = false;
                        sb.append("\nN:").append(name);
                        name = name.replace(";", "");
                        sb.append("\nFN:").append(name);
                    }
                    String mimeType = cursorData.getString(1);
                    if (Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
                        String number = cursorData.getString(3);
                        int type = cursorData.getInt(4);
                        String typeString = "TYPE=";
                        if (type == Phone.TYPE_CUSTOM) {
                            typeString = typeString + "X-"
                                    + cursorData.getString(5) + ":";
                        } else if (type == Phone.TYPE_HOME) {
                            typeString = typeString + "HOME:";
                        } else if (type == Phone.TYPE_MOBILE) {
                            typeString = typeString + "MOBILE:";
                        } else if (type == Phone.TYPE_WORK) {
                            typeString = typeString + "WORK:";
                        } else if (type == Phone.TYPE_COMPANY_MAIN) {
                            typeString = typeString + "COMPANY_MAIN:";
                        } else if (type == Phone.TYPE_FAX_WORK) {
                            typeString = typeString + "FAX_WORK:";
                        } else if (type == Phone.TYPE_FAX_HOME) {
                            typeString = typeString + "FAX_HOME:";
                        } else if (type == Phone.TYPE_CALLBACK) {
                            typeString = typeString + "CALLBACK:";
                        } else if (type == Phone.TYPE_OTHER) {
                            typeString = typeString + "OTHER:";
                        }
                        sb.append("\nTEL;").append(typeString).append(number);
                        //  根据号码查询时候是视频好友(是否限制手机号)
                        number = number.replace(" ", "").replace("-", "");
                        if (number.length() > 0 && number.startsWith("+86")) {
                            number = number.substring(3);
                        }
                        po = dao.queryFriendInfoByPhone(number);
                        if (po != null) {
                            // 传递视频号和 服务端联系人id
                            sb.append("\nX-VIDEO:").append(po.getNubeNumber()+ "_" + po.getSourcesId());
                            // 传递 头像
                            sb.append("\nX-HEADURL:").append(po.getHeadUrl());
                            // 传递昵称
                            sb.append("\nX-NICKNAME:").append(po.getNickname());
                        }else{
                            // 传递视频号和 服务端联系人id
                            sb.append("\nX-VIDEO:").append("");
                            // 传递 头像
                            sb.append("\nX-HEADURL:").append("");
                            // 传递昵称
                            sb.append("\nX-NICKNAME:").append("");
                        }

                    } else if (Email.CONTENT_ITEM_TYPE.equals(mimeType)) {
                        String number = cursorData.getString(3);
                        int type = cursorData.getInt(4);
                        String typeString = "TYPE=";
                        if (type == Email.TYPE_CUSTOM) {
                            typeString = typeString + "X-"
                                    + cursorData.getString(5) + ":";
                        } else if (type == Email.TYPE_WORK) {
                            typeString = typeString + "WORK:";
                        } else if (type == Email.TYPE_HOME) {
                            typeString = typeString + "HOME:";
                        } else  {
                            typeString = typeString + "OTHER:";
                        }
                        sb.append("\nEMAIL;").append(typeString)
                                .append(number);
                    }else if (Nickname.CONTENT_ITEM_TYPE.equals(mimeType)) {
                        String nickNames = cursorData.getString(3);
                        int type = cursorData.getInt(4);
                        String typeString = "TYPE=";
                        if (type == Nickname.TYPE_OTHER_NAME) {
                            typeString += "OTHER:";
                        }else if(type == Nickname.TYPE_MAIDEN_NAME){
                            typeString += "MAIDEN:";
                        }else if(type == Nickname.TYPE_SHORT_NAME){
                            typeString += "SHORT:";
                        }else if(type == Nickname.TYPE_CUSTOM){
                            typeString += "CUSTOM:";
                        }else {
                            typeString += "DEFAULT:";
                        }
                        sb.append("\nNICKNAME;").append(typeString)
                                .append(nickNames);

                    } else if (Im.CONTENT_ITEM_TYPE.equals(mimeType)) {
                        String imdata = cursorData.getString(3);
                        int type = cursorData.getInt(7);
                        String typeString = "TYPE=";
                        if (type == Im.PROTOCOL_CUSTOM) {
                            typeString = typeString + "X-"
                                    + cursorData.getString(8) + ":";
                        } else if (type == Im.PROTOCOL_AIM) {
                            typeString = typeString + "AIM:";
                        } else if (type == Im.PROTOCOL_QQ) {
                            typeString = typeString + "QQ:";
                        } else if (type == Im.PROTOCOL_MSN) {
                            typeString = typeString + "MSN:";
                        } else if (type == Im.PROTOCOL_YAHOO) {
                            typeString = typeString + "YAHOO:";
                        } else if (type == Im.PROTOCOL_SKYPE) {
                            typeString = typeString + "SKYPE:";
                        } else if (type == Im.PROTOCOL_GOOGLE_TALK) {
                            typeString = typeString + "GOOGLE_TALK:";
                        } else if (type == Im.PROTOCOL_ICQ) {
                            typeString = typeString + "ICQ:";
                        } else if (type == Im.PROTOCOL_JABBER) {
                            typeString = typeString + "JABBER:";
                        }
                        sb.append("\nIM;").append(typeString).append(imdata);
                    } else if (StructuredPostal.CONTENT_ITEM_TYPE
                            .equals(mimeType)) {
                        String address = cursorData.getString(3);
                        int type = cursorData.getInt(4);
                        String typeString = "TYPE=";
                        if (type == StructuredPostal.TYPE_CUSTOM) {
                            typeString = typeString + "X-"
                                    + cursorData.getString(5) + ":";
                        } else if (type == StructuredPostal.TYPE_HOME) {
                            typeString = typeString + "HOME:";
                        } else if (type == StructuredPostal.TYPE_WORK) {
                            typeString = typeString + "WORK:";
                        } else {
                            typeString = typeString + "OTHER:";
                        }
                        sb.append("\nADR;").append(typeString)
                                .append(address);
                    } else if (Note.CONTENT_ITEM_TYPE.equals(mimeType)) {
                        String note = cursorData.getString(3);
                        sb.append("\nNOTE:").append(note);
                    } else if (Organization.CONTENT_ITEM_TYPE.equals(mimeType)) {
                        String company = cursorData.getString(3);
                        String department = cursorData.getString(7);
                        String position = cursorData.getString(6);
                        String jobDesCription = cursorData.getString(8);
                        // 添加部门，单位
                        if (!TextUtils.isEmpty(company) || !TextUtils.isEmpty(department)) {
                            sb.append("\nORG:").append(company + ";" + department);
                        }
                        if (!TextUtils.isEmpty(position)) {
                            sb.append("\nTITLE:").append(position+";"+jobDesCription);
                        }
                    } else if (Event.CONTENT_ITEM_TYPE.equals(mimeType)) {
                        String event = cursorData.getString(3);
                        int type = cursorData.getInt(4);
                        String typeString = "TYPE=";
                        if (type == Event.TYPE_CUSTOM) {
                            typeString = typeString + "X-"
                                    + cursorData.getString(5) + ":";
                        } else if (type == Event.TYPE_BIRTHDAY) {
                            typeString = typeString + "BIRTHDAY:";
                        } else if (type == Event.TYPE_ANNIVERSARY) {
                            typeString = typeString + "ANNIVERSARY:";
                        } else {
                            typeString = typeString + "OTHER:";
                        }
                        sb.append("\nBDAY;").append(typeString).append(event);
                    }else if (Website.CONTENT_ITEM_TYPE.equals(mimeType)) {
                        String event = cursorData.getString(3);
                        int type = cursorData.getInt(4);
                        String typeString = "TYPE=";
                        if (type == Website.TYPE_CUSTOM) {
                            typeString = typeString + "X-"
                                    + cursorData.getString(5) + ":";
                        } else if (type == Website.TYPE_BLOG) {
                            typeString = typeString + "BLOG:";
                        } else if (type == Website.TYPE_FTP) {
                            typeString = typeString + "FTP:";
                        } else if (type == Website.TYPE_HOME) {
                            typeString = typeString + "HOME:";
                        } else if (type == Website.TYPE_HOMEPAGE) {
                            typeString = typeString + "HOMEPAGE:";
                        } else if (type == Website.TYPE_WORK) {
                            typeString = typeString + "WORK:";
                        } else   {
                            typeString = typeString + "OTHER:";
                        }
                        sb.append("\nURL;").append(typeString).append(event);
                    }
                }
                sb.append("\nEND:VCARD\n");
                String content = sb.toString().replace("null", "");
                CustomLog.d("PostCardUtil","名片分享发送的名片封装的内容："+content);
                // 防止添加过程中内存满了，所以写一条，添加一条
                writer.write(content);
            }
            return path;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }  catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception e2) {
            }
        }

        return "";
    }


    /**
     *
     * @author: qn-lihs
     * @Title: backupContacts
     * @Description:  vcard 文件路径
     * @param context
     * @param vcardMap
     * @return
     * @date: 2013-11-15 下午3:47:47
     */
    public static String backupContactsVcard(Activity context,Map<String, ContactFriendBean> vcardMap){

        if (vcardMap == null) {
            return "";
        }
        OutputStreamWriter writer = null;
        String phone = "";
        String name= "";
        String nickName = "";
        String headUrl = "";
        try {
            MedicalDaoImpl dao = new MedicalDaoImpl(context);
            String path = Environment.getExternalStorageDirectory() + "/contacts.vcf";
            writer =  new OutputStreamWriter( new FileOutputStream(path), "UTF-8");
            ContactFriendBean po;
            Set<Map.Entry<String, ContactFriendBean>> set = vcardMap.entrySet();
            for (Iterator<Map.Entry<String, ContactFriendBean>> it = set.iterator(); it.hasNext();) {
                Map.Entry<String, ContactFriendBean> entry = (Map.Entry<String, ContactFriendBean>) it.next();
                po = entry.getValue();
                StringBuffer vcard = new StringBuffer("BEGIN:VCARD\nVERSION:3.0");
                // 姓名
                name = po.getName();
                vcard.append("\nN:").append(name);
                name = name.replace(";", "");
                vcard.append("\nFN:").append(name);
                // 号码
                phone = po.getNumber();
                phone = phone.replace(" ", "").replace("-", "");
                if (phone.length() > 0 && phone.startsWith("+86")) {
                    phone = phone.substring(3);
                }
                // 手机号
                String typeString = "TYPE=MOBILE:";
                vcard.append("\nTEL;").append(typeString).append(phone);
                // 传递视频号和 服务端联系人id
                vcard.append("\nX-VIDEO:").append(po.getNubeNumber()+ "_" + po.getSourcesId());
                // 传递 头像
                vcard.append("\nX-HEADURL:").append(po.getHeadUrl());
                // 传递昵称
                vcard.append("\nX-NICKNAME:").append(po.getNickname());
                vcard.append("\nEND:VCARD\n");
                String tempvcard = vcard.toString().replace("null", "");
                writer.write(tempvcard);
                writer.flush();
            }
            return path;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }  catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception e2) {
            }
        }

        return "";

    }
}