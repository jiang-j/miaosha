package com.jiangj.redis;

/**
 * Created by jiangjian on 2018/4/30.
 */
public class GoodsKey extends BasePrefix {



    public GoodsKey(String prefix) {
        super(prefix);
    }

    public GoodsKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static KeyPrefix getGoodsList = new GoodsKey(60, "gl");

    public static KeyPrefix getGoodsDetail = new GoodsKey(60, "gd");

    public static KeyPrefix getMiaoshaGoodsStock = new GoodsKey(60, "gs");
}
