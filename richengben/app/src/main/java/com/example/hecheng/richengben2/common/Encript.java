package com.example.hecheng.richengben2.common;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Administrator on 2017/3/8.
 */

public class Encript {
    public static String encriptByMd5 (String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //确定计算方法
        MessageDigest md5 = MessageDigest.getInstance("MD5");

        //加密后的字符串
        byte[] strByte = Base64.encode(md5.digest(str.getBytes("utf-8")),Base64.DEFAULT);
        String newstr = new String(strByte);
        return newstr;
    }
}
