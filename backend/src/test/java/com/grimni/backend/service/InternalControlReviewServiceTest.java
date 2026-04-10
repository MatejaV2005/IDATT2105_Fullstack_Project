package com.grimni.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.grimni.domain.InternalControlReview;
import com.grimni.domain.OrgUserBridge;
import com.grimni.domain.Organization;
import com.grimni.domain.User;
import com.grimni.dto.CreateInternalControlReviewRequest;
import com.grimni.repository.InternalControlReviewRepository;
import com.grimni.repository.OrgUserBridgeRepository;
import com.grimni.repository.UserRepository;
import com.grimni.service.InternalControlReviewService;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class InternalControlReviewServiceTest {

    @Mock
    private InternalControlReviewRepository internalControlReviewRepository;

    @Mock
    private OrgUserBridgeRepository orgUserBridgeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InternalControlReviewService internalControlReviewService;

    private Organization testOrg;
    private User testUser;
    private OrgUserBridge membership;

    @BeforeEach
    void setUp() {
        testOrg = new Organization();
        testOrg.setId(10L);
        testOrg.setOrgName("Test Org");

        testUser = new User();
        testUser.setId(1L);
        testUser.setLegalName("Manager User");
        testUser.setEmail("manager@example.com");

        membership = new OrgUserBridge();
        membership.setOrganization(testOrg);
        membership.setUser(testUser);
    }

    @Nested
    @DisplayName("getReviews")
    class GetReviewsTests {

        @Test
        @DisplayName("returns org reviews")
        void getReviews_success() {
            InternalControlReview newest = createReview(2L, "Newest", LocalDateTime.now());
            InternalControlReview older = createReview(1L, "Older", LocalDateTime.now().minusDays(1));

            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));
            when(internalControlReviewRepository.findByOrganization_IdOrderByCreatedAtDesc(10L))
                .thenReturn(List.of(newest, older));

            List<InternalControlReview> reviews = internalControlReviewService.getReviews(1L, 10L);

            assertEquals(2, reviews.size());
            assertEquals(2L, reviews.get(0).getId());
            assertEquals(1L, reviews.get(1).getId());
        }

        @Test
        @DisplayName("throws when org membership is missing")
        void getReviews_orgNotFound_throws() {
            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> internalControlReviewService.getReviews(1L, 10L));
        }
    }

    @Nested
    @DisplayName("createReview")
    class CreateReviewTests {

        @Test
        @DisplayName("creates review with trimmed summary")
        void createReview_success() {
            CreateInternalControlReviewRequest request = new CreateInternalControlReviewRequest("  Daily note  ");

            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(internalControlReviewRepository.save(any(InternalControlReview.class))).thenAnswer(invocation -> {
                InternalControlReview review = invocation.getArgument(0);
                ReflectionTestUtils.setField(review, "id", 5L);
                return review;
            });

            InternalControlReview review = internalControlReviewService.createReview(request, 1L, 10L);

            assertEquals(5L, review.getId());
            assertEquals("Daily note", review.getSummary());
            assertEquals(testOrg, review.getOrganization());
            assertEquals(testUser, review.getReviewedBy());
        }

        @Test
        @DisplayName("rejects blank summary")
        void createReview_blankSummary_throws() {
            CreateInternalControlReviewRequest request = new CreateInternalControlReviewRequest("   ");

            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> internalControlReviewService.createReview(request, 1L, 10L)
            );

            assertEquals("Summary cannot be blank", exception.getMessage());
            verify(internalControlReviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws when user is missing")
        void createReview_userNotFound_throws() {
            CreateInternalControlReviewRequest request = new CreateInternalControlReviewRequest("Summary");

            when(orgUserBridgeRepository.findByOrganizationIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> internalControlReviewService.createReview(request, 1L, 10L));
            verify(internalControlReviewRepository, never()).save(any());
        }
    }

    private InternalControlReview createReview(Long id, String summary, LocalDateTime createdAt) {
        InternalControlReview review = new InternalControlReview();
        ReflectionTestUtils.setField(review, "id", id);
        review.setOrganization(testOrg);
        review.setReviewedBy(testUser);
        review.setSummary(summary);
        ReflectionTestUtils.setField(review, "createdAt", createdAt);
        return review;
    }
}
