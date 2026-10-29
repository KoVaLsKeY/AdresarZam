package org.example.adresarzam.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Entity
@Table(name = "employee")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotBlank
    @Size(min = 2, max = 50, message = "Meno musi obsahovat 2-50 znakov")
    @Column(name = "first_name", nullable = false,length = 50)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 50, message = "Priezvisko musi mat 2-50 znakov")
    @Column(name = "last_name", nullable = false,length = 50)
    private String lastName;

    @NotBlank
    @Column(name = "birth_date")
    private LocalDate birthDate;

    @NotBlank
    @Size(min = 2, max = 150, message = "Email musi obsahovat 2 za 150 znakov")
    @Email(message = "neplatny email")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "Invalid email format"
    )
    @Column(name = "email")
    private String email;

    @NotBlank
    @Pattern(
            regexp = "^\\+421[0-9]{9}$",
            message = "Phone number must be in format +421XXXXXXXXX"
    )
    @Column(name = "phone")
    private String phone;

    @NotBlank
    @Column(name = "job_title")
    private String jobTitle;

    @NotBlank
    @Min(value = 1001, message = "salary musi byt >1000")
    @Column(name = "salary")
    private Integer salary;

    @Column(name = "full_time")
    private Boolean fullTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public Integer getSalary() {
        return salary;
    }

    public void setSalary(Integer salary) {
        this.salary = salary;
    }

    public Boolean getFullTime() {
        return fullTime;
    }

    public void setFullTime(Boolean fullTime) {
        this.fullTime = fullTime;
    }
}