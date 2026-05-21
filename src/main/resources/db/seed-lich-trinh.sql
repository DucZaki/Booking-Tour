-- Seed lịch trình mẫu cho tour 1–18 (chạy lại an toàn)
DELETE FROM lich_trinh WHERE tour_id BETWEEN 1 AND 18;

INSERT INTO lich_trinh (tour_id, ngay_thu, tieu_de, so_bua_an, hoat_dong_chinh, hinh_anh, noi_dung, nghi_dem) VALUES

-- Tour 1: Hà Nội 3N2Đ
(1, 1, 'Hà Nội – Check-in & Phố cổ', 'Ăn trưa, tối', 'Khám phá Hồ Gươm và 36 phố phường',
 '/anh/chuyendi/hanoi.jpg',
 'Đón sân bay / ga, nhận phòng khách sạn
 Tham quan Hồ Gươm, Đền Ngọc Sơn
 Đi bộ phố cổ, thưởng thức Phở Thìn buổi tối',
 'Khách sạn 3–4 sao Hà Nội'),

(1, 2, 'Hà Nội – Văn hóa & Lịch sử', 'Ăn sáng, trưa, tối', 'Viếng Lăng Bác và Văn Miếu Quốc Tử Giám',
 '/anh/chuyendi/hanoi.jpg',
 'Viếng Lăng Chủ tịch Hồ Chí Minh (sáng sớm)
 Tham quan Văn Miếu – Quốc Tử Giám
 Trải nghiệm Xích lô quanh Hồ Tây
 Mua sắm chợ Đồng Xuân',
 'Khách sạn 3–4 sao Hà Nội'),

(1, 3, 'Hà Nội – Trả phòng & Tiễn khách', 'Ăn sáng, trưa', 'Mua quà lưu niệm và tiễn sân bay',
 '/anh/chuyendi/hanoi.jpg',
 'Ăn sáng tại khách sạn
 Tự do mua sắm quà lưu niệm
 Trả phòng, tiễn sân bay / ga',
 '—'),

-- Tour 2: Đà Nẵng - Hội An 4N3Đ
(2, 1, 'Đà Nẵng – Ngũ Hành Sơn', 'Ăn trưa, tối', 'Tham quan Ngũ Hành Sơn và biển Mỹ Khê',
 '/anh/diemden/danang.jpg',
 'Đón sân bay Đà Nẵng
 Tham quan Ngũ Hành Sơn, làng đá mỹ nghệ
 Tắm biển Mỹ Khê buổi chiều',
 'Khách sạn Đà Nẵng'),

(2, 2, 'Bà Nà Hills – Cầu Vàng', 'Ăn sáng, trưa, tối', 'Khám phá Bà Nà Hills và Cầu Vàng',
 '/anh/diemden/danang.jpg',
 'Cáp treo lên Bà Nà Hills
 Check-in Cầu Vàng, Fantasy Park
 Chụp ảnh trên mây, tự do vui chơi',
 'Khách sạn Đà Nẵng'),

(2, 3, 'Hội An – Phố cổ', 'Ăn sáng, trưa, tối', 'Dạo phố cổ Hội An và thả hoa đăng',
 '/anh/diemden/danang.jpg',
 'Di chuyển sang Hội An
 Tham quan Chùa Cầu, phố cổ
 Buổi tối: thả hoa đăng sông Hoài',
 'Khách sạn / homestay Hội An'),

(2, 4, 'Hội An – Tiễn khách', 'Ăn sáng, trưa', 'Mua sắm và tiễn sân bay',
 '/anh/diemden/danang.jpg',
 'Tự do mua đèn lồng, quà lưu niệm
 Trả phòng, tiễn sân bay Đà Nẵng',
 '—'),

-- Tour 3: Huế 2N1Đ
(3, 1, 'Huế – Đại Nội & Sông Hương', 'Ăn trưa, tối', 'Khám phá Đại Nội Huế và nghe ca Huế',
 '/anh/diemden/hue.jpg',
 'Tham quan Đại Nội Huế
 Chùa Thiên Mụ
 Tàu thuyền nghe ca Huế trên sông Hương',
 'Khách sạn Huế'),

(3, 2, 'Huế – Lăng tẩm & Tiễn', 'Ăn sáng, trưa', 'Tham quan lăng tẩm và ẩm thực cung đình',
 '/anh/diemden/hue.jpg',
 'Tham quan Lăng Khải Định / Tự Đức
 Thưởng thức cơm vua / ẩm thực Huế
 Tiễn ga / sân bay',
 '—'),

-- Tour 4: Hạ Long 2N1Đ
(4, 1, 'Hạ Long – Du thuyền', 'Ăn trưa, tối', 'Du ngoạn vịnh Hạ Long trên du thuyền',
 '/anh/diemden/halong.jpg',
 'Xe từ Hà Nội xuống cảng
 Lên du thuyền, ăn trưa trên tàu
 Tham quan hang Sửng Sốt, chèo kayak',
 'Du thuyền Hạ Long'),

(4, 2, 'Hạ Long – Bình minh & Về', 'Ăn sáng, trưa', 'Tập Tai-chi và trở về Hà Nội',
 '/anh/diemden/halong.jpg',
 'Tập Tai-chi trên boong tàu
 Tiếp tục tham quan hang động
 Trả tàu, về Hà Nội / tiễn khách',
 '—'),

-- Tour 5: Nha Trang 3N
(5, 1, 'Nha Trang – Biển & Tháp Bà', 'Ăn trưa, tối', 'Tắm biển và tham quan Tháp Bà Ponagar',
 '/anh/diemden/nhatrang.jpg',
 'Đón sân bay / ga, nhận phòng
 Tham quan Tháp Bà Ponagar
 Tắm biển Nha Trang buổi chiều',
 'Resort / khách sạn Nha Trang'),

(5, 2, 'Nha Trang – Đảo & Lặn', 'Ăn sáng, trưa, tối', 'Tour đảo Hòn Mun, lặn ngắm san hô',
 '/anh/diemden/nhatrang.jpg',
 'Cano ra đảo Hòn Mun
 Snorkeling / lặn ngắm san hô
 Ăn hải sản trên đảo',
 'Resort / khách sạn Nha Trang'),

(5, 3, 'Nha Trang – VinWonders & Tiễn', 'Ăn sáng, trưa', 'Vui chơi VinWonders và tiễn khách',
 '/anh/diemden/nhatrang.jpg',
 'VinWonders / tắm bùn khoáng nóng
 Mua sắm chợ Đầm
 Tiễn sân bay Cam Ranh',
 '—'),

-- Tour 6: Phú Quốc 4N
(6, 1, 'Phú Quốc – Bãi Sao & Sunset', 'Ăn trưa, tối', 'Khám phá bãi biển phía Nam đảo',
 '/anh/diemden/hue.jpg',
 'Đón sân bay Phú Quốc
 Bãi Sao / Bãi Khem
 Sunset Sanato / bar biển',
 'Resort Phú Quốc'),

(6, 2, 'Phú Quốc – Cáp treo Hòn Thơm', 'Ăn sáng, trưa, tối', 'Cáp treo vượt biển và Sun World Hòn Thơm',
 '/anh/diemden/hue.jpg',
 'Cáp treo Hòn Thơm dài nhất
 Sun World Hòn Thơm
 Tắm biển, chụp ảnh cầu vồm',
 'Resort Phú Quốc'),

(6, 3, 'Phú Quốc – Grand World', 'Ăn sáng, trưa, tối', 'Grand World và chợ đêm Dinh Cậu',
 '/anh/diemden/hue.jpg',
 'Grand World Phú Quốc
 Nhà tù Phú Quốc (tùy chọn)
 Chợ đêm Dinh Cậu',
 'Resort Phú Quốc'),

(6, 4, 'Phú Quốc – Tiễn khách', 'Ăn sáng, trưa', 'Mua nước mắm, tiêu và tiễn sân bay',
 '/anh/diemden/hue.jpg',
 'Mua đặc sản nước mắm, tiêu đen
 Trả phòng resort
 Tiễn sân bay',
 '—'),

-- Tour 7: Sa Pa 2N
(7, 1, 'Sa Pa – Bản Cát Cát', 'Ăn trưa, tối', 'Trekking bản Cát Cát và chợ tình',
 '/anh/diemden/sapa.jpg',
 'Xe từ Hà Nội lên Sa Pa
 Trekking bản Cát Cát
 Chợ tình / quảng trường Sa Pa',
 'Homestay / khách sạn Sa Pa'),

(7, 2, 'Sa Pa – Fansipan & Tiễn', 'Ăn sáng, trưa', 'Cáp treo Fansipan Legend',
 '/anh/diemden/sapa.jpg',
 'Cáp treo Fansipan Legend
 Ngắm núi Hoàng Liên Sơn
 Xe về Hà Nội / tiễn khách',
 '—'),

-- Tour 8: Cần Thơ 2N1Đ
(8, 1, 'Cần Thơ – Chợ nổi Cái Răng', 'Ăn trưa, tối', 'Chợ nổi sáng sớm và miệt vườn',
 '/anh/diemden/cantho.jpg',
 'Đón khách, nhận phòng
 Chợ nổi Cái Răng (4h–6h sáng)
 Tham quan miệt vườn, làm kẹo dừa',
 'Khách sạn Cần Thơ'),

(8, 2, 'Cần Thơ – Tiễn khách', 'Ăn sáng, trưa', 'Nhà cổ Bình Thủy và tiễn',
 '/anh/diemden/cantho.jpg',
 'Nhà cổ Bình Thủy
 Chùa Ông
 Tiễn sân bay / ga',
 '—'),

-- Tour 9: Beijing 4N
(9, 1, 'Bắc Kinh – Quảng trường Thiên An Môn', 'Ăn trưa, tối', 'Quảng trường Thiên An Môn và Cố Cung',
 '/anh/diemden/thuonghai.jpg',
 'Đón sân bay Bắc Kinh
 Quảng trường Thiên An Môn
 Tham quan Tử Cấm Thành (bên ngoài)',
 'Khách sạn Bắc Kinh'),

(9, 2, 'Bắc Kinh – Vạn Lý Trường Thành', 'Ăn sáng, trưa, tối', 'Leo Vạn Lý Trường Thành',
 '/anh/diemden/thuonghai.jpg',
 'Di chuyển ra Vạn Lý Trường Thành
 Tham quan đoạn Badaling
 Mua sắm đá cẩm thạch / đồ lưu niệm',
 'Khách sạn Bắc Kinh'),

(9, 3, 'Bắc Kinh – Di Tân Cung & Hutong', 'Ăn sáng, trưa, tối', 'Di Hoa Viên và phố Hutong',
 '/anh/diemden/thuonghai.jpg',
 'Di Hoa Viên
 Tham quan khu Hutong bằng xích lô
 Vịt quay Bắc Kinh',
 'Khách sạn Bắc Kinh'),

(9, 4, 'Bắc Kinh – Tiễn khách', 'Ăn sáng, trưa', 'Mua sắm và tiễn sân bay',
 '/anh/diemden/thuonghai.jpg',
 'Tự do mua sắm Wangfujing
 Tiễn sân bay',
 '—'),

-- Tour 10: Shanghai 3N
(10, 1, 'Thượng Hải – Bến Thượng Hải', 'Ăn trưa, tối', 'Dạo Bến Thượng Hải và phố Nam Kinh',
 '/anh/chuyendi/shanghai.jpg',
 'Đón sân bay Thượng Hải
 Bến Thượng Hải – phố Nam Kinh
 Ngắm skyline Pudong về đêm',
 'Khách sạn Thượng Hải'),

(10, 2, 'Thượng Hải – Disney / Tháp Minh Châu', 'Ăn sáng, trưa, tối', 'Tháp Minh Châu Phương Đông hoặc Disneyland',
 '/anh/chuyendi/shanghai.jpg',
 'Tháp Minh Châu Phương Đông
 Hoặc Shanghai Disneyland (tùy chọn)
 Phố đi bộ Tân Thiên Địa',
 'Khách sạn Thượng Hải'),

(10, 3, 'Thượng Hải – Tiễn khách', 'Ăn sáng, trưa', 'Yu Garden và tiễn sân bay',
 '/anh/chuyendi/shanghai.jpg',
 'Yu Garden – phố cổ Thượng Hải
 Mua sắm quà lưu niệm
 Tiễn sân bay',
 '—'),

-- Tour 11: Zhangjiajie 3N
(11, 1, 'Trương Gia Giới – Công viên Quốc gia', 'Ăn trưa, tối', 'Khám phá cột đá sao Hồng',
 '/anh/diemden/hue.jpg',
 'Đón sân bay / ga
 Công viên Quốc gia Trương Gia Giới
 Đi cáp treo Thiên Tử Sơn',
 'Khách sạn Wulingyuan'),

(11, 2, 'Trương Gia Giới – Cầu kính & Thang máy', 'Ăn sáng, trưa, tối', 'Cầu kính Đại Hiệp Cốc',
 '/anh/diemden/hue.jpg',
 'Cầu kính Đại Hiệp Cốc
 Thang máy Bách Lộ
 Ngắm hoàng hôn trên đỉnh núi',
 'Khách sạn Wulingyuan'),

(11, 3, 'Trương Gia Giới – Tiễn', 'Ăn sáng, trưa', 'Mua sắm và tiễn',
 '/anh/diemden/hue.jpg',
 'Tự do chụp ảnh cột đá
 Tiễn sân bay / ga',
 '—'),

-- Tour 12: Tokyo 5N
(12, 1, 'Tokyo – Asakusa & Skytree', 'Ăn trưa, tối', 'Chùa Senso-ji và Tokyo Skytree',
 '/anh/diemden/tokyo.jpg',
 'Đón sân bay Narita/Haneda
 Chùa Senso-ji Asakusa
 Tokyo Skytree ngắm cảnh',
 'Khách sạn Tokyo'),

(12, 2, 'Tokyo – Shibuya & Harajuku', 'Ăn sáng, trưa, tối', 'Giao lộ Shibuya và phố Harajuku',
 '/anh/diemden/tokyo.jpg',
 'Giao lộ Shibuya scramble
 Phố Takeshita Harajuku
 Meiji Shrine',
 'Khách sạn Tokyo'),

(12, 3, 'Tokyo – Disneyland', 'Ăn sáng, trưa, tối', 'Cả ngày Tokyo Disneyland',
 '/anh/diemden/tokyo.jpg',
 'Tokyo Disneyland cả ngày
 Xem parade và pháo hoa (tùy mùa)',
 'Khách sạn Tokyo'),

(12, 4, 'Tokyo – Akihabara & Odaiba', 'Ăn sáng, trưa, tối', 'Akihabara và khu Odaiba',
 '/anh/diemden/tokyo.jpg',
 'Akihabara – văn hóa anime
 Odaiba – tượng Gundam
 TeamLab (tùy chọn)',
 'Khách sạn Tokyo'),

(12, 5, 'Tokyo – Tiễn khách', 'Ăn sáng, trưa', 'Mua quà và tiễn sân bay',
 '/anh/diemden/tokyo.jpg',
 'Mua quà Don Quijote / konbini
 Tiễn sân bay',
 '—'),

-- Tour 13: Kyoto 3N
(13, 1, 'Kyoto – Fushimi Inari & Gion', 'Ăn trưa, tối', 'Hàng ngàn cổng Torii đỏ',
 '/anh/diemden/kyoto.jpg',
 'Fushimi Inari Taisha
 Phố Gion – gặp Geisha
 Kiyomizu-dera',
 'Khách sạn Kyoto'),

(13, 2, 'Kyoto – Arashiyama', 'Ăn sáng, trưa, tối', 'Rừng tre Arashiyama và chùa Tenryu-ji',
 '/anh/diemden/kyoto.jpg',
 'Rừng tre Arashiyama
 Chùa Tenryu-ji
 Trà đạo / kimono rental',
 'Khách sạn Kyoto'),

(13, 3, 'Kyoto – Tiễn', 'Ăn sáng, trưa', 'Chùa Vàng Kinkaku-ji và tiễn',
 '/anh/diemden/kyoto.jpg',
 'Kinkaku-ji (Chùa Vàng)
 Nijō Castle
 Tiễn ga / sân bay Osaka',
 '—'),

-- Tour 14: Osaka 2N
(14, 1, 'Osaka – Lâu đài & Dotonbori', 'Ăn trưa, tối', 'Dotonbori ẩm thực đường phố',
 '/anh/diemden/osaka.jpg',
 'Lâu đài Osaka
 Dotonbori – Takoyaki, Okonomiyaki
 Umeda Sky Building',
 'Khách sạn Osaka'),

(14, 2, 'Osaka – Universal & Tiễn', 'Ăn sáng, trưa', 'Universal Studios Japan',
 '/anh/diemden/osaka.jpg',
 'Universal Studios Japan
 Mua sắm Shinsaibashi
 Tiễn sân bay Kansai',
 '—'),

-- Tour 15: Seoul 4N
(15, 1, 'Seoul – Gyeongbokgung', 'Ăn trưa, tối', 'Cung điện Gyeongbokgung mặc Hanbok',
 '/anh/diemden/seoul.jpg',
 'Đón sân bay Incheon
 Gyeongbokgung – mặc Hanbok
 Làng Bukchon Hanok',
 'Khách sạn Seoul'),

(15, 2, 'Seoul – Myeongdong & Namsan', 'Ăn sáng, trưa, tối', 'Mua sắm Myeongdong và tháp Namsan',
 '/anh/diemden/seoul.jpg',
 'Myeongdong shopping
 Tháp Namsan – khóa tình yêu
 Dongdaemun Design Plaza',
 'Khách sạn Seoul'),

(15, 3, 'Seoul – DMZ / Everland', 'Ăn sáng, trưa, tối', 'Tour DMZ hoặc Everland',
 '/anh/diemden/seoul.jpg',
 'Tour DMZ (tùy chọn)
 Hoặc Everland / Lotte World
 Hongdae – K-pop street',
 'Khách sạn Seoul'),

(15, 4, 'Seoul – Tiễn khách', 'Ăn sáng, trưa', 'Mua quà và tiễn sân bay',
 '/anh/diemden/seoul.jpg',
 'Mua mỹ phẩm, quà K-food
 Tiễn sân bay Incheon',
 '—'),

-- Tour 16: Hà Nội - Hạ Long 2N
(16, 1, 'Hà Nội – City tour', 'Ăn trưa, tối', 'Half-day Hà Nội',
 '/anh/diemden/hanoi.jpg',
 'Hồ Gươm, Văn Miếu
 Phố cổ, ẩm thực địa phương',
 'Khách sạn Hà Nội'),

(16, 2, 'Hạ Long – Du thuyền 1 ngày', 'Ăn sáng, trưa, tối', 'Tour du thuyền vịnh Hạ Long',
 '/anh/diemden/halong.jpg',
 'Xe xuống Hạ Long
 Tour du thuyền 1 ngày
 Về Hà Nội / tiễn khách',
 '—'),

-- Tour 17: Đà Nẵng - Phú Quốc 5N
(17, 1, 'Đà Nẵng – Bà Nà', 'Ăn trưa, tối', 'Bà Nà Hills',
 '/anh/diemden/danang.jpg',
 'Cầu Vàng Bà Nà Hills
 Biển Mỹ Khê',
 'Khách sạn Đà Nẵng'),

(17, 2, 'Hội An – Phố cổ', 'Ăn sáng, trưa, tối', 'Hội An ancient town',
 '/anh/diemden/danang.jpg',
 'Phố cổ Hội An
 Thả hoa đăng',
 'Khách sạn Hội An'),

(17, 3, 'Bay – Phú Quốc', 'Ăn sáng, trưa, tối', 'Bay sang Phú Quốc',
 '/anh/diemden/phuquoc.jpg',
 'Bay Đà Nẵng – Phú Quốc
 Nhận phòng resort
 Tắm biển chiều',
 'Resort Phú Quốc'),

(17, 4, 'Phú Quốc – Đảo & VinWonders', 'Ăn sáng, trưa, tối', 'VinWonders và cáp treo',
 '/anh/diemden/phuquoc.jpg',
 'VinWonders Phú Quốc
 Cáp treo Hòn Thơm
 Sunset beach',
 'Resort Phú Quốc'),

(17, 5, 'Phú Quốc – Tiễn', 'Ăn sáng, trưa', 'Mua quà và tiễn sân bay',
 '/anh/diemden/phuquoc.jpg',
 'Chợ Dinh Cậu
 Tiễn sân bay Phú Quốc',
 '—'),

-- Tour 18: Bắc Kinh - Thượng Hải 7N (rút gọn 4 ngày hiển thị)
(18, 1, 'Bắc Kinh – Thiên An Môn', 'Ăn trưa, tối', 'Quảng trường Thiên An Môn',
 '/anh/chuyendi/backinh.jpg',
 'Đón sân bay Bắc Kinh
 Thiên An Môn – Cố Cung
 Phố Hutong',
 'Khách sạn Bắc Kinh'),

(18, 2, 'Bắc Kinh – Vạn Lý Trường Thành', 'Ăn sáng, trưa, tối', 'Vạn Lý Trường Thành',
 '/anh/chuyendi/backinh.jpg',
 'Vạn Lý Trường Thành Badaling
 Di Hoa Viên',
 'Khách sạn Bắc Kinh'),

(18, 3, 'Tàu cao tốc – Thượng Hải', 'Ăn sáng, trưa, tối', 'Di chuyển sang Thượng Hải',
 '/anh/chuyendi/shanghai.jpg',
 'Tàu cao tốc Bắc Kinh – Thượng Hải
 Bến Thượng Hải
 Phố Nam Kinh',
 'Khách sạn Thượng Hải'),

(18, 4, 'Thượng Hải – Tiễn khách', 'Ăn sáng, trưa', 'Tháp Minh Châu và tiễn',
 '/anh/chuyendi/shanghai.jpg',
 'Tháp Minh Châu Phương Đông
 Yu Garden
 Tiễn sân bay Thượng Hải',
 '—');
