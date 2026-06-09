package edu.bookingtour.scheduler;

import edu.bookingtour.service.TourManifestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.trip-lifecycle.enabled", havingValue = "true", matchIfMissing = true)
public class TripLifecycleScheduler {

    private static final Logger log = LoggerFactory.getLogger(TripLifecycleScheduler.class);

    private final TourManifestService tourManifestService;

    public TripLifecycleScheduler(TourManifestService tourManifestService) {
        this.tourManifestService = tourManifestService;
    }

    @Scheduled(cron = "${app.trip-lifecycle.completion-cron:0 */5 * * * *}")
    public void runLifecycleJobs() {
        try {
            int cancelled = tourManifestService.cancelEmptyDeparturesPastBookingDeadline();
            if (cancelled > 0) {
                log.info("Auto-cancelled {} empty departure(s) past booking deadline", cancelled);
            }
            int started = tourManifestService.autoStartDueDepartures();
            if (started > 0) {
                log.info("Auto-started {} departure(s)", started);
            }
            int completed = tourManifestService.completeDueDepartures();
            if (completed > 0) {
                log.info("Auto-completed {} departure(s)", completed);
            }
        } catch (Exception ex) {
            log.warn("Trip lifecycle job failed: {}", ex.getMessage());
        }
    }
}
