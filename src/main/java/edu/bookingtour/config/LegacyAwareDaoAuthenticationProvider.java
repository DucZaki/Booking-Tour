package edu.bookingtour.config;

import edu.bookingtour.entity.NguoiDung;
import edu.bookingtour.repo.NguoiDungRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Hỗ trợ mật khẩu legacy (plain text) — tự nâng cấp BCrypt khi đăng nhập thành công.
 * Báo lỗi rõ cho tài khoản OAuth không có mật khẩu form.
 */
public class LegacyAwareDaoAuthenticationProvider extends DaoAuthenticationProvider {

    private final NguoiDungRepository nguoiDungRepository;

    public LegacyAwareDaoAuthenticationProvider(NguoiDungRepository nguoiDungRepository) {
        this.nguoiDungRepository = nguoiDungRepository;
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {
        String presented = authentication.getCredentials() != null
                ? authentication.getCredentials().toString()
                : "";
        String stored = userDetails.getPassword();

        if (stored == null || stored.isBlank()) {
            NguoiDung user = nguoiDungRepository.findByTenDangNhap(userDetails.getUsername()).orElse(null);
            if (user != null && user.getProvider() != null && !user.getProvider().isBlank()) {
                throw new BadCredentialsException(
                        "Tài khoản này đăng ký bằng Google. Vui lòng bấm \"Đăng nhập với Google\".");
            }
            throw new BadCredentialsException(
                    "Tài khoản chưa có mật khẩu. Vui lòng dùng Google hoặc liên hệ admin.");
        }

        if (!isBcryptHash(stored)) {
            if (stored.equals(presented)) {
                PasswordEncoder encoder = getPasswordEncoder();
                nguoiDungRepository.findByTenDangNhap(userDetails.getUsername()).ifPresent(u -> {
                    u.setMatKhau(encoder.encode(presented));
                    nguoiDungRepository.save(u);
                });
                return;
            }
            throw new BadCredentialsException("Tên đăng nhập, email hoặc mật khẩu không đúng!");
        }

        super.additionalAuthenticationChecks(userDetails, authentication);
    }

    private static boolean isBcryptHash(String value) {
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }
}
