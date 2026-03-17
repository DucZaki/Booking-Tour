package edu.bookingtour.controller.admin;

import edu.bookingtour.entity.DanhGia;
import edu.bookingtour.service.DanhGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/danh-gia")
public class AdminDanhGiaController {
    @Autowired
    private DanhGiaService danhGiaService;
    @GetMapping
    public String listToursWithReviews(@RequestParam(required = false) String sort, Model model) {
        model.addAttribute("tours", danhGiaService.getToursWithReviews(sort));
        model.addAttribute("sort", sort);
        return "admin/danh-gia/tour-list";
    }

    @GetMapping("/detail")
    public String listDanhGia(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer diem,
            @RequestParam(required = false) String hoTen,
            @RequestParam(required = false) Integer tourId,
            @RequestParam(required = false) String sort,
            Model model) {
        Page<DanhGia> danhGia = danhGiaService.filter(diem, hoTen, tourId, sort, page, size);
        model.addAttribute("danhGia", danhGia.getContent());
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPage", danhGia.getTotalPages());
        model.addAttribute("diem", diem);
        model.addAttribute("hoTen", hoTen);
        model.addAttribute("tourId", tourId);
        model.addAttribute("sort", sort);
        return "admin/danh-gia/list";
    }
}
