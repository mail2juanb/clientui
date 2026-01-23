package com.clientui.clientui.controller;

import com.clientui.clientui.beans.NoteBean;
import com.clientui.clientui.beans.PatientBean;
import com.clientui.clientui.beans.RiskLevelBean;
import com.clientui.clientui.dto.ValidationErrorDTO;
import com.clientui.clientui.proxies.MicroservicesProxy;
import com.clientui.clientui.tracing.TracingHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ClientController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ClientControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MicroservicesProxy servicesProxy;

    @MockitoBean
    private TracingHelper tracingHelper;

    private PatientBean testPatient;
    private List<PatientBean> testPatients;
    private List<NoteBean> testNotes;
    private RiskLevelBean testRiskLevel;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // Patients
        testPatient = new PatientBean();
        testPatient.setId(1L);
        testPatient.setLastname("TestNone");
        testPatient.setFirstname("Test");
        testPatient.setDateofbirth(LocalDate.of(1966, 12, 31));
        testPatient.setGender("F");
        testPatient.setAddress("1 Brookside St");
        testPatient.setPhone("100-222-3333");

        PatientBean patient2 = new PatientBean();
        patient2.setId(2L);
        patient2.setLastname("TestBorderline");
        patient2.setFirstname("Test");
        patient2.setDateofbirth(LocalDate.of(1945, 6, 24));
        patient2.setGender("M");
        patient2.setAddress("2 High St");
        patient2.setPhone("200-333-4444");

        testPatients = Arrays.asList(testPatient, patient2);

        // Notes
        NoteBean note1 = new NoteBean();
        note1.setPatId(1L);
        note1.setPatient("TestNone");
        note1.setNote("Patient states feeling terrific.");

        NoteBean note2 = new NoteBean();
        note2.setPatId(1L);
        note2.setPatient("TestNone");
        note2.setNote("Patient reports stress at work.");

        testNotes = Arrays.asList(note1, note2);

        // Risk level
        testRiskLevel = new RiskLevelBean();
        testRiskLevel.setPatId(1L);
        testRiskLevel.setRiskLevel("None");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testShowHomes() throws Exception {
        mockMvc.perform(get("/home")
                        .header("X-Auth-Username", "testuser")
                        .header("X-Auth-Roles", "ROLE_USER"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("currentPage", "home"))
                .andExpect(model().attribute("userConnected", "testuser"))
                .andExpect(model().attribute("userRole", "ROLE_USER"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testShowPatients() throws Exception {
        when(servicesProxy.retrievePatientList()).thenReturn(testPatients);

        mockMvc.perform(get("/patients")
                        .header("X-Auth-Username", "testuser")
                        .header("X-Auth-Roles", "ROLE_USER"))
                .andExpect(status().isOk())
                .andExpect(view().name("list"))
                .andExpect(model().attribute("patients", testPatients))
                .andExpect(model().attribute("patients", hasSize(2)));

        verify(servicesProxy, times(1)).retrievePatientList();
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testShowUpdateForm() throws Exception {
        when(servicesProxy.retrievePatientId(1L)).thenReturn(testPatient);
        when(servicesProxy.retrieveNotesPatId(1L)).thenReturn(testNotes);
        when(servicesProxy.getRiskLevel(1L)).thenReturn(testRiskLevel);

        mockMvc.perform(get("/update/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("update"))
                .andExpect(model().attribute("patient", testPatient))
                .andExpect(model().attribute("notes", testNotes))
                .andExpect(model().attribute("riskLevel", "None"));

        verify(servicesProxy).retrievePatientId(1L);
        verify(servicesProxy).retrieveNotesPatId(1L);
        verify(servicesProxy).getRiskLevel(1L);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testAddNoteSuccess() throws Exception {
        when(servicesProxy.retrievePatientId(1L)).thenReturn(testPatient);
        doNothing().when(servicesProxy).addNote(any(NoteBean.class));

        mockMvc.perform(post("/update/1/addnotes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("note", "New test note"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/update/1"))
                .andExpect(flash().attribute("success", "Note successfully added"));

        verify(servicesProxy).addNote(any(NoteBean.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAddPatientSuccess() throws Exception {
        doNothing().when(servicesProxy).addPatient(any(PatientBean.class));

        mockMvc.perform(post("/add/addPatient")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("lastname", "NewPatient")
                        .param("firstname", "John")
                        .param("dob", "1990-01-01")
                        .param("sex", "M")
                        .param("address", "123 Test St")
                        .param("phone", "555-1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/patients"))
                .andExpect(flash().attribute("success", "Patient successfully added"));

        verify(servicesProxy).addPatient(any(PatientBean.class));
    }

    @Test
    @DisplayName("Accès non authentifié redirigé vers login")
    void testUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("POST /add/addPatient - Validation error 400")
    void testAddPatientValidationError() throws Exception {
        ValidationErrorDTO validationError = new ValidationErrorDTO();
        validationError.setField("lastname");
        validationError.setDefaultMessage("Lastname cannot be empty");

        String body = objectMapper.writeValueAsString(List.of(validationError));

        Request fakeRequest = Request.create(Request.HttpMethod.POST, "/add/addPatient",
                Collections.emptyMap(), new byte[0], StandardCharsets.UTF_8, new RequestTemplate());

        FeignException feignException = FeignException.errorStatus("addPatient",
                Response.builder()
                        .request(fakeRequest)
                        .status(400)
                        .headers(Collections.emptyMap())
                        .body(body.getBytes(StandardCharsets.UTF_8))
                        .build()
        );

        doThrow(feignException).when(servicesProxy).addPatient(any(PatientBean.class));

        mockMvc.perform(post("/add/addPatient")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("lastname", "")
                        .param("firstname", "John")
                        .param("dob", "1990-01-01")
                        .param("sex", "M")
                        .param("address", "123 Test St")
                        .param("phone", "555-1234"))
                .andExpect(status().isOk())
                .andExpect(view().name("update"))
                .andExpect(model().attributeExists("errors"))
                .andExpect(model().attribute("errors",
                        org.hamcrest.Matchers.hasEntry("lastname", "Lastname cannot be empty")));
    }


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("POST /add/addPatient - Conflict 409")
    void testAddPatientConflictError() throws Exception {
        String body = objectMapper.writeValueAsString(Collections.singletonMap("error", "Patient already exists"));

        Request fakeRequest = Request.create(Request.HttpMethod.POST, "/add/addPatient",
                Collections.emptyMap(), new byte[0], StandardCharsets.UTF_8, new RequestTemplate());

        FeignException feignException = FeignException.errorStatus("addPatient",
                Response.builder()
                        .request(fakeRequest)
                        .status(409)
                        .headers(Collections.emptyMap())
                        .body(body.getBytes(StandardCharsets.UTF_8))
                        .build()
        );

        doThrow(feignException).when(servicesProxy).addPatient(any(PatientBean.class));

        mockMvc.perform(post("/add/addPatient")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("lastname", "Duplicate")
                        .param("firstname", "John")
                        .param("dob", "1990-01-01")
                        .param("sex", "M")
                        .param("address", "123 Test St")
                        .param("phone", "555-1234"))
                .andExpect(status().isOk())
                .andExpect(view().name("add"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Patient already exists"));
    }


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("POST /update/{id}/addnotes - Patient not found")
    void testAddNotePatientNotFound() throws Exception {
        // Simulates the recovery of a patient who does not exist.
        doThrow(FeignException.errorStatus(
                "retrievePatientId",
                Response.builder()
                        .request(Request.create(Request.HttpMethod.GET, "/patients/999",
                                Collections.emptyMap(), new byte[0], StandardCharsets.UTF_8, new RequestTemplate()))
                        .status(404)
                        .headers(Collections.emptyMap())
                        .body("Patient not found".getBytes(StandardCharsets.UTF_8))
                        .build()
        )).when(servicesProxy).retrievePatientId(999L);

        // The body of the test remains the same.
        mockMvc.perform(post("/update/999/addnotes")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .param("note", "Note for non-existing patient"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/patients"))
                .andExpect(flash().attributeExists("error"));
    }
}
