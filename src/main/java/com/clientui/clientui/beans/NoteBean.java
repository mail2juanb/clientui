package com.clientui.clientui.beans;


/**
 * Bean class representing a Note object in the *MicroDiab* project.
 * This class is used to transfer note-related data between the frontend (*clientui*) and the backend (*mnotes* microservice).
 * It encapsulates information about a patient's note, including the patient identifier, name, and note content.
 *
 * <p>This bean is part of the *MicroDiab* application, which aims to analyze diabetes risk based on patient notes.
 * The data is validated in the *mnotes* microservice using annotations such as {@code @NotNull}, {@code @Positive}, and {@code @NotBlank}.</p>
 *
 * <p>Example usage in the frontend:</p>
 * <pre>
 *   NoteBean note = new NoteBean(1L, "John Doe", "Patient reports high blood sugar levels.");
 * </pre>
 *
 * @see com.clientui.clientui.proxies.MicroservicesProxy
 * @see com.clientui.clientui.controller.ClientController
 */
public class NoteBean {

    /**
     * Unique identifier linking the note to a patient in the SQL database of *mpatient*.
     * This field is mandatory and must be a positive number.
     *
     * <p>Validation annotations in *mnotes*:</p>
     * <pre>
     *   {@code @NotNull(message = "patId cannot be null")}
     *   {@code @Positive(message = "patId must be a positive number")}
     * </pre>
     */
    private Long patId;

    /**
     * Name of the patient associated with the note.
     * This field is mandatory and cannot be blank.
     *
     * <p>Validation annotations in *mnotes*:</p>
     * <pre>
     *   {@code @NotBlank(message = "patient is mandatory")}
     * </pre>
     */
    private String patient;

    /**
     * Text content of the note. Supports line breaks.
     * This field is mandatory and cannot be blank.
     *
     * <p>Validation annotations in *mnotes*:</p>
     * <pre>
     *   {@code @NotBlank(message = "note is mandatory")}
     * </pre>
     */
    private String note;

    /**
     * Default constructor.
     * Initializes an empty {@code NoteBean} object.
     */
    public NoteBean() {
    }

    /**
     * Parameterized constructor.
     * Initializes a {@code NoteBean} object with the provided values.
     *
     * @param patId   Unique identifier of the patient.
     * @param patient Name of the patient.
     * @param note    Text content of the note.
     */
    public NoteBean(Long patId, String patient, String note) {
        this.patId = patId;
        this.patient = patient;
        this.note = note;
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
     * Gets the name of the patient.
     *
     * @return the name of the patient.
     */
    public String getPatient() {
        return patient;
    }

    /**
     * Sets the name of the patient.
     *
     * @param patient the name of the patient to set.
     */
    public void setPatient(String patient) {
        this.patient = patient;
    }

    /**
     * Gets the text content of the note.
     *
     * @return the note content.
     */
    public String getNote() {
        return note;
    }

    /**
     * Sets the text content of the note.
     *
     * @param note the note content to set.
     */
    public void setNote(String note) {
        this.note = note;
    }

    /**
     * Returns a string representation of the {@code NoteBean} object.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "NoteBean{" +
                "patId=" + patId +
                ", patient='" + patient + '\'' +
                ", note='" + note + '\'' +
                '}';
    }
}
