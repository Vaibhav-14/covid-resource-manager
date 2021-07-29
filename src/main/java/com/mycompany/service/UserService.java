package com.mycompany.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mycompany.beans.User;
import com.mycompany.dao.IUserFunctionDAO;


@Service("userService")
public class UserService {
	
	@Autowired
	private IUserFunctionDAO userDao;

	public void addUser(User u)
	{
		userDao.save(u);
	}

}