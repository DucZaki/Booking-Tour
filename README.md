# ZakiBooking

**Website đặt tour du lịch trực tuyến** — đồ án tốt nghiệp, xây dựng bằng Java Spring Boot.

Trong bối cảnh du lịch chuyển sang số, người dùng cần tra cứu tour, so sánh giá và thanh toán nhanh trên một nền tảng thống nhất; doanh nghiệp cần công cụ quản lý tour, đơn đặt và doanh thu tập trung. **ZakiBooking** ra đời để giải quyết hai nhu cầu đó trong một hệ thống web hoàn chỉnh: giao diện thân thiện cho khách hàng và khu vực quản trị cho admin.

| | |
|---|---|
| **Tác giả** | Nguyễn Minh Đức — MSSV 23010634 |
| **Khoa** | Hệ thống thông tin — Trường Đại học Phenikaa |
| **GVHD** | TS. Đặng Thị Thúy An |
| **Mã nguồn** | [github.com/DucZaki/Booking-Tour](https://github.com/DucZaki/Booking-Tour) |

---

## Mô tả dự án

ZakiBooking là ứng dụng web full-stack theo mô hình **MVC phân tầng** (Controller → Service → Repository → Entity). Giao diện render phía server bằng **Thymeleaf + Bootstrap 5**; dữ liệu lưu trên **MySQL** (18 bảng, quản lý schema qua **Flyway V1–V7**). Bảo mật bằng **Spring Security**: đăng nhập form, phân quyền USER/ADMIN, OAuth2 Google và Facebook.

Hệ thống triển khai **18 use-case** — 11 luồng phía khách hàng và 7 luồng quản trị — bao phủ toàn bộ vòng đời từ xem tour → đặt chỗ → thanh toán → check-in → quản trị nội dung.

### Luồng nghiệp vụ chính

```
Khách hàng:  Trang chủ → Tìm/Lọc tour → Chi tiết → Đặt tour → VNPay → Email xác nhận + QR check-in
Admin:       Dashboard → CRUD tour → Quản lý đơn / user / mã giảm giá / liên hệ
```

### Điểm khác biệt so với nền tảng thương mại

- Tùy biến nghiệp vụ theo mô hình tour Việt Nam: **điểm đón**, **ngày khởi hành**, **giá vé theo từng điểm đón**
- Tích hợp **VNPay Sandbox** với callback/IPN cập nhật trạng thái tự động
- **Chatbot AI Zaki** tư vấn tour dựa trên dữ liệu thật trong CSDL (RAG-lite)
- **Chuyến đi gần bạn** — gợi ý tour theo vị trí địa lý (Geolocation + Haversine)
- **QR check-in** sau thanh toán, hỗ trợ xác nhận tại điểm tập trung
- Mã nguồn mở, có thể mở rộng và triển khai tự host (Railway, VPS…)

---

## Chức năng

### Phía khách hàng

| Nhóm | Mô tả |
|------|--------|
| Xác thực | Đăng ký, đăng nhập (tên đăng nhập, BCrypt), OAuth2 Google/Facebook |
| Tour | Danh sách, lọc theo điểm đến/giá/ngày/phương tiện, chi tiết lịch trình |
| Đặt tour | Chọn ngày KH, điểm đón, số khách; áp mã giảm giá (PERCENT / FIXED) |
| Thanh toán | VNPay Sandbox; email xác nhận qua SMTP |
| Sau đặt | QR check-in, lịch sử đơn, yêu thích, đánh giá |
| Bổ trợ | Chatbot AI, chuyến đi gần bạn, tin tức (News API), báo giá vé (Amadeus) |

### Phía quản trị (`/admin`)

Dashboard thống kê doanh thu và đơn đặt · CRUD tour (ảnh, lịch trình, ngày KH, điểm đón) · Quản lý đơn đặt · Quản lý người dùng · Mã giảm giá · Liên hệ từ form khách hàng

---

## Kiến trúc & công nghệ

```
┌─────────────┐     ┌──────────────────┐     ┌─────────────┐
│  Thymeleaf  │────▶│  Spring Boot 3   │────▶│   MySQL     │
│  Bootstrap  │     │  Security + JPA  │     │   Flyway    │
└─────────────┘     └────────┬─────────┘     └─────────────┘
                             │
              ┌──────────────┼──────────────┐
              ▼              ▼              ▼
           VNPay        Amadeus       Groq / Gemini
         News API       OAuth2          (Chatbot)
```

| Thành phần | Công nghệ |
|------------|-----------|
| Backend | Java 17, Spring Boot 3.5.6, Spring MVC, Spring Data JPA |
| Bảo mật | Spring Security, BCrypt, OAuth2 Client |
| Frontend | Thymeleaf, Bootstrap 5, JavaScript |
| CSDL | MySQL 8.x+, Flyway migration |
| Tích hợp | VNPay, Amadeus, News API, Gmail SMTP, LLM APIs |
| Build / Deploy | Maven, Railway (tùy chọn) |

**Cấu trúc package:** `controller` · `service` · `repo` · `entity` · `dto` · `config` · `client` · `util`

---

## Cài đặt nhanh

**Yêu cầu:** JDK 17+, MySQL 8+, Git

```bash
git clone https://github.com/DucZaki/Booking-Tour.git
cd Booking-Tour
```

```sql
CREATE DATABASE booking_tour CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 1. Cấu hình (khuyến nghị dùng `.env`)

Dự án đọc cấu hình từ **biến môi trường** (xem `src/main/resources/application.properties`). Khi chạy local, bạn có thể tạo file `.env` ở thư mục gốc (file này đã được `.gitignore`, không commit).

Ví dụ `.env` tối thiểu để chạy local:

```env
SPRING_PROFILES_ACTIVE=local

# MySQL local
SPRING_DATASOURCE_PASSWORD=your_mysql_password

# Base URL dùng cho QR/check-in/link trong email (nếu dùng ngrok, thay bằng https://....ngrok-free.app)
APP_BASE_URL=http://localhost:8080
```

Nếu bạn muốn bật đầy đủ các tích hợp, thêm các biến sau (để trống nếu chưa dùng tính năng đó):

```env
# OAuth2
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
FACEBOOK_CLIENT_ID=
FACEBOOK_CLIENT_SECRET=

# VNPay (Sandbox)
VNP_TMN_CODE=
VNP_HASH_SECRET=
VNP_PAY_URL=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
VNP_API_URL=https://sandbox.vnpayment.vn/merchant_webapi/api/transaction
# Tuỳ chọn (mặc định trỏ local)
VNP_RETURN_URL=http://localhost:8080/payment/vnpay-callback
VNP_IPN_URL=http://localhost:8080/payment/vnpay-ipn

# Amadeus / News
AMADEUS_API_KEY=
AMADEUS_API_SECRET=
NEWS_API_TOKEN=

# Chatbot AI (ưu tiên: Groq -> OpenRouter -> Gemini)
GROQ_API_KEY=
GROQ_MODEL=llama-3.1-8b-instant
OPENROUTER_API_KEY=
OPENROUTER_MODEL=google/gemini-2.0-flash-exp:free
GEMINI_API_KEY=

# SMTP (Gmail)
MAIL_USERNAME=
MAIL_PASSWORD=
MAIL_FROM=
MAIL_ENABLED=true
```

Ghi chú:

- MySQL mặc định dùng `root@127.0.0.1:3306/booking_tour` (xem `application.properties`). Nếu máy bạn không dùng user `root` hoặc khác host/port, hãy tạo `src/main/resources/application-local.properties` để override (file này đã được `.gitignore`).
- Flyway sẽ tự migrate schema khi khởi động.

```powershell
# Windows
.\mvnw.cmd spring-boot:run
```

```bash
# macOS/Linux
./mvnw spring-boot:run
```

Truy cập **http://localhost:8080** · Admin: **http://localhost:8080/admin**

| Tài khoản seed | Username | Password |
|----------------|----------|----------|
| Admin | `admin` | `admin123` |

### 2. Build / Test

```bash
# Chạy test
./mvnw test

# Build JAR
./mvnw clean package

# Chạy JAR (sau khi build)
java -jar target/*.jar
```

### 3. Deploy Railway (tuỳ chọn)

- File mẫu biến môi trường: `railway.env.example` (copy vào Railway Variables -> Raw Editor).
- Đồng bộ dữ liệu MySQL local lên Railway: `scripts/SYNC-DB-TO-RAILWAY.md`.
- Khi deploy, nhớ đặt `APP_BASE_URL` là domain Railway để link QR/VNPay/email đúng.

---

## Tài liệu

| Tài liệu | Đường dẫn |
|----------|-----------|
| Báo cáo đồ án | [`docs/BAO-CAO-DO-AN.md`](docs/BAO-CAO-DO-AN.md) |
| Sơ đồ UML | [`docs/diagrams/`](docs/diagrams/) |
| Đồng bộ DB Railway | [`scripts/SYNC-DB-TO-RAILWAY.md`](scripts/SYNC-DB-TO-RAILWAY.md) |

---

## Troubleshooting

| Lỗi / Triệu chứng | Nguyên nhân thường gặp | Cách xử lý |
|---|---|---|
| Không kết nối được MySQL | Sai mật khẩu / MySQL chưa chạy | Kiểm tra MySQL service, đặt `SPRING_DATASOURCE_PASSWORD`, tạo DB `booking_tour` |
| Flyway báo checksum / validation failed | Bạn sửa file migration đã chạy | Không sửa migration cũ; tạo migration mới (V8, V9...) hoặc reset DB dev |
| OAuth2 redirect_uri_mismatch | Callback URL khác cấu hình trên Google/Facebook Console | Cập nhật Authorized redirect URI đúng domain/port hiện tại |
| VNPay callback/IPN không cập nhật | `VNP_HASH_SECRET` sai hoặc URL callback/IPN không public | Kiểm tra env VNPay; khi test ngoài local dùng ngrok và đặt `APP_BASE_URL`, `VNP_RETURN_URL`, `VNP_IPN_URL` |
| Không gửi được email | Gmail chặn đăng nhập hoặc thiếu `MAIL_USERNAME/PASSWORD` | Dùng App Password Gmail và bật `MAIL_ENABLED=true` |

---

## Liên hệ

**Nguyễn Minh Đức** · minhd4360@gmail.com · [GitHub Issues](https://github.com/DucZaki/Booking-Tour/issues)

---

*Dự án phục vụ mục đích học tập — đồ án liên ngành. Liên hệ tác giả trước khi sử dụng thương mại.*
