package com.juechen.service;

import com.spring.BeanPostProcessor;
import com.spring.Component;

/**
 * @Author: Juechen
 * @Date: 2024/4/19
 * @Description: TODO
 * @Version: 1.0
 **/
@Component("juechenBeanPostProcessor")
public class JuechenBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("初始化前"); // 将会打印3次
        if ("userService".equals(beanName)) {
            ((UserService)bean).setName("千古玦尘");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("初始化后");
        return bean;
    }
}
