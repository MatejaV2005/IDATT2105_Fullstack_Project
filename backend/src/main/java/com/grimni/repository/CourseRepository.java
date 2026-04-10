package com.grimni.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByOrganizationId(Long orgId);
}
