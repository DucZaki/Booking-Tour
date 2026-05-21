-- Support multiple departure points per tour.

CREATE TABLE IF NOT EXISTS `chuyen_di_diem_don` (
  `chuyen_di_id` int NOT NULL,
  `diem_don_id` int NOT NULL,
  PRIMARY KEY (`chuyen_di_id`, `diem_don_id`),
  CONSTRAINT `fk_cddd_chuyen_di` FOREIGN KEY (`chuyen_di_id`) REFERENCES `chuyen_di` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_cddd_diem_don` FOREIGN KEY (`diem_don_id`) REFERENCES `diem_don` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Store selected departure for a booking (so invoices/history reflect correct departure).
ALTER TABLE `dat_cho`
  ADD COLUMN `id_diem_don` int DEFAULT NULL;

ALTER TABLE `dat_cho`
  ADD KEY `idx_dat_cho_id_diem_don` (`id_diem_don`);

ALTER TABLE `dat_cho`
  ADD CONSTRAINT `fk_dat_cho_diem_don` FOREIGN KEY (`id_diem_don`) REFERENCES `diem_don` (`id`);

-- Backfill rules:
-- 1) Tours to Ha Noi: departures = HCM + Da Nang
-- 2) Tours to Da Nang or HCM: departures = Ha Noi
-- 3) Others: allow all 3 departures
INSERT IGNORE INTO chuyen_di_diem_don (chuyen_di_id, diem_don_id)
SELECT cd.id, ddn.id
FROM chuyen_di cd
JOIN diem_den den ON den.id = cd.id_diem_den
JOIN diem_don ddn ON ddn.ten IN ('Hồ Chí Minh', 'TP. Hồ Chí Minh', 'Đà Nẵng')
WHERE den.thanh_pho LIKE '%Hà Nội%';

INSERT IGNORE INTO chuyen_di_diem_don (chuyen_di_id, diem_don_id)
SELECT cd.id, ddn.id
FROM chuyen_di cd
JOIN diem_den den ON den.id = cd.id_diem_den
JOIN diem_don ddn ON ddn.ten = 'Hà Nội'
WHERE den.thanh_pho LIKE '%Đà Nẵng%'
   OR den.thanh_pho LIKE '%Hồ Chí Minh%'
   OR den.thanh_pho LIKE '%TP%Hồ Chí Minh%'
   OR den.thanh_pho LIKE '%TP.HCM%'
   OR den.thanh_pho LIKE '%Sài Gòn%'
   OR den.thanh_pho LIKE '%Sai Gon%';

INSERT IGNORE INTO chuyen_di_diem_don (chuyen_di_id, diem_don_id)
SELECT cd.id, ddn.id
FROM chuyen_di cd
JOIN diem_den den ON den.id = cd.id_diem_den
JOIN diem_don ddn ON ddn.ten IN ('Hà Nội', 'Đà Nẵng', 'Hồ Chí Minh', 'TP. Hồ Chí Minh')
WHERE den.thanh_pho NOT LIKE '%Hà Nội%'
  AND den.thanh_pho NOT LIKE '%Đà Nẵng%'
  AND den.thanh_pho NOT LIKE '%Hồ Chí Minh%'
  AND den.thanh_pho NOT LIKE '%TP%Hồ Chí Minh%'
  AND den.thanh_pho NOT LIKE '%TP.HCM%'
  AND den.thanh_pho NOT LIKE '%Sài Gòn%'
  AND den.thanh_pho NOT LIKE '%Sai Gon%';

-- Keep legacy single departure column consistent: set it to the first allowed departure if it is invalid.
UPDATE chuyen_di cd
LEFT JOIN chuyen_di_diem_don cddd ON cddd.chuyen_di_id = cd.id AND cddd.diem_don_id = cd.id_diem_don
SET cd.id_diem_don = (
  SELECT x.diem_don_id FROM chuyen_di_diem_don x WHERE x.chuyen_di_id = cd.id ORDER BY x.diem_don_id LIMIT 1
)
WHERE cd.id_diem_don IS NULL OR cddd.diem_don_id IS NULL;
