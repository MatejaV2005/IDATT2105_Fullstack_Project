package com.grimni.domain.ids;

import java.io.Serializable;
import java.util.Objects;
import com.grimni.domain.enums.RoutineUserRole;
import jakarta.persistence.*;

@Embeddable
public class RoutineUserBridgeId implements Serializable {
    private Long userId;
    private Long routineId;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role")
    private RoutineUserRole userRole;

    public RoutineUserBridgeId() {}

    public RoutineUserBridgeId(Long userId, Long routineId, RoutineUserRole userRole) {
        this.userId = userId;
        this.routineId = routineId;
        this.userRole = userRole;
    }

    public RoutineUserRole getUserRole() {
        return userRole;
    }

    public void setUserRole(RoutineUserRole userRole) {
        this.userRole = userRole;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoutineUserBridgeId that)) return false;
        return Objects.equals(userId, that.userId)
            && Objects.equals(routineId, that.routineId)
            && userRole == that.userRole;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, routineId, userRole);
    }
}
