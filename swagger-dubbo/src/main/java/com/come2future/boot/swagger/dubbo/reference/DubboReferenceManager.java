package com.come2future.boot.swagger.dubbo.reference;

import static com.come2future.boot.swagger.dubbo.constant.CommonConstant.SLASH;
import static com.come2future.boot.swagger.dubbo.constant.CommonConstant.UNIQUE_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.GENERIC_SERIALIZATION_DEFAULT;
import static org.apache.dubbo.rpc.Constants.SCOPE_LOCAL;

import com.come2future.boot.swagger.dubbo.entity.DubboBeanMethod;
import com.come2future.boot.swagger.dubbo.util.CollectionUtil;
import com.come2future.boot.swagger.dubbo.util.SpringUtil;
import com.come2future.boot.swagger.dubbo.util.StringUtil;
import io.swagger.v3.oas.annotations.Operation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.apache.dubbo.config.utils.ReferenceConfigCache;
import org.apache.dubbo.rpc.service.GenericService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class DubboReferenceManager implements ApplicationContextAware {

  private static final Logger LOGGER = LoggerFactory.getLogger(DubboReferenceManager.class);

  public static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

  private static final Map<String, DubboBeanMethod> INTERFACE_MAP_REF = new ConcurrentHashMap<>();

  private static String staticRegistry;
  private static List<RegistryConfig> registries;

  private final ReferenceConfigCache cache = ReferenceConfigCache.getCache();

  private ApplicationContext applicationContext;

  public DubboBeanMethod getDubboBeanMethod(String interfaceClass,
      String methodName, String operationId) {
    if (!StringUtils.isEmpty(operationId)) {
      operationId = SLASH + operationId;
    }
    String key = String.format(UNIQUE_KEY, interfaceClass, methodName, operationId);
    return INTERFACE_MAP_REF.get(key);
  }


  public List<DubboBeanMethod> getDubboBeanMethodList(String interfaceClass) {
    List<DubboBeanMethod> dubboBeanMethodList = new ArrayList<>();
    INTERFACE_MAP_REF.forEach((key, value) -> {
      if (key.startsWith(SLASH + interfaceClass)) {
        dubboBeanMethodList.add(value);
      }
    });
    return dubboBeanMethodList;
  }

  public Map<String, DubboBeanMethod> getDubboBeanMethod() {
    if (INTERFACE_MAP_REF.isEmpty()) {
      applicationContext.getBeansOfType(ServiceConfig.class).values().forEach(
          bean -> {
            Class<?> clazz = bean.getInterfaceClass();
            String clazzName = clazz.getCanonicalName();
            Object impl = bean.getRef();

            ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
            reference.setBootstrap(bean.getBootstrap());
            List<RegistryConfig> registryConfigList = bean.getRegistries();
            if (CollectionUtil.isNotEmpty(registryConfigList)) {
              reference.setRegistries(registryConfigList);
            } else {
              if (CollectionUtil.isNotEmpty(registries)) {
                reference.setRegistries(registryConfigList);
              } else {
                registries = new ArrayList<>(
                    SpringUtil.getAllBeansOfType(RegistryConfig.class).values());
                registries.forEach(registry -> {
                  if (StringUtil.isEmpty(registry.getAddress())) {
                    registry.refresh();
                  }
                });
                if (CollectionUtil.isNotEmpty(registries)) {
                  reference.setRegistries(registries);
                } else {
                  RegistryConfig registryConfig = new RegistryConfig();
                  registryConfig.setAddress(staticRegistry);
                  reference.setRegistry(registryConfig);
                }
              }
            }

            reference.setScope(SCOPE_LOCAL);
            reference.setGeneric(GENERIC_SERIALIZATION_DEFAULT);
            reference.setInterface(bean.getInterfaceClass());
            reference.setGroup(bean.getGroup());
            reference.setVersion(bean.getVersion());
            GenericService genericService = cache.get(reference);

            for (Method method : clazz.getDeclaredMethods()) {

              String operationId = "";
              Operation operation = method.getAnnotation(Operation.class);
              if (null != operation && !StringUtils.isEmpty(operation.operationId())) {
                operationId = SLASH + operation.operationId();
              } else {
                try {
                  Method implMethod = impl.getClass()
                      .getDeclaredMethod(method.getName(), method.getParameterTypes());
                  operation = implMethod.getAnnotation(Operation.class);
                  if (null != operation && !StringUtils.isEmpty(operation.operationId())) {
                    operationId = SLASH + operation.operationId();
                  }
                } catch (NoSuchMethodException ignored) {
                }
              }

              String key = String.format(UNIQUE_KEY, clazzName, method.getName(), operationId);

              DubboBeanMethod dubboBeanMethod = new DubboBeanMethod();
              dubboBeanMethod.setInterfaceClass(clazz);
              method.setAccessible(true);
              dubboBeanMethod.setMethod(method);
              dubboBeanMethod.setBean(impl);

              dubboBeanMethod.setGenericService(genericService);

              try {
                MethodHandle methodHandle = LOOKUP.unreflect(method);
                dubboBeanMethod.setMethodHandle(methodHandle);
              } catch (IllegalAccessException e) {
                LOGGER.warn("get bean method or method handle fail, exception message: {}",
                    e.getMessage());
              }
              INTERFACE_MAP_REF.putIfAbsent(key, dubboBeanMethod);
            }
          }
      );
    }
    return INTERFACE_MAP_REF;
  }

  @Override
  public void setApplicationContext(@NonNull ApplicationContext applicationContext)
      throws BeansException {
    this.applicationContext = applicationContext;
  }

  @Value("${dubbo.registry.address:}")
  public void setStaticRegistry(String staticRegistry) {
    DubboReferenceManager.staticRegistry = staticRegistry;
  }

}
