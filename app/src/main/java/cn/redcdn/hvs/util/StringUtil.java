package cn.redcdn.hvs.util;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Desc
 * Created by wangkai on 2017/2/27.
 */

public class StringUtil {

    /**
     * 判断字符串是否为empty
     *
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        if (str == null || "".equals(str))
            return true;
        return false;
    }

    /**
     *
     * @author: zrp
     * @Title: isNumeric
     * @Description: 是否全数字
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        if (str.matches("\\d*")) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 获得32位的UUID
     * @return
     */
    public static String getUUID() {
        String s = UUID.randomUUID().toString();
        // 去掉“-”符号
        return s.substring(0, 8) + s.substring(9, 13) + s.substring(14, 18)
                + s.substring(19, 23) + s.substring(24);
    }

    /***
     * @Description: 将字符串数组用分隔符组成一个字符串
     * @param list   字符串数组
     * @param separator 分隔符
     * @return 组合后的字符串
     */
    public static String list2String(List<String> list, char separator){
        if(list==null||list.size()==0){
            return "";
        }
        StringBuffer buf = new StringBuffer();
        int i=0;
        int size = list.size();
        for(i=0; i<size-1;i++){
            buf.append(list.get(i));
            buf.append(separator);
        }
        buf.append(list.get(size-1));
        return buf.toString();
    }

    /**
     * 将字符串数组整合成SQL　WHERE　IN　条件中的字串
     * @param list 字符串数组
     * @return
     */
    public static String list2DBINString(List<String> list){
        if(list==null||list.size()==0){
            return "";
        }
        StringBuffer buf = new StringBuffer();
        int i=0;
        int size = list.size();
        for(i=0; i<size-1;i++){
            buf.append("'");
            buf.append(list.get(i));
            buf.append("',");
        }
        buf.append("'");
        buf.append(list.get(size-1));
        buf.append("'");
        return buf.toString();
    }

    public static String sortRecipentIds(String src, char separator){
        if(TextUtils.isEmpty(src)){
            return "";
        }

        String recipentIds = src;
        //增加排序功能
        if(src.contains(separator+"")){
            String[] tempIds = src.split(separator+"");
            if(tempIds!=null&&tempIds.length>1){
                List<String> idlist = new ArrayList<String>();
                for(int i=0;i<tempIds.length;i++){
                    String item = tempIds[i];
                    idlist.add(item);
                }
                Collections.sort(idlist);
                recipentIds = StringUtil.list2String(idlist, separator);
            }
        }

        return recipentIds;
    }

    /**
     * 判断字符串是否为empty
     *
     * @param str
     * @return
     */
    public static boolean isEmpty(Object str) {
        if (str == null || "".equals(str.toString()))
            return true;
        return false;
    }
}
