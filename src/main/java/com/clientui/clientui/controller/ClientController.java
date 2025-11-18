package com.clientui.clientui.controller;

import com.clientui.clientui.beans.NoteBean;
import com.clientui.clientui.beans.PatientBean;
import com.clientui.clientui.proxies.MicroservicesProxy;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.annotation.NewSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

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

    //TODO : Ajouter de nouveaux patients
}
