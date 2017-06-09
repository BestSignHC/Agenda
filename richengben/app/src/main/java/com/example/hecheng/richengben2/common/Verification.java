package com.example.hecheng.richengben2.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/3/8.
 */

public class Verification {

    /**
     * 大陆手机号码验证
     * @param phoneNum ：输入字符串
     * @return
     */
    public static boolean isPhoneNum (String phoneNum) {
        if (phoneNum == null || phoneNum.length() != 11) {
            return false;
        }
        String regExp = "^((13[0-9])|(15[^4])|(18[0,2,3,5-9])|(17[0-8])|(147))\\d{8}$";
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(phoneNum);
        return m.matches();
    }

    /**
     * 数字、字母、下划线
     * @param pwd
     * @return
     */
    public static boolean isPwd (String pwd) {
        return pwd.matches("[A-Za-z0-9_]+");
    }
}
