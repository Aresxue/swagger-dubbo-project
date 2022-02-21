package springfox.documentation.spring.web.scanners;

import springfox.documentation.service.ResourceGroup;

/**
 * @author: Ares
 * @time: 2022-02-21 13:39:09
 * @description: ResourcePathProvider adapter
 * @version: JDK 1.8
 */
public class ResourcePathProviderAdapter extends ResourcePathProvider {

  public ResourcePathProviderAdapter(ResourceGroup resourceGroup) {
    super(resourceGroup);
  }

}
