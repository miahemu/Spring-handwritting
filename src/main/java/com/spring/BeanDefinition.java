package com.spring;

/**
 * @Author: Juechen
 * @Date: 2024/4/18
 * @Description: 封装了Bean的所有信息
 * @Version: 1.0
 **/
public class BeanDefinition {

    private Class clazz;
    private String scope;

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
