package com.clientui.clientui.beans;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

public class PatientBeanTest {

    @Test
    void testNoArgsConstructor() {
        // Arrange & Act
        PatientBean patient = new PatientBean();

        // Assert
        assertThat(patient).isNotNull();
        assertThat(patient.getId()).isNull();
        assertThat(patient.getLastname()).isNull();
        assertThat(patient.getFirstname()).isNull();
        assertThat(patient.getDateofbirth()).isNull();
        assertThat(patient.getGender()).isNull();
        assertThat(patient.getAddress()).isNull();
        assertThat(patient.getPhone()).isNull();
    }

    @Test
    void testAllArgsConstructor() {
        // Arrange
        Long id = 1L;
        String lastname = "Dupont";
        String firstname = "Jean";
        LocalDate dateofbirth = LocalDate.of(1990, 5, 15);
        String gender = "M";
        String address = "123 Rue de Paris";
        String phone = "0123456789";

        // Act
        PatientBean patient = new PatientBean(id, lastname, firstname, dateofbirth, gender, address, phone);

        // Assert
        assertThat(patient.getId()).isEqualTo(id);
        assertThat(patient.getLastname()).isEqualTo(lastname);
        assertThat(patient.getFirstname()).isEqualTo(firstname);
        assertThat(patient.getDateofbirth()).isEqualTo(dateofbirth);
        assertThat(patient.getGender()).isEqualTo(gender);
        assertThat(patient.getAddress()).isEqualTo(address);
        assertThat(patient.getPhone()).isEqualTo(phone);
    }

    @Test
    void testSetters() {
        // Arrange
        PatientBean patient = new PatientBean();
        Long id = 1L;
        String lastname = "Dupont";
        String firstname = "Jean";
        LocalDate dateofbirth = LocalDate.of(1990, 5, 15);
        String gender = "M";
        String address = "123 Rue de Paris";
        String phone = "0123456789";

        // Act
        patient.setId(id);
        patient.setLastname(lastname);
        patient.setFirstname(firstname);
        patient.setDateofbirth(dateofbirth);
        patient.setGender(gender);
        patient.setAddress(address);
        patient.setPhone(phone);

        // Assert
        assertThat(patient.getId()).isEqualTo(id);
        assertThat(patient.getLastname()).isEqualTo(lastname);
        assertThat(patient.getFirstname()).isEqualTo(firstname);
        assertThat(patient.getDateofbirth()).isEqualTo(dateofbirth);
        assertThat(patient.getGender()).isEqualTo(gender);
        assertThat(patient.getAddress()).isEqualTo(address);
        assertThat(patient.getPhone()).isEqualTo(phone);
    }

    @Test
    void testToString() {
        // Arrange
        Long id = 1L;
        String lastname = "Dupont";
        String firstname = "Jean";
        LocalDate dateofbirth = LocalDate.of(1990, 5, 15);
        String gender = "M";
        String address = "123 Rue de Paris";
        String phone = "0123456789";
        PatientBean patient = new PatientBean(id, lastname, firstname, dateofbirth, gender, address, phone);

        // Act
        String result = patient.toString();

        // Assert
        assertThat(result).contains(
                "id=" + id,
                "lastname='" + lastname + "'",
                "firstname='" + firstname + "'",
                "dateofbirth=" + dateofbirth,
                "gender='" + gender + "'",
                "address='" + address + "'",
                "phone='" + phone + "'"
        );
    }
}
