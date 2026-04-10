package com.grimni.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.CourseResponsibleUser;
import com.grimni.domain.ids.CourseUserProgressId;

public interface CourseResponsibleUserRepository extends JpaRepository<CourseResponsibleUser, CourseUserProgressId> {
    List<CourseResponsibleUser> findByCourseId(Long courseId);
    Optional<CourseResponsibleUser> findByCourseIdAndUserId(Long courseId, Long userId);
}
