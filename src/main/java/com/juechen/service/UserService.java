package com.juechen.service;

import com.spring.Autowired;
import com.spring.BeanNameAware;
import com.spring.Component;
import com.spring.InitializingBean;

/**
 * @Author: Juechen
 * @Date: 2024/4/18
 * @Description: TODO
 * @Version: 1.0
 **/
@Component("userService")
public class UserService implements BeanNameAware, InitializingBean {

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
        System.out.println("初始化");
    }

    public void test() {
        System.out.println(orderService);
        System.out.println(beanName);
        // 输出：千古玦尘
        System.out.println(name);
    }
}
