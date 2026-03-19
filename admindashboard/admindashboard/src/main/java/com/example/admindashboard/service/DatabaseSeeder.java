package com.example.admindashboard.service; // Updated to match your folder structure!

import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=========================================================");
        System.out.println("🔄 Checking database for default accounts...");

        // 1. SEED ADMIN ACCOUNT
        if (userRepository.findByUsername("ADM001").isEmpty()) {
            User admin = new User();
            admin.setUsername("ADM001");
            admin.setFullName("System Administrator");
            admin.setEmail("admin@whitecirclegroup.com");
            admin.setRole("ADMIN");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setJoiningDate(LocalDate.now());
            // Removed the setStatus line that was causing the error
            userRepository.save(admin);
            System.out.println("✅ Created Admin -> ID: ADM001 | Pass: admin123");
        }

        // 2. SEED EMPLOYEE ACCOUNT
        if (userRepository.findByUsername("EMP001").isEmpty()) {
            User employee = new User();
            employee.setUsername("EMP001");
            employee.setFullName("Test Employee");
            employee.setEmail("employee@whitecirclegroup.com");
            employee.setRole("EMPLOYEE");
            employee.setDesignation("Software Engineer");
            employee.setBusinessUnit("Engineering");
            employee.setPassword(passwordEncoder.encode("welcome123"));
            employee.setJoiningDate(LocalDate.now());
            // Removed the setStatus line
            userRepository.save(employee);
            System.out.println("✅ Created Employee -> ID: EMP001 | Pass: welcome123");
        }

        // 3. SEED CLIENT ACCOUNT
        if (userRepository.findByUsername("CLI001").isEmpty()) {
            User client = new User();
            client.setUsername("CLI001");
            client.setFullName("Acme Corp Client");
            client.setEmail("client@acmecorp.com");
            client.setRole("CLIENT");
            client.setPassword(passwordEncoder.encode("welcome123"));
            client.setJoiningDate(LocalDate.now());
            // Removed the setStatus line
            userRepository.save(client);
            System.out.println("✅ Created Client -> ID: CLI001 | Pass: welcome123");
        }

        System.out.println("=========================================================");
    }
}