package cn.ares.api.swagger.dubbo.web;

import static cn.ares.api.swagger.common.constant.SwaggerConstant.DEFAULT_URL;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.oas.mappers.ServiceModelToOpenApiMapper;
import springfox.documentation.service.Documentation;
import springfox.documentation.spring.web.OnServletBasedWebApplication;
import springfox.documentation.spring.web.json.Json;
import springfox.documentation.spring.web.json.JsonSerializer;

/**
 * @author: Ares
 * @time: 2021-06-27 15:17
 * @description: copy from springfox.documentation.oas.web.OpenApiControllerWebMvc
 * @version: JDK 1.8
 * @see springfox.documentation.oas.web.OpenApiControllerWebMvc
 */

@Hidden
@RestController
@RequestMapping("${swagger.dubbo.doc:swagger-dubbo}")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Conditional(OnServletBasedWebApplication.class)
public class DubboOpenApiControllerWebMvc {

  protected static final String HAL_MEDIA_TYPE = "application/hal+json";

  @Autowired
  private DubboDocumentationCache dubboDocumentationCache;

  @Autowired
  private ServiceModelToOpenApiMapper mapper;

  private final JsonSerializer jsonSerializer;

  @Value("${swagger.dubbo.group:dubbo interface document}")
  private String swaggerGroup;

  @GetMapping(value = DEFAULT_URL, produces = {APPLICATION_JSON_VALUE, HAL_MEDIA_TYPE})
  public ResponseEntity<Json> getDocumentation() {
    Documentation documentation = dubboDocumentationCache.documentationByGroup(swaggerGroup);
    if (null == documentation) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    OpenAPI oas = mapper.mapDocumentation(documentation);

    return new ResponseEntity<>(jsonSerializer.toJson(oas), HttpStatus.OK);
  }

  @Autowired
  public DubboOpenApiControllerWebMvc(JsonSerializer jsonSerializer) {
    this.jsonSerializer = jsonSerializer;
  }

}