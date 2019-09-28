package com.bophia.erik.ykxrider.utils;

import java.math.BigDecimal;

/**
 * 时间转换工具类
 */
public class TimeTransformation {
    public String getTime(Long MillTime){

        Integer ss = 1000;
        Integer mi = ss * 60;
        Integer hh = mi * 60;
        Integer dd = hh * 24;

        long day = MillTime / dd;
        long hour = (MillTime - day * dd) / hh;
        long minute = (MillTime - day * dd - hour * hh) / mi;
        long second = (MillTime - day * dd - hour * hh - minute * mi) / ss;

        String time = day * 24 + hour + ":" + minute + ":" + second;

        return time;
    }

    public double getMin(Long MillTime){

        Integer ss = 1000;
        Integer mi = ss * 60;
        Integer hh = mi * 60;
        Integer dd = hh * 24;

        long day = MillTime / dd;
        long hour = (MillTime - day * dd) / hh;
        long minute = (MillTime - day * dd - hour * hh) / mi;
        long second = (MillTime - day * dd - hour * hh - minute * mi) / ss;

        double doubleTime = day * 24 * 60 + minute + second / (double)60;
        BigDecimal bigDecimal = new BigDecimal(doubleTime);
        double time = bigDecimal.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();//保留两位小数，四舍五入

        return time;
    }


}
