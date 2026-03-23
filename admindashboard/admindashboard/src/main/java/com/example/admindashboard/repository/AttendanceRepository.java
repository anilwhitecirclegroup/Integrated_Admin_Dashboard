package com.example.admindashboard.repository;

import com.example.admindashboard.model.Attendance;
import com.example.admindashboard.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // Find today's attendance for a specific user (to prevent double check-in)
    Optional<Attendance> findByUserAndDate(User user, LocalDate date);

    // The "IgnoreCase" automatically fixes the Pending vs PENDING bug!
    List<Attendance> findByStatusIgnoreCase(String status);

    // Notice we changed this to "ApprovalStatus" to match your Service file!
    List<Attendance> findByApprovalStatusIgnoreCase(String approvalStatus);

    long countByDate(LocalDate date);

    List<Attendance> findByUserAndStatusIgnoreCaseOrderByIdDesc(User user, String status);

    List<Attendance> findByUserAndApprovalStatusIgnoreCaseOrderByIdDesc(User user, String approvalStatus);

    // Fetch all attendance records for a user, newest first
    List<Attendance> findByUserOrderByIdDesc(User user);

    List<Attendance> findByStatus(String status);


    // --- NEW: ADVANCED SEARCH & FILTERING FOR ATTENDANCE REPORT PAGE ---
    // 1. For the UI Table (Returns Paginated Data)
    @Query("SELECT a FROM Attendance a WHERE " +
            "(:fromDate IS NULL OR a.weekStartDate >= :fromDate) AND " +
            "(:toDate IS NULL OR a.weekStartDate <= :toDate) AND " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(a.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.user.username) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Attendance> searchAttendance(
            @Param("fromDate") String fromDate,
            @Param("toDate") String toDate,
            @Param("keyword") String keyword,
            Pageable pageable);

    // 2. For the Excel Export (Returns Full List Data)
    @Query("SELECT a FROM Attendance a WHERE " +
            "(:fromDate IS NULL OR a.weekStartDate >= :fromDate) AND " +
            "(:toDate IS NULL OR a.weekStartDate <= :toDate) AND " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(a.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.user.username) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Attendance> findAttendanceBySearchCriteria(
            @Param("fromDate") String fromDate,
            @Param("toDate") String toDate,
            @Param("keyword") String keyword);
}