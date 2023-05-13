package cn.ares.api.swagger.common.entity;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

/**
 * @author: Ares
 * @time: 2021-12-22 21:47:55
 * @description: Invoke bean method
 * @description: 用于调用的对象和方法
 * @version: JDK 1.8
 */
public class InvokeMethod {

  /***
   * An instance of the class in which the method is located
   * 方法所在的类的实例
   */
  private Object target;

  private MethodHandle methodHandle;

  private Method method;

  public InvokeMethod() {
  }

  public InvokeMethod(Object target) {
    this.target = target;
  }

  public InvokeMethod(Object target, Method method) {
    this.target = target;
    this.method = method;
  }

  public InvokeMethod(Object target, MethodHandle methodHandle, Method method) {
    this.target = target;
    this.methodHandle = methodHandle;
    this.method = method;
  }

  public Object getTarget() {
    return target;
  }

  public void setTarget(Object target) {
    this.target = target;
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

}
