package com.clientui.clientui.beans;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


public class NoteBean {

    // NOTE : L'id n'est pas utilisé ici. On utilise le patId
    // private String id;      // Identifiant unique généré par MongoDB (ObjectId mappé en String)

//    @NotNull(message = "patId cannot be null")
//    @Positive(message = "patId must be a positive number")
    private Long patId;     // Clé de correspondance avec la base SQL

//    @NotBlank(message = "patient is mandatory")
    private String patient; // Nom du patient

//    @NotBlank(message = "note is mandatory")
    private String note;    // Champ texte pour la note (supporte les retours à la ligne)

    // Constructors
    public NoteBean() {
    }

    public NoteBean(Long patId, String patient, String note) {
        this.patId = patId;
        this.patient = patient;
        this.note = note;
    }


    // Getters Setters
//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }

    public Long getPatId() {
        return patId;
    }

    public void setPatId(Long patId) {
        this.patId = patId;
    }

    public String getPatient() {
        return patient;
    }

    public void setPatient(String patient) {
        this.patient = patient;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }


    @Override
    public String toString() {
        return "NoteBean{" +
                "patId=" + patId +
                ", patient='" + patient + '\'' +
                ", note='" + note + '\'' +
                '}';
    }
}
