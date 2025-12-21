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


@ControllerAdvice
public class FeignExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(FeignExceptionHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    MicroservicesProxy servicesProxy;

    @ExceptionHandler(FeignException.class)
    public ModelAndView handleFeignException(FeignException e, HttpServletRequest request) {
        logger.warn("FeignException interceptée : status={}, URI={}", e.status(), request.getRequestURI());

        // Récupère l'ID du patient depuis la requête
        final PatientBean requestPatient = (PatientBean) request.getAttribute("patient");
        if (requestPatient == null) {
            logger.error("Aucun patient trouvé dans les attributs de la requête.");
            return new ModelAndView("redirect:/patients").addObject("error", "Patient introuvable.");
        }
        logger.info("Patient récupéré avec request = ID: {}, LASTNAME: {}, FIRSTNAME: {}, GENDER: {}  ...", requestPatient.getId(), requestPatient.getLastname(), requestPatient.getFirstname(), requestPatient.getGender());

        Long patientId = null;
        if (requestPatient.getId() != null) {               // Cela arrive avec la page add, l'id n'est pas encore affecté
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
                logger.error("Erreur Feign lors de la récupération des données pour le patient {} : {}", patientId, ex.contentUTF8());
                // Redirige vers la liste des patients en cas d'erreur
                ModelAndView mav = new ModelAndView("redirect:/patients");
                mav.addObject("error", "Erreur lors de la récupération des données du patient : " + ex.getMessage());
                return mav;
            }
        }

        // Détermine la vue depuis la requete
        String viewName = (String) request.getAttribute("targetView");
        if (viewName == null) {
            // Valeur par défaut si l'attribut n'est pas défini
            viewName = "update";
        }
        logger.info("Vue cible : {}", viewName);


        // Création du ModelAndView
        ModelAndView mav = new ModelAndView(viewName);
        mav.addObject("patient", patient);
        mav.addObject("notes", notes);
        mav.addObject("newNote", newNote);
        mav.addObject("riskLevel", riskLevel.getRiskLevel());
        mav.addObject("currentPage", viewName);

        // Traite le corps de la réponse Feign
        final String body = e.contentUTF8();
        logger.info("Contenu retour Feign : {}", body);

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
                    mav.addObject("error", map.getOrDefault("error", "Conflit détecté."));
                } catch (Exception ex) {
                    logger.error("Erreur lors du parsing du corps 409 : {}", ex.getMessage());
                    //return new ModelAndView("redirect:/patients").addObject("error", "Conflit détecté (format inattendu) : " + ex.getMessage());
                    return new ModelAndView("redirect:/home").addObject("error", "Conflit détecté (format inattendu) : " + ex.getMessage());
                }

            }

            // Autre code HTTP
            else {
                //mav.addObject("error", String.format("Erreur %d : %s", e.status(), e.getMessage()));
                return new ModelAndView("redirect:/home").addObject("error", "Erreur " + e.status() + " : " + e.getMessage());
            }
        } catch (Exception ex) {
            logger.error("Erreur parsing Feign : {}", ex.getMessage(), ex);
            //mav.addObject("error", "Erreur lors du traitement de la réponse : " + ex.getMessage());
            return new ModelAndView("redirect:/home").addObject("error", "Erreur lors du traitement du status de la réponse : " + ex.getMessage());
        }

        return mav;
    }
}
