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
     * Handles exceptions of type {@link FeignException} thrown during Feign client calls in the context of patient management.
     * This method analyzes the HTTP status code and error content to provide user-friendly feedback,
     * such as validation errors, conflicts, or service unavailability. It also ensures the UI remains consistent
     * by retrieving the latest patient data, notes, and risk level when possible.
     *
     * <p><strong>Processing Steps:</strong></p>
     * <ol>
     *   <li><strong>Critical Errors (500, 503):</strong> Redirects to a service unavailable handler.</li>
     *   <li><strong>Patient List Requests (/patients):</strong> Returns a view with an empty patient list and an error message.</li>
     *   <li><strong>Patient-Specific Requests (/update):</strong> Retrieves the patient ID from the request attributes,
     *       fetches the latest patient data, notes, and risk level, and processes the error response.</li>
     *   <li><strong>Validation Errors (400):</strong> Parses the error response to extract field-specific validation messages
     *       and returns them to the update view.</li>
     *   <li><strong>Conflict Errors (409):</strong> Extracts the conflict message and returns it to the appropriate view.</li>
     *   <li><strong>Other Errors:</strong> Redirects to the home page with a generic error message.</li>
     * </ol>
     *
     * <p><strong>Error Handling:</strong></p>
     * <ul>
     *   <li>Logs critical errors and redirects to a dedicated error handler for service unavailability.</li>
     *   <li>For validation errors, maps field-specific messages to the UI for user correction.</li>
     *   <li>For conflicts, extracts and displays the error message from the response body.</li>
     *   <li>For all other errors, redirects to the home page with a generic error message.</li>
     * </ul>
     *
     * @param e       The {@link FeignException} thrown by the Feign client, containing the HTTP status and error details.
     * @param request The current {@link HttpServletRequest}, used to retrieve the patient context, target view,
     *                and request attributes (e.g., patient ID, new note).
     * @return A {@link ModelAndView} object containing:
     *         <ul>
     *           <li>The patient data, notes, and risk level (if available).</li>
     *           <li>Field-specific validation errors (for 400 responses).</li>
     *           <li>Conflict or generic error messages (for 409 or other responses).</li>
     *           <li>A redirect to the home page for critical or unhandled errors.</li>
     *         </ul>
     * @see #handleServiceUnavailable(FeignException, HttpServletRequest)
     * @see ValidationErrorDTO
     */
    @ExceptionHandler(FeignException.class)
    public ModelAndView handleFeignException(FeignException e, HttpServletRequest request) {

        // Handle critical errors first (503, 500, etc.)
        if (e.status() == 503 || e.status() >= 500) {
            return handleServiceUnavailable(e, request);
        }

        // Case 2: Request for patient list (/patients)
        if (request.getRequestURI().contains("/patients") && !request.getRequestURI().contains("/update/")) {
            ModelAndView mav = new ModelAndView("list"); // Utilise le template existant
            mav.addObject("currentPage", "patients");
            mav.addObject("error", "Erreur lors de la récupération des patients : " + e.getMessage());
            mav.addObject("patients", new ArrayList<PatientBean>()); // Liste vide pour éviter les NullPointerException
            return mav;
        }

        // Case 3: Patient-specific queries (update, add) - Retrieves the patient ID from the query
        final PatientBean requestPatient = (PatientBean) request.getAttribute("patient");
        if (requestPatient == null) {
            logger.error("No patient ID found in query.");
            return new ModelAndView("home").addObject("error", "No patient ID found in query.");
        }

        Long patientId = null;
        if (requestPatient.getId() != null) {
            patientId = requestPatient.getId();
        }

        // Initialises default objects
        PatientBean patient = requestPatient;
        List<NoteBean> notes = new ArrayList<>();
        RiskLevelBean riskLevel = new RiskLevelBean();
        riskLevel.setRiskLevel("Undefined");
        NoteBean newNote = new NoteBean();
        newNote.setPatId(patientId);

        // If we have a patient ID, we retrieve the data.
        if (patientId != null) {
            try {
                patient = servicesProxy.retrievePatientId(patientId);
                notes = servicesProxy.retrieveNotesPatId(patientId);
                riskLevel = servicesProxy.getRiskLevel(patientId);
            } catch (FeignException ex) {
                logger.debug("Feign error when retrieving data for the patient {} : {}", patientId, ex.contentUTF8());
                // If it is a service unavailable error, redirect to home
                if (ex.status() == 503 || ex.status() >= 500) {
                    return handleServiceUnavailable(ex, request);
                }
                // Otherwise, redirect to the patient list.
                ModelAndView mav = new ModelAndView("redirect:/patients");
                mav.addObject("error", "Error retrieving patient data : " + ex.getMessage());
                return mav;
            }
        }

        // Determines the view from the query
        String viewName = (String) request.getAttribute("targetView");
        if (viewName == null) {
            viewName = "update";
        }

        // ModelAndView creation
        ModelAndView mav = new ModelAndView(viewName);
        mav.addObject("patient", patient);
        mav.addObject("notes", notes);
        mav.addObject("newNote", newNote);
        mav.addObject("riskLevel", riskLevel.getRiskLevel());
        mav.addObject("currentPage", viewName);

        // Processes the body of the Feign response
        final String body = e.contentUTF8();

        try {
            // 400 - Validation errors
            if (e.status() == 400) {
                List<ValidationErrorDTO> errors = objectMapper.readValue(
                        body, new TypeReference<List<ValidationErrorDTO>>() {});

                Map<String, String> errorMap = new HashMap<>();
                for (ValidationErrorDTO err : errors) {
                    errorMap.put(err.getField(), err.getDefaultMessage());
                }

                // We set the newNote with the values entered by the user.
                NoteBean noteFromRequest = (NoteBean) request.getAttribute("newNote");
                if (noteFromRequest != null) {
                    mav.addObject("newNote", noteFromRequest);
                }

                mav.addObject("errors", errorMap);
                mav.setViewName("update");
            }
            // 409 - Conflit / duplicate
            else if (e.status() == 409) {
                try {
                    Map<String, String> map = objectMapper.readValue(body, Map.class);
                    mav.addObject("error", map.getOrDefault("error", "Conflict detected."));
                } catch (Exception ex) {
                    return new ModelAndView("home")
                            .addObject("currentPage", "home")
                            .addObject("error", "Conflict detected (unexpected format): " + ex.getMessage());
                }
            }
            // Other HTTP codes
            else {
                return new ModelAndView("home")
                        .addObject("currentPage", "home")
                        .addObject("error", "Error " + e.status() + " : " + e.getMessage());
            }
        } catch (Exception ex) {
            return new ModelAndView("home")
                    .addObject("currentPage", "home")
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

        ModelAndView mav = new ModelAndView("home");
        mav.addObject("currentPage", "home");
        mav.addObject("error", errorMessage);
        return mav;
    }

}
