package com.clientui.clientui.configuration;

import com.clientui.clientui.filter.HeaderAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collections;

/**
 * Configuration class for Spring Security in the ClientUI application.
 * This class defines security filters, authentication providers, and HTTP security rules.
 * It enables HTTP Basic authentication, disables CSRF protection, and adds a custom authentication filter.
 */
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    /**
     * Configures the security filter chain for HTTP requests.
     * Permits public access to actuator, API documentation, and static resources.
     * All other requests require authentication.
     *
     * @param http the {@link HttpSecurity} object to configure
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/actuator/**",
                                "/apidocs/**",
                                "/swagger*/**",
                                "/v3/api-docs/**",
                                "/webjars/**",
                                "/favicon.ico"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .addFilterBefore(new HeaderAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * Provides a custom {@link AuthenticationProvider} that does not perform any authentication logic.
     * This is used to prevent Spring Security from generating a default user.
     *
     * @return a custom {@link AuthenticationProvider} instance
     */
    @Bean
    public AuthenticationProvider customAuthenticationProvider() {
        return new AuthenticationProvider() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                return authentication;
            }

            @Override
            public boolean supports(Class<?> authentication) {
                return true;
            }
        };
    }

    /**
     * Configures a custom {@link AuthenticationManager} using the provided {@link AuthenticationProvider}.
     *
     * @param authenticationConfiguration the {@link AuthenticationConfiguration} to use
     * @return a custom {@link AuthenticationManager} instance
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return new ProviderManager(Collections.singletonList(customAuthenticationProvider()));
    }


}

