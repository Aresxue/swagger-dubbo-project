package com.come2future.boot.swagger.dubbo.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.aop.Advisor;
import org.springframework.aop.AopInvocationException;
import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.IntroductionAwareMethodMatcher;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.TargetClassAware;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.MethodIntrospector;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * @author: Ares
 * @time: 2021-06-29 10:26:00
 * @description: aop operate util
 * @version: JDK 1.8
 */
public class AopUtil {

  public static boolean isAopProxy(@Nullable Object object) {
    return object instanceof SpringProxy && (Proxy.isProxyClass(object.getClass()) || object
        .getClass().getName().contains("$$"));
  }

  public static boolean isJdkDynamicProxy(@Nullable Object object) {
    return object instanceof SpringProxy && Proxy.isProxyClass(object.getClass());
  }

  public static boolean isCglibProxy(@Nullable Object object) {
    return object instanceof SpringProxy && object.getClass().getName().contains("$$");
  }

  public static Class<?> getTargetClass(Object candidate) {
    Assert.notNull(candidate, "Candidate object must not be null");
    Class<?> result = null;
    if (candidate instanceof TargetClassAware) {
      result = ((TargetClassAware) candidate).getTargetClass();
    }

    if (result == null) {
      result =
          isCglibProxy(candidate) ? candidate.getClass().getSuperclass() : candidate.getClass();
    }

    return result;
  }

  public static Method selectInvocableMethod(Method method, @Nullable Class<?> targetType) {
    if (targetType == null) {
      return method;
    } else {
      Method methodToUse = MethodIntrospector.selectInvocableMethod(method, targetType);
      if (Modifier.isPrivate(methodToUse.getModifiers()) && !Modifier
          .isStatic(methodToUse.getModifiers()) && SpringProxy.class.isAssignableFrom(targetType)) {
        throw new IllegalStateException(String.format(
            "Need to invoke method '%s' found on proxy for target class '%s' but cannot be delegated to target bean. Switch its visibility to package or protected.",
            method.getName(), method.getDeclaringClass().getSimpleName()));
      } else {
        return methodToUse;
      }
    }
  }

  public static boolean isEqualsMethod(@Nullable Method method) {
    return ReflectionUtils.isEqualsMethod(method);
  }

  public static boolean isHashCodeMethod(@Nullable Method method) {
    return ReflectionUtils.isHashCodeMethod(method);
  }

  public static boolean isToStringMethod(@Nullable Method method) {
    return ReflectionUtils.isToStringMethod(method);
  }

  public static boolean isFinalizeMethod(@Nullable Method method) {
    return method != null && "finalize".equals(method.getName()) && method.getParameterCount() == 0;
  }

  public static Method getMostSpecificMethod(Method method, @Nullable Class<?> targetClass) {
    Class<?> specificTargetClass =
        targetClass != null ? ClassUtils.getUserClass(targetClass) : null;
    Method resolvedMethod = ClassUtils.getMostSpecificMethod(method, specificTargetClass);
    return BridgeMethodResolver.findBridgedMethod(resolvedMethod);
  }

  public static boolean canApply(Pointcut pc, Class<?> targetClass) {
    return canApply(pc, targetClass, false);
  }

  public static boolean canApply(Pointcut pc, Class<?> targetClass, boolean hasIntroductions) {
    Assert.notNull(pc, "Pointcut must not be null");
    if (!pc.getClassFilter().matches(targetClass)) {
      return false;
    } else {
      MethodMatcher methodMatcher = pc.getMethodMatcher();
      if (methodMatcher == MethodMatcher.TRUE) {
        return true;
      } else {
        IntroductionAwareMethodMatcher introductionAwareMethodMatcher = null;
        if (methodMatcher instanceof IntroductionAwareMethodMatcher) {
          introductionAwareMethodMatcher = (IntroductionAwareMethodMatcher) methodMatcher;
        }

        Set<Class<?>> classes = new LinkedHashSet<>();
        if (!Proxy.isProxyClass(targetClass)) {
          classes.add(ClassUtils.getUserClass(targetClass));
        }

        classes.addAll(ClassUtils.getAllInterfacesForClassAsSet(targetClass));

        for (Class<?> clazz : classes) {
          Method[] methods = ReflectionUtils.getAllDeclaredMethods(clazz);

          for (Method method : methods) {
            if (introductionAwareMethodMatcher != null) {
              if (introductionAwareMethodMatcher.matches(method, targetClass, hasIntroductions)) {
                return true;
              }
            } else if (methodMatcher.matches(method, targetClass)) {
              return true;
            }
          }
        }

        return false;
      }
    }
  }

  public static boolean canApply(Advisor advisor, Class<?> targetClass) {
    return canApply(advisor, targetClass, false);
  }

  public static boolean canApply(Advisor advisor, Class<?> targetClass, boolean hasIntroductions) {
    if (advisor instanceof IntroductionAdvisor) {
      return ((IntroductionAdvisor) advisor).getClassFilter().matches(targetClass);
    } else if (advisor instanceof PointcutAdvisor) {
      PointcutAdvisor pca = (PointcutAdvisor) advisor;
      return canApply(pca.getPointcut(), targetClass, hasIntroductions);
    } else {
      return true;
    }
  }

  public static List<Advisor> findAdvisorsThatCanApply(List<Advisor> candidateAdvisors,
      Class<?> clazz) {
    if (candidateAdvisors.isEmpty()) {
      return candidateAdvisors;
    } else {
      List<Advisor> eligibleAdvisors = new ArrayList<>();

      for (Advisor candidate : candidateAdvisors) {
        if (candidate instanceof IntroductionAdvisor && canApply(candidate, clazz)) {
          eligibleAdvisors.add(candidate);
        }
      }

      boolean hasIntroductions = !eligibleAdvisors.isEmpty();

      for (Advisor candidate : candidateAdvisors) {
        if (!(candidate instanceof IntroductionAdvisor) && canApply(candidate, clazz,
            hasIntroductions)) {
          eligibleAdvisors.add(candidate);
        }
      }

      return eligibleAdvisors;
    }
  }

  @Nullable
  public static Object invokeJoinPointUsingReflection(@Nullable Object target, Method method,
      Object[] args) throws Throwable {
    try {
      method.setAccessible(true);
      return method.invoke(target, args);
    } catch (InvocationTargetException var4) {
      throw var4.getTargetException();
    } catch (IllegalArgumentException var5) {
      throw new AopInvocationException(
          "AOP configuration seems to be invalid: tried calling method [" + method + "] on target ["
              + target + "]", var5);
    } catch (IllegalAccessException var6) {
      throw new AopInvocationException("Could not access method [" + method + "]", var6);
    }finally {
      method.setAccessible(false);
    }
  }


  /**
   * @author: Ares
   * @description: get proxy object
   * @description: 获取代理对象
   * @time: 2021-06-29 10:36:00
   * @params: [proxy] 代理对象
   * @return: java.lang.Object response
   */
  public static Object getTarget(Object proxy) throws Exception {
    if (!isAopProxy(proxy)) {
      return proxy;
    } else {
      return isJdkDynamicProxy(proxy) ? getJdkDynamicProxyTargetObject(proxy)
          : getCglibProxyTargetObject(proxy);
    }
  }


  /**
   * @author: Ares
   * @description: get cglib proxy object
   * @description: 获取cglib代理对象
   * @time: 2021-06-29 10:36:00
   * @params: [proxy] request
   * @return: java.lang.Object response
   */
  public static Object getCglibProxyTargetObject(Object proxy) throws Exception {
    Field field = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
    field.setAccessible(true);
    Object dynamicAdvisedInterceptor = field.get(proxy);
    field.setAccessible(false);
    Field advised = dynamicAdvisedInterceptor.getClass().getDeclaredField("advised");
    field.setAccessible(false);
    advised.setAccessible(true);
    return ((AdvisedSupport) advised.get(dynamicAdvisedInterceptor)).getTargetSource()
        .getTarget();
  }

  /**
   * @author: Ares
   * @description: get jdk proxy object
   * @description: 获取jdk代理对象
   * @time: 2021-06-29 10:37:00
   * @params: [proxy] request
   * @return: java.lang.Object response
   */
  public static Object getJdkDynamicProxyTargetObject(Object proxy) throws Exception {
    Field field = proxy.getClass().getSuperclass().getDeclaredField("h");
    field.setAccessible(true);
    AopProxy aopProxy = (AopProxy) field.get(proxy);
    field.setAccessible(false);
    Field advised = aopProxy.getClass().getDeclaredField("advised");
    field.setAccessible(false);
    advised.setAccessible(true);
    return ((AdvisedSupport) advised.get(aopProxy)).getTargetSource().getTarget();
  }

}
