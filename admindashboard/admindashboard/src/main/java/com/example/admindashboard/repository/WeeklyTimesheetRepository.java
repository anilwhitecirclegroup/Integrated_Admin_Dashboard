package com.example.admindashboard.repository;

import com.example.admindashboard.model.WeeklyTimesheet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface WeeklyTimesheetRepository extends JpaRepository<WeeklyTimesheet, Long> {

    java.util.Optional<WeeklyTimesheet> findByUserAndWeekStartDate(com.example.admindashboard.model.User user, LocalDate weekStartDate);

    List<WeeklyTimesheet> findByUserOrderByWeekStartDateDesc(com.example.admindashboard.model.User user);

    List<WeeklyTimesheet> findByUserAndStatusIgnoreCaseOrderByIdDesc(com.example.admindashboard.model.User user, String status);


    // 1. For the UI Table (Returns Paginated Data)
    @Query("SELECT w FROM WeeklyTimesheet w WHERE " +
            "(cast(:fromDate as date) IS NULL OR w.weekStartDate >= :fromDate) AND " +
            "(cast(:toDate as date) IS NULL OR w.weekStartDate <= :toDate) AND " +
            "(cast(:keyword as text) IS NULL OR :keyword = '' OR " +
            "LOWER(w.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(w.user.username) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<WeeklyTimesheet> searchTimesheets(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("keyword") String keyword,
            Pageable pageable);

    // 2. For the Excel Export (Returns Full List Data)
    @Query("SELECT w FROM WeeklyTimesheet w WHERE " +
            "(cast(:fromDate as date) IS NULL OR w.weekStartDate >= :fromDate) AND " +
            "(cast(:toDate as date) IS NULL OR w.weekStartDate <= :toDate) AND " +
            "(cast(:keyword as text) IS NULL OR :keyword = '' OR " +
            "LOWER(w.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(w.user.username) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<WeeklyTimesheet> findTimesheetsBySearchCriteria(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("keyword") String keyword);
}