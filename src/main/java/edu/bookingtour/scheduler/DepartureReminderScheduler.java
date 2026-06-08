package edu.bookingtour.scheduler;

import edu.bookingtour.service.DepartureReminderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.mail.departure-reminder.enabled", havingValue = "true", matchIfMissing = true)
public class DepartureReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(DepartureReminderScheduler.class);

    private final DepartureReminderService departureReminderService;

    public DepartureReminderScheduler(DepartureReminderService departureReminderService) {
        this.departureReminderService = departureReminderService;
    }

    /** Mặc định 8:00 sáng mỗi ngày — gửi nhắc trước ngày khởi hành (xem days-before). */
    @Scheduled(cron = "${app.mail.departure-reminder.cron:0 0 8 * * *}")
    public void sendDepartureReminders() {
        try {
            departureReminderService.sendDueReminders();
        } catch (Exception ex) {
            log.warn("Departure reminder job failed: {}", ex.getMessage());
        }
    }
}
