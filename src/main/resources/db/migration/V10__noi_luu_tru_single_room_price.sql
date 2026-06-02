-- Giá phụ thu phòng đơn theo nơi lưu trú (idempotent — an toàn khi chạy lại sau lỗi Flyway)
SET @col_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'noi_luu_tru'
      AND COLUMN_NAME = 'gia_phong_don'
);

SET @ddl := IF(
    @col_exists = 0,
    'ALTER TABLE noi_luu_tru ADD COLUMN gia_phong_don DECIMAL(10,2) DEFAULT 500000.00',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE noi_luu_tru
SET gia_phong_don = 500000.00
WHERE gia_phong_don IS NULL;
