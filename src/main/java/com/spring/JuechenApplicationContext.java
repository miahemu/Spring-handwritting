package com.spring;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: Juechen
 * @Date: 2024/4/18
 * @Description: spring容器类
 * @Version: 1.0
 **/
public class JuechenApplicationContext {

    private Class configClass;

    private ConcurrentHashMap<String, Object> singletonObjectsMap = new ConcurrentHashMap<>();// 模拟单例池，讲解单例和多例的区别
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    public JuechenApplicationContext(Class configClass) {
        this.configClass = configClass;

        // 解析配置类
        scan(configClass);

        // 不能用foreach单独循环获取key
        // for(String beanName : beanDefinition.keys()){...}
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            if ("singleton".equals(beanDefinition.getScope())) {
                Object bean = creatBean(beanName,beanDefinition); // 单例Bean
                singletonObjectsMap.put(beanName, bean);
            }
        }
    }

    public Object creatBean(String beanName, BeanDefinition beanDefinition) {
        // 根据Bean的定义去创建对象
        Class clazz = beanDefinition.getClazz();
        // 通过无参的构造方法反射得到一个bean对象
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();
            // 依赖注入(得到orderService属性)
            for (Field declaredField : clazz.getDeclaredFields()) {
                if (declaredField.isAnnotationPresent(Autowired.class)) {
                    Object bean = getBean(declaredField.getName());
                    declaredField.setAccessible(true);
                    declaredField.set(instance, bean);
                }
            }
            // Aware回调模拟实现(输出：userService)
            if (instance instanceof BeanNameAware) {
                ((BeanNameAware) instance).setBeanName(beanName);
            }

            // 初始化前
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
            }

            // 初始化
            if (instance instanceof InitializingBean) {
                try {
                    ((InitializingBean) instance).afterPropertiesSet();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            // 初始化后
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessAfterInitialization(instance, beanName);
            }

            return instance;

        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }


    private void scan(Class configClass) {

        // 1.找到存在ComponentScan的路径
        ComponentScan componentScanAnnotation = (ComponentScan) configClass.getDeclaredAnnotation(ComponentScan.class);
        // 2.拿到扫描路径
        String path = componentScanAnnotation.value();//  com.juechen.service
        path = path.replace(".", "/");

        // 3.扫描
        // 根据包名得到得到包下所有的类，这里要用到类加载器
        // Bootstrap --->jre/lib
        // Ext --------->jre/ext/lib
        // App --------->classpath
        // 3.1 得到应用类加载器（App）
        ClassLoader classLoader = JuechenApplicationContext.class.getClassLoader();
        // 3.2 我希望从类加载器中拿到path路径下的资源（相对路径）
        URL resource = classLoader.getResource(path);
        // 3.3 得到了一个目录
        File file = new File(resource.getFile());
        // 判断是否是目录
        if (file.isDirectory()) {

            File[] files = file.listFiles();
            for (File f : files) {
                String fileName = f.getAbsolutePath(); // 得到的是绝对路径，需要“剪枝”为了后续准备
                if (fileName.endsWith(".class")) {
                    // 剪枝操作
                    String className = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
                    className = className.replace("\\", ".");
                    // 输出:com.juechen.service.UserService 和 com.juechen.service.XxUtil
//                System.out.println(className);


                    Class<?> clazz = null;
                    try {
                        // 加载这个类路径下的文件，className是全限定类名
                        clazz = classLoader.loadClass(className);
                        if (clazz.isAnnotationPresent(Component.class)) {
                            // 当前这个类是一个Bean
                            // 4.解析并判断是单例Bean还是多例的,生成BeanDefinition对象

                            if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                                // 先得到这个对象才能调用它的方法
                                BeanPostProcessor instance = (BeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
                                beanPostProcessorList.add(instance);
                            }


                            Component componentAnnotation = clazz.getDeclaredAnnotation(Component.class);
                            String beanName = componentAnnotation.value();
                            // 这不是一个Bean，这只是一个Bean的定义
                            BeanDefinition beanDefinition = new BeanDefinition();
                            beanDefinition.setClazz(clazz);

                            if (clazz.isAnnotationPresent(Scope.class)) {
                                Scope scopeAnnotation = clazz.getDeclaredAnnotation(Scope.class);
                                beanDefinition.setScope(scopeAnnotation.value());
                            } else {
                                // scope不设置的话默认是单例的
                                beanDefinition.setScope("singleton");
                            }
                            // 5.放到Map集合中
                            beanDefinitionMap.put(beanName, beanDefinition);
                        }
                    } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException |
                             IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }

    public Object getBean(String beanName) {
        if (beanDefinitionMap.containsKey(beanName)) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if ("singleton".equals(beanDefinition.getScope())) {
                // 这样就可以保证多次调用getBean对象的时候，单例bean返回的都是一个bean对象
                return singletonObjectsMap.get(beanName);
            } else {
                // 多例，每一次getBean的时候我们都要去创建新对象
                return creatBean(beanName,beanDefinition);

            }
        } else {
            // 不存在对应的Bean
            throw new NullPointerException();
        }
    }
}
