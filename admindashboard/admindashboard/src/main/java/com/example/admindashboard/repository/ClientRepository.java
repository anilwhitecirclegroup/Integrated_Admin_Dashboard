package com.example.admindashboard.repository;

import com.example.admindashboard.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    @Query("SELECT c FROM Client c WHERE " +
            "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.clientId) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Client> searchClients(@Param("keyword") String keyword);
}