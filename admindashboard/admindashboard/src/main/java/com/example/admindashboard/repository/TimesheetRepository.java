package com.example.admindashboard.repository;

import com.example.admindashboard.model.Timesheet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.admindashboard.model.User;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimesheetRepository extends JpaRepository<Timesheet, Long> {

    List<Timesheet> findByUser(User user);

    // Used by Employee to see their own history
    List<Timesheet> findByUserOrderByWeekStartDateDesc(User user);

    // Used by Admin to see lists of Pending/Approved/Rejected
    List<Timesheet> findByStatus(String status);

    List<Timesheet> findByStatusIgnoreCase(String status);

    List<Timesheet> findByUserAndWeekStartDate(User user, LocalDate weekStartDate);

    List<Timesheet> findByUserAndStatusIgnoreCaseOrderByIdDesc(User user, String status);

    @Query("SELECT t FROM Timesheet t WHERE t.weekStartDate BETWEEN :from AND :to " +
            "AND (LOWER(t.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(t.user.username) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Timesheet> findTimesheetsBySearchCriteria(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("keyword") String keyword
    );

    @Query("SELECT t FROM Timesheet t JOIN t.user u WHERE " +
            "(LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "t.weekStartDate >= :fromDate AND t.weekStartDate <= :toDate")
    Page<Timesheet> searchTimesheets(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("keyword") String keyword,
            Pageable pageable);

    //  Default fetch for Date Range only (when search is empty)
    Page<Timesheet> findByWeekStartDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    List<Timesheet> findByWeekStartDateBetween(LocalDate from, LocalDate to);


}