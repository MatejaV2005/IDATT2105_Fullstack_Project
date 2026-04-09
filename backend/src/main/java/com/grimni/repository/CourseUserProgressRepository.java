package com.grimni.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.CourseUserProgress;
import com.grimni.domain.ids.CourseUserProgressId;

public interface CourseUserProgressRepository extends JpaRepository<CourseUserProgress, CourseUserProgressId> {
    boolean existsByCourseId(Long courseId);
    List<CourseUserProgress> findByCourseId(Long courseId);
    Optional<CourseUserProgress> findByCourseIdAndUserId(Long courseId, Long userId);
    List<CourseUserProgress> findByCourseIdIn(List<Long> courseIds);
}
