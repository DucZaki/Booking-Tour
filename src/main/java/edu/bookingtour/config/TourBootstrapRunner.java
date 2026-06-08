package edu.bookingtour.config;

import edu.bookingtour.service.TourBootstrapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "tour.bootstrap.on-startup", havingValue = "true")
@Order(3)
public class TourBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TourBootstrapRunner.class);

    private final TourBootstrapService tourBootstrapService;

    public TourBootstrapRunner(TourBootstrapService tourBootstrapService) {
        this.tourBootstrapService = tourBootstrapService;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Đang kích hoạt tất cả tour (gia hạn + 3 ngày KH tháng hiện tại)...");
        TourBootstrapService.BootstrapResult result = tourBootstrapService.activateAllToursForCurrentMonth();
        log.info("Bootstrap xong: {} tour, +{} ngày khởi hành (T{}/{})",
                result.totalTours(), result.departuresAdded(), result.month(), result.year());
    }
}
