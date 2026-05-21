-- Link booking to selected departure date (for confirmation emails)
ALTER TABLE `dat_cho`
  ADD COLUMN `id_ngay_khoi_hanh` int DEFAULT NULL;

ALTER TABLE `dat_cho`
  ADD KEY `idx_dat_cho_id_ngay_khoi_hanh` (`id_ngay_khoi_hanh`);

ALTER TABLE `dat_cho`
  ADD CONSTRAINT `fk_dat_cho_ngay_khoi_hanh` FOREIGN KEY (`id_ngay_khoi_hanh`) REFERENCES `ngay_khoi_hanh` (`id`);
