package com.grimni.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.CourseUserProgress;
import com.grimni.domain.ids.CourseUserProgressId;

public interface CourseUserProgressRepository extends JpaRepository<CourseUserProgress, CourseUserProgressId> {

}
