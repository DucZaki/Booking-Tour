ALTER TABLE ngay_khoi_hanh
    ADD COLUMN gio_tap_trung VARCHAR(5) NOT NULL DEFAULT '06:00' COMMENT 'Giờ tập trung tại điểm đón (HH:mm)';

UPDATE ngay_khoi_hanh
SET gio_tap_trung = COALESCE(NULLIF(TRIM(gio_bay_di), ''), '06:00')
WHERE gio_tap_trung = '06:00'
  AND gio_bay_di IS NOT NULL
  AND TRIM(gio_bay_di) <> ''
  AND gio_bay_di <> 'N/A';
