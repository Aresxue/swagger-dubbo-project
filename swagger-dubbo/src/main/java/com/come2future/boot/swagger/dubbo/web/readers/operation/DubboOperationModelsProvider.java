package com.come2future.boot.swagger.dubbo.web.readers.operation;

import static springfox.documentation.schema.ResolvedTypes.resolvedTypeSignature;

import com.come2future.boot.swagger.dubbo.util.ClassUtil;
import com.fasterxml.classmate.ResolvedType;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import springfox.documentation.schema.plugins.SchemaPluginsManager;
import springfox.documentation.service.ResolvedMethodParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.ViewProviderPlugin;
import springfox.documentation.spi.service.contexts.RequestMappingContext;
import springfox.documentation.spring.web.readers.operation.OperationModelsProvider;

/**
 * @author: Ares
 * @time: 2021/7/2 15:16
 * @description: copy from springfox.documentation.spring.web.readers.operation.OperationModelsProvider
 * @version: JDK 1.8
 * @see springfox.documentation.spring.web.readers.operation.OperationModelsProvider
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DubboOperationModelsProvider {

  private static final Logger LOG = LoggerFactory.getLogger(OperationModelsProvider.class);

  private final SchemaPluginsManager pluginsManager;

  @Autowired
  public DubboOperationModelsProvider(SchemaPluginsManager pluginsManager) {
    this.pluginsManager = pluginsManager;
  }

  public void apply(RequestMappingContext context) {
    collectFromReturnType(context);
    collectParameters(context);
    collectGlobalModels(context);
  }

  private void collectGlobalModels(RequestMappingContext context) {
    for (ResolvedType each : context.getAdditionalModels()) {
      context.operationModelsBuilder().addInputParam(each);
      context.operationModelsBuilder().addReturn(each);
    }
  }

  public boolean supports(DocumentationType delimiter) {
    return true;
  }

  private void collectFromReturnType(RequestMappingContext context) {
    ResolvedType modelType = context.getReturnType();
    modelType = context.alternateFor(modelType);
    LOG.debug(
        "Adding return parameter of type {}",
        resolvedTypeSignature(modelType).orElse("<null>"));

    context.operationModelsBuilder()
        .addReturn(
            modelType,
            viewForReturn(context));
  }

  private void collectParameters(RequestMappingContext context) {
    LOG.debug(
        "Reading parameters models for handlerMethod |{}|",
        context.getName());

    List<ResolvedMethodParameter> parameterTypes = context.getParameters();
    for (ResolvedMethodParameter parameterType : parameterTypes) {
      Class<?> clazz = parameterType.getParameterType().getErasedType();
      if (parameterType.hasParameterAnnotation(RequestBody.class)
          || parameterType.hasParameterAnnotation(RequestPart.class) || !ClassUtil
          .isBaseOrWrapOrString(clazz)) {
        ResolvedType modelType = context.alternateFor(parameterType.getParameterType());
        LOG.debug(
            "Adding input parameter of type {}",
            resolvedTypeSignature(modelType).orElse("<null>"));
        context.operationModelsBuilder().addInputParam(
            modelType,
            viewForParameter(
                context,
                parameterType),
            new HashSet<>());
      }
    }
    LOG.debug(
        "Finished reading parameters models for handlerMethod |{}|",
        context.getName());
  }

  private Optional<ResolvedType> viewForReturn(RequestMappingContext context) {
    ViewProviderPlugin viewProvider =
        pluginsManager.viewProvider(context.getDocumentationContext().getDocumentationType());
    return viewProvider.viewFor(
        context);
  }

  private Optional<ResolvedType> viewForParameter(
      RequestMappingContext context,
      ResolvedMethodParameter parameter) {
    ViewProviderPlugin viewProvider =
        pluginsManager.viewProvider(context.getDocumentationContext().getDocumentationType());
    return viewProvider.viewFor(
        parameter);
  }
}
