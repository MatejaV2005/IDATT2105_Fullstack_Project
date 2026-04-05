package com.grimni.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.CcpRecord;

public interface CcpRecordRepository extends JpaRepository<CcpRecord, Long>  {

}
