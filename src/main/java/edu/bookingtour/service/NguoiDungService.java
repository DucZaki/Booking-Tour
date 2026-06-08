package edu.bookingtour.service;

import edu.bookingtour.entity.NguoiDung;
import edu.bookingtour.repo.NguoiDungRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NguoiDungService {

    @Autowired
    private NguoiDungRepository nguoiDungRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<String> findhotenbinhluan() {
        return nguoiDungRepository.findTatCaHoTen();
    }

    public Optional<NguoiDung> findById(Integer id) {
        return nguoiDungRepository.findById(id);
    }

    public Optional<NguoiDung> findByTenDangNhap(String tenDangNhap) {
        return nguoiDungRepository.findByTenDangNhap(tenDangNhap);
    }

    /**
     * Tìm user theo tên đăng nhập hoặc email (không phân biệt hoa thường với email).
     */
    public Optional<NguoiDung> findByLogin(String login) {
        if (login == null || login.isBlank()) {
            return Optional.empty();
        }
        String trimmed = login.trim();
        Optional<NguoiDung> byUsername = nguoiDungRepository.findByTenDangNhap(trimmed);
        if (byUsername.isPresent()) {
            return byUsername;
        }
        if (trimmed.contains("@")) {
            return nguoiDungRepository.findByEmailIgnoreCase(trimmed);
        }
        return nguoiDungRepository.findByEmailIgnoreCase(trimmed);
    }

    public List<NguoiDung> findAll() {
        return nguoiDungRepository.findAll();
    }

    public NguoiDung save(NguoiDung nguoiDung) {
        return nguoiDungRepository.save(nguoiDung);
    }

    public NguoiDung createUserByAdmin(NguoiDung nguoiDung) {
        validateUserFields(nguoiDung, true, null);
        nguoiDung.setMatKhau(passwordEncoder.encode(nguoiDung.getMatKhau()));
        if (nguoiDung.getVaiTro() == null || nguoiDung.getVaiTro().isBlank()) {
            nguoiDung.setVaiTro("USER");
        }
        return nguoiDungRepository.save(nguoiDung);
    }

    public NguoiDung update(Integer id, NguoiDung nguoiDung) {
        NguoiDung user = nguoiDungRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));
        validateUserFields(nguoiDung, false, id);
        user.setTenDangNhap(nguoiDung.getTenDangNhap().trim());
        user.setEmail(nguoiDung.getEmail().trim());
        user.setHoTen(nguoiDung.getHoTen().trim());
        user.setNumber(nguoiDung.getNumber() != null ? nguoiDung.getNumber().trim() : null);
        user.setVaiTro(nguoiDung.getVaiTro());
        if (nguoiDung.getMatKhau() != null && !nguoiDung.getMatKhau().isBlank()) {
            user.setMatKhau(passwordEncoder.encode(nguoiDung.getMatKhau()));
        }
        return nguoiDungRepository.save(user);
    }

    public void deleteById(Integer id) {
        if (!nguoiDungRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy người dùng với ID: " + id);
        }
        nguoiDungRepository.deleteById(id);
    }

    public Page<Object[]> findAllUserDetail(int page, int size, String keyword, String sort) {
        Pageable pageable = PageRequest.of(page, size);
        String kw = normalizeKeyword(keyword);
        String sortKey = normalizeSort(sort);

        return switch (sortKey) {
            case "spending_asc" -> nguoiDungRepository.searchUsersBySpendingAsc(kw, pageable);
            case "bookings_desc" -> nguoiDungRepository.searchUsersByBookingsDesc(kw, pageable);
            case "name_asc" -> nguoiDungRepository.searchUsersByNameAsc(kw, pageable);
            default -> nguoiDungRepository.searchUsersBySpendingDesc(kw, pageable);
        };
    }

    private static String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }

    private static String normalizeSort(String sort) {
        if (sort == null) {
            return "spending_desc";
        }
        return switch (sort) {
            case "spending_asc", "bookings_desc", "name_asc", "spending_desc" -> sort;
            default -> "spending_desc";
        };
    }

    public NguoiDung registerNewUser(NguoiDung nguoiDung) {
        validateUserFields(nguoiDung, true, null);
        nguoiDung.setMatKhau(passwordEncoder.encode(nguoiDung.getMatKhau()));
        if (nguoiDung.getVaiTro() == null || nguoiDung.getVaiTro().isEmpty()) {
            nguoiDung.setVaiTro("USER");
        }
        return nguoiDungRepository.save(nguoiDung);
    }

    /**
     * Thay đổi mật khẩu
     */
    public void changePassword(Integer userId, String oldPassword, String newPassword) {
        NguoiDung user = nguoiDungRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Kiểm tra mật khẩu cũ
        if (!passwordEncoder.matches(oldPassword, user.getMatKhau())) {
            throw new RuntimeException("Mật khẩu cũ không đúng!");
        }

        // Cập nhật mật khẩu mới
        user.setMatKhau(passwordEncoder.encode(newPassword));
        nguoiDungRepository.save(user);
    }

    public boolean isUsernameExists(String username) {
        return nguoiDungRepository.findByTenDangNhap(username).isPresent();
    }

    public boolean isEmailExists(String email) {
        return nguoiDungRepository.findByEmail(email).isPresent();
    }

    /**
     * Cập nhật đường dẫn ảnh đại diện
     */
    public void updateAvatar(Integer userId, String avatarPath) {
        NguoiDung user = nguoiDungRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        user.setAnhDaiDien(avatarPath);
        nguoiDungRepository.save(user);
    }

    private void validateUserFields(NguoiDung user, boolean isCreate, Integer excludeId) {
        if (user == null) {
            throw new IllegalArgumentException("Thông tin người dùng không hợp lệ.");
        }

        String username = user.getTenDangNhap() != null ? user.getTenDangNhap().trim() : "";
        if (username.isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống.");
        }
        if (username.length() < 3) {
            throw new IllegalArgumentException("Tên đăng nhập phải có ít nhất 3 ký tự.");
        }

        String hoTen = user.getHoTen() != null ? user.getHoTen().trim() : "";
        if (hoTen.isEmpty()) {
            throw new IllegalArgumentException("Họ tên không được để trống.");
        }

        String email = user.getEmail() != null ? user.getEmail().trim() : "";
        if (email.isEmpty()) {
            throw new IllegalArgumentException("Email không được để trống.");
        }
        if (!email.toLowerCase().endsWith("@gmail.com")) {
            throw new IllegalArgumentException("Email phải là Gmail (@gmail.com).");
        }

        String number = user.getNumber() != null ? user.getNumber().trim().replaceAll("\\s+", "") : "";
        if (number.isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại không được để trống.");
        }
        if (!number.matches("^[0-9]{10}$")) {
            throw new IllegalArgumentException("Số điện thoại phải đúng 10 chữ số.");
        }

        String role = user.getVaiTro() != null ? user.getVaiTro().trim().toUpperCase() : "";
        if (!role.equals("USER") && !role.equals("GUIDE") && !role.equals("ADMIN")) {
            throw new IllegalArgumentException("Vai trò không hợp lệ.");
        }
        user.setVaiTro(role);

        String password = user.getMatKhau();
        if (isCreate) {
            if (password == null || password.isBlank()) {
                throw new IllegalArgumentException("Mật khẩu không được để trống.");
            }
        }
        if (password != null && !password.isBlank() && password.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự.");
        }

        if (excludeId == null) {
            if (nguoiDungRepository.findByTenDangNhapIgnoreCase(username).isPresent()) {
                throw new IllegalArgumentException("Tên đăng nhập đã tồn tại, vui lòng chọn tên khác.");
            }
            if (nguoiDungRepository.findByEmailIgnoreCase(email).isPresent()) {
                throw new IllegalArgumentException("Email đã được sử dụng.");
            }
        } else {
            if (nguoiDungRepository.findByTenDangNhapIgnoreCaseAndIdNot(username, excludeId).isPresent()) {
                throw new IllegalArgumentException("Tên đăng nhập đã tồn tại, vui lòng chọn tên khác.");
            }
            if (nguoiDungRepository.findByEmailIgnoreCaseAndIdNot(email, excludeId).isPresent()) {
                throw new IllegalArgumentException("Email đã được sử dụng.");
            }
        }

        user.setTenDangNhap(username);
        user.setHoTen(hoTen);
        user.setEmail(email.toLowerCase());
        user.setNumber(number);
    }
}