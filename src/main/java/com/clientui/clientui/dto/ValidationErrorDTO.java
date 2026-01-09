package com.clientui.clientui.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Data Transfer Object (DTO) for representing validation errors.
 * This class is used to encapsulate field-specific validation error messages.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidationErrorDTO {

    /** The name of the field that failed validation. */
    private String field;

    /** The default error message associated with the validation failure. */
    private String defaultMessage;

    /**
     * Gets the name of the field that failed validation.
     *
     * @return the field name
     */
    public String getField() {
        return field;
    }

    /**
     * Sets the name of the field that failed validation.
     *
     * @param field the field name to set
     */
    public void setField(String field) {
        this.field = field;
    }

    /**
     * Gets the default error message associated with the validation failure.
     *
     * @return the error message
     */
    public String getDefaultMessage() {
        return defaultMessage;
    }

    /**
     * Sets the default error message associated with the validation failure.
     *
     * @param defaultMessage the error message to set
     */
    public void setDefaultMessage(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }
}
