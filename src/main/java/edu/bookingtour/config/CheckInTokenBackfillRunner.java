package edu.bookingtour.config;

import edu.bookingtour.entity.DatCho;
import edu.bookingtour.repo.DatChoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Gán mã QR cho đơn cũ chưa có ma_check_in. */
@Component
public class CheckInTokenBackfillRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CheckInTokenBackfillRunner.class);

    private final DatChoRepository datChoRepository;

    public CheckInTokenBackfillRunner(DatChoRepository datChoRepository) {
        this.datChoRepository = datChoRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<DatCho> missing = datChoRepository.findAll().stream()
                .filter(d -> d.getMaCheckIn() == null || d.getMaCheckIn().isBlank())
                .toList();
        if (missing.isEmpty()) {
            return;
        }
        for (DatCho d : missing) {
            d.setMaCheckIn(UUID.randomUUID().toString().replace("-", ""));
        }
        datChoRepository.saveAll(missing);
        log.info("Check-in QR: backfill {} đơn chưa có mã.", missing.size());
    }
}
