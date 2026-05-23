-- Chạy trên MySQL (local hoặc Railway) khi:
-- - Flyway báo "failed migration to version 6", hoặc
-- - Import dump đã có cột V6 nhưng flyway_schema_history chỉ tới V5
USE booking_tour;

-- Xóa bản ghi V6 failed (nếu có)
DELETE FROM flyway_schema_history WHERE installed_rank = 6;

-- Đánh dấu V6 đã chạy thành công (schema đã có từ dump / chạy tay trước đó)
INSERT INTO flyway_schema_history
  (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES
  (6, '6', 'ma giam gia promo columns', 'SQL', 'V6__ma_giam_gia_promo_columns.sql', NULL, 'repair', NOW(3), 0, 1);
