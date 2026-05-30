package com.example.admindashboard.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "thanks_wallets")
public class ThanksWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Links the wallet directly to the employee
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnore
    private User user;

    // Dashboard Metrics
    private Integer totalPointsEarned = 0;
    private Integer walletBalance = 0;
    private Integer rewardsReceived = 0;

    // Constructors
    public ThanksWallet() {}

    public ThanksWallet(User user) {
        this.user = user;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Integer getTotalPointsEarned() { return totalPointsEarned; }
    public void setTotalPointsEarned(Integer totalPointsEarned) { this.totalPointsEarned = totalPointsEarned; }

    public Integer getWalletBalance() { return walletBalance; }
    public void setWalletBalance(Integer walletBalance) { this.walletBalance = walletBalance; }

    public Integer getRewardsReceived() { return rewardsReceived; }
    public void setRewardsReceived(Integer rewardsReceived) { this.rewardsReceived = rewardsReceived; }
}