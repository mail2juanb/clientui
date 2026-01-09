package com.clientui.clientui.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * Configuration class for customizing Spring MVC web configuration.
 * This class implements {@link WebMvcConfigurer} to override default MVC settings
 * and add custom resource handlers.
 *
 * Specifically, it exposes the "apidocs" directory as a static resource,
 * allowing API documentation to be served directly from the classpath.
 *
 * @see WebMvcConfigurer
 * @see ResourceHandlerRegistry
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Overrides the default resource handling to add custom static resource locations.
     * Maps the "/apidocs/**" path to the "classpath:/static/apidocs/" directory,
     * enabling access to API documentation files.
     *
     * @param registry the {@link ResourceHandlerRegistry} to configure
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Expose le dossier "apidocs" comme ressource statique
        registry.addResourceHandler("/apidocs/**")
                .addResourceLocations("classpath:/static/apidocs/");
    }
}
