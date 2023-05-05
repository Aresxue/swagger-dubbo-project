package cn.ares.api.swagger.dubbo.web;

import static cn.ares.api.swagger.dubbo.constant.SwaggerConstant.DEFAULT_URL;
import static cn.ares.api.swagger.dubbo.constant.SwaggerConstant.SWAGGER_VERSION;

import cn.ares.api.swagger.dubbo.constant.SwaggerConstant;
import io.swagger.v3.oas.annotations.Hidden;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import springfox.documentation.swagger.web.SwaggerResource;

/**
 * @author: Ares
 * @time: 2021/3/10 22:54
 * @description: copy from springfox.documentation.swagger.web.InMemorySwaggerResourcesProvider
 * @version: JDK 1.8
 * @see springfox.documentation.swagger.web.InMemorySwaggerResourcesProvider
 */
@Hidden
@Controller
@RequestMapping("/swagger-dubbo-resources")
public class DubboSwaggerResourceController {

  @Value("${swagger.dubbo.doc:swagger-dubbo}")
  private String docPath;

  @Value("${swagger.dubbo.group:dubbo interface document}")
  private String swaggerGroup;

  @RequestMapping
  @ResponseBody
  public ResponseEntity<List<SwaggerResource>> swaggerResources() {
    List<SwaggerResource> resources = new ArrayList<>();

    SwaggerResource swaggerResource = new SwaggerResource();
    swaggerResource.setName(swaggerGroup);
    swaggerResource.setUrl(swaggerLocation("/" + docPath + DEFAULT_URL, swaggerGroup));
    swaggerResource.setSwaggerVersion(SWAGGER_VERSION);
    resources.add(swaggerResource);

    Collections.sort(resources);
    return new ResponseEntity<>(resources, HttpStatus.OK);
  }

  private String swaggerLocation(String swaggerUrl, String swaggerGroup) {
    String base = Optional.of(swaggerUrl).get();
    if (SwaggerConstant.DEFAULT.equals(swaggerGroup)) {
      return base;
    }
    return base + "?group=" + swaggerGroup;
  }

}
