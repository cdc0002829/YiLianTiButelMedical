package cn.redcdn.hvs.im.column; /**
 * @Title: NewFriendTable.java
 * @Package com.channelsoft.netphone.column
 * @author miaolikui
 * @date 2013-8-27 上午10:47:02
 * @version V1.0
 */

import android.content.ContentValues;
import android.database.Cursor;
import cn.redcdn.hvs.im.bean.NewFriendBean;

/**
 * @ClassName: NewFriendTable
 * @Description:
 * @author miaolikui
 * @date 2013-8-27 上午10:47:02
 *
 */
public class NewFriendTable {
    public static String TABLENAME="t_newfriend";

    public static String NEWFRIEND_COLUMN_ID="id";
    public static String NEWFRIEND_COLUMN_LASTTIME="lastTime";
    public static String NEWFRIEND_COLUMN_NAME="name";
    public static String NEWFRIEND_COLUMN_NUMBER="number";
    public static String NEWFRIEND_COLUMN_NUBENUMBER="nubeNumber";
    public static String NEWFRIEND_COLUMN_STATUS="status";
    public static String NEWFRIEND_COLUMN_ISNEW="isNews";
    public static String NEWFRIEND_COLUMN_CONTACTUSERID="contactUserId";
    public static String NEWFRIEND_COLUMN_HEADURL = "headUrl";
    public static String NEWFRIEND_COLUMN_REALNAME = "realName";
    public static String NEWFRIEND_COLUMN_VISIBLE = "visible";
    public static String NEWFRIEND_COLUMN_SEX = "sex"; // 性别 0：未知，1：男，2：女(同联系人人中定义)

    public static final String RESERVESTR1 = "reserveStr1"; // 扩展保留字段1
    public static final String RESERVESTR2 = "reserveStr2"; // 扩展保留字段2
    public static final String RESERVESTR3 = "reserveStr3"; // 扩展保留字段3
    public static final String RESERVESTR4 = "reserveStr4"; // 扩展保留字段4
    public static final String RESERVESTR5 = "reserveStr5"; // 扩展保留字段5


    public static ContentValues makeContentValue(NewFriendBean bean){
        if(bean==null){
            return null;
        }
        ContentValues value = new ContentValues();
        value.put(NEWFRIEND_COLUMN_ID, bean.getId());
        value.put(NEWFRIEND_COLUMN_LASTTIME, bean.getLastTime());
        value.put(NEWFRIEND_COLUMN_NAME, bean.getName());
        value.put(NEWFRIEND_COLUMN_NUMBER, bean.getNumber());
        value.put(NEWFRIEND_COLUMN_NUBENUMBER, bean.getNubeNumber());
        value.put(NEWFRIEND_COLUMN_STATUS, bean.getStatus());
        value.put(NEWFRIEND_COLUMN_ISNEW, bean.getIsNews());
        value.put(NEWFRIEND_COLUMN_CONTACTUSERID, bean.getContactUserId());
        value.put(NEWFRIEND_COLUMN_HEADURL, bean.getHeadUrl());
        value.put(NEWFRIEND_COLUMN_REALNAME, bean.getRealName());
        value.put(NEWFRIEND_COLUMN_VISIBLE, bean.getVisible());
        value.put(NEWFRIEND_COLUMN_SEX, bean.getSex());

        return value;
    }

    public static NewFriendBean pureCursor(Cursor cursor){
        if(cursor==null||cursor.isClosed()){
            return null;
        }
        NewFriendBean bean = new NewFriendBean();
        bean.setId(cursor.getString(cursor.getColumnIndexOrThrow(NEWFRIEND_COLUMN_ID)));
        bean.setLastTime(cursor.getString(cursor.getColumnIndexOrThrow(NEWFRIEND_COLUMN_LASTTIME)));
        bean.setName(cursor.getString(cursor.getColumnIndexOrThrow(NEWFRIEND_COLUMN_NAME)));
        bean.setNumber(cursor.getString(cursor.getColumnIndexOrThrow(NEWFRIEND_COLUMN_NUMBER)));
        bean.setNubeNumber(cursor.getString(cursor.getColumnIndexOrThrow(NEWFRIEND_COLUMN_NUBENUMBER)));
        bean.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow(NEWFRIEND_COLUMN_STATUS)));
        bean.setIsNews(cursor.getInt(cursor.getColumnIndexOrThrow(NEWFRIEND_COLUMN_ISNEW)));
        bean.setVisible(cursor.getInt(cursor.getColumnIndexOrThrow(NEWFRIEND_COLUMN_VISIBLE)));
        bean.setContactUserId(cursor.getString(cursor.getColumnIndexOrThrow(NEWFRIEND_COLUMN_CONTACTUSERID)));
        bean.setHeadUrl(cursor.getString(cursor.getColumnIndexOrThrow(NEWFRIEND_COLUMN_HEADURL)));
        bean.setRealName(cursor.getString(cursor.getColumnIndexOrThrow(NEWFRIEND_COLUMN_REALNAME)));
        bean.setSex(cursor.getString(cursor.getColumnIndexOrThrow(NEWFRIEND_COLUMN_SEX)));

        return bean;
    }
}
