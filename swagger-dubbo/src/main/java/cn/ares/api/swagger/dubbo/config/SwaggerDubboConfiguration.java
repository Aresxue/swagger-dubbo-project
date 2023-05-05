package cn.ares.api.swagger.dubbo.config;

import cn.ares.api.swagger.dubbo.web.DubboDocumentationCache;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author: Ares
 * @time: 2021-07-01 18:52:00
 * @description: swagger dubbo configuration
 * @version: JDK 1.8
 */
@ConditionalOnProperty(name = "swagger.dubbo.enable", havingValue = "true", matchIfMissing = true)
@Configuration
@ComponentScan(basePackages = {
    "cn.ares.api.swagger.dubbo",
    "springfox.documentation"})
public class SwaggerDubboConfiguration {

  @Bean
  public DubboDocumentationCache dubboResourceGroupCache() {
    return new DubboDocumentationCache();
  }

}
