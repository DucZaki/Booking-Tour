# Đồng bộ dữ liệu MySQL local → Railway

## 1. Export từ máy bạn (đã có sẵn file mẫu)

```powershell
cd Booking-Tour-main
.\scripts\export-local-db.ps1
```

File tạo ra: **`scripts/exports/booking_tour_local.sql`** (schema + toàn bộ dữ liệu + `flyway_schema_history`).

> File này **không commit** lên Git (có user, booking, hash mật khẩu).

---

## 2. Chuẩn bị MySQL trên Railway

1. Vào project Railway → service **MySQL**
2. Bật **Public Networking** (nếu import từ máy bạn)
3. Copy biến kết nối:
   - `MYSQLHOST`, `MYSQLPORT`, `MYSQLUSER`, `MYSQLPASSWORD`, `MYSQLDATABASE`

Service Spring Boot đã có:

```env
SPRING_DATASOURCE_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}
SPRING_DATASOURCE_USERNAME=${{MySQL.MYSQLUSER}}
SPRING_DATASOURCE_PASSWORD=${{MySQL.MYSQLPASSWORD}}
```

---

## 3. Import dump vào Railway

### Cách A — MySQL Workbench (dễ nhất)

1. **Server → New Connection**
   - Host: `MYSQLHOST` (Railway)
   - Port: `MYSQLPORT`
   - User / Password: từ Railway
2. Connect → chọn database `MYSQLDATABASE`
3. **Server → Data Import** → chọn `scripts/exports/booking_tour_local.sql` → Start Import

### Cách B — Dòng lệnh (PowerShell)

```powershell
$env:MYSQL_PWD = "<MYSQLPASSWORD từ Railway>"
& "C:\Program Files\MySQL\MySQL Server 9.4\bin\mysql.exe" `
  --host=<MYSQLHOST> --port=<MYSQLPORT> `
  -u<MYSQLUSER> <MYSQLDATABASE> `
  < scripts\exports\booking_tour_local.sql
```

### Cách C — Railway CLI / Query tab

Nếu file nhỏ, có thể paste từng phần trong Query — không khuyến nghị với file lớn.

---

## 4. Sau khi import

1. **Redeploy** service Spring Boot (hoặc restart)
2. Flyway: dump đã có `flyway_schema_history` (V1–V5). Nếu app có **V6** mới hơn, Flyway sẽ tự chạy V6 khi khởi động.
3. Kiểm tra:
   - Đăng nhập user cũ (tài khoản local)
   - Danh sách tour, đơn `dat_cho`, admin

---

## 5. Lưu ý quan trọng

| Vấn đề | Giải pháp |
|--------|-----------|
| Railway DB **đã có dữ liệu** | Import sẽ `DROP TABLE` — **mất hết data cũ trên Railway** |
| Ảnh tour (`/anh/...`) | Chỉ có trong DB là **đường dẫn**; file ảnh phải có trên server (static trong JAR hoặc volume) |
| Upload avatar user | Railway ephemeral — upload mới có thể mất khi redeploy; cần volume/S3 sau này |
| `APP_BASE_URL` | Đặt đúng domain Railway để QR/VNPay/email đúng |

---

## 6. Cập nhật lại sau này

Mỗi lần đổi nhiều trên local:

```powershell
.\scripts\export-local-db.ps1
# Import lại lên Railway (bước 3)
```

Hoặc chỉ export **dữ liệu** (không đè schema):

```powershell
mysqldump --no-create-info ... booking_tour > scripts/exports/data_only.sql
```
