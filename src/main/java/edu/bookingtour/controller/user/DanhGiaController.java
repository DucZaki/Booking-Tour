package edu.bookingtour.controller.user;

import edu.bookingtour.service.DanhGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequestMapping("/danh-gia")
public class DanhGiaController {

    @Autowired
    private DanhGiaService danhGiaService;

    @PostMapping("/add")
    public String addDanhGia(@RequestParam Integer tourId, @RequestParam Integer diem, @RequestParam String binhLuan, Principal principal) {
        danhGiaService.save(tourId, diem, binhLuan, principal.getName());
        return "redirect:/tour/" + tourId;
    }
}
