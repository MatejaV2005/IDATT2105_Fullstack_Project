package com.grimni.domain;

import com.grimni.domain.enums.OrgUserRole;
import com.grimni.domain.ids.OrgUserBridgeId;
import jakarta.persistence.*;

@Entity
@Table(name = "org_user_bridge")
public class OrgUserBridge extends CreatedAtEntity {

    @EmbeddedId
    private OrgUserBridgeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("orgId")
    @JoinColumn(name = "org_id")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private OrgUserRole userRole;
    
    public OrgUserBridge() {
    }

    public OrgUserBridgeId getId() {
        return id;
    }

    public void setId(OrgUserBridgeId id) {
        this.id = id;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public OrgUserRole getUserRole() {
        return userRole;
    }

    public void setUserRole(OrgUserRole userRole) {
        this.userRole = userRole;
    }

}