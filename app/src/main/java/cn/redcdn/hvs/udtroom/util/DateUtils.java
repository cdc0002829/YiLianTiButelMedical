package cn.redcdn.hvs.udtroom.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author guoyx
 */

public class DateUtils {

    /**
     * 将秒级时间戳转换为 yyyyMMdd
     * @param seconds
     * @return
     */
    public static String timeStamp2Date(String seconds) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return sdf.format(new Date(Long.valueOf(seconds + "000")));
    }
}
