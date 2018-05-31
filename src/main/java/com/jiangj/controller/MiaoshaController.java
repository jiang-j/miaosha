package com.jiangj.controller;

import com.jiangj.access.AccessLimit;
import com.jiangj.domain.MiaoshaOrder;
import com.jiangj.domain.MiaoshaUser;
import com.jiangj.rabbitmq.MQSender;
import com.jiangj.rabbitmq.MiaoshaMessage;
import com.jiangj.redis.AccessKey;
import com.jiangj.redis.GoodsKey;
import com.jiangj.redis.RedisService;
import com.jiangj.result.CodeMsg;
import com.jiangj.result.Result;
import com.jiangj.service.GoodsService;
import com.jiangj.service.MiaoshaService;
import com.jiangj.service.MiaoshaUserService;
import com.jiangj.service.OrderService;
import com.jiangj.vo.GoodsVo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jiangjian on 2018/4/26.
 */
@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private MiaoshaUserService userService;

    @Autowired
    private MiaoshaService miaoshaService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private RedisService redisService;

    @Autowired
    MQSender mqSender;

    private Map<Long, Boolean> localOverMap =  new ConcurrentHashMap<>();

    /**
     * 系统初始化
     * */
    @Override
    public void afterPropertiesSet(){
        List<GoodsVo> goodsVos = goodsService.listGoodsVo();
        if (goodsVos == null){
            return;
        }
        for (GoodsVo goodsVo : goodsVos){
            redisService.set(GoodsKey.getMiaoshaGoodsStock,""+goodsVo.getId(), ""+goodsVo.getStockCount());
        }
    }

    @RequestMapping(value = "/{path}/do_miaosha",method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> toList(Model model, MiaoshaUser user,
                                  @RequestParam("goodsId")long goodsId,
                                @PathVariable("path") String path){
        if (user == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        //验证path
        boolean check = miaoshaService.checkPath(user, goodsId, path);
        if(!check){
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }

        //内存标记，减少redis访问
        if (localOverMap != null && localOverMap.size() > 0){
            boolean over = localOverMap.get(goodsId);
            if (over){
                return Result.error(CodeMsg.MIAO_SHA_OVER);
            }
        }


        //预减库存
        long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock,""+goodsId);
        if(stock < 0){
            localOverMap.put(goodsId,true);
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }

        //判断是否秒杀过
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
        if (null != order){
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }

        //入队
        MiaoshaMessage mm = new MiaoshaMessage();
        mm.setGoodsId(goodsId);
        mm.setUser(user);
        mqSender.sendMiaoshaMessage(mm);

        return Result.success(0);//排队中
    }

    /**
     * orderId：成功
     * -1：秒杀失败
     * 0： 排队中
     * */
    @RequestMapping(value="/result", method=RequestMethod.GET)
    @ResponseBody
    public Result<Long> miaoshaResult(Model model,MiaoshaUser user,
                                      @RequestParam("goodsId")long goodsId) {
        model.addAttribute("user", user);
        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        long result  = miaoshaService.getMiaoshaResult(user.getId(), goodsId);
        return Result.success(result);
    }

    /**
     * 获取秒杀地址
     * */
    @AccessLimit(seconds=5, maxCount=5, needLogin=true)
    @RequestMapping(value="/path", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaPath(HttpServletRequest request,Model model, MiaoshaUser user,
                                         @RequestParam("goodsId")long goodsId,
                                         @RequestParam(value = "verifyCode",defaultValue = "0") int verifyCode) {
        model.addAttribute("user", user);
        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        boolean check = miaoshaService.checkVerifyCode(user, goodsId, verifyCode);
        if(!check) {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        String path = miaoshaService.createMiaoshaPath(user,goodsId);

        return Result.success(path);
    }

    /**
     * 图片验证码
     * */
    @RequestMapping(value="/verifyCode", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> getVerifyCode(HttpServletResponse response,Model model, MiaoshaUser user, @RequestParam("goodsId")long goodsId) {
        model.addAttribute("user", user);
        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        OutputStream outputStream = null;
        try {
            BufferedImage image = miaoshaService.createVerifyCode(user,goodsId);
            outputStream = response.getOutputStream();
            ImageIO.write(image,"JPEG",outputStream);
            return null;
        }catch (Exception e){
            e.printStackTrace();
            return Result.error(CodeMsg.MIAOSHA_FAIL);
        }finally {
            if (outputStream != null){
                try {
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }


}
