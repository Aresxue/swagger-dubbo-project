package cn.ares.api.swagger.dubbo.web.readers;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static springfox.documentation.service.Tags.emptyTags;
import static springfox.documentation.swagger.common.SwaggerPluginSupport.pluginDoesApply;

import cn.ares.api.swagger.dubbo.entity.DubboBeanMethod;
import cn.ares.api.swagger.dubbo.reference.DubboReferenceManager;
import cn.ares.boot.util.common.StringUtil;
import cn.ares.boot.util.spring.AopUtil;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.tags.Tags;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import springfox.documentation.builders.BuilderDefaults;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.ApiListingContext;

/**
 * @author: Ares
 * @time: 2021/7/6 18:08
 * @description: copy from springfox.documentation.swagger.web.SwaggerApiListingReader
 * @version: JDK 1.8
 * @see springfox.documentation.swagger.web.SwaggerApiListingReader
 */
@Component
public class DubboSwaggerApiListingReader {

  @Autowired
  private DubboReferenceManager dubboReferenceManager;

  public void apply(ApiListingContext apiListingContext) {
    Optional<? extends Class<?>> interfaceClazz = apiListingContext.getResourceGroup()
        .getControllerClass();
    if (interfaceClazz.isPresent()) {
      Optional<Api> apiAnnotation = ofNullable(findAnnotation(interfaceClazz.get(), Api.class));
      Set<Tag> oasTags = tagsFromOasAnnotations(interfaceClazz.get());
      String description = apiAnnotation.map(Api::description)
          .map(BuilderDefaults::emptyToNull)
          .orElse(null);

      Set<String> tagSet = new TreeSet<>();
      if (oasTags.isEmpty()) {
        tagSet.addAll(apiAnnotation.map(tags()).orElse(new TreeSet<>()));
      }

      List<DubboBeanMethod> dubboBeanMethodList = dubboReferenceManager
          .getDubboBeanMethodList(interfaceClazz.get().getCanonicalName());

      if (!CollectionUtils.isEmpty(dubboBeanMethodList)) {
        Object bean = dubboBeanMethodList.get(0).getTarget();
        try {
          Class<?> implClazz = AopUtil.getTarget(bean).getClass();
          apiAnnotation = ofNullable(findAnnotation(implClazz, Api.class));
          if (StringUtil.isEmpty(description)) {
            description = apiAnnotation.map(Api::description)
                .map(BuilderDefaults::emptyToNull)
                .orElse(null);
          }

          if (oasTags.isEmpty()) {
            oasTags.addAll(tagsFromOasAnnotations(implClazz));
            if (oasTags.isEmpty()) {
              tagSet.addAll(apiAnnotation.map(tags()).orElse(new TreeSet<>()));
            }
          }
        } catch (Exception ignored) {
        }
      }

      apiListingContext.apiListingBuilder()
          .description(description)
          .tagNames(tagSet)
          .tags(oasTags);
    }
  }

  private Set<Tag> tagsFromOasAnnotations(Class<?> controller) {
    HashSet<Tag> controllerTags = new HashSet<>();
    Tags tags = findAnnotation(controller, Tags.class);
    if (tags != null) {
      Arrays.stream(tags.value())
          .forEach(t -> controllerTags.add(new Tag(t.name(), t.description())));
    }
    io.swagger.v3.oas.annotations.tags.Tag tag = findAnnotation(controller,
        io.swagger.v3.oas.annotations.tags.Tag.class);
    if (tag != null) {
      controllerTags.add(new Tag(tag.name(), tag.description()));
    }
    return controllerTags;
  }

  private Function<Api, Set<String>> tags() {
    return input -> Stream.of(input.tags())
        .filter(emptyTags())
        .collect(toCollection(TreeSet::new));
  }


  public boolean supports(DocumentationType delimiter) {
    return pluginDoesApply(delimiter);
  }
}
