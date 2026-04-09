package com.grimni.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.OrgDangerAnalysisCollaborator;
import com.grimni.domain.ids.OrgUserBridgeId;

public interface OrgDangerAnalysisCollaboratorRepository extends JpaRepository<OrgDangerAnalysisCollaborator, OrgUserBridgeId>  {

}
