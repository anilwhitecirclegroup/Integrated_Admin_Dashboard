package com.example.admindashboard.repository;

import com.example.admindashboard.model.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    List<Holiday> findByYearAndActiveTrue(Integer year);

    List<Holiday> findByHolidayDateGreaterThanEqualAndActiveTrueOrderByHolidayDateAsc(LocalDate date);

    List<Holiday> findByHolidayDateBetweenAndActiveTrue(LocalDate start, LocalDate end);

    Optional<Holiday> findByHolidayDateAndActiveTrue(LocalDate date);
}
