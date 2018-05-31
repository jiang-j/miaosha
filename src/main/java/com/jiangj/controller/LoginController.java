package com.jiangj.controller;

import com.jiangj.result.Result;
import com.jiangj.service.MiaoshaUserService;
import com.jiangj.vo.LoginVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * Created by jiangjian on 2018/4/26.
 */
@Slf4j
@Controller
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private MiaoshaUserService userService;


    @RequestMapping("/to_login")
    public String toLogin(){
        return "login";
    }

    @ResponseBody
    @RequestMapping("/do_login")
    public Result<Boolean> doLogin(HttpServletResponse response, @Valid LoginVo loginVo){
        log.info(loginVo.toString());
        userService.login(response, loginVo);
        return Result.success(true);
    }
}
