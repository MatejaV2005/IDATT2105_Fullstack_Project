package com.grimni.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.OrgDangerAnalysisCollaborator;
import com.grimni.domain.ids.OrgUserBridgeId;

public interface OrgDangerAnalysisCollaboratorRepository extends JpaRepository<OrgDangerAnalysisCollaborator, OrgUserBridgeId>  {
    List<OrgDangerAnalysisCollaborator> findByOrganizationId(Long orgId);
}
