package com.grimni.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.CourseLink;

public interface CourseLinkRepository extends JpaRepository<CourseLink, Long> {
    List<CourseLink> findByCourseId(Long courseId);
    Optional<CourseLink> findByIdAndCourseId(Long linkId, Long courseId);
}
