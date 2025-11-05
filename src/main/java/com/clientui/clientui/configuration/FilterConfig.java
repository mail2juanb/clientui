package com.clientui.clientui.configuration;

import com.clientui.clientui.filter.AuthHeadersFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    // Filtre pour avoir les headers avant la security
    @Bean(name = "customAuthHeadersFilter")  // Nom différent
    public FilterRegistrationBean<AuthHeadersFilter> customAuthHeadersFilter() {
        FilterRegistrationBean<AuthHeadersFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new AuthHeadersFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(Integer.MIN_VALUE);
        return registration;
    }
}

