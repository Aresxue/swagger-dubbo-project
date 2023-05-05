package cn.ares.api.swagger.dubbo.constant;

/**
 * @author: Ares
 * @time: 2021-06-27 15:20:00
 * @description: Swagger constant
 * @version: JDK 1.8
 */
public interface SwaggerConstant {

  String DEFAULT = "default";
  String SWAGGER_VERSION = "3.0.3";
  String DEFAULT_URL = "/api-docs";
  String MAVEN_DEPENDENCY = "&lt;dependency&gt;<br/>"
      + "&nbsp;&nbsp;&nbsp;&nbsp;&lt;groupId&gt;{0}&lt;/groupId&gt;<br/>"
      + "&nbsp;&nbsp;&nbsp;&nbsp;&lt;artifactId&gt;{1}&lt;/artifactId&gt;<br/>"
      + "&nbsp;&nbsp;&nbsp;&nbsp;&lt;version&gt;{2}&lt;/version&gt;<br/>"
      + "&lt;/dependency&gt;<br/>";

  String DEFAULT_CONTACT_NAME = "Ares";
  String DEFAULT_VERSION = "1.0.0";
  String ARES_ADDRESS = "https://github.com/Aresxue";
  String GIT_ADDRESS = "https://github.com/Aresxue/swagger-dubbo.git";
  String DEFAULT_EMAIL = "ares320635@gmail.com";

  String UNIQUE_KEY = "/%s/%s%s";

  String SLASH = "/";
  String POUND = "#";
}
