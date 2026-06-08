package edu.bookingtour.config;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Nạp biến từ file {@code .env} (thư mục gốc dự án) trước khi Spring Boot khởi động.
 */
public final class EnvBootstrap {

    private EnvBootstrap() {
    }

    public static void loadDotEnv() {
        Dotenv dotenv = Dotenv.configure()
                .directory(".")
                .filename(".env")
                .ignoreIfMissing()
                .load();
        dotenv.entries().forEach(entry -> {
            String key = entry.getKey();
            if (System.getenv(key) == null && System.getProperty(key) == null) {
                System.setProperty(key, entry.getValue());
            }
        });
    }
}
