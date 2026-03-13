# 🌍 BookingTour - Hệ Thống Đặt Tour Du Lịch Trực Tuyến

**BookingTour** là một ứng dụng web hiện đại được xây dựng trên nền tảng Spring Boot, giúp người dùng dễ dàng tìm kiếm, khám phá và đặt các tour du lịch hấp dẫn. Hệ thống cung cấp trải nghiệm mượt mà từ khâu chọn điểm đến đến khâu thanh toán trực tuyến.

---

## ✨ Tính Năng Chính

### 🛡️ Người Dùng (User)
*   **Tìm Kiếm & Lọc Tour:** Tìm kiếm tours theo điểm đến, ngày khởi hành, giá cả và phương tiện.
*   **Xem Chi Tiết Tour:** Thông tin chi tiết về lịch trình, điểm đón, nơi lưu trú và đánh giá từ khách hàng khác.
*   **Đặt Chỗ (Booking):** Quy trình đặt tour nhanh chóng, quản lý số lượng chỗ còn trống.
*   **Thanh Toán Trực Tuyến:** Tích hợp cổng thanh toán **VNPay** an toàn và tiện lợi.
*   **Quản Lý Tài Khoản:** Đăng nhập (hỗ trợ **OAuth2 Google**), xem lịch sử đặt tour, quản lý tour yêu thích.
*   **Đánh Giá & Phản Hồi:** Gửi nhận xét và chấm điểm cho các tour đã tham gia.

### ⚙️ Quản Trị Viên (Admin)
*   **Quản Lý Tour:** Thêm, sửa, xóa các tour du lịch, quản lý ngày khởi hành và lịch trình.
*   **Quản Lý Đơn Hàng:** Theo dõi danh sách đặt chỗ, xác nhận thanh toán và trạng thái tour.
*   **Thống Kê & Báo Cáo:** Theo dõi doanh thu và lượt đặt tour qua biểu đồ trực quan.
*   **Quản Lý Người Dùng & Liên Hệ:** Tiếp nhận thông tin liên hệ và quản lý danh sách khách hàng.

---

## 🛠️ Công Nghệ Sử Dụng

*   **Backend:** Java 17, Spring Boot 3.x
*   **Security:** Spring Security (Form Login & OAuth2 Google)
*   **Database:** MySQL, Spring Data JPA
*   **Frontend:** Thymeleaf, HTML5, CSS3, JavaScript
*   **Payment:** VNPay API Integration
*   **API:** Amadeus API (dành cho các dịch vụ liên quan đến chuyến bay/du lịch)
*   **Công cụ khác:** Lombok, Maven, Git

---

## 📂 Cấu Trúc Project

```text
src/main/java/edu/bookingtour/
├── client/         # Client gọi API bên thứ 3 (Amadeus, VNPay)
├── config/         # Cấu hình hệ thống (Security, VNPay, MVC)
├── controller/     # Xử lý Request từ người dùng (Admin & User)
├── dto/            # Data Transfer Objects
├── entity/         # Các thực thể database (JPA)
├── repo/           # Interface tương tác với database
└── service/        # Xử lý logic nghiệp vụ
```

---

## 🚀 Hướng Dẫn Cài Đặt

### 1. Yêu Cầu Hệ Thống
*   Java JDK 17+
*   MySQL Server 8.0+
*   Maven 3.x

### 2. Cài Đặt Database
1. Tạo một database mới trong MySQL:
   ```sql
   CREATE DATABASE booking_tour CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
2. Cấu hình thông tin kết nối trong file `src/main/resources/application.properties` (hoặc file `.env` nếu có).

### 3. Chạy Ứng Dụng
1. Clone dự án về máy:
   ```bash
   git clone https://github.com/DucZaki/Booking-Tour.git
   ```
2. Di chuyển vào thư mục dự án:
   ```bash
   cd Booking-Tour
   ```
3. Chạy ứng dụng bằng Maven:
   ```bash
   mvn spring-boot:run
   ```
4. Truy cập vào trình duyệt: `http://localhost:8080`

---

## 👥 Thành Viên Nhóm

*   **Nguyễn Minh Đức** - MSV: 23010634
*   **Ngô Văn Hưng** - MSV: 23010622

**Giảng viên hướng dẫn:** Vũ Văn Quang

---

## 📞 Liên Hệ

Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với các thành viên nhóm hoặc qua github dự án.

---
*Cảm ơn bạn đã quan tâm đến dự án của chúng tôi!* 😊
