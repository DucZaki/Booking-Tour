<!DOCTYPE html>
<html lang="vi">
    <head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Zaki Booking - Du lịch thông minh</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="../static/css/style.css" rel="stylesheet">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@splidejs/splide@4.1.3/dist/css/splide.min.css">

        </head>
        <script src="https://cdn.jsdelivr.net/npm/@splidejs/splide@4.1.3/dist/js/splide.min.js"></script>
        <body>
        <!-- Navbar -->
        <nav class="navbar navbar-expand-lg bg-white shadow-sm fixed-top">
            <div class="container">
                <a class="navbar-brand fw-bold text-primary" href="#">ZakiBooking</a>
                <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                    <span class="navbar-toggler-icon"></span>
                </button>
                <div class="collapse navbar-collapse justify-content-end" id="navbarNav">
                    <ul class="navbar-nav">
                        <li class="nav-item"><a class="nav-link active" href="#">Trang chủ</a></li>
                        <li class="nav-item"><a class="nav-link" href="#">Tour</a></li>
                        <li class="nav-item"><a class="nav-link" href="#">Điểm đến</a></li>
                        <li class="nav-item"><a class="nav-link" href="#">Tin tức</a></li>
                        <li class="nav-item"><a class="nav-link" href="#">Liên hệ</a></li>
                    </ul>
                    <a href="#" class="btn btn-primary ms-3">Đăng nhập</a>
                </div>
            </div>
        </nav>

        <!-- Hero Banner -->
        <header class="hero-section d-flex align-items-center text-center text-white" style="background-image: url('img/banner.jpg');">
            <div class="container">
                <h1 class="display-4 fw-bold">Khám phá thế giới cùng Zaki Booking</h1>
                <p class="lead mb-4">Hơn 100+ tour du lịch hấp dẫn trong và ngoài nước</p>
                <div class="search-box bg-white p-4 rounded shadow">
                    <div class="row g-2">
                        <div class="col-md-3"><input type="text" class="form-control" placeholder="Điểm khởi hành"></div>
                        <div class="col-md-3"><input type="text" class="form-control" placeholder="Điểm đến"></div>
                        <div class="col-md-3"><input type="date" class="form-control"></div>
                        <div class="col-md-3"><button class="btn btn-primary w-100">Tìm tour</button></div>
                    </div>
                </div>
            </div>
        </header>

        <!-- Top Destinations -->
        <section class="py-5 text-center">
            <div class="container">
                <h2 class="fw-bold mb-4">Điểm đến nổi bật</h2>

                <div id="carouselDiemDen" class="carousel slide" data-bs-ride="carousel" data-bs-interval="4000">
                    <div class="carousel-inner">

                        <div class="carousel-item"
                             th:each="dd, stat : ${dsNoiBat}"
                             th:classappend="${stat.index == 0} ? ' active'">
                            <img th:src="@{${dd.hinh_anh}}" class="d-block w-100 rounded" alt="" style="height: 250px; object-fit: cover;">
                                <div class="carousel-caption d-none d-md-block p-0 mt-2" style="color: #000;">
                                    <h5 th:text="${dd.thanh_pho}" class="fw-bold"></h5>
                                    <p th:text="${dd.chau_luc}"></p>
                                </div>
                        </div>

                    </div>

                    <!-- Nút điều hướng -->
                    <button class="carousel-control-prev" type="button" data-bs-target="#carouselDiemDen" data-bs-slide="prev">
                        <span class="carousel-control-prev-icon bg-dark rounded-circle p-3" aria-hidden="true"></span>
                        <span class="visually-hidden">Trước</span>
                    </button>

                    <button class="carousel-control-next" type="button" data-bs-target="#carouselDiemDen" data-bs-slide="next">
                        <span class="carousel-control-next-icon bg-dark rounded-circle p-3" aria-hidden="true"></span>
                        <span class="visually-hidden">Tiếp</span>
                    </button>
                </div>
            </div>
        </section>

        <!-- Popular Tours -->
        <section class="bg-light py-5">
            <div class="container text-center">
                <h2 class="fw-bold mb-4">Tour phổ biến</h2>
                <div class="row g-4">
                    <div class="col-md-4" th:each="ds : ${dsnoibatcd}">
                        <div class="card border-0 shadow-sm h-100">
                            <img th:src="@{${ds.hinh_anh}}" class="card-img-top rounded" alt=""  style="width:100%; height:250px; object-fit: cover;">
                                <div class="card-body">
                                    <h5 class="card-title" th:text="${ds.tieu_de}" ></h5>
                                    <p th:text="${#numbers.formatDecimal(ds.gia, 0, 'COMMA', 2, 'POINT')} + ' VND'"></p>
                                    <button class="btn btn-outline-primary">Xem chi tiết</button>
                                </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- Reviews -->
        <section class="py-5 text-center bg-light">
            <div class="container">
                <h2 class="fw-bold mb-4">Khách hàng nói gì?</h2>

                <div id="reviewCarousel" class="carousel slide" data-bs-ride="carousel" data-bs-interval="5000">
                    <div class="carousel-inner">
                        <!-- Slide 1 -->
                        <div class="carousel-item active">
                            <blockquote class="blockquote">
                                <p class="mb-4">“Tour rất tuyệt vời, dịch vụ chuyên nghiệp, đáng tiền!”</p>
                                <footer class="blockquote-footer">Nguyễn Minh Anh</footer>
                            </blockquote>
                        </div>

                        <!-- Slide 2 -->
                        <div class="carousel-item">
                            <blockquote class="blockquote">
                                <p class="mb-4">“Zaki Booking giúp tôi dễ dàng đặt tour chỉ trong vài phút.”</p>
                                <footer class="blockquote-footer">Trần Quang Huy</footer>
                            </blockquote>
                        </div>

                        <!-- Slide 3 -->
                        <div class="carousel-item">
                            <blockquote class="blockquote">
                                <p class="mb-4">“Hướng dẫn viên thân thiện, lịch trình hợp lý, rất hài lòng!”</p>
                                <footer class="blockquote-footer">Phạm Thị Hòa</footer>
                            </blockquote>
                        </div>
                    </div>

                    <!-- ⬅️ Nút sang trái -->
                    <button class="carousel-control-prev" type="button" data-bs-target="#reviewCarousel" data-bs-slide="prev">
                        <span class="carousel-control-prev-icon bg-dark rounded-circle p-3" aria-hidden="true"></span>
                        <span class="visually-hidden">Trước</span>
                    </button>

                    <!-- ➡️ Nút sang phải -->
                    <button class="carousel-control-next" type="button" data-bs-target="#reviewCarousel" data-bs-slide="next">
                        <span class="carousel-control-next-icon bg-dark rounded-circle p-3" aria-hidden="true"></span>
                        <span class="visually-hidden">Tiếp</span>
                    </button>
                </div>
            </div>
        </section>



        <!-- Newsletter -->
        <section class="newsletter bg-primary text-white py-5 text-center">
            <div class="container">
                <h3 class="fw-bold">Đăng ký nhận tin khuyến mãi</h3>
                <p>Nhận ngay ưu đãi du lịch mới nhất mỗi tuần</p>
                <div class="d-flex justify-content-center mt-3">
                    <input type="email" class="form-control w-50 me-2" placeholder="Nhập email của bạn">
                        <button class="btn btn-light">Đăng ký</button>
                </div>
            </div>
        </section>

        <!-- Footer -->
        <footer class="bg-dark text-white py-4">
            <div class="container text-center">
                <p class="mb-1">&copy; 2025 Zaki Booking. All rights reserved.</p>
                <p>Email: info@zakibooking.com | Hotline: 1900 9999</p>
            </div>
        </footer>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
        <script src="../static/js/script.js"></script>
        </body>
    </html>
