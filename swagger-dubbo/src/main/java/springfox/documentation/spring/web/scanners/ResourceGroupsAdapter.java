package springfox.documentation.spring.web.scanners;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import springfox.documentation.service.ApiDescription;
import springfox.documentation.service.ResourceGroup;

/**
 * @author: Ares
 * @time: 2022-02-21 13:42:41
 * @description: ResourceGroups Adapter
 * @version: JDK 1.8
 */
public class ResourceGroupsAdapter {

  public static Iterable<ResourceGroup> collectResourceGroups(
      Collection<ApiDescription> apiDescriptions) {
    return ResourceGroups.collectResourceGroups(apiDescriptions);
  }

  public static Iterable<ResourceGroup> sortedByName(Set<ResourceGroup> resourceGroups) {
    return ResourceGroups.sortedByName(resourceGroups);
  }

  public static Predicate<ApiDescription> belongsTo(final String groupName) {
    return ResourceGroups.belongsTo(groupName);
  }

}