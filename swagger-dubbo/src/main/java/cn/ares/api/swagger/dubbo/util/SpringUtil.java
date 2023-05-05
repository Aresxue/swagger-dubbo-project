package cn.ares.api.swagger.dubbo.util;


import static cn.ares.api.swagger.dubbo.constant.SymbolConstant.SPOT;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_SUPPORT;
import static org.springframework.context.annotation.AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Role;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

/**
 * @author: Ares
 * @time: 2021-05-31 16:18:00
 * @description: Spring相关处理的工具类
 * @description: 部分功能需要在bean初始化之后使用
 * @version: JDK 1.8
 */
@Component(value = "bootSpringUtil")
@Role(value = ROLE_SUPPORT)
public class SpringUtil implements ApplicationContextAware, BeanClassLoaderAware, BeanFactoryAware {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringUtil.class);

  private static ApplicationContext applicationContext;
  private static SingletonBeanRegistry beanFactory;

  private static ClassLoader classLoader;

  /**
   * @author: Ares
   * @description: get component scanning packages
   * @time: 2021-11-24 18:14:00
   * @params: [registry, allowEmpty] registry, allow package name empty
   * @return: java.util.Set<java.lang.String> package set
   */
  public static Set<String> getComponentScanningPackages(BeanDefinitionRegistry registry,
      boolean allowEmpty) {
    Set<String> packageSet = new LinkedHashSet<>();
    String[] names = registry.getBeanDefinitionNames();
    for (String name : names) {
      BeanDefinition definition = registry.getBeanDefinition(name);
      if (definition instanceof AnnotatedBeanDefinition) {
        AnnotatedBeanDefinition annotatedDefinition = (AnnotatedBeanDefinition) definition;
        SpringUtil.addComponentScanningPackageSet(packageSet, annotatedDefinition.getMetadata());
      }
    }
    if (!allowEmpty) {
      packageSet = packageSet.stream().filter(StringUtil::isNotEmpty).collect(Collectors.toSet());
    }
    return packageSet;
  }

  /**
   * @author: Ares
   * @description: get component scanning packages with don't allow empty
   * @description: 获取要扫描的包的集合且如果为空且剔除
   * @time: 2021-12-21 19:48:35
   * @params: [registry] registry bean定义注册器
   * @return: java.util.Set<java.lang.String> scan package set 扫描包集合
   */
  public static Set<String> getComponentScanningPackageSet(BeanDefinitionRegistry registry) {
    return getComponentScanningPackages(registry, false);
  }


  /**
   * @author: Ares
   * @description: 通过名称获取bean
   * @time: 2021-05-31 16:21:00
   * @params: [beanName] Bean的名称
   * @return: java.lang.Object Bean
   */
  public static Object getBean(String beanName) {
    return getApplicationContext().getBean(beanName);
  }

  /**
   * @author: Ares
   * @description: 通过class获取Bean
   * @time: 2021-05-31 16:22:00
   * @params: [clazz] class
   * @return: T Bean
   */
  public static <T> T getBean(Class<T> clazz) {
    return getApplicationContext().getBean(clazz);
  }

  /**
   * @author: Ares
   * @description: 通过class获取Bean
   * @time: 2021-05-31 16:23:00
   * @params: [beanName, clazz] Bean名称, class
   * @return: T Bean
   */
  public static <T> T getBean(String beanName, Class<T> clazz) {
    return getApplicationContext().getBean(beanName, clazz);
  }

  /**
   * @author: Ares
   * @description: 判断是否包含Bean
   * @time: 2021-05-31 16:26:00
   * @params: [beanName] Bean的名称
   * @return: boolean true为包含
   */
  public static boolean containsBean(String beanName) {
    return getApplicationContext().containsBean(beanName);
  }

  /**
   * @author: Ares
   * @description: 判断Bean是否是单例
   * @time: 2021-05-31 16:26:00
   * @params: [beanName] Bean的名称
   * @return: boolean true为是单例
   */
  public static boolean isSingleton(String beanName) {
    return getApplicationContext().isSingleton(beanName);
  }

  /**
   * @author: Ares
   * @description: 获取Bean的Class
   * @time: 2021-05-31 16:28:00
   * @params: [beanName] Bean的名称
   * @return: java.lang.Class Class
   */
  public static Class<?> getType(String beanName) {
    return getApplicationContext().getType(beanName);
  }

  /**
   * @author: Ares
   * @description: 通过类型获取所有bean
   * @time: 2019/6/1 14:23
   * @params: [clazz] 请求参数
   * @return: java.util.Map<java.lang.String
   */
  public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
    return getApplicationContext().getBeansOfType(clazz);
  }

  /**
   * @author: Ares
   * @description: 获取所有bean(包括父应用上下文)
   * @time: 2022-12-30 14:36:04
   * @params: [clazz] in 入参
   * @return: java.util.Map<java.lang.String, T> out 出参
   */
  public static <T> Map<String, T> getAllBeansOfType(Class<T> clazz) {
    Map<String, T> beanMap = MapUtil.newHashMap(16);
    getAllBeansOfType(clazz, getApplicationContext(), beanMap);
    return beanMap;
  }

  private static <T> void getAllBeansOfType(Class<T> clazz,
      ApplicationContext applicationContext, Map<String, T> beanMap) {
    beanMap.putAll(applicationContext.getBeansOfType(clazz));
    Optional.ofNullable(applicationContext.getParent()).ifPresent(
        parentApplicationContext -> getAllBeansOfType(clazz, parentApplicationContext, beanMap));
  }


  public static String[] getBeanNamesForAnnotation(Class<? extends Annotation> clazz) {
    return getApplicationContext().getBeanNamesForAnnotation(clazz);
  }

  public static Map<String, Object> getBeansWithAnnotation(
      Class<? extends Annotation> annotationType) {
    return getApplicationContext().getBeansWithAnnotation(annotationType);
  }

  public static String[] getBeanNamesForType(@Nullable Class<?> type) {
    return getApplicationContext().getBeanNamesForType(type);
  }

  public static String[] getBeanNamesForType(@Nullable Class<?> type, boolean includeNonSingletons,
      boolean allowEagerInit) {
    return getApplicationContext().getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
  }

  public static String[] getBeanDefinitionNames() {
    return getApplicationContext().getBeanDefinitionNames();
  }

  public static void registerSingleton(String beanName, Object singletonObject) {
    beanFactory.registerSingleton(beanName, singletonObject);
  }

  public static <T> void registerBean(@Nullable String beanName, Class<T> beanClass,
      @Nullable Supplier<T> supplier, BeanDefinitionCustomizer... customizers) {
    GenericApplicationContext genericApplicationContext = (GenericApplicationContext) getApplicationContext();
    genericApplicationContext.registerBean(beanName, beanClass, supplier, customizers);
  }

  public static Environment getEnvironment() {
    return getApplicationContext().getEnvironment();
  }


  /**
   * @author: Ares
   * @description: 获取所有的Bean
   * @time: 2019-06-11 15:32:00
   * @params: [] 请求参数
   * @return: java.util.List<java.lang.Class ?>> 响应参数
   */
  public static List<Class<?>> getAllBeans() {
    List<Class<?>> result = new LinkedList<>();
    String[] beans = getApplicationContext().getBeanDefinitionNames();
    for (String beanName : beans) {
      Class<?> clazz = getType(beanName);
      result.add(clazz);
    }
    return result;
  }

  /**
   * @author: Ares
   * @description: 容器启动完成后在运行时根据前缀获取spring的配置信息
   * @time: 2020-03-19 0:15:00
   * @params: [prefix, retainPrefix] 前缀, 是否保留前缀
   * @return: java.util.Properties 响应参数
   */
  public static Properties getPropertiesByPrefix(String prefix, boolean retainPrefix) {
    Properties properties = new Properties();
    StandardEnvironment standardEnvironment = (StandardEnvironment) getApplicationContext().getEnvironment();
    standardEnvironment.getPropertySources().forEach(propertySource -> {
      if (propertySource instanceof MapPropertySource) {
        MapPropertySource mapPropertySource = (MapPropertySource) propertySource;
        mapPropertySource.getSource().forEach((key, value) -> {
          if (key.startsWith(prefix)) {
            String propertyKey = retainPrefix ? key : StringUtil.replace(key, prefix + SPOT, "");
            if (!properties.containsKey(propertyKey)) {
              properties.put(propertyKey, value);
              properties.setProperty(propertyKey, String.valueOf(value));
            }
          }
        });
      }
    });
    return properties;
  }

  public static String resolvePlaceholders(String text) {
    return getEnvironment().resolvePlaceholders(text);
  }

  /**
   * @author: Ares
   * @description: 容器启动完成后在运行时根据前缀获取spring的配置信息(不保留前缀)
   * @time: 2020-03-19 0:15:00
   * @params: [prefix] 前缀
   * @return: java.util.Properties 响应参数
   */
  public static Properties getPropertiesByPrefix(String prefix) {
    return getPropertiesByPrefix(prefix, false);
  }

  /**
   * @author: Ares
   * @description: add componentScanning packages
   * @time: 2021/11/24 16:49
   * @params: [packages, metadata] request
   * @return: void response
   */
  public static void addComponentScanningPackageSet(Set<String> packageSet,
      AnnotationMetadata metadata) {
    AnnotationAttributes attributes = AnnotationAttributes.fromMap(
        metadata.getAnnotationAttributes(ComponentScan.class.getName(), true));
    if (attributes != null) {
      addPackages(packageSet, attributes.getStringArray("value"));
      addPackages(packageSet, attributes.getStringArray("basePackages"));
      addClasses(packageSet, attributes.getStringArray("basePackageClasses"));
      if (packageSet.isEmpty()) {
        packageSet.add(ClassUtils.getPackageName(metadata.getClassName()));
      }
    }
  }

  public static void addPackages(Set<String> packages, String[] values) {
    if (values != null) {
      Collections.addAll(packages, values);
    }
  }

  public static void addClasses(Set<String> packages, String[] values) {
    if (values != null) {
      for (String value : values) {
        packages.add(ClassUtils.getPackageName(value));
      }
    }
  }

  public static BeanNameGenerator resolveBeanNameGenerator(BeanDefinitionRegistry registry) {

    BeanNameGenerator beanNameGenerator = null;

    if (registry instanceof SingletonBeanRegistry) {
      SingletonBeanRegistry singletonBeanRegistry = (SingletonBeanRegistry) registry;
      beanNameGenerator = (BeanNameGenerator) singletonBeanRegistry.getSingleton(
          CONFIGURATION_BEAN_NAME_GENERATOR);
    }

    if (beanNameGenerator == null) {
      LOGGER.info("BeanNameGenerator bean can't be found in BeanFactory with name ["
          + CONFIGURATION_BEAN_NAME_GENERATOR + "]");
      LOGGER.info(
          "BeanNameGenerator will be a instance of " + AnnotationBeanNameGenerator.class.getName()
              + " , it maybe a potential problem on bean name generation.");

      beanNameGenerator = new AnnotationBeanNameGenerator();

    }

    return beanNameGenerator;

  }

  public static Set<BeanDefinitionHolder> findServiceBeanDefinitionHolders(
      ClassPathBeanDefinitionScanner scanner, String packageToScan, BeanDefinitionRegistry registry,
      BeanNameGenerator beanNameGenerator) {
    Set<BeanDefinition> beanDefinitions = scanner.findCandidateComponents(packageToScan);

    Set<BeanDefinitionHolder> beanDefinitionHolders = new LinkedHashSet<>(beanDefinitions.size());

    for (BeanDefinition beanDefinition : beanDefinitions) {

      String beanName = beanNameGenerator.generateBeanName(beanDefinition, registry);
      BeanDefinitionHolder beanDefinitionHolder = new BeanDefinitionHolder(beanDefinition,
          beanName);
      beanDefinitionHolders.add(beanDefinitionHolder);
    }

    return beanDefinitionHolders;

  }

  public static Class<?> resolveClass(BeanDefinitionHolder beanDefinitionHolder,
      ClassLoader classLoader) {
    BeanDefinition beanDefinition = beanDefinitionHolder.getBeanDefinition();
    return resolveClass(beanDefinition, classLoader);
  }

  public static Class<?> resolveClass(BeanDefinitionHolder beanDefinitionHolder) {
    BeanDefinition beanDefinition = beanDefinitionHolder.getBeanDefinition();
    return resolveClass(beanDefinition, classLoader);
  }

  public static Class<?> resolveClass(BeanDefinition beanDefinition, ClassLoader classLoader) {
    String beanClassName = beanDefinition.getBeanClassName();
    if (StringUtil.isEmpty(beanClassName)) {
      return null;
    }
    return ClassUtils.resolveClassName(beanClassName, classLoader);
  }

  public static Class<?> resolveClass(BeanDefinition beanDefinition) {
    String beanClassName = beanDefinition.getBeanClassName();
    if (StringUtil.isEmpty(beanClassName)) {
      return null;
    }
    return ClassUtils.resolveClassName(beanClassName, classLoader);
  }

  private static ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  @Override
  public void setApplicationContext(@NonNull ApplicationContext applicationContext)
      throws BeansException {
    SpringUtil.applicationContext = applicationContext;
  }

  @Override
  public void setBeanClassLoader(@NonNull ClassLoader classLoader) {
    SpringUtil.classLoader = classLoader;
  }

  @Override
  public void setBeanFactory(@NonNull BeanFactory beanFactory) throws BeansException {
    SpringUtil.beanFactory = (SingletonBeanRegistry) beanFactory;
  }

}
