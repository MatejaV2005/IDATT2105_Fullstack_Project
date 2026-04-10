package com.grimni.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.FileObject;

public interface FileObjectRepository extends JpaRepository<FileObject, Long> {
    List<FileObject> findByOrganization_Id(Long orgId);
}
