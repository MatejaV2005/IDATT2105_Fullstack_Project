package com.grimni.domain;

import jakarta.persistence.*;

// example POST body for testing:
// {"username":"...", "email":"...", "password":"...", "role":"EMPLOYEE", "organization": {"id": 1}}

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
}