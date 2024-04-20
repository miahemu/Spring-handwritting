package com.juechen;

import com.juechen.service.UserService;
import com.spring.JuechenApplicationContext;

/**
 * @Author: Juechen
 * @Date: 2024/4/18
 * @Description: 测试类
 * @Version: 1.0
 **/
public class Test {
    public static void main(String[] args) {
        // 启动spring
        JuechenApplicationContext applicationContext = new JuechenApplicationContext(AppConfig.class);

        UserService userService = (UserService) applicationContext.getBean("userService");
        userService.test();  // 1.先执行代理对象  2.然后执行业务类里的test方法

    }
}
