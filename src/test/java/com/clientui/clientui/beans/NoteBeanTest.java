package com.clientui.clientui.beans;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class NoteBeanTest {

    @Test
    void testDefaultConstructor() {
        // Arrange & Act
        NoteBean noteBean = new NoteBean();

        // Assert
        assertThat(noteBean).isNotNull();
        assertThat(noteBean.getPatId()).isNull();
        assertThat(noteBean.getPatient()).isNull();
        assertThat(noteBean.getNote()).isNull();
    }

    @Test
    void testParameterizedConstructor() {
        // Arrange
        Long patId = 1L;
        String patient = "Jean Dupont";
        String note = "Note de test";

        // Act
        NoteBean noteBean = new NoteBean(patId, patient, note);

        // Assert
        assertThat(noteBean.getPatId()).isEqualTo(patId);
        assertThat(noteBean.getPatient()).isEqualTo(patient);
        assertThat(noteBean.getNote()).isEqualTo(note);
    }

    @Test
    void testSettersAndGetters() {
        // Arrange
        NoteBean noteBean = new NoteBean();
        Long patId = 2L;
        String patient = "Marie Martin";
        String note = "Autre note de test";

        // Act
        noteBean.setPatId(patId);
        noteBean.setPatient(patient);
        noteBean.setNote(note);

        // Assert
        assertThat(noteBean.getPatId()).isEqualTo(patId);
        assertThat(noteBean.getPatient()).isEqualTo(patient);
        assertThat(noteBean.getNote()).isEqualTo(note);
    }

    @Test
    void testToString() {
        // Arrange
        Long patId = 3L;
        String patient = "Pierre Durand";
        String note = "Note pour test";
        NoteBean noteBean = new NoteBean(patId, patient, note);

        // Act
        String toStringResult = noteBean.toString();

        // Assert
        assertThat(toStringResult)
                .contains("NoteBean{")
                .contains("patId=" + patId)
                .contains("patient='" + patient + "'")
                .contains("note='" + note + "'");
    }

}
