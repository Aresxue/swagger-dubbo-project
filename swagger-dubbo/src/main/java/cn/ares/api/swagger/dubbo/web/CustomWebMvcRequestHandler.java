package cn.ares.api.swagger.dubbo.web;

import cn.ares.boot.util.common.ArrayUtil;
import cn.ares.boot.util.spring.AopUtil;
import com.fasterxml.classmate.ResolvedType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import springfox.documentation.RequestHandler;
import springfox.documentation.RequestHandlerKey;
import springfox.documentation.service.ResolvedMethodParameter;
import springfox.documentation.spring.web.WebMvcNameValueExpressionWrapper;
import springfox.documentation.spring.web.WebMvcPatternsRequestConditionWrapper;
import springfox.documentation.spring.web.WebMvcRequestMappingInfoWrapper;
import springfox.documentation.spring.web.readers.operation.HandlerMethodResolver;
import springfox.documentation.spring.wrapper.NameValueExpression;
import springfox.documentation.spring.wrapper.PatternsRequestCondition;

/**
 * @author: Ares
 * @time: 2021-07-01 22:17:00
 * @description: copy from springfox.documentation.spring.web.WebMvcRequestHandler
 * @version: JDK 1.8
 * @see springfox.documentation.spring.web.WebMvcRequestHandler
 */
public class CustomWebMvcRequestHandler implements RequestHandler {

  private final String contextPath;
  private final HandlerMethodResolver methodResolver;
  private final RequestMappingInfo requestMapping;
  private final HandlerMethod handlerMethod;

  public CustomWebMvcRequestHandler(String contextPath, HandlerMethodResolver methodResolver,
      RequestMappingInfo requestMapping, HandlerMethod handlerMethod) {
    this.contextPath = contextPath;
    this.methodResolver = methodResolver;
    this.requestMapping = requestMapping;
    this.handlerMethod = handlerMethod;
  }

  @Override
  public HandlerMethod getHandlerMethod() {
    return handlerMethod;
  }

  @Override
  public RequestHandler combine(RequestHandler other) {
    return this;
  }

  @Override
  public Class<?> declaringClass() {
    return handlerMethod.getBeanType();
  }

  @Override
  public boolean isAnnotatedWith(Class<? extends Annotation> annotation) {
    return null != AnnotationUtils.findAnnotation(handlerMethod.getMethod(), annotation);
  }

  @Override
  public PatternsRequestCondition<?> getPatternsCondition() {
    return new WebMvcPatternsRequestConditionWrapper(contextPath,
        requestMapping.getPatternsCondition());
  }

  @Override
  public String groupName() {
    Class<?> beanType = handlerMethod.getBeanType();
    if (ArrayUtil.isNotEmpty(beanType.getInterfaces())) {
      Class<?> interfaceClazz = beanType.getInterfaces()[0];
      return interfaceClazz.getSimpleName();
    } else {
      return beanType.getSimpleName();
    }
  }

  @Override
  public String getName() {
    return this.handlerMethod.getMethod().getName();
  }

  @Override
  public Set<RequestMethod> supportedMethods() {
    return this.requestMapping.getMethodsCondition().getMethods();
  }

  @Override
  public Set<MediaType> produces() {
    return this.requestMapping.getProducesCondition().getProducibleMediaTypes();
  }

  @Override
  public Set<MediaType> consumes() {
    return this.requestMapping.getConsumesCondition().getConsumableMediaTypes();
  }

  @Override
  public Set<NameValueExpression<String>> headers() {
    return WebMvcNameValueExpressionWrapper
        .from(this.requestMapping.getHeadersCondition().getExpressions());
  }

  @Override
  public Set<NameValueExpression<String>> params() {
    return WebMvcNameValueExpressionWrapper
        .from(this.requestMapping.getParamsCondition().getExpressions());
  }

  @Override
  public <T extends Annotation> Optional<T> findAnnotation(Class<T> annotation) {
    Method method = handlerMethod.getMethod();
    Annotation result = AnnotationUtils.findAnnotation(method, annotation);
    if (null == result) {
      try {
        Class<?> clazz = AopUtil.getTarget(handlerMethod.getBean()).getClass();
        Method implMethod = clazz.getDeclaredMethod(method.getName(), method.getParameterTypes());
        result = AnnotationUtils.findAnnotation(implMethod, annotation);
      } catch (Exception exception) {
        exception.printStackTrace();
      }
    }
    return (Optional<T>) Optional.ofNullable(result);
  }

  @Override
  public RequestHandlerKey key() {
    return new RequestHandlerKey(this.requestMapping.getPatternsCondition().getPatterns(),
        this.requestMapping.getMethodsCondition().getMethods(),
        this.requestMapping.getConsumesCondition().getConsumableMediaTypes(),
        this.requestMapping.getProducesCondition().getProducibleMediaTypes());
  }

  @Override
  public springfox.documentation.spring.wrapper.RequestMappingInfo<?> getRequestMapping() {
    return new WebMvcRequestMappingInfoWrapper(this.requestMapping);
  }

  @Override
  public List<ResolvedMethodParameter> getParameters() {
    List<ResolvedMethodParameter> result = new ArrayList<>();
    List<ResolvedMethodParameter> resolvedMethodParameterList = methodResolver
        .methodParameters(handlerMethod);
    Object bean = handlerMethod.getBean();
    Method method = handlerMethod.getMethod();
    try {
      Method implMethod = bean.getClass()
          .getDeclaredMethod(method.getName(), method.getParameterTypes());
      HandlerMethod implHandlerMethod = new HandlerMethod(bean, implMethod);
      Map<Integer, ResolvedMethodParameter> resolvedMethodParameterMap = methodResolver
          .methodParameters(implHandlerMethod).stream()
          .collect(
              Collectors.toMap(ResolvedMethodParameter::getParameterIndex, Function.identity()));
      for (ResolvedMethodParameter resolvedMethodParameter : resolvedMethodParameterList) {
        ResolvedMethodParameter resolvedMethodParameterImpl = resolvedMethodParameterMap
            .get(resolvedMethodParameter.getParameterIndex());
        List<Annotation> annotationList = resolvedMethodParameterImpl.getAnnotations();
        ResolvedMethodParameter newResolvedMethodParameter = resolvedMethodParameter;
        if (!CollectionUtils.isEmpty(annotationList)) {
          for (Annotation annotation : annotationList) {
            newResolvedMethodParameter = resolvedMethodParameter.annotate(annotation);
          }
        }
        result.add(newResolvedMethodParameter);
      }
    } catch (NoSuchMethodException ignored) {
    }

    return result;
  }

  @Override
  public ResolvedType getReturnType() {
    return this.methodResolver.methodReturnType(this.handlerMethod);
  }

  @Override
  public <T extends Annotation> Optional<T> findControllerAnnotation(Class<T> annotation) {
    Class<?> clazz = handlerMethod.getBeanType();
    Annotation result = AnnotationUtils.findAnnotation(clazz, annotation);
    if (null == result && ArrayUtil.isNotEmpty(clazz.getInterfaces())) {
      clazz = clazz.getInterfaces()[0];
      result = AnnotationUtils.findAnnotation(clazz, annotation);
    }
    return (Optional<T>) Optional.ofNullable(result);
  }

  @Override
  public String toString() {
    return (new StringJoiner(", ", CustomWebMvcRequestHandler.class.getSimpleName() + "{", "}"))
        .add("requestMapping=" + requestMapping).add("handlerMethod=" + handlerMethod)
        .add("key=" + this.key()).toString();
  }
}
