package edu.bookingtour.controller.admin;

import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.MaGiamGia;
import edu.bookingtour.service.MaGiamGiaService;
import edu.bookingtour.service.TourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin/promo")
public class AdminMaGiamGiaController {

    @Autowired
    private MaGiamGiaService maGiamGiaService;

    @Autowired
    private TourService tourService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        var promos = maGiamGiaService.findAll(page, size);
        model.addAttribute("promos", promos);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPage", promos.getTotalPages());
        model.addAttribute("today", LocalDate.now());
        return "admin/promo/promo-list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("promo", newPromo());
        addFormData(model);
        return "admin/promo/promo-form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        MaGiamGia promo = maGiamGiaService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã giảm giá"));
        if (promo.getGiaTriGiam() == null && promo.getPhanTramGiam() != null) {
            promo.setGiaTriGiam(java.math.BigDecimal.valueOf(promo.getPhanTramGiam()));
        }
        if (promo.getLoaiGiam() == null || promo.getLoaiGiam().isBlank()) {
            promo.setLoaiGiam(MaGiamGia.LOAI_PHAN_TRAM);
        }
        model.addAttribute("promo", promo);
        addFormData(model);
        return "admin/promo/promo-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("promo") MaGiamGia promo,
                       @RequestParam(required = false) String applyScope,
                       @RequestParam(required = false) Integer[] tourIds,
                       RedirectAttributes ra) {
        try {
            boolean allTours = !"PICK".equals(applyScope);
            List<Integer> ids = tourIds != null ? Arrays.asList(tourIds) : List.of();
            maGiamGiaService.save(promo, ids, allTours);
            ra.addFlashAttribute("success", true);
            return "redirect:/admin/promo";
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:" + (promo.getId() != null ? "/admin/promo/edit/" + promo.getId() : "/admin/promo/create");
        }
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        maGiamGiaService.delete(id);
        return "redirect:/admin/promo";
    }

    private MaGiamGia newPromo() {
        MaGiamGia m = new MaGiamGia();
        m.setLoaiGiam(MaGiamGia.LOAI_PHAN_TRAM);
        m.setApDungTatCa(true);
        m.setNgayBatDau(LocalDate.now());
        m.setNgayKetThuc(LocalDate.now().plusMonths(3));
        return m;
    }

    private void addFormData(Model model) {
        List<ChuyenDi> tours = tourService.findAll();
        model.addAttribute("allTours", tours);
    }
}
