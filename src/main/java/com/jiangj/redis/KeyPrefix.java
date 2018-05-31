package com.jiangj.redis;

/**
 * Created by jiangjian on 2018/4/25.
 */
public interface KeyPrefix {

    public int expireSeconds();

    public String getPrefix();
}
