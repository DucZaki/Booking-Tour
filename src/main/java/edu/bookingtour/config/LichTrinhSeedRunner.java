package edu.bookingtour.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Nạp lịch trình mẫu cho tour 1–18 nếu DB còn thiếu dữ liệu demo.
 */
@Component
public class LichTrinhSeedRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(LichTrinhSeedRunner.class);
    private static final int EXPECTED_TOUR_COUNT = 18;
    private static final int MIN_EXPECTED_ROWS = 45;

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public LichTrinhSeedRunner(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Integer tourCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM chuyen_di WHERE id BETWEEN 1 AND 18",
                Integer.class);
        if (tourCount == null || tourCount < EXPECTED_TOUR_COUNT) {
            log.warn("Bỏ qua seed lich_trinh: cần {} tour cha id 1–18 trong chuyen_di, hiện có {}",
                    EXPECTED_TOUR_COUNT, tourCount == null ? 0 : tourCount);
            return;
        }

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM lich_trinh WHERE tour_id BETWEEN 1 AND 18",
                Integer.class);
        if (count != null && count >= MIN_EXPECTED_ROWS) {
            return;
        }
        log.info("Nạp dữ liệu lịch trình mẫu (hiện có {} dòng)...", count);
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("db/seed-lich-trinh.sql"));
        }
        Integer after = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM lich_trinh WHERE tour_id BETWEEN 1 AND 18",
                Integer.class);
        log.info("Hoàn tất seed lich_trinh: {} dòng cho tour 1–18", after);
    }
}
