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

import com.grimni.domain.Deviation;
import com.grimni.repository.DeviationRepository;

@RestController
@CrossOrigin
public class DeviationController {
    private DeviationRepository deviationRepository;

    @Autowired
    public DeviationController(DeviationRepository deviationRepository) {
        this.deviationRepository = deviationRepository;
    }

    @PostMapping("/deviations")
    public ResponseEntity<?> createDeviation(@RequestBody Deviation deviation) {
        deviation = deviationRepository.save(deviation);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/deviations")
    public ResponseEntity<Iterable<Deviation>> getAllDeviations() {
        Iterable<Deviation> allDeviations = deviationRepository.findAll();
        return new ResponseEntity<>(allDeviations, HttpStatus.OK);
    }

    @GetMapping("/deviations/{deviationId}")
    public ResponseEntity<Deviation> getDeviationById(@PathVariable Long deviationId) {
        return deviationRepository.findById(deviationId)
                .map(deviation -> new ResponseEntity<>(deviation, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
