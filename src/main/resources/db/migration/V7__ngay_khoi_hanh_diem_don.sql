-- Vé / phương tiện theo từng điểm đón trên mỗi ngày khởi hành
CREATE TABLE IF NOT EXISTS `ngay_khoi_hanh_diem_don` (
  `id` int NOT NULL AUTO_INCREMENT,
  `ngay_khoi_hanh_id` int NOT NULL,
  `diem_don_id` int NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `active_from` datetime DEFAULT NULL,
  `active_to` datetime DEFAULT NULL,
  `gia_ve_di` double DEFAULT NULL,
  `ma_chuyen_bay_di` varchar(50) DEFAULT NULL,
  `gio_bay_di` varchar(20) DEFAULT NULL,
  `gio_den_di` varchar(20) DEFAULT NULL,
  `gia_ve_ve` double DEFAULT NULL,
  `ma_chuyen_bay_ve` varchar(50) DEFAULT NULL,
  `gio_bay_ve` varchar(20) DEFAULT NULL,
  `gio_den_ve` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_nkh_diem_don` (`ngay_khoi_hanh_id`, `diem_don_id`),
  KEY `idx_nkhdd_diem_don` (`diem_don_id`),
  CONSTRAINT `fk_nkhdd_nkh` FOREIGN KEY (`ngay_khoi_hanh_id`) REFERENCES `ngay_khoi_hanh` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_nkhdd_diem_don` FOREIGN KEY (`diem_don_id`) REFERENCES `diem_don` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Backfill: mỗi ngày KH × mỗi điểm đón của tour (copy vé từ bản ghi cũ nếu trùng điểm đón mặc định)
INSERT IGNORE INTO `ngay_khoi_hanh_diem_don`
  (`ngay_khoi_hanh_id`, `diem_don_id`, `active`,
   `gia_ve_di`, `ma_chuyen_bay_di`, `gio_bay_di`, `gio_den_di`,
   `gia_ve_ve`, `ma_chuyen_bay_ve`, `gio_bay_ve`, `gio_den_ve`)
SELECT
  nkh.id,
  dd.diem_don_id,
  1,
  nkh.gia_ve_di,
  nkh.ma_chuyen_bay_di,
  nkh.gio_bay_di,
  nkh.gio_den_di,
  nkh.gia_ve_ve,
  nkh.ma_chuyen_bay_ve,
  nkh.gio_bay_ve,
  nkh.gio_den_ve
FROM `ngay_khoi_hanh` nkh
JOIN `chuyen_di_diem_don` dd ON dd.chuyen_di_id = nkh.id_chuyen_di;

INSERT IGNORE INTO `ngay_khoi_hanh_diem_don`
  (`ngay_khoi_hanh_id`, `diem_don_id`, `active`,
   `gia_ve_di`, `ma_chuyen_bay_di`, `gio_bay_di`, `gio_den_di`,
   `gia_ve_ve`, `ma_chuyen_bay_ve`, `gio_bay_ve`, `gio_den_ve`)
SELECT
  nkh.id,
  cd.id_diem_don,
  1,
  nkh.gia_ve_di,
  nkh.ma_chuyen_bay_di,
  nkh.gio_bay_di,
  nkh.gio_den_di,
  nkh.gia_ve_ve,
  nkh.ma_chuyen_bay_ve,
  nkh.gio_bay_ve,
  nkh.gio_den_ve
FROM `ngay_khoi_hanh` nkh
JOIN `chuyen_di` cd ON cd.id = nkh.id_chuyen_di
WHERE cd.id_diem_don IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `ngay_khoi_hanh_diem_don` x
    WHERE x.ngay_khoi_hanh_id = nkh.id AND x.diem_don_id = cd.id_diem_don
  );
