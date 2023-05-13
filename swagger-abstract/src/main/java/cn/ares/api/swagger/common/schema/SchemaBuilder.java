package cn.ares.api.swagger.common.schema;

import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;
import static springfox.documentation.schema.ResolvedTypes.modelRefFactory;

import com.fasterxml.classmate.TypeResolver;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import springfox.documentation.schema.ModelReference;
import springfox.documentation.schema.ReferenceModelSpecification;
import springfox.documentation.schema.TypeNameExtractor;
import springfox.documentation.schema.property.ModelSpecificationFactory;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.EnumTypeDeterminer;
import springfox.documentation.spi.schema.ModelBuilderPlugin;
import springfox.documentation.spi.schema.contexts.ModelContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

/**
 * @author: Ares
 * @time: 2022-08-05 11:24:08
 * @description: 支持Schema加在类上的相关属性配置
 * @version: JDK 1.8
 */
@Component
@Order(SwaggerPluginSupport.OAS_PLUGIN_ORDER)
@Role(value = ROLE_INFRASTRUCTURE)
public class SchemaBuilder implements ModelBuilderPlugin {

  private final TypeResolver typeResolver;
  private final TypeNameExtractor typeNameExtractor;
  private final EnumTypeDeterminer enumTypeDeterminer;
  private final ModelSpecificationFactory modelSpecifications;

  @Autowired
  public SchemaBuilder(TypeResolver typeResolver, TypeNameExtractor typeNameExtractor,
      EnumTypeDeterminer enumTypeDeterminer, ModelSpecificationFactory modelSpecifications) {
    this.typeResolver = typeResolver;
    this.typeNameExtractor = typeNameExtractor;
    this.enumTypeDeterminer = enumTypeDeterminer;
    this.modelSpecifications = modelSpecifications;
  }

  @Override
  public void apply(ModelContext context) {
    Schema annotation = AnnotationUtils.findAnnotation(
        typeResolver.resolve(context.getType()).getErasedType(), Schema.class);
    if (annotation != null) {
      List<ModelReference> modelRefs = new ArrayList<>();
      List<ReferenceModelSpecification> subclassKeys = new ArrayList<>();
      for (Class<?> each : annotation.subTypes()) {
        modelRefs.add(modelRefFactory(context, enumTypeDeterminer, typeNameExtractor)
            .apply(typeResolver.resolve(each)));
        subclassKeys.add(
            modelSpecifications.create(context, typeResolver.resolve(each)).getReference()
                .orElse(null));
      }
      context.getBuilder()
          .description(annotation.description())
          .discriminator(annotation.discriminatorProperty())
          .subTypes(modelRefs);
      context.getModelSpecificationBuilder()
          .facets(f -> f.description(annotation.description()))
          .compoundModel(cm -> cm.discriminator(annotation.discriminatorProperty())
              .subclassReferences(subclassKeys));
    }
  }

  @Override
  public boolean supports(@NonNull DocumentationType documentationType) {
    return DocumentationType.OAS_30.equals(documentationType);
  }

}
