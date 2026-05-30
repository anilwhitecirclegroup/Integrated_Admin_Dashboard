package com.example.admindashboard.repository;

import com.example.admindashboard.model.EmployeeLeaveWallet;
import com.example.admindashboard.model.LeaveTypeMaster;
import com.example.admindashboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeLeaveWalletRepository
        extends JpaRepository<EmployeeLeaveWallet, Long> {

    Optional<EmployeeLeaveWallet> findByUserAndLeaveType(
            User user,
            LeaveTypeMaster leaveType
    );

    List<EmployeeLeaveWallet> findByUser(User user);

}