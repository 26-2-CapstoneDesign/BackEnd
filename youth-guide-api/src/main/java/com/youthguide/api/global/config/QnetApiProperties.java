package com.youthguide.api.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "qnet.api")
public record QnetApiProperties(
        Endpoint certificateList,
        Endpoint examSchedule
) {
    public record Endpoint(String baseUrl, String key) {}
}