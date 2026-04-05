package com.grimni.domain.ids;

import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Embeddable;

/*
 * This entire class is made to represent a composite primary key.
 * */
@Embeddable
public class OrgUserBridgeId implements Serializable {
    private Long orgId;
    private Long userId;

    public OrgUserBridgeId() {}

    public OrgUserBridgeId(Long orgId, Long userId) {
        this.orgId = orgId;
        this.userId = userId;
    }

    public Long getOrgId() { return orgId; }
    public Long getUserId() { return userId; }

    public void setOrgId(Long orgId) { this.orgId = orgId; }
    public void setUserId(Long userId) { this.userId = userId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrgUserBridgeId that)) return false;
        return Objects.equals(orgId, that.orgId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orgId, userId);
    }
}