package com.come2future.boot.swagger.dubbo.demo.configs;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.spring.context.annotation.DubboComponentScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@DubboComponentScan(basePackages = {"com.come2future.boot.swagger.dubbo.demo"})
public class DubboConfig {

  @Value("${dubbo.registry.address}")
  private String registryAddress;

  @Bean
  public ApplicationConfig applicationConfig() {
    ApplicationConfig applicationConfig = new ApplicationConfig();
    applicationConfig.setName("dubbo-example-app");
    applicationConfig.setOwner("Ares");
    return applicationConfig;
  }

  @Bean
  public RegistryConfig registryConfig() {
    RegistryConfig registryConfig = new RegistryConfig();
    registryConfig.setAddress(registryAddress);
    return registryConfig;
  }

  @Bean
  public ProtocolConfig protocolConfig() {
    ProtocolConfig protocol = new ProtocolConfig();
    protocol.setName("dubbo");
    protocol.setPort(20880);
    return protocol;
  }

}
