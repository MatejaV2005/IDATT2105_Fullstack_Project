package com.grimni.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.CcpUserBridge;
import com.grimni.domain.OrgDangerAnalysisCollaborator;

public interface OrgDangerAnalysisCollaboratorRepository extends JpaRepository<OrgDangerAnalysisCollaborator, Long>  {

}
