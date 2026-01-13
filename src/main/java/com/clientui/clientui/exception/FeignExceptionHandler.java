package com.clientui.clientui.exception;

import com.clientui.clientui.beans.NoteBean;
import com.clientui.clientui.beans.PatientBean;
import com.clientui.clientui.beans.RiskLevelBean;
import com.clientui.clientui.dto.ValidationErrorDTO;
import com.clientui.clientui.proxies.MicroservicesProxy;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Global exception handler for {@link FeignException} in the ClientUI application.
 * This class is responsible for intercepting and processing exceptions thrown by Feign clients,
 * providing appropriate error handling and user feedback for HTTP errors (e.g., 400, 409, 500, 503).
 * It also manages the retrieval of patient data, notes, and risk levels in case of errors,
 * and redirects users to relevant views with error messages.
 *
 * <p>This handler is annotated with {@link ControllerAdvice}, making it applicable to all controllers in the application.
 * It specifically addresses:</p>
 * <ul>
 *   <li>HTTP 400 (Bad Request): Validation errors (e.g., invalid patient data).</li>
 *   <li>HTTP 409 (Conflict): Duplicate entries or conflicts.</li>
 *   <li>HTTP 500/503 (Server Errors): Service unavailability or internal errors.</li>
 * </ul>
 *
 * <p>In case of errors, it retrieves the latest available patient data, notes, and risk level (if possible)
 * and redirects the user to the appropriate view with contextual error messages.</p>
 *
 * @see FeignException
 * @see ControllerAdvice
 * @see ModelAndView
 * @see PatientBean
 * @see NoteBean
 * @see RiskLevelBean
 * @see MicroservicesProxy
 */
@ControllerAdvice
public class FeignExceptionHandler {

    /** Logger for this class. */
    private static final Logger logger = LoggerFactory.getLogger(FeignExceptionHandler.class);

    /** Jackson ObjectMapper for JSON processing. */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Proxy for communicating with backend microservices. */
    @Autowired
    MicroservicesProxy servicesProxy;

    /**
     * Handles exceptions of type {@link FeignException} thrown during Feign client calls.
     * This method analyzes the HTTP status code and error content to provide appropriate feedback
     * to the user, such as validation errors, conflicts, or service unavailability.
     *
     * <p>Steps:</p>
     * <ol>
     *   <li>Logs the error and checks for critical service errors (500, 503).</li>
     *   <li>Retrieves the patient ID from the request attributes.</li>
     *   <li>Fetches the latest patient data, notes, and risk level (if possible).</li>
     *   <li>Processes the Feign error response (e.g., validation errors, conflicts).</li>
     *   <li>Redirects the user to the appropriate view with error details.</li>
     * </ol>
     *
     * @param e       The {@link FeignException} thrown by the Feign client.
     * @param request The current {@link HttpServletRequest}, used to retrieve patient context and target view.
     * @return A {@link ModelAndView} object containing the error details and patient data,
     *         or a redirect to the home/patients page in case of critical errors.
     */
    @ExceptionHandler(FeignException.class)
    public ModelAndView handleFeignException(FeignException e, HttpServletRequest request) {
        //logger.warn("FeignException intercepted : status={}, URI={}", e.status(), request.getRequestURI());

        // Traiter d'abord les erreurs critiques (503, 500, etc.)
        if (e.status() == 503 || e.status() >= 500) {
            return handleServiceUnavailable(e, request);
        }

        // Cas 2: Requête pour la liste des patients (/patients)
        if (request.getRequestURI().contains("/patients") && !request.getRequestURI().contains("/update/")) {
            ModelAndView mav = new ModelAndView("list"); // Utilise le template existant
            mav.addObject("currentPage", "patients");
            mav.addObject("error", "Erreur lors de la récupération des patients : " + e.getMessage());
            mav.addObject("patients", new ArrayList<PatientBean>()); // Liste vide pour éviter les NullPointerException
            return mav;
        }

        // Cas 3: Requêtes spécifiques à un patient (update, add) - Récupère l'ID du patient depuis la requête
        final PatientBean requestPatient = (PatientBean) request.getAttribute("patient");
        if (requestPatient == null) {
            logger.error("No patient ID found in query.");
            return new ModelAndView("redirect:/home").addObject("error", "No patient ID found in query.");
        }

        Long patientId = null;
        if (requestPatient.getId() != null) {
            patientId = requestPatient.getId();
        }

        // Initialise les objets par défaut
        PatientBean patient = requestPatient;
        List<NoteBean> notes = new ArrayList<>();
        RiskLevelBean riskLevel = new RiskLevelBean();
        riskLevel.setRiskLevel("Undefined");
        NoteBean newNote = new NoteBean();
        newNote.setPatId(patientId);

        // Si on a un patientId, récupère les données
        if (patientId != null) {
            try {
                patient = servicesProxy.retrievePatientId(patientId);
                notes = servicesProxy.retrieveNotesPatId(patientId);
                riskLevel = servicesProxy.getRiskLevel(patientId);
            } catch (FeignException ex) {
                logger.debug("Feign error when retrieving data for the patient {} : {}", patientId, ex.contentUTF8());
                // Si c'est une erreur de service indisponible, rediriger vers home
                if (ex.status() == 503 || ex.status() >= 500) {
                    return handleServiceUnavailable(ex, request);
                }
                // Sinon, redirige vers la liste des patients
                ModelAndView mav = new ModelAndView("redirect:/patients");
                mav.addObject("error", "Error retrieving patient data : " + ex.getMessage());
                return mav;
            }
        }

        // Détermine la vue depuis la requete
        String viewName = (String) request.getAttribute("targetView");
        if (viewName == null) {
            viewName = "update";
        }

        // Création du ModelAndView
        ModelAndView mav = new ModelAndView(viewName);
        mav.addObject("patient", patient);
        mav.addObject("notes", notes);
        mav.addObject("newNote", newNote);
        mav.addObject("riskLevel", riskLevel.getRiskLevel());
        mav.addObject("currentPage", viewName);

        // Traite le corps de la réponse Feign
        final String body = e.contentUTF8();

        try {
            // 400 - Erreurs de validation
            if (e.status() == 400) {
                List<ValidationErrorDTO> errors = objectMapper.readValue(
                        body, new TypeReference<List<ValidationErrorDTO>>() {});

                Map<String, String> errorMap = new HashMap<>();
                for (ValidationErrorDTO err : errors) {
                    errorMap.put(err.getField(), err.getDefaultMessage());
                }
                mav.addObject("errors", errorMap);
            }
            // 409 - Conflit / doublon
            else if (e.status() == 409) {
                try {
                    Map<String, String> map = objectMapper.readValue(body, Map.class);
                    mav.addObject("error", map.getOrDefault("error", "Conflict detected."));
                } catch (Exception ex) {
                    return new ModelAndView("redirect:/home")
                            .addObject("error", "Conflict detected (unexpected format): " + ex.getMessage());
                }
            }
            // Autre code HTTP
            else {
                return new ModelAndView("redirect:/home")
                        .addObject("error", "Error " + e.status() + " : " + e.getMessage());
            }
        } catch (Exception ex) {
            return new ModelAndView("redirect:/home")
                    .addObject("error", "Error processing response status : " + ex.getMessage());
        }

        return mav;
    }

    /**
     * Handles service unavailability errors (HTTP 503, 500, etc.).
     * Logs the error and redirects the user to the home page with a user-friendly message.
     *
     * @param e       The {@link FeignException} indicating the service error.
     * @param request The current {@link HttpServletRequest}.
     * @return A {@link ModelAndView} redirecting to the home page with an error message.
     */
    private ModelAndView handleServiceUnavailable(FeignException e, HttpServletRequest request) {
        logger.error("A service is unavailable or encountered an error. Status: {}", e.status());

        String errorMessage;
        if (e.status() == 503) {
            errorMessage = "A required service is currently unavailable. Please try again later.";
        } else {
            errorMessage = "A service encountered an internal error (status " + e.status() + "). Please try again later.";
        }

        ModelAndView mav = new ModelAndView("/home");
        mav.addObject("currentPage", "home");
        mav.addObject("error", errorMessage);
        return mav;
    }

}
