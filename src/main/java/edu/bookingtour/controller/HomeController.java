package edu.bookingtour.controller;

import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.DanhGia;
import edu.bookingtour.entity.DiemDen;
import edu.bookingtour.repo.*;
import edu.bookingtour.repo.ChuyenDiRepository;
import edu.bookingtour.service.NguoiDungService;
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
    @Autowired
    private DanhGiaRepository danhgiarepo;
    @Autowired
    private NguoiDungService nguoidungservice;
    @GetMapping("/")
    public String hienThiTrangChu(Model model) {
        List<DiemDen> dsNoiBat = diemdenrp.findByNoiBat(true);
        model.addAttribute("dsNoiBat", dsNoiBat);
        List<ChuyenDi> dsnoibat = chuyendirepository.findByNoiBat(true);
        model.addAttribute("dsnoibatcd", dsnoibat);
        List<DanhGia> dsDanhGia = danhgiarepo.findAll();
        model.addAttribute("dsDanhGia", dsDanhGia);
        List<String> dsnd= nguoidungservice.findhotenbinhluan();
        return "index";

    }

}
