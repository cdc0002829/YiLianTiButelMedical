package cn.redcdn.hvs.util;

import android.text.TextUtils;
import cn.redcdn.log.CustomLog;
import com.butel.connectevent.utils.LogUtil;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Desc
 * Created by wangkai on 2017/2/24.
 */

public class DateUtil {

    private static final String TAG = "DateUtil";
    /** 15:03:34 */
    public static final String FORMAT_HH_MM_SS = "HH:mm:ss";
    /** 20120219150334 */
    public static final String FORMAT_DATABASE = "yyyyMMddHHmmss";

    /** 2012-02-19 05:11 */
    public static final String FORMAT_YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
    public static final String FORMAT_YYYYMMDDHHMMSSSSS = "yyyyMMddHHmmssSSS";
    public static final String FORMAT_YYYY_MM_DD_HH_MM_N = "yyyy/MM/dd HH:mm";
    /**
     * 获取当前指定样式日期
     * @param formatType
     * @return
     */
    public static String getCurrentTimeSpecifyFormat(String formatType) {
        Date date = new Date();

        return formatDate2String(date, formatType);
    }

    /**
     * 将日期对象转换成日期字符串
     * @param date
     * @param format
     * @return
     */
    public static String formatDate2String(Date date, String format) {
        if (date == null) {
            return "";
        }

        try {
            SimpleDateFormat formatPattern = new SimpleDateFormat(format);
            return formatPattern.format(date);
        } catch (Exception e) {
            CustomLog.e(TAG,"formatPattern.format error" + e.toString());
            return "";
        }
    }

    /**
     * @author: zhaguitao
     * @Title: formatDateString
     * @Description: 将日期字符串格式化成数据库样式
     * @param dateStr
     *            日期字符串
     * @param fromFormat
     *            原始样式
     * @return 数据库样式的日期字符串
     * @date: 2013-5-22 上午10:15:35
     */
    public static String getDBOperateTime(String dateStr, String fromFormat) {
        // 1、将原始日期字符串转换成Date对象
        Date date = formatString2Date(dateStr, fromFormat);

        // 2、将Date对象转换成数据库样式样式字符串
        return formatDate2String(date, FORMAT_DATABASE);
    }

    /**
     * @author: zhaguitao
     * @Title: formatString2Date
     * @Description: 将日期字符串转换成Date对象
     * @param dateStr
     *            日期字符串
     * @param format
     *            日期字符串样式
     * @return Date对象
     * @date: 2013-5-22 下午2:07:29
     */
    public static Date formatString2Date(String dateStr, String format) {
        if (TextUtils.isEmpty(dateStr)) {
            return null;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.parse(dateStr);
        } catch (Exception e) {
            LogUtil.e("sdf.parse", e);
            return null;
        }
    }

    /**
     * Description:所有数据库操作时间取值都采用本方法
     *
     * @return 当前时间yyyyMMddHHmmss格式，如：20120219111945
     */
    public static String getDBOperateTime() {
        return getCurrentTimeSpecifyFormat(FORMAT_DATABASE);
    }

    /**
     * @author: zhaguitao
     * @Title: formatMs2String
     * @Description: 将毫秒日期对象转换成日期字符串
     * @param
     * @param format
     *            日期字符串样式
     * @return 日期字符串
     * @date: 2014-11-13
     */
    public static String formatMs2String(long milliseconds, String format) {
        try {
            Date date = new Date(milliseconds);
            SimpleDateFormat formatPattern = new SimpleDateFormat(format);
            return formatPattern.format(date);
        } catch (Exception e) {
            LogUtil.e("formatPattern.format", e);
            return "";
        }
    }


    /**
     * 获得毫秒级的时间值
     * @param timestring  时间字符串
     * @param format      时间字符串的格式
     * @return
     */
    public static long getTimeInMillis(String timestring, String format){
        Date date = formatString2Date(timestring, format);
        if(date!=null){
            Calendar fromCal = Calendar.getInstance();
            fromCal.setTime(date);
            return fromCal.getTimeInMillis();
        }
        return 0;
    }

    /**
     * @author: niuben
     * @Title: realDateIntervalDay
     * @Description: 计算日期之间的间隔天数
     * @param dateFrom
     *            起始日期ms
     * @param dateTo
     *            结束日期ms
     * @return 日期之间的间隔天数(dateFrom<dateTo 返回负数;dateFrom<dateTo正数 )
     * @date: 2016-3-9 下午2:25:28
     */
    public static int realDateIntervalDay(long dateFrom, long dateTo) {
        int rdd=realDateIntervalDay(formatString2Date(formatMs2String(dateFrom, FORMAT_DATABASE), FORMAT_DATABASE),
                formatString2Date(formatMs2String(dateTo, FORMAT_DATABASE), FORMAT_DATABASE));
        return dateFrom-dateTo>0?rdd:(-1*rdd);
    }

    /**
     * @author: zhaguitao
     * @Title: realDateIntervalDay
     * @Description: 计算日期之间的间隔天数
     * @param dateFrom
     *            起始日期
     * @param dateTo
     *            结束日期
     * @return 日期之间的间隔天数
     * @date: 2013-5-22 下午2:25:28
     */
    public static int realDateIntervalDay(Date dateFrom, Date dateTo) {
        Calendar fromCal = Calendar.getInstance();
        fromCal.setTime(dateFrom);

        Calendar toCal = Calendar.getInstance();
        toCal.setTime(dateTo);

        if (fromCal.after(toCal)) {
            // swap dates so that fromCal is start and toCal is end
            Calendar swap = fromCal;
            fromCal = toCal;
            toCal = swap;
        }
        int days = toCal.get(Calendar.DAY_OF_YEAR)
                - fromCal.get(Calendar.DAY_OF_YEAR);
        int y2 = toCal.get(Calendar.YEAR);
        if (fromCal.get(Calendar.YEAR) != y2) {
            fromCal = (Calendar) fromCal.clone();
            do {
                // 得到当年的实际天数
                days += fromCal.getActualMaximum(Calendar.DAY_OF_YEAR);
                fromCal.add(Calendar.YEAR, 1);
            } while (fromCal.get(Calendar.YEAR) != y2);
        }
        return days;
    }

    /**
     * @author: zhaguitao
     * @Title: getDateTimeByFormatAndMs
     * @Description: 将毫秒型日期转换成指定格式日期字符串
     * @param longTime
     * @param format
     * @return
     * @date: 2013-2-25 下午12:00:51
     */
    public static String getDateTimeByFormatAndMs(long longTime, String format) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(longTime);

        return formatDate2String(c.getTime(), format);
    }

}
