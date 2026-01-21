package com.clientui.clientui.exception;

import feign.FeignException;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Custom implementation of {@link ErrorDecoder} for Feign clients.
 * This class decodes Feign error responses and ensures the response body is readable for further processing.
 */
public class CustomErrorDecoder implements ErrorDecoder {

    /** Logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(CustomErrorDecoder.class);
    //private final ErrorDecoder defaultErrorDecoder = new Default();

    /**
     * Decodes the error response and reconstructs it to ensure the body remains readable.
     *
     * @param methodKey the Feign method key
     * @param response the Feign response containing the error
     * @return a {@link FeignException} with the decoded error information
     */
    @Override
    public Exception decode(String methodKey, Response response) {
        String body = null;

        if (response.body() != null) {
            try {
                body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
            } catch (IOException e) {
                log.debug("Error reading the response body", e);
            }
        }

        // Reconstruct the response with the body so that it remains legible afterwards.
        Response newResponse = response.toBuilder()
                .body(body, StandardCharsets.UTF_8)
                .build();

        return FeignException.errorStatus(methodKey, newResponse);
    }
}
