package com.example.admindashboard.repository;

import com.example.admindashboard.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    // 1. AUTHENTICATION
    Optional<User> findByUsername(String username);

    // 2.This is used by DashboardController to count Total Employees/Clients
    long countByRole(String role);

    // 3. EXISTING LIST METHODS (For Dropdowns & Old Reports)
    List<User> findByRole(String role);

    List<User> findByRoleOrderByUsernameAsc(String role);

    // Used for the search bar in the simplified report view
    List<User> findByRoleAndFullNameContainingIgnoreCase(String role, String keyword);

    List<User> findByFullNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(String fullName, String username);

    //  4. NEW PAGINATION METHODS (For the New Report Dashboard)

    // Fetch page of employees (for the default view)
    Page<User> findByRole(String role, Pageable pageable);

    // Search for employees with Pagination (Custom Query)
    @Query("SELECT u FROM User u WHERE u.role = 'EMPLOYEE' AND (" +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<User> searchEmployees(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.businessUnit) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchByKeyword(@Param("keyword") String keyword);

    //  5. BIRTHDAY WIDGET
    // This method joins the User table with the EmployeeProfile table to find today's birthdays
    @Query("SELECT u FROM User u JOIN u.employeeProfile ep WHERE " +
            "EXTRACT(MONTH FROM ep.dob) = EXTRACT(MONTH FROM CURRENT_DATE) AND " +
            "EXTRACT(DAY FROM ep.dob) = EXTRACT(DAY FROM CURRENT_DATE)")
    List<User> findByBirthdayToday();

}