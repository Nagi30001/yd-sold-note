package com.ydxsj.ydsoldnote.util;

import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PublicUtil {

    public static SimpleDateFormat SDF_YYYY_MM_DD = new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat SDF_YYYY_MM = new SimpleDateFormat("yyyy-MM");
    public static SimpleDateFormat SDF_YYYY_MM_DD_HH_MM_SS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static SimpleDateFormat SDF_YYYY_MM_DD_HH_MM = new SimpleDateFormat("yyyy-MM-dd HH:mm");


    /**
     * 时间
     * @param timestamp
     * @param sdf
     * @return
     */
    public static String timestampToString(String timestamp,SimpleDateFormat sdf){
        if (StringUtils.isEmpty(timestamp)){
            return null;
        }
        return sdf.format(timestampToDate(timestamp));
    }

    /**
     * 时间
     * @param sdf
     * @return
     */
    public static String stringTotimestamp(String date,SimpleDateFormat sdf){
        try {
            return String.valueOf(sdf.parse(date).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *  时间戳(13) to Date
     * @param timestamp
     * @return
     */
    public static Date timestampToDate(String timestamp){
        if (StringUtils.isEmpty(timestamp)){
            return null;
        }
        return new Date((Long.parseLong(timestamp)));
    }


    /**
     * 字符串转换成 Date
     * @param date
     * @param sdf
     * @return
     */
    public static Date stringToDate(String date,SimpleDateFormat sdf){
        try {
            return sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *  获取两个日期之间的所有日期
     * @param startTime
     * @param endTime
     * @return
     */
    public static List<String> getDays(String startTime, String endTime) {

        // 返回的日期集合
        List<String> days = new ArrayList<String>();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date start = dateFormat.parse(startTime);
            Date end = dateFormat.parse(endTime);
            Calendar tempStart = Calendar.getInstance();
            tempStart.setTime(start);

            Calendar tempEnd = Calendar.getInstance();
            tempEnd.setTime(end);
            tempEnd.add(Calendar.DATE, +1);// 日期加1(包含结束)
            while (tempStart.before(tempEnd)) {
                days.add(dateFormat.format(tempStart.getTime()));
                tempStart.add(Calendar.DAY_OF_YEAR, 1);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return days;
    }

    public static int getYear(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    public static int getMonth(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MONTH)+1;
    }

    public static int getMonthMaxDay(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }


}
