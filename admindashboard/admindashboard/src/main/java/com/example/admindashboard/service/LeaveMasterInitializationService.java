package com.example.admindashboard.service;

import com.example.admindashboard.model.LeaveTypeMaster;
import com.example.admindashboard.repository.LeaveTypeMasterRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

@Service
public class LeaveMasterInitializationService {

    private final LeaveTypeMasterRepository leaveTypeMasterRepository;

    public LeaveMasterInitializationService(
            LeaveTypeMasterRepository leaveTypeMasterRepository
    ) {
        this.leaveTypeMasterRepository = leaveTypeMasterRepository;
    }

    @PostConstruct
    public void initializeLeaveTypes() {

        createLeaveTypeIfNotExists(
                "CL",
                "Casual Leave",
                1.0,
                12.0
        );

        createLeaveTypeIfNotExists(
                "SL",
                "Sick Leave",
                0.5,
                10.0
        );

        createLeaveTypeIfNotExists(
                "EL",
                "Earned Leave",
                1.5,
                18.0
        );

        createLeaveTypeIfNotExists(
                "COMP_OFF",
                "Comp Off",
                0.0,
                10.0
        );

        createLeaveTypeIfNotExists(
                "LOP",
                "Loss Of Pay",
                0.0,
                0.0
        );

        createLeaveTypeIfNotExists(
                "MATERNITY",
                "Maternity Leave",
                0.0,
                180.0
        );

        createLeaveTypeIfNotExists(
                "PATERNITY",
                "Paternity Leave",
                0.0,
                15.0
        );

        createLeaveTypeIfNotExists(
                "MARRIAGE",
                "Marriage Leave",
                0.0,
                15.0
        );

        createLeaveTypeIfNotExists(
                "BEREAVEMENT",
                "Bereavement Leave",
                0.0,
                5.0
        );

        createLeaveTypeIfNotExists(
                "WFH",
                "Work From Home",
                0.0,
                50.0
        );

        createLeaveTypeIfNotExists(
                "FLOATER",
                "Floater Holiday",
                0.0,
                5.0
        );
    }

    private void createLeaveTypeIfNotExists(
            String code,
            String name,
            Double monthlyCredit,
            Double maxBalance
    ) {

        boolean exists = leaveTypeMasterRepository
                .findByLeaveCode(code)
                .isPresent();

        if (!exists) {

            LeaveTypeMaster leaveType = new LeaveTypeMaster();

            leaveType.setLeaveCode(code);
            leaveType.setLeaveName(name);
            leaveType.setMonthlyCredit(monthlyCredit);
            leaveType.setMaxBalance(maxBalance);

            leaveType.setCarryForward(true);
            leaveType.setIsPaid(!code.equals("LOP"));
            leaveType.setRequiresApproval(true);
            leaveType.setActive(true);

            leaveTypeMasterRepository.save(leaveType);

            System.out.println("Created Leave Type: " + code);
        }
    }
}