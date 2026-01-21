package com.clientui.clientui.controller;

import com.clientui.clientui.beans.NoteBean;
import com.clientui.clientui.beans.PatientBean;
import com.clientui.clientui.beans.RiskLevelBean;
import com.clientui.clientui.proxies.MicroservicesProxy;
import com.clientui.clientui.tracing.TracingHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientControllerTest {

    @Mock
    private MicroservicesProxy servicesProxy;

    @Mock
    private TracingHelper tracing;

    private ClientController controller;

    @BeforeEach
    void setUp() {
        controller = new ClientController(servicesProxy, tracing);
    }


    @Test
    void addUserInfoToModel_shouldAddUserInfo() {
        Model model = mock(Model.class);

        controller.addUserInfoToModel("john", "ADMIN", model);

        verify(model).addAttribute("userConnected", "john");
        verify(model).addAttribute("userRole", "ADMIN");
    }


    @Test
    void showHomes_shouldReturnHomeView() {
        Model model = mock(Model.class);

        String view = controller.showHomes("john", "ADMIN", model);

        assertThat(view).isEqualTo("home");
        verify(model).addAttribute("currentPage", "home");
        verify(model).addAttribute("userConnected", "john");
        verify(model).addAttribute("userRole", "ADMIN");
    }

    @Test
    void showHomes_withDefaultUsername_shouldReturnHomeView() {
        Model model = mock(Model.class);

        String view = controller.showHomes("PasDeUsername", "PasDeRole", model);

        assertThat(view).isEqualTo("home");
        verify(model).addAttribute("userConnected", "PasDeUsername");
        verify(model).addAttribute("userRole", "PasDeRole");
    }


    @Test
    void showPatients_shouldReturnListView() {
        Model model = mock(Model.class);

        when(servicesProxy.retrievePatientList())
                .thenReturn(List.of(new PatientBean()));

        String view = controller.showPatients(model, null);

        assertThat(view).isEqualTo("list");
        verify(model).addAttribute("currentPage", "patients");
        verify(model).addAttribute(eq("patients"), any(List.class));
    }

    @Test
    void showPatients_withError_shouldAddErrorToModel() {
        Model model = mock(Model.class);
        when(servicesProxy.retrievePatientList()).thenReturn(List.of());

        controller.showPatients(model, "error-msg");

        verify(model).addAttribute("error", "error-msg");
    }


    @Test
    void showUpdateForm_shouldReturnUpdateView() {
        Model model = mock(Model.class);

        PatientBean patient = new PatientBean();
        patient.setLastname("Doe");

        RiskLevelBean riskLevel = new RiskLevelBean();
        riskLevel.setRiskLevel("BORDERLINE");

        when(servicesProxy.retrievePatientId(1L)).thenReturn(patient);
        when(servicesProxy.retrieveNotesPatId(1L)).thenReturn(List.of());
        when(servicesProxy.getRiskLevel(1L)).thenReturn(riskLevel);

        String view = controller.showUpdateForm(1L, model);

        assertThat(view).isEqualTo("update");
        verify(model).addAttribute("currentPage", "update");
        verify(model).addAttribute("patient", patient);
        verify(model).addAttribute(eq("notes"), any(List.class));
        verify(model).addAttribute("riskLevel", "BORDERLINE");
    }

    @Test
    void showUpdateForm_whenNotesAreNull_shouldInitializeEmptyList() {
        Model model = mock(Model.class);

        PatientBean patient = new PatientBean();
        patient.setLastname("Doe");

        RiskLevelBean riskLevel = new RiskLevelBean();
        riskLevel.setRiskLevel("NONE");

        when(servicesProxy.retrievePatientId(1L)).thenReturn(patient);
        when(servicesProxy.retrieveNotesPatId(1L)).thenReturn(null);
        when(servicesProxy.getRiskLevel(1L)).thenReturn(riskLevel);

        String view = controller.showUpdateForm(1L, model);

        assertThat(view).isEqualTo("update");
        verify(model).addAttribute(eq("notes"), argThat(list ->
                list instanceof List && ((List<?>) list).isEmpty()
        ));
    }

    @Test
    void showUpdateForm_shouldCreateNewNoteWithCorrectPatientInfo() {
        Model model = mock(Model.class);

        PatientBean patient = new PatientBean();
        patient.setId(5L);
        patient.setLastname("Smith");

        RiskLevelBean riskLevel = new RiskLevelBean();
        riskLevel.setRiskLevel("HIGH");

        when(servicesProxy.retrievePatientId(5L)).thenReturn(patient);
        when(servicesProxy.retrieveNotesPatId(5L)).thenReturn(List.of());
        when(servicesProxy.getRiskLevel(5L)).thenReturn(riskLevel);

        controller.showUpdateForm(5L, model);

        verify(model).addAttribute(eq("newNote"), argThat(note ->
                note instanceof NoteBean &&
                        ((NoteBean) note).getPatId().equals(5L) &&
                        ((NoteBean) note).getPatient().equals("Smith")
        ));
    }


    @Test
    void addNote_withValidationErrors_shouldReturnUpdateView() {
        Model model = mock(Model.class);
        BindingResult result = mock(BindingResult.class);
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        NoteBean note = new NoteBean();
        PatientBean patient = new PatientBean();

        when(result.hasErrors()).thenReturn(true);
        when(servicesProxy.retrievePatientId(1L)).thenReturn(patient);
        when(servicesProxy.retrieveNotesPatId(1L)).thenReturn(List.of());

        String view = controller.addNote(1L, note, result, model, redirectAttributes);

        assertThat(view).isEqualTo("update");
        verify(servicesProxy, never()).addNote(any());
    }

    @Test
    void addNote_withValidationErrors_shouldLogErrors() {
        Model model = mock(Model.class);
        BindingResult result = mock(BindingResult.class);
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        NoteBean note = new NoteBean();
        PatientBean patient = new PatientBean();
        patient.setLastname("TestName");

        ObjectError error1 = new ObjectError("note", "Error 1");
        ObjectError error2 = new ObjectError("note", "Error 2");

        when(result.hasErrors()).thenReturn(true);
        when(result.getAllErrors()).thenReturn(List.of(error1, error2));
        when(servicesProxy.retrievePatientId(1L)).thenReturn(patient);
        when(servicesProxy.retrieveNotesPatId(1L)).thenReturn(List.of());

        controller.addNote(1L, note, result, model, redirectAttributes);

        verify(result).getAllErrors();
    }

    @Test
    void addNote_withoutErrors_shouldRedirect() {
        Model model = mock(Model.class);
        BindingResult result = mock(BindingResult.class);
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        NoteBean note = new NoteBean();
        PatientBean patient = new PatientBean();
        patient.setLastname("Doe");

        when(result.hasErrors()).thenReturn(false);
        when(servicesProxy.retrievePatientId(1L)).thenReturn(patient);

        String view = controller.addNote(1L, note, result, model, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/update/1");
        assertThat(note.getPatId()).isEqualTo(1L);
        assertThat(note.getPatient()).isEqualTo("Doe");
        verify(servicesProxy).addNote(note);
        verify(redirectAttributes)
                .addFlashAttribute("success", "Note successfully added");
    }


    @Test
    void showAddPatientForm_shouldReturnAddView() {
        Model model = mock(Model.class);

        String view = controller.showAddPatientForm(model);

        assertThat(view).isEqualTo("add");
        verify(model).addAttribute(eq("patient"), any(PatientBean.class));
        verify(model).addAttribute("currentPage", "add");
    }


    @Test
    void addPatient_shouldRedirectToPatients() {
        PatientBean patient = new PatientBean();
        Model model = mock(Model.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        String view = controller.addPatient(patient, model, request, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/patients");
        verify(servicesProxy).addPatient(patient);
        verify(redirectAttributes)
                .addFlashAttribute("success", "Patient successfully added");
    }

    @Test
    void addPatient_shouldSetRequestAttributes() {
        PatientBean patient = new PatientBean();
        patient.setLastname("Test");
        Model model = mock(Model.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        controller.addPatient(patient, model, request, redirectAttributes);

        verify(request).setAttribute("patient", patient);
        verify(request).setAttribute("targetView", "add");
    }


    @Test
    void updatePatient_shouldRedirectToUpdatePage() {
        PatientBean patient = new PatientBean();
        HttpServletRequest request = mock(HttpServletRequest.class);
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        String view = controller.updatePatient(1L, patient, request, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/update/1");
        assertThat(patient.getId()).isEqualTo(1L);
        verify(servicesProxy).updatePatient(1L, patient);
        verify(redirectAttributes)
                .addFlashAttribute("success", "Patient successfully updated");
    }

    @Test
    void updatePatient_shouldSetRequestAttributes() {
        PatientBean patient = new PatientBean();
        HttpServletRequest request = mock(HttpServletRequest.class);
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        controller.updatePatient(3L, patient, request, redirectAttributes);

        verify(request).setAttribute("patient", patient);
        verify(request).setAttribute("targetView", "update");
        assertThat(patient.getId()).isEqualTo(3L);
    }
}