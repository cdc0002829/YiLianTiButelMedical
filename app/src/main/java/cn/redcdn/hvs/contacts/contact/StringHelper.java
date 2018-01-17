package cn.redcdn.hvs.contacts.contact;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import cn.redcdn.log.CustomLog;

public class StringHelper {
  /**
   * �õ� ȫƴ
   * 
   * @param src
   * @return
   */
  public static String getPingYin(String src) {

    // if (src.equals("最新推荐")) {
    // return src;
    // }
    if (src == null) {
      return "";
    }
    char[] t1 = null;
    t1 = src.toCharArray();
    String[] t2 = new String[t1.length];
    HanyuPinyinOutputFormat t3 = new HanyuPinyinOutputFormat();
    t3.setCaseType(HanyuPinyinCaseType.LOWERCASE);
    t3.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    t3.setVCharType(HanyuPinyinVCharType.WITH_V);
    String t4 = "";
    int t0 = t1.length;
    try {
      for (int i = 0; i < t0; i++) {
        // �ж��Ƿ�Ϊ�����ַ�
        if (Character.toString(t1[i]).matches("[\\u4E00-\\u9FA5]+")) {
          t2 = PinyinHelper.toHanyuPinyinStringArray(t1[i], t3);
          t4 += t2[0];
        } else {
          t4 += Character.toString(t1[i]);
        }
      }
      return t4;
    } catch (BadHanyuPinyinOutputFormatCombination e1) {
      e1.printStackTrace();
    }
    return t4;
  }

  public static String getAllPingYin(String src) {
    if (src == null) {
      return "";
    }
    char[] t1 = null;
    t1 = src.toCharArray();
    String[] t2 = new String[t1.length];
    HanyuPinyinOutputFormat t3 = new HanyuPinyinOutputFormat();
    t3.setCaseType(HanyuPinyinCaseType.UPPERCASE);
    t3.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    t3.setVCharType(HanyuPinyinVCharType.WITH_V);
    String t4 = "";
    int t0 = t1.length;
    try {
      for (int i = 0; i < t0; i++) {
        if (Character.toString(t1[i]).matches("[\\u4E00-\\u9FA5]+")) {
          t2 = PinyinHelper.toHanyuPinyinStringArray(t1[i], t3);
          t4 += t2[0];
        } else if (Character.toString(t1[i]).matches("^[a-zA-Z]*")) {
          if (i == 0) {
            t4 += Character.toString(t1[i]).toUpperCase();
          } else {
            // 如果是英文，间接取出字符并连接到字符串t4后
            t4 += Character.toString(t1[i]);
          }
        } else {
          t4 += Character.toString(t1[i]);
        }
      }
      return t4;
    } catch (BadHanyuPinyinOutputFormatCombination e1) {
      CustomLog.e("StringHelper", e1.toString());
    }
    return t4;
  }

  /**
   * �õ�����ĸ
   * 
   * @param str
   * @return
   */
  public static String getHeadChar(String str) {

    if (str == null || str.isEmpty()) {
      return "";
    }
    String convert = "";
    char word = str.charAt(0);
    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word);
    if (pinyinArray != null) {
      convert += pinyinArray[0].charAt(0);
    } else {
      convert += word;
    }
    return convert.toUpperCase();
  }

  /**
   * �õ���������ĸ��д
   * 
   * @param str
   * @return
   */
  public static String getPinYinHeadChar(String str) {
    if (str == null) {
      return "";
    }
    String convert = "";
    for (int j = 0; j < str.length(); j++) {
      char word = str.charAt(j);
      String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word);
      if (pinyinArray != null) {
        convert += pinyinArray[0].charAt(0);
      } else {
        convert += word;
      }
    }
    return convert.toUpperCase();
  }
}
