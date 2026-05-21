-- Initial schema for BookingTour (MySQL)
-- Generated from the project's MySQL dump, cleaned for Flyway.

CREATE TABLE `diem_den` (
  `id` int NOT NULL AUTO_INCREMENT,
  `thanh_pho` varchar(255) DEFAULT NULL,
  `quoc_gia` varchar(255) DEFAULT NULL,
  `chau_luc` varchar(255) DEFAULT NULL,
  `hinh_anh` varchar(255) DEFAULT NULL,
  `noi_bat` tinyint(1) DEFAULT '0',
  `mo_ta` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `diem_don` (
  `id` int NOT NULL AUTO_INCREMENT,
  `ten` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `ma_giam_gia` (
  `id` int NOT NULL AUTO_INCREMENT,
  `ma` varchar(50) DEFAULT NULL,
  `mo_ta` varchar(255) DEFAULT NULL,
  `phan_tram_giam` int DEFAULT NULL,
  `ngay_bat_dau` date DEFAULT NULL,
  `ngay_ket_thuc` date DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `nguoi_dung` (
  `id` int NOT NULL AUTO_INCREMENT,
  `ten_dang_nhap` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `mat_khau` varchar(255) DEFAULT NULL,
  `vai_tro` tinytext,
  `ngay_tao` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `ho_ten` varchar(255) DEFAULT NULL,
  `number` varchar(255) DEFAULT NULL,
  `provider` varchar(255) DEFAULT NULL,
  `anh_dai_dien` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ten_dang_nhap` (`ten_dang_nhap`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `contacts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `content` text,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `guest_number` int DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `phone_number` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `tittle` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `noi_luu_tru` (
  `id` int NOT NULL AUTO_INCREMENT,
  `ten` varchar(255) DEFAULT NULL,
  `loai` varchar(100) DEFAULT NULL,
  `dia_chi` varchar(255) DEFAULT NULL,
  `gia` decimal(10,2) DEFAULT NULL,
  `id_diem_den` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id_diem_den` (`id_diem_den`),
  CONSTRAINT `noi_luu_tru_ibfk_1` FOREIGN KEY (`id_diem_den`) REFERENCES `diem_den` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `phuong_tien` (
  `id` int NOT NULL AUTO_INCREMENT,
  `loai` tinytext,
  `hang` varchar(255) DEFAULT NULL,
  `id_diem_den` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id_diem_den` (`id_diem_den`),
  CONSTRAINT `phuong_tien_ibfk_1` FOREIGN KEY (`id_diem_den`) REFERENCES `diem_den` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `chuyen_di` (
  `id` int NOT NULL AUTO_INCREMENT,
  `tieu_de` varchar(255) DEFAULT NULL,
  `mo_ta` varchar(255) DEFAULT NULL,
  `gia` decimal(10,2) DEFAULT NULL,
  `ngay_khoi_hanh` date DEFAULT NULL,
  `ngay_ket_thuc` date DEFAULT NULL,
  `id_diem_den` int DEFAULT NULL,
  `id_phuong_tien` int DEFAULT NULL,
  `id_noi_luu_tru` int DEFAULT NULL,
  `noi_bat` tinyint(1) DEFAULT '0',
  `hinh_anh` varchar(255) DEFAULT NULL,
  `highlight` text,
  `id_diem_don` int DEFAULT NULL,
  `trang_thai` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id_diem_den` (`id_diem_den`),
  KEY `id_phuong_tien` (`id_phuong_tien`),
  KEY `id_noi_luu_tru` (`id_noi_luu_tru`),
  KEY `FKf7y4cdxg2tmd2m7p0s4jwspxo` (`id_diem_don`),
  CONSTRAINT `FKf7y4cdxg2tmd2m7p0s4jwspxo` FOREIGN KEY (`id_diem_don`) REFERENCES `diem_don` (`id`),
  CONSTRAINT `chuyen_di_ibfk_1` FOREIGN KEY (`id_diem_den`) REFERENCES `diem_den` (`id`),
  CONSTRAINT `chuyen_di_ibfk_2` FOREIGN KEY (`id_phuong_tien`) REFERENCES `phuong_tien` (`id`),
  CONSTRAINT `chuyen_di_ibfk_3` FOREIGN KEY (`id_noi_luu_tru`) REFERENCES `noi_luu_tru` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `ngay_khoi_hanh` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nam` int DEFAULT NULL,
  `ngay` date NOT NULL,
  `thang` int DEFAULT NULL,
  `id_chuyen_di` int NOT NULL,
  `gia_ve` double DEFAULT NULL,
  `gia_ve_di` double DEFAULT NULL,
  `gia_ve_ve` double DEFAULT NULL,
  `gio_bay_di` varchar(255) DEFAULT NULL,
  `gio_bay_ve` varchar(255) DEFAULT NULL,
  `gio_den_di` varchar(255) DEFAULT NULL,
  `gio_den_ve` varchar(255) DEFAULT NULL,
  `ma_chuyen_bay_di` varchar(255) DEFAULT NULL,
  `ma_chuyen_bay_ve` varchar(255) DEFAULT NULL,
  `ngay_ve` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK4ag0i3l2vuvf5mc52pw3nk1mo` (`id_chuyen_di`,`ngay`),
  CONSTRAINT `FKde2b6vwklfoia4jx3pmb9r2m6` FOREIGN KEY (`id_chuyen_di`) REFERENCES `chuyen_di` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `lich_trinh` (
  `id` int NOT NULL AUTO_INCREMENT,
  `ngay_thu` int DEFAULT NULL,
  `nghi_dem` varchar(255) DEFAULT NULL,
  `noi_dung` text,
  `so_bua_an` varchar(255) DEFAULT NULL,
  `tieu_de` varchar(255) DEFAULT NULL,
  `hoat_dong_chinh` varchar(500) DEFAULT NULL,
  `hinh_anh` varchar(500) DEFAULT NULL,
  `tour_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKpnc5i4nnynpvv6u68h24chauk` (`tour_id`),
  CONSTRAINT `FKpnc5i4nnynpvv6u68h24chauk` FOREIGN KEY (`tour_id`) REFERENCES `chuyen_di` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `dat_cho` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_nguoi_dung` int DEFAULT NULL,
  `id_chuyen_di` int DEFAULT NULL,
  `so_luong` int DEFAULT NULL,
  `ngay_dat` date DEFAULT NULL,
  `trang_thai` varchar(50) DEFAULT NULL,
  `id_ma_giam_gia` int DEFAULT NULL,
  `dia_chi` varchar(500) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `ghi_chu` text,
  `ho_ten` varchar(255) DEFAULT NULL,
  `so_dien_thoai` varchar(20) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `tong_gia` double DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id_nguoi_dung` (`id_nguoi_dung`),
  KEY `id_chuyen_di` (`id_chuyen_di`),
  KEY `id_ma_giam_gia` (`id_ma_giam_gia`),
  CONSTRAINT `dat_cho_ibfk_1` FOREIGN KEY (`id_nguoi_dung`) REFERENCES `nguoi_dung` (`id`),
  CONSTRAINT `dat_cho_ibfk_2` FOREIGN KEY (`id_chuyen_di`) REFERENCES `chuyen_di` (`id`),
  CONSTRAINT `dat_cho_ibfk_3` FOREIGN KEY (`id_ma_giam_gia`) REFERENCES `ma_giam_gia` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `cho_xac_nhan` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_dat_cho` int DEFAULT NULL,
  `trang_thai` tinytext,
  `ngay_cap_nhat` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `id_dat_cho` (`id_dat_cho`),
  CONSTRAINT `cho_xac_nhan_ibfk_1` FOREIGN KEY (`id_dat_cho`) REFERENCES `dat_cho` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `danh_gia` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_chuyen_di` int NOT NULL,
  `id_nguoi_dung` int NOT NULL,
  `diem` int DEFAULT NULL,
  `binh_luan` varchar(255) DEFAULT NULL,
  `ngay_danh_gia` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `id_chuyen_di` (`id_chuyen_di`),
  KEY `id_nguoi_dung` (`id_nguoi_dung`),
  CONSTRAINT `danh_gia_ibfk_1` FOREIGN KEY (`id_chuyen_di`) REFERENCES `chuyen_di` (`id`),
  CONSTRAINT `danh_gia_ibfk_2` FOREIGN KEY (`id_nguoi_dung`) REFERENCES `nguoi_dung` (`id`),
  CONSTRAINT `danh_gia_chk_1` CHECK ((`diem` between 1 and 5))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `quan_ly_cho` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_chuyen_di` int DEFAULT NULL,
  `tong_so_cho` int DEFAULT NULL,
  `da_dat` int DEFAULT NULL,
  `con_lai` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id_chuyen_di` (`id_chuyen_di`),
  CONSTRAINT `quan_ly_cho_ibfk_1` FOREIGN KEY (`id_chuyen_di`) REFERENCES `chuyen_di` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `yeu_thich` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_nguoi_dung` int DEFAULT NULL,
  `id_chuyen_di` int DEFAULT NULL,
  `ngay_them` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `id_nguoi_dung` (`id_nguoi_dung`),
  KEY `id_chuyen_di` (`id_chuyen_di`),
  CONSTRAINT `yeu_thich_ibfk_1` FOREIGN KEY (`id_nguoi_dung`) REFERENCES `nguoi_dung` (`id`),
  CONSTRAINT `yeu_thich_ibfk_2` FOREIGN KEY (`id_chuyen_di`) REFERENCES `chuyen_di` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
