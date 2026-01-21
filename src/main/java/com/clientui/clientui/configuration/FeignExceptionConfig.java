package com.clientui.clientui.configuration;

import com.clientui.clientui.exception.CustomErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Configuration class for custom Feign exception handling in the MicroDiab application.
 * Provides a bean for a custom error decoder to handle exceptions thrown by Feign clients.
 *
 * <p>This class is annotated with {@link Configuration @Configuration} to indicate
 * that it contains Spring bean definitions.</p>
 */
@Configuration
public class FeignExceptionConfig {

    /**
     * Creates a {@link CustomErrorDecoder} bean for custom exception handling
     * in Feign clients. This decoder is responsible for interpreting and processing
     * error responses from Feign calls.
     *
     * @return A {@link CustomErrorDecoder} instance.
     */
    @Bean
    public CustomErrorDecoder mCustomErrorDecoder() {
        return new CustomErrorDecoder();
    }
}
