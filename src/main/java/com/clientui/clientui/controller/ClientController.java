package com.clientui.clientui.controller;

import com.clientui.clientui.beans.NoteBean;
import com.clientui.clientui.beans.PatientBean;
import com.clientui.clientui.beans.RiskLevelBean;
import com.clientui.clientui.proxies.MicroservicesProxy;
import com.clientui.clientui.tracing.TracingHelper;
import feign.FeignException;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    private final MicroservicesProxy servicesProxy;
    private final TracingHelper tracing;

    /**
     * Constructs a new ClientController with the specified MicroservicesProxy.
     *
     * @param servicesProxy The proxy used to communicate with backend microservices.
     */
    public ClientController(MicroservicesProxy servicesProxy, TracingHelper tracing) {
        this.servicesProxy = servicesProxy;
        this.tracing = tracing;
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

        tracing.tag("page", "home");
        tracing.tag("user.name", username);
        tracing.tag("user.roles", roles);
        tracing.event("Rendering home page");

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

        tracing.tag("page", "patients-list");
        tracing.event("Retrieving patient list");

        if (error != null) {
            tracing.error("UIError", error);
            model.addAttribute("error", error);
        }

        model.addAttribute("currentPage", "patients");
        List<PatientBean> patients = servicesProxy.retrievePatientList();
        tracing.tag("patient.count", patients.size());
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

        tracing.tag("page", "update");
        tracing.tag("patient.id", id);
        tracing.event("Displaying update form");

        model.addAttribute("currentPage", "update");

        final PatientBean patient = servicesProxy.retrievePatientId(id);
        model.addAttribute("patient", patient);

        NoteBean newNote = new NoteBean();
        newNote.setPatId(id);
        newNote.setPatient(patient.getLastname());
        model.addAttribute("newNote", newNote);

        List<NoteBean> notes = servicesProxy.retrieveNotesPatId(id);
        if (notes == null) {
            notes = new ArrayList<>(); // Liste vide par défaut
            logger.warn("No notes found for patient ID: {}. Empty list initialised.", id);
        }
        model.addAttribute("notes", notes);

        RiskLevelBean riskLevel = servicesProxy.getRiskLevel(id);
        model.addAttribute("riskLevel", riskLevel.getRiskLevel());

        return "update";
    }

    /**
     * Adds a new note for a specific patient and updates the view accordingly.
     *
     * <p>This method retrieves the patient by ID, associates the new note with the patient,
     * and persists the note via the services proxy. It also sets the patient and note as
     * request attributes for further processing in the view layer.
     *
     * @param id                  The unique identifier of the patient.
     * @param newNote             The note to be added, wrapped in a {@link NoteBean}.
     * @param model               The model to which attributes can be added (not used in this method).
     * @param request             The HTTP request, used to set attributes for the view.
     * @param redirectAttributes  Attributes for redirecting with flash messages (success/error).
     * @return A redirect to the patient update page, including the patient ID in the path.
     * @throws FeignException     If the patient is not found or the note cannot be added.
     */
@PostMapping("/update/{id}/addnotes")
@NewSpan("clientui-patient-add-note")
public String addNote(
        @PathVariable("id") Long id,
        @ModelAttribute("newNote") NoteBean newNote,
        Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {

    tracing.tag("patient.id", id);
    tracing.event("Adding note");

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

//    // Passe le patient et la nouvelle note en attribut de la requête
    request.setAttribute("patient", patient);
    request.setAttribute("newNote", newNote);
    request.setAttribute("targetView", "update");

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

        tracing.tag("page", "add-patient-form");
        tracing.event("Displaying the form for adding a patient");

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

        tracing.tag("patient.lastname", patient.getLastname());
        tracing.event("Submitting patient addition form");

        request.setAttribute("patient", patient);

        request.setAttribute("targetView", "add");

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

        tracing.tag("patient.lastname", patient.getLastname());
        tracing.event("Submitting patient addition form");

        patient.setId(id);
        request.setAttribute("patient", patient);

        request.setAttribute("targetView", "update");

        servicesProxy.updatePatient(id, patient);
        redirectAttributes.addFlashAttribute("success", "Patient successfully updated");

        return "redirect:/update/" + id;
    }

}
