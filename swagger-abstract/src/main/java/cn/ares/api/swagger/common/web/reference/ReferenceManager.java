package cn.ares.api.swagger.common.web.reference;

import cn.ares.api.swagger.common.entity.InvokeMethod;
import java.util.List;

/**
 * @author: Ares
 * @time: 2022-06-28 12:59:21
 * @description: Reference manager
 * @version: JDK 1.8
 */
public interface ReferenceManager {

  List<InvokeMethod> getInvokeMethodList(String interfaceClass);

}
