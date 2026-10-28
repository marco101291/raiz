package com.raiz.config;

import io.swagger.v3.oas.models.media.Schema;
import org.jspecify.annotations.Nullable;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;

import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The frontend and Alivia generate their types from the OpenAPI document. Without this, every field is
 * optional there ({@code label?: string}) even though the backend always sends it.
 * <p>
 * The rule comes from JSpecify: a DTO record component is required unless it is annotated
 * {@code @Nullable}, and a nullable one also accepts {@code null}. A new field is therefore required by
 * default, and the Java type and the contract cannot disagree.
 */
@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    private static final String DTO_PACKAGE = "com.raiz.web.dto";

    @Bean
    OpenApiCustomizer requiredFromNullness() {
        List<Class<?>> records = dtoRecords();
        return openApi -> openApi.getComponents().getSchemas().forEach((name, schema) ->
                recordNamed(name, records).ifPresent(type -> applyNullness(type, schema)));
    }

    private static List<Class<?>> dtoRecords() {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(Record.class));
        return scanner.findCandidateComponents(DTO_PACKAGE).stream()
                .<Class<?>>map(bean -> ClassUtils.resolveClassName(bean.getBeanClassName(), null))
                .toList();
    }

    /** springdoc names schemas after the simple class name, plus the type arguments for generics. */
    private static Optional<Class<?>> recordNamed(String schemaName, List<Class<?>> records) {
        return records.stream()
                .filter(type -> type.getTypeParameters().length == 0
                        ? schemaName.equals(type.getSimpleName())
                        : schemaName.startsWith(type.getSimpleName()))
                .findFirst();
    }

    /** Subtypes of a polymorphic response keep their own properties inside {@code allOf}. */
    @SuppressWarnings({"rawtypes", "unchecked"}) // swagger-models exposes raw Schema lists and maps
    private static void applyNullness(Class<?> type, Schema<?> schema) {
        List<Schema> parts = new ArrayList<>(List.of(schema));
        if (schema.getAllOf() != null) {
            parts.addAll(schema.getAllOf());
        }
        for (Schema part : parts) {
            Map<String, Schema> properties = part.getProperties();
            if (properties == null) {
                continue;
            }
            for (RecordComponent component : type.getRecordComponents()) {
                String name = component.getName();
                Schema property = properties.get(name);
                if (property == null) {
                    continue;
                }
                if (component.getAnnotatedType().isAnnotationPresent(Nullable.class)) {
                    properties.put(name, orNull(property));
                } else if (part.getRequired() == null || !part.getRequired().contains(name)) {
                    part.addRequiredItem(name);
                }
            }
        }
    }

    /** OpenAPI 3.1 has no {@code nullable}: null is one more type, or one more option next to a $ref. */
    private static Schema<?> orNull(Schema<?> property) {
        Schema<Object> nullType = new Schema<>();
        nullType.addType("null");
        if (property.get$ref() != null) {
            return new Schema<>().oneOf(List.of(property, nullType));
        }
        var types = new LinkedHashSet<String>();
        if (property.getTypes() != null) {
            types.addAll(property.getTypes());
        } else if (property.getType() != null) {
            types.add(property.getType());
        }
        types.add("null");
        property.setTypes(types);
        return property;
    }
}
