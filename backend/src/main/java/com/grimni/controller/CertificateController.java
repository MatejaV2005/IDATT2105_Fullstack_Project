package com.grimni.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.grimni.domain.Certificate;
import com.grimni.repository.CertificateRepository;

@RestController
@CrossOrigin
public class CertificateController {
    private CertificateRepository certificateRepository;

    @Autowired
    public CertificateController(CertificateRepository certificateRepository) {
        this.certificateRepository = certificateRepository;
    }

    @PostMapping("/certificates")
    public ResponseEntity<?> createCertificate(@RequestBody Certificate certificate) {
        certificate = certificateRepository.save(certificate);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/certificates")
    public ResponseEntity<Iterable<Certificate>> getAllCertificates() {
        Iterable<Certificate> allCertificates = certificateRepository.findAll();
        return new ResponseEntity<>(allCertificates, HttpStatus.OK);
    }

    @GetMapping("/certificates/{certificateId}")
    public ResponseEntity<Certificate> getCertificateById(@PathVariable Long certificateId) {
        return certificateRepository.findById(certificateId)
                .map(certificate -> new ResponseEntity<>(certificate, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
