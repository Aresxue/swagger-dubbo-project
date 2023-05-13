package cn.ares.api.swagger.common.web.readers.operation;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toSet;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;
import static springfox.documentation.service.Tags.emptyTags;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Stream;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

/**
 * @author: Ares
 * @time: 2021-07-06 18:55:00
 * @description: copy from springfox.documentation.swagger.readers.operation.OpenApiOperationTagsReader
 * @version: JDK 1.8
 * @see springfox.documentation.swagger.readers.operation.OpenApiOperationTagsReader
 */
@Component
@Order(SwaggerPluginSupport.OAS_PLUGIN_ORDER)
@Role(value = ROLE_INFRASTRUCTURE)
public class CustomOpenApiOperationTagsReader {

  public void apply(OperationContext context) {
    context.operationBuilder()
        .tags(Stream.concat(
                operationTags(context).stream(),
                controllerTags(context).stream())
            .collect(toSet()));
  }

  private Set<String> controllerTags(OperationContext context) {
    return tagsFromOasAnnotations(context)
        .stream()
        .map(springfox.documentation.service.Tag::getName)
        .collect(toSet());
  }

  private Set<springfox.documentation.service.Tag> tagsFromOasAnnotations(
      OperationContext context) {
    HashSet<springfox.documentation.service.Tag> controllerTags = new HashSet<>();
    Optional<Tags> tags = context.findAnnotation(Tags.class);
    tags.ifPresent(ts ->
        Arrays.stream(ts.value())
            .forEach(t -> controllerTags
                .add(new springfox.documentation.service.Tag(t.name(), t.description()))));
    Optional<Tag> tag = context.findAnnotation(Tag.class);
    tag.ifPresent(t ->
        controllerTags.add(new springfox.documentation.service.Tag(t.name(), t.description())));

    tags = context.findControllerAnnotation(Tags.class);
    tags.ifPresent(ts ->
        Arrays.stream(ts.value())
            .forEach(t -> controllerTags
                .add(new springfox.documentation.service.Tag(t.name(), t.description()))));
    tag = context.findControllerAnnotation(Tag.class);
    tag.ifPresent(t ->
        controllerTags.add(new springfox.documentation.service.Tag(t.name(), t.description())));

    return controllerTags;
  }

  private Set<String> operationTags(OperationContext context) {
    Optional<Operation> oasAnnotation = context.findAnnotation(Operation.class);
    return new HashSet<>(oasAnnotation.map(tagsFromOasOperation()).orElse(new HashSet<>()));
  }

  private Function<Operation, Set<String>> tagsFromOasOperation() {
    return input -> Stream.of(input.tags())
        .filter(emptyTags())
        .collect(toCollection(TreeSet::new));
  }

  public boolean supports(DocumentationType delimiter) {
    return true;
  }
}


