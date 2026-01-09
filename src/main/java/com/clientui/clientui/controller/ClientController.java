package com.clientui.clientui.controller;

import com.clientui.clientui.beans.NoteBean;
import com.clientui.clientui.beans.PatientBean;
import com.clientui.clientui.beans.RiskLevelBean;
import com.clientui.clientui.proxies.MicroservicesProxy;
import feign.FeignException;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller class for handling client-side requests in the MicroDiab application.
 * This class manages the interaction between the frontend (Thymeleaf templates) and the backend microservices.
 * It provides endpoints for displaying patient lists, updating patient information, adding new patients,
 * and managing patient notes. It also integrates with Spring Security for user authentication and role management.
 *
 * <p>This controller uses Feign clients to communicate with other microservices (e.g., mPatient, mNotes, mRisk)
 * and leverages Spring's tracing capabilities for monitoring and debugging purposes.
 *
 * @see com.clientui.clientui.beans.PatientBean
 * @see com.clientui.clientui.beans.NoteBean
 * @see com.clientui.clientui.beans.RiskLevelBean
 * @see com.clientui.clientui.proxies.MicroservicesProxy
 */
@Controller
public class ClientController {

    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);

    @Autowired(required = false)
    private Tracer tracer;

    private final MicroservicesProxy servicesProxy;

    /**
     * Constructs a new ClientController with the specified MicroservicesProxy.
     *
     * @param servicesProxy The proxy used to communicate with backend microservices.
     */
    public ClientController(MicroservicesProxy servicesProxy) {
        this.servicesProxy = servicesProxy;
    }

    /**
     * Adds user information (username and roles) to the model for every request.
     * This method is automatically invoked by Spring MVC before any handler method is called.
     *
     * @param username The username extracted from the request header.
     * @param roles    The roles extracted from the request header.
     * @param model    The model to which user information is added.
     */
    @ModelAttribute
    public void addUserInfoToModel(
            @RequestHeader(value = "X-Auth-Username", required = false, defaultValue = "None_Username") String username,
            @RequestHeader(value = "X-Auth-Roles", required = false, defaultValue = "None_Role") String roles,
            Model model) {
        model.addAttribute("userConnected", username);
        model.addAttribute("userRole", roles);
    }

    /**
     * Displays the home page of the application.
     *
     * @param username The username extracted from the request header.
     * @param roles    The roles extracted from the request header.
     * @param model    The model to which attributes are added.
     * @return The name of the Thymeleaf template for the home page.
     */
    @RequestMapping("/home")
    @NewSpan("clientui-home-display")
    public String showHomes(
            @RequestHeader(value = "X-Auth-Username", required = false, defaultValue = "None_Username") String username,
            @RequestHeader(value = "X-Auth-Roles", required = false, defaultValue = "None_Username") String roles,
            Model model) {

        // Récupère le span courant (créé automatiquement par @NewSpan)
        //Span currentSpan = tracer.currentSpan();
        Span currentSpan = currentSpanOrNull();
        if (currentSpan != null) {
            // Ajoute des tags personnalisés
            currentSpan.tag("user.name", username);
            currentSpan.tag("user.roles", roles);
            currentSpan.tag("page", "home");
            currentSpan.event("Rendering of the home page for the user: " + username + " - Role : " + roles);
        } else {
            logger.warn("Home page rendering - No current span found for the showHomes method.");
        }

            // Logique métier
            model.addAttribute("currentPage", "home");
            model.addAttribute("userConnected", username);
            model.addAttribute("userRole", roles);

            return "home";
    }

    /**
     * Displays the list of patients.
     *
     * @param model The model to which attributes are added.
     * @param error An optional error message to display.
     * @return The name of the Thymeleaf template for the patient list page.
     */
    @RequestMapping("/patients")
    @NewSpan("clientui-patients-list")
    public String showPatients(Model model, @RequestParam(required = false) String error) {
        //Span currentSpan = tracer.currentSpan();
        Span currentSpan = currentSpanOrNull();
        if (currentSpan != null) {
            currentSpan.tag("page", "patients-list");
            currentSpan.event("Retrieving the patient list");
        } else {
            logger.warn("Patient list page rendering - No current span found for the showPatients method.");
        }

        // Affichage des erreurs générales du handler lors des add ou update patient
        if (error != null) {
            model.addAttribute("error", error);
        }

        // Logique métier
        model.addAttribute("currentPage", "patients");
        List<PatientBean> patients = servicesProxy.retrievePatientList();
        model.addAttribute("patients", patients);

        return "list";
    }

    /**
     * Displays the form for updating a patient's information.
     *
     * @param id    The ID of the patient to update.
     * @param model The model to which attributes are added.
     * @return The name of the Thymeleaf template for the update form.
     */
    @RequestMapping("/update/{id}")
    @NewSpan("clientui-patient-update-form")
    public String showUpdateForm(@PathVariable("id") Long id, Model model) {
        Span currentSpan = currentSpanOrNull();
        if (currentSpan != null) {
            currentSpan.tag("patient.id", String.valueOf(id));
            currentSpan.event("Affichage du formulaire de mise à jour pour le patient ID : " + id);
        } else {
            logger.warn("Page rendering MAJ Patient id = {} - No current span found for the showUpdateForm method.", id);
        }

        // Logique métier
        model.addAttribute("currentPage", "update");

        // Récupération du patient
        final PatientBean patient = servicesProxy.retrievePatientId(id);
        model.addAttribute("patient", patient);

        // Ajouter un objet newNote pour le formulaire
        NoteBean newNote = new NoteBean();
        newNote.setPatId(id);
        newNote.setPatient(patient.getLastname());
        model.addAttribute("newNote", newNote);

        // Récupération des notes avec gestion du cas null
        List<NoteBean> notes = servicesProxy.retrieveNotesPatId(id);
        if (notes == null) {
            notes = new ArrayList<>(); // Liste vide par défaut
            logger.warn("No notes found for patient ID: {}. Empty list initialised.", id);
        }
        model.addAttribute("notes", notes);

        // Récupération du RiskLevel depuis mRisk
        RiskLevelBean riskLevel = servicesProxy.getRiskLevel(id);
        model.addAttribute("riskLevel", riskLevel.getRiskLevel());

        return "update";
    }

    /**
     * Adds a note for a specific patient.
     *
     * @param id                  The ID of the patient.
     * @param newNote             The note to add.
     * @param result              The binding result for validation.
     * @param model               The model to which attributes are added.
     * @param redirectAttributes  Attributes for redirecting with flash messages.
     * @return A redirect to the patient update page.
     */
@PostMapping("/update/{id}/addnotes")
@NewSpan("clientui-patient-add-note")
public String addNote(
        @PathVariable("id") Long id,
        @Valid @ModelAttribute("newNote") NoteBean newNote,
        BindingResult result,
        Model model,
        RedirectAttributes redirectAttributes) {

    Span currentSpan = currentSpanOrNull();
    if (currentSpan != null) {
        currentSpan.tag("patient.id", String.valueOf(id));
        currentSpan.event("Adding a note for patient ID: " + id);
    } else {
        logger.warn("Adding a note for patient ID: {} - No current span found.", id);
    }

    PatientBean patient;
    try {
        patient = servicesProxy.retrievePatientId(id);
        newNote.setPatId(id);
        newNote.setPatient(patient.getLastname());
    } catch (FeignException.NotFound ex) {
        logger.warn("Patient not found with ID {}", id);
        redirectAttributes.addFlashAttribute("error", "Patient not found.");
        return "redirect:/patients";
    }

    // Gestion des erreurs de validation
    if (result.hasErrors()) {
        result.getAllErrors().forEach(error -> logger.warn("Validation error: {}", error.getDefaultMessage()));

        List<NoteBean> notes = servicesProxy.retrieveNotesPatId(id);
        model.addAttribute("patient", patient);
        model.addAttribute("notes", notes);
        return "update";
    }

    // Appeler le microservice mnotes pour sauvegarder la note
    servicesProxy.addNote(newNote);

    redirectAttributes.addFlashAttribute("success", "Note successfully added");
    return "redirect:/update/" + id;
}


    /**
     * Displays the form for adding a new patient.
     *
     * @param model The model to which attributes are added.
     * @return The name of the Thymeleaf template for the add patient form.
     */
    @GetMapping("/add")
    @NewSpan("clientui-patient-add-form")
    public String showAddPatientForm(Model model) {
        Span currentSpan = currentSpanOrNull();
        if (currentSpan != null) {
            currentSpan.tag("page", "add-patient-form");
            currentSpan.event("Displaying the form for adding a patient");
        } else {
            logger.warn("Displaying the patient addition form - No current span found.");
        }

        // Ajouter un nouvel objet PatientBean vide pour le formulaire
        model.addAttribute("patient", new PatientBean());
        model.addAttribute("currentPage", "add");

        return "add"; // Nom du template Thymeleaf pour le formulaire
    }

    /**
     * Processes the submission of the add patient form.
     *
     * @param patient             The patient to add.
     * @param model               The model to which attributes are added.
     * @param request             The HTTP request.
     * @param redirectAttributes  Attributes for redirecting with flash messages.
     * @return A redirect to the patient list page.
     */
    @PostMapping("/add/addPatient")
    @NewSpan("clientui-patient-add-submit")
    public String addPatient(@ModelAttribute("patient") PatientBean patient, Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Span currentSpan = currentSpanOrNull();
        if (currentSpan != null) {
            currentSpan.tag("patient.lastname", patient.getLastname());
            currentSpan.event("Submitting patient addition form");
        } else {
            logger.warn("Submitting patient addition form - No current span found.");
        }

        // NOTE : Pour conserver ce que l'utilisateur a renseigné.
        request.setAttribute("patient", patient);

        // Ajoute le nom de la vue dans les attributs de la requête
        request.setAttribute("targetView", "add");

        /* NOTE :Gestion des erreurs de validation via le FeignExceptionHandler.
        Elles sont levées par le microservice back concerné. */

        servicesProxy.addPatient(patient);
        redirectAttributes.addFlashAttribute("success", "Patient successfully added");
        return "redirect:/patients";
    }

    /**
     * Processes the submission of the update patient form.
     *
     * @param id                  The ID of the patient to update.
     * @param patient             The updated patient information.
     * @param request             The HTTP request.
     * @param redirectAttributes  Attributes for redirecting with flash messages.
     * @return A redirect to the patient update page.
     */
    @PostMapping("/update/{id}/updatepatient")
    @NewSpan("clientui-patient-update-submit")
    public String updatePatient(@PathVariable("id") Long id, @ModelAttribute("patient") PatientBean patient,
                                HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Span currentSpan = currentSpanOrNull();
        if (currentSpan != null) {
            currentSpan.tag("patient.id", String.valueOf(id));
            currentSpan.event("Submission of the Patient ID Update Form: " + id);
        } else {
            logger.warn("Submission of patient update form ID: {} - No current span found.", id);
        }

        // NOTE : Pour conserver ce que l'utilisateur a renseigné.
        patient.setId(id);      // Force l'id du patient depuis le PathVariable
        request.setAttribute("patient", patient);

        // Ajoute le nom de la vue dans les attributs de la requête
        request.setAttribute("targetView", "update");

        /* NOTE :Gestion des erreurs de validation via le FeignExceptionHandler.
        Elles sont levées par le microservice back concerné. */

        // Mise à jour du patient via le microservice
        servicesProxy.updatePatient(id, patient);
        redirectAttributes.addFlashAttribute("success", "Patient successfully updated");

        // Redirection vers la page de mise à jour du patient
        return "redirect:/update/" + id;
    }

    /**
     * Helper method to safely retrieve the current span from the tracer.
     *
     * @return The current span, or null if the tracer is not available.
     */
    private Span currentSpanOrNull() {
        return tracer != null ? tracer.currentSpan() : null;
    }
}
