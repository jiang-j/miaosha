package com.jiangj.service;

import com.jiangj.exception.GlobalException;
import com.jiangj.result.CodeMsg;
import com.jiangj.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jiangj.dao.UserDao;
import com.jiangj.domain.User;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Service
public class UserService {
	
	@Autowired
	private UserDao userDao;
	
	public User getUserById(int id) {
		return userDao.getById(id);
	}
	
	@Transactional
	public boolean tx() {
		
		User u1 = new User();
		u1.setId(2);
		u1.setName("test1");
		userDao.insert(u1);
		
		User u2 = new User();
		u2.setId(1);
		u2.setName("test2");
		userDao.insert(u2);
		
		return true;
	}

	public String login(HttpServletResponse response, @Valid LoginVo loginVo) {
		if(null == loginVo){
			throw new GlobalException(CodeMsg.SERVER_ERROR);
		}
		String mobile = loginVo.getMobile();
		String passwd = loginVo.getPassword();
		return  null;
	}
}
