package com.come2future.boot.swagger.dubbo.plugin;

import com.come2future.boot.swagger.dubbo.annotations.ApiOperationExpand;
import io.swagger.annotations.ApiOperation;
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
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
public class OperationOrderBuilderPlugin extends AbstractOperationBuilderPlugin {

  @Override
  public void apply(OperationContext context) {
    int position;

    Optional<ApiOperation> apiOperation = context.findAnnotation(ApiOperation.class);
    if (apiOperation.isPresent()) {
      int posit = apiOperation.get().position();
      if (posit != 0) {
        position = posit;
      } else {
        position = findPosition(context);
      }
    } else {
      position = findPosition(context);
    }

    List<VendorExtension> vendorExtensions = new ArrayList<>();
    vendorExtensions.add(new StringVendorExtension("x-order", String.valueOf(position)));
    context.operationBuilder().extensions(vendorExtensions);
  }

  @Override
  public boolean supports(@Nullable DocumentationType delimiter) {
    return true;
  }


  private int findPosition(OperationContext context) {
    int position = Integer.MAX_VALUE;
    Optional<ApiOperationExpand> apiOperationExpand = context.findAnnotation(
        ApiOperationExpand.class);
    if (apiOperationExpand.isPresent()) {
      if (apiOperationExpand.get().order() != 0) {
        position = apiOperationExpand.get().order();
      }
    }
    return position;
  }

}
