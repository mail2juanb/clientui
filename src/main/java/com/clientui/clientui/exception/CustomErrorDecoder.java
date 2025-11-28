package com.clientui.clientui.exception;

import feign.FeignException;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


public class CustomErrorDecoder implements ErrorDecoder {

    private static final Logger log = LoggerFactory.getLogger(CustomErrorDecoder.class);
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {

        log.debug("CustomErrorDecoder appelé avec status : {}", response.status());

        String body = null;

        try {
            body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
            log.debug("Corps de la réponse : {}", body);
        } catch (IOException e) {
            log.error("Erreur lors de la lecture du corps de la réponse", e);
        }

        // RECONSTRUIRE LE RESPONSE avec le body pour qu'il reste lisible ensuite
        Response newResponse = response.toBuilder()
                .body(body, StandardCharsets.UTF_8)
                .build();

        log.debug("CustomErrorDecoder fin de la méthode...");

        return FeignException.errorStatus(methodKey, newResponse);
    }
}
