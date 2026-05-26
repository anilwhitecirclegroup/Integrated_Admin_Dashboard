package com.example.admindashboard.repository;

import com.example.admindashboard.model.CompOffEntry;
import com.example.admindashboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CompOffEntryRepository
        extends JpaRepository<CompOffEntry, Long> {

    List<CompOffEntry> findByUserAndStatus(User user, String status);

    List<CompOffEntry> findByExpiryDateBefore(LocalDate date);

}