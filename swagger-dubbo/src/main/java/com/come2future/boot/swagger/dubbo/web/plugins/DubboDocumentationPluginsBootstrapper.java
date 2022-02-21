package com.come2future.boot.swagger.dubbo.web.plugins;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static springfox.documentation.builders.BuilderDefaults.nullToEmptyList;
import static springfox.documentation.spi.service.contexts.Orderings.pluginOrdering;

import com.come2future.boot.swagger.dubbo.web.DubboDocumentationCache;
import com.come2future.boot.swagger.dubbo.web.scanners.DubboApiDocumentationScanner;
import com.fasterxml.classmate.TypeResolver;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import springfox.documentation.PathProvider;
import springfox.documentation.schema.AlternateTypeRule;
import springfox.documentation.schema.AlternateTypeRuleConvention;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.DocumentationPlugin;
import springfox.documentation.spi.service.RequestHandlerCombiner;
import springfox.documentation.spi.service.contexts.Defaults;
import springfox.documentation.spi.service.contexts.DocumentationContext;
import springfox.documentation.spi.service.contexts.DocumentationContextBuilder;
import springfox.documentation.spring.web.plugins.DefaultConfiguration;

/**
 * @author: Ares
 * @time: 2021-06-27 16:38
 * @description: copy from springfox.documentation.spring.web.plugins.AbstractDocumentationPluginsBootstrapper
 * @version: JDK 1.8
 * @see springfox.documentation.spring.web.plugins.AbstractDocumentationPluginsBootstrapper
 */
@Component
public class DubboDocumentationPluginsBootstrapper implements
    ApplicationListener<ApplicationReadyEvent> {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(DubboDocumentationPluginsBootstrapper.class);

  private final DubboDocumentationPluginsManager documentationPluginsManager;
  private final DubboApiDocumentationScanner resourceListing;
  private final DefaultConfiguration defaultConfiguration;

  private final AtomicBoolean isStart = new AtomicBoolean(false);

  @Autowired
  private DubboDocumentationCache dubboDocumentationCache;

  private RequestHandlerCombiner combiner;
  private List<AlternateTypeRuleConvention> typeConventions;

  public DubboDocumentationPluginsBootstrapper(
      DubboDocumentationPluginsManager documentationPluginsManager,
      DubboApiDocumentationScanner resourceListing,
      Defaults defaults,
      TypeResolver typeResolver,
      PathProvider pathProvider) {
    this.documentationPluginsManager = documentationPluginsManager;
    this.resourceListing = resourceListing;
    this.defaultConfiguration = new DefaultConfiguration(defaults, typeResolver, pathProvider);
  }

  protected DocumentationContext buildContext(DocumentationPlugin each) {
    return each.configure(withDefaults(each));
  }

  protected void scanDocumentation(DocumentationContext context) {
    try {
      getScanned().addDocumentation(resourceListing.scan(context));
    } catch (Exception e) {
      LOGGER.error(String.format("Unable to scan documentation context %s", context.getGroupName()),
          e);
    }
  }

  private DocumentationContextBuilder withDefaults(DocumentationPlugin plugin) {
    DocumentationType documentationType = plugin.getDocumentationType();
    List<AlternateTypeRule> rules = nullToEmptyList(typeConventions).stream()
        .map(AlternateTypeRuleConvention::rules)
        .flatMap(Collection::stream)
        .collect(toList());
    Set<String> consumes = new HashSet<>();
    consumes.add(APPLICATION_JSON_VALUE);
    return defaultConfiguration.create(documentationType).consumes(consumes).rules(rules);
  }


  public DubboDocumentationPluginsManager getDocumentationPluginsManager() {
    return documentationPluginsManager;
  }

  public DubboApiDocumentationScanner getResourceListing() {
    return resourceListing;
  }

  public DefaultConfiguration getDefaultConfiguration() {
    return defaultConfiguration;
  }

  public DubboDocumentationCache getScanned() {
    return dubboDocumentationCache;
  }

  public RequestHandlerCombiner getCombiner() {
    return combiner;
  }

  public List<AlternateTypeRuleConvention> getTypeConventions() {
    return typeConventions;
  }

  @Autowired(required = false)
  public void setCombiner(RequestHandlerCombiner combiner) {
    this.combiner = combiner;
  }

  @Autowired(required = false)
  public void setTypeConventions(List<AlternateTypeRuleConvention> typeConventions) {
    this.typeConventions = typeConventions;
  }

  @Override
  public void onApplicationEvent(@NonNull ApplicationReadyEvent requestHandledEvent) {
    if (!isStart.get()) {
      isStart.set(true);
      while (true) {
        try {
          List<DocumentationPlugin> plugins = documentationPluginsManager.documentationPlugins()
              .stream()
              .sorted(pluginOrdering())
              .collect(toList());
          LOGGER.debug("Found {} custom documentation plugin(s)", plugins.size());
          for (DocumentationPlugin each : plugins) {
            DocumentationType documentationType = each.getDocumentationType();
            if (each.isEnabled()) {
              scanDocumentation(buildContext(each));
            } else {
              LOGGER.debug("Skipping initializing disabled plugin bean {} v{}",
                  documentationType.getName(), documentationType.getVersion());
            }
          }
          break;
        } catch (Exception ignored) {
        }
      }
    }
  }
}
