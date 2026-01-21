package com.clientui.clientui.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ValidationErrorDTOTest {

    @Test
    void testGettersAndSetters() {
        ValidationErrorDTO errorDTO = new ValidationErrorDTO();

        // Checking default values
        assertThat(errorDTO.getField()).isNull();
        assertThat(errorDTO.getDefaultMessage()).isNull();

        // Definition of values
        String testField = "email";
        String testMessage = "Email is invalid";
        errorDTO.setField(testField);
        errorDTO.setDefaultMessage(testMessage);

        // Verification of values after definition
        assertThat(errorDTO.getField()).isEqualTo(testField);
        assertThat(errorDTO.getDefaultMessage()).isEqualTo(testMessage);
    }

    @Test
    void testNoArgsConstructor() {
        // Verifying that the constructor works without arguments
        ValidationErrorDTO errorDTO = new ValidationErrorDTO();
        assertThat(errorDTO).isNotNull();
    }

    // It shows that deserialisation works even if the JSON contains additional data.
    @Test
    void testDeserializationWithUnknownFields() throws Exception {
        // JSON with an unknown field and a ‘lastname’ field
        String json = "{\"field\":\"lastname\",\"defaultMessage\":\"Lastname is required\",\"unknownField\":\"value\"}";

        ObjectMapper mapper = new ObjectMapper();
        ValidationErrorDTO errorDTO = mapper.readValue(json, ValidationErrorDTO.class);

        // Verification that known fields are correctly deserialised
        assertThat(errorDTO.getField()).isEqualTo("lastname");
        assertThat(errorDTO.getDefaultMessage()).isEqualTo("Lastname is required");
    }
}
