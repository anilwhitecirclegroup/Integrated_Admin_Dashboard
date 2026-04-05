package com.example.admindashboard.repository;

import com.example.admindashboard.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    // 1. AUTHENTICATION
    Optional<User> findByUsername(String username);

    // 2. Count by looking inside the Role object
    long countByRole_RoleName(String roleName);

    // 3. EXISTING LIST METHODS (Updated to look inside Role object)
    List<User> findByRole_RoleName(String roleName);

    List<User> findByRole_RoleNameOrderByUsernameAsc(String roleName);

    List<User> findByRole_RoleNameAndFullNameContainingIgnoreCase(String roleName, String keyword);

    List<User> findByFullNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(String fullName, String username);

    // 4. NEW PAGINATION METHODS (Updated for the new architecture)
    Page<User> findByRole_RoleName(String roleName, Pageable pageable);

    // FIXED: Query now checks u.role.roleName
    @Query("SELECT u FROM User u WHERE u.role.roleName = 'EMPLOYEE' AND (" +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<User> searchEmployees(@Param("keyword") String keyword, Pageable pageable);

    // FIXED: Safely JOINs EmployeeProfile so it doesn't crash when searching businessUnit
    @Query("SELECT u FROM User u LEFT JOIN u.employeeProfile ep WHERE " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(ep.businessUnit) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchByKeyword(@Param("keyword") String keyword);

    // 5. BIRTHDAY WIDGET
    @Query("SELECT u FROM User u JOIN u.employeeProfile ep WHERE " +
            "EXTRACT(MONTH FROM ep.dob) = EXTRACT(MONTH FROM CURRENT_DATE) AND " +
            "EXTRACT(DAY FROM ep.dob) = EXTRACT(DAY FROM CURRENT_DATE)")
    List<User> findByBirthdayToday();

}