package com.juechen.service;

import com.spring.BeanPostProcessor;
import com.spring.annotation.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @Author: Juechen
 * @Date: 2024/4/19
 * @Description: 自定义的Bean后置处理器,用于在Bean初始化前后执行一些自定义的逻辑
 * @Version: 1.0
 **/
@Component("juechenBeanPostProcessor")
public class JuechenBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("初始化前~~~"); // 因为该目录下有3个类加了@Component注解，故会执行3次
        if ("userService".equals(beanName)) {
            ((UserServiceImpl) bean).setName("千古玦尘");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("初始化后");
        if ("userService".equals(beanName)) {

            // 该类基于实现的接口进行代理，所以用JDK动态代理实现；否则用CGLIB动态代理实现
            // 总之，JDK动态代理基于接口实现，而CGLIB动态代理基于继承实现
            Object proxyInstance = Proxy.newProxyInstance(JuechenBeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    // 这里先会执行代理逻辑，然后才去执行业务test方法
                    System.out.println("代理逻辑");
                    return method.invoke(bean,args);
                }
            });
            return proxyInstance;
        }
        return bean;
    }
}
