package edu.bookingtour.service;

import edu.bookingtour.entity.NguoiDung;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private NguoiDungService nguoiDungService;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        NguoiDung nguoiDung = nguoiDungService.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user: " + login));

        String role = nguoiDung.getVaiTro();
        if (role != null && role.startsWith("ROLE_")) {
            role = role.substring(5);
        }

        return User.builder()
                .username(nguoiDung.getTenDangNhap())
                .password(nguoiDung.getMatKhau() != null ? nguoiDung.getMatKhau() : "")
                .roles(role != null ? role : "USER")
                .build();
    }
}