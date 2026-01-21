package com.clientui.clientui.configuration;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.auth.BasicAuthRequestInterceptor;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Configuration class for Feign clients in the MicroDiab application.
 * Provides beans for Feign request interceptors, including basic authentication
 * and B3 headers propagation for distributed tracing.
 *
 * <p>This class is annotated with {@link Configuration @Configuration} to indicate
 * that it contains Spring bean definitions.</p>
 */
@Configuration
public class FeignConfig {

    /**
     * Autowired tracer for accessing the current span in distributed tracing.
     */
    @Autowired
    private Tracer tracer;

    /**
     * Creates a {@link BasicAuthRequestInterceptor} bean for Feign clients.
     * Configures basic authentication with a default username and password.
     *
     * @return A {@link BasicAuthRequestInterceptor} instance.
     */
    @Bean
    public BasicAuthRequestInterceptor basicAuthRequestInterceptor() {
        return new BasicAuthRequestInterceptor("username", "user");
    }

    /**
     * Creates a {@link RequestInterceptor} bean for propagating B3 headers
     * (TraceId and SpanId) in Feign requests. This enables distributed tracing
     * across microservices.
     *
     * @return A {@link RequestInterceptor} instance.
     */
    @Bean
    public RequestInterceptor b3HeadersRequestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                Span currentSpan = tracer.currentSpan();
                if (currentSpan != null) {
                    requestTemplate.header("X-B3-TraceId", currentSpan.context().traceId());
                    requestTemplate.header("X-B3-SpanId", currentSpan.context().spanId());
                    requestTemplate.header("X-B3-Sampled", "1");
                }
            }
        };
    }
}
