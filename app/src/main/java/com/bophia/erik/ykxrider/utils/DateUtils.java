package com.bophia.erik.ykxrider.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * 时间工具类，通过当前时间获取配送费用
 */
public class DateUtils {

    public static String estimateDisMoney(String orderTime){
        String disMoney = null;

        //截取时间 HH:mm 格式
        String time = orderTime.substring(11,16);

        if (isInTime("09:00-20:00",time)){

            disMoney = "2.5";

        }else if (isInTime("20:00-22:00",time)){

            disMoney = "3.0";

        }else if (isInTime("22:00-00:00",time)){

            disMoney = "4.0";

        }else if (isInTime("00:00-02:00",time)){

            disMoney = "8.0";

        }else {

            disMoney = "0.0";

        }

        return disMoney;
    }


/**
 * 判断某一时间是否在一个区间内
 *
 * @param sourceTime
 *            时间区间,半闭合,如[10:00-20:00)
 * @param curTime
 *            需要判断的时间 如10:00
 * @return
 * @throws IllegalArgumentException
 */
    public static boolean isInTime(String sourceTime, String curTime) {
        if (sourceTime == null || !sourceTime.contains("-") || !sourceTime.contains(":")) {
            throw new IllegalArgumentException("Illegal Argument arg:" + sourceTime);
        }
        if (curTime == null || !curTime.contains(":")) {
            throw new IllegalArgumentException("Illegal Argument arg:" + curTime);
        }
        String[] args = sourceTime.split("-");
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        try {
            long now = sdf.parse(curTime).getTime();
            long start = sdf.parse(args[0]).getTime();
            long end = sdf.parse(args[1]).getTime();
            if (args[1].equals("00:00")) {
                args[1] = "24:00";
            }
            if (end < start) {
                if (now >= end && now < start) {
                    return false;
                } else {
                    return true;
                }
            } else {
                if (now >= start && now < end) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Illegal Argument arg:" + sourceTime);
        }
    }

}
