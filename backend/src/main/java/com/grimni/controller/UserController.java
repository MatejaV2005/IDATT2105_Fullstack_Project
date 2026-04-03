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
import com.grimni.domain.User;
import com.grimni.repository.CertificateRepository;
import com.grimni.repository.UserRepository;

@RestController
@CrossOrigin
public class UserController {
	private UserRepository userRepository;
	private CertificateRepository certificateRepository;

	@Autowired
    public UserController(UserRepository userRepository, CertificateRepository certificateRepository) {
        this.userRepository = userRepository;
        this.certificateRepository = certificateRepository;
    }
	
	@PostMapping("/users")
	public ResponseEntity<?> createUser(@RequestBody User user) {
        user = userRepository.save(user);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

    @GetMapping("/users")
    public ResponseEntity<Iterable<User>> getAllUsers() {
        Iterable<User> allUsers = userRepository.findAll();
        return new ResponseEntity<>(allUsers, HttpStatus.OK);
	}

    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        return userRepository.findById(userId)
            .map(user -> new ResponseEntity<>(user, HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/users/{userId}/certificates")
    public ResponseEntity<Iterable<Certificate>> getUserCertificates(@PathVariable Long userId) {
        Iterable<Certificate> certificates = certificateRepository.findByUserUserId(userId);
        return new ResponseEntity<>(certificates, HttpStatus.OK);
    }
}
