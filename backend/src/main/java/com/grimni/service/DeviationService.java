package com.grimni.service;

import org.springframework.stereotype.Service;

import com.grimni.domain.Deviation;
import com.grimni.domain.User;
import com.grimni.repository.DeviationRepository;
import com.grimni.repository.UserRepository;

import java.util.List;

@Service
public class DeviationService {

    private final DeviationRepository deviationRepository;
    private final UserRepository userRepository;

    public DeviationService(DeviationRepository deviationRepository, UserRepository userRepository) {
        this.deviationRepository = deviationRepository;
        this.userRepository = userRepository;
    }

    public List<Deviation> getAllDeviations() {
        return deviationRepository.findAll();
    }

    public Deviation getDeviationById(Long id) {
        return deviationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deviation not found"));
    }

    public Deviation resolveDeviation(Long id, Long reviewerId, String preventativeMeasure) {
        Deviation deviation = getDeviationById(id);

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        deviation.setReviewStatus(com.grimni.domain.enums.ReviewStatus.CLOSED);
        deviation.setReviewedBy(reviewer);
        deviation.setReviewedAt(java.time.LocalDateTime.now());
        deviation.setPreventativeMeasureActuallyTaken(preventativeMeasure);

        return deviationRepository.save(deviation);
    }
}
