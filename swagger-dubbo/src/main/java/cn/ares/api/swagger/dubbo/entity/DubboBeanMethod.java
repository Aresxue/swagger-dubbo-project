package cn.ares.api.swagger.dubbo.entity;

import cn.ares.api.swagger.common.entity.InvokeMethod;
import org.apache.dubbo.rpc.service.GenericService;

/**
 * @author: Ares
 * @time: 2021-06-28 21:06:00
 * @description: Dubbo bean method
 * @version: JDK 1.8
 */
public class DubboBeanMethod extends InvokeMethod {

  private Class<?> interfaceClass;
  /**
   * priority use，performance better than method
   */

  private GenericService genericService;

  public Class<?> getInterfaceClass() {
    return interfaceClass;
  }

  public void setInterfaceClass(Class<?> interfaceClass) {
    this.interfaceClass = interfaceClass;
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
        ", methodHandle=" + super.getMethodHandle() +
        ", method=" + super.getMethod() +
        ", bean=" + super.getTarget() +
        ", genericService=" + genericService +
        '}';
  }

}
