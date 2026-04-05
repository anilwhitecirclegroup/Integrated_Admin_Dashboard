package com.example.admindashboard.controller;

import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class UserApiController {

    @Autowired
    private UserRepository userRepository;

    // REMOVED LOCK: All authenticated users (including standard employees)
    // need to be able to search for colleagues using the dashboard search bar.
    @GetMapping("/api/users/search")
    public ResponseEntity<List<User>> searchEmployees(@RequestParam("query") String query, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        boolean isManager = currentUser != null && currentUser.getRole() != null && "MANAGER".equalsIgnoreCase(currentUser.getRole().getRoleName());

        List<User> matchingUsers = userRepository.searchByKeyword(query);

        // VISIBILITY: Managers can only search for employees within their own team
        // (If you want Managers to search the whole company like regular employees,
        // you can safely delete this IF block!)
        if (isManager && currentUser != null) {
            matchingUsers = matchingUsers.stream()
                    .filter(u -> u.getManager() != null && u.getManager().getId().equals(currentUser.getId()))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(matchingUsers);
    }

    // REMOVED LOCK: All authenticated users need to see the birthday widget.
    @GetMapping("/api/users/birthdays-today")
    public ResponseEntity<List<User>> getBirthdaysToday(Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        boolean isManager = currentUser != null && currentUser.getRole() != null && "MANAGER".equalsIgnoreCase(currentUser.getRole().getRoleName());

        List<User> users = userRepository.findByBirthdayToday();

        // VISIBILITY: Managers only see birthdays of their own team members
        if (isManager && currentUser != null) {
            users = users.stream()
                    .filter(u -> u.getManager() != null && u.getManager().getId().equals(currentUser.getId()))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(users);
    }
}