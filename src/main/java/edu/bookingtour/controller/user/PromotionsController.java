package edu.bookingtour.controller.user;

import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.MaGiamGia;
import edu.bookingtour.repo.ChuyenDiRepository;
import edu.bookingtour.service.MaGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
public class PromotionsController {

    @Autowired
    private MaGiamGiaService maGiamGiaService;

    @Autowired
    private ChuyenDiRepository chuyenDiRepository;

    @GetMapping("/uu-dai")
    public String promotionsPage(Model model) {
        List<MaGiamGia> promos = maGiamGiaService.findAllActive(LocalDate.now());
        List<ChuyenDi> hotTours = chuyenDiRepository.findByNoiBat(true);
        if (hotTours.size() > 6) {
            hotTours = hotTours.subList(0, 6);
        }
        model.addAttribute("promos", promos);
        model.addAttribute("hotTours", hotTours);
        return "pages/uu-dai";
    }
}
