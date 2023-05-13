package cn.ares.api.swagger.dubbo.web.plugins;

import static cn.ares.api.swagger.common.constant.SwaggerConstant.ARES_ADDRESS;
import static cn.ares.api.swagger.common.constant.SwaggerConstant.DEFAULT_CONTACT_NAME;
import static cn.ares.api.swagger.common.constant.SwaggerConstant.DEFAULT_EMAIL;
import static cn.ares.api.swagger.common.constant.SwaggerConstant.DEFAULT_VERSION;
import static cn.ares.api.swagger.common.constant.SwaggerConstant.GIT_ADDRESS;
import static cn.ares.api.swagger.common.constant.SwaggerConstant.MAVEN_DEPENDENCY;

import cn.ares.api.swagger.dubbo.web.read.paramters.DubboParameterDataTypeReader;
import cn.ares.api.swagger.dubbo.web.read.paramters.DubboParameterTypeReader;
import cn.ares.api.swagger.dubbo.web.readers.DubboSwaggerApiListingReader;
import cn.ares.api.swagger.common.web.readers.operation.CustomOpenApiOperationTagsReader;
import cn.ares.api.swagger.dubbo.web.readers.operation.DubboOperationModelsProvider;
import cn.ares.api.swagger.dubbo.web.readers.operation.DubboOperationParameterReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.apache.dubbo.config.ApplicationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.common.Compatibility;
import springfox.documentation.service.ApiDescription;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiListing;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Operation;
import springfox.documentation.service.RequestParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.contexts.ModelContext;
import springfox.documentation.spi.service.ApiListingBuilderPlugin;
import springfox.documentation.spi.service.ApiListingScannerPlugin;
import springfox.documentation.spi.service.DocumentationPlugin;
import springfox.documentation.spi.service.ModelNamesRegistryFactoryPlugin;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.OperationModelsProviderPlugin;
import springfox.documentation.spi.service.ParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.ApiListingContext;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.spi.service.contexts.ParameterContext;
import springfox.documentation.spi.service.contexts.RequestMappingContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.spring.web.readers.operation.OperationModelsProvider;
import springfox.documentation.spring.web.readers.operation.OperationParameterReader;
import springfox.documentation.spring.web.readers.parameter.ParameterDataTypeReader;
import springfox.documentation.spring.web.readers.parameter.ParameterTypeReader;
import springfox.documentation.spring.web.scanners.ApiListingScanningContext;
import springfox.documentation.spring.web.scanners.DefaultModelNamesRegistryFactory;
import springfox.documentation.swagger.readers.operation.OpenApiOperationTagsReader;
import springfox.documentation.swagger.web.SwaggerApiListingReader;

/**
 * @author: Ares
 * @time: 2021-06-27 16:21
 * @description: copy from springfox.documentation.spring.web.plugins.DocumentationPluginsManager
 * @version: JDK 1.8
 * @see springfox.documentation.spring.web.plugins.DocumentationPluginsManager
 */
@Component
public class DubboDocumentationPluginsManager {

  @Autowired
  private ApplicationConfig applicationConfig;

  @Value("${swagger.dubbo.application.version:}")
  private String version;
  @Value("${swagger.dubbo.application.groupId:}")
  private String groupId;
  @Value("${swagger.dubbo.application.artifactId:}")
  private String artifactId;
  @Value("${swagger.dubbo.group:dubbo interface document}")
  private String swaggerGroup;

  @Autowired
  @Qualifier("apiListingBuilderPluginRegistry")
  private PluginRegistry<ApiListingBuilderPlugin, DocumentationType> apiListingPlugins;
  @Autowired
  @Qualifier("parameterBuilderPluginRegistry")
  private PluginRegistry<ParameterBuilderPlugin, DocumentationType> parameterPlugins;
  @Autowired
  @Qualifier("operationBuilderPluginRegistry")
  private PluginRegistry<OperationBuilderPlugin, DocumentationType> operationBuilderPlugins;
  @Autowired
  @Qualifier("operationModelsProviderPluginRegistry")
  private PluginRegistry<OperationModelsProviderPlugin, DocumentationType> operationModelsProviders;
  @Autowired
  @Qualifier("apiListingScannerPluginRegistry")
  private PluginRegistry<ApiListingScannerPlugin, DocumentationType> apiListingScanners;
  @Autowired
  @Qualifier("modelNamesRegistryFactoryPluginRegistry")
  private PluginRegistry<ModelNamesRegistryFactoryPlugin, DocumentationType> modelNameRegistryFactoryPlugins;

  @Autowired
  private DubboOperationParameterReader dubboOperationParameterReader;
  @Autowired
  private DubboParameterTypeReader dubboParameterTypeReader;
  @Autowired
  private DubboParameterDataTypeReader dubboParameterDataTypeReader;
  @Autowired
  private DubboOperationModelsProvider dubboOperationModelsProvider;
  @Autowired
  private DubboSwaggerApiListingReader dubboSwaggerApiListingReader;
  @Autowired
  private CustomOpenApiOperationTagsReader dubboOpenApiOperationTagsReader;

  public Collection<DocumentationPlugin> documentationPlugins() throws IllegalStateException {
    List<DocumentationPlugin> plugins = new ArrayList<>();

    String description = "";
    version = !StringUtil.isEmpty(version) ? version : applicationConfig.getVersion();
    if (!StringUtil.isEmpty(groupId) && !StringUtil.isEmpty(artifactId) && !StringUtils
        .isEmpty(version)) {
      description = MessageFormat.format(MAVEN_DEPENDENCY, groupId, artifactId, version);
    }
    if (null == version) {
      version = DEFAULT_VERSION;
    }
    String contactName = applicationConfig.getOwner();
    if (StringUtil.isEmpty(contactName)) {
      contactName = DEFAULT_CONTACT_NAME;
    }

    ApiInfo apiInfo = new ApiInfoBuilder()
        .title(swaggerGroup)
        .description(description)
        .termsOfServiceUrl(ARES_ADDRESS)
        .contact(new Contact(contactName, GIT_ADDRESS, DEFAULT_EMAIL))
        .version(version)
        .build();

    Docket docket = new Docket(DocumentationType.OAS_30)
        .apiInfo(apiInfo)
        .groupName(swaggerGroup)
        .select()
        //.globalRequestParameters(requestParameters)
//        .extensions(openApiExtensionResolver.buildExtensions("1.2.x"))
        //.extensions(openApiExtensionResolver.buildSettingExtensions())
//        .securityContexts(securityContexts).securitySchemes(securitySchemes);
        .build();

    plugins.add(docket);
    return plugins;
  }

  public Operation operation(OperationContext operationContext) {
    for (OperationBuilderPlugin each : operationBuilderPlugins
        .getPluginsFor(operationContext.getDocumentationType())) {
      if (each instanceof OperationParameterReader) {
        dubboOperationParameterReader.apply(operationContext);
      } else if (each instanceof OpenApiOperationTagsReader) {
        dubboOpenApiOperationTagsReader.apply(operationContext);
      } else {
        each.apply(operationContext);
      }
    }
    return operationContext.operationBuilder().build();
  }

  public Compatibility<springfox.documentation.service.Parameter, RequestParameter> parameter(
      ParameterContext parameterContext) {
    for (ParameterBuilderPlugin each : parameterPlugins
        .getPluginsFor(parameterContext.getDocumentationType())) {
      if (each instanceof ParameterTypeReader) {
        dubboParameterTypeReader.apply(parameterContext);
      } else if (each instanceof ParameterDataTypeReader) {
        dubboParameterDataTypeReader.apply(parameterContext);
      } else {
        each.apply(parameterContext);
      }
    }
    return new Compatibility<>(
        parameterContext.parameterBuilder().build(),
        parameterContext.requestParameterBuilder().build());
  }

  public Set<ModelContext> modelContexts(RequestMappingContext context) {
    DocumentationType documentationType = context.getDocumentationContext().getDocumentationType();
    for (OperationModelsProviderPlugin each : operationModelsProviders
        .getPluginsFor(documentationType)) {
      if (each instanceof OperationModelsProvider) {
        dubboOperationModelsProvider.apply(context);
      } else {
        each.apply(context);
      }
    }
    return context.operationModelsBuilder().build();
  }

  public ApiListing apiListing(ApiListingContext context) {
    for (ApiListingBuilderPlugin each : apiListingPlugins
        .getPluginsFor(context.getDocumentationType())) {
      if (each instanceof SwaggerApiListingReader) {
        dubboSwaggerApiListingReader.apply(context);
      } else {
        each.apply(context);
      }
    }
    return context.apiListingBuilder().build();
  }


  public ModelNamesRegistryFactoryPlugin modelNamesGeneratorFactory(
      DocumentationType documentationType) {
    return modelNameRegistryFactoryPlugins
        .getPluginOrDefaultFor(documentationType, new DefaultModelNamesRegistryFactory());
  }

  public Collection<ApiDescription> additionalListings(final ApiListingScanningContext context) {
    final DocumentationType documentationType = context.getDocumentationContext()
        .getDocumentationType();
    List<ApiDescription> additional = new ArrayList<>();
    for (ApiListingScannerPlugin each : apiListingScanners.getPluginsFor(documentationType)) {
      additional.addAll(each.apply(context.getDocumentationContext()));
    }
    return additional;
  }

}
