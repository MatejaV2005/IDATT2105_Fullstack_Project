package com.grimni.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.CourseResponsibleUser;
import com.grimni.domain.ids.CourseUserProgressId;

public interface CourseResponsibleUserRepository extends JpaRepository<CourseResponsibleUser, CourseUserProgressId> {

}
