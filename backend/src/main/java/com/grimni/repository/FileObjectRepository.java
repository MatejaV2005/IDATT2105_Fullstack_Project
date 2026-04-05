package com.grimni.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.FileObject;

public interface FileObjectRepository extends JpaRepository<FileObject, Long> {

}
