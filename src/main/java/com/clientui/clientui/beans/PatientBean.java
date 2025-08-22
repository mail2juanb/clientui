package com.clientui.clientui.beans;

import java.time.LocalDate;

public class PatientBean {

    private Long id;
    private String lastname;
    private String firstname;
    private LocalDate dateofbirth;
    private String gender;
    private String address;
    private String phone;

    // Constructeur sans arguments
    public PatientBean() {
    }

    // Constructeur avec tous les arguments
    public PatientBean(Long id, String lastname, String firstname, LocalDate dateofbirth, String gender, String address, String phone) {
        this.id = id;
        this.lastname = lastname;
        this.firstname = firstname;
        this.dateofbirth = dateofbirth;
        this.gender = gender;
        this.address = address;
        this.phone = phone;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

//    public void setId(Long id) {
//        this.id = id;
//    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public LocalDate getDateofbirth() {
        return dateofbirth;
    }

    public void setDateofbirth(LocalDate dateofbirth) {
        this.dateofbirth = dateofbirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    @Override
    public String toString() {
        return "PatientBean{" +
                "id=" + id +
                ", lastname='" + lastname + '\'' +
                ", firstname='" + firstname + '\'' +
                ", dateofbirth=" + dateofbirth +
                ", gender='" + gender + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
