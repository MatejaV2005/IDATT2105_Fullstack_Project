package com.grimni.repository;

import org.springframework.data.repository.CrudRepository;

import com.grimni.domain.Course;

public interface CourseRepository extends CrudRepository<Course, Long> {
}
