package cn.ares.api.swagger.dubbo.plugin;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static springfox.documentation.service.Tags.emptyTags;

import cn.ares.api.swagger.dubbo.annotation.ApiExpand;
import io.swagger.annotations.Api;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import springfox.documentation.service.StringVendorExtension;
import springfox.documentation.service.Tag;
import springfox.documentation.service.VendorExtension;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ApiListingBuilderPlugin;
import springfox.documentation.spi.service.contexts.ApiListingContext;

@Component
@Order(value = Ordered.HIGHEST_PRECEDENCE + 2000)
public class ApiListingOrderReader implements ApiListingBuilderPlugin {

  @Override
  public void apply(ApiListingContext apiListingContext) {
    Optional<? extends Class<?>> controller = apiListingContext.getResourceGroup()
        .getControllerClass();
    if (controller.isPresent()) {
      Optional<Api> apiAnnotation = ofNullable(findAnnotation(controller.get(), Api.class));
      String description = apiAnnotation.map(Api::description)
          .filter(((Predicate<String>) String::isEmpty).negate())
          .orElse(null);

      Set<String> tagSet = apiAnnotation.map(tags())
          .orElse(new TreeSet<>());
      if (tagSet.isEmpty()) {
        tagSet.add(apiListingContext.getResourceGroup().getGroupName());
      }
      Set<Tag> tagsSet = new HashSet<>();
      Integer order = applyOrder(controller);
      String author = applyAuthor(controller);
      for (String tagName : tagSet) {
        List<VendorExtension> vendorExtensions = new ArrayList<>();
        vendorExtensions.add(new StringVendorExtension("x-order", Objects.toString(order)));
        if (!StringUtils.isEmpty(author)) {
          vendorExtensions.add(new StringVendorExtension("x-author", author));
        }
        Tag tag = new Tag(tagName, description, vendorExtensions);
        tagsSet.add(tag);
      }
      apiListingContext.apiListingBuilder().tags(tagsSet);
    }

  }

  private String applyAuthor(Optional<? extends Class<?>> controller) {
    if (controller.isPresent()) {
      Optional<ApiExpand> apiExpandAnnotation = ofNullable(
          findAnnotation(controller.get(), ApiExpand.class));
      if (apiExpandAnnotation.isPresent()) {
        return apiExpandAnnotation.get().author();
      }
    }
    return null;
  }

  private Integer applyOrder(Optional<? extends Class<?>> controller) {
    if (controller.isPresent()) {
      Optional<ApiExpand> apiExpandAnnotation = ofNullable(
          findAnnotation(controller.get(), ApiExpand.class));
      int apiExpandOrder = Integer.MAX_VALUE;
      Integer apiSortOrder = Integer.MAX_VALUE;
      if (apiExpandAnnotation.isPresent()) {
        apiExpandOrder = apiExpandAnnotation.get().order();
      }
      Integer min = apiSortOrder.compareTo(apiExpandOrder) < 0 ? apiSortOrder : apiExpandOrder;
      return min;
    }
    return 0;
  }

  @Override
  public boolean supports(@Nullable DocumentationType documentationType) {
    return true;
  }

  private Function<Api, Set<String>> tags() {
    return input -> Stream.of(input.tags())
        .filter(emptyTags())
        .collect(toCollection(TreeSet::new));
  }

}
