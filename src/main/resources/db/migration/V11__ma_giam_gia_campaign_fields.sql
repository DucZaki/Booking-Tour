-- Mã giảm giá: chiến dịch, giới hạn lượt dùng, trần giảm (align distribute_project)
ALTER TABLE `ma_giam_gia`
  ADD COLUMN `giam_toi_da` decimal(14, 0) DEFAULT NULL AFTER `gia_toi_thieu`,
  ADD COLUMN `don_toi_thieu` decimal(14, 0) DEFAULT NULL AFTER `giam_toi_da`,
  ADD COLUMN `so_lan_dung_toi_da` int DEFAULT NULL AFTER `don_toi_thieu`,
  ADD COLUMN `so_lan_da_dung` int NOT NULL DEFAULT 0 AFTER `so_lan_dung_toi_da`,
  ADD COLUMN `active` tinyint(1) NOT NULL DEFAULT 1 AFTER `so_lan_da_dung`,
  ADD COLUMN `gioi_han_moi_user` int DEFAULT NULL AFTER `active`,
  ADD COLUMN `kieu_chien_dich` varchar(30) NOT NULL DEFAULT 'STANDARD' AFTER `gioi_han_moi_user`,
  ADD COLUMN `so_ngay_dat_truoc` int DEFAULT NULL AFTER `kieu_chien_dich`,
  ADD COLUMN `so_gio_last_minute` int DEFAULT NULL AFTER `so_ngay_dat_truoc`;

UPDATE `ma_giam_gia`
SET `active` = 1,
    `so_lan_da_dung` = 0,
    `kieu_chien_dich` = 'STANDARD'
WHERE `active` IS NULL OR `kieu_chien_dich` IS NULL OR `kieu_chien_dich` = '';
