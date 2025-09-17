package com.clientui.clientui.controller;

import com.clientui.clientui.beans.PatientBean;
import com.clientui.clientui.proxies.MicroservicePatientsProxy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class ClientController {

    private final MicroservicePatientsProxy patientsProxy;

    public ClientController(MicroservicePatientsProxy patientsProxy) {
        this.patientsProxy = patientsProxy;
    }


    @RequestMapping("/home")
    public String showHomes(
            @RequestHeader(value = "X-Auth-Username", required = false, defaultValue = "Unknown") String username,
            @RequestHeader(value = "X-Auth-Roles", required = false, defaultValue = "USER") String roles,
            Model model) {

        model.addAttribute("userConnected", username);
        model.addAttribute("userRole", roles);

        return "home";
    }

    @RequestMapping("/patients")
    public String showPatients(Model model) {
        List<PatientBean> patients = patientsProxy.retrievePatientList();;
        model.addAttribute("patients", patients);
        return "list";
    }

    @RequestMapping("/update/{id}")
    public String showUpdateForm(@PathVariable("id") Long id, Model model) {
        final PatientBean patient = patientsProxy.retrievePatientId(id);
        model.addAttribute("patient", patient);
        return "update";
    }

    //TODO : Ajouter de nouveaux patients
}
