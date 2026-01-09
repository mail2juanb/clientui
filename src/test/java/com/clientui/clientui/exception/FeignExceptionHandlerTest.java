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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

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
     * Crée une instance de FeignException avec un statut et un body donnés
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

    // Règle métier : Si aucun patient n'est présent dans la requête, rediriger vers /home
    @Test
    void handleFeignException_WhenNoPatientInRequest_ShouldRedirectToHome() {
        // Arrange
        when(request.getAttribute("patient")).thenReturn(null);
        FeignException feignException = createFeignException(400, "[]");
        //when(request.getRequestURI()).thenReturn("/test");

        // Act
        ModelAndView mav = feignExceptionHandler.handleFeignException(feignException, request);

        // Assert
        assertEquals("redirect:/home", mav.getViewName());
        assertEquals("No patient ID found in query.", mav.getModel().get("error"));
    }

    // Règle métier : Si le patient n'a pas d'ID, utiliser la vue par défaut (update)
    @Test
    void handleFeignException_WhenPatientHasNoId_ShouldUseDefaultView() {
        // Arrange
        patientBean.setId(null);
        when(request.getAttribute("patient")).thenReturn(patientBean);
        when(request.getAttribute("targetView")).thenReturn(null);
        //when(request.getRequestURI()).thenReturn("/test");
        FeignException feignException = createFeignException(400, "[]");

        // Act
        ModelAndView mav = feignExceptionHandler.handleFeignException(feignException, request);

        // Assert
        assertEquals("update", mav.getViewName());
        assertNotNull(mav.getModel().get("patient"));
        assertNotNull(mav.getModel().get("notes"));
        assertNotNull(mav.getModel().get("newNote"));
        assertEquals("Undefined", mav.getModel().get("riskLevel"));
    }

    // Règle métier : Pour les erreurs 400, ajouter les erreurs de validation ou rediriger vers /home si le corps est invalide
    @ParameterizedTest
    @MethodSource("provideErrorBodiesFor400")
    void handleFeignException_WhenStatus400_ShouldHandleVariousBodies(String errorBody, String expectedView, boolean shouldHaveErrors) {
        // Arrange
        when(request.getAttribute("patient")).thenReturn(patientBean);
        when(request.getAttribute("targetView")).thenReturn("update");
//        when(request.getRequestURI()).thenReturn("/test");
        when(servicesProxy.retrievePatientId(anyLong())).thenReturn(patientBean);
        when(servicesProxy.retrieveNotesPatId(anyLong())).thenReturn(notesList);
        when(servicesProxy.getRiskLevel(anyLong())).thenReturn(riskLevelBean);

        FeignException feignException = createFeignException(400, errorBody);

        // Act
        ModelAndView mav = feignExceptionHandler.handleFeignException(feignException, request);

        // Assert
        assertEquals(expectedView, mav.getViewName());
        if (shouldHaveErrors) {
            @SuppressWarnings("unchecked")
            Map<String, String> errors = (Map<String, String>) mav.getModel().get("errors");
            assertNotNull(errors);
        } else {
            assertTrue(mav.getModel().containsKey("error"));
        }
    }

    private static Stream<Arguments> provideErrorBodiesFor400() {
        return Stream.of(
                Arguments.of("[{\"field\":\"name\",\"defaultMessage\":\"Name is required\"}]", "update", true),
                Arguments.of("invalid json", "redirect:/home", false),
                Arguments.of("", "redirect:/home", false)
        );
    }

    // Règle métier : Pour les erreurs 409, ajouter un message d'erreur ou utiliser un message par défaut
    @Test
    void handleFeignException_WhenStatus409_ShouldAddErrorMessage() {
        // Arrange
        when(request.getAttribute("patient")).thenReturn(patientBean);
        when(request.getAttribute("targetView")).thenReturn("update");
//        when(request.getRequestURI()).thenReturn("/test");

        String errorBody = "{\"error\":\"Duplicate entry\"}";
        FeignException feignException = createFeignException(409, errorBody);

        when(servicesProxy.retrievePatientId(anyLong())).thenReturn(patientBean);
        when(servicesProxy.retrieveNotesPatId(anyLong())).thenReturn(notesList);
        when(servicesProxy.getRiskLevel(anyLong())).thenReturn(riskLevelBean);

        // Act
        ModelAndView mav = feignExceptionHandler.handleFeignException(feignException, request);

        // Assert
        assertEquals("update", mav.getViewName());
        assertEquals("Duplicate entry", mav.getModel().get("error"));
    }

    // Règle métier : Pour les erreurs 5xx ou non spécifiées, rediriger vers /home
    @ParameterizedTest
    @MethodSource("provideUnhandledStatusCodes")
    void handleFeignException_WhenUnhandledStatus_ShouldRedirectToHome(int status, String body) {
        // Arrange
        //when(request.getRequestURI()).thenReturn("/test");

        FeignException feignException = createFeignException(status, body);

        // Act
        ModelAndView mav = feignExceptionHandler.handleFeignException(feignException, request);

        // Assert
        assertEquals("/home", mav.getViewName());
        assertTrue(mav.getModel().containsKey("error"));
    }

    private static Stream<Arguments> provideUnhandledStatusCodes() {
        return Stream.of(
                Arguments.of(500, "Internal Server Error"),
                Arguments.of(503, "Service Unavailable"));
    }

    // Règle métier : Si une vue personnalisée est spécifiée, l'utiliser
    @Test
    void handleFeignException_WithCustomTargetView_ShouldUseSpecifiedView() {
        // Arrange
        when(request.getAttribute("patient")).thenReturn(patientBean);
        when(request.getAttribute("targetView")).thenReturn("custom-view");
        //when(request.getRequestURI()).thenReturn("/test");

        String errorBody = "[{\"field\":\"email\",\"defaultMessage\":\"Invalid email\"}]";
        FeignException feignException = createFeignException(400, errorBody);

        when(servicesProxy.retrievePatientId(anyLong())).thenReturn(patientBean);
        when(servicesProxy.retrieveNotesPatId(anyLong())).thenReturn(notesList);
        when(servicesProxy.getRiskLevel(anyLong())).thenReturn(riskLevelBean);

        // Act
        ModelAndView mav = feignExceptionHandler.handleFeignException(feignException, request);

        // Assert
        assertEquals("custom-view", mav.getViewName());
        assertEquals("custom-view", mav.getModel().get("currentPage"));
    }

    // Règle métier : Si une exception est levée lors de la récupération des données, rediriger vers /patients
    @Test
    void handleFeignException_WhenFeignExceptionOnDataFetch_ShouldRedirectToPatients() {
        // Arrange
        when(request.getAttribute("patient")).thenReturn(patientBean);
//        when(request.getRequestURI()).thenReturn("/test");

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

    //2. Test pour la gestion d'un code HTTP non géré (redirection vers /home)
    @Test
    void handleFeignException_WhenUnhandledHttpStatus_ShouldRedirectToHome() {
        // Arrange
        when(request.getAttribute("patient")).thenReturn(patientBean);
        //when(request.getRequestURI()).thenReturn("/test");
        when(servicesProxy.getRiskLevel(anyLong())).thenReturn(riskLevelBean);

        FeignException feignException = createFeignException(401, "Unauthorized");

        // Act
        ModelAndView mav = feignExceptionHandler.handleFeignException(feignException, request);

        // Assert
        assertEquals("redirect:/home", mav.getViewName());
        assertTrue(mav.getModel().containsKey("error"));
        assertTrue(((String) mav.getModel().get("error")).startsWith("Error 401 :"));
    }

    @Test
    void handleFeignException_WhenFeignExceptionOnDataFetchWithStatus503_ShouldRedirectToHome() {
        // Arrange
        when(request.getAttribute("patient")).thenReturn(patientBean);
        //when(request.getRequestURI()).thenReturn("/test");

        // Simule une FeignException avec un statut 503 lors de la récupération des données du patient
        FeignException innerException = createFeignException(503, "Service Unavailable");
        when(servicesProxy.retrievePatientId(anyLong())).thenThrow(innerException);

        // Crée une FeignException initiale (par exemple, 400)
        FeignException feignException = createFeignException(400, "[]");

        // Act
        ModelAndView mav = feignExceptionHandler.handleFeignException(feignException, request);

        // Assert
        assertEquals("/home", mav.getViewName());
        assertTrue(mav.getModel().containsKey("error"));
        assertEquals("A required service is currently unavailable. Please try again later.", mav.getModel().get("error"));
    }

    @Test
    void handleFeignException_WhenStatus409AndInvalidBody_ShouldRedirectToHome() {
        // Arrange
        when(request.getAttribute("patient")).thenReturn(patientBean);
        //when(request.getRequestURI()).thenReturn("/test");

        FeignException feignException = createFeignException(409, "invalid json");

        when(servicesProxy.retrievePatientId(anyLong())).thenReturn(patientBean);
        when(servicesProxy.retrieveNotesPatId(anyLong())).thenReturn(notesList);
        when(servicesProxy.getRiskLevel(anyLong())).thenReturn(riskLevelBean);

        // Act
        ModelAndView mav = feignExceptionHandler.handleFeignException(feignException, request);

        // Assert
        assertEquals("redirect:/home", mav.getViewName());
        assertTrue(mav.getModel().containsKey("error"));
    }

    // Gestion des erreurs 503 (Service Unavailable)
    // Vérifier que la méthode handleServiceUnavailable retourne bien la vue /home avec le bon message d’erreur.
    @Test
    void handleServiceUnavailable_WhenStatus503_ShouldReturnHomeWithError() {
        // Arrange
        FeignException feignException = createFeignException(503, "Service Unavailable");
//        when(request.getRequestURI()).thenReturn("/test");

        // Act
        ModelAndView mav = feignExceptionHandler.handleFeignException(feignException, request);

        // Assert
        assertEquals("/home", mav.getViewName());
        assertEquals("A required service is currently unavailable. Please try again later.", mav.getModel().get("error"));
    }

    @ParameterizedTest
    @MethodSource("provide5xxStatusCodes")
    void handleFeignException_WhenFeignExceptionOnDataFetchWithStatus5xx_ShouldRedirectToHome(int status, String expectedMessage) {
        // Arrange
        when(request.getAttribute("patient")).thenReturn(patientBean);
        //when(request.getRequestURI()).thenReturn("/test");

        // Simule une FeignException avec un statut 5xx lors de la récupération des données du patient
        FeignException innerException = createFeignException(status, "Error");
        when(servicesProxy.retrievePatientId(anyLong())).thenThrow(innerException);

        // Crée une FeignException initiale (par exemple, 400)
        FeignException feignException = createFeignException(400, "[]");

        // Act
        ModelAndView mav = feignExceptionHandler.handleFeignException(feignException, request);

        // Assert
        assertEquals("/home", mav.getViewName());
        assertTrue(mav.getModel().containsKey("error"));
        assertEquals(expectedMessage, mav.getModel().get("error"));
    }

    private static Stream<Arguments> provide5xxStatusCodes() {
        return Stream.of(
                Arguments.of(503, "A required service is currently unavailable. Please try again later."),
                Arguments.of(500, "A service encountered an internal error (status 500). Please try again later.")
        );
    }


}
