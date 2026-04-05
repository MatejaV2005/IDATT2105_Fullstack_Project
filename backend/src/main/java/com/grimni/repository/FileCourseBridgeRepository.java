package com.grimni.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.FileCourseBridge;
import com.grimni.domain.ids.FileCourseBridgeId;

public interface FileCourseBridgeRepository extends JpaRepository<FileCourseBridge, FileCourseBridgeId> {

}
