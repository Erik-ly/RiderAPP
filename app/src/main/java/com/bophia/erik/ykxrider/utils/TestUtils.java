package com.bophia.erik.ykxrider.utils;

import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class TestUtils {

    public static void main(String[] args){
//
//        List<String> list = test2.newOrderList;
//
//        for (int i=0; i <list.size();i++){
//            System.out.println("List:======="+ "i:" + i + list.get(i));
//        }


//         String APP_ID = "wx817cbea5c383cf64";
//         String APP_Secret = "31bf6f084c86aadb0e3cd75c62721518";
//         String code = "001tDE1Q0b4epa20tf2Q0t5L1Q0tDE1F";
//
//        String url = "https://api.weixin.qq.com/sns/oauth2/access_token";
//
//        String result = HttpRequest.sendPost(url,"appid=" + APP_ID + "&secret=" + APP_Secret + "&code=" + code + "&grant_type=authorization_code");
//
//        System.out.println("result_http:=========" + result);

//        Double s = MapUtils.getDistance(113.734156,34.754188,113.707267,34.775587);
//        System.out.println("distance:============" + s);

//        DateUtils.estimateDisMoney("2018-11-09 18:10:04");

//        String str = "微信号";
//
//        String strEn = null;
//        String strDe = null;
//        try {
//
//            strEn = EncryptionUtil.byte2hex(EncryptionUtil.encode(str.getBytes(), EncryptionUtil.APPKEY.getBytes()));
//            strDe = new String(EncryptionUtil.decode(EncryptionUtil.hex2byte(strEn), EncryptionUtil.APPKEY.getBytes()));
//
//        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
//                | BadPaddingException | IllegalArgumentException e1) {
//            e1.printStackTrace();
//        }
//
//        System.out.println("strEn:=============" + strEn);
//        System.out.println("strDe:=============" + strDe);


//        String string = "null";
////        boolean b = StringUtils.isNullOrEmpty(string);
//        if (!StringUtils.isNullOrEmpty(string) || string.equals("备注")){
//            System.out.println("b==========");
//        }else {
//            System.out.println("a==========");
//        }


//        String string = "#你#我#他#";
//        String st = string.replace("#"," ");
//        System.out.println("st==========" + st);

//        String string = "1543548312387";
//        Long stringLong = Long.parseLong(string);
//
//        int a = stringLong.intValue();
////        int a = (int)stringLong;
//
////        Integer.MAX_VALUE;
//        System.out.println("stringLong=====" + stringLong);
//        System.out.println("a=====" + a);
//        System.out.println("Integer.MAX_VALUE=====" + Integer.MAX_VALUE);


//        float strFloat = 6.688f;
//        int num = 2;
//
//        float a = strFloat / num;
//
//        System.out.println(a);

//        String string = "商品1；商品2；商品3；";
//        String string1 = string.substring(0,string.length()-1);
//        System.out.println("string:====" + string);
//        System.out.println("string1:====" + string1);


//        String beginTime=new String("2017-06-09 10:22:22");
//        String endTime=new String("2017-05-09 11:22:22");
//
//        int i = beginTime.compareTo(endTime);
//        System.out.println("i===========" + i);

        String date = "2017-06-09 10:22:22";
        String today = date.substring(0,10);
        System.out.println("today===========" + today);



    }
}
