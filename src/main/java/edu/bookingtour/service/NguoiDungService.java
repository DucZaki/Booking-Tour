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
    public Optional<NguoiDung> findByEmail(String email) {
        return nguoiDungRepository.findByEmail(email);
    }
    public List<NguoiDung> findAll() {
        return nguoiDungRepository.findAll();
    }
    public NguoiDung save(NguoiDung nguoiDung) {
        NguoiDung user = new NguoiDung();
        user.setTenDangNhap(nguoiDung.getTenDangNhap());
        user.setEmail(nguoiDung.getEmail());
        user.setMatKhau(nguoiDung.getMatKhau());
        user.setVaiTro(nguoiDung.getVaiTro());
        user.setHoTen(nguoiDung.getHoTen());
        user.setNumber(nguoiDung.getNumber());
        return nguoiDungRepository.save(nguoiDung);
    }

    /**
     * Cập nhật thông tin người dùng
     */
    public NguoiDung update(Integer id, NguoiDung nguoiDung) {
        NguoiDung user = nguoiDungRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));
        user.setTenDangNhap(nguoiDung.getTenDangNhap());
        user.setEmail(nguoiDung.getEmail());
        user.setHoTen(nguoiDung.getHoTen());
        user.setNumber(nguoiDung.getNumber());
        user.setVaiTro(nguoiDung.getVaiTro());

        // Chỉ cập nhật mật khẩu nếu có mật khẩu mới
        if (nguoiDung.getMatKhau() != null && !nguoiDung.getMatKhau().isEmpty()) {
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
    public Page<NguoiDung> findAllUser(int page, int perPage) {
        Pageable pageable = PageRequest.of(page, perPage);
        return nguoiDungRepository.findAll(pageable);
    }
    /**
     * Đăng ký người dùng mới
     */
    public NguoiDung registerNewUser(NguoiDung nguoiDung) {
        // Kiểm tra username đã tồn tại chưa
        if (nguoiDungRepository.findByTenDangNhap(nguoiDung.getTenDangNhap()).isPresent()) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }

        // Kiểm tra email đã tồn tại chưa
        if (nguoiDungRepository.findByEmail(nguoiDung.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã được sử dụng!");
        }

        // Mã hóa mật khẩu
        nguoiDung.setMatKhau(passwordEncoder.encode(nguoiDung.getMatKhau()));

        // Đặt vai trò mặc định là USER
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
}