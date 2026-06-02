-- Chạy trong MySQL Workbench nếu app vẫn báo Flyway failed (sau khi đã sửa file migration):
-- 1) Xóa bản ghi migration thất bại
DELETE FROM flyway_schema_history WHERE success = 0;

-- 2) (Tuỳ chọn) Thêm cột nếu V10 dừng giữa chừng mà chưa có cột
-- ALTER TABLE noi_luu_tru ADD COLUMN gia_phong_don DECIMAL(10,2) DEFAULT 500000.00;

-- 3) Khởi động lại app — Flyway sẽ migrate lại
