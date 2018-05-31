package com.jiangj.service;

import com.alibaba.druid.util.StringUtils;
import com.jiangj.dao.MiaoshaUserDao;
import com.jiangj.domain.MiaoshaUser;
import com.jiangj.exception.GlobalException;
import com.jiangj.redis.MiaoshaUserKey;
import com.jiangj.redis.RedisService;
import com.jiangj.result.CodeMsg;
import com.jiangj.util.MD5Util;
import com.jiangj.util.UUIDUtil;
import com.jiangj.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * Created by jiangjian on 2018/4/26.
 */
@Service 
public class MiaoshaUserService {

    @Autowired
    RedisService redisService;

    @Autowired
    private MiaoshaUserDao miaoshaUserDao;


    public boolean login(HttpServletResponse response, @Valid LoginVo loginVo) {
        if(null == loginVo){
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String mobile = loginVo.getMobile();
        String formPass = loginVo.getPassword();

        //判断手机号是否存在
        MiaoshaUser user = getById(Long.parseLong(mobile));
        if(user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        String passDB = user.getPassword();
        String slatDB = user.getSalt();
        String calcPass = MD5Util.formPassToDBPass(formPass,slatDB);
        if(!passDB.equals(calcPass)){
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }
        //生成cookie
        String token = UUIDUtil.uuid();
        addCookie(response,token,user);
        return true;
    }

    public static final String COOKI_NAME_TOKEN = "token";

    private void addCookie(HttpServletResponse response,String token, MiaoshaUser user) {

        redisService.set(MiaoshaUserKey.token,token,user);
        Cookie cookie = new Cookie(COOKI_NAME_TOKEN, token);
        cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public MiaoshaUser getById(long id) {
        //取缓存
        MiaoshaUser user = redisService.get(MiaoshaUserKey.getById, ""+id, MiaoshaUser.class);
        if(user != null) {
            return user;
        }
        //取数据库
        user = miaoshaUserDao.getById(id);
        if(user != null) {
            redisService.set(MiaoshaUserKey.getById, ""+id, user);
        }
        return user;
    }

    public boolean updatePassword(String token,long id,String password){
        //取对象
        MiaoshaUser user = getById(id);
        if (user == null){
            throw new  GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        MiaoshaUser updateUser = new MiaoshaUser();
        updateUser.setId(user.getId());
        updateUser.setPassword(MD5Util.formPassToDBPass(password,user.getSalt()));
        miaoshaUserDao.update(updateUser);

        //处理缓存
        redisService.delete(MiaoshaUserKey.getById, ""+id);
        user.setPassword(updateUser.getPassword());
        redisService.set(MiaoshaUserKey.token,token,user);
        return true;
    }

    public MiaoshaUser getByToken(HttpServletResponse response,String token) {
        if (StringUtils.isEmpty(token)){
            return null;
        }
        MiaoshaUser user = redisService.get(MiaoshaUserKey.token,token,MiaoshaUser.class);
        if (null != user){
            addCookie(response,token,user);
        }
        return user;
    }
}
