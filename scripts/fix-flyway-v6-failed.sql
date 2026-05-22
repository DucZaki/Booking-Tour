-- Chạy trên MySQL local (booking_tour) nếu Flyway báo "failed migration to version 6"
-- Sau đó: ./mvnw flyway:repair (hoặc xóa dòng failed và chạy lại app)

DELETE FROM flyway_schema_history WHERE version = '6' AND success = 0;
