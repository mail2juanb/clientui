package com.clientui.clientui.proxies;

import com.clientui.clientui.beans.NoteBean;
import com.clientui.clientui.beans.PatientBean;
import com.clientui.clientui.configuration.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;


//@FeignClient(name = "mpatient", url = "localhost:9001")
@FeignClient(name = "mgateway", url = "localhost:9010", configuration = FeignConfig.class)
public interface MicroservicesProxy {

    @GetMapping(value = "/mpatient/patients")
    List<PatientBean> retrievePatientList();

    @GetMapping(value = "/mpatient/patient/{id}")
    PatientBean retrievePatientId(@PathVariable("id") Long id);

    @PostMapping(value = "/mpatient/patient")
    void addPatient(@RequestBody PatientBean patient);

    @PutMapping(value = "/mpatient/patient/{id}")
    void updatePatient(@PathVariable("id") Long id, @RequestBody PatientBean patient);

    @GetMapping(value = "/mnotes/notes/{patId}")
    List<NoteBean> retrieveNotesPatId(@PathVariable("patId") Long patId);

    @PostMapping(value = "/mnotes/notes")
    void addNote(@RequestBody NoteBean newNote);

}
