package cn.redcdn.hvs.im.util;


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
import java.util.List;

import a_vcard.android.syncml.pim.PropertyNode;
import a_vcard.android.syncml.pim.VDataBuilder;
import a_vcard.android.syncml.pim.VNode;
import a_vcard.android.syncml.pim.vcard.ContactStruct;
import a_vcard.android.syncml.pim.vcard.VCardComposer;
import a_vcard.android.syncml.pim.vcard.VCardException;
import a_vcard.android.syncml.pim.vcard.VCardParser;
import cn.redcdn.hvs.database.DBConstant;
import cn.redcdn.hvs.im.bean.ContactFriendBean;
import cn.redcdn.log.CustomLog;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;


/**
 * <dl>
 * <dt>VCardUtil.java</dt>
 * <dd>Description:VCard工具栏，提供Vcard生成和读取</dd>
 * <dd>Copyright: Copyright (C) 2011</dd>
 * <dd>Company: 安徽青牛信息技术有限公司</dd>
 * <dd>CreateDate: 2013-8-9 下午3:05:49</dd>
 * </dl>
 *
 * @author sunkai
 */
public class VCardUtil {

    /**
     * @author: sunkai
     * @Title: createVCF
     * @Description: 保存VCard到sdcard根目录下，名称为：MyInformation.vcf
     * @param context
     * @param linkmanDetailList
     * @return
     * @date: 2013-8-9 下午3:07:03
     */
    public static String createVCF(Context context,
                                   List<ContactFriendBean> linkmanDetailList) {  // 暂时废弃，有问题（没有维护，若要使用，请联系维护人员）
        //检测sdcard内存
        StatFs statfs = new StatFs(getSDPath());
        long blockSize = statfs.getBlockSize();
        long availableBlocks = statfs.getAvailableBlocks();
        long availaSize = availableBlocks * blockSize;
        if (availaSize < 5 * 1024) {
            //Toast.makeText(context, "空间不足", Toast.LENGTH_SHORT).show();
            CustomLog.d("VCardUtil","customCreateVCF 空间不足 5 * 1024");
            return "";
        }
        OutputStreamWriter writer;
        String VcfName = "MyInformation.vcf";
        File file = new File(getSDPath(), VcfName);
        if(file!=null&&file.exists()){
            file.delete();
        }
        try {
            file.createNewFile();
            writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            VCardComposer composer;
            ContactStruct contact;
            StringBuffer vcardString = new StringBuffer();
            for(ContactFriendBean po :linkmanDetailList){
                composer = new VCardComposer();
                contact = new ContactStruct();
                contact.name = po.getName();
                contact.addPhone(Phone.TYPE_MOBILE, po.getNubeNumber(), null,
                        true);
                vcardString.append(composer.createVCard(contact,
                        VCardComposer.VERSION_VCARD30_INT));
            }

            // 生成VCard
            writer.write(vcardString.toString());
            writer.close();
            return file.getPath();
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
     * @author: sunkai
     * @Title: getSDPath
     * @Description: 获取sdcard路径
     * @return
     * @date: 2013-8-9 下午3:12:03
     */
    public static String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
        }else{
            sdDir = new File(DBConstant.VCF_FILE_ROM_FOLDER);
            if(!sdDir.exists()){
                sdDir.mkdirs();
            }else{// 12.30号每次清空上次生成的内存文件
                if(sdDir.isDirectory()){
                    for(File file :sdDir.listFiles()){
                        if(file != null){
                            file.delete();
                        }
                    }
                }
            }
        }
        return sdDir.toString();

    }

    /**
     * @author: sunkai
     * @Title: readVCard
     * @Description: 读取VCard信息，并封装成list返回
     * @param file
     * @return
     * @date: 2013-8-9 下午3:12:55
     */
    public static List<ContactFriendBean> readVCard(String file) {
        List<ContactFriendBean> linkManList = new ArrayList<ContactFriendBean>();
        VCardParser parser = new VCardParser();
        VDataBuilder builder = new VDataBuilder();
        String vcardString = "";
        String line;
        // 读文件
        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file), "UTF-8"));
            while ((line = reader.readLine()) != null) {
                vcardString += line + "\n";
            }
            reader.close();
            // 检测是否能转换成VCard
            boolean parsed = parser.parse(vcardString, "UTF-8", builder);
            if (!parsed) {
                throw new VCardException("Could not parse vCard file: " + file);
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

        // 获得VCard中的联系人数据
        List<VNode> pimContacts = builder.vNodeList;

        // 封装到list中
        ContactFriendBean friend;
        for (VNode contact : pimContacts) {
            ArrayList<PropertyNode> props = contact.propList;
            String name = null;
            String phone =null;
            String video = null;
            String headUrl = null;
            String nickName = null;
            friend = new ContactFriendBean();
            for (PropertyNode prop : props) {
                if ("FN".equals(prop.propName)) {
                    name = prop.propValue;
                }else if("TEL".equals(prop.propName)){
                    phone = prop.propValue;
                }
            }
            friend.setNubeNumber(phone);
            linkManList.add(friend);
        }
        return linkManList;
    }

    public static List<ContactFriendBean> readSendVCard(String file) {

        List<ContactFriendBean> linkManList = new ArrayList<ContactFriendBean>();
        VCardParser parser = new VCardParser();
        VDataBuilder builder = new VDataBuilder();
        String vcardString = "";
        String line;
        // 读文件
        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file), "UTF-8"));
            while ((line = reader.readLine()) != null) {
                vcardString += line + "\n";
            }
            reader.close();
            // 检测是否能转换成VCard
            boolean parsed = parser.parse(vcardString, "UTF-8", builder);
            if (!parsed) {
                throw new VCardException("Could not parse vCard file: " + file);
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

        // 获得VCard中的联系人数据
        List<VNode> pimContacts = builder.vNodeList;
        // 封装到list中
        ContactFriendBean friend;
        for (VNode contact : pimContacts) {
            ArrayList<PropertyNode> props = contact.propList;
            String name = null;
            String phone =null;
            String video = null;
            String headUrl = null;
            String nickName = null;
            friend = new ContactFriendBean();
            for (PropertyNode prop : props) {
                if ("FN".equals(prop.propName)) {
                    name = prop.propValue;
                }else if("TEL".equals(prop.propName)){
                    phone = prop.propValue;
                }else if ("X-VIDEO".equals(prop.propName)) {
                    video = prop.propValue;
                }else if ("X-HEADURL".equals(prop.propName)) {
                    headUrl = prop.propValue;
                }else if ("X-NICKNAME".equals(prop.propName)) {
                    nickName = prop.propValue;
                }
            }
            friend.setHeadUrl(headUrl);
            friend.setNickname(nickName);
            friend.setName(name);
            friend.setNumber(phone);
            friend.setNubeNumber(video);// 视频号+"_"+sourceid
            linkManList.add(friend);
        }
        return linkManList;
    }

    public static List<ContactFriendBean> readMoreDetailVCard(String file) {
        CustomLog.d("VCardUtil"," begin file:" + file);

        List<ContactFriendBean> linkManList = new ArrayList<ContactFriendBean>();
        VCardParser parser = new VCardParser();
        VDataBuilder builder = new VDataBuilder();

        ContactFriendBean.PhoneInfo phoneItem = null;// 联系号码

        ContactFriendBean.EmailInfo emailItem = null; // Email

        ContactFriendBean.ContactAddressInfo addressInfo = null;//地址

        ContactFriendBean.ContactBirthdayInfo birthdayInfo = null;

        ContactFriendBean.ContactIMInfo contactIMinfo = null;

        ContactFriendBean.WebSiteInfo webSiteInfo = null;

        ContactFriendBean.SystemContactNickname nickNameInfo = null;

        String vcardString = "";
        String line;
        // 读文件
        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file), "UTF-8"));
            while ((line = reader.readLine()) != null) {
                vcardString += line + "\n";
            }
            reader.close();
            // 检测是否能转换成VCard
            boolean parsed = parser.parse(vcardString, "UTF-8", builder);
            if (!parsed) {
                throw new VCardException("Could not parse vCard file: " + file);
            }

        } catch (UnsupportedEncodingException e) {
            CustomLog.e("VCardUtil","UnsupportedEncodingException" + e.toString());
        } catch (FileNotFoundException e) {
            CustomLog.e("VCardUtil","FileNotFoundException" + e.toString());
        } catch (IOException e) {
            CustomLog.e("VCardUtil","IOException" + e.toString());
        } catch (VCardException e) {
            CustomLog.e("VCardUtil","VCardException" + e.toString());
        }

        // 获得VCard中的联系人数据
        List<VNode> pimContacts = builder.vNodeList;

        // 封装到list中
        ContactFriendBean friend;
        for (VNode contact : pimContacts) {

            ArrayList<PropertyNode> props = contact.propList;
            String name = null;
            String phone = null;
            String video = null;
            String headUrl = null;
            String nickName = null;

            friend = new ContactFriendBean();

            for (PropertyNode prop : props) {
                if ("FN".equals(prop.propName)) {
                    name = prop.propValue;
                    friend.setName(name);
                } else if ("TEL".equals(prop.propName)) {
                    // 排除号码 +86 和 -
                    phone = prop.propValue;
                    prop.propValue = prop.propValue.replace("-", "").replace(
                            " ", "");
                    if (prop.propValue.indexOf("+86") == 0) {
                        prop.propValue = prop.propValue.substring(3,
                                prop.propValue.length());
                    }
                    phone = prop.propValue;
                    int type = -1;
                    // String label = null;
                    // boolean isCustom = false;
                    for (String typeString : prop.paramMap_TYPE) {
                        if (typeString.equals("PREF")) {

                        } else if (typeString.equalsIgnoreCase("HOME")) {
                            type = Phone.TYPE_HOME;
                        } else if (typeString.equalsIgnoreCase("WORK")) {
                            type = Phone.TYPE_WORK;
                        } else if (typeString.equalsIgnoreCase("MOBILE")
                                || typeString.equalsIgnoreCase("CELL")) {
                            type = Phone.TYPE_MOBILE;
                        } else if (typeString.equalsIgnoreCase("COMPANY_MAIN")) {
                            type = Phone.TYPE_COMPANY_MAIN;
                        } else if (typeString.equalsIgnoreCase("FAX_WORK")) {
                            type = Phone.TYPE_FAX_WORK;
                        } else if (typeString.equalsIgnoreCase("FAX_HOME")) {
                            type = Phone.TYPE_FAX_HOME;
                        } else if (typeString.equalsIgnoreCase("CALLBACK")) {
                            type = Phone.TYPE_CALLBACK;
                        } else if (typeString.equalsIgnoreCase("OTHER")) {
                            type = Phone.TYPE_OTHER;
                        } else if (typeString.toUpperCase().startsWith("X-")) {
                            type = Phone.TYPE_CUSTOM;
                        } else if (type < 0) {
                            type = Phone.TYPE_CUSTOM;
                        }
                    }
                    // moren wei shouji leixing
                    if (type < 0) {
                        type = Phone.TYPE_MOBILE;
                    }
                    phoneItem = new ContactFriendBean.PhoneInfo();
                    phoneItem.number = phone;
                    phoneItem.type = type;
                    friend.getPhoneList().add(phoneItem);

                }else if ("EMAIL".equals(prop.propName)) {
                    String email = prop.propValue;
                    int type = -1;
                    for (String typeString : prop.paramMap_TYPE) {
                        if (typeString.equalsIgnoreCase("PREF")) {
                        } else if (typeString.equalsIgnoreCase("HOME")) {
                            type = Email.TYPE_HOME;
                        } else if (typeString.equalsIgnoreCase("WORK")) {
                            type = Email.TYPE_WORK;
                        } else if (typeString.equalsIgnoreCase("OTHER")) {
                            type = Email.TYPE_OTHER;
                        } else if (typeString.equalsIgnoreCase("CELL")) {
                            type = Email.TYPE_CUSTOM;
                        } else if (typeString.toUpperCase().startsWith("X-")) {
                            type = Email.TYPE_CUSTOM;
                        } else if (type < 0) {
                            type = Email.TYPE_CUSTOM;
                        }
                    }
                    // 默认为工作邮箱
                    if (type < 0) {
                        type = Email.TYPE_WORK;
                    }
                    emailItem = new ContactFriendBean.EmailInfo();
                    emailItem.email = email;
                    emailItem.type = type;
                    friend.getEmailList().add(emailItem);

                } else if ("X-VIDEO".equals(prop.propName)) {
                    video = prop.propValue;
                    friend.getVideoList().add(video);
                } else if ("X-HEADURL".equals(prop.propName)) {
                    headUrl = prop.propValue;
                    friend.getHeadUrlList().add(headUrl);
                } else if ("X-NICKNAME".equals(prop.propName)) {
                    nickName = prop.propValue;
                    friend.getNickNameList().add(nickName);
                }else if ("NOTE".equals(prop.propName)) {
                    String note = prop.propValue;
                    friend.setNote(note);
                }else if ("TITLE".equals(prop.propName)) {
                    String principalship = prop.propValue;
                    friend.setTitle(principalship);
                } else if ("ORG".equals(prop.propName)) {
                    String org = prop.propValue;
                    friend.setOrgNisation(org);
                }else if ("NICKNAME".equals(prop.propName)) {
                    String nickName1 = prop.propValue;
                    //TODO NICKnAME
                    int type = -1;
                    for (String typeString : prop.paramMap_TYPE) {
                        if (typeString.equalsIgnoreCase("DEFAULT")) {
                        } else if (typeString.equalsIgnoreCase("MAIDEN")) {
                            type = Nickname.TYPE_MAIDEN_NAME;
                        } else if (typeString.equalsIgnoreCase("SHORT")) {
                            type = Nickname.TYPE_SHORT_NAME;
                        } else if (typeString.equalsIgnoreCase("OTHER")) {
                            type = Nickname.TYPE_OTHER_NAME;
                        }else {
                            type =Nickname.TYPE_CUSTOM;
                        }
                    }
                    nickNameInfo = new ContactFriendBean.SystemContactNickname();
                    nickNameInfo.nickNameItem = nickName1;
                    nickNameInfo.type = type;
                    friend.getSystemContactNickname().add(nickNameInfo);

                }else if ("ADR".equals(prop.propName)) {
                    String address = prop.propValue;
                    int type = -1;
                    for (String typeString : prop.paramMap_TYPE) {
                        if (typeString.equalsIgnoreCase("PREF")) {
                        } else if (typeString.equalsIgnoreCase("HOME")) {
                            type = StructuredPostal.TYPE_HOME;
                        } else if (typeString.equalsIgnoreCase("WORK")) {
                            type = StructuredPostal.TYPE_WORK;
                        } else if (typeString.equalsIgnoreCase("OTHER")) {
                            type = StructuredPostal.TYPE_OTHER;
                        } else if (typeString.toUpperCase().startsWith("X-")) {
                            type = StructuredPostal.TYPE_CUSTOM;
                        } else if (type < 0) {
                            type = StructuredPostal.TYPE_CUSTOM;
                        }
                    }
                    if (type < 0) {
                        type = StructuredPostal.TYPE_HOME;
                    }
                    addressInfo = new ContactFriendBean.ContactAddressInfo();
                    addressInfo.type = type;
                    addressInfo.address = address;
                    friend.getAddressInfo().add(addressInfo);

                } else if ("BDAY".equals(prop.propName)) {
                    String time = prop.propValue;
                    int type = -1;
                    for (String typeString : prop.paramMap_TYPE) {
                        if (typeString.equalsIgnoreCase("BIRTHDAY")) {
                            type = Event.TYPE_BIRTHDAY;
                        } else if (typeString.equalsIgnoreCase("ANNIVERSARY")) {
                            type = Event.TYPE_ANNIVERSARY;
                        } else if (typeString.equalsIgnoreCase("OTHER")) {
                            type = Event.TYPE_OTHER;
                        } else if (typeString.toUpperCase().startsWith("X-")) {
                            type = StructuredPostal.TYPE_CUSTOM;
                        } else if (type < 0) {
                            type = StructuredPostal.TYPE_CUSTOM;
                        }
                        birthdayInfo = new ContactFriendBean.ContactBirthdayInfo();
                        birthdayInfo.type = type;
                        birthdayInfo.birthday = time;
                        friend.getBirthdayInfo().add(birthdayInfo);
                    }

                } else if ("IM".equals(prop.propName)) {
                    String imnumber = prop.propValue;
                    int type = -2;
                    for (String typeString : prop.paramMap_TYPE) {
                        if (typeString.equalsIgnoreCase("AIM")) {
                            type = Im.PROTOCOL_AIM;
                        } else if (typeString.equalsIgnoreCase("QQ")) {
                            type = Im.PROTOCOL_QQ;
                        } else if (typeString.equalsIgnoreCase("MSN")) {
                            type = Im.PROTOCOL_MSN;
                        } else if (typeString.equalsIgnoreCase("YAHOO")) {
                            type = Im.PROTOCOL_YAHOO;
                        } else if (typeString.equalsIgnoreCase("SKYPE")) {
                            type = Im.PROTOCOL_SKYPE;
                        } else if (typeString.equalsIgnoreCase("GOOGLE_TALK")) {
                            type = Im.PROTOCOL_GOOGLE_TALK;
                        } else if (typeString.equalsIgnoreCase("ICQ")) {
                            type = Im.PROTOCOL_ICQ;
                        } else if (typeString.equalsIgnoreCase("JABBER")) {
                            type = Im.PROTOCOL_JABBER;
                        } else if (typeString.toUpperCase().startsWith("X-")) {
                            type = StructuredPostal.TYPE_CUSTOM;
                        } else if (type < -1) {
                            type = StructuredPostal.TYPE_CUSTOM;
                        }
                        contactIMinfo = new ContactFriendBean.ContactIMInfo();
                        contactIMinfo.type = type;
                        contactIMinfo.contactIm = imnumber;
                        friend.getcontactImInfo().add(contactIMinfo);
                    }
                }else if ("URL".equals(prop.propName)) {
                    String url = prop.propValue;
                    int type = -2;
                    for (String typeString : prop.paramMap_TYPE) {
                        if (typeString.equalsIgnoreCase("HOME")) {
                            type = Website.TYPE_HOME;
                        } else if (typeString.equalsIgnoreCase("HOMEPAGE")) {
                            type = Website.TYPE_HOMEPAGE;
                        } else if (typeString.equalsIgnoreCase("WORK")) {
                            type = Website.TYPE_WORK;
                        } else if (typeString.equalsIgnoreCase("BLOG")) {
                            type = Website.TYPE_BLOG;
                        }  else if (typeString.equalsIgnoreCase("FTP")) {
                            type = Website.TYPE_FTP;
                        }else{
                            type = Website.TYPE_OTHER;
                        }
                        webSiteInfo = new ContactFriendBean.WebSiteInfo();
                        webSiteInfo.type = type;
                        webSiteInfo.webSiteItem = url;
                        friend.getWebsiteInfo().add(webSiteInfo);
                    }
                }
            }
            linkManList.add(friend);
        }
        return linkManList;
    }
    //=================================================================

    public static String customCreateVCF(Context context, String name,
                                         String number, String nubeId) {
        //检测sdcard内存
        String filePath = getSDPath();
        StatFs statfs = new StatFs(filePath);
        long blockSize = statfs.getBlockSize();
        long availableBlocks = statfs.getAvailableBlocks();
        long availaSize = availableBlocks * blockSize;
        if (availaSize < 5 * 1024) {
            //Toast.makeText(context, "空间不足", Toast.LENGTH_SHORT).show();
            CustomLog.d("VCardUtil","customCreateVCF 空间不足 5 * 1024");
            return "";
        }
        OutputStreamWriter writer;
        String VcfName = "MyInformation.vcf";
        File file = new File(filePath, VcfName);
        if(file!=null&&file.exists()){
            file.delete();
        }
        try {
            file.createNewFile();
            writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");

            StringBuffer s = new StringBuffer("");
            s.append("BEGIN:VCARD\nVERSION:3.0");
            s.append("\nN:").append(name);
            name = name.replace(";", "");
            s.append("\nFN:").append(name);
            s.append("\nX-VIDEO:").append(number + "_" + nubeId);
            s.append("\nEND:VCARD\n");
            String str = s.toString().replace("null", "");
            writer.write(str);
            writer.flush();
            writer.close();

            return file.getPath();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static List<ContactFriendBean> customReadVCard(String file) {
        List<ContactFriendBean> linkManList = new ArrayList<ContactFriendBean>();
        VCardParser parser = new VCardParser();
        VDataBuilder builder = new VDataBuilder();
        String vcardString = "";
        String line;
        // 读文件
        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file), "UTF-8"));
            while ((line = reader.readLine()) != null) {
                vcardString += line + "\n";
            }
            reader.close();
            // 检测是否能转换成VCard
            boolean parsed = parser.parse(vcardString, "UTF-8", builder);
            if (!parsed) {
                throw new VCardException("Could not parse vCard file: " + file);
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

        // 获得VCard中的联系人数据
        List<VNode> pimContacts = builder.vNodeList;

        // 封装到list中
        ContactFriendBean friend;
        for (VNode contact : pimContacts) {
            ArrayList<PropertyNode> props = contact.propList;
            String name = null;
            String phone =null;
            String uid =null;
            friend = new ContactFriendBean();
            for (PropertyNode prop : props) {
                if ("FN".equals(prop.propName)) {
                    name = prop.propValue;
                }else if("X-VIDEO".equals(prop.propName)){
                    int index = prop.propValue.indexOf("_");
                    if(index!=-1){
                        phone = prop.propValue.substring(0, index);
                        uid = prop.propValue.substring(index+1);
                    }else{
                        continue;
                    }
                }
            }
            friend.setName(name);
            friend.setNubeNumber(phone);
            friend.setUid(uid);
            linkManList.add(friend);
        }
        return linkManList;
    }
}

