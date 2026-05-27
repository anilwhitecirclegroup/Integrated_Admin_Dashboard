package com.example.admindashboard.controller;

import com.example.admindashboard.model.MediclaimDependent;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.EmployeeProfileRepository;
import com.example.admindashboard.repository.InsurancePolicyRepository;
import com.example.admindashboard.repository.MediclaimDependentRepository;
import com.example.admindashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.admindashboard.model.Mediclaim;
import com.example.admindashboard.repository.MediclaimRepository;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/employee/mediclaim")
public class MediclaimController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeProfileRepository profileRepository;

    @Autowired
    private InsurancePolicyRepository policyRepository;

    @Autowired
    private MediclaimDependentRepository dependentRepository;

    @Autowired
    private MediclaimRepository mediclaimRepository;

    @GetMapping("/auth")
    public String mediclaimAuth() { return "mediclaim-login"; }

    @GetMapping("/portal")
    public String mediclaimPortal(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/employee/mediclaim/auth";
        }

        Optional<User> userOpt = userRepository.findByUsername(principal.getName());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            model.addAttribute("user", user);

            // Fetch all claims for the user, ordered by newest first
            List<Mediclaim> claims = mediclaimRepository.findByUserOrderBySubmissionDateDesc(user);
            model.addAttribute("claims", claims);

            // Calculate how many claims are currently "Pending" for the badge
            long pendingCount = claims.stream().filter(c -> "Pending".equals(c.getStatus())).count();
            model.addAttribute("pendingCount", pendingCount);
        }

        return "mediclaim-dashboard";
    }

    @GetMapping("/policy")
    public String mediclaimPolicy() { return "mediclaim-policy"; }

    @GetMapping("/claim")
    public String mediclaimClaim() { return "mediclaim-claim"; }

    // 4. UPDATE THIS GET MAPPING FOR TRACKING
    @GetMapping("/track/{claimId}")
    public String mediclaimTrack(@PathVariable Long claimId, Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/employee/mediclaim/auth";
        }

        Optional<User> userOpt = userRepository.findByUsername(principal.getName());
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // 1. Fetch the specific active claim by ID
            Optional<Mediclaim> activeClaimOpt = mediclaimRepository.findById(claimId);
            if (activeClaimOpt.isPresent()) {
                model.addAttribute("activeClaim", activeClaimOpt.get());
            }

            // 2. Fetch all claims for the history table
            List<Mediclaim> allClaims = mediclaimRepository.findByUserOrderBySubmissionDateDesc(user);
            model.addAttribute("allClaims", allClaims);
        }

        return "mediclaim-track";
    }

    @GetMapping("/notifications")
    public String mediclaimNotifications() { return "mediclaim-notifications"; }

    // UPDATED: Profile mapping to fetch dynamic data
    @GetMapping("/profile")
    public String mediclaimProfile(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/employee/mediclaim/auth";
        }

        String username = principal.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            model.addAttribute("user", user);

            // Fetch Employee Profile using the smart repository method
            profileRepository.findByUser_Username(username).ifPresent(profile -> {
                model.addAttribute("profile", profile);
            });

            // Fetch Insurance Policy
            policyRepository.findByUser(user).ifPresent(policy -> {
                model.addAttribute("policy", policy);
            });

            // Fetch Dependents
            List<MediclaimDependent> dependents = dependentRepository.findByUser(user);
            model.addAttribute("dependents", dependents);
        }

        return "mediclaim-profile";
    }

    @GetMapping("/hospitals")
    public String mediclaimHospitals() { return "mediclaim-hospitals"; }

    @PostMapping("/verify-login")
    @ResponseBody
    public ResponseEntity<String> verifyMediclaimLogin(@RequestBody Map<String, String> payload, Principal principal) {
        String empId = payload.get("empId");
        String password = payload.get("password");

        if (principal == null || !principal.getName().equals(empId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Employee ID.");
        }

        Optional<User> userOpt = userRepository.findByUsername(empId);

        if (userOpt.isPresent()) {
            String dbPassword = userOpt.get().getPassword();

            // Logic: Compare {noop} password manually
            if (dbPassword != null && dbPassword.startsWith("{noop}")) {
                String rawDbPassword = dbPassword.replace("{noop}", "");
                if (password.equals(rawDbPassword)) {
                    return ResponseEntity.ok("Success");
                }
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password.");
    }

    // 1. UPDATE THIS GET MAPPING
    @GetMapping("/dependents")
    public String mediclaimDependents(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/employee/mediclaim/auth";
        }

        String username = principal.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            // Fetch real dependents from the database and send to the UI
            List<MediclaimDependent> dependents = dependentRepository.findByUser(userOpt.get());
            model.addAttribute("dependents", dependents);
        }

        return "mediclaim-dependents";
    }

    // 2. ADD THIS NEW POST MAPPING
    @PostMapping("/dependents/add")
    public String addDependent(@RequestParam("fullName") String fullName,
                               @RequestParam("relationship") String relationship,
                               @RequestParam("dob") String dob,
                               @RequestParam(value = "isCovered", required = false) String isCovered,
                               Principal principal) {

        if (principal == null) return "redirect:/employee/mediclaim/auth";

        Optional<User> userOpt = userRepository.findByUsername(principal.getName());

        if (userOpt.isPresent()) {
            MediclaimDependent dependent = new MediclaimDependent();
            dependent.setUser(userOpt.get());
            dependent.setFullName(fullName);
            dependent.setRelationship(relationship);
            dependent.setDob(java.time.LocalDate.parse(dob)); // Parses HTML5 yyyy-mm-dd format

            // If the checkbox is checked, it sends a value ("on"). If unchecked, it sends null.
            dependent.setCovered(isCovered != null);

            dependentRepository.save(dependent);
        }

        // Refresh the page to show the newly added dependent
        return "redirect:/employee/mediclaim/dependents";
    }

    // 3. ADD THIS POST MAPPING FOR CLAIM SUBMISSION
    @PostMapping("/claim/submit")
    @ResponseBody
    public ResponseEntity<String> submitClaim(
            @RequestParam("hospitalName") String hospitalName,
            @RequestParam("city") String city,
            @RequestParam("claimType") String claimType,
            @RequestParam("diagnosis") String diagnosis,
            @RequestParam("dateOfAdmission") String dateOfAdmission,
            @RequestParam("dateOfDischarge") String dateOfDischarge,
            @RequestParam("totalBill") Double totalBill,
            @RequestParam("claimAmount") Double claimAmount,
            @RequestParam(value = "remarks", required = false) String remarks,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Principal principal) {

        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");

        Optional<User> userOpt = userRepository.findByUsername(principal.getName());
        if (userOpt.isEmpty()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found");

        try {
            Mediclaim claim = new Mediclaim();
            claim.setUser(userOpt.get());
            claim.setHospitalName(hospitalName);
            claim.setCity(city);
            claim.setClaimType(claimType);
            claim.setDiagnosis(diagnosis);

            // Convert Strings to LocalDate
            claim.setDateOfAdmission(java.time.LocalDate.parse(dateOfAdmission));
            claim.setDateOfDischarge(java.time.LocalDate.parse(dateOfDischarge));

            claim.setTotalBill(totalBill);
            claim.setClaimAmount(claimAmount);
            claim.setRemarks(remarks);

            // Handle the file upload (Saves to a local 'uploads' directory)
            if (file != null && !file.isEmpty()) {
                // Generate a unique filename to prevent overwriting
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path uploadPath = Paths.get("uploads/");

                // Create directory if it doesn't exist
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Save the file and attach the filename to the database record
                Files.copy(file.getInputStream(), uploadPath.resolve(fileName));
                claim.setDocumentFilename(fileName);
            }

            mediclaimRepository.save(claim);
            return ResponseEntity.ok("Claim submitted successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to submit claim");
        }
    }


}