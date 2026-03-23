package com.example.admindashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
// 1. Scan everything in both base packages
@ComponentScan(basePackages = {"com.example.admindashboard", "com.whitecircle.hrms"})
// 2. Point to the specific folders where your database code lives
@EntityScan(basePackages = {"com.example.admindashboard.model", "com.whitecircle.hrms.model"})
@EnableJpaRepositories(basePackages = {"com.example.admindashboard.repository", "com.whitecircle.hrms.repository"})

@EnableAsync
public class AdmindashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdmindashboardApplication.class, args);
    }
}