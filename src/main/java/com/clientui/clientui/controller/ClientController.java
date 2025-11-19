package com.clientui.clientui.controller;

import com.clientui.clientui.beans.NoteBean;
import com.clientui.clientui.beans.PatientBean;
import com.clientui.clientui.proxies.MicroservicesProxy;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ClientController {

    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);

    @Autowired
    private Tracer tracer;

    private final MicroservicesProxy servicesProxy;

    public ClientController(MicroservicesProxy servicesProxy) {
        this.servicesProxy = servicesProxy;
    }

    // Méthode pour ajouter automatiquement userConnected et userRole à chaque modèle
    @ModelAttribute
    public void addUserInfoToModel(
            @RequestHeader(value = "X-Auth-Username", required = false, defaultValue = "PasDeUsername") String username,
            @RequestHeader(value = "X-Auth-Roles", required = false, defaultValue = "PasDeRole") String roles,
            Model model) {
        model.addAttribute("userConnected", username);
        model.addAttribute("userRole", roles);
    }


    @RequestMapping("/home")
    @NewSpan("clientui-home-display")
    public String showHomes(
            @RequestHeader(value = "X-Auth-Username", required = false, defaultValue = "PasDeUsername") String username,
            @RequestHeader(value = "X-Auth-Roles", required = false, defaultValue = "PasDeRole") String roles,
            Model model) {

        // Récupère le span courant (créé automatiquement par @NewSpan)
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            // Ajoute des tags personnalisés
            currentSpan.tag("user.name", username);
            currentSpan.tag("user.roles", roles);
            currentSpan.tag("page", "home");
            currentSpan.event("Rendu de la page home pour l'utilisateur : " + username + " - Role : " + roles);
            logger.info("Rendu de la page home - Span courant : traceId={}, spanId={}",
                    currentSpan.context().traceId(),
                    currentSpan.context().spanId());
        } else {
            logger.warn("Rendu de la page home - Aucun span courant trouvé pour la méthode showHomes.");
        }

            // Logique métier
            model.addAttribute("currentPage", "home");
            model.addAttribute("userConnected", username);
            model.addAttribute("userRole", roles);

            return "home";
    }


    @RequestMapping("/patients")
    @NewSpan("clientui-patients-list")
    public String showPatients(Model model) {

        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            currentSpan.tag("page", "patients-list");
            currentSpan.event("Récupération de la liste des patients");
            logger.info("Rendu de la page liste des patients - Span courant : traceId={}, spanId={}",
                    currentSpan.context().traceId(),
                    currentSpan.context().spanId());
        } else {
            logger.warn("Rendu de la page liste des patients - Aucun span courant trouvé pour la méthode showPatients.");
        }

        // Ajout des attributs pour le template
        // Implémenté automatiquement via addUserInfoToModel - @ModemAttribute
//        model.addAttribute("userConnected", username);
//        model.addAttribute("userRole", roles);

        // Logique métier
        model.addAttribute("currentPage", "patients");
        List<PatientBean> patients = servicesProxy.retrievePatientList();;
        model.addAttribute("patients", patients);

        return "list";
    }


    @RequestMapping("/update/{id}")
    @NewSpan("clientui-patient-update-form")
    public String showUpdateForm(@PathVariable("id") Long id, Model model) {

        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            currentSpan.tag("patient.id", String.valueOf(id));
            currentSpan.event("Affichage du formulaire de mise à jour pour le patient ID : " + id);
            logger.info("Rendu de la page MAJ Patient : {} - Span courant : traceId={}, spanId={}",
                    id,
                    currentSpan.context().traceId(),
                    currentSpan.context().spanId());
        } else {
            logger.warn("Rendu de la page MAJ Patient id = {} - Aucun span courant trouvé pour la méthode showUpdateForm.", id);
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
        logger.info("Note list size = {}", notes.size());
        if (notes == null) {
            notes = new ArrayList<>(); // Liste vide par défaut
            logger.warn("Aucune note trouvée pour le patient ID : {}. Liste vide initialisée.", id);
        }
        model.addAttribute("notes", notes);

        return "update";
    }


    @PostMapping("/update/{id}/addnotes")
    @NewSpan("clientui-patient-add-note")
    public String addNote(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("newNote") NoteBean newNote,
            BindingResult result,
            Model model) {

        logger.info("Méthode addNote appelée avec id = {}", id); // Log de début de méthode

        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            currentSpan.tag("patient.id", String.valueOf(id));
            currentSpan.event("Ajout d'une note pour le patient ID : " + id);
            logger.info("Ajout d'une note pour le patient ID : {} - Span courant : traceId={}, spanId={}",
                    id,
                    currentSpan.context().traceId(),
                    currentSpan.context().spanId());
        } else {
            logger.warn("Ajout d'une note pour le patient ID : {} - Aucun span courant trouvé.", id);
        }

        // Assigner patId et patient à newNote
        newNote.setPatId(id);
        newNote.setPatient(servicesProxy.retrievePatientId(id).getLastname());


        // Gestion des erreurs de validation
        if (result.hasErrors()) {
            logger.warn("Il y a des erreurs de validation lors de l'ajout d'une note");

            // Afficher les erreurs de validation
            result.getAllErrors().forEach(error -> {
                logger.warn("Erreur de validation: {}", error.getDefaultMessage());
            });

            // Recharge les données nécessaires pour la vue
            PatientBean patient = servicesProxy.retrievePatientId(id);
            List<NoteBean> notes = servicesProxy.retrieveNotesPatId(id);
            model.addAttribute("patient", patient);
            model.addAttribute("notes", notes);
            return "update";
        }

        // Appeler le microservice mnotes pour sauvegarder la note
        logger.info("Nouvelle note à sauvegarder -- patId = {} -- patient = {} -- note = {}", newNote.getPatId(), newNote.getPatient(), newNote.getNote());
        servicesProxy.addNote(newNote);

        // Rediriger vers la page de mise à jour du patient
        return "redirect:/update/" + id;
    }


    // Méthode pour afficher le formulaire d'ajout d'un patient
    @GetMapping("/add")
    @NewSpan("clientui-patient-add-form")
    public String showAddPatientForm(Model model) {

        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            currentSpan.tag("page", "add-patient-form");
            currentSpan.event("Affichage du formulaire d'ajout d'un patient");
            logger.info("Affichage du formulaire d'ajout d'un patient - Span courant : traceId={}, spanId={}",
                    currentSpan.context().traceId(),
                    currentSpan.context().spanId());
        } else {
            logger.warn("Affichage du formulaire d'ajout d'un patient - Aucun span courant trouvé.");
        }

        // Ajouter un nouvel objet PatientBean vide pour le formulaire
        model.addAttribute("patient", new PatientBean());
        model.addAttribute("currentPage", "add");

        return "add"; // Nom du template Thymeleaf pour le formulaire
    }


    // Méthode pour traiter la soumission du formulaire d'ajout d'un patient
    @PostMapping("/add/addPatient")
    @NewSpan("clientui-patient-add-submit")
    public String addPatient(
            @Valid @ModelAttribute("patient") PatientBean patient,
            BindingResult result,
            Model model) {

        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            currentSpan.tag("patient.lastname", patient.getLastname());
            currentSpan.event("Soumission du formulaire d'ajout d'un patient");
            logger.info("Soumission du formulaire d'ajout d'un patient - Span courant : traceId={}, spanId={}",
                    currentSpan.context().traceId(),
                    currentSpan.context().spanId());
        } else {
            logger.warn("Soumission du formulaire d'ajout d'un patient - Aucun span courant trouvé.");
        }

        // Gestion des erreurs de validation
        if (result.hasErrors()) {
            logger.warn("Erreurs de validation lors de l'ajout d'un patient");
            result.getAllErrors().forEach(error -> {
                logger.warn("Erreur de validation: {}", error.getDefaultMessage());
            });
            model.addAttribute("currentPage", "add");
            return "add"; // Retourne au formulaire en cas d'erreur
        }
        // Appeler le microservice pour sauvegarder le patient
        servicesProxy.addPatient(patient);
        logger.info("Nouveau patient ajouté : {}", patient.getLastname());
        // Rediriger vers la liste des patients
        return "redirect:/patients";
    }


}
