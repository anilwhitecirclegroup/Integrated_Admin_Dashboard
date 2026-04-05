package com.example.admindashboard.service;

import com.example.admindashboard.model.User;
import com.example.admindashboard.model.Permission;
import com.example.admindashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Attempting login for user: " + username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Set<GrantedAuthority> authorities = new HashSet<>();

        // 1. Add the main Role
        if (user.getRole() != null) {
            // Clean it up just in case, and format it for Spring Security
            String cleanRole = user.getRole().getRoleName().replace("ROLE_", "").toUpperCase().trim();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + cleanRole));

            // 2. Add all granular permissions attached to this Role
            if (user.getRole().getPermissions() != null) {
                for (Permission permission : user.getRole().getPermissions()) {
                    authorities.add(new SimpleGrantedAuthority(permission.getPermissionName()));
                }
            }
        } else {
            System.err.println("WARNING: User " + username + " has no assigned role!");
            // Provide a basic fallback so the app doesn't completely crash for this user
            authorities.add(new SimpleGrantedAuthority("ROLE_UNASSIGNED"));
        }

        System.out.println("User FOUND! Loaded Authorities: " + authorities);

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}