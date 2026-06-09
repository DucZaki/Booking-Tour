-- Gán HDV theo từng điểm xuất phát (Hà Nội / HCM / Đà Nẵng ... mỗi nơi 1 HDV).
-- Điểm xuất phát có guide riêng sẽ ưu tiên; nếu để trống thì dùng HDV cấp ngày khởi hành.

ALTER TABLE ngay_khoi_hanh_diem_don
    ADD COLUMN guide_id INT NULL;

ALTER TABLE ngay_khoi_hanh_diem_don
    ADD CONSTRAINT fk_nkhdd_guide FOREIGN KEY (guide_id) REFERENCES nguoi_dung (id) ON DELETE SET NULL;

-- Kế thừa HDV đang gán ở cấp ngày khởi hành cho mọi điểm xuất phát hiện có
UPDATE ngay_khoi_hanh_diem_don dd
JOIN ngay_khoi_hanh nkh ON nkh.id = dd.ngay_khoi_hanh_id
SET dd.guide_id = nkh.guide_id
WHERE nkh.guide_id IS NOT NULL;
