package com.grimni.domain;

import com.grimni.domain.enums.RoutineUserRole;
import com.grimni.domain.ids.CcpUserBridgeId;
import jakarta.persistence.*;

@Entity
@Table(name = "ccp_user_bridge")
public class CcpUserBridge extends CreatedAtEntity {

    @EmbeddedId
    private CcpUserBridgeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ccpId")
    @JoinColumn(name = "ccp_id")
    private Ccp ccp;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false, insertable = false, updatable = false)
    private RoutineUserRole userRole;
    
    public CcpUserBridge() {
    }

    public CcpUserBridgeId getId() {
        return id;
    }

    public void setId(CcpUserBridgeId id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Ccp getCcp() {
        return ccp;
    }

    public void setCcp(Ccp ccp) {
        this.ccp = ccp;
    }

    public RoutineUserRole getUserRole() {
        return userRole;
    }

    public void setUserRole(RoutineUserRole userRole) {
        this.userRole = userRole;
    }

}