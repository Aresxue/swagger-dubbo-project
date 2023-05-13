package cn.ares.api.swagger.common.web.paramters;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;
import static springfox.documentation.swagger.readers.parameter.Examples.examples;

import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import springfox.documentation.builders.ExampleBuilder;
import springfox.documentation.service.AllowableListValues;
import springfox.documentation.service.AllowableValues;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.EnumTypeDeterminer;
import springfox.documentation.spi.service.ExpandedParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.ParameterExpansionContext;
import springfox.documentation.spring.web.DescriptionResolver;
import springfox.documentation.swagger.common.SwaggerPluginSupport;
import springfox.documentation.swagger.schema.ApiModelProperties;

/**
 * @author: Ares
 * @time: 2021-11-20 20:15:00
 * @description: copy from
 * springfox.documentation.swagger.readers.parameter.SwaggerExpandedParameterBuilder
 * @version: JDK 1.8
 * @see springfox.documentation.swagger.readers.parameter.SwaggerExpandedParameterBuilder
 */
@Component
@Order(SwaggerPluginSupport.OAS_PLUGIN_ORDER)
@Role(value = ROLE_INFRASTRUCTURE)
public class OpenApiExpandedParameterBuilder implements ExpandedParameterBuilderPlugin {

  private final DescriptionResolver descriptions;
  private final EnumTypeDeterminer enumTypeDeterminer;

  @Autowired
  public OpenApiExpandedParameterBuilder(
      DescriptionResolver descriptions,
      EnumTypeDeterminer enumTypeDeterminer) {
    this.descriptions = descriptions;
    this.enumTypeDeterminer = enumTypeDeterminer;
  }

  @Override
  public void apply(ParameterExpansionContext context) {
    Optional<Schema> schemaOptional = context.findAnnotation(Schema.class);
    schemaOptional.ifPresent(schema -> fromSchema(context, schema));
    Optional<ApiParam> apiParamOptional = context.findAnnotation(ApiParam.class);
    apiParamOptional.ifPresent(apiParam -> fromApiParam(context, apiParam));
  }

  @Override
  public boolean supports(@Nullable DocumentationType delimiter) {
    return SwaggerPluginSupport.pluginDoesApply(delimiter);
  }

  private void fromApiParam(
      ParameterExpansionContext context,
      ApiParam apiParam) {
    String allowableProperty = ofNullable(apiParam.allowableValues())
        .filter(((Predicate<String>) String::isEmpty).negate())
        .orElse(null);
    AllowableValues allowable = allowableValues(ofNullable(allowableProperty),
        context.getFieldType().getErasedType());

    maybeSetParameterName(context, apiParam.name());
    context.getParameterBuilder()
        .description(descriptions.resolve(apiParam.value()))
        .defaultValue(apiParam.defaultValue())
        .required(apiParam.required())
        .allowMultiple(apiParam.allowMultiple())
        .allowableValues(allowable)
        .parameterAccess(apiParam.access())
        .hidden(apiParam.hidden())
        .scalarExample(apiParam.example())
        .complexExamples(examples(apiParam.examples()))
        .order(SwaggerPluginSupport.OAS_PLUGIN_ORDER)
        .build();

    context.getRequestParameterBuilder()
        .description(descriptions.resolve(apiParam.value()))
        .required(apiParam.required())
        .hidden(apiParam.hidden())
        .example(new ExampleBuilder().value(apiParam.example()).build())
        .precedence(SwaggerPluginSupport.OAS_PLUGIN_ORDER)
        .query(q -> q.enumerationFacet(e -> e.allowedValues(allowable)));
  }

  private void fromSchema(
      ParameterExpansionContext context,
      Schema schema) {
    String allowableProperty = Arrays.stream(schema.allowableValues())
        .filter(((Predicate<String>) String::isEmpty).negate()).findFirst().orElse(null);
    AllowableValues allowable = allowableValues(ofNullable(allowableProperty),
        context.getFieldType().getErasedType());

    maybeSetParameterName(context, schema.name());
    context.getParameterBuilder()
        .description(descriptions.resolve(schema.description()))
        .required(schema.required())
        .allowableValues(allowable)
        .parameterAccess(schema.accessMode().name())
        .hidden(schema.hidden())
        .scalarExample(schema.example())
        .order(SwaggerPluginSupport.OAS_PLUGIN_ORDER)
        .build();

    context.getRequestParameterBuilder()
        .description(descriptions.resolve(schema.description()))
        .required(schema.required())
        .hidden(schema.hidden())
        .example(new ExampleBuilder().value(schema.example()).build())
        .precedence(SwaggerPluginSupport.OAS_PLUGIN_ORDER)
        .query(q -> q.enumerationFacet(e -> e.allowedValues(allowable)));
  }

  private void maybeSetParameterName(
      ParameterExpansionContext context,
      String parameterName) {
    if (!StringUtils.isEmpty(parameterName)) {
      context.getParameterBuilder().name(parameterName);
      context.getRequestParameterBuilder().name(parameterName);
    }
  }

  private AllowableValues allowableValues(
      final Optional<String> optionalAllowable,
      Class<?> fieldType) {

    AllowableValues allowable = null;
    if (enumTypeDeterminer.isEnum(fieldType)) {
      allowable = new AllowableListValues(getEnumValues(fieldType), "LIST");
    } else if (optionalAllowable.isPresent()) {
      allowable = ApiModelProperties.allowableValueFromString(optionalAllowable.get());
    }
    return allowable;
  }

  private List<String> getEnumValues(final Class<?> subject) {
    return Stream.of(subject.getEnumConstants())
        .map((Function<Object, String>) Object::toString)
        .collect(toList());
  }

}