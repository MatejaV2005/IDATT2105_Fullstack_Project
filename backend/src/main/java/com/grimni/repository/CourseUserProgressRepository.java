package com.grimni.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.grimni.domain.CourseUserProgress;
import com.grimni.domain.ids.CourseUserProgressId;

public interface CourseUserProgressRepository extends JpaRepository<CourseUserProgress, CourseUserProgressId> {
    boolean existsByCourseId(Long courseId);
    List<CourseUserProgress> findByCourseId(Long courseId);
    Optional<CourseUserProgress> findByCourseIdAndUserId(Long courseId, Long userId);
    List<CourseUserProgress> findByCourseIdIn(List<Long> courseIds);

    @Query("""
        select progress
        from CourseUserProgress progress
        join fetch progress.user
        join fetch progress.course course
        where course.organization.id = :orgId
        """)
    List<CourseUserProgress> findAllByOrganizationIdWithUser(@Param("orgId") Long orgId);
}
