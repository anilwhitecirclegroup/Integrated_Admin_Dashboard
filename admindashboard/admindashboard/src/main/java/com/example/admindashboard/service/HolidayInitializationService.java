package com.example.admindashboard.service;

import com.example.admindashboard.model.Holiday;
import com.example.admindashboard.repository.HolidayRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

@Service
public class HolidayInitializationService {

    private final HolidayRepository holidayRepository;

    public HolidayInitializationService(HolidayRepository holidayRepository) {
        this.holidayRepository = holidayRepository;
    }

    @PostConstruct
    public void initializeHolidays() {
        if (holidayRepository.count() > 0) {
            System.out.println("Holidays already seeded. Skipping initialization.");
            return;
        }

        System.out.println("Seeding 2026 Holiday Data...");

        // ══════════════ FIXED HOLIDAYS ══════════════
        createHoliday(2026, 1, 26, "Republic Day", "FIXED");

        // Holi (3 days)
        createHoliday(2026, 3, 2, "Holi Celebrations", "FIXED");
        createHoliday(2026, 3, 3, "Holi Celebrations", "FIXED");
        createHoliday(2026, 3, 4, "Holi Celebrations", "FIXED");

        createHoliday(2026, 4, 3, "Good Friday", "FIXED");
        createHoliday(2026, 5, 1, "Labour / Maharashtra Day", "FIXED");
        createHoliday(2026, 8, 15, "Independence Day", "FIXED");
        createHoliday(2026, 10, 2, "Gandhi Jayanti", "FIXED");
        createHoliday(2026, 10, 19, "Dussehra (Vijaya Dashami)", "FIXED");

        // Diwali (4 days)
        createHoliday(2026, 11, 6, "Diwali Holidays", "FIXED");
        createHoliday(2026, 11, 7, "Diwali Holidays", "FIXED");
        createHoliday(2026, 11, 8, "Diwali Holidays", "FIXED");
        createHoliday(2026, 11, 9, "Diwali Holidays", "FIXED");

        createHoliday(2026, 12, 25, "Christmas Day", "FIXED");

        // ══════════════ FLOATER HOLIDAYS ══════════════
        createHoliday(2026, 3, 14, "Maha Shivaratri", "FLOATER");
        createHoliday(2026, 4, 6, "Ram Navami", "FLOATER");
        createHoliday(2026, 7, 6, "Eid-ul-Adha", "FLOATER");
        createHoliday(2026, 8, 19, "Raksha Bandhan", "FLOATER");
        createHoliday(2026, 9, 14, "Ganesh Chaturthi", "FLOATER");
        createHoliday(2026, 11, 13, "Guru Nanak Jayanti", "FLOATER");

        System.out.println("Holiday seeding complete.");
    }

    private void createHoliday(int year, int month, int day, String name, String type) {
        Holiday holiday = new Holiday();
        LocalDate date = LocalDate.of(year, month, day);

        holiday.setHolidayDate(date);
        holiday.setHolidayName(name);
        holiday.setDayOfWeek(date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        holiday.setHolidayType(type);
        holiday.setYear(year);
        holiday.setActive(true);

        holidayRepository.save(holiday);
        System.out.println("  Created: " + name + " (" + date + ") - " + type);
    }
}
