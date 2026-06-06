package edu.bookingtour.controller.user;

import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.DanhGia;
import edu.bookingtour.entity.DiemDen;
import edu.bookingtour.repo.*;
import edu.bookingtour.repo.ChuyenDiRepository;
import edu.bookingtour.service.NguoiDungService;
import edu.bookingtour.service.TourCardStatsService;
import edu.bookingtour.service.TourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Autowired
    private TourService tourservice;
    @Autowired
    private TourCardStatsService tourCardStatsService;
    @GetMapping("/")
    public String hienThiTrangChu(Model model) {
        List<DiemDen> dsNoiBat = diemdenrp.findByNoiBat(true);
        model.addAttribute("dsNoiBat", dsNoiBat);
        List<ChuyenDi> dsnoibat = chuyendirepository.findByNoiBat(true);
        model.addAttribute("dsnoibatcd", dsnoibat);
        List<Integer> tourIds = dsnoibat.stream().map(ChuyenDi::getId).collect(Collectors.toList());
        model.addAttribute("tourStats", tourCardStatsService.forTours(tourIds));
        List<DanhGia> dsDanhGia = danhgiarepo.findAll();
        model.addAttribute("dsDanhGia", dsDanhGia);
        List<String> dsnd= nguoidungservice.findhotenbinhluan();
        model.addAttribute("dsnd", dsnd);
        return "index";
    }
}
