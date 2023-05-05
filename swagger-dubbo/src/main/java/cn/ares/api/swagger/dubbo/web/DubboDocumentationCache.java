package cn.ares.api.swagger.dubbo.web;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import springfox.documentation.service.Documentation;

/**
 * @author: Ares
 * @time: 2021-07-01 21:08
 * @description: copy from springfox.documentation.spring.web.DocumentationCache
 * @version: JDK 1.8
 * @see springfox.documentation.spring.web.DocumentationCache
 */
public class DubboDocumentationCache {
  private final Map<String, Documentation> documentationLookup = new LinkedHashMap<>();

  public void addDocumentation(Documentation documentation) {
    documentationLookup.put(documentation.getGroupName(), documentation);
  }

  public Documentation documentationByGroup(String groupName) {
    return documentationLookup.get(groupName);
  }

  public Map<String, Documentation> all() {
    return Collections.unmodifiableMap(documentationLookup);
  }

  public void clear() {
    documentationLookup.clear();
  }
}
