package com.clientui.clientui.exception;

import com.clientui.clientui.beans.PatientBean;
import com.clientui.clientui.dto.ValidationErrorDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@ControllerAdvice
public class FeignExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(FeignExceptionHandler.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @ExceptionHandler(FeignException.class)
    public ModelAndView handleFeignException(FeignException e, HttpServletRequest request) {

        PatientBean patient = (PatientBean) request.getAttribute("patient");

        logger.warn("FeignException interceptée globalement : status={}", e.status());

        ModelAndView mav = new ModelAndView("add");

        mav.addObject("patient", patient);
        mav.addObject("currentPage", "add");

        String body = e.contentUTF8();
        logger.debug("Contenu retour Feign : {}", body);

        try {

            // 400 - Erreurs de validation
            if (e.status() == 400) {
                List<ValidationErrorDTO> errors =
                        objectMapper.readValue(body,
                                new TypeReference<List<ValidationErrorDTO>>() {
                                });

                Map<String, String> errorMap = new HashMap<>();

                for (ValidationErrorDTO err : errors) {
                    errorMap.put(err.getField(), err.getDefaultMessage());
                }

                mav.addObject("errors", errorMap);
            }

            // 409 - Conflit / doublon
            else if (e.status() == 409) {
                Map<String, String> map = objectMapper.readValue(body, Map.class);
                mav.addObject("error", map.get("error"));
            }

            // Autre code HTTP
            else {
                mav.addObject("error", "Une erreur inattendue est survenue.");
            }
        } catch (Exception ex) {
            logger.error("Erreur parsing Feign : {}", ex.getMessage());
            mav.addObject("error", "Erreur lors du traitement de la réponse.");
        }

        return mav;
    }
}
