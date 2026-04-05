package com.grimni.domain.ids;

import java.io.Serializable;
import java.util.Objects;
import com.grimni.domain.enums.RoutineUserRole;
import jakarta.persistence.*;

@Embeddable
public class CcpUserBridgeId implements Serializable {
    private Long userId;
    private Long ccpId;

    @Enumerated(EnumType.STRING)
    private RoutineUserRole userRole;

    public CcpUserBridgeId() {}

    public CcpUserBridgeId(Long userId, Long ccpId, RoutineUserRole userRole) {
        this.userId = userId;
        this.ccpId = ccpId;
        this.userRole = userRole;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CcpUserBridgeId that)) return false;
        return Objects.equals(userId, that.userId)
            && Objects.equals(ccpId, that.ccpId)
            && userRole == that.userRole;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, ccpId, userRole);
    }
}