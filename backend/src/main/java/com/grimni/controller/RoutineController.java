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

import com.grimni.domain.Routine;
import com.grimni.repository.PrerequisiteRoutineRepository;

@RestController
@CrossOrigin
public class RoutineController {
    private PrerequisiteRoutineRepository routineRepository;

    @Autowired
    public RoutineController(PrerequisiteRoutineRepository routineRepository) {
        this.routineRepository = routineRepository;
    }

    @PostMapping("/routines")
    public ResponseEntity<?> createRoutine(@RequestBody Routine routine) {
        routine = routineRepository.save(routine);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/routines")
    public ResponseEntity<Iterable<Routine>> getAllRoutines() {
        Iterable<Routine> allRoutines = routineRepository.findAll();
        return new ResponseEntity<>(allRoutines, HttpStatus.OK);
    }

    @GetMapping("/routines/{routineId}")
    public ResponseEntity<Routine> getRoutineById(@PathVariable Long routineId) {
        return routineRepository.findById(routineId)
                .map(routine -> new ResponseEntity<>(routine, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
