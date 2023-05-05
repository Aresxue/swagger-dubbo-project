package cn.ares.api.swagger.dubbo.web.scanners;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static springfox.documentation.service.Tags.toTags;
import static springfox.documentation.spi.service.contexts.Orderings.listingReferencePathComparator;

import cn.ares.api.swagger.dubbo.reference.DubboReferenceManager;
import cn.ares.api.swagger.dubbo.util.AopUtil;
import cn.ares.api.swagger.dubbo.web.DubboWebMvcRequestHandler;
import io.swagger.v3.oas.annotations.tags.Tags;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import springfox.documentation.PathProvider;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.DocumentationBuilder;
import springfox.documentation.service.ApiListing;
import springfox.documentation.service.ApiListingReference;
import springfox.documentation.service.Documentation;
import springfox.documentation.service.PathAdjuster;
import springfox.documentation.service.ResourceGroup;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.service.contexts.DocumentationContext;
import springfox.documentation.spi.service.contexts.RequestMappingContext;
import springfox.documentation.spring.web.paths.PathMappingAdjuster;
import springfox.documentation.spring.web.readers.operation.HandlerMethodResolver;
import springfox.documentation.spring.web.scanners.ApiListingReferenceScanResult;
import springfox.documentation.spring.web.scanners.ApiListingScanningContext;

/**
 * @author: Ares
 * @time: 2021-06-27 16:15
 * @description: copy from springfox.documentation.spring.web.scanners.ApiDocumentationScanner
 * @version: JDK 1.8
 * @see springfox.documentation.spring.web.scanners.ApiDocumentationScanner
 */
@Component
public class DubboApiDocumentationScanner {

  private static final Logger LOGGER = LoggerFactory.getLogger(DubboApiDocumentationScanner.class);

  private final DubboApiListingScanner dubboApiListingScanner;
  private final DubboReferenceManager dubboReferenceManager;
  private final HandlerMethodResolver handlerMethodResolver;
  private final RequestMappingHandlerMapping requestMappingHandlerMapping;

  private RequestMappingInfo.BuilderConfiguration config;

  @Value("${server.servlet.context-path:/}")
  private String servletContextPath;

  @Value("${swagger.dubbo.context-path:/ares}")
  private String contextPath;

  @PostConstruct
  public void init() {
    config = new RequestMappingInfo.BuilderConfiguration();
    config.setPathMatcher(requestMappingHandlerMapping.getPathMatcher());
    config.setUrlPathHelper(requestMappingHandlerMapping.getUrlPathHelper());
    config.setTrailingSlashMatch(true);
    config.setSuffixPatternMatch(false);
    config.setContentNegotiationManager(
        requestMappingHandlerMapping.getContentNegotiationManager());
  }


  @Autowired
  public DubboApiDocumentationScanner(DubboApiListingScanner dubboApiListingScanner,
      DubboReferenceManager dubboReferenceManager,
      HandlerMethodResolver handlerMethodResolver,
      RequestMappingHandlerMapping requestMappingHandlerMapping) {
    this.dubboApiListingScanner = dubboApiListingScanner;
    this.dubboReferenceManager = dubboReferenceManager;
    this.handlerMethodResolver = handlerMethodResolver;
    this.requestMappingHandlerMapping = requestMappingHandlerMapping;
  }

  public Documentation scan(DocumentationContext context) {
    LOGGER.debug("Scanning for api listing references");

    Map<ResourceGroup, List<RequestMappingContext>> resourceGroupRequestMappings = new HashMap<>(
        16);

    AtomicInteger requestMappingContextId = new AtomicInteger();

    dubboReferenceManager.getDubboBeanMethod().forEach((key, dubboBeanMethod) -> {
      Class<?> interfaceClass = dubboBeanMethod.getInterfaceClass();
      Object bean = dubboBeanMethod.getBean();
      Method method = dubboBeanMethod.getMethod();

      try {
        String groupName = null;
        io.swagger.v3.oas.annotations.tags.Tag tag = interfaceClass
            .getAnnotation(io.swagger.v3.oas.annotations.tags.Tag.class);
        if (tag != null) {
          groupName = tag.name();
        }
        groupName = getGroupName(groupName, interfaceClass);
        Object originBean = AopUtil.getTarget(bean);
        Class<?> implClazz = originBean.getClass();
        if (null == groupName) {
          tag = implClazz.getAnnotation(io.swagger.v3.oas.annotations.tags.Tag.class);
          if (tag != null) {
            groupName = tag.name();
          }
        }
        groupName = getGroupName(groupName, implClazz);
        if (null == groupName) {
          groupName = interfaceClass.getSimpleName();
        }

        ResourceGroup resourceGroup = new ResourceGroup(groupName, interfaceClass, 0);

        RequestMappingInfo.Builder builder = RequestMappingInfo
            .paths(key)
            .consumes(APPLICATION_JSON_VALUE)
            .methods(RequestMethod.POST);
        RequestMappingInfo requestMappingInfo = builder.options(config).build();

        HandlerMethod handlerMethod = new HandlerMethod(originBean, method);
        RequestHandler requestHandler = new DubboWebMvcRequestHandler(
            servletContextPath + contextPath,
            handlerMethodResolver, requestMappingInfo, handlerMethod);
        RequestMappingContext requestMappingContext = new RequestMappingContext(
            String.valueOf(requestMappingContextId.get()), context, requestHandler);
        resourceGroupRequestMappings.putIfAbsent(resourceGroup, new ArrayList<>());
        resourceGroupRequestMappings.get(resourceGroup).add(requestMappingContext);

        requestMappingContextId.incrementAndGet();
      } catch (Exception e) {
        LOGGER
            .error("generate proxy requestMapping fail, class: {}, method: {}, exception message: ",
                interfaceClass, method, e);
      }
    });

    ApiListingReferenceScanResult apiListingReferenceScanResult = new ApiListingReferenceScanResult(
        resourceGroupRequestMappings);
    ApiListingScanningContext listingContext = new ApiListingScanningContext(context,
        apiListingReferenceScanResult.getResourceGroupRequestMappings());

    Map<String, List<ApiListing>> apiListings = dubboApiListingScanner.scan(listingContext);
    Set<Tag> tags = toTags(apiListings);
    tags.addAll(context.getTags());
    DocumentationBuilder group = new DocumentationBuilder()
        .name(context.getGroupName())
        .apiListingsByResourceGroupName(apiListings)
        .produces(context.getProduces())
        .consumes(context.getConsumes())
        .host(context.getHost())
        .schemes(context.getProtocols())
        .basePath(contextPath)
        .extensions(context.getVendorExtentions())
        .tags(tags);

    Set<ApiListingReference> apiReferenceSet = new TreeSet<>(listingReferencePathComparator());
    apiReferenceSet.addAll(apiListingReferences(apiListings, context));

    group.resourceListing(r ->
        r.apiVersion(context.getApiInfo().getVersion())
            .apis(apiReferenceSet.stream()
                .sorted(context.getListingReferenceOrdering())
                .collect(toList()))
            .securitySchemes(context.getSecuritySchemes())
            .info(context.getApiInfo())
            .servers(context.getServers()));
    return group.build();
  }

  private Annotation[] mergeAnnotation(Annotation[] methodAnnotations,
      Annotation[] implMethodAnnotations) {
    int methodAnnotationsLength = methodAnnotations.length;
    Annotation[] annotations = new Annotation[implMethodAnnotations.length];
    System.arraycopy(methodAnnotations, 0, annotations, 0, methodAnnotationsLength);
    System.arraycopy(implMethodAnnotations, 0, annotations, methodAnnotationsLength,
        implMethodAnnotations.length);
    return annotations;
  }

  private Collection<ApiListingReference> apiListingReferences(
      Map<String, List<ApiListing>> apiListings,
      DocumentationContext context) {
    return apiListings.entrySet().stream().map(toApiListingReference(context)).collect(toSet());
  }

  private Function<Entry<String, List<ApiListing>>, ApiListingReference> toApiListingReference(
      final DocumentationContext context) {

    return input -> {
      String description = String.join(System.getProperty("line.separator"),
          descriptions(input.getValue()));
      PathAdjuster adjuster = new PathMappingAdjuster(context);
      PathProvider pathProvider = context.getPathProvider();
      String path = pathProvider.getResourceListingPath(context.getGroupName(), input.getKey());
      return new ApiListingReference(adjuster.adjustedPath(path), description, 0);
    };
  }

  private Iterable<String> descriptions(Collection<ApiListing> apiListings) {
    return apiListings.stream()
        .map(ApiListing::getDescription)
        .sorted(Comparator.naturalOrder()).collect(toList());
  }

  private String getGroupName(String groupName, Class<?> implClazz) {
    if (null == groupName) {
      Tags tags = implClazz.getAnnotation(Tags.class);
      if (null != tags) {
        io.swagger.v3.oas.annotations.tags.Tag[] tagArray = tags.value();
        if (tagArray.length > 0) {
          groupName = tagArray[0].name();
        }
      }
    }
    return groupName;
  }

}
