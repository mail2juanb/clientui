package com.clientui.clientui.beans;


/**
 * Bean class representing a RiskLevel object in the *MicroDiab* project.
 * This class is used to transfer risk level data between the frontend (*clientui*) and the backend (*mrisk* microservice).
 * It encapsulates the risk level associated with a patient, which is determined based on notes and patient history.
 *
 * <p>This bean is part of the *MicroDiab* application, which analyzes diabetes risk levels for patients.
 * The data is validated in the *mrisk* microservice using annotations such as {@code @NotNull} and {@code @NotBlank}.</p>
 *
 * <p>The risk level can be one of the following values: "None", "Borderline", "In Danger", or "Early Onset".</p>
 *
 * <p>Example usage in the frontend:</p>
 * <pre>
 *   RiskLevelBean riskLevel = new RiskLevelBean("In Danger", 1L);
 * </pre>
 *
 * @see com.clientui.clientui.proxies.MicroservicesProxy
 * @see com.clientui.clientui.controller.ClientController
 */
public class RiskLevelBean {

    /**
     * Unique identifier linking the risk level to a patient in the SQL database of *mpatient*.
     * This field is mandatory and cannot be null.
     *
     * <p>Validation annotations in *mrisk*:</p>
     * <pre>
     *   {@code @NotNull(message = "patId cannot be null")}
     * </pre>
     */
    private Long patId;

    /**
     * Risk level associated with the patient.
     * This field is mandatory and cannot be blank.
     * Possible values: "None", "Borderline", "In Danger", "Early Onset".
     *
     * <p>Validation annotations in *mrisk*:</p>
     * <pre>
     *   {@code @NotBlank(message = "riskLevel is mandatory")}
     * </pre>
     */
    private String riskLevel;

    /**
     * Default constructor.
     * Initializes an empty {@code RiskLevelBean} object.
     */
    public RiskLevelBean() {
    }

    /**
     * Parameterized constructor.
     * Initializes a {@code RiskLevelBean} object with the provided values.
     *
     * @param riskLevel The risk level of the patient.
     * @param patId     Unique identifier of the patient.
     */
    public RiskLevelBean(String riskLevel, Long patId) {
        this.riskLevel = riskLevel;
        this.patId = patId;
    }


    /**
     * Gets the risk level of the patient.
     *
     * @return the risk level of the patient.
     */
    public String getRiskLevel() {
        return riskLevel;
    }

    /**
     * Sets the risk level of the patient.
     *
     * @param riskLevel the risk level of the patient to set.
     */
    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    /**
     * Gets the unique identifier of the patient.
     *
     * @return the patient identifier.
     */
    public Long getPatId() {
        return patId;
    }

    /**
     * Sets the unique identifier of the patient.
     *
     * @param patId the patient identifier to set.
     */
    public void setPatId(Long patId) {
        this.patId = patId;
    }

    /**
     * Returns a string representation of the {@code RiskLevelBean} object.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "RiskLevelBean{" +
                "patId=" + patId +
                ", riskLevel='" + riskLevel + '\'' +
                '}';
    }
}
