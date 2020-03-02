package com.ydxsj.ydsoldnote.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PublicUtil {

    public static SimpleDateFormat SDF_YYYY_DD_MM = new SimpleDateFormat("YYYY-dd-MM");
    public static SimpleDateFormat SDF_YYYY_DD_MM_HH_MM_SS = new SimpleDateFormat("YYYY-dd-MM HH:mm:ss");


    public static String timestampToString(String timestamp,SimpleDateFormat sdf){
        return sdf.format(timestampToDate(timestamp));
    }

    /**
     *  时间戳(13) to Date
     * @param timestamp
     * @return
     */
    public static Date timestampToDate(String timestamp){
        return new Date((Long.parseLong(timestamp)));

    }
}
