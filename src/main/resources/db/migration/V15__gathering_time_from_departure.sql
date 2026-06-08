-- Giờ tập trung = giờ bay/xe trừ 1h (bus) hoặc 2h (máy bay)
UPDATE ngay_khoi_hanh nkh
    JOIN chuyen_di cd ON cd.id = nkh.id_chuyen_di
    JOIN phuong_tien pt ON pt.id = cd.id_phuong_tien
SET nkh.gio_tap_trung = DATE_FORMAT(
        SUBTIME(
            STR_TO_DATE(CONCAT('2000-01-01 ', LEFT(TRIM(nkh.gio_bay_di), 5)), '%Y-%m-%d %H:%i'),
            CASE WHEN LOWER(pt.loai) = 'bus' THEN '01:00:00' ELSE '02:00:00' END
        ),
        '%H:%i')
WHERE nkh.gio_bay_di IS NOT NULL
  AND TRIM(nkh.gio_bay_di) <> ''
  AND UPPER(TRIM(nkh.gio_bay_di)) <> 'N/A'
  AND LEFT(TRIM(nkh.gio_bay_di), 5) REGEXP '^[0-9]{1,2}:[0-9]{2}$';
