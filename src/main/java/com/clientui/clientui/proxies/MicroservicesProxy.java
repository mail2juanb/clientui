package com.clientui.clientui.proxies;

import com.clientui.clientui.beans.NoteBean;
import com.clientui.clientui.beans.PatientBean;
import com.clientui.clientui.beans.RiskLevelBean;
import com.clientui.clientui.configuration.FeignConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Feign client interface for communicating with backend microservices in the MicroDiab application.
 * This interface defines methods to interact with 'mpatient', 'mnotes', and 'mrisk' microservices.
 */
@FeignClient(name = "mgateway", url = "${mgateway.url:http://mgateway:9010}", configuration = FeignConfig.class)
@Tag(name = "Microservices Proxy", description = "Feign client for interacting with backend microservices")
public interface MicroservicesProxy {

    /**
     * Retrieves the list of all patients from the 'mpatient' microservice.
     *
     * @return A list of PatientBean objects representing all patients.
     */
    @Operation(
        summary = "Retrieve the list of all patients",
        description = "Fetches the complete list of patients from the 'mpatient' microservice.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the patient list"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @GetMapping(value = "/mpatient/patients")
    List<PatientBean> retrievePatientList();

    /**
     * Retrieves a specific patient by their ID from the 'mpatient' microservice.
     *
     * @param id The unique identifier of the patient.
     * @return A PatientBean object representing the requested patient.
     */
    @Operation(
        summary = "Retrieve a patient by ID",
        description = "Fetches a specific patient from the 'mpatient' microservice using their unique ID.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the patient"),
            @ApiResponse(responseCode = "404", description = "Patient not found")
        }
    )
    @GetMapping(value = "/mpatient/patient/{id}")
    PatientBean retrievePatientId(
        @Parameter(description = "Unique identifier of the patient", required = true, example = "1")
        @PathVariable("id") Long id
    );

    /**
     * Adds a new patient to the 'mpatient' microservice.
     *
     * @param patient The PatientBean object representing the patient to add.
     */
    @Operation(
        summary = "Add a new patient",
        description = "Sends a request to the 'mpatient' microservice to add a new patient.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Patient successfully added"),
            @ApiResponse(responseCode = "400", description = "Invalid patient data")
        }
    )
    @PostMapping(value = "/mpatient/patient")
    void addPatient(
        @Parameter(description = "Patient object to add", required = true)
        @RequestBody PatientBean patient
    );

    /**
     * Updates an existing patient in the 'mpatient' microservice.
     *
     * @param id The unique identifier of the patient to update.
     * @param patient The PatientBean object containing updated patient data.
     */
    @Operation(
        summary = "Update an existing patient",
        description = "Sends a request to the 'mpatient' microservice to update an existing patient.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Patient successfully updated"),
            @ApiResponse(responseCode = "404", description = "Patient not found"),
            @ApiResponse(responseCode = "400", description = "Invalid patient data")
        }
    )
    @PutMapping(value = "/mpatient/patient/{id}")
    void updatePatient(
        @Parameter(description = "Unique identifier of the patient to update", required = true, example = "1")
        @PathVariable("id") Long id,
        @Parameter(description = "Updated patient object", required = true)
        @RequestBody PatientBean patient
    );

    /**
     * Retrieves all notes for a specific patient from the 'mnotes' microservice.
     *
     * @param patId The unique identifier of the patient.
     * @return A list of NoteBean objects representing the patient's notes.
     */
    @Operation(
        summary = "Retrieve all notes for a patient",
        description = "Fetches all notes associated with a specific patient from the 'mnotes' microservice.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the notes"),
            @ApiResponse(responseCode = "404", description = "Patient not found")
        }
    )
    @GetMapping(value = "/mnotes/notes/{patId}")
    List<NoteBean> retrieveNotesPatId(
        @Parameter(description = "Unique identifier of the patient", required = true, example = "1")
        @PathVariable("patId") Long patId
    );

    /**
     * Adds a new note for a patient in the 'mnotes' microservice.
     *
     * @param newNote The NoteBean object representing the note to add.
     */
    @Operation(
        summary = "Add a new note for a patient",
        description = "Sends a request to the 'mnotes' microservice to add a new note for a patient.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Note successfully added"),
            @ApiResponse(responseCode = "400", description = "Invalid note data")
        }
    )
    @PostMapping(value = "/mnotes/notes")
    void addNote(
        @Parameter(description = "Note object to add", required = true)
        @RequestBody NoteBean newNote
    );

    /**
     * Retrieves the risk level for a specific patient from the 'mrisk' microservice.
     *
     * @param patId The unique identifier of the patient.
     * @return A RiskLevelBean object representing the patient's risk level.
     */
    @Operation(
        summary = "Retrieve the risk level for a patient",
        description = "Fetches the risk level associated with a specific patient from the 'mrisk' microservice.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the risk level"),
            @ApiResponse(responseCode = "404", description = "Patient not found")
        }
    )
    @GetMapping("/mrisk/risk/{patId}")
    RiskLevelBean getRiskLevel(
        @Parameter(description = "Unique identifier of the patient", required = true, example = "1")
        @PathVariable("patId") Long patId
    );
}
