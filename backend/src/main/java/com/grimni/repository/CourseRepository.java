package com.grimni.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
