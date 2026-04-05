package com.grimni.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.CourseUserProgress;

public interface CourseUserProgressRepository extends JpaRepository<CourseUserProgress, Long> {

}
