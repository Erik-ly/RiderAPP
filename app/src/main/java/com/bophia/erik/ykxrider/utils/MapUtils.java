package com.bophia.erik.ykxrider.utils;

/**
 * 计算两个经纬度的直线距离
 */
public class MapUtils {

    //地球半径
    private static final double EARTH_RADIUS = 6378137.0;

    /**
     * 获取两个经纬度之间的距离
     * @param longitude1 第一坐标经度
     * @param latitude1 第一个坐标纬度
     * @param longitude2 第二个坐标经度
     * @param latitude2 第二个坐标纬度
     * @return 亮点间距离，单位：米
     */
    public static double getDistance(double longitude1, double latitude1,
                                     double longitude2, double latitude2){

        double Lat1 = rad(latitude1);
        double Lat2 = rad(latitude2);
        double a = Lat1 - Lat2;
        double b = rad(longitude1) - rad(longitude2);

        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2),2) + Math.cos(Lat1) * Math.cos(Lat2) * Math.pow(Math.sin(b / 2),2)));

        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;

        return s;
    }

    private static double rad(double d){
        return d * Math.PI / 180.0;
    }

}
