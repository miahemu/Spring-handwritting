package com.juechen.service;

import com.spring.InitializingBean;
import com.spring.annotation.Autowired;
import com.spring.BeanNameAware;
import com.spring.annotation.Component;

/**
 * @Author: Juechen
 * @Date: 2024/4/18
 * @Description: TODO
 * @Version: 1.0
 **/
@Component("userService")
public class UserServiceImpl implements BeanNameAware, UserService , InitializingBean {

    @Autowired
    private OrderService orderService;

    // 希望返回的是userService，不能直接用Autowired注解注入
    private String beanName;

    private String name;

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setBeanName(String name) {
        beanName = name;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("执行了初始化方法~~~");
    }


    public void test() {

        // 输出：com.juechen.service.OrderService@20ad9418
        System.out.println(orderService);

        // 输出：userService
        System.out.println(beanName);

        // 输出：千古玦尘
        System.out.println(name);
    }


}
