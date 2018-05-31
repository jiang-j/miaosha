package com.jiangj.rabbitmq;

import com.jiangj.domain.MiaoshaOrder;
import com.jiangj.domain.MiaoshaUser;
import com.jiangj.redis.RedisService;
import com.jiangj.service.GoodsService;
import com.jiangj.service.MiaoshaService;
import com.jiangj.service.OrderService;
import com.jiangj.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by jiangjian on 2018/5/2.
 */
@Service
@Slf4j
public class MQReceiver {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MiaoshaService miaoshaService;

//    @RabbitListener(queues = MQConfig.QUEUE)
//    public void receiver(String message){
//        log.info("receiver message:{}",message);
//    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE1)
    public void receiverTopic1(String message){
        log.info("receiver TOPIC_QUEUE1 message:{}",message);
    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE2)
    public void receiverTopic2(String message){
        log.info("receiver TOPIC_QUEUE2 message:{}",message);
    }

    @RabbitListener(queues = MQConfig.HEADER_QUEUE)
    public void receiverHeaderQueue(byte[] message){
        log.info("receiver header queue message:{}", new String(message));
    }

    @RabbitListener(queues = MQConfig.MIAOSHA_QUEUE)
    public void miaoshaReceiver(String message){
        log.info("miaoshaReceiver:{}",message);
        MiaoshaMessage mm  = RedisService.stringToBean(message, MiaoshaMessage.class);
        MiaoshaUser user = mm.getUser();
        long goodsId = mm.getGoodsId();

        GoodsVo goods = goodsService.getGoodsByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if(stock <= 0) {
            return;
        }
        //判断是否已经秒杀到了
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        if(order != null) {
            return;
        }
        //减库存 下订单 写入秒杀订单
        miaoshaService.miaosha(user, goods);
    }

}
