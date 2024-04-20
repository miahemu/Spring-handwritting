package com.spring;

import com.spring.annotation.Autowired;
import com.spring.annotation.Component;
import com.spring.annotation.ComponentScan;
import com.spring.annotation.Scope;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 已完成以下功能：
 * 容器启动
 * BeanDefinition 扫描
 * Bean的生命周期
 * 单例与多例Bean
 * 依赖注入
 * AOP
 * Aware回调
 * 初始化
 * BeanPostProcessor
 *
 * @Author: Juechen
 * @Date: 2024/4/18
 * @Description: spring容器类
 * @Version: 1.0
 **/
public class JuechenApplicationContext {

    private Class configClass;

    // 模拟单例池，讲解单例和多例的区别
    private ConcurrentHashMap<String, Object> singletonObjectsMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    public JuechenApplicationContext(Class configClass) {
        this.configClass = configClass;

        // 解析配置类
        // 这段代码主要用于扫描指定包下的所有类，并根据类上的注解信息生成对应的BeanDefinition对象，然后将这些BeanDefinition对象存储到一个Map中。
        scan(configClass);

        // 不能用foreach单独循环获取key
        // for(String beanName : beanDefinition.keys()){...} (X)
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            if ("singleton".equals(beanDefinition.getScope())) {
                Object bean = creatBean(beanName,beanDefinition);
                // 创建单例作用域的Bean，并将其放入singletonObjectsMap中进行管理
                singletonObjectsMap.put(beanName, bean);
            }
        }
    }


    private void scan(Class configClass) {

        // 1.获取扫描路径
        ComponentScan componentScanAnnotation = (ComponentScan) configClass.getDeclaredAnnotation(ComponentScan.class);
        // 2.拿到扫描路径
        String path = componentScanAnnotation.value();  // com.juechen.service
        path = path.replace(".", "/");  // com/juechen/service

        // 3.扫描
        // 根据包路径扫描类 ---> 这里要用到类加载器,下面是三种常用的类加载器
        // Bootstrap --->jre/lib
        // Ext --------->jre/ext/lib
        // App --------->classpath
        // 3.1 毫无疑问，这里得到的是应用类加载器（App）
        ClassLoader classLoader = JuechenApplicationContext.class.getClassLoader();
        // 3.2 我希望从类加载器中拿到path路径下的资源（相对路径）
        URL resource = classLoader.getResource(path);  // com/juechen/service
        // 3.3 得到了一个目录
        File file = new File(resource.getFile());
        // 判断是否是目录
        if (file.isDirectory()) {

            File[] files = file.listFiles();
            for (File f : files) {
                String fileName = f.getAbsolutePath(); // 得到的是绝对路径，需要“剪枝”得到我想要的路径
                if (fileName.endsWith(".class")) {
                    // 4. 处理类文件
                    String className = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
                    className = className.replace("\\", ".");
                    // 输出:com.juechen.service.UserService 和 com.juechen.service.XxUtil
//                System.out.println(className);


                    Class<?> clazz = null;
                    try {
                        // 加载这个类路径下的文件，className是全限定类名
                        clazz = classLoader.loadClass(className);
                        // 判断是否被@Component注解标记，如果是则认为是一个Bean
                        if (clazz.isAnnotationPresent(Component.class)) {
                            // 5.解析并判断是单例Bean还是多例的
                            // 这里是一个条件判断，用于检查clazz类是否实现了BeanPostProcessor接口
                            // 如果是对象的话，判断就应该是 (xxx instanceof BeanPostProcessor)
                            if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                                // 使用Java反射机制创建clazz类的实例对象,通过getDeclaredConstructor()获取类的构造方法，然后调用newInstance()创建对象
                                BeanPostProcessor instance = (BeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
                                // 这个列表通常用于存储实现了BeanPostProcessor接口的类的实例，用于后续在Bean初始化前后执行一些处理逻辑
                                beanPostProcessorList.add(instance);
                            }

                            Component componentAnnotation = clazz.getDeclaredAnnotation(Component.class);
                            String beanName = componentAnnotation.value();
                            // 6. 生成BeanDefinition对象,并设置类对象和作用域
                            BeanDefinition beanDefinition = new BeanDefinition();
                            beanDefinition.setClazz(clazz);

                            if (clazz.isAnnotationPresent(Scope.class)) {
                                Scope scopeAnnotation = clazz.getDeclaredAnnotation(Scope.class);
                                beanDefinition.setScope(scopeAnnotation.value());
                            } else {
                                // scope不设置的话默认是单例的
                                beanDefinition.setScope("singleton");
                            }
                            // 7.放到beanDefinitionMap集合中
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

    public Object creatBean(String beanName, BeanDefinition beanDefinition) {
        // 根据Bean的定义去创建对象
        Class clazz = beanDefinition.getClazz();
        try {
            // 通过无参的构造方法反射得到一个bean对象
            Object instance = clazz.getDeclaredConstructor().newInstance();
            // 依赖注入(得到orderService属性)
            for (Field declaredField : clazz.getDeclaredFields()) {
                if (declaredField.isAnnotationPresent(Autowired.class)) {
                    Object bean = getBean(declaredField.getName());

                    // 将字段设置为可访问，以便能够设置私有字段的值
                    declaredField.setAccessible(true);

                    // 将获取到的依赖对象（bean）设置到字段上
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

    //用于获取指定名称对应Bean对象的方法
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
