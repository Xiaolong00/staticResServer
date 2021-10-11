package com.ice.statics.utils;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 初始化本地路径
 */
@Configuration
@ConfigurationProperties(prefix = "path.resources")
public class StaticLocation {

    private static StaticLocation factory;

    public static StaticLocation getInstance() {
        return factory;
    }

    private String staticLocations;

    public StaticLocation() {
        factory = this;
    }

    public String getStaticLocations() {
        return staticLocations;
    }

    public void setStaticLocations(String staticLocations) {
        this.staticLocations = staticLocations;
    }

}
