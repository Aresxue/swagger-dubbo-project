package cn.ares.api.swagger.dubbo.entity;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import org.apache.dubbo.rpc.service.GenericService;

/**
 * @author: Ares
 * @time: 2021-06-28 21:06:00
 * @description: dubbo bean method
 * @version: JDK 1.8
 */
public class DubboBeanMethod {

  private Class<?> interfaceClass;
  /**
   * priority use，performance better than method
   */
  private MethodHandle methodHandle;
  private Method method;
  private Object bean;
  private GenericService genericService;

  public Class<?> getInterfaceClass() {
    return interfaceClass;
  }

  public void setInterfaceClass(Class<?> interfaceClass) {
    this.interfaceClass = interfaceClass;
  }

  public MethodHandle getMethodHandle() {
    return methodHandle;
  }

  public void setMethodHandle(MethodHandle methodHandle) {
    this.methodHandle = methodHandle;
  }

  public Method getMethod() {
    return method;
  }

  public void setMethod(Method method) {
    this.method = method;
  }

  public Object getBean() {
    return bean;
  }

  public void setBean(Object bean) {
    this.bean = bean;
  }

  public GenericService getGenericService() {
    return genericService;
  }

  public void setGenericService(GenericService genericService) {
    this.genericService = genericService;
  }

  @Override
  public String toString() {
    return "DubboBeanMethod{" +
        "interfaceClass=" + interfaceClass +
        ", methodHandle=" + methodHandle +
        ", method=" + method +
        ", bean=" + bean +
        ", genericService=" + genericService +
        '}';
  }
}
