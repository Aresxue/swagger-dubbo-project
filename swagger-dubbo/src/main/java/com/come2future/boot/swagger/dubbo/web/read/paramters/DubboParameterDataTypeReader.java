package com.come2future.boot.swagger.dubbo.web.read.paramters;

import static springfox.documentation.schema.Collections.collectionElementType;
import static springfox.documentation.schema.Collections.isContainerType;
import static springfox.documentation.schema.Maps.isMapType;
import static springfox.documentation.schema.ResolvedTypes.modelRefFactory;
import static springfox.documentation.schema.ScalarTypes.builtInScalarType;

import com.come2future.boot.swagger.dubbo.util.ClassUtil;
import com.fasterxml.classmate.ResolvedType;
import java.util.HashSet;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import springfox.documentation.schema.ModelReference;
import springfox.documentation.schema.ModelSpecification;
import springfox.documentation.schema.ScalarModelSpecification;
import springfox.documentation.schema.ScalarType;
import springfox.documentation.schema.TypeNameExtractor;
import springfox.documentation.schema.plugins.SchemaPluginsManager;
import springfox.documentation.schema.property.ModelSpecificationFactory;
import springfox.documentation.service.ResolvedMethodParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.EnumTypeDeterminer;
import springfox.documentation.spi.schema.ViewProviderPlugin;
import springfox.documentation.spi.schema.contexts.ModelContext;
import springfox.documentation.spi.service.contexts.ParameterContext;

/**
 * @author: Ares
 * @time: 2021/7/2 14:32
 * @description: copy from springfox.documentation.spring.web.readers.parameter.ParameterDataTypeReader
 * @version: JDK 1.8
 * @see springfox.documentation.spring.web.readers.parameter.ParameterDataTypeReader
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class DubboParameterDataTypeReader {

  private static final Logger LOG = LoggerFactory.getLogger(DubboParameterDataTypeReader.class);

  private final TypeNameExtractor nameExtractor;
  private final EnumTypeDeterminer enumTypeDeterminer;
  private final SchemaPluginsManager pluginsManager;
  private final ModelSpecificationFactory models;

  @Autowired
  public DubboParameterDataTypeReader(
      SchemaPluginsManager pluginsManager,
      TypeNameExtractor nameExtractor,
      EnumTypeDeterminer enumTypeDeterminer,
      ModelSpecificationFactory models) {
    this.nameExtractor = nameExtractor;
    this.enumTypeDeterminer = enumTypeDeterminer;
    this.pluginsManager = pluginsManager;
    this.models = models;
  }

  public boolean supports(DocumentationType delimiter) {
    return true;
  }

  public void apply(ParameterContext context) {
    ResolvedMethodParameter methodParameter = context.resolvedMethodParameter();
    ResolvedType parameterType = methodParameter.getParameterType();

    parameterType = context.alternateFor(parameterType);
    springfox.documentation.schema.ModelReference modelRef = null;
    ModelContext modelContext = modelContext(context, methodParameter, parameterType);
    ModelSpecification parameterModel = models.create(modelContext, parameterType);
    if (methodParameter.hasParameterAnnotation(PathVariable.class) && treatAsAString(
        parameterType)) {
      modelRef = new springfox.documentation.schema.ModelRef("string");
      context.requestParameterBuilder()
          .query(q -> q.model(m -> m.scalarModel(ScalarType.STRING)));
    } else if (methodParameter.hasParameterAnnotation(RequestParam.class) && isMapType(
        parameterType)) {
      modelRef = new springfox.documentation.schema.ModelRef(
          "",
          new springfox.documentation.schema.ModelRef("string"),
          true);
      context.requestParameterBuilder()
          .query(q -> q.model(m -> m.mapModel(map ->
              map.key(v -> v.scalarModel(ScalarType.STRING))
                  .value(v -> v.scalarModel(ScalarType.STRING)))));
    } else if (methodParameter.hasParameterAnnotation(RequestPart.class) ||
        methodParameter.hasParameterAnnotation(RequestBody.class)) {
      context.requestParameterBuilder().contentModel(parameterModel);
    } else {
      Class<?> clazz = parameterType.getErasedType();
      if (ClassUtil.isBaseOrWrapOrString(clazz)) {
        modelRef = handleFormParameter(context, parameterType, modelRef, parameterModel);
      } else {
        context.requestParameterBuilder().contentModel(parameterModel);
      }
    }
    context.parameterBuilder()
        .type(parameterType)
        .modelRef(Optional.ofNullable(modelRef)
            .orElse(modelRefFactory(
                modelContext,
                enumTypeDeterminer,
                nameExtractor).apply(parameterType)));
  }

  private ModelReference handleFormParameter(
      ParameterContext context,
      ResolvedType parameterType,
      ModelReference modelRef,
      ModelSpecification parameterModel) {
    if (treatRequestParamAsString(parameterType)) {
      modelRef = new springfox.documentation.schema.ModelRef("string");
      context.requestParameterBuilder()
          .query(q -> q.model(m -> m.scalarModel(ScalarType.STRING)));
    } else if (isContainerType(parameterType)
        && context.getDocumentationType() == DocumentationType.OAS_30) {
      modelRef = new springfox.documentation.schema.ModelRef("string");
      context.requestParameterBuilder()
          .query(q -> q.model(m -> m.scalarModel(collectionItemScalarType(parameterModel)))
              .explode(true));
    } else {
      String typeName = springfox.documentation.schema.Types
          .typeNameFor(parameterType.getErasedType());
      if (builtInScalarType(parameterType).isPresent()) {
        modelRef = new springfox.documentation.schema.ModelRef(typeName);
      }
      context.requestParameterBuilder()
          .query(q -> q.model(m -> m.copyOf(parameterModel)));
    }
    return modelRef;
  }

  private ScalarType collectionItemScalarType(ModelSpecification parameterModel) {
    return parameterModel.getCollection()
        .map(c -> c.getModel().getScalar()
            .map(ScalarModelSpecification::getType)
            .orElse(ScalarType.STRING))
        .orElseGet(() -> {
          LOG.warn("Could not infer parameter type: " + parameterModel);
          return ScalarType.STRING;
        });
  }

  private ModelContext modelContext(
      ParameterContext context,
      ResolvedMethodParameter methodParameter,
      ResolvedType parameterType) {
    ViewProviderPlugin viewProvider = pluginsManager
        .viewProvider(context.getDocumentationContext()
            .getDocumentationType());

    return context.getOperationContext()
        .operationModelsBuilder()
        .addInputParam(
            parameterType,
            viewProvider.viewFor(methodParameter),
            new HashSet<>());
  }

  private boolean treatRequestParamAsString(ResolvedType parameterType) {
    return treatAsAString(parameterType) && !isContainerType(parameterType)
        || (isContainerType(parameterType) && treatAsAString(collectionElementType(parameterType)));
  }

  private boolean treatAsAString(ResolvedType parameterType) {
    return !(builtInScalarType(parameterType.getErasedType()).isPresent()
        || enumTypeDeterminer.isEnum(parameterType.getErasedType()));
  }
}

