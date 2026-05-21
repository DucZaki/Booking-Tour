-- QR check-in token per booking
ALTER TABLE `dat_cho`
  ADD COLUMN `ma_check_in` varchar(64) DEFAULT NULL,
  ADD COLUMN `checked_in_at` datetime(6) DEFAULT NULL;

ALTER TABLE `dat_cho`
  ADD UNIQUE KEY `uk_dat_cho_ma_check_in` (`ma_check_in`);
