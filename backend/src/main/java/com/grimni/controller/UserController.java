package com.grimni.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.grimni.domain.User;
import com.grimni.repository.UserRepository;

@RestController
@CrossOrigin
public class UserController {
	private UserRepository userRepository;

	@Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
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
}
