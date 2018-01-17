package cn.redcdn.hvs.im.util;


import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;

import cn.redcdn.hvs.util.StringUtil;


/**
 * @className:PinyingUtil.java
 * @classDescription:拼音操作工具类
 * @author:xiayingjie
 * @createTime:2010-10-21
 */

public class PinyinUtil {

    /**
     * 将汉字转换为全拼
     *
     * @param src
     * @return String
     */
    public static String getPinYin(String src) {
        char[] t1 = null;
        t1 = src.toCharArray();
        String[] t2 = new String[t1.length];
        // 设置汉字拼音输出的格式
        HanyuPinyinOutputFormat t3 = new HanyuPinyinOutputFormat();
        t3.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        t3.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        t3.setVCharType(HanyuPinyinVCharType.WITH_V);
        String t4 = "";
        int t0 = t1.length;
        try {
            for (int i = 0; i < t0; i++) {
                // 判断能否为汉字字符
                if (Character.toString(t1[i]).matches("[\\u4E00-\\u9FA5]+")) {
                    t2 = PinyinHelper.toHanyuPinyinStringArray(t1[i], t3);// 将汉字的几种全拼都存到t2数组中
                    if (t2 != null) {
                        t4 += t2[0];// 取出该汉字全拼的第一种读音并连接到字符串t4后
                    }
                } else {
                    // 如果不是汉字字符，间接取出字符并连接到字符串t4后
                    t4 += Character.toString(t1[i]);
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
        }
        return t4;
    }
    /**
     * @author: chuwx
     * @Title: getSimplePinYin
     * @Description:简拼
     * @param src
     * @return
     * @date: 2013-7-9 上午10:57:47
     */
    public static String getSimplePinYin(String src){
        if(StringUtil.isEmpty(src)) return "";
        StringBuilder builder = new StringBuilder("");
        String shortPY;
        for (int j = 0; j < src.length(); j++) {
            shortPY = PinyinUtil.getPinYin(src.substring(j,j + 1));
            if (!"".equals(shortPY)) {
                builder.append(shortPY.toLowerCase().substring(0, 1));
            }
        }
        if(builder != null) return builder.toString().trim();
        return "";
    }

    /**
     * 提取每个汉字的首字母
     *
     * @param str
     * @return String
     */
    public static String getPinYinHeadChar(String str) {
        String convert = "";
        for (int j = 0; j < str.length(); j++) {
            char word = str.charAt(j);
            // 提取汉字的首字母
            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word);
            if (pinyinArray != null) {
                convert += pinyinArray[0].charAt(0);
            } else {
                convert += word;
            }
        }
        return convert;
    }

    /**
     * 将字符串转换成ASCII码
     *
     * @param cnStr
     * @return String
     */
    public static String getCnASCII(String cnStr) {
        StringBuffer strBuf = new StringBuffer();
        // 将字符串转换成字节序列
        byte[] bGBK = cnStr.getBytes();
        for (int i = 0; i < bGBK.length; i++) {
            // 将每个字符转换成ASCII码
            strBuf.append(Integer.toHexString(bGBK[i] & 0xff));
        }
        return strBuf.toString();
    }

    public static void main(String[] args) {
    }

    /**
     *
     * @author: zrp
     * @Title: getNumberStrByLetStr
     * @Description: 得到拼音对应的数字键
     * @param letStr
     * @return
     * @date: 2012-10-30 上午11:38:10
     */
    public static String getNumberStrByLetStr(String letStr) {
        String number = "";

        for (int i = 0; i < letStr.length(); i++) {
            number += getNumberByChar(letStr.charAt(i));
        }

        return number;
    }

    private static String getNumberByChar(char letter) {
        String number = "";

        switch (letter) {
            case '1':
                number = "1";
                break;
            case 'a':
            case 'b':
            case 'c':
            case '2':
                number = "2";
                break;
            case 'd':
            case 'e':
            case 'f':
            case '3':
                number = "3";
                break;
            case 'g':
            case 'h':
            case 'i':
            case '4':
                number = "4";
                break;
            case 'j':
            case 'k':
            case 'l':
            case '5':
                number = "5";
                break;
            case 'm':
            case 'n':
            case 'o':
            case '6':
                number = "6";
                break;
            case 'p':
            case 'q':
            case 'r':
            case 's':
            case '7':
                number = "7";
                break;
            case 't':
            case 'u':
            case 'v':
            case '8':
                number = "8";
                break;
            case 'x':
            case 'w':
            case 'y':
            case 'z':
            case '9':
                number = "9";
                break;
            case '0':
                number = "0";
                break;
        }

        return number;
    }

}