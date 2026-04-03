package com.grimni.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;

@Entity(name = "User")
@Table(name = "users")
public class User {
    
    public enum Role {
        EMPLOYEE,
        MANAGER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String username;
    private String email;
    private String password;
    @ManyToOne
    @JoinColumn(name = "orgId")
    private Organization organization;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "user")
    @JsonManagedReference("user-certificates")
    private List<Certificate> certificates;

    @ManyToMany(mappedBy = "users")
    @JsonIgnore 
    private List<Routine> routines;

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Organization getOrganization() {
        return organization;
    }

    public Role getRole() {
        return role;
    }
    public List<Certificate> getCertificates() {
        return certificates;
    }
    public List<Routine> getRoutines() {
        return routines;
    }
}