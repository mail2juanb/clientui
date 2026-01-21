package com.clientui.clientui.configuration;

import com.clientui.clientui.filter.AuthHeadersFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Configuration class for custom filters in the MicroDiab application.
 * Registers a custom authentication headers filter to ensure headers are available
 * before Spring Security processes the request.
 *
 * <p>This class is annotated with {@link Configuration @Configuration} to indicate
 * that it contains Spring bean definitions.</p>
 */
@Configuration
public class FilterConfig {

    /**
     * Creates a {@link FilterRegistrationBean} for the {@link AuthHeadersFilter}.
     * This filter is registered with the lowest possible order to ensure it runs
     * before Spring Security and applies to all URL patterns.
     *
     * @return A {@link FilterRegistrationBean} instance for the custom filter.
     */
    @Bean(name = "customAuthHeadersFilter")
    public FilterRegistrationBean<AuthHeadersFilter> customAuthHeadersFilter() {
        FilterRegistrationBean<AuthHeadersFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new AuthHeadersFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(Integer.MIN_VALUE);
        return registration;
    }
}

