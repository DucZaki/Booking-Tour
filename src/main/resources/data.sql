-- =============================================
-- DATA.SQL - Dữ liệu khởi tạo cho BookingTour
-- File này sẽ tự động chạy khi ứng dụng khởi động
-- Sử dụng INSERT IGNORE để tránh lỗi duplicate
-- =============================================

-- =============================================
-- 1. ĐIỂM ĐẾN (diem_den)
-- =============================================
INSERT IGNORE INTO diem_den (id, thanh_pho, quoc_gia, chau_luc, noi_bat, mo_ta) VALUES
(1, 'Sapa', 'Việt Nam', 'Châu Á', 1, 'Thành phố sương mù với ruộng bậc thang tuyệt đẹp'),
(2, 'Hạ Long', 'Việt Nam', 'Châu Á', 1, 'Vịnh Hạ Long - Di sản thiên nhiên thế giới'),
(3, 'Đà Nẵng', 'Việt Nam', 'Châu Á', 1, 'Thành phố đáng sống nhất Việt Nam'),
(4, 'Huế', 'Việt Nam', 'Châu Á', 0, 'Cố đô với di sản văn hóa phong phú'),
(5, 'Phú Quốc', 'Việt Nam', 'Châu Á', 1, 'Đảo ngọc phía Nam'),
(6, 'Cần Thơ', 'Việt Nam', 'Châu Á', 0, 'Thủ phủ miền Tây sông nước'),
(7, 'Bangkok', 'Thái Lan', 'Châu Á', 1, 'Thủ đô Thái Lan sôi động'),
(8, 'Singapore', 'Singapore', 'Châu Á', 1, 'Quốc đảo sư tử'),
(9, 'Seoul', 'Hàn Quốc', 'Châu Á', 1, 'Thủ đô Hàn Quốc hiện đại'),
(10, 'Paris', 'Pháp', 'Châu Âu', 1, 'Kinh đô ánh sáng'),
(11, 'Berlin', 'Đức', 'Châu Âu', 0, 'Thủ đô nước Đức'),
(12, 'New York', 'Mỹ', 'Châu Mỹ', 1, 'Thành phố không bao giờ ngủ'),
(13, 'Toronto', 'Canada', 'Châu Mỹ', 0, 'Thành phố đa văn hóa');

-- =============================================
-- 2. ĐIỂM ĐÓN (diem_don)
-- =============================================
INSERT IGNORE INTO diem_don (id, ten) VALUES
(1, 'Hà Nội'),
(2, 'Hồ Chí Minh'),
(3, 'Đà Nẵng');

-- =============================================
-- 3. PHƯƠNG TIỆN (phuong_tien)
-- =============================================
INSERT IGNORE INTO phuong_tien (id, loai, hang) VALUES
(1, 'Plane', 'Vietnam Airlines'),
(2, 'Plane', 'VietJet Air'),
(3, 'Plane', 'Bamboo Airways'),
(4, 'Bus', 'Xe khách cao cấp');

-- =============================================
-- 4. NƠI LƯU TRÚ (noi_luu_tru)
-- =============================================
INSERT IGNORE INTO noi_luu_tru (id, ten, loai, dia_chi, gia) VALUES
(1, 'Khách sạn Mường Thanh', 'Khách sạn 4 sao', 'Hà Nội', 1200000.00),
(2, 'Vinpearl Resort', 'Resort 5 sao', 'Phú Quốc', 3500000.00),
(3, 'Homestay Sapa', 'Homestay', 'Sapa, Lào Cai', 500000.00),
(4, 'Khách sạn Hạ Long', 'Khách sạn 3 sao', 'Quảng Ninh', 800000.00),
(5, 'InterContinental Đà Nẵng', 'Resort 5 sao', 'Đà Nẵng', 5000000.00);

-- =============================================
-- 5. TÀI KHOẢN ADMIN MẶC ĐỊNH
-- Mật khẩu: admin123 (đã mã hóa BCrypt)
-- =============================================
INSERT IGNORE INTO nguoi_dung (id, ten_dang_nhap, email, mat_khau, vai_tro, ho_ten)
VALUES (1, 'admin', 'admin@zakibooking.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'ROLE_ADMIN', 'Administrator');
