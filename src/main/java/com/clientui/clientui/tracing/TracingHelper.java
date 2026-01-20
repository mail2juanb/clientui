package com.clientui.clientui.tracing;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.stereotype.Component;

/**
 * Helper class for managing tracing spans and tags in a Spring Boot application.
 * This class provides utility methods to add tags, events, and error information to the current span.
 */
@Component
public class TracingHelper {

    private final Tracer tracer;


    /**
     * Constructs a new TracingHelper with the provided Tracer.
     *
     * @param tracer The Tracer instance used to interact with the current span.
     */
    public TracingHelper(Tracer tracer) {
        this.tracer = tracer;
    }


    /**
     * Retrieves the current span from the tracer.
     *
     * @return The current span, or null if no span is active or tracer is not available.
     */
    private Span currentSpan() {
        return tracer != null ? tracer.currentSpan() : null;
    }


    /**
     * Adds a tag with a string value to the current span.
     *
     * @param key   The key of the tag.
     * @param value The value of the tag.
     */
    public void tag(String key, String value) {
        Span span = currentSpan();
        if (span != null) {
            span.tag(key, value);
        }
    }


    /**
     * Adds a tag with a long value to the current span.
     *
     * @param key   The key of the tag.
     * * @param value The long value of the tag, converted to a string.
     */
    public void tag(String key, long value) {
        tag(key, String.valueOf(value));
    }


    /**
     * Records an event on the current span.
     *
     * @param message The message describing the event.
     */
    public void event(String message) {
        Span span = currentSpan();
        if (span != null) {
            span.event(message);
        }
    }


    /**
     * Records an error event on the current span, including the error type and message.
     *
     * @param type    The type of the error.
     * @param message The error message.
     */
    public void error(String type, String message) {
        Span span = currentSpan();
        if (span != null) {
            span.tag("error.type", type);
            span.event(message);
        }
    }

}
