package edu.bookingtour.service;

import edu.bookingtour.entity.NguoiDung;
import edu.bookingtour.repo.NguoiDungRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        NguoiDung nguoiDung = nguoiDungRepository.findByTenDangNhap(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user: " + username));

        return User.builder()
                .username(nguoiDung.getTenDangNhap())
                .password(nguoiDung.getMatKhau())
                .roles(nguoiDung.getVaiTro())
                .build();
    }
}