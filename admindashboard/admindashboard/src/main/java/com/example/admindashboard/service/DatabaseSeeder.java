package com.example.admindashboard.service;

import com.example.admindashboard.model.*;
import com.example.admindashboard.repository.ClientRepository;
import com.example.admindashboard.repository.PermissionRepository;
import com.example.admindashboard.repository.RoleRepository;
import com.example.admindashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private com.example.admindashboard.repository.HospitalRepository hospitalRepository;

    // NEW: Injecting JdbcTemplate to fix the database constraint
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=========================================================");
        System.out.println("🔄 Checking database for roles, permissions, and default accounts...");

        // ==========================================
        // DATABASE MIGRATION FIX
        // ==========================================
        try {
            System.out.println("🛠️ Patching legacy database constraints...");
            // This drops the strict NOT NULL rule from the old unused role column
            jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN role DROP NOT NULL");
            System.out.println("✅ Legacy role column successfully patched.");
        } catch (Exception e) {
            // If the column doesn't exist or is already patched, we just safely ignore it
            System.out.println("⚡ Legacy role column is already patched or removed.");
        }

        // 0. ENSURE ROLES EXIST FIRST
        Role adminRole = getOrCreateRole("ADMIN");
        Role employeeRole = getOrCreateRole("EMPLOYEE");
        Role clientRole = getOrCreateRole("CLIENT");
        Role itAdminRole = getOrCreateRole("IT_ADMIN");
        Role superAdminRole = getOrCreateRole("SUPER_ADMIN");
        Role hrAdminRole = getOrCreateRole("HR_ADMIN");
        Role hrExecutiveRole = getOrCreateRole("HR_EXECUTIVE");
        Role managerRole = getOrCreateRole("MANAGER");
        Role financeRole = getOrCreateRole("FINANCE");
        Role recruiterRole = getOrCreateRole("RECRUITER");

        // ==========================================
        // THE MATRIX MAPPING (Strict 1:1 with BRD)
        // ==========================================
        System.out.println("🔑 Forging Granular Permissions based on RBAC Document...");

        // CORE ROUTING PERMISSION
        Permission adminDashView = getOrCreatePermission("admin_dashboard_view");

        // EMPLOYEE MANAGEMENT
        Permission empView = getOrCreatePermission("employee_view");
        Permission empCreate = getOrCreatePermission("employee_create");
        Permission empEdit = getOrCreatePermission("employee_edit");
        Permission empDelete = getOrCreatePermission("employee_delete");

        // ATTENDANCE & LEAVE
        Permission attView = getOrCreatePermission("attendance_view");
        Permission attMark = getOrCreatePermission("attendance_mark");
        Permission attEdit = getOrCreatePermission("attendance_edit");
        Permission attApprove = getOrCreatePermission("attendance_approve");
        Permission attExport = getOrCreatePermission("attendance_export");
        Permission leaveApply = getOrCreatePermission("leave_apply");
        Permission leaveApprove = getOrCreatePermission("leave_approve");
        Permission leaveView = getOrCreatePermission("leave_view");
        Permission leaveConfig = getOrCreatePermission("leave_configure");

        // PAYROLL & FINANCE
        Permission payrollView = getOrCreatePermission("payroll_view");
        Permission payrollGen = getOrCreatePermission("payroll_generate");
        Permission payrollEdit = getOrCreatePermission("payroll_edit");
        Permission payslipView = getOrCreatePermission("payslip_view");
        Permission payslipDownload = getOrCreatePermission("payslip_download");

        // RECRUITMENT & APPRAISAL
        Permission recPost = getOrCreatePermission("recruitment_postjob");
        Permission recManage = getOrCreatePermission("recruitment_candidate_manage");
        Permission recInterview = getOrCreatePermission("recruitment_interview");
        Permission recOffer = getOrCreatePermission("recruitment_offer");
        Permission appCreate = getOrCreatePermission("appraisal_create");
        Permission appRate = getOrCreatePermission("appraisal_rate");
        Permission appView = getOrCreatePermission("appraisal_view");

        // ASSETS, DOCS & SETTINGS
        Permission assetAdd = getOrCreatePermission("asset_add");
        Permission assetAssign = getOrCreatePermission("asset_assign");
        Permission assetView = getOrCreatePermission("asset_view");
        Permission docUpload = getOrCreatePermission("doc_upload");
        Permission docView = getOrCreatePermission("doc_view");
        Permission docDelete = getOrCreatePermission("doc_delete");
        Permission settingCompany = getOrCreatePermission("settings_manage_company");
        Permission settingHolidays = getOrCreatePermission("settings_manage_holidays");
        Permission settingShifts = getOrCreatePermission("settings_manage_shifts");
        Permission settingRoles = getOrCreatePermission("settings_manage_roles");

        System.out.println("🔗 Assigning Permissions to Roles...");

        // 1. SUPER ADMIN (Full Access)
        superAdminRole.setPermissions(new HashSet<>(Arrays.asList(
                adminDashView, empView, empCreate, empEdit, empDelete, attView, attMark, attEdit, attApprove, attExport,
                leaveApply, leaveApprove, leaveView, leaveConfig, payrollView, payrollGen, payrollEdit, payslipView, payslipDownload,
                recPost, recManage, recInterview, recOffer, appCreate, appRate, appView, assetAdd, assetAssign, assetView,
                docUpload, docView, docDelete, settingCompany, settingHolidays, settingShifts, settingRoles
        )));
        roleRepository.save(superAdminRole);

        // 2. HR ADMIN (HR Ops, No Payroll Gen, Cannot config roles)
        hrAdminRole.setPermissions(new HashSet<>(Arrays.asList(
                adminDashView, empView, empCreate, empEdit, empDelete, attView, attMark, attEdit, attApprove, attExport,
                leaveApply, leaveApprove, leaveView, leaveConfig, payrollView, payrollEdit, payslipView, payslipDownload,
                recPost, recManage, recInterview, recOffer, appCreate, appRate, appView, assetAssign, assetView, docUpload, docView, docDelete,
                settingHolidays, settingShifts
        )));
        roleRepository.save(hrAdminRole);

        // 3. HR EXECUTIVE (No delete rights, no config rights, can assign assets)
        hrExecutiveRole.setPermissions(new HashSet<>(Arrays.asList(
                adminDashView, empView, empCreate, empEdit, attView, attMark, attEdit, attApprove, attExport,
                leaveApply, leaveApprove, leaveView, payslipView, payslipDownload, appView, assetAssign, assetView, docUpload, docView
        )));
        roleRepository.save(hrExecutiveRole);

        // 4. MANAGER (Team approvals, Interviews, Appraisal Ratings)
        managerRole.setPermissions(new HashSet<>(Arrays.asList(
                adminDashView, empView, attView, attApprove, attExport, leaveApply, leaveApprove, leaveView,
                payslipView, payslipDownload, recInterview, appRate, appView, assetView, docView
        )));
        roleRepository.save(managerRole);

        // 5. FINANCE (Payroll generation, structure edits)
        financeRole.setPermissions(new HashSet<>(Arrays.asList(
                adminDashView, empView, leaveApply, payrollView, payrollGen, payrollEdit, payslipView, payslipDownload
        )));
        roleRepository.save(financeRole);

        // 6. RECRUITER (Job posting, candidates, offers)
        recruiterRole.setPermissions(new HashSet<>(Arrays.asList(
                adminDashView, empView, leaveApply, payslipView, payslipDownload, recPost, recManage, recInterview, recOffer
        )));
        roleRepository.save(recruiterRole);

        // 7. IT ADMIN (Hardware, Systems)
        itAdminRole.setPermissions(new HashSet<>(Arrays.asList(
                adminDashView, empView, leaveApply, payslipView, payslipDownload, assetAdd, assetAssign, assetView, docUpload, docView
        )));
        roleRepository.save(itAdminRole);

        // 8. STANDARD EMPLOYEE (Self-Service only, NO adminDashView)
        employeeRole.setPermissions(new HashSet<>(Arrays.asList(
                empView, attMark, leaveApply, leaveView, payslipView, payslipDownload, appView, assetView, docView
        )));
        roleRepository.save(employeeRole);

        // ==========================================
        // THE LEGACY DATA PATCHER
        // ==========================================
        System.out.println("🔍 Scanning for legacy users missing a Role Object...");
        List<User> usersWithoutRoles = userRepository.findAll().stream()
                .filter(u -> u.getRole() == null)
                .toList();

        for (User u : usersWithoutRoles) {
            if (u.getUsername().toUpperCase().startsWith("ADM")) {
                u.setRole(superAdminRole);
            } else if (u.getUsername().toUpperCase().startsWith("CLI")) {
                u.setRole(clientRole);
            } else {
                u.setRole(employeeRole);
            }
            userRepository.save(u);
            System.out.println("🔧 UPGRADED: Assigned Role to legacy user -> " + u.getUsername());
        }

        // OVERRIDE: UPGRADE ADM001 TO SUPER_ADMIN
        Optional<User> admOpt = userRepository.findByUsername("ADM001");
        if (admOpt.isPresent()) {
            User adm = admOpt.get();
            if (adm.getRole() != null && "ADMIN".equals(adm.getRole().getRoleName())) {
                adm.setRole(superAdminRole);
                userRepository.save(adm);
                System.out.println("🚀 Upgraded ADM001 from legacy ADMIN to SUPER_ADMIN!");
            }
        }

        // 1. SEED ADMIN ACCOUNT
        if (userRepository.findByUsername("ADM001").isEmpty()) {
            User admin = new User();
            admin.setUsername("ADM001");
            admin.setFullName("System Administrator");
            admin.setEmail("admin@whitecirclegroup.com");
            admin.setRole(superAdminRole);
            admin.setPassword("{noop}admin123");

            EmployeeProfile adminProfile = new EmployeeProfile();
            adminProfile.setJoiningDate(LocalDate.now());
            adminProfile.setUser(admin);
            admin.setEmployeeProfile(adminProfile);

            userRepository.save(admin);
            System.out.println("✅ Created Admin -> ID: ADM001 | Pass: admin123");
        }

        // 2. SEED CLIENT ACCOUNT (CLI001)
        User cli001User;
        if (userRepository.findByUsername("CLI001").isEmpty()) {
            User client = new User();
            client.setUsername("CLI001");
            client.setFullName("Acme Corp Client");
            client.setEmail("client@acmecorp.com");
            client.setRole(clientRole);
            client.setPassword("{noop}welcome123");
            cli001User = userRepository.save(client);
            System.out.println("✅ Created Client User -> ID: CLI001 | Pass: welcome123");
        } else {
            cli001User = userRepository.findByUsername("CLI001").get();
        }

        // 2.5 ENSURE PROPER 'CLIENT' TABLE RECORD EXISTS FOR CLI001
        if (clientRepository.findByUser_Username("CLI001").isEmpty()) {
            Client clientRecord = new com.example.admindashboard.model.Client();
            clientRecord.setUser(cli001User);
            clientRecord.setClientId("CLI001");
            clientRecord.setCompanyName("Acme Corporation");
            clientRecord.setDomain("Manufacturing & Logistics");
            clientRecord.setContactPerson("Acme Contact");
            clientRecord.setOfficialEmail("client@acmecorp.com");
            clientRecord.setPhoneNumber("+1-800-555-0199");
            clientRecord.setAccountStatus("Active Contract");
            clientRecord.setTeamLead("Rahul Verma");

            clientRepository.save(clientRecord);
            System.out.println("✅ Generated secure Client Table Profile for CLI001");
        }

        // ==========================================
        // REAL EMPLOYEE DATA SEEDING
        // ==========================================

        // Employee 1: Om Tripathi
        if (userRepository.findByUsername("EMP114").isEmpty()) {
            User emp1 = new User();
            emp1.setUsername("EMP114");
            emp1.setPassword("{noop}welcome123");
            emp1.setRole(employeeRole);
            emp1.setFullName("Om Tripathi");
            emp1.setEmail("om.whitecirclegroup@gmail.com");

            EmployeeProfile profile1 = new EmployeeProfile();
            profile1.setDesignation("Marketing Lead");
            profile1.setMobileNumber("6265147016");
            profile1.setExperience("05 Years");
            profile1.setJoiningDate(LocalDate.of(2023, 3, 5));
            profile1.setBusinessUnit("Bhopal");
            profile1.setProjectName("Saller");
            profile1.setProjectCode("S65");
            profile1.setReportingManager("Virendra Tiwari");
            profile1.setCustomerName("Manish Rastogi");
            profile1.setWorkLocation("Bhopal");
            profile1.setCity("Bhopal");
            profile1.setBuHrContact("7509759872");
            profile1.setDob(LocalDate.of(1992, 8, 24));

            profile1.setUser(emp1);
            emp1.setEmployeeProfile(profile1);
            userRepository.save(emp1);
            System.out.println("✅ Created Employee -> ID: EMP114 | Name: Om Tripathi");
        }

        // Employee 2: Om Singrore
        if (userRepository.findByUsername("EMP187").isEmpty()) {
            User emp2 = new User();
            emp2.setUsername("EMP187");
            emp2.setPassword("{noop}welcome123");
            emp2.setRole(employeeRole);
            emp2.setFullName("Om Singrore");
            emp2.setEmail("omsingrorewhitecirclegroup@gmail.com");

            EmployeeProfile profile2 = new EmployeeProfile();
            profile2.setDesignation("Project Manager");
            profile2.setMobileNumber("+91 75879 57916");
            profile2.setExperience("3.5 Years");
            profile2.setJoiningDate(LocalDate.of(2022, 5, 7));
            profile2.setBusinessUnit("Bhopal");
            profile2.setProjectName("Index");
            profile2.setProjectCode("IX47");
            profile2.setReportingManager("Sudeep Radhakrishnan");
            profile2.setCustomerName("Neha Tabbu");
            profile2.setWorkLocation("Bhopal");
            profile2.setCity("Bhopal");
            profile2.setBuHrContact("7509759872");
            profile2.setDob(LocalDate.of(1989, 4, 16));

            profile2.setUser(emp2);
            emp2.setEmployeeProfile(profile2);
            userRepository.save(emp2);
            System.out.println("✅ Created Employee -> ID: EMP187 | Name: Om Singrore");
        }

        // Employee 3: Saumya Katare
        if (userRepository.findByUsername("EMP129").isEmpty()) {
            User emp3 = new User();
            emp3.setUsername("EMP129");
            emp3.setPassword("{noop}welcome123");
            emp3.setRole(employeeRole);
            emp3.setFullName("Saumya Katare");
            emp3.setEmail("saumyawhitecirclegroup@gmail.com");

            EmployeeProfile profile3 = new EmployeeProfile();
            profile3.setDesignation("IOS Developer");
            profile3.setMobileNumber("+91 7724051300");
            profile3.setExperience("4.8 Years");
            profile3.setJoiningDate(LocalDate.of(2022, 5, 7));
            profile3.setBusinessUnit("Bhopal");
            profile3.setProjectName("Sald");
            profile3.setProjectCode("SD09");
            profile3.setReportingManager("Manikanata");
            profile3.setCustomerName("Gairy Singh Nahar");
            profile3.setWorkLocation("Raipur");
            profile3.setCity("Raipur");
            profile3.setBuHrContact("7509759872");
            profile3.setDob(LocalDate.of(1995, 5, 8));

            profile3.setUser(emp3);
            emp3.setEmployeeProfile(profile3);
            userRepository.save(emp3);
            System.out.println("✅ Created Employee -> ID: EMP129 | Name: Saumya Katare");
        }

        // ==========================================
        // RBAC TESTING ACCOUNTS
        // ==========================================

        // Test Account 1: HR Admin
        if (userRepository.findByUsername("EMP201").isEmpty()) {
            User hrUser = new User();
            hrUser.setUsername("EMP201");
            hrUser.setPassword("{noop}welcome123");
            hrUser.setRole(hrAdminRole);
            hrUser.setFullName("Priya Sharma");
            hrUser.setEmail("priya.hr@whitecirclegroup.com");

            EmployeeProfile hrProfile = new EmployeeProfile();
            hrProfile.setDesignation("HR Director");
            hrProfile.setJoiningDate(LocalDate.of(2021, 1, 15));
            hrProfile.setBusinessUnit("Head Office");

            hrProfile.setUser(hrUser);
            hrUser.setEmployeeProfile(hrProfile);
            userRepository.save(hrUser);
            System.out.println("✅ Created HR Admin -> ID: EMP201");
        }

        // Test Account 2: Finance / Payroll
        if (userRepository.findByUsername("EMP301").isEmpty()) {
            User finUser = new User();
            finUser.setUsername("EMP301");
            finUser.setPassword("{noop}welcome123");
            finUser.setRole(financeRole);
            finUser.setFullName("Rahul Verma");
            finUser.setEmail("rahul.finance@whitecirclegroup.com");

            EmployeeProfile finProfile = new EmployeeProfile();
            finProfile.setDesignation("Payroll Manager");
            finProfile.setJoiningDate(LocalDate.of(2020, 6, 10));
            finProfile.setBusinessUnit("Head Office");

            finProfile.setUser(finUser);
            finUser.setEmployeeProfile(finProfile);
            userRepository.save(finUser);
            System.out.println("✅ Created Finance Admin -> ID: EMP301");
        }

        // Test Account 3: Recruiter
        if (userRepository.findByUsername("EMP401").isEmpty()) {
            User recUser = new User();
            recUser.setUsername("EMP401");
            recUser.setPassword("{noop}welcome123");
            recUser.setRole(recruiterRole);
            recUser.setFullName("Sneha Gupta");
            recUser.setEmail("sneha.talent@whitecirclegroup.com");

            EmployeeProfile recProfile = new EmployeeProfile();
            recProfile.setDesignation("Lead Recruiter");
            recProfile.setJoiningDate(LocalDate.of(2022, 11, 1));
            recProfile.setBusinessUnit("Head Office");

            recProfile.setUser(recUser);
            recUser.setEmployeeProfile(recProfile);
            userRepository.save(recUser);
            System.out.println("✅ Created Recruiter -> ID: EMP401");
        }

        // ==========================================
        // NETWORK HOSPITALS SEEDING
        // ==========================================
        System.out.println("🏥 Checking for Network Hospitals...");
        if (hospitalRepository.count() == 0) {
            System.out.println("⚙️ Seeding dummy network hospitals for testing...");

            Hospital h1 = new Hospital();
            h1.setName("Apollo Hospitals");
            h1.setLocation("Saket, New Delhi");
            h1.setContactNumber("+91-11-29871090");
            h1.setCashless(true);○
            h1.setEmergency24x7(true);

            Hospital h2 = new Hospital();
            h2.setName("Fortis Escorts Heart Institute");
            h2.setLocation("Okhla, New Delhi");
            h2.setContactNumber("+91-11-47135000");
            h2.setCashless(true);
            h2.setEmergency24x7(false);

            Hospital h3 = new Hospital();
            h3.setName("Max Super Speciality Hospital");
            h3.setLocation("Vaishali, Ghaziabad");
            h3.setContactNumber("+91-120-4188000");
            h3.setCashless(false);
            h3.setEmergency24x7(true);

            Hospital h4 = new Hospital();
            h4.setName("Orange City Hospital & Research Institute");
            h4.setLocation("Nagpur, Maharashtra");
            h4.setContactNumber("+91-712-6634800");
            h4.setCashless(true);
            h4.setEmergency24x7(true);

            Hospital h5 = new Hospital();
            h5.setName("Bansal Hospital");
            h5.setLocation("Bhopal, Madhya Pradesh");
            h5.setContactNumber("+91-755-4086000");
            h5.setCashless(true);
            h5.setEmergency24x7(false);

            Hospital h6 = new Hospital();
            h6.setName("Care CHL Hospital");
            h6.setLocation("Indore, Madhya Pradesh");
            h6.setContactNumber("+91-731-4774444");
            h6.setCashless(false);
            h6.setEmergency24x7(false);

            hospitalRepository.saveAll(Arrays.asList(h1, h2, h3, h4, h5, h6));
            System.out.println("✅ Successfully seeded 6 Network Hospitals.");
        } else {
            System.out.println("⚡ Network Hospitals already exist. Skipping seed.");
        }

        System.out.println("=========================================================");
    }

    private Role getOrCreateRole(String roleName) {
        return roleRepository.findByRoleName(roleName).orElseGet(() -> {
            Role newRole = new Role();
            newRole.setRoleName(roleName);
            return roleRepository.save(newRole);
        });
    }

    private Permission getOrCreatePermission(String permissionName) {
        return permissionRepository.findByPermissionName(permissionName).orElseGet(() -> {
            Permission newPermission = new Permission();
            newPermission.setPermissionName(permissionName);
            return permissionRepository.save(newPermission);
        });
    }
}