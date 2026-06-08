package edu.bookingtour.service;

import edu.bookingtour.entity.DatCho;
import edu.bookingtour.repo.DatChoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DepartureReminderService {

    private static final Logger log = LoggerFactory.getLogger(DepartureReminderService.class);

    private final DatChoRepository datChoRepository;
    private final EmailService emailService;

    @Value("${app.mail.departure-reminder.days-before:1}")
    private int daysBefore;

    @Value("${app.mail.departure-reminder.enabled:true}")
    private boolean enabled;

    public DepartureReminderService(DatChoRepository datChoRepository, EmailService emailService) {
        this.datChoRepository = datChoRepository;
        this.emailService = emailService;
    }

    @Transactional
    public int sendDueReminders() {
        if (!enabled) {
            return 0;
        }
        LocalDate departureDate = LocalDate.now().plusDays(daysBefore);
        List<DatCho> bookings = datChoRepository.findDueForDepartureReminder(departureDate);
        int sent = 0;
        for (DatCho booking : bookings) {
            if (emailService.sendDepartureReminder(booking)) {
                booking.setDepartureReminderSentAt(LocalDateTime.now());
                datChoRepository.save(booking);
                sent++;
            }
        }
        if (sent > 0) {
            log.info("Sent {} departure reminder email(s) for departures on {}", sent, departureDate);
        }
        return sent;
    }
}
