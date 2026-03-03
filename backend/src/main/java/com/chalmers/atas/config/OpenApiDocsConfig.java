package com.chalmers.atas.config;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiDocsConfig {

    @Bean
    public OpenApiCustomizer hideCurrentUserParam() {
        return openApi ->
                openApi.getPaths().values()
                        .forEach(pathItem ->
                                pathItem.readOperations().forEach(operation -> {
                                    if (operation.getParameters() != null) {
                                        operation.getParameters().removeIf(parameter ->
                                                parameter.getName().equals("currentUser"));
                                    }
                                })
                        );
    }
}
