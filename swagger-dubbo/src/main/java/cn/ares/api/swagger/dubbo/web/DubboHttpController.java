package cn.ares.api.swagger.dubbo.web;

import cn.ares.api.swagger.dubbo.entity.DubboBeanMethod;
import cn.ares.api.swagger.dubbo.reference.DubboReferenceManager;
import cn.ares.api.swagger.dubbo.util.IoUtil;
import cn.ares.boot.util.common.ClassUtil;
import cn.ares.boot.util.common.StringUtil;
import cn.ares.boot.util.json.JsonUtil;
import io.swagger.v3.oas.annotations.Hidden;
import java.io.ByteArrayOutputStream;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.dubbo.rpc.service.GenericService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("${swagger.dubbo.context-path:ares}")
@Hidden
public class DubboHttpController {

  private static final Logger LOGGER = LoggerFactory.getLogger(DubboHttpController.class);

  @Value("${swagger.dubbo.enable:true}")
  private boolean enable;

  @Autowired
  private DubboReferenceManager referenceManager;

  @RequestMapping(value = "/{interfaceClass}/{methodName}", produces = "application/json; charset=utf-8")
  @ResponseBody
  public ResponseEntity<Object> invokeDubbo(@PathVariable("interfaceClass") String interfaceClass,
      @PathVariable("methodName") String methodName, HttpServletRequest request) {
    return invokeDubbo(interfaceClass, methodName, "", request);
  }

  @RequestMapping(value = "/{interfaceClass}/{methodName}/{operationId}", produces = "application/json; charset=utf-8")
  @ResponseBody
  public ResponseEntity<Object> invokeDubbo(@PathVariable("interfaceClass") String interfaceClass,
      @PathVariable("methodName") String methodName,
      @PathVariable("operationId") String operationId, HttpServletRequest request) {
    if (!enable) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    DubboBeanMethod dubboBeanMethod = referenceManager
        .getDubboBeanMethod(interfaceClass, methodName, operationId);

    if (null == dubboBeanMethod) {
      LOGGER.info("current dubbo interface or method not exist");
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    Method method = dubboBeanMethod.getMethod();
    MethodHandle methodHandle = dubboBeanMethod.getMethodHandle();
    Object bean = dubboBeanMethod.getTarget();

    Object result;
    try {
      Object[] params = new Object[method.getParameterCount()];
      if (method.getParameterCount() != 0) {
        Map<String, Object> newParameterMap = new HashMap<>(16);
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (!parameterMap.isEmpty()) {
          parameterMap.forEach((key, value) -> {
            int openIndex = key.indexOf("[");
            int closeIndex = key.indexOf("].");
            if (StringUtil.isNotEmpty(value[0])) {
              if (openIndex != -1 && closeIndex != -1) {
                String subKey = key.substring(0, openIndex);
                int index = Integer.parseInt(key.substring(openIndex + 1, closeIndex));
                String grandsonKey = key.substring(closeIndex + 2);
                Object subResult = newParameterMap.get(key);
                if (null == subResult) {
                  subResult = new ArrayList<>();
                  newParameterMap.put(subKey, subResult);
                }
                List<Map<String, Object>> list = (List) subResult;
                Map<String, Object> subMap;
                if (index >= list.size()) {
                  subMap = new HashMap<>(16);
                  subMap.put(grandsonKey, value[0]);
                  list.add(index, subMap);
                } else {
                  subMap = list.get(index);
                  subMap.put(grandsonKey, value[0]);
                }
              } else {
                newParameterMap.put(key, value[0]);
              }
            }
          });
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        IoUtil.copy(request.getInputStream(), byteArrayOutputStream);
        String body = byteArrayOutputStream.toString();

        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
          Parameter parameter = parameters[i];
          Class<?> clazz = parameter.getType();
          if (ClassUtil.isBaseOrWrapOrString(clazz)) {
            String paramName = "param" + i;
            io.swagger.v3.oas.annotations.Parameter swaggerParameter = parameter
                .getAnnotation(io.swagger.v3.oas.annotations.Parameter.class);
            if (null != swaggerParameter && StringUtil.isNotEmpty(swaggerParameter.name())) {
              paramName = swaggerParameter.name();
            } else {
              Parameter[] implParameters = bean.getClass()
                  .getDeclaredMethod(method.getName(), method.getParameterTypes()).getParameters();
              swaggerParameter = implParameters[i]
                  .getAnnotation(io.swagger.v3.oas.annotations.Parameter.class);
              if (null != swaggerParameter && StringUtil.isNotEmpty(swaggerParameter.name())) {
                paramName = swaggerParameter.name();
              }
            }
            Object paramValue = newParameterMap.get(paramName);
            if (null == paramValue) {
              paramValue = newParameterMap.get(parameter.getName());
              params[i] = paramValue;
            } else {
              params[i] = JsonUtil.parseObject(paramValue.toString(), clazz);
            }
          } else if (StringUtil.isNotEmpty(body)) {
            params[i] = JsonUtil.parseObject(body, clazz);
          }
        }
      }

      // take precedence use generic invoke, if fail use method handle but that is not execute dubbo filters
      try {
        GenericService genericService = dubboBeanMethod.getGenericService();
        String[] paramTypes = Arrays.stream(method.getParameterTypes()).map(Class::getCanonicalName)
            .toArray(String[]::new);
        result = genericService.$invoke(methodName, paramTypes, params);
        return ResponseEntity.ok(result);
      } catch (Exception e) {
        LOGGER.error("dubbo generic invoke exception: ", e);
      }

      // if method handle is not null use it, if fail use method
      if (methodHandle != null) {
        try {
          if (params.length == 1) {
            result = methodHandle.invoke(bean, params[0]);
          } else if (params.length == 2) {
            result = methodHandle.invoke(bean, params[0], params[1]);
          } else if (params.length == 3) {
            result = methodHandle.invoke(bean, params[0], params[1], params[2]);
          } else if (params.length == 4) {
            result = methodHandle.invoke(bean, params[0], params[1], params[2], params[3]);
          } else if (params.length == 5) {
            result = methodHandle
                .invoke(bean, params[0], params[1], params[2], params[3], params[4]);
          } else if (params.length == 6) {
            result = methodHandle
                .invoke(bean, params[0], params[1], params[2], params[3], params[4], params[5]);
          } else {
            throw new RuntimeException("param length > 6");
          }
          return ResponseEntity.ok(result);
        } catch (Throwable throwable) {
          LOGGER.warn("invoke method handle fail: ", throwable);
        }
      }
      result = method.invoke(bean, params);
    } catch (Throwable throwable) {
      LOGGER.error("invoke service fail: ", throwable);
      if (throwable instanceof InvocationTargetException) {
        InvocationTargetException target = (InvocationTargetException) throwable;
        result = target.getTargetException().getMessage();
      } else {
        result = throwable.getMessage();
      }
    }
    return ResponseEntity.ok(result);
  }

}
