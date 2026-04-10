package com.grimni.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.InternalControlReview;

public interface InternalControlReviewRepository extends JpaRepository<InternalControlReview, Long> {

    @EntityGraph(attributePaths = {"organization", "reviewedBy"})
    List<InternalControlReview> findByOrganization_IdOrderByCreatedAtDesc(Long orgId);

    @EntityGraph(attributePaths = {"organization", "reviewedBy"})
    Optional<InternalControlReview> findByIdAndOrganization_Id(Long id, Long orgId);
}
