package com.clientui.clientui.exception;

import com.clientui.clientui.beans.NoteBean;
import com.clientui.clientui.beans.PatientBean;
import com.clientui.clientui.beans.RiskLevelBean;
import com.clientui.clientui.proxies.MicroservicesProxy;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FeignExceptionHandlerTest {

    @Mock
    private MicroservicesProxy servicesProxy;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private FeignExceptionHandler feignExceptionHandler;

    private PatientBean patientBean;
    private RiskLevelBean riskLevelBean;
    private List<NoteBean> notesList;

    @BeforeEach
    void setUp() {
        patientBean = new PatientBean();
        patientBean.setId(1L);
        patientBean.setLastname("Doe");
        patientBean.setFirstname("John");
        patientBean.setGender("M");

        riskLevelBean = new RiskLevelBean();
        riskLevelBean.setRiskLevel("Undefined");

        notesList = new ArrayList<>();
    }

    /**
     * Creates an instance of FeignException with a given status and body.
     */
    private FeignException createFeignException(int status, String body) {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "/test",
                Collections.emptyMap(),
                null,
                StandardCharsets.UTF_8,
                new RequestTemplate()
        );

        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

        return FeignException.errorStatus(
                "testMethod",
                feign.Response.builder()
                        .status(status)
                        .reason("Test Reason")
                        .request(request)
                        .headers(Collections.emptyMap())
                        .body(bodyBytes)
                        .build()
        );
    }

    // ==================== TESTS POUR SERVICE UNAVAILABLE (503 et 500+) ====================

    @Test
    void handleFeignException_WhenStatus503_ShouldReturnHomeWithServiceUnavailableMessage() {
        // Arrange
        FeignException feignException = createFeignException(503, "Service Unavailable");

        // Act
        ModelAndView mav = feignExceptionHandler.handleFeignException(feignException, request);

        // Assert
        assertEquals("home", mav.getViewName());
        assertEquals("home", mav.getModel().get("currentPage"));
        assertEquals("A required service is currently unavailable. Please try again later.",
                mav.getModel().get("error"));
    }

    @Test
    void handleFeignException_WhenStatus500_ShouldReturnHomeWithInternalErrorMessage() {
        // Arrange
        FeignException feignException = createFeignException(500, "Internal Server Error");

        // Act
        ModelAndView mav = feignExceptionHandler.handleFeignException(feignException, request);

        // Assert
        assertEquals("home", mav.getViewName());
        assertEquals("home", mav.getModel().get("currentPage"));
        assertEquals("A service encountered an internal error (status 500). Please try again later.",
                mav.getModel().get("error"));
    }

    @Test
    void handleFeignException_WhenStatus502_ShouldReturnHomeWithInternalErrorMessage() {
        // Arrange
        FeignException feignException = createFeignException(502, "Bad Gateway");

        // Act
        ModelAndView mav = feignExceptionHandler.handleFeignException(feignException, request);

        // Assert
        assertEquals("home", mav.getViewName());
        assertEquals("A service encountered an internal error (status 502). Please try again later.",
                mav.getModel().get("error"));
    }

    // ==================== TESTS POUR /patients URI ====================

    @Test
    void handleFeignException_WhenErrorOnPatientsListRetrieval_ShouldReturnListViewWithError() {
        // Arrange
        when(request.getRequestURI()).thenReturn("/patients");
        FeignException feignException = createFeignException(400, "[]");

        // Act
        ModelAndView mav = feignExceptionHandler.handleFeignException(feignException, request);

        // Assert
        assertEquals("list", mav.getViewName());
        assertEquals("patients", mav.getModel().get("currentPage"));
        assertTrue(mav.getModel().containsKey("error"));
        assertTrue(((String) mav.getModel().get("error")).contains("Erreur lors de la récupération des patients"));
        assertNotNull(mav.getModel().get("patients"));
        assertTrue(((List<?>) mav.getModel().get("patients")).isEmpty());
    }

    @Test
    void handleFeignException_WhenErrorOnPatientsUpdate_ShouldNotReturnListView() {
        // Arrange
        when(request.getRequestURI()).thenReturn("/patients/update/1");
        when(request.getAttribute("patient")).thenReturn(patientBean);
        when(request.getAttribute("targetView")).thenReturn("update");
        when(servicesProxy.retrievePatientId(anyLong())).thenReturn(patientBean);
        when(servicesProxy.retrieveNotesPatId(anyLong())).thenReturn(notesList);
        when(servicesProxy.getRiskLevel(anyLong())).thenReturn(riskLevelBean);

        FeignException feignException = createFeignException(400, "[]");

        // Act
        ModelAndView mav = feignExceptionHandler.handleFeignException(feignException, request);

        // Assert
        assertNotEquals("list", mav.getViewName());
        assertEquals("update", mav.getViewName());
    }

    // ==================== TESTS POUR NO PATIENT IN REQUEST ====================

    @Test
    void handleFeignException_WhenNoPatientInRequest_ShouldRedirectToHome() {
        // Arrange
        when(request.getAttribute("patient")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/test");
        FeignException feignException = createFeignException(400, "[]");

        // Act
        ModelAndView mav = feignExceptionHandler.handleFeignException(feignException, request);

        // Assert
        assertEquals("home", mav.getViewName());
        assertEquals("No patient ID found in query.", mav.getModel().get("error"));
    }

    // ==================== TESTS POUR PATIENT SANS ID ====================

    @Test
    void handleFeignException_WhenPatientHasNoId_ShouldNotCallServices() {
        // Arrange
        patientBean.setId(null);
        when(request.getAttribute("patient")).thenReturn(patientBean);
        when(request.getAttribute("targetView")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/test");
        FeignException feignException = createFeignException(400, "[]");

        // Act
        ModelAndView mav = feignExceptionHandler.handleFeignException(feignException, request);

        // Assert
        assertEquals("update", mav.getViewName());
        verify(servicesProxy, never()).retrievePatientId(anyLong());
        verify(servicesProxy, never()).retrieveNotesPatId(anyLong());
        verify(servicesProxy, never()).getRiskLevel(anyLong());
        assertEquals("Undefined", mav.getModel().get("riskLevel"));
    }

    // ==================== TESTS POUR STATUS 400 - VALIDATION ERRORS ====================

    @Test
    void handleFeignException_WhenStatus400WithValidationErrors_ShouldAddErrorsToModel() {
        // Arrange
        when(request.getAttribute("patient")).thenReturn(patientBean);
        when(request.getAttribute("targetView")).thenReturn("update");
        when(request.getRequestURI()).thenReturn("/test");
        when(servicesProxy.retrievePatientId(anyLong())).thenReturn(patientBean);
        when(servicesProxy.retrieveNotesPatId(anyLong())).thenReturn(notesList);
        when(servicesProxy.getRiskLevel(anyLong())).thenReturn(riskLevelBean);

        String errorBody = "[{\"field\":\"note\",\"defaultMessage\":\"note is mandatory\"}," +
                "{\"field\":\"patId\",\"defaultMessage\":\"patId cannot be null\"}]";
        FeignException feignException = createFeignException(400, errorBody);

        // Act
        ModelAndView mav = feignExceptionHandler.handleFeignException(feignException, request);

        // Assert
        assertEquals("update", mav.getViewName());
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) mav.getModel().get("errors");
        assertNotNull(errors);
        assertEquals("note is mandatory", errors.get("note"));
        assertEquals("patId cannot be null", errors.get("patId"));
    }

    @Test
    void handleFeignException_WhenStatus400WithNewNoteInRequest_ShouldAddNewNoteToModel() {
        // Arrange
        NoteBean noteFromRequest = new NoteBean();
        noteFromRequest.setNote("Test note");
        noteFromRequest.setPatId(1L);

        when(request.getAttribute("patient")).thenReturn(patientBean);
        when(request.getAttribute("newNote")).thenReturn(noteFromRequest);
        when(request.getAttribute("targetView")).thenReturn("update");
        when(request.getRequestURI()).thenReturn("/test");
        when(servicesProxy.retrievePatientId(anyLong())).thenReturn(patientBean);
        when(servicesProxy.retrieveNotesPatId(anyLong())).thenReturn(notesList);
        when(servicesProxy.getRiskLevel(anyLong())).thenReturn(riskLevelBean);

        String errorBody = "[{\"field\":\"note\",\"defaultMessage\":\"note is mandatory\"}]";
        FeignException feignException = createFeignException(400, errorBody);

        // Act
        ModelAndView mav = feignExceptionHandler.handleFeignException(feignException, request);

        // Assert
        assertEquals("update", mav.getViewName());
        NoteBean resultNote = (NoteBean) mav.getModel().get("newNote");
        assertNotNull(resultNote);
        assertEquals("Test note", resultNote.getNote());
        assertEquals(1L, resultNote.getPatId());
    }

    @Test
    void handleFeignException_WhenStatus400WithInvalidJson_ShouldRedirectToHome() {
        // Arrange
        when(request.getAttribute("patient")).thenReturn(patientBean);
        when(request.getRequestURI()).thenReturn("/test");
        when(servicesProxy.retrievePatientId(anyLong())).thenReturn(patientBean);
        when(servicesProxy.retrieveNotesPatId(anyLong())).thenReturn(notesList);
        when(servicesProxy.getRiskLevel(anyLong())).thenReturn(riskLevelBean);

        FeignException feignException = createFeignException(400, "invalid json");

        // Act
        ModelAndView mav = feignExceptionHandler.handleFeignException(feignException, request);

        // Assert
        assertEquals("home", mav.getViewName());
        assertTrue(mav.getModel().containsKey("error"));
        assertTrue(((String) mav.getModel().get("error")).contains("Error processing response status"));
    }

    @Test
    void handleFeignException_WhenStatus400WithEmptyBody_ShouldRedirectToHome() {
        // Arrange
        when(request.getAttribute("patient")).thenReturn(patientBean);
        when(request.getRequestURI()).thenReturn("/test");
        when(servicesProxy.retrievePatientId(anyLong())).thenReturn(patientBean);
        when(servicesProxy.retrieveNotesPatId(anyLong())).thenReturn(notesList);
        when(servicesProxy.getRiskLevel(anyLong())).thenReturn(riskLevelBean);

        FeignException feignException = createFeignException(400, "");

        // Act
        ModelAndView mav = feignExceptionHandler.handleFeignException(feignException, request);

        // Assert
        assertEquals("home", mav.getViewName());
        assertTrue(mav.getModel().containsKey("error"));
    }

    // ==================== TESTS POUR STATUS 409 - CONFLICT ====================

    @Test
    void handleFeignException_WhenStatus409WithValidJson_ShouldAddErrorMessage() {
        // Arrange
        when(request.getAttribute("patient")).thenReturn(patientBean);
        when(request.getAttribute("targetView")).thenReturn("update");
        when(request.getRequestURI()).thenReturn("/test");
        when(servicesProxy.retrievePatientId(anyLong())).thenReturn(patientBean);
        when(servicesProxy.retrieveNotesPatId(anyLong())).thenReturn(notesList);
        when(servicesProxy.getRiskLevel(anyLong())).thenReturn(riskLevelBean);

        String errorBody = "{\"error\":\"Duplicate entry detected\"}";
        FeignException feignException = createFeignException(409, errorBody);

        // Act
        ModelAndView mav = feignExceptionHandler.handleFeignException(feignException, request);

        // Assert
        assertEquals("update", mav.getViewName());
        assertEquals("Duplicate entry detected", mav.getModel().get("error"));
    }

    @Test
    void handleFeignException_WhenStatus409WithoutErrorKey_ShouldUseDefaultMessage() {
        // Arrange
        when(request.getAttribute("patient")).thenReturn(patientBean);
        when(request.getAttribute("targetView")).thenReturn("update");
        when(request.getRequestURI()).thenReturn("/test");
        when(servicesProxy.retrievePatientId(anyLong())).thenReturn(patientBean);
        when(servicesProxy.retrieveNotesPatId(anyLong())).thenReturn(notesList);
        when(servicesProxy.getRiskLevel(anyLong())).thenReturn(riskLevelBean);

        String errorBody = "{\"message\":\"Some other message\"}";
        FeignException feignException = createFeignException(409, errorBody);

        // Act
        ModelAndView mav = feignExceptionHandler.handleFeignException(feignException, request);

        // Assert
        assertEquals("update", mav.getViewName());
        assertEquals("Conflict detected.", mav.getModel().get("error"));
    }

    @Test
    void handleFeignException_WhenStatus409WithInvalidJson_ShouldRedirectToHome() {
        // Arrange
        when(request.getAttribute("patient")).thenReturn(patientBean);
        when(request.getRequestURI()).thenReturn("/test");
        when(servicesProxy.retrievePatientId(anyLong())).thenReturn(patientBean);
        when(servicesProxy.retrieveNotesPatId(anyLong())).thenReturn(notesList);
        when(servicesProxy.getRiskLevel(anyLong())).thenReturn(riskLevelBean);

        FeignException feignException = createFeignException(409, "invalid json");

        // Act
        ModelAndView mav = feignExceptionHandler.handleFeignException(feignException, request);

        // Assert
        assertEquals("home", mav.getViewName());
        assertTrue(mav.getModel().containsKey("error"));
        assertTrue(((String) mav.getModel().get("error")).contains("Conflict detected (unexpected format)"));
    }

    // ==================== TESTS POUR AUTRES STATUS CODES ====================

    @Test
    void handleFeignException_WhenStatus401_ShouldRedirectToHome() {
        // Arrange
        when(request.getAttribute("patient")).thenReturn(patientBean);
        when(request.getRequestURI()).thenReturn("/test");
        when(servicesProxy.retrievePatientId(anyLong())).thenReturn(patientBean);
        when(servicesProxy.retrieveNotesPatId(anyLong())).thenReturn(notesList);
        when(servicesProxy.getRiskLevel(anyLong())).thenReturn(riskLevelBean);

        FeignException feignException = createFeignException(401, "Unauthorized");

        // Act
        ModelAndView mav = feignExceptionHandler.handleFeignException(feignException, request);

        // Assert
        assertEquals("home", mav.getViewName());
        assertTrue(mav.getModel().containsKey("error"));
        assertTrue(((String) mav.getModel().get("error")).contains("Error 401 :"));
    }

    // ==================== TESTS POUR FEIGN EXCEPTION LORS DE LA RÉCUPÉRATION DES DONNÉES ====================

    @Test
    void handleFeignException_WhenFeignExceptionOnDataFetchWithStatus404_ShouldRedirectToPatients() {
        // Arrange
        when(request.getAttribute("patient")).thenReturn(patientBean);
        when(request.getRequestURI()).thenReturn("/test");

        FeignException innerException = createFeignException(404, "Not Found");
        when(servicesProxy.retrievePatientId(anyLong())).thenThrow(innerException);

        FeignException feignException = createFeignException(400, "[]");

        // Act
        ModelAndView mav = feignExceptionHandler.handleFeignException(feignException, request);

        // Assert
        assertEquals("redirect:/patients", mav.getViewName());
        assertTrue(mav.getModel().containsKey("error"));
        assertTrue(((String) mav.getModel().get("error")).startsWith("Error retrieving patient data :"));
    }

    @Test
    void handleFeignException_WhenFeignExceptionOnDataFetchWithStatus503_ShouldRedirectToHome() {
        // Arrange
        when(request.getAttribute("patient")).thenReturn(patientBean);
        when(request.getRequestURI()).thenReturn("/test");

        FeignException innerException = createFeignException(503, "Service Unavailable");
        when(servicesProxy.retrievePatientId(anyLong())).thenThrow(innerException);

        FeignException feignException = createFeignException(400, "[]");

        // Act
        ModelAndView mav = feignExceptionHandler.handleFeignException(feignException, request);

        // Assert
        assertEquals("home", mav.getViewName());
        assertEquals("A required service is currently unavailable. Please try again later.",
                mav.getModel().get("error"));
    }

    @Test
    void handleFeignException_WhenFeignExceptionOnDataFetchWithStatus500_ShouldRedirectToHome() {
        // Arrange
        when(request.getAttribute("patient")).thenReturn(patientBean);
        when(request.getRequestURI()).thenReturn("/test");

        FeignException innerException = createFeignException(500, "Internal Server Error");
        when(servicesProxy.retrievePatientId(anyLong())).thenThrow(innerException);

        FeignException feignException = createFeignException(400, "[]");

        // Act
        ModelAndView mav = feignExceptionHandler.handleFeignException(feignException, request);

        // Assert
        assertEquals("home", mav.getViewName());
        assertEquals("A service encountered an internal error (status 500). Please try again later.",
                mav.getModel().get("error"));
    }

    // ==================== TESTS POUR TARGET VIEW PERSONNALISÉ ====================

    @Test
    void handleFeignException_WithCustomTargetView_ShouldUseSpecifiedView() {
        // Arrange
        when(request.getAttribute("patient")).thenReturn(patientBean);
        when(request.getAttribute("targetView")).thenReturn("custom-view");
        when(request.getRequestURI()).thenReturn("/test");
        when(servicesProxy.retrievePatientId(anyLong())).thenReturn(patientBean);
        when(servicesProxy.retrieveNotesPatId(anyLong())).thenReturn(notesList);
        when(servicesProxy.getRiskLevel(anyLong())).thenReturn(riskLevelBean);

        String errorBody = "[{\"field\":\"email\",\"defaultMessage\":\"Invalid email\"}]";
        FeignException feignException = createFeignException(400, errorBody);

        // Act
        ModelAndView mav = feignExceptionHandler.handleFeignException(feignException, request);

        // Assert
        assertEquals("update", mav.getViewName());
        assertEquals("custom-view", mav.getModel().get("currentPage"));
    }

    @Test
    void handleFeignException_WithNullTargetView_ShouldUseDefaultUpdateView() {
        // Arrange
        when(request.getAttribute("patient")).thenReturn(patientBean);
        when(request.getAttribute("targetView")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/test");
        when(servicesProxy.retrievePatientId(anyLong())).thenReturn(patientBean);
        when(servicesProxy.retrieveNotesPatId(anyLong())).thenReturn(notesList);
        when(servicesProxy.getRiskLevel(anyLong())).thenReturn(riskLevelBean);

        String errorBody = "[{\"field\":\"name\",\"defaultMessage\":\"Name required\"}]";
        FeignException feignException = createFeignException(400, errorBody);

        // Act
        ModelAndView mav = feignExceptionHandler.handleFeignException(feignException, request);

        // Assert
        assertEquals("update", mav.getViewName());
        assertEquals("update", mav.getModel().get("currentPage"));
    }

    // ==================== TESTS POUR VÉRIFIER LES ATTRIBUTS DU MODÈLE ====================

    @Test
    void handleFeignException_ShouldAddAllRequiredAttributesToModel() {
        // Arrange
        when(request.getAttribute("patient")).thenReturn(patientBean);
        when(request.getAttribute("targetView")).thenReturn("update");
        when(request.getRequestURI()).thenReturn("/test");
        when(servicesProxy.retrievePatientId(anyLong())).thenReturn(patientBean);
        when(servicesProxy.retrieveNotesPatId(anyLong())).thenReturn(notesList);
        when(servicesProxy.getRiskLevel(anyLong())).thenReturn(riskLevelBean);

        String errorBody = "[{\"field\":\"note\",\"defaultMessage\":\"note is mandatory\"}]";
        FeignException feignException = createFeignException(400, errorBody);

        // Act
        ModelAndView mav = feignExceptionHandler.handleFeignException(feignException, request);

        // Assert
        assertNotNull(mav.getModel().get("patient"));
        assertNotNull(mav.getModel().get("notes"));
        assertNotNull(mav.getModel().get("newNote"));
        assertNotNull(mav.getModel().get("riskLevel"));
        assertNotNull(mav.getModel().get("currentPage"));
        assertEquals("Undefined", mav.getModel().get("riskLevel"));
    }

    @Test
    void handleFeignException_ShouldPreservePatientDataFromRequest() {
        // Arrange
        patientBean.setFirstname("Jane");
        patientBean.setLastname("Smith");

        when(request.getAttribute("patient")).thenReturn(patientBean);
        when(request.getAttribute("targetView")).thenReturn("update");
        when(request.getRequestURI()).thenReturn("/test");
        when(servicesProxy.retrievePatientId(anyLong())).thenReturn(patientBean);
        when(servicesProxy.retrieveNotesPatId(anyLong())).thenReturn(notesList);
        when(servicesProxy.getRiskLevel(anyLong())).thenReturn(riskLevelBean);

        String errorBody = "[{\"field\":\"note\",\"defaultMessage\":\"note is mandatory\"}]";
        FeignException feignException = createFeignException(400, errorBody);

        // Act
        ModelAndView mav = feignExceptionHandler.handleFeignException(feignException, request);

        // Assert
        PatientBean resultPatient = (PatientBean) mav.getModel().get("patient");
        assertEquals("Jane", resultPatient.getFirstname());
        assertEquals("Smith", resultPatient.getLastname());
    }
}