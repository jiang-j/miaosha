package com.jiangj.controller;

import com.alibaba.druid.util.StringUtils;
import com.jiangj.domain.MiaoshaUser;
import com.jiangj.redis.GoodsKey;
import com.jiangj.redis.RedisService;
import com.jiangj.result.Result;
import com.jiangj.service.GoodsService;
import com.jiangj.service.MiaoshaUserService;
import com.jiangj.vo.GoodsDetailVo;
import com.jiangj.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Created by jiangjian on 2018/4/26.
 */
@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    RedisService redisService;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private MiaoshaUserService userService;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;

    @RequestMapping(value = "/to_list",produces = "text/html")
    @ResponseBody
    public String toList(HttpServletRequest request, HttpServletResponse response, Model model, MiaoshaUser user){
        model.addAttribute("user",user);

        //取缓存
        String html = redisService.get(GoodsKey.getGoodsList, "",String.class);
        if (!StringUtils.isEmpty(html)){
            return html;
        }

        List<GoodsVo> goodsVos = goodsService.listGoodsVo();
        model.addAttribute("goodsList",goodsVos);

        IWebContext ctx = new WebContext(request,response,
                request.getServletContext(),request.getLocale(), model.asMap());

        html = thymeleafViewResolver.getTemplateEngine().process("goods_list",ctx);
        if (!StringUtils.isEmpty(html)){
            redisService.set(GoodsKey.getGoodsList,"",html);
        }
        return html;
    }

    @RequestMapping(value = "/to_detail/{goodsId}",produces = "text/html")
    @ResponseBody
    public String toDetail2(HttpServletRequest request, HttpServletResponse response,Model model,MiaoshaUser user,
                           @PathVariable("goodsId")long goodsId){
        model.addAttribute("user", user);

        //取缓存
        String html = redisService.get(GoodsKey.getGoodsDetail, ""+goodsId,String.class);
        if (!StringUtils.isEmpty(html)){
            return html;
        }

        GoodsVo goods = goodsService.getGoodsByGoodsId(goodsId);
        model.addAttribute("goods", goods);

        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();

        int miaoshaStatus = 0;
        int remainSeconds = 0;

        if (now < startAt){//秒杀还未开始，倒计时
            miaoshaStatus = 0;
            remainSeconds = (int)(startAt - now)/1000;
        }else if (now > endAt){//秒杀已结束
            miaoshaStatus = 2;
            remainSeconds = -1;
        }else {//正在进行中
            miaoshaStatus = 1;
            remainSeconds = 0;
        }
        model.addAttribute("miaoshaStatus", miaoshaStatus);
        model.addAttribute("remainSeconds", remainSeconds);

        IWebContext ctx = new WebContext(request,response,
                request.getServletContext(),request.getLocale(), model.asMap());

        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail",ctx);
        if (!StringUtils.isEmpty(html)){
            redisService.set(GoodsKey.getGoodsDetail,""+goodsId,html);
        }
        return html;
    }

    @RequestMapping(value = "/detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> toDetail(HttpServletRequest request, HttpServletResponse response, Model model, MiaoshaUser user,
                           @PathVariable("goodsId")long goodsId){


        GoodsVo goods = goodsService.getGoodsByGoodsId(goodsId);

        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();

        int miaoshaStatus = 0;
        int remainSeconds = 0;

        if (now < startAt){//秒杀还未开始，倒计时
            miaoshaStatus = 0;
            remainSeconds = (int)(startAt - now)/1000;
        }else if (now > endAt){//秒杀已结束
            miaoshaStatus = 2;
            remainSeconds = -1;
        }else {//正在进行中
            miaoshaStatus = 1;
            remainSeconds = 0;
        }
        GoodsDetailVo goodsDetailVo = new GoodsDetailVo();
        goodsDetailVo.setGoods(goods);
        goodsDetailVo.setMiaoshaStatus(miaoshaStatus);
        goodsDetailVo.setRemainSeconds(remainSeconds);
        goodsDetailVo.setUser(user);

        return Result.success(goodsDetailVo);
    }
}
