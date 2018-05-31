package com.jiangj.rabbitmq;

import com.jiangj.domain.MiaoshaUser;

public class MiaoshaMessage {

    private MiaoshaUser user;
    private long goodsId;

    public long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(long goodsId) {
        this.goodsId = goodsId;
    }

    public MiaoshaUser getUser() {
        return user;
    }

    public void setUser(MiaoshaUser user) {
        this.user = user;
    }
}
