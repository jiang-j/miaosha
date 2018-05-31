package com.jiangj.controller;


import com.jiangj.domain.MiaoshaUser;
import com.jiangj.rabbitmq.MQSender;
import com.jiangj.redis.UserKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jiangj.domain.User;
import com.jiangj.result.CodeMsg;
import com.jiangj.result.Result;
import com.jiangj.redis.RedisService;
import com.jiangj.service.UserService;

@Controller
public class HelloController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private RedisService redisService;

	@Autowired
	private MQSender mqSender;
	
	@ResponseBody
	@RequestMapping("/")
	public String index() {
		return "Hello World!!";
	}

	@ResponseBody
	@RequestMapping("/mq/header")
	public Result<Boolean> mqSenderHeader() {
		mqSender.sendHeader("hello jiangjian");
		return Result.success(true);
	}

	@ResponseBody
	@RequestMapping("/mq/funout")
	public Result<Boolean> mqSenderFunout() {
		mqSender.sendFunout("hello jiangjian");
		return Result.success(true);
	}

	@ResponseBody
	@RequestMapping("/mq/topic")
	public Result<Boolean> mqSenderTopic() {
		mqSender.sendTopic("hello jiangjian");
		return Result.success(true);
	}

//	@ResponseBody
//	@RequestMapping("/mq")
//	public Result<Boolean> mqSender() {
//		mqSender.send("hello jiangjian");
//		return Result.success(true);
//	}
	
	@RequestMapping("/hello")
	public String hello(ModelMap model) {
		model.addAttribute("name", "jiang jian");
		return "hello";
	}
	
	@ResponseBody
	@RequestMapping("/success")
	public Result<String> success() {
		return Result.success("success!!");
	}
	
	@ResponseBody
	@RequestMapping("/serverError")
	public Result<String> serverError() {
		return Result.error(CodeMsg.SERVER_ERROR);
	}
	
	@ResponseBody
	@RequestMapping("/db/get")
	public Result<User> dbGet() {
		User user = userService.getUserById(1);
		return Result.success(user);
	}

	@ResponseBody
	@RequestMapping("/db/tx")
	public Result<Boolean> dbTx() {
		
		return Result.success(userService.tx());
	}

	@ResponseBody
	@RequestMapping("/redis/get")
	public Result<User> redisGet() {
		User user = redisService.get(UserKey.getById,"1",User.class);
		return Result.success(user);
	}

	@ResponseBody
	@RequestMapping("/redis/set")
	public Result<Boolean> redisSet() {
		User user = new User();
		user.setId(11);
		user.setName("jiangjian");
		redisService.set(UserKey.getById,"1",user);

		return Result.success(true);
	}

	@ResponseBody
	@RequestMapping("/user/info")
	public Result<MiaoshaUser> info(MiaoshaUser user) {
		return Result.success(user);
	}
}
