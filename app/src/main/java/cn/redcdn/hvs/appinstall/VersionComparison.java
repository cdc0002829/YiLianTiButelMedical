package cn.redcdn.hvs.appinstall;

import cn.redcdn.log.CustomLog;

public class VersionComparison {
  /**
   * 
   * @param version1
   * @param version2
   * @return if version1 > version2, return 1, if equal, return 0, else return
   *         -1
   */
  public static int compare(String version1, String version2) {
    if (version1 == null || version1.length() == 0 || version2 == null
        || version2.length() == 0) {
      CustomLog.e("VersionComparison", "参数有误");
      return -2;
    }
    int index1 = 0;
    int index2 = 0;
    while (index1 < version1.length() && index2 < version2.length()) {
      int[] number1 = getValue(version1, index1);
      int[] number2 = getValue(version2, index2);
      if (number1[0] < number2[0]) {
        return -1;
      } else if (number1[0] > number2[0]) {
        return 1;
      } else {
        index1 = number1[1] + 1;
        index2 = number2[1] + 1;
      }
    }
    if (index1 == version1.length() && index2 == version2.length()) {
      return 0;
    }
    if (index1 < version1.length()) {
      return 1;
    } else {
      return -1;
    }
  }

  /**
   * 
   * @param version
   * @param index
   *          the starting point
   * @return the number between two dots, and the index of the dot
   */
  public static int[] getValue(String version, int index) {
    int[] valueIndex = new int[2];
    StringBuilder sb = new StringBuilder();
    while (index < version.length() && version.charAt(index) != '.') {
      sb.append(version.charAt(index));
      index++;
    }
    valueIndex[0] = Integer.parseInt(sb.toString());
    valueIndex[1] = index;
    return valueIndex;
  }

}
