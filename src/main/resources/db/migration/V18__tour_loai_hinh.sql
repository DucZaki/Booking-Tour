-- Loại hình tour cho mục "Khám phá theo loại hình"
-- Giá trị: GIA_DINH, TREKKING, NGHI_DUONG, GHEP_DOAN
ALTER TABLE chuyen_di
    ADD COLUMN loai_hinh VARCHAR(20) NULL;

-- Nghỉ dưỡng: biển, resort, vịnh
UPDATE chuyen_di
SET loai_hinh = 'NGHI_DUONG'
WHERE LOWER(tieu_de) REGEXP 'resort|beach|biển|bay|vịnh|nha trang|phú quốc|hạ long';

-- Trekking / mạo hiểm: trek, núi, nature, mạo hiểm
UPDATE chuyen_di
SET loai_hinh = 'TREKKING'
WHERE loai_hinh IS NULL
  AND LOWER(tieu_de) REGEXP 'trek|núi|nature|mạo hiểm|sa pa|zhangjiajie|trương gia giới';

-- Gia đình: family, văn hoá, các tour nhẹ nhàng phù hợp gia đình
UPDATE chuyen_di
SET loai_hinh = 'GIA_DINH'
WHERE loai_hinh IS NULL
  AND LOWER(tieu_de) REGEXP 'family|gia đình|culture|văn hóa|văn hoá|huế|kyoto|tokyo';

-- Còn lại: tour ghép đoàn
UPDATE chuyen_di
SET loai_hinh = 'GHEP_DOAN'
WHERE loai_hinh IS NULL;
