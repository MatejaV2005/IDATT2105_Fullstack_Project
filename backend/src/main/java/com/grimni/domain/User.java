package com.grimni.domain;

import java.util.List;

import jakarta.persistence.*;

// example POST body for testing:
// {"username":"...", "email":"...", "password":"...", "role":"EMPLOYEE", "organization": {"id": 1}}

@Entity(name = "User")
@Table(name = "users")
public class User {
    
    public enum Role {
        EMPLOYEE,
        MANAGER,
        ADMIN
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

    // TODO: Potentially unnecesarry, but can be neet as we can call user.getRefreshTokens() easily
    @OneToMany(mappedBy = "user")
    List<RefreshToken> tokens;

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

    public List<RefreshToken> getRefreshTokens() {
        return tokens;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}