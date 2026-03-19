package com.example.admindashboard.service;

import com.example.admindashboard.model.EmployeeProfile;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    // REMOVED PasswordEncoder! We will use {noop} instead to match your current setup.

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=========================================================");
        System.out.println("🔄 Checking database for default accounts and employee data...");

        // 1. SEED ADMIN ACCOUNT
        if (userRepository.findByUsername("ADM001").isEmpty()) {
            User admin = new User();
            admin.setUsername("ADM001");
            admin.setFullName("System Administrator");
            admin.setEmail("admin@whitecirclegroup.com");
            admin.setRole("ADMIN");
            admin.setPassword("{noop}admin123"); // USING {noop}
            admin.setJoiningDate(LocalDate.now());
            userRepository.save(admin);
            System.out.println("✅ Created Admin -> ID: ADM001 | Pass: admin123");
        }

        // 2. SEED CLIENT ACCOUNT
        if (userRepository.findByUsername("CLI001").isEmpty()) {
            User client = new User();
            client.setUsername("CLI001");
            client.setFullName("Acme Corp Client");
            client.setEmail("client@acmecorp.com");
            client.setRole("CLIENT");
            client.setPassword("{noop}welcome123"); // USING {noop}
            client.setJoiningDate(LocalDate.now());
            userRepository.save(client);
            System.out.println("✅ Created Client -> ID: CLI001 | Pass: welcome123");
        }

        // ==========================================
        // REAL EMPLOYEE DATA SEEDING
        // ==========================================

        // Employee 1: Om Tripathi
        if (userRepository.findByUsername("EMP114").isEmpty()) {
            User emp1 = new User();
            emp1.setUsername("EMP114");
            emp1.setPassword("{noop}welcome123"); // USING {noop}
            emp1.setRole("EMPLOYEE");
            emp1.setFullName("Om Tripathi");
            emp1.setEmail("om.whitecirclegroup@gmail.com");
            emp1.setDesignation("Marketing Lead");
            emp1.setMobileNumber("6265147016");
            emp1.setExperience("05 Years");
            emp1.setJoiningDate(LocalDate.of(2023, 3, 5));
            emp1.setBusinessUnit("Bhopal");
            emp1.setProjectName("Saller");
            emp1.setProjectCode("S65");
            emp1.setReportingManager("Virendra Tiwari");
            emp1.setCustomerName("Manish Rastogi");
            emp1.setWorkLocation("Bhopal");
            emp1.setCity("Bhopal");
            emp1.setBuHrContact("7509759872");

            EmployeeProfile profile1 = new EmployeeProfile();
            profile1.setDob(LocalDate.of(1992, 8, 24));

            profile1.setUser(emp1);
            emp1.setEmployeeProfile(profile1);
            userRepository.save(emp1);
            System.out.println("✅ Created Employee -> ID: EMP114 | Name: Om Tripathi");
        }

        // Employee 2: Om Singrore
        if (userRepository.findByUsername("EMP187").isEmpty()) {
            User emp2 = new User();
            emp2.setUsername("EMP187");
            emp2.setPassword("{noop}welcome123"); // USING {noop}
            emp2.setRole("EMPLOYEE");
            emp2.setFullName("Om Singrore");
            emp2.setEmail("omsingrorewhitecirclegroup@gmail.com");
            emp2.setDesignation("Project Manager");
            emp2.setMobileNumber("+91 75879 57916");
            emp2.setExperience("3.5 Years");
            emp2.setJoiningDate(LocalDate.of(2022, 5, 7));
            emp2.setBusinessUnit("Bhopal");
            emp2.setProjectName("Index");
            emp2.setProjectCode("IX47");
            emp2.setReportingManager("Sudeep Radhakrishnan");
            emp2.setCustomerName("Neha Tabbu");
            emp2.setWorkLocation("Bhopal");
            emp2.setCity("Bhopal");
            emp2.setBuHrContact("7509759872");

            EmployeeProfile profile2 = new EmployeeProfile();
            profile2.setDob(LocalDate.of(1989, 4, 16));

            profile2.setUser(emp2);
            emp2.setEmployeeProfile(profile2);
            userRepository.save(emp2);
            System.out.println("✅ Created Employee -> ID: EMP187 | Name: Om Singrore");
        }

        // Employee 3: Saumya Katare
        if (userRepository.findByUsername("EMP129").isEmpty()) {
            User emp3 = new User();
            emp3.setUsername("EMP129");
            emp3.setPassword("{noop}welcome123"); // USING {noop}
            emp3.setRole("EMPLOYEE");
            emp3.setFullName("Saumya Katare");
            emp3.setEmail("saumyawhitecirclegroup@gmail.com");
            emp3.setDesignation("IOS Developer");
            emp3.setMobileNumber("+91 7724051300");
            emp3.setExperience("4.8 Years");
            emp3.setJoiningDate(LocalDate.of(2022, 5, 7));
            emp3.setBusinessUnit("Bhopal");
            emp3.setProjectName("Sald");
            emp3.setProjectCode("SD09");
            emp3.setReportingManager("Manikanata");
            emp3.setCustomerName("Gairy Singh Nahar");
            emp3.setWorkLocation("Raipur");
            emp3.setCity("Raipur");
            emp3.setBuHrContact("7509759872");

            EmployeeProfile profile3 = new EmployeeProfile();
            profile3.setDob(LocalDate.of(1995, 5, 8));

            profile3.setUser(emp3);
            emp3.setEmployeeProfile(profile3);
            userRepository.save(emp3);
            System.out.println("✅ Created Employee -> ID: EMP129 | Name: Saumya Katare");
        }

        System.out.println("=========================================================");
    }
}