package com.example.admindashboard.repository;

import com.example.admindashboard.model.ThanksWallet;
import com.example.admindashboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ThanksWalletRepository extends JpaRepository<ThanksWallet, Long> {
    Optional<ThanksWallet> findByUser(User user);
}