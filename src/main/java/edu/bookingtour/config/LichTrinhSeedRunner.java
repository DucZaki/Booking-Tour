package edu.bookingtour.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(name = "app.runners.seed-lich-trinh", havingValue = "true", matchIfMissing = false)
public class LichTrinhSeedRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(LichTrinhSeedRunner.class);
    private static final int MIN_EXPECTED_ROWS = 45;

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public LichTrinhSeedRunner(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
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
        } catch (Exception e) {
            log.error("LichTrinhSeedRunner thất bại, bỏ qua để không ảnh hưởng khởi động.", e);
        }
    }
}
