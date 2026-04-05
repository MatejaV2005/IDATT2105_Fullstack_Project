package com.grimni.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.CourseLink;

public interface CourseLinkRepository extends JpaRepository<CourseLink, Long> {

}
