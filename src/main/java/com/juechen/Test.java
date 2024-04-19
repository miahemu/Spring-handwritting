package com.juechen;

import com.juechen.service.UserService;
import com.spring.JuechenApplicationContext;

/**
 * @Author: Juechen
 * @Date: 2024/4/18
 * @Description: 测试
 * @Version: 1.0
 **/
public class Test {
    public static void main(String[] args) {
        JuechenApplicationContext applicationContext = new JuechenApplicationContext(AppConfig.class);

        UserService userService = (UserService) applicationContext.getBean("userService");
        userService.test();

    }
}
