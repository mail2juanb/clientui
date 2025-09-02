package com.clientui.clientui.proxies;

import com.clientui.clientui.beans.PatientBean;
import com.clientui.clientui.configuration.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;


//@FeignClient(name = "mpatient", url = "localhost:9001")
@FeignClient(name = "mgateway", url = "localhost:9010", configuration = FeignConfig.class)
public interface MicroservicePatientsProxy {

    @GetMapping(value = "/mpatient/patients")
    List<PatientBean> retrievePatientList();

    @GetMapping(value = "/mpatient/patient/{id}")
    PatientBean retrievePatientId(@PathVariable("id") Long id);

}
