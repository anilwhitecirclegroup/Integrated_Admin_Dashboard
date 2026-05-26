package com.example.admindashboard.repository;

import com.example.admindashboard.model.InsurancePolicy;
import com.example.admindashboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InsurancePolicyRepository extends JpaRepository<InsurancePolicy, Long> {
    Optional<InsurancePolicy> findByUser(User user);
}