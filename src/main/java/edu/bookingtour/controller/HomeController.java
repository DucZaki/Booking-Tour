package edu.bookingtour.controller;

import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.DiemDen;
import edu.bookingtour.repo.ChuyenDiRepository;
import edu.bookingtour.repo.DiemDenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {
    @Autowired
    private DiemDenRepository diemdenrp;
    @Autowired
    private ChuyenDiRepository chuyendirepository;
    @GetMapping("/home")
    public String hienThiTrangChu(Model model) {
        List<DiemDen> dsNoiBat = diemdenrp.findByNoibat(true);
        model.addAttribute("dsNoiBat", dsNoiBat);
        List<ChuyenDi> dsnoibat = chuyendirepository.findByNoiBat(true);
        model.addAttribute("dsnoibatcd", dsnoibat);
        return "index";
    }

}
