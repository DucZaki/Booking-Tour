# 🌍 BookingTour - Hệ Thống Đặt Tour Du Lịch Trực Tuyến & Hỗ Trợ Khách Hàng Bằng AI

**BookingTour** là một ứng dụng web hiện đại được phát triển trên nền tảng **Spring Boot 3**, cung cấp giải pháp toàn diện cho việc quản lý, tìm kiếm, đặt chỗ và thanh toán các tour du lịch trực tuyến. Đặc biệt, hệ thống tích hợp công nghệ AI tiên tiến giúp tư vấn, giải đáp thắc mắc và đề xuất lịch trình tự động cho khách hàng.

---

## ✨ Tính Năng Nổi Bật

### 🧑‍💻 Phân Hệ Khách Hàng (User)
*   **Tìm kiếm & Bộ lọc Thông minh:** Tìm kiếm tour theo điểm xuất phát/đích, ngày khởi hành, khoảng giá mong muốn và phương tiện di chuyển.
*   **Chi Tiết Chuyến Đi Phong Phú:** Hiển thị chi tiết lịch trình từng ngày, phương tiện di chuyển, điểm đón khách, chính sách phụ thu, số chỗ trống, đánh giá thực tế và số lượng đã đặt.
*   **Đặt Tour Tiện Lợi:** Quy trình đặt chỗ (Booking) tự động kiểm tra số lượng chỗ khả dụng và cập nhật tức thì.
*   **Tích Hợp AI Chatbot:** Trợ lý ảo thông minh hoạt động 24/7 (sử dụng Groq, Gemini hoặc OpenRouter) tự động truy xuất dữ liệu hệ thống (tour, lịch trình, mã giảm giá) để tư vấn cụ thể và cá nhân hóa trải nghiệm người dùng.
*   **Thanh Toán Trực Tuyến:** Tích hợp cổng thanh toán quốc dân **VNPay** hỗ trợ giao dịch tức thì, cập nhật trạng thái đơn hàng thời gian thực qua cơ chế IPN Callback.
*   **Mã QR Check-in:** Tự động tạo mã QR Code tiện dụng hỗ trợ check-in nhanh chóng khi bắt đầu chuyến đi.
*   **Quản Lý Cá Nhân:**
    *   Đăng nhập linh hoạt (Form đăng ký/đăng nhập truyền thống và **OAuth2 Google / Facebook**).
    *   Lưu trữ danh sách tour yêu thích (Favorites).
    *   Quản lý lịch sử đặt tour, trạng thái thanh toán và thông tin check-in.
    *   Đánh giá (Rating & Review) trực quan cho những chuyến đi đã trải nghiệm.

### ⚙️ Phân Hệ Quản Trị (Admin)
*   **Dashboard Thống Kê:** Biểu đồ doanh thu trực quan, số lượt đặt tour, thống kê tỉ lệ thanh toán thành công và tăng trưởng người dùng.
*   **Quản Lý Sản Phẩm (Tour):** Thêm mới, chỉnh sửa thông tin chi tiết tour, cập nhật hình ảnh trực tiếp, lịch trình ngày đi, các ngày khởi hành và kiểm soát số lượng hành khách.
*   **Quản Lý Đơn Hàng:** Xem chi tiết giao dịch, xác nhận trạng thái đặt chỗ, xử lý hoàn tiền hoặc hủy tour.
*   **Quản Lý Người Dùng & Phân Quyền:** Quản lý tài khoản khách hàng, vai trò hệ thống, tiếp nhận thông tin liên hệ và phản hồi từ người dùng.
*   **Quản Lý Khuyến Mãi:** Tạo và theo dõi hiệu lực của các mã giảm giá (Vouchers) áp dụng cho các tour cụ thể.

---

## 🛠️ Công Nghệ & Thư Viện Sử Dụng

### Backend
*   **Ngôn ngữ:** Java 17
*   **Framework chính:** Spring Boot 3.x, Spring MVC, Spring Data JPA
*   **Bảo mật:** Spring Security, OAuth2 Client
*   **Database:** MySQL 8.x
*   **Migration Tool:** Flyway (quản lý lịch sử thay đổi schema tự động)
*   **API Bên Thứ Ba:**
    *   **VNPay API:** Cổng thanh toán trực tuyến.
    *   **Amadeus API:** Tìm kiếm thông tin chuyến bay và dịch vụ liên quan.
    *   **News API:** Tự động cập nhật tin tức du lịch thế giới.
    *   **AI Providers (Groq, Gemini, OpenRouter):** Cung cấp mô hình ngôn ngữ lớn (LLM) hỗ trợ Chatbot.

### Frontend
*   **Template Engine:** Thymeleaf (kết xuất HTML phía server)
*   **UI/UX:** HTML5, CSS3, Vanilla JavaScript, Bootstrap 5, FontAwesome Icons

---

## 📂 Cấu Trúc Mã Nguồn

```text
src/main/java/edu/bookingtour/
├── client/         # Các Client tích hợp API bên thứ 3 (Amadeus, NewsAPI)
├── config/         # Cấu hình hệ thống (SecurityConfig, VNPayConfig, WebMvcConfig)
├── controller/     # Lớp điều khiển xử lý HTTP Request
│   ├── admin/      # Các Controller dành riêng cho khu vực quản trị viên
│   └── user/       # Các Controller phục vụ người dùng cuối
├── dto/            # Data Transfer Objects (chuyển đổi dữ liệu)
├── entity/         # Thực thể JPA mapping trực tiếp với bảng trong Database
├── repo/           # Các JPA Repository giao tiếp với Database
├── service/        # Lớp xử lý Logic nghiệp vụ (Business Logic)
└── util/           # Lớp tiện ích hỗ trợ (Mã hóa, Sinh mã QR, Xử lý chuỗi)
```

---

## 🚀 Hướng Dẫn Cài Đặt Dưới Local

### 1. Chuẩn Bị Môi Trường
*   Cài đặt **Java Development Kit (JDK) 17** trở lên.
*   Cài đặt hệ quản trị cơ sở dữ liệu **MySQL Server 8.0+**.
*   Cài đặt công cụ build **Maven 3.x** (hoặc dùng wrapper `mvnw` đi kèm).

### 2. Tạo Cơ Sở Dữ Liệu
Mở MySQL Command Line hoặc công cụ quản trị (như DBeaver, Navicat) và thực thi câu lệnh:
```sql
CREATE DATABASE booking_tour CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Cấu Hình Biến Môi Trường & Bí Mật
Để chạy dự án đầy đủ tính năng, bạn nên cấu hình các API Key của bên thứ 3 trong file `src/main/resources/application-local.properties` (đã được bỏ qua trong Git để bảo mật):

```properties
# Cơ sở dữ liệu local
spring.datasource.url=jdbc:mysql://localhost:3306/booking_tour
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD

# OAuth2 Google & Facebook (Nếu cần tính năng đăng nhập mạng xã hội)
spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET
spring.security.oauth2.client.registration.facebook.client-id=YOUR_FACEBOOK_CLIENT_ID
spring.security.oauth2.client.registration.facebook.client-secret=YOUR_FACEBOOK_CLIENT_SECRET

# Tích Hợp VNPay Sandbox
vnp.tmn-code=YOUR_VNPAY_TMN_CODE
vnp.hash-secret=YOUR_VNPAY_HASH_SECRET

# News API Token (Cập nhật tin tức)
news.api.token=YOUR_NEWS_API_TOKEN

# Trợ Lý AI Chatbot (Groq / Gemini)
groq.api.key=YOUR_GROQ_API_KEY
```

> [!NOTE]
> Hệ thống sử dụng **Flyway** để quản lý cơ sở dữ liệu. Khi ứng dụng khởi chạy lần đầu tiên, toàn bộ cấu trúc bảng và dữ liệu mẫu sẽ tự động được tạo từ thư mục `src/main/resources/db/migration` mà không cần import tay.

### 4. Khởi Chạy Ứng Dụng
1. Clone dự án về máy tính:
   ```bash
   git clone https://github.com/DucZaki/Booking-Tour.git
   cd Booking-Tour
   ```
2. Cấp quyền thực thi và khởi chạy dự án:
   ```bash
   chmod +x mvnw
   ./mvnw spring-boot:run
   ```
3. Truy cập vào trình duyệt tại địa chỉ: [http://localhost:8080](http://localhost:8080)

---

## 📞 Liên Hệ & Hỗ Trợ

Nếu bạn gặp khó khăn trong quá trình cài đặt hoặc có ý kiến đóng góp cho dự án, vui lòng tạo **Issue** trên trang Github hoặc liên hệ trực tiếp với nhóm phát triển qua thông tin hỗ trợ được tích hợp trên website.

---
*Chúc bạn có những trải nghiệm tuyệt vời cùng BookingTour!* 🚀✨
