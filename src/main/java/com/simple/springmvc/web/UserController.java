package com.simple.springmvc.web;

import com.simple.springmvc.anno.SimpleAutowired;
import com.simple.springmvc.anno.SimpleController;
import com.simple.springmvc.anno.SimpleMapping;
import com.simple.springmvc.service.UserService;

/**
 * @author simple
 * @version 1.0
 * @date 2019-01-17 09:45
 * @since 1.0
 */
@SimpleController
@SimpleMapping("/user")
public class UserController {
    @SimpleAutowired
    private UserService userService;

    @SimpleMapping("/")
    public String getUser() {
        return userService.getUser();
    }
}
