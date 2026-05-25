package com.example.admindashboard.security;

import com.example.admindashboard.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // NEW: This turns on @PreAuthorize!
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Note: In production, consider enabling CSRF for form endpoints
                .authorizeHttpRequests(auth -> auth
                        // Publicly accessible assets and login
                        .requestMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll()

                        // All other requests MUST be authenticated.
                        // The actual granular permission checks will now happen inside the Controllers!
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/perform_login")
                        .successHandler((request, response, authentication) -> {

                            String authorities = authentication.getAuthorities().toString();
                            // Get which portal user selected during login
                            String portalType = request.getParameter("portalType");

                            boolean isClient = authorities.contains("ROLE_CLIENT");

                            // PORTAL VALIDATION
                            // Client trying Employee Portal
                            if ("employee".equalsIgnoreCase(portalType) && isClient) {

                                request.getSession().setAttribute(
                                        "SPRING_SECURITY_LAST_EXCEPTION",
                                        new RuntimeException("Client users cannot login through Employee Portal.")
                                );

                                response.sendRedirect("/login?error=true");
                                return;
                            }

                            // Employee/Admin trying Client Portal
                            if ("client".equalsIgnoreCase(portalType) && !isClient) {

                                request.getSession().setAttribute(
                                        "SPRING_SECURITY_LAST_EXCEPTION",
                                        new RuntimeException("Employee users cannot login through Client Portal.")
                                );
                                response.sendRedirect("/login?error=true");
                                return;
                            }

                            // NORMAL ROLE-BASED ROUTING
                            if (authorities.contains("admin_dashboard_view")) {
                                response.sendRedirect("/admin/dashboard");
                            }
                            else if (isClient) {
                                response.sendRedirect("/client/dashboard");
                            }
                            else {
                                response.sendRedirect("/employee/dashboard");
                            }
                        })
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .userDetailsService(customUserDetailsService)
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }
}