package edu.bookingtour.controller.api;

import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.DiemDen;
import edu.bookingtour.entity.NguoiDung;
import edu.bookingtour.entity.YeuThich;
import edu.bookingtour.repo.ChuyenDiRepository;
import edu.bookingtour.repo.DiemDenRepository;
import edu.bookingtour.repo.FavoriteRepository;
import edu.bookingtour.repo.NguoiDungRepository;
import edu.bookingtour.service.NguoiDungService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST API Controller cho Flutter Mobile App
 * Tất cả endpoints bắt đầu bằng /api/mobile
 */
@RestController
@RequestMapping("/api/mobile")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class MobileApiController {

    @Autowired
    private NguoiDungService nguoiDungService;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private ChuyenDiRepository chuyenDiRepository;

    @Autowired
    private DiemDenRepository diemDenRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==================== AUTH APIs ====================

    /**
     * Đăng nhập - trả về thông tin user nếu thành công
     */
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Tên đăng nhập và mật khẩu không được để trống"));
        }

        Optional<NguoiDung> userOpt = nguoiDungService.findByTenDangNhap(username);

        if (userOpt.isEmpty()) {
            // Thử tìm bằng email
            userOpt = nguoiDungRepository.findByEmail(username);
        }

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Tên đăng nhập hoặc mật khẩu không đúng"));
        }

        NguoiDung user = userOpt.get();

        // Kiểm tra mật khẩu
        if (!passwordEncoder.matches(password, user.getMatKhau())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Tên đăng nhập hoặc mật khẩu không đúng"));
        }

        // Đăng nhập thành công - trả về thông tin user (không có mật khẩu)
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đăng nhập thành công");
        response.put("user", Map.of(
                "id", user.getId(),
                "tenDangNhap", user.getTenDangNhap(),
                "email", user.getEmail() != null ? user.getEmail() : "",
                "hoTen", user.getHoTen() != null ? user.getHoTen() : "",
                "number", user.getNumber() != null ? user.getNumber() : "",
                "vaiTro", user.getVaiTro() != null ? user.getVaiTro() : "USER"));
        // Trong môi trường thực tế, bạn sẽ trả về JWT token ở đây
        response.put("token", "session-" + user.getId() + "-" + System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    /**
     * Đăng ký tài khoản mới
     */
    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> registerRequest) {
        String username = registerRequest.get("tenDangNhap");
        String email = registerRequest.get("email");
        String password = registerRequest.get("matKhau");
        String fullName = registerRequest.get("hoTen");
        String phone = registerRequest.get("number");

        // Validate
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Tên đăng nhập không được để trống"));
        }

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Email không được để trống"));
        }

        if (password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Mật khẩu không được để trống"));
        }

        // Kiểm tra tồn tại
        if (nguoiDungService.findByTenDangNhap(username).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Tên đăng nhập đã tồn tại"));
        }

        if (nguoiDungRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Email đã được sử dụng"));
        }

        try {
            NguoiDung newUser = new NguoiDung();
            newUser.setTenDangNhap(username);
            newUser.setEmail(email);
            newUser.setMatKhau(password); // Service sẽ mã hóa
            newUser.setHoTen(fullName);
            newUser.setNumber(phone);
            newUser.setVaiTro("USER");

            nguoiDungService.registerNewUser(newUser);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đăng ký thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    // ==================== TOUR APIs ====================

    /**
     * Lấy tất cả tour
     */
    @GetMapping("/tours")
    public ResponseEntity<List<ChuyenDi>> getAllTours() {
        List<ChuyenDi> tours = chuyenDiRepository.findAll();
        return ResponseEntity.ok(tours);
    }

    /**
     * Lấy tour nổi bật
     */
    @GetMapping("/tours/featured")
    public ResponseEntity<?> getFeaturedTours() {
        List<ChuyenDi> tours = chuyenDiRepository.findByNoiBat(true);
        List<Map<String, Object>> result = tours.stream().map(t -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", t.getId());
            map.put("tieuDe", t.getTieuDe());
            map.put("moTa", t.getMoTa());
            map.put("gia", t.getGia());
            map.put("ngayKhoiHanh", t.getNgayKhoiHanh());
            map.put("ngayKetThuc", t.getNgayKetThuc());
            map.put("noiBat", t.getNoiBat());
            map.put("hinhAnh", t.getHinhAnh());
            map.put("highlight", t.getHighlight());

            // Simplified relationships to avoid proxies
            if (t.getIdDiemDen() != null) {
                Map<String, Object> dd = new HashMap<>();
                dd.put("id", t.getIdDiemDen().getId());
                dd.put("thanhPho", t.getIdDiemDen().getThanhPho());
                dd.put("quocGia", t.getIdDiemDen().getQuocGia());
                map.put("idDiemDen", dd);
            }

            return map;
        }).collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * Lấy chi tiết tour
     */
    @GetMapping("/tours/{id}")
    public ResponseEntity<?> getTourById(@PathVariable Integer id) {
        Optional<ChuyenDi> tour = chuyenDiRepository.findById(id);
        if (tour.isPresent()) {
            return ResponseEntity.ok(tour.get());
        }
        return ResponseEntity.notFound().build();
    }

    // ==================== DESTINATION APIs ====================

    /**
     * Lấy tất cả điểm đến
     */
    @GetMapping("/destinations")
    public ResponseEntity<List<DiemDen>> getAllDestinations() {
        List<DiemDen> destinations = diemDenRepository.findAll();
        return ResponseEntity.ok(destinations);
    }

    /**
     * Lấy điểm đến nổi bật
     */
    @GetMapping("/destinations/featured")
    public ResponseEntity<?> getFeaturedDestinations() {
        List<DiemDen> destinations = diemDenRepository.findByNoiBat(true);
        List<Map<String, Object>> result = destinations.stream().map(d -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", d.getId());
            map.put("thanhPho", d.getThanhPho());
            map.put("quocGia", d.getQuocGia());
            map.put("chauLuc", d.getChauLuc());
            map.put("hinhAnh", d.getHinhAnh());
            map.put("noiBat", d.getNoiBat());
            map.put("moTa", d.getMoTa());
            return map;
        }).collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ==================== USER APIs ====================

    /**
     * Lấy thông tin user theo ID
     */
    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Integer id) {
        Optional<NguoiDung> user = nguoiDungRepository.findById(id);
        if (user.isPresent()) {
            NguoiDung u = user.get();
            return ResponseEntity.ok(Map.of(
                    "id", u.getId(),
                    "tenDangNhap", u.getTenDangNhap(),
                    "email", u.getEmail() != null ? u.getEmail() : "",
                    "hoTen", u.getHoTen() != null ? u.getHoTen() : "",
                    "number", u.getNumber() != null ? u.getNumber() : "",
                    "vaiTro", u.getVaiTro() != null ? u.getVaiTro() : "USER"));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Đổi mật khẩu
     */
    @PostMapping("/user/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        Integer userId = Integer.parseInt(request.get("userId"));
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        try {
            nguoiDungService.changePassword(userId, oldPassword, newPassword);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đổi mật khẩu thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    // ==================== FAVORITES APIs ====================

    /**
     * Lấy danh sách yêu thích của user
     */
    @GetMapping("/favorites/{userId}")
    public ResponseEntity<List<YeuThich>> getUserFavorites(@PathVariable Integer userId) {
        Optional<NguoiDung> user = nguoiDungRepository.findById(userId);
        if (user.isPresent()) {
            List<YeuThich> favorites = favoriteRepository.findByIdNguoiDung(user.get());
            return ResponseEntity.ok(favorites);
        }
        return ResponseEntity.ok(List.of());
    }

    /**
     * Thêm tour vào yêu thích
     */
    @PostMapping("/favorites/add")
    public ResponseEntity<?> addFavorite(@RequestBody Map<String, Integer> request) {
        Integer userId = request.get("userId");
        Integer tourId = request.get("tourId");

        Optional<NguoiDung> user = nguoiDungRepository.findById(userId);
        Optional<ChuyenDi> tour = chuyenDiRepository.findById(tourId);

        if (user.isEmpty() || tour.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User hoặc Tour không tồn tại"));
        }

        // Kiểm tra đã tồn tại chưa
        if (favoriteRepository.existsByIdNguoiDungAndIdChuyenDi(user.get(), tour.get())) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Tour đã có trong danh sách yêu thích"));
        }

        YeuThich favorite = new YeuThich();
        favorite.setIdNguoiDung(user.get());
        favorite.setIdChuyenDi(tour.get());
        favoriteRepository.save(favorite);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã thêm vào yêu thích"));
    }

    /**
     * Xóa tour khỏi yêu thích
     */
    @DeleteMapping("/favorites/{id}")
    public ResponseEntity<?> removeFavorite(@PathVariable Integer id) {
        favoriteRepository.deleteById(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã xóa khỏi yêu thích"));
    }
}
