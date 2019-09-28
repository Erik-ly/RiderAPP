package com.bophia.erik.ykxrider.utils;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串相关工具类
 */
public class StringUtils {
    
	 /**
     * StringUtils工具类方法
     * 	获取一定长度的随机字符串，范围0-9，a-z
     * @param length：指定字符串长度
     * @return 一定长度的随机字符串
     */
    public static String getRandomStringByLength(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }
 
    /**
     * StringUtils工具类方法
     * 	获取一定长度的随机字符串，范围0-9 
     * @param length：指定字符串长度
     * @return 一定长度的随机字符串
     */
    public static String getRandomNumberByLength(int length) {
        String base = "0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }
    /**
     * 	（可多传多个参数）判断字符串是否为空
     */
    public static boolean isEmpty(String ... str){
        boolean stat = true ;
        for(String  temp: str){
        	System.out.println(temp);
        	System.out.println(temp==null);
        	System.out.println( "".equals(temp));
        	
            if (temp==null || "".equals(temp)  ){
            	stat  = false;
            }
        }
        return stat;
    }

    /**
     * 	（可多传多个参数）判断字符串是否为空(包括对"null")
     */
    public static boolean isNullOrEmpty(String ... str  ){
    	 //遍历参数内容
    	boolean stat = true ;
        for(String temp : str){
            if (temp==null || "".equals(temp) || "null".equals(temp)  ){
            	stat  = false;
            }
            
        }
        return stat;

    }

    /**
     * 把输入的参数格式化为日志需要的形式
     * 
     * */
    public static String formatLoggerMessage(String ... str) {
    	String formatStr="[";
    	
    	for (int i = 0; i < str.length; i++) {
			if(i!=str.length-1) {
				formatStr=formatStr+str[i]+", ";
			}else {
				formatStr=formatStr+str[i];
			}
		}
    	
    	formatStr+="]";
    	
    	return formatStr;
    }
    
    /**
     * 判断字符串是否纯数字
     */
    public static boolean isDigital(String str){
        if (!isEmpty(str))
            return str.matches("[0-9]+");
        return false;
    }
    
    /**
     * 计算含有中文的字符串长度
     * @param value 字符串（支持含中文字符串）
     * @return
     */
    public static int length(String value) {
        int valueLength = 0;
        String chinese = "[\u0391-\uFFE5]";
        /* 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1 */
        for (int i = 0; i < value.length(); i++) {
            /* 获取一个字符 */
            String temp = value.substring(i, i + 1);
            /* 判断是否为中文字符 */
            if (temp.matches(chinese)) {
                /* 中文字符长度为2 */
                valueLength += 2;
            } else {
                /* 其他字符长度为1 */
                valueLength += 1;
            }
        }
        return valueLength;
    }
    
    /**
     * 字符串数组转换List<String>
     * @param items
     * @return
     */
    public static List<String> stringsToList(String[] items) {
        List<String> lists = new ArrayList<String>();
        for(int i=0; i<items.length; i++){
            lists.add(items[i]);
        }
        return lists;
    }

    /**
     * 字符串填充，将sour使用fillStr前补或后补满len长度
     * @param sour 待填充字符串，支持含有中文
     * @param fillStr 填充数据
      * @param len 填充完整字符串长度
     * @param isLeft 是否左补填充数据，否则右补填充数据
     * @return
     */
    public static String fill(String sour, String fillStr, int len, boolean isLeft){
        if (sour == null) {
            sour = "";
        }
        int fillLen = len - length(sour);
        String fill = "";
        for (int i=0; i<fillLen; i++) {
            fill = fill + fillStr;
        }
        if (isLeft) {
            return fill + sour;
        } else {
            return sour + fill;
        }
    }
    
    /**
     * 字符串填充，中间填充
     * @param sour 左侧待填充字符串，支持含有中文
     * @param space 填充的数据，一般为* 、空格、——
     * @param lengh 填充完整字符串长度
     * @param modelparams 右侧待填充字符串，支持含有中文
     * @return
     */
    @SuppressWarnings("unused")
	public static String fillMiddle(String sour, String space, int lengh, String modelparams){
        String s, ss = null;
        int lenS = sour.length();
        int lenM = modelparams.length();
        //做非空处理
        if(sour == null) {
            sour = "";
        }
        if(modelparams == null) {
            modelparams = "";
        }
        if (space == "") {
            space = " "; 
        }
        //若输入为汉字，则长度取2倍
        if (sour.matches("[\u4e00-\u9fa5]+")) {
            lenS = lenS*2;
        }
        if (modelparams.matches("[\u4e00-\u9fa5]+")) {
            lenM = lenM*2;
        }
        //若输入有数字则，总长度加3
        if (sour.matches("[0-9]") && modelparams.matches("[0-9]")) {
            lengh = lengh + 4;
        }
        //长度保护
        if (lengh < (lenS + lenM)) {
            new Exception("哥们，长度设置太小了");
        }
        
        s = fill(modelparams, space, lengh - lenS-lenM, true);
        ss = sour + s +"\n";
        
        return ss;
    }
    
    
    /**
     * 字符串填充
     * 
     * @param strData 待填充字符串，不支持含有中文
     * @param nLen
     * @param
     * @param nOption
     *            0:左侧填充; 1:右侧填充; 2:两边填充
     * @return
     */
    public static String paddingString(String strData, int nLen, String subStr,
                                       int nOption) {
        int i, addCharLen;

        String strHead = "";
        String strEnd = "";

        i = strData.length();
        if (i >= nLen) {
            return strData;
        }

        switch (nOption) {
        case 0:
            addCharLen = (nLen - i) / subStr.length();
            for (i = 0; i < addCharLen; i++) {
                strHead += subStr;
            }
            return strHead + strData;
        case 1:
            addCharLen = (nLen - i) / subStr.length();
            for (i = 0; i < addCharLen; i++) {
                strEnd += subStr;
            }
            return strData + strEnd;
        case 2:
            addCharLen = (nLen - i) / (subStr.length() * 2);
            for (i = 0; i < addCharLen; i++) {
                strHead += subStr;
                strEnd += subStr;
            }
            return strHead + strData + strEnd;
        default:
            return strData;
        }
    }

    /**
     * 整形转换成BCD型的字符串
     * 9转换成后将变成09,00 09
     * 19转换后将变成19, 00 19
     * @param value
     * @param bytesNum
     *            BCD字节个数
     * @return
     */
    public static String intToBcd(int value, int bytesNum) {
        switch(bytesNum){
        case 1:
            if (value >= 0  && value <= 99){
                return paddingString(String.valueOf(value),2,"0",0);
            }
            break;
        case 2:
            if (value >= 0  && value <= 999) {
                return paddingString(String.valueOf(value),4,"0",0);
            }
            break;
            
        case 3:
            if (value >= 0  && value <= 999) {
                return paddingString(String.valueOf(value),3,"0",0);
            }
            break;
        }
        
        return "";
    }

    

    /**
     * 往value中填充一个字符0 ,当数据长度正好为2的整数倍时，不填充
     * 
     * @param value
     * @param option
     *            0:往后填充 ;1:往前填充
     * @return
     */
    public static String paddingZeroToHexStr(String value, int option) {
        
        if (value.length() % 2 == 0){
            return value;
        }
        
        if (option == 0){
            return "0" + value;
        }
        else if (option == 1){
            return value + "0";
        }
        else{
            return value;
        }
    }

    /**
     * 判断是否是Hex格式数据
     * 
     * @param value
     * @return
     */
    public static boolean checkHexStr(String value) {
        int i;
        int len;
        
        if (value == null) return false;
        
        len = value.length();
        if (len == 0) return false;

      for (i= 0;i<len;i++){
          if (!((value.charAt(i) >= '0' && value.charAt(i) <= '9')|| 
                  (value.charAt(i) >= 'a' && value.charAt(i) <= 'f') ||
                  (value.charAt(i) >= 'A' && value.charAt(i) <= 'F'))){
              return false;               
          }
      }
      return true;
    }

    /**
     * 判断字符串是否是数字0-9
     * 
     * @param value
     * @return
     */
/*    public static boolean checkDigitStr(String value) {
        int i;
        int len;
        
        if (value == null) return false;
        
        len = value.length();
        if (len == 0) return false;

      for (i= 0;i<len;i++){
          if (value.charAt(i) < '0' || value.charAt(i) > '9') {
              return false;               
          }
      }
      return true;
    }*/
    
    /**
     * Binary数据转换成Hex
     * 
     * @param value
     * @return
     */
    public static String binaryToHex(String value) {
        int i,j,len;
        String result ="";
        char[] hexVocable = { '0', '1', '2', '3', 
                '4', '5', '6','7', 
                '8', '9', 'A', 'B', 
                'C', 'D', 'E', 'F' };
        String[] binString  = {"0000", "0001", "0010", "0011",
                "0100", "0101", "0110", "0111",
                "1000", "1001", "1010", "1011",
                "1100", "1101", "1110", "1111"};
        //System.out.println("value: " + value);
        
        len = value.length();
        for(i=0; i<len; i += 4){
            for(j=0; j<16; j++){
                if(binString[j].equals(value.substring(i, i+4))){
                    result += hexVocable[j];
                    break;
                }                
            }
        }
        //System.out.println("result: " + result);
        return result;
    }

    /**
     * Hex数据转换成Binary
     * 
     * @param value
     * @return
     */
    public static String hexToBinary(String value) {
        int i,j,len;
        String result ="";
        char[] hexVocable = { '0', '1', '2', '3', 
                '4', '5', '6','7', 
                '8', '9', 'A', 'B', 
                'C', 'D', 'E', 'F' };
        String[] binString  = {"0000", "0001", "0010", "0011",
                "0100", "0101", "0110", "0111",
                "1000", "1001", "1010", "1011",
                "1100", "1101", "1110", "1111"};
        
        len = value.length();
        for(i=0; i<len; i++){
            for(j=0; j<16; j++){
                if(value.charAt(i) == hexVocable[j]){
                    result += binString[j];
                    break;
                }                
            }
        }
        //System.out.println("result: " + result);
        return result;
    }
    
    /**
     * 获取二进制字符串
     * 0x00 0x01 0x00 0x01 0x01转换成"01011"
     * @param value
     * @return
     */
    public static String getBinaryString(byte[] value) {
        int len;
        String result ="";
        
        len = value.length;
        
        for(int i=0;i<len;i++) {
            result += String.valueOf(value[i]);
        }
        
        return result;
    }
    /**
     * mmdd日期格式
     * @param date
     * @return
     */
    public static boolean isMatchDate(String date){
        if(date.length()!=4||date==null){
            return false;
        }
        String eL="(((0[13578]|1[02])(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)(0[1-9]|[12][0-9]|30))|(02(0[1-9]|[1][0-9]|2[0-9])))";
           Pattern p = Pattern.compile(eL);
            Matcher m = p.matcher(date);
            boolean b = m.matches();   
            return b;
    }
    
    /**
     * 删除字符串左侧所有字符ch
     * @param
     * @return
     */
    public static String deleteLeftChar(String data, char ch){
        int flag = 0;
        for(int i = 0;i<data.length();i++){
            if(data.charAt(i) == ch){
                flag = i;
            }else{
                break;
            }
        }
        flag = flag+1;
        data = data.substring(flag);
        return data;
    }
    
    /**
     * 删除字符串右侧所有字符ch
     * @param
     * @return
     */
    public static String deleteRightChar(String data, char ch){
        
        int flag = data.length();
        for(int i = data.length()-1;i>=0;i--){
            if(data.charAt(i) == ch){
                flag = i;
            }else{
                break;
            }
        }

        data = data.substring(0,flag);
        return data;
    }
    
    
        /**
     * 将显示金额的字符串（带原点），转化为没有原点的字符串
     * 
     * 示例：123.32 --->  12332
     *       2.  ----> 200
     *       2.3  ----> 230
     *       0.12 --->  12
     *       0.02 --->   2
     * @param str
     * @return
     */
    public static String decimalWipeDot(String str){
        
        //拿到字符串
        if (str.contains(".")) {
            
            //判断点后还有几位
            int indexOf = str.indexOf(".");
            //用索引和长度进行判断
            int length = str.length();
            int cha = length - indexOf; 
            char charAt = str.charAt(indexOf - 1);

            switch (cha) {
            case 1:
                //小数后有0位--先去掉点，在加个00   eg: 2. --> 200   0.-->0
                if (charAt == '0') {
                    str= "0";
                }else {
                    str = str.replace(".", "");
                    str= str+"00";
                }
                break;
            case 2:
                //小数后有1位--先去掉点，在加个0  eg: 2.3 -->  230  0.1 -->10
                if (charAt == '0') {
                    str= str.charAt(indexOf + 1)+"0";
                }else {
                    str = str.replace(".", "");
                    str= str+"0";
                }
                
                break;
            case 3:
                //小数后有2位--直接去掉点   eg:   2.03 --> 203   0.12-->12  0.02 --> 2
                char charAt2 = str.charAt(indexOf + 1);
                if (str.length() == 4) {
                    
                    if (charAt == '0') {
                        if (charAt2 == '0') {
                            str= str.substring(length - 1,length);
                        }else {
                            
                            str= str.substring(indexOf + 1,length);
                        }
                    }else {
                        str = str.replace(".", "");
                    }
                }else {
                    str = str.replace(".", "");
                }
                
                break;
            default:
                break;
            }
        }else {
            //没有点， 直接加00  eg:  23 ---> 2300   0--->0   0000023--->  2300    00000--->0
            //去掉前面多余的0 ，在判断这个数是否为0
            if (str.equals("")) {
                str = "0";
            }
            int int1 = Integer.parseInt(str);
            str = String.valueOf(int1);
            
            if (int1 != 0){
                str = str+"00";
            }else {
                str = "0";
            }
        }
        return str;
    }
    
    /**
     * 将金额的字符串(不带点)  转化为带点的金额字符串
     * 示例       12332 ---> 123.32
     *       200   --->  2
     *       230   ----> 2.3 
     *        12 ---> 0.12 
     *         2  ---> 0.02
     * 
     * 
     * @param str
     * @return
     */
    public static String decimalAddDot(String str){
        boolean isNegative = false;
        str = str.replace(" ","");

        if (str == null || str == "") {
            return "0.00";
        }
        //截取字符第一个字符
         String sign = str.substring(0,1);
        if (sign.equals("-")) {
            isNegative = true;//标记为负数
            str = str.substring(1,str.length());
        }else{
            //正数不作处理
        }

        int length = str.length();
        if (length >= 3) {  //200-->2.00
            str = str.substring(0, length-2)+"."+str.substring(length-2, length);
        }else {
            switch (length) {
            case 2:// 20-->0.20   23 --> 0.23
                str = "0."+str;
                break;
            case 1://  2-->0.02   
                str = "0.0"+str;
                break;
            case 0:
                //说明没有    0.00
                str = "0.00";
                break;
            default:
                break;
            }
        }

        if (isNegative) {
            return "-"+str;
        }else{
            return str;
        }

    
        
    }
}