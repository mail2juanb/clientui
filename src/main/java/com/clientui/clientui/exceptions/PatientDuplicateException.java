package com.clientui.clientui.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class PatientDuplicateException extends RuntimeException {

    private static final Logger log = LoggerFactory.getLogger(PatientDuplicateException.class);

    public PatientDuplicateException(String message) {
        super(message);
        log.info("THROW Exception : {} - message : {}", getClass().getName(), getMessage());
    }
}
