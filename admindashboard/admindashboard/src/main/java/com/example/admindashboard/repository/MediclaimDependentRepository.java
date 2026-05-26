package com.example.admindashboard.repository;

import com.example.admindashboard.model.MediclaimDependent;
import com.example.admindashboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MediclaimDependentRepository extends JpaRepository<MediclaimDependent, Long> {
    List<MediclaimDependent> findByUser(User user);
}