package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.entity.User;
import com.hospital.exception.BusinessException;
import com.hospital.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用户控制器
 */
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService = new UserService();

    public Result<User> login(String username, String password) {
        try {
            User user = userService.login(username, password);
            if (user != null) {
                return Result.success("登录成功", user);
            } else {
                return Result.fail("用户名或密码错误");
            }
        } catch (BusinessException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            logger.error("登录异常", e);
            return Result.fail("系统发生错误，请联系管理员");
        }
    }

    public Result<Void> register(User user) {
        try {
            userService.register(user);
            return Result.success();
        } catch (BusinessException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            logger.error("注册异常", e);
            return Result.fail("系统发生错误，请联系管理员");
        }
    }
}
