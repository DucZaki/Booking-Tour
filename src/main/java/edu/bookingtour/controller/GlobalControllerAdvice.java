package edu.bookingtour.controller;

import edu.bookingtour.entity.DiemDen;
import edu.bookingtour.entity.NguoiDung;
import edu.bookingtour.repo.DiemDenRepository;
import edu.bookingtour.repo.NguoiDungRepository;
import edu.bookingtour.util.NavDestinationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.Optional;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private DiemDenRepository diemDenRepository;

    @ModelAttribute("currentUser")
    public NguoiDung getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Optional<NguoiDung> user = nguoiDungRepository.findByTenDangNhap(auth.getName());
            return user.orElse(null);
        }
        return null;
    }

    @ModelAttribute("navDomestic")
    public List<DiemDen> navDomestic() {
        return NavDestinationUtil.split(diemDenRepository.findAll()).domestic();
    }

    @ModelAttribute("navInternational")
    public List<DiemDen> navInternational() {
        return NavDestinationUtil.split(diemDenRepository.findAll()).international();
    }
}
