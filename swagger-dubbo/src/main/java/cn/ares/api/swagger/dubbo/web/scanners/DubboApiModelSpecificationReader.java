package cn.ares.api.swagger.dubbo.web.scanners;

import cn.ares.api.swagger.dubbo.web.plugins.DubboDocumentationPluginsManager;
import com.fasterxml.classmate.TypeResolver;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import springfox.documentation.schema.ModelSpecification;
import springfox.documentation.schema.ModelSpecificationProvider;
import springfox.documentation.spi.schema.contexts.ModelContext;
import springfox.documentation.spi.service.contexts.RequestMappingContext;

/**
 * @author: Ares
 * @time: 2021/7/2 15:12
 * @description: copy from springfox.documentation.spring.web.scanners.ApiModelSpecificationReader
 * @version: JDK 1.8
 * @see springfox.documentation.spring.web.scanners.ApiModelSpecificationReader
 */
@Component
public class DubboApiModelSpecificationReader {

  private final ModelSpecificationProvider modelProvider;
  private final DubboDocumentationPluginsManager pluginsManager;
  private final TypeResolver resolver;

  @Autowired
  public DubboApiModelSpecificationReader(
      @Qualifier("cachedModels") ModelSpecificationProvider modelProvider,
      DubboDocumentationPluginsManager pluginsManager,
      TypeResolver resolver) {
    this.modelProvider = modelProvider;
    this.pluginsManager = pluginsManager;
    this.resolver = resolver;
  }

  public Set<ModelSpecification> read(RequestMappingContext context) {
    Set<ModelSpecification> specifications = new HashSet<>();
    Set<ModelContext> modelContexts = pluginsManager.modelContexts(context);
    for (ModelContext each : modelContexts) {
      markIgnorablesAsHasSeen(
          context.getIgnorableParameterTypes(),
          each);
      modelProvider.modelSpecificationsFor(each)
          .ifPresent(specifications::add);
      specifications.addAll(modelProvider.modelDependenciesSpecifications(each));
    }
    return specifications;
  }

  private void markIgnorablesAsHasSeen(
      Set<Class> ignorableParameterTypes,
      ModelContext modelContext) {

    for (Class<?> ignorableParameterType : ignorableParameterTypes) {
      modelContext.seen(resolver.resolve(ignorableParameterType));
    }
  }

}
