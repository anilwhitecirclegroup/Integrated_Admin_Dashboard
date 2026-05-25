package com.example.admindashboard.repository;

import com.example.admindashboard.model.LeaveLedger;
import com.example.admindashboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveLedgerRepository
        extends JpaRepository<LeaveLedger, Long> {

    List<LeaveLedger> findByUserOrderByTransactionDateDesc(User user);

}