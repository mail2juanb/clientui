package com.clientui.clientui.exceptions;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomErrorDecoder implements ErrorDecoder {

    private static final Logger log = LoggerFactory.getLogger(CustomErrorDecoder.class);
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode (String invoqueur, Response response) {
        if (response.status() == 400) {
            return new PatientBadRequestException("Requête incorrecte..." + response.body() + "....." + response.status() + "...." + response.reason());
        }
        return defaultErrorDecoder.decode(invoqueur, response);
    }
}
