package com.grimni.service;

import org.springframework.stereotype.Service;

import com.grimni.domain.CcpRecord;
import com.grimni.domain.Organization;
import com.grimni.domain.Deviation;
import com.grimni.domain.User;
import com.grimni.repository.DeviationRepository;
import com.grimni.repository.UserRepository;
import com.grimni.repository.OrganizationRepository;
import com.grimni.repository.CcpRecordRepository;

import java.util.List;

import com.grimni.dto.CreateDeviationRequest;
import com.grimni.domain.enums.ReviewStatus;

@Service
public class DeviationService {

    private final DeviationRepository deviationRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final CcpRecordRepository ccpRecordRepository;

    public DeviationService(DeviationRepository deviationRepository, UserRepository userRepository,
            OrganizationRepository organizationRepository, CcpRecordRepository ccpRecordRepository) {
        this.deviationRepository = deviationRepository;
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.ccpRecordRepository = ccpRecordRepository;
    }

    public Deviation createDeviation(CreateDeviationRequest request, Long reporterId) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Organization organization = organizationRepository.findById(request.organizationId())
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        Deviation deviation = new Deviation();
        deviation.setOrganization(organization);
        deviation.setReportedBy(reporter);
        deviation.setCategory(request.category());
        deviation.setWhatWentWrong(request.whatWentWrong());
        deviation.setImmediateActionTaken(request.immediateActionTaken());
        deviation.setPotentialCause(request.potentialCause());
        deviation.setPotentialPreventativeMeasure(request.potentialPreventativeMeasure());
        deviation.setPreventativeMeasureActuallyTaken("");
        deviation.setReviewStatus(ReviewStatus.OPEN);

        if (request.ccpRecordId() != null) {
            CcpRecord ccpRecord = ccpRecordRepository.findById(request.ccpRecordId())
                    .orElseThrow(() -> new RuntimeException("CCP Record not found"));
            deviation.setCcpRecord(ccpRecord);
        }

        return deviationRepository.save(deviation);
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
