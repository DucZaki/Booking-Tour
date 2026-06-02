-- Lưu cơ cấu hành khách và phụ thu phòng đơn cho mỗi booking
ALTER TABLE `dat_cho`
    ADD COLUMN `so_nguoi_lon` int DEFAULT NULL,
    ADD COLUMN `so_tre_em` int DEFAULT NULL,
    ADD COLUMN `so_tre_nho` int DEFAULT NULL,
    ADD COLUMN `so_em_be` int DEFAULT NULL,
    ADD COLUMN `so_phong_don` int DEFAULT NULL,
    ADD COLUMN `phu_thu_phong_don` double DEFAULT NULL;
