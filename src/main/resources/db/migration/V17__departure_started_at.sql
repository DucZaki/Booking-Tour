-- Thời điểm bắt đầu thực tế (staff/admin hoặc tự động đến giờ tập trung)
ALTER TABLE ngay_khoi_hanh
    ADD COLUMN thoi_diem_bat_dau DATETIME NULL;

-- Dọn trạng thái IN_PROGRESS sai trên các ngày KH còn ở tương lai (vd. demo seed)
UPDATE ngay_khoi_hanh
SET trang_thai_doan = 'SCHEDULED',
    thoi_diem_bat_dau = NULL
WHERE trang_thai_doan = 'IN_PROGRESS'
  AND ngay > CURDATE();
