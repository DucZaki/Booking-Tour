package edu.bookingtour.controller.admin;

import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.service.TourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/admin/tour")
public class AdminTourController {
    @Autowired
    private TourService tourService;

    @GetMapping
    public String tourList(Model model) {
        List<ChuyenDi> tour = tourService.findAll();
        model.addAttribute("tour", tour);
        return "admin/tour/tour-list";
    }
    @GetMapping("/create")
    public String tourAdd(Model model) {
        ChuyenDi chuyenDi = new ChuyenDi();
        model.addAttribute("chuyenDi", chuyenDi);
        return "admin/tour/tour-create";
    }
    @PostMapping("/save")
    public String save(
            @ModelAttribute ChuyenDi chuyenDi,
            @RequestParam("images") MultipartFile[] images
    ) {
        tourService.save(chuyenDi, images);
        return "redirect:/admin/tour";
    }

    @GetMapping("/edit/{id}")
    public String tourEdit(Model model, @PathVariable Integer id) {
        ChuyenDi tour = tourService.findById(id).orElseThrow(() -> new RuntimeException("Tour not found"));
        model.addAttribute("chuyenDi", tour);
        return "admin/tour/tour-edit";
    }
    @PostMapping("/update/{id}")
    public String update(
            @PathVariable Integer id,
            @ModelAttribute ChuyenDi chuyenDi,
            @RequestParam("images") MultipartFile[] images
    ) {
        tourService.update(id, chuyenDi, images);
        return "redirect:/admin/tour";
    }

}
