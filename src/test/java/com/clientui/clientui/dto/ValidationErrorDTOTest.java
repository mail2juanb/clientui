package com.clientui.clientui.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ValidationErrorDTOTest {

    @Test
    void testGettersAndSetters() {
        // Création d'une instance de ValidationErrorDTO
        ValidationErrorDTO errorDTO = new ValidationErrorDTO();

        // Vérification des valeurs par défaut
        assertThat(errorDTO.getField()).isNull();
        assertThat(errorDTO.getDefaultMessage()).isNull();

        // Définition des valeurs
        String testField = "email";
        String testMessage = "Email is invalid";
        errorDTO.setField(testField);
        errorDTO.setDefaultMessage(testMessage);

        // Vérification des valeurs après définition
        assertThat(errorDTO.getField()).isEqualTo(testField);
        assertThat(errorDTO.getDefaultMessage()).isEqualTo(testMessage);
    }

    @Test
    void testNoArgsConstructor() {
        // Vérification que le constructeur sans arguments fonctionne
        ValidationErrorDTO errorDTO = new ValidationErrorDTO();
        assertThat(errorDTO).isNotNull();
    }

    // Il valide que la classe ValidationErrorDTO peut ignorer les champs inconnus dans un JSON, ce qui est souvent nécessaire pour la compatibilité ascendante ou descendante des APIs.
    // Il montre que la désérialisation fonctionne même si le JSON contient des données supplémentaires.
    @Test
    void testDeserializationWithUnknownFields() throws Exception {
        // JSON avec un champ inconnu et un champ "lastname"
        String json = "{\"field\":\"lastname\",\"defaultMessage\":\"Lastname is required\",\"unknownField\":\"value\"}";

        ObjectMapper mapper = new ObjectMapper();
        ValidationErrorDTO errorDTO = mapper.readValue(json, ValidationErrorDTO.class);

        // Vérification que les champs connus sont correctement désérialisés
        assertThat(errorDTO.getField()).isEqualTo("lastname");
        assertThat(errorDTO.getDefaultMessage()).isEqualTo("Lastname is required");
    }
}
