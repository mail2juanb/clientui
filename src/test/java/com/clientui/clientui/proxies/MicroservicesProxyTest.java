package com.clientui.clientui.proxies;

import com.clientui.clientui.beans.NoteBean;
import com.clientui.clientui.beans.PatientBean;
import com.clientui.clientui.beans.RiskLevelBean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MicroservicesProxyTest {

    @Mock
    private MicroservicesProxy microservicesProxy;


    @Test
    void testRetrievePatientList() {
        // Arrange
        PatientBean patient1 = new PatientBean();
        patient1.setId(1L);
        PatientBean patient2 = new PatientBean();
        patient2.setId(2L);
        List<PatientBean> expectedPatients = Arrays.asList(patient1, patient2);

        when(microservicesProxy.retrievePatientList()).thenReturn(expectedPatients);

        // Act
        List<PatientBean> result = microservicesProxy.retrievePatientList();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        verify(microservicesProxy, times(1)).retrievePatientList();
    }

    @Test
    void testRetrievePatientId() {
        // Arrange
        PatientBean expectedPatient = new PatientBean();
        expectedPatient.setId(1L);
        when(microservicesProxy.retrievePatientId(anyLong())).thenReturn(expectedPatient);

        // Act
        PatientBean result = microservicesProxy.retrievePatientId(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(microservicesProxy, times(1)).retrievePatientId(1L);
    }

    @Test
    void testAddPatient() {
        // Arrange
        PatientBean newPatient = new PatientBean();
        newPatient.setId(3L);

        // Act
        microservicesProxy.addPatient(newPatient);

        // Assert
        verify(microservicesProxy, times(1)).addPatient(any(PatientBean.class));
    }

    @Test
    void testUpdatePatient() {
        // Arrange
        PatientBean updatedPatient = new PatientBean();
        updatedPatient.setId(1L);

        // Act
        microservicesProxy.updatePatient(1L, updatedPatient);

        // Assert
        verify(microservicesProxy, times(1)).updatePatient(anyLong(), any(PatientBean.class));
    }

    @Test
    void testRetrieveNotesPatId() {
        // Arrange
        NoteBean note1 = new NoteBean();
        note1.setPatId(102L);
        NoteBean note2 = new NoteBean();
        note2.setPatId(103L);
        List<NoteBean> expectedNotes = Arrays.asList(note1, note2);

        when(microservicesProxy.retrieveNotesPatId(anyLong())).thenReturn(expectedNotes);

        // Act
        List<NoteBean> result = microservicesProxy.retrieveNotesPatId(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        verify(microservicesProxy, times(1)).retrieveNotesPatId(1L);
    }

    @Test
    void testAddNote() {
        // Arrange
        NoteBean newNote = new NoteBean();
        newNote.setPatId(99L);

        // Act
        microservicesProxy.addNote(newNote);

        // Assert
        verify(microservicesProxy, times(1)).addNote(any(NoteBean.class));
    }

    @Test
    void testGetRiskLevel() {
        // Arrange
        RiskLevelBean riskLevel = new RiskLevelBean();
        riskLevel.setRiskLevel("HIGH");
        when(microservicesProxy.getRiskLevel(anyLong())).thenReturn(riskLevel);

        // Act
        RiskLevelBean result = microservicesProxy.getRiskLevel(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getRiskLevel()).isEqualTo("HIGH");
        verify(microservicesProxy, times(1)).getRiskLevel(1L);
    }
}
