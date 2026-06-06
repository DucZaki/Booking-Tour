-- Vai trò HDV / điều hành: phân công đoàn + trạng thái check-in chi tiết

ALTER TABLE ngay_khoi_hanh
    ADD COLUMN guide_id INT NULL,
    ADD COLUMN trang_thai_doan VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED';

ALTER TABLE ngay_khoi_hanh
    ADD CONSTRAINT fk_nkh_guide FOREIGN KEY (guide_id) REFERENCES nguoi_dung (id) ON DELETE SET NULL;

ALTER TABLE dat_cho
    ADD COLUMN checkin_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN so_giay_to VARCHAR(50) NULL,
    ADD COLUMN so_ghe VARCHAR(20) NULL,
    ADD COLUMN so_phong VARCHAR(20) NULL;

UPDATE dat_cho SET checkin_status = 'CHECKED_IN' WHERE checked_in_at IS NOT NULL;
