package com.clientui.clientui.beans;

import java.time.LocalDate;

/**
 * Bean class representing a Patient object in the *MicroDiab* project.
 * This class is used to transfer patient-related data between the frontend (*clientui*) and the backend (*mpatient* microservice).
 * It encapsulates personal information about a patient, such as name, date of birth, gender, address, and phone number.
 *
 * <p>This bean is part of the *MicroDiab* application, which manages patient data and analyzes diabetes risk.
 * The data is validated in the *mpatient* microservice using annotations such as {@code @NotBlank}, {@code @NotNull}, and {@code @Past}.</p>
 *
 * <p>Example usage in the frontend:</p>
 * <pre>
 *   PatientBean patient = new PatientBean(
 *       1L,
 *       "Doe",
 *       "John",
 *       LocalDate.of(1980, 1, 1),
 *       "Male",
 *       "123 Main St, Paris",
 *       "+33123456789"
 *   );
 * </pre>
 *
 * @see com.clientui.clientui.proxies.MicroservicesProxy
 * @see com.clientui.clientui.controller.ClientController
 */
public class PatientBean {

    /**
     * Unique identifier of the patient.
     * This field is automatically generated in the *mpatient* microservice.
     *
     * <p>Validation annotations in *mpatient*:</p>
     * <pre>
     *   {@code @Id}
     *   {@code @GeneratedValue(strategy = GenerationType.IDENTITY)}
     * </pre>
     */
    private Long id;

    /**
     * Last name of the patient.
     * This field is mandatory and cannot be blank.
     *
     * <p>Validation annotations in *mpatient*:</p>
     * <pre>
     *   {@code @NotBlank(message = "lastname is mandatory")}
     * </pre>
     */
    private String lastname;

    /**
     * First name of the patient.
     * This field is mandatory and cannot be blank.
     *
     * <p>Validation annotations in *mpatient*:</p>
     * <pre>
     *   {@code @NotBlank(message = "firstname is mandatory")}
     * </pre>
     */
    private String firstname;

    /**
     * Date of birth of the patient.
     * This field is mandatory and must be a date in the past.
     *
     * <p>Validation annotations in *mpatient*:</p>
     * <pre>
     *   {@code @NotNull(message = "dateofbirth is mandatory")}
     *   {@code @Past(message = "dateofbirth must be in the past")}
     * </pre>
     */
    private LocalDate dateofbirth;

    /**
     * Gender of the patient.
     * This field is mandatory and cannot be blank.
     *
     * <p>Validation annotations in *mpatient*:</p>
     * <pre>
     *   {@code @NotBlank(message = "gender is mandatory")}
     * </pre>
     */
    private String gender;

    /**
     * Address of the patient.
     * This field is optional.
     */
    private String address;

    /**
     * Phone number of the patient.
     * This field is optional.
     */
    private String phone;

    /**
     * Default constructor.
     * Initializes an empty {@code PatientBean} object.
     */
    public PatientBean() {
    }

    /**
     * Parameterized constructor.
     * Initializes a {@code PatientBean} object with the provided values.
     *
     * @param id          Unique identifier of the patient.
     * @param lastname    Last name of the patient.
     * @param firstname   First name of the patient.
     * @param dateofbirth Date of birth of the patient.
     * @param gender      Gender of the patient.
     * @param address     Address of the patient.
     * @param phone       Phone number of the patient.
     */
    public PatientBean(Long id, String lastname, String firstname, LocalDate dateofbirth, String gender, String address, String phone) {
        this.id = id;
        this.lastname = lastname;
        this.firstname = firstname;
        this.dateofbirth = dateofbirth;
        this.gender = gender;
        this.address = address;
        this.phone = phone;
    }


    /**
     * Gets the unique identifier of the patient.
     *
     * @return the unique identifier of the patient.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the patient.
     *
     * @param id the unique identifier of the patient to set.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the last name of the patient.
     *
     * @return the last name of the patient.
     */
    public String getLastname() {
        return lastname;
    }

    /**
     * Sets the last name of the patient.
     *
     * @param lastname the last name of the patient to set.
     */
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    /**
     * Gets the first name of the patient.
     *
     * @return the first name of the patient.
     */
    public String getFirstname() {
        return firstname;
    }

    /**
     * Sets the first name of the patient.
     *
     * @param firstname the first name of the patient to set.
     */
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    /**
     * Gets the date of birth of the patient.
     *
     * @return the date of birth of the patient.
     */
    public LocalDate getDateofbirth() {
        return dateofbirth;
    }

    /**
     * Sets the date of birth of the patient.
     *
     * @param dateofbirth the date of birth of the patient to set.
     */
    public void setDateofbirth(LocalDate dateofbirth) {
        this.dateofbirth = dateofbirth;
    }

    /**
     * Gets the gender of the patient.
     *
     * @return the gender of the patient.
     */
    public String getGender() {
        return gender;
    }

    /**
     * Sets the gender of the patient.
     *
     * @param gender the gender of the patient to set.
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     * Gets the address of the patient.
     *
     * @return the address of the patient.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the address of the patient.
     *
     * @param address the address of the patient to set.
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Gets the phone number of the patient.
     *
     * @return the phone number of the patient.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the phone number of the patient.
     *
     * @param phone the phone number of the patient to set.
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Returns a string representation of the {@code PatientBean} object.
     *
     * @return a string representation of the object.
     */
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
