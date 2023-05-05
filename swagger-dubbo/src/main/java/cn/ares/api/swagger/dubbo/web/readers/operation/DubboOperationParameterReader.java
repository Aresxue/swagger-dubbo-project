package cn.ares.api.swagger.dubbo.web.readers.operation;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.slf4j.LoggerFactory.getLogger;

import cn.ares.api.swagger.dubbo.web.plugins.DubboDocumentationPluginsManager;
import com.fasterxml.classmate.ResolvedType;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.common.Compatibility;
import springfox.documentation.service.Parameter;
import springfox.documentation.service.RequestParameter;
import springfox.documentation.service.ResolvedMethodParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.EnumTypeDeterminer;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.spi.service.contexts.ParameterContext;
import springfox.documentation.spring.web.readers.operation.ParameterAggregator;
import springfox.documentation.spring.web.readers.parameter.ModelAttributeParameterExpander;

/**
 * @author: Ares
 * @time: 2021/7/2 13:24
 * @description: copy from springfox.documentation.spring.web.readers.operation.OperationParameterReader
 * @version: JDK 1.8
 * @see springfox.documentation.spring.web.readers.operation.OperationParameterReader
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DubboOperationParameterReader {

  private static final Logger LOGGER = getLogger(DubboOperationParameterReader.class);

  private final ParameterAggregator aggregator;

  @Autowired
  private DubboDocumentationPluginsManager pluginsManager;

  @Autowired
  public DubboOperationParameterReader(
      ModelAttributeParameterExpander expander,
      EnumTypeDeterminer enumTypeDeterminer,
      ParameterAggregator aggregator) {
    this.aggregator = aggregator;
  }

  public void apply(OperationContext context) {
    context.operationBuilder().parameters(context.getGlobalOperationParameters());
    List<Compatibility<Parameter, RequestParameter>> compatibilities = readParameters(context);
    context.operationBuilder().parameters(
        compatibilities.stream()
            .map(Compatibility::getLegacy)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList()));
    context.operationBuilder()
        .requestParameters(new HashSet<>(context.getGlobalRequestParameters()));
    Collection<RequestParameter> requestParameters = compatibilities.stream()
        .map(Compatibility::getModern)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(toSet());
    context.operationBuilder()
        .requestParameters(aggregator.aggregate(requestParameters));
  }

  public boolean supports(DocumentationType delimiter) {
    return true;
  }

  private List<Compatibility<springfox.documentation.service.Parameter, RequestParameter>> readParameters(OperationContext context) {
    List<ResolvedMethodParameter> methodParameters = context.getParameters();
    List<Compatibility<springfox.documentation.service.Parameter, RequestParameter>> parameters = new ArrayList<>();
    LOGGER.debug("Reading parameters for method {} at path {}", context.getName(),
        context.requestMappingPattern());

    int index = 0;
    for (ResolvedMethodParameter methodParameter : methodParameters) {
      LOGGER.debug("Processing parameter {}", methodParameter.defaultName().orElse("<unknown>"));
      ResolvedType alternate = context.alternateFor(methodParameter.getParameterType());
      if (!shouldIgnore(methodParameter, alternate, context.getIgnorableParameterTypes())) {

        ParameterContext parameterContext = new ParameterContext(methodParameter,
            context.getDocumentationContext(),
            context.getGenericsNamingStrategy(),
            context,
            index++);

        parameters.add(pluginsManager.parameter(parameterContext));
      }
    }
    return parameters.stream()
        .filter(hiddenParameter().negate())
        .collect(toList());
  }

  private Predicate<Compatibility<Parameter, RequestParameter>> hiddenParameter() {
    return c -> c.getLegacy()
        .map(springfox.documentation.service.Parameter::isHidden)
        .orElse(false);
  }

  private boolean shouldIgnore(
      final ResolvedMethodParameter parameter,
      ResolvedType resolvedParameterType,
      final Set<Class> ignorableParamTypes) {

    if (ignorableParamTypes.contains(resolvedParameterType.getErasedType())) {
      return true;
    }
    return ignorableParamTypes.stream()
        .filter(Annotation.class::isAssignableFrom)
        .anyMatch(parameter::hasParameterAnnotation);

  }

}
