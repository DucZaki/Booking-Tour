-- Align ma_giam_gia with MaGiamGia entity (loai giam, gia tri, ap dung tour)
ALTER TABLE `ma_giam_gia`
  ADD COLUMN `loai_giam` varchar(20) DEFAULT 'PERCENT' AFTER `mo_ta`,
  ADD COLUMN `gia_tri_giam` decimal(14, 0) DEFAULT NULL AFTER `loai_giam`,
  ADD COLUMN `gia_toi_thieu` decimal(14, 0) DEFAULT NULL AFTER `phan_tram_giam`,
  ADD COLUMN `ap_dung_tat_ca` tinyint(1) NOT NULL DEFAULT 1 AFTER `gia_toi_thieu`;

CREATE TABLE IF NOT EXISTS `ma_giam_gia_tour` (
  `ma_giam_gia_id` int NOT NULL,
  `chuyen_di_id` int NOT NULL,
  PRIMARY KEY (`ma_giam_gia_id`, `chuyen_di_id`),
  CONSTRAINT `fk_mggt_ma` FOREIGN KEY (`ma_giam_gia_id`) REFERENCES `ma_giam_gia` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
