package com.grimni.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.grimni.domain.OrgUserBridge;
import com.grimni.domain.Organization;
import com.grimni.domain.User;
import com.grimni.domain.enums.OrgUserRole;
import com.grimni.domain.ids.OrgUserBridgeId;
import com.grimni.dto.CreateOrganizationRequest;
import com.grimni.dto.UpdateOrganizationRequest;
import com.grimni.repository.OrgUserBridgeRepository;
import com.grimni.repository.OrganizationRepository;
import com.grimni.repository.UserRepository;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrgUserBridgeRepository orgUserBridgeRepository;
    private final UserRepository userRepository;

    public OrganizationService(OrganizationRepository organizationRepository,
                               OrgUserBridgeRepository orgUserBridgeRepository,
                               UserRepository userRepository) {
        this.organizationRepository = organizationRepository;
        this.orgUserBridgeRepository = orgUserBridgeRepository;
        this.userRepository = userRepository;
    }

    public Organization createOrganization(CreateOrganizationRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Organization org = new Organization();
        org.setOrgName(request.orgName());
        org.setOrgAddress(request.orgAddress());
        org.setOrgNumber(request.orgNumber());
        org.setAlcoholEnabled(request.alcoholEnabled());
        org.setFoodEnabled(request.foodEnabled());

        org = organizationRepository.save(org);

        OrgUserBridge bridge = new OrgUserBridge();
        bridge.setId(new OrgUserBridgeId(org.getId(), userId));
        bridge.setOrganization(org);
        bridge.setUser(user);
        bridge.setUserRole(OrgUserRole.OWNER);
        orgUserBridgeRepository.save(bridge);

        return org;
    }

    public List<Organization> findOrganizationsByUserId(Long userId) {
        return orgUserBridgeRepository.findByUserId(userId).stream()
                .map(OrgUserBridge::getOrganization)
                .toList();
    }

    public Organization findOrganizationByIdAndUser(Long orgId, Long userId) {
        OrgUserBridge bridge = orgUserBridgeRepository.findByOrganizationIdAndUserId(orgId, userId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        return bridge.getOrganization();
    }

    public Organization findOrganizationById(Long orgId) {
        return organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
    }

    public Organization updateOrganization(Long orgId, UpdateOrganizationRequest request, Long userId) {
        orgUserBridgeRepository.findByOrganizationIdAndUserId(orgId, userId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        if (request.orgName() != null) org.setOrgName(request.orgName());
        if (request.orgAddress() != null) org.setOrgAddress(request.orgAddress());
        if (request.orgNumber() != null) org.setOrgNumber(request.orgNumber());
        if (request.alcoholEnabled() != null) org.setAlcoholEnabled(request.alcoholEnabled());
        if (request.foodEnabled() != null) org.setFoodEnabled(request.foodEnabled());

        return organizationRepository.save(org);
    }
}
