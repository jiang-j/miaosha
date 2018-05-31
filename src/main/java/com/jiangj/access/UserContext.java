package com.jiangj.access;

import com.jiangj.domain.MiaoshaUser;

public class UserContext {

    private static ThreadLocal<MiaoshaUser> miaoshaUserThreadLocal = new ThreadLocal<MiaoshaUser>();

    public static void setUser(MiaoshaUser user){
        miaoshaUserThreadLocal.set(user);
    }

    public static MiaoshaUser getUser(){
        return miaoshaUserThreadLocal.get();
    }
}
