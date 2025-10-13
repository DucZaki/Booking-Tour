package edu.bookingtour.controller;

import edu.bookingtour.entity.DiemDen;
import edu.bookingtour.repo.DiemDenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DiemDenController {

    @Autowired
    private DiemDenRepository diemdenrp;

    @GetMapping("/")
    public String hienThiTrangChu(Model model) {
        List<DiemDen> dsNoiBat = diemdenrp.findByNoiBat(true);
        model.addAttribute("dsNoiBat", dsNoiBat);
        return "index";
    }

}
