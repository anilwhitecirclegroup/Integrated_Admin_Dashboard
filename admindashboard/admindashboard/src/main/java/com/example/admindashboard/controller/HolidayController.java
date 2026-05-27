package com.example.admindashboard.controller;

import com.example.admindashboard.model.Holiday;
import com.example.admindashboard.repository.HolidayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/holidays")
public class HolidayController {

    @Autowired
    private HolidayRepository holidayRepository;

    // 1. GET UPCOMING HOLIDAYS (Next 5 from today)
    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingHolidays() {
        try {
            List<Holiday> holidays = holidayRepository
                    .findByHolidayDateGreaterThanEqualAndActiveTrueOrderByHolidayDateAsc(LocalDate.now());

            List<Map<String, Object>> result = holidays.stream()
                    .limit(5)
                    .map(this::toMap)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching holidays: " + e.getMessage());
        }
    }

    // 2. GET ALL HOLIDAYS FOR CURRENT YEAR
    @GetMapping("/all")
    public ResponseEntity<?> getAllHolidays() {
        try {
            int currentYear = LocalDate.now().getYear();
            List<Holiday> holidays = holidayRepository.findByYearAndActiveTrue(currentYear);

            List<Map<String, Object>> result = holidays.stream()
                    .map(this::toMap)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching holidays: " + e.getMessage());
        }
    }

    // 3. GET HOLIDAYS FOR A SPECIFIC MONTH
    @GetMapping("/month")
    public ResponseEntity<?> getHolidaysForMonth(
            @RequestParam("month") int month,
            @RequestParam("year") int year) {
        try {
            LocalDate start = LocalDate.of(year, month, 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

            List<Holiday> holidays = holidayRepository
                    .findByHolidayDateBetweenAndActiveTrue(start, end);

            List<Map<String, Object>> result = holidays.stream()
                    .map(this::toMap)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching holidays: " + e.getMessage());
        }
    }

    // Helper: Convert Holiday entity to a clean map for frontend
    private Map<String, Object> toMap(Holiday h) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("date", h.getHolidayDate().toString());
        map.put("name", h.getHolidayName());
        map.put("dayOfWeek", h.getDayOfWeek());
        map.put("type", h.getHolidayType());
        return map;
    }
}
