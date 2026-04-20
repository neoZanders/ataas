package com.chalmers.atas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.timeedit")
public record TimeEditProperties(
        String apiKey,
        String organizationId
) {
}
