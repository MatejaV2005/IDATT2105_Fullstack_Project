package com.grimni.domain;

import jakarta.persistence.*;

import java.util.List;

import com.grimni.util.Domain;

@Entity
@Table(name = "routines")
public class Routine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long routineId;
    
    @ManyToOne
    @JoinColumn(name = "orgId")
    private Organization organization;
    
    private String law;
    private String reason;
    private String description;
    
    @ManyToMany
    @JoinTable(
        name = "routine_user_bridge", 
        joinColumns = @JoinColumn(name = "routineId"), 
        inverseJoinColumns = @JoinColumn(name = "userId"))
    private List<User> users;
    
    public Domain domain;

    // dokumentert gjennomgang av rutiner?

    public String getDescription() {
        return description;
    }
    public Domain getDomain() {
        return domain;
    }
    public String getLaw() {
        return law;
    }
    public Organization getOrganization() {
        return organization;
    }
    public String getReason() {
        return reason;
    }
    public Long getRoutineId() {
        return routineId;
    }
    public List<User> getUsers() {
        return users;
    }
}