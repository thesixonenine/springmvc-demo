package com.simple.springmvc.service.impl;

import com.simple.springmvc.anno.SimpleService;
import com.simple.springmvc.entity.User;
import com.simple.springmvc.service.UserService;

/**
 * @author simple
 * @version 1.0
 * @date 2019-01-17 09:54
 * @since 1.0
 */
@SimpleService("userService")
public class UserServiceImpl implements UserService {

    public String getUser() {
        User user = new User();
        user.setId(1L);
        user.setAge("20");
        user.setName("胡杨");
        return user.toString();
    }
}
