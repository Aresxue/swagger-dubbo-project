package com.come2future.boot.swagger.dubbo.plugin;

import com.come2future.boot.swagger.dubbo.annotations.ApiOperationExpand;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import springfox.documentation.service.StringVendorExtension;
import springfox.documentation.service.VendorExtension;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.OperationContext;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 101)
public class OperationAuthorBuilderPlugin extends AbstractOperationBuilderPlugin {

  @Override
  public void apply(OperationContext context) {
    Optional<ApiOperationExpand> apiOperationExpandOptional = context.findAnnotation(
        ApiOperationExpand.class);
    if (apiOperationExpandOptional.isPresent()) {
      String author = apiOperationExpandOptional.get().author();
      if (author != null && !"".equals(author) && !"null".equals(author)) {
        List<VendorExtension> vendorExtensions = new ArrayList<>();
        vendorExtensions.add(new StringVendorExtension("x-author", author));
        context.operationBuilder().extensions(vendorExtensions);
      }
    }
  }

  @Override
  public boolean supports(@Nullable DocumentationType delimiter) {
    return true;
  }

}
