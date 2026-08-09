# ZakiBooking

Dự án cá nhân — website đặt tour du lịch

Khách vào xem tour, đặt chỗ, thanh toán VNPay. Admin quản lý tour, đơn, user. HDV (role GUIDE) vào `/staff` xem đoàn, check-in khách bằng QR.

---

## Mục lục

- [Giới thiệu nhanh](#giới-thiệu-nhanh)
- [3 role trong hệ thống](#3-role-trong-hệ-thống)
- [Chức năng](#chức-năng)
- [Vấn đề phức tạp của dự án](#phần-code-mất-công-nhất)
- [Tech stack](#tech-stack)
- [Chạy dự án](#chạy-dự-án)
- [Liên hệ](#liên-hệ)

---

## Giới thiệu nhanh

Backend Spring Boot 3, frontend Thymeleaf + Bootstrap, database MySQL (Flyway migrate schema).

Có thêm VNPay, OAuth2 Google/Facebook, báo giá vé qua Amadeus, chatbot AI (Groq / OpenRouter / Gemini), gửi mail xác nhận.

Luồng cơ bản:

```
USER:   Xem tour → Đặt → VNPay → Nhận email + QR check-in
ADMIN:  Quản lý tour, ngày KH, đơn, user, mã giảm giá
GUIDE:  Xem đoàn được giao → Manifest → Check-in → Cập nhật trạng thái chuyến
```

---

## 3 role trong hệ thống

| Role | Vào đâu | Làm gì |
|------|---------|--------|
| **USER** | `/`, `/user/**`, `/booking/**` | Khách: xem tour, đặt, thanh toán, đánh giá |
| **ADMIN** | `/admin/**` | Quản trị toàn bộ |
| **GUIDE** | `/staff/**` | HDV: đoàn được phân công, check-in khách |

Login xong redirect theo role: admin → `/admin`, guide → `/staff`, còn lại về trang chủ.

Admin cũng vào được `/staff` nếu cần xem vận hành.

---

## Chức năng

### USER

- Đăng ký / đăng nhập (BCrypt), login Google/Facebook
- Lọc tour: điểm đến, giá, ngày, phương tiện
- Chi tiết tour: lịch 3 tháng, chọn ngày KH, chọn **điểm xuất phát**, xem giá theo điểm đón
- Đặt tour: nhập hành khách theo độ tuổi, phòng đơn, mã giảm giá
- Thanh toán VNPay; đơn PENDING còn 15 phút để trả lại
- QR check-in, lịch sử đơn, yêu thích, đánh giá
- Tour gần bạn (GPS), chatbot Zaki, tin tức

### ADMIN

- Dashboard doanh thu, biểu đồ
- CRUD tour, lịch trình, ảnh, ngày khởi hành
- Gán HDV cho đoàn hoặc từng điểm xuất phát
- Quản lý đơn (PENDING / PAID / FAILED), user, mã giảm giá, liên hệ

### GUIDE

- Dashboard đoàn theo tuần/tháng
- Manifest khách đã thanh toán (chỉ thấy điểm đón mình phụ trách)
- Check-in qua QR hoặc nhập tay
- Bắt đầu / kết thúc chuyến, xuất CSV manifest

---

## Vấn đề phức tạp của dự án 
### Nhiều điểm xuất phát, giá khác nhau

Một tour có thể đón khách ở HN, SG... Mỗi ngày KH, mỗi điểm đón có giá vé riêng (gọi Amadeus lấy giá máy bay).

Làm qua bảng `ngay_khoi_hanh_diem_don`, service `NgayKhoiHanhDiemDonService` — sync điểm đón, `getQuote()` trả giá lúc khách chọn trên form.

### Chống đặt quá chỗ

Hai người cùng lúc đặt tour còn 1 chỗ thì dễ lỗi. Dùng `TourCapacityService.assertCanBook()` với lock dòng (`SELECT FOR UPDATE`), đếm cả đơn PAID và PENDING (trong 15 phút, trùng hạn thanh toán VNPay).

### Tính giá phức tạp

Không phải nhân đơn giá × số người. Tách người lớn / trẻ / em bé, phụ thu phòng đơn, vé trẻ em tính khác theo hãng bay. Gom trong `BookingPricingService`, validate hành khách ở `PaymentController`.

### VNPay

Callback browser + IPN server có thể gọi 2 lần. Verify chữ ký HMAC, so số tiền, `txnRef` dạng `{id}_{timestamp}` để repay không bị trùng. Đơn đã PAID thì bỏ qua, không update lại.

### HDV chỉ thấy khách của mình

Một ngày KH có nhiều điểm đón, mỗi điểm có HDV riêng. `TourManifestService` gán guide theo điểm đón hoặc cả đoàn, lọc manifest và quyền check-in cho đúng người.

### Trạng thái chuyến đi

Đoàn có SCHEDULED → IN_PROGRESS → COMPLETED (hoặc CANCELLED nếu không ai đặt). Có scheduler tự chuyển trạng thái, gửi mail khi tour bắt đầu/kết thúc.

### Chatbot

 `ChatService` nhét danh sách tour thật + mã giảm giá vào prompt, thử Groq rồi OpenRouter rồi Gemini nếu lỗi.

### Mã giảm giá

Có loại Early Bird (đặt trước X ngày), Last-minute (sát giờ KH), giới hạn số lần dùng. Validate trong `MaGiamGiaService`, trừ lượt dùng bằng câu UPDATE có điều kiện.

### Hạn đặt vé

Không cho đặt quá sát giờ tập trung. `DepartureBookingPolicy` tính deadline, calendar tour hiện ngày đã đóng booking.

---

## Tech stack

| | |
|---|---|
| Backend | Java 17, Spring Boot 3.5.6, JPA |
| Frontend | Thymeleaf, Bootstrap 5, JS |
| DB | MySQL, Flyway |
| Auth | Spring Security, OAuth2 |
| Thanh toán / API | VNPay, Amadeus, News API, Gmail SMTP, LLM |

Package: `controller` (user / admin / staff) · `service` · `repo` · `entity` · `dto` · `config` · `client` · `util`

---

## Chạy dự án 

**Cần:** JDK 17+, MySQL 8+

```bash
git clone https://github.com/DucZaki/Booking-Tour.git
cd Booking-Tour
```

```sql
CREATE DATABASE booking_tour CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Copy env:

```bash
cp .env.example .env
```

Sửa tối thiểu:

```env
SPRING_PROFILES_ACTIVE=local
SPRING_DATASOURCE_PASSWORD=your_mysql_password
APP_BASE_URL=http://localhost:8080
```

Các key khác (VNPay, OAuth, mail...): xem file `.env.example`.

Chạy:

```powershell
# Windows
.\mvnw.cmd spring-boot:run
```

```bash
# macOS / Linux
./mvnw spring-boot:run
```

Mở http://localhost:8080

| Role | URL | Account |
|------|-----|---------|
| Admin | /admin | `admin` / `admin123` |
| Guide | /staff | Tạo user role GUIDE trong admin, hoặc bật `guide.demo.seed.enabled=true` |
| User | / | Đăng ký mới |

Build:

```bash
./mvnw clean package
java -jar target/*.jar
```

---

## Liên hệ

**Nguyễn Minh Đức**

- Email: minhd4360@gmail.com
- GitHub: [@DucZaki](https://github.com/DucZaki)

