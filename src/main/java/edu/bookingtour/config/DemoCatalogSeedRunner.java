package edu.bookingtour.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Nạp catalog demo (18 tour + điểm đến + phương tiện…) khi DB trống sau Flyway.
 */
@Component
@Order(1)
public class DemoCatalogSeedRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoCatalogSeedRunner.class);
    private static final int MIN_TOURS = 18;

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public DemoCatalogSeedRunner(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Integer tourCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM chuyen_di", Integer.class);
        if (tourCount != null && tourCount >= MIN_TOURS) {
            return;
        }
        log.warn("DB thiếu tour ({} dòng) — nạp seed catalog demo...", tourCount == null ? 0 : tourCount);
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("db/seed-catalog.sql"));
        }
        Integer after = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM chuyen_di", Integer.class);
        log.info("Hoàn tất seed catalog: {} tour trong chuyen_di", after);
    }
}
