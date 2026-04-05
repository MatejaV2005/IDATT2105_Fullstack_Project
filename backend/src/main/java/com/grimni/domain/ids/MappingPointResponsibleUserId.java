package com.grimni.domain.ids;

import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Embeddable;

@Embeddable
public class MappingPointResponsibleUserId implements Serializable {
    private Long userId;
    private Long mappingPointId;

    public MappingPointResponsibleUserId() {}

    public MappingPointResponsibleUserId(Long userId, Long mappingPointId) {
        this.userId = userId;
        this.mappingPointId = mappingPointId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MappingPointResponsibleUserId that)) return false;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(mappingPointId, that.mappingPointId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, mappingPointId);
    }
}