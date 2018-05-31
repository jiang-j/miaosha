package com.jiangj.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Created by jiangjian on 2018/4/26.
 */
@Slf4j
public class MD5Util {

    public static String md5(String src){
        return DigestUtils.md5Hex(src);
    }

    private static final String salt = "1a2b3c4d";

    //用户输入的明文passwd转换成form表单提交的passwd
    public static String inputPassToFormPass(String inputPass) {
        String str = ""+salt.charAt(0)+salt.charAt(2) + inputPass +salt.charAt(5) + salt.charAt(4);
        return md5(str);
    }

    //把form里面的passwd转换成db里面的passwd
    public static String formPassToDBPass(String formPass, String salt) {
        String str = ""+salt.charAt(0)+salt.charAt(2) + formPass +salt.charAt(5) + salt.charAt(4);
        return md5(str);
    }

    //把用户输入的passwd转换成db里面的passwd
    public static String inputPassToDbPass(String inputPass, String saltDB) {
        String formPass = inputPassToFormPass(inputPass);
        String dbPass = formPassToDBPass(formPass, saltDB);
        return dbPass;
    }

    public static void main(String[] args) {
        log.info(inputPassToFormPass("123456"));
        log.info(formPassToDBPass(inputPassToFormPass("123456"),salt));
        log.info(inputPassToDbPass("123456",salt));
    }
}
