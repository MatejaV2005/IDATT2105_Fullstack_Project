package com.grimni.domain;

import com.grimni.domain.enums.RoutineUserRole;
import com.grimni.domain.ids.RoutineUserBridgeId;
import jakarta.persistence.*;

@Entity
@Table(name = "routine_user_bridge")
public class RoutineUserBridge extends CreatedAtEntity {

    @EmbeddedId
    private RoutineUserBridgeId id; // Has user_role as a part of composite keys

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("routineId")
    @JoinColumn(name = "routine_id")
    private PrerequisiteRoutine routine;

    public RoutineUserBridge() {
    }

    public RoutineUserBridgeId getId() {
        return id;
    }

    public void setId(RoutineUserBridgeId id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public PrerequisiteRoutine getRoutine() {
        return routine;
    }

    public void setRoutine(PrerequisiteRoutine routine) {
        this.routine = routine;
    }

    public RoutineUserRole getUserRole() {
        return id != null ? id.getUserRole() : null;
    }

    public void setUserRole(RoutineUserRole userRole) {
        if (id == null) {
            id = new RoutineUserBridgeId();
        }
        id.setUserRole(userRole);
    }

}
