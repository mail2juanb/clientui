package com.clientui.clientui.beans;

public class NoteBean {

    private String id;      // Identifiant unique généré par MongoDB (ObjectId mappé en String)
    private Long patId;     // Clé de correspondance avec la base SQL
    private String patient; // Nom du patient
    private String note;    // Champ texte pour la note (supporte les retours à la ligne)

    // Constructors
    public NoteBean() {
    }

    public NoteBean(String id, Long patId, String patient, String note) {
        this.id = id;
        this.patId = patId;
        this.patient = patient;
        this.note = note;
    }


    // Getters Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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
                "id='" + id + '\'' +
                ", patId=" + patId +
                ", patient='" + patient + '\'' +
                ", note='" + note + '\'' +
                '}';
    }
}
