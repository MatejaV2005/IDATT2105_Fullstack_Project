package com.grimni.domain;

import com.grimni.domain.ids.MappingPointResponsibleUserId;
import jakarta.persistence.*;

@Entity
@Table(name = "mapping_point_user_bridge")
public class MappingPointResponsibleUser extends CreatedAtEntity {

    @EmbeddedId
    private MappingPointResponsibleUserId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("mappingPointId")
    @JoinColumn(name = "mapping_point_id")
    private MappingPoint mappingPoint;
}