package com.simple.springmvc.v1;

import com.simple.springmvc.anno.SimpleAutowired;
import com.simple.springmvc.anno.SimpleController;
import com.simple.springmvc.anno.SimpleMapping;
import com.simple.springmvc.anno.SimpleService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author simple
 * @version 1.0
 * @date 2019-01-17 09:58
 * @since 1.0
 */
public class DispatcherServlet extends HttpServlet {
    private static final String PROPS_PATH = "simple.properties";
    private String packageName;
    private List<Class<?>> clazzList = new ArrayList<Class<?>>();
    private Map<String, Object> ioc = new HashMap<String, Object>();
    private Map<String, Object> urlClass = new HashMap<String, Object>();
    private Map<String, Method> urlMapping = new HashMap<String, Method>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doDispatch(req, resp);
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 解析请求url, 调用指定方法, 获取结果并返回

        // 为了能调用指定方法, 需要根据注解上的url找到这个方法
        // 需要扫描有注解的方法 并将url与lei实例匹配在一起

        // 初始化加载配置文件, 获取需要扫描的包
        loadProperties();
        // 扫描指定包下的带注解类
        scanPackage();
        // 实例化并保存到IOC容器中
        initIoc();
        // 依赖注入
        doDi();
        // 初始化mapping Map<url, Method>
        initMapping();

        doService(req, resp);
    }

    private void doService(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Method method = urlMapping.get(req.getServletPath());
        if (null == method) {
            resp.getWriter().write("404 Not Found!!!");
            return;
        }
        Object o = urlClass.get(req.getServletPath());
        try {
            Object invoke = method.invoke(o);
            resp.setContentType("application/json;charset=utf-8");
            resp.getWriter().write(invoke.toString());
            return;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        resp.getWriter().write("Hello World!");
    }

    private void initMapping() {
        for (Class<?> clazz : clazzList) {
            Annotation[] clazzAnnotations = clazz.getAnnotations();
            for (Annotation annotation : clazzAnnotations) {
                if (annotation.annotationType().equals(SimpleMapping.class)) {
                    SimpleMapping simpleMapping = (SimpleMapping) annotation;
                    String controllerMapping = simpleMapping.value();
                    Method[] methods = clazz.getMethods();
                    for (Method method : methods) {
                        Annotation[] annotations = method.getAnnotations();
                        for (Annotation annotation1 : annotations) {
                            if (annotation1.annotationType().equals(SimpleMapping.class)) {
                                SimpleMapping annotation2 = (SimpleMapping) annotation1;
                                String methodMapping = annotation2.value();
                                String url = ("/" + controllerMapping + "/" + methodMapping).replaceAll("/+", "/");
                                url = url.substring(0, url.length() - 1);
                                urlMapping.put(url, method);
                                urlClass.put(url, ioc.get(isSubFirstChar(clazz.getSimpleName())));
                            }
                        }
                    }
                }
            }
        }
    }

    private void doDi() {
        for (Class<?> clazz : clazzList) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                Annotation[] annotations = field.getAnnotations();
                for (Annotation annotation : annotations) {
                    if (annotation.annotationType().equals(SimpleAutowired.class)) {
                        if (null != ioc.get(field.getName())) {
                            field.setAccessible(true);
                            try {
                                field.set(ioc.get(isSubFirstChar(clazz.getSimpleName())), ioc.get(field.getName()));
                            } catch (IllegalAccessException e) {
                                System.err.println("注入失败");
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    private void initIoc() {
        if (clazzList.size() == 0) {
            return;
        }
        for (Class<?> next : clazzList) {
            try {
                Object o = next.newInstance();
                ioc.put(null != next.getAnnotation(SimpleService.class) ? next.getAnnotation(SimpleService.class).value() : isSubFirstChar(next.getSimpleName()), o);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
    }

    private String isSubFirstChar(String beanName) {
        char[] chars = beanName.toCharArray();
        chars[0] += 32;
        return new String(chars);
    }

    private void scanPackage() {
        // 扫描包下的带注解的类
        File file = null;
        try {
            file = new File((this.getClass().getClassLoader().getResource("") + packageName.replace(".", "/") + "/").substring(5));
        } catch (Exception e) {
            System.err.println("读取包资源失败");
            e.printStackTrace();
        }
        if (null == file) {
            System.exit(-1);
        }
        scanFile(file);
    }

    private void scanFile(File file) {
        File[] files = file.listFiles();
        if (null == files || files.length == 0) {
            return;
        }
        for (File f : files) {
            if (!f.isFile()) {
                scanFile(f);
            } else {
                String replace = f.getAbsolutePath().replace("/", ".");
                int indexOf = replace.indexOf(packageName);
                String realPackageName = replace.substring(indexOf);
                String className = realPackageName.substring(0, realPackageName.length() - 6);
                try {
                    Class<?> clazz = Class.forName(className);
                    if (clazzList.contains(clazz)) {
                        continue;
                    }
                    Annotation[] annotations = clazz.getAnnotations();
                    for (Annotation annotation : annotations) {
                        if (annotation.annotationType().equals(SimpleController.class)) {
                            clazzList.add(clazz);
                        }
                        if (annotation.annotationType().equals(SimpleService.class)) {
                            clazzList.add(clazz);
                        }
                    }
                } catch (ClassNotFoundException e) {
                    System.out.println("这个不是类");
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadProperties() {
        InputStream inputStream;
        Properties props = new Properties();
        try {
            inputStream = this.getClass().getClassLoader().getResourceAsStream(PROPS_PATH);
            props.load(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        packageName = props.getProperty("scan");
    }
}
