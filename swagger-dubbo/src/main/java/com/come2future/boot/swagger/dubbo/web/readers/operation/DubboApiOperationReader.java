package com.come2future.boot.swagger.dubbo.web.readers.operation;

import static java.util.Arrays.asList;

import com.come2future.boot.swagger.dubbo.web.plugins.DubboDocumentationPluginsManager;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.OperationNameGenerator;
import springfox.documentation.builders.OperationBuilder;
import springfox.documentation.service.Operation;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.spi.service.contexts.RequestMappingContext;
import springfox.documentation.spring.web.readers.operation.OperationReader;

/**
 * @author: Ares
 * @time: 2021/7/2 13:18
 * @description: coy from springfox.documentation.spring.web.readers.operation.ApiOperationReader
 * @version: JDK 1.8
 * @see springfox.documentation.spring.web.readers.operation.ApiOperationReader
 */
@Component
public class DubboApiOperationReader implements OperationReader {

  private static final Set<RequestMethod> REQUEST_METHODS = new LinkedHashSet<>(asList(RequestMethod.values()));

  private final DubboDocumentationPluginsManager pluginsManager;
  private final OperationNameGenerator nameGenerator;

  @Autowired
  public DubboApiOperationReader(DubboDocumentationPluginsManager pluginsManager,
      OperationNameGenerator nameGenerator) {
    this.pluginsManager = pluginsManager;
    this.nameGenerator = nameGenerator;
  }

  @Override
  public List<Operation> read(RequestMappingContext outerContext) {

    List<Operation> operations = new ArrayList<>();

    Set<RequestMethod> requestMethods = outerContext.getMethodsCondition();
    Set<RequestMethod> supportedMethods = supportedMethods(requestMethods);

    //Setup response message list
    int currentCount = 0;
    for (RequestMethod httpRequestMethod : supportedMethods) {
      OperationContext operationContext = new OperationContext(new OperationBuilder(nameGenerator),
          httpRequestMethod,
          outerContext,
          currentCount);

      Operation operation = pluginsManager.operation(operationContext);
      if (!operation.isHidden()) {
        operations.add(operation);
        currentCount++;
      }
    }
    operations.sort(outerContext.operationOrdering());

    return operations;
  }

  private Set<RequestMethod> supportedMethods(Set<RequestMethod> requestMethods) {
    return requestMethods == null || requestMethods.isEmpty()
        ? REQUEST_METHODS
        : requestMethods;
  }

}
