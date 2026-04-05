//package com.grimni.controller;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.grimni.domain.Organization;
//import com.grimni.repository.OrganizationRepository;
//
//@RestController
//@CrossOrigin
//public class OrganizationController {
//
//	private OrganizationRepository organizationRepository;
//
//	@Autowired
//    public OrganizationController(OrganizationRepository organizationRepository) {
//        this.organizationRepository = organizationRepository;
//    }
//	
//	@PostMapping("/organizations")
//	public ResponseEntity<?> createOrganization(@RequestBody Organization organization) {
//        organization = organizationRepository.save(organization);
//		return new ResponseEntity<>(HttpStatus.CREATED);
//	}
//
//    @GetMapping("/organizations")
//    public ResponseEntity<Iterable<Organization>> getAllOrganizations() {
//        Iterable<Organization> allOrganizations = organizationRepository.findAll();
//        return new ResponseEntity<>(allOrganizations, HttpStatus.OK);
//	}
//}
