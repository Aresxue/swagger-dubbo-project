package cn.ares.api.swagger.dubbo.web.scanners;

import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.toList;

import cn.ares.api.swagger.dubbo.web.readers.operation.DubboApiOperationReader;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import springfox.documentation.service.ApiDescription;
import springfox.documentation.service.Operation;
import springfox.documentation.spi.service.contexts.ApiSelector;
import springfox.documentation.spi.service.contexts.PathContext;
import springfox.documentation.spi.service.contexts.RequestMappingContext;
import springfox.documentation.spring.web.plugins.DocumentationPluginsManager;
import springfox.documentation.spring.web.scanners.ApiDescriptionLookup;
import springfox.documentation.spring.web.scanners.CachingOperationReader;
import springfox.documentation.spring.wrapper.PatternsRequestCondition;

/**
 * @author: Ares
 * @time: 2021/7/2 12:37
 * @description: copy from springfox.documentation.spring.web.scanners.ApiDescriptionReader
 * @version: JDK 1.8
 * @see springfox.documentation.spring.web.scanners.ApiDescriptionReader
 */

@Component
public class DubboApiDescriptionReader {

  private static final Logger LOGGER = LoggerFactory.getLogger(DubboApiDescriptionReader.class);

  private final CachingOperationReader operationReader;
  private final DocumentationPluginsManager pluginsManager;
  private final ApiDescriptionLookup lookup;

  @Autowired
  public DubboApiDescriptionReader(DocumentationPluginsManager pluginsManager,
      ApiDescriptionLookup lookup, DubboApiOperationReader dubboApiOperationReader) {
    this.operationReader = new CachingOperationReader(dubboApiOperationReader);
    this.pluginsManager = pluginsManager;
    this.lookup = lookup;
  }

  public List<ApiDescription> read(RequestMappingContext outerContext) {
    PatternsRequestCondition<?> patternsCondition = outerContext.getPatternsCondition();
    ApiSelector selector = outerContext.getDocumentationContext().getApiSelector();

    List<ApiDescription> apiDescriptionList = new ArrayList<>();
    for (String path : matchingPaths(selector, patternsCondition)) {
      String methodName = outerContext.getName();
      try {
        RequestMappingContext operationContext = outerContext.copyPatternUsing(path)
            .withKnownModels(outerContext.getModelMap());

        List<Operation> operations = operationReader.read(operationContext);
        if (operations.size() > 0) {
          operationContext.apiDescriptionBuilder()
              .groupName(outerContext.getGroupName())
              .operations(operations)
              .pathDecorator(pluginsManager
                  .decorator(new PathContext(outerContext, operations.stream().findFirst())))
              .path(path)
              .description(methodName)
              .hidden(false);
          ApiDescription apiDescription = operationContext.apiDescriptionBuilder().build();
          lookup.add(outerContext.key(), apiDescription);
          apiDescriptionList.add(apiDescription);
        }
      } catch (Error e) {
        String contentMsg =
            "Skipping process path[" + path + "], method[" + methodName + "] as it has an error.";
        LOGGER.error(contentMsg, e);
      }
    }
    return apiDescriptionList;
  }

  private List<String> matchingPaths(ApiSelector selector,
      PatternsRequestCondition<?> patternsCondition) {
    return patternsCondition.getPatterns().stream()
        .filter(selector.getPathSelector())
        .sorted(naturalOrder())
        .collect(toList());
  }

}
