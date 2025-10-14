//package edu.bookingtour.config;
//
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//import javax.sql.DataSource;
//
//@Component
//public class DatabaseChecker implements CommandLineRunner {
//
//    private final DataSource dataSource;
//
//    public DatabaseChecker(DataSource dataSource) {
//        this.dataSource = dataSource;
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//        System.out.println("🔍 Kiểm tra kết nối database...");
//        try (var conn = dataSource.getConnection()) {
//            System.out.println("✅ Đã kết nối tới: " + conn.getMetaData().getURL());
//        } catch (Exception e) {
//            System.err.println("❌ Lỗi kết nối DB: " + e.getMessage());
//        }
//    }
//}
