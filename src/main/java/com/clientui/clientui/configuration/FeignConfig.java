package com.clientui.clientui.configuration;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.auth.BasicAuthRequestInterceptor;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Autowired
    private Tracer tracer;

    @Bean
    public BasicAuthRequestInterceptor basicAuthRequestInterceptor() {
        return new BasicAuthRequestInterceptor("username", "user");
    }

    @Bean
    public RequestInterceptor b3HeadersRequestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                Span currentSpan = tracer.currentSpan();
                if (currentSpan != null) {
                    // Propagation des en-têtes B3
                    requestTemplate.header("X-B3-TraceId", currentSpan.context().traceId());
                    requestTemplate.header("X-B3-SpanId", currentSpan.context().spanId());
                }
            }
        };
    }
}
