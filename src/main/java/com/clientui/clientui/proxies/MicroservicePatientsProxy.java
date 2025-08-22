package com.clientui.clientui.proxies;

import com.clientui.clientui.beans.PatientBean;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;


@FeignClient(name = "Mpatient", url = "localhost:9001")
public interface MicroservicePatientsProxy {

    @GetMapping(value = "/Patients")
    List<PatientBean> retrievePatientList();

    @GetMapping(value = "/Patient/{id}")
    PatientBean retrievePatientId(@PathVariable("id") Long id);

}
