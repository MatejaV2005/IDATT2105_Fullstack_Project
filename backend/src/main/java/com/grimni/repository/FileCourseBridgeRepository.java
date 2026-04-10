package com.grimni.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.FileCourseBridge;
import com.grimni.domain.ids.FileCourseBridgeId;

public interface FileCourseBridgeRepository extends JpaRepository<FileCourseBridge, FileCourseBridgeId> {
    List<FileCourseBridge> findByCourseId(Long courseId);
}
