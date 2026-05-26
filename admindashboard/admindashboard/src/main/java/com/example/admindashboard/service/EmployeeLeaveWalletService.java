package com.example.admindashboard.service;

import com.example.admindashboard.model.EmployeeLeaveWallet;
import com.example.admindashboard.model.LeaveTypeMaster;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.EmployeeLeaveWalletRepository;
import com.example.admindashboard.repository.LeaveTypeMasterRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeLeaveWalletService {

    private final EmployeeLeaveWalletRepository walletRepository;
    private final LeaveTypeMasterRepository leaveTypeRepository;

    public EmployeeLeaveWalletService(
            EmployeeLeaveWalletRepository walletRepository,
            LeaveTypeMasterRepository leaveTypeRepository
    ) {
        this.walletRepository = walletRepository;
        this.leaveTypeRepository = leaveTypeRepository;
    }

    public void initializeEmployeeWallet(User user) {

        List<LeaveTypeMaster> leaveTypes =
                leaveTypeRepository.findAll();

        for (LeaveTypeMaster leaveType : leaveTypes) {

            boolean exists = walletRepository
                    .findByUserAndLeaveType(user, leaveType)
                    .isPresent();

            if (!exists) {

                EmployeeLeaveWallet wallet =
                        new EmployeeLeaveWallet();

                wallet.setUser(user);

                wallet.setLeaveType(leaveType);

                /*
                 * Initial Balance Logic
                 */

                double openingBalance =
                        leaveType.getMaxBalance();

                // LOP and COMP_OFF should start with 0
                if (
                        leaveType.getLeaveCode().equals("LOP")
                                ||
                        leaveType.getLeaveCode().equals("COMP_OFF")
                ) {
                    openingBalance = 0.0;
                }

                wallet.setOpeningBalance(openingBalance);

                wallet.setEarnedCredit(0.0);

                wallet.setUsedBalance(0.0);

                wallet.setEncashedBalance(0.0);

                wallet.setAvailableBalance(openingBalance);

                walletRepository.save(wallet);

                System.out.println(
                        "Wallet created for user: "
                                + user.getUsername()
                                + " | Leave Type: "
                                + leaveType.getLeaveCode()
                );
            }
        }
    }
}