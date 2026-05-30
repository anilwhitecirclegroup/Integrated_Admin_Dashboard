package com.example.admindashboard.repository;

import com.example.admindashboard.model.ThanksTransaction;
import com.example.admindashboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThanksTransactionRepository extends JpaRepository<ThanksTransaction, Long> {
    // Fetches the ledger history, newest first
    List<ThanksTransaction> findByUserOrderByTransactionDateDesc(User user);
}