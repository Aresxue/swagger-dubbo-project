package com.come2future.boot.swagger.dubbo.web.read.paramters;

import static springfox.documentation.schema.Collections.collectionElementType;
import static springfox.documentation.schema.Collections.isContainerType;

import com.come2future.boot.swagger.dubbo.util.ClassUtil;
import com.fasterxml.classmate.ResolvedType;
import java.util.Collection;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.service.ResolvedMethodParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.ParameterContext;

/**
 * @author: Ares
 * @time: 2021-07-02 14:09:00
 * @description: copy from springfox.documentation.spring.web.readers.parameter.ParameterTypeReader
 * @version: JDK 1.8
 * @see springfox.documentation.spring.web.readers.parameter.ParameterTypeReader
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DubboParameterTypeReader {

  private static final Logger LOGGER = LoggerFactory.getLogger(DubboParameterTypeReader.class);

  public void apply(ParameterContext context) {
    String parameterType = findParameterType(context);
    context.parameterBuilder().parameterType(parameterType);
    Collection<MediaType> accepts =
        "body".equals(parameterType)
            ? Collections.singleton(MediaType.APPLICATION_JSON)
            : null;
    context.requestParameterBuilder()
        .in(parameterType)
        .accepts(accepts);
  }

  public boolean supports(DocumentationType delimiter) {
    return true;
  }

  public static String findParameterType(ParameterContext parameterContext) {
    ResolvedMethodParameter resolvedMethodParameter = parameterContext.resolvedMethodParameter();
    ResolvedType parameterType = resolvedMethodParameter.getParameterType();
    parameterType = parameterContext.alternateFor(parameterType);

    // Multi-part file trumps any other annotations
    if (isFileType(parameterType) || isListOfFiles(parameterType)) {
      if (resolvedMethodParameter.hasParameterAnnotation(RequestPart.class)) {
        parameterContext.requestParameterBuilder()
            .accepts(Collections.singleton(MediaType.MULTIPART_FORM_DATA));
        return "formData";
      }
      parameterContext.requestParameterBuilder()
          .accepts(Collections.singleton(MediaType.APPLICATION_OCTET_STREAM));
      return "body";
    }

    if (resolvedMethodParameter.hasParameterAnnotation(PathVariable.class)) {
      return "path";
    } else if (resolvedMethodParameter.hasParameterAnnotation(RequestBody.class)) {
      return "body";
    } else if (resolvedMethodParameter.hasParameterAnnotation(RequestPart.class)) {
      parameterContext.requestParameterBuilder()
          .accepts(Collections.singleton(MediaType.MULTIPART_FORM_DATA));
      return "formData";
    } else if (resolvedMethodParameter.hasParameterAnnotation(RequestHeader.class)) {
      return "header";
    } else if (resolvedMethodParameter.hasParameterAnnotation(ModelAttribute.class)) {
      parameterContext.requestParameterBuilder()
          .accepts(Collections.singleton(MediaType.APPLICATION_FORM_URLENCODED));
      LOGGER.warn("@ModelAttribute annotated parameters should have already been expanded via "
          + "the ExpandedParameterBuilderPlugin");
    }

    Class<?> clazz = resolvedMethodParameter.getParameterType().getErasedType();
    if (ClassUtil.isBaseOrWrapOrString(clazz)) {
      return "query";
    } else {
      parameterContext.requestParameterBuilder().description(StringUtils.uncapitalize(clazz.getSimpleName()));
      return "body";
    }
  }

  private static boolean isListOfFiles(ResolvedType parameterType) {
    return isContainerType(parameterType) && isFileType(collectionElementType(parameterType));
  }

  private static boolean isFileType(ResolvedType parameterType) {
    return MultipartFile.class.isAssignableFrom(parameterType.getErasedType()) ||
        FilePart.class.isAssignableFrom(parameterType.getErasedType());
  }

}
