-- Sức chứa mặc định theo tour và theo từng ngày khởi hành
ALTER TABLE chuyen_di
    ADD COLUMN suc_chua_mac_dinh INT NOT NULL DEFAULT 50;

ALTER TABLE ngay_khoi_hanh
    ADD COLUMN suc_chua INT NULL;

UPDATE chuyen_di SET suc_chua_mac_dinh = 50 WHERE suc_chua_mac_dinh IS NULL OR suc_chua_mac_dinh < 1;

UPDATE ngay_khoi_hanh nkh
    JOIN chuyen_di cd ON cd.id = nkh.id_chuyen_di
SET nkh.suc_chua = cd.suc_chua_mac_dinh
WHERE nkh.suc_chua IS NULL;
