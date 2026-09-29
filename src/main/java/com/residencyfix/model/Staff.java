package com.residencyfix.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "staff")
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Staff name is required")
    @Column(nullable = false)
    private String name;

    @Email
    @NotBlank(message = "Email is required")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Specialization is required")
    @Column(nullable = false)
    private String specialization;

    private String phone;

    @JsonIgnore
    @OneToMany(mappedBy = "assignedStaff", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Complaint> assignedComplaints = new ArrayList<>();

    public Staff() {
    }

    public Staff(String name, String email, String specialization, String phone) {
        this.name = name;
        this.email = email;
        this.specialization = specialization;
        this.phone = phone;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<Complaint> getAssignedComplaints() {
        return assignedComplaints;
    }

    public void setAssignedComplaints(List<Complaint> assignedComplaints) {
        this.assignedComplaints = assignedComplaints;
    }
}
