package edu.bookingtour.controller.admin;

import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.repo.DiemDenRepository;
import edu.bookingtour.service.PhuongTienService;
import edu.bookingtour.service.TourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/tour")
public class AdminTourController {
    @Autowired
    private TourService tourService;
    @Autowired
    private PhuongTienService phuongTienService;
    @Autowired
    private DiemDenRepository diemDenRepository;
    @Value("${image.path}")
    private String imagePath;

    @GetMapping
    public String tourList(Model model) {
        List<ChuyenDi> tour = tourService.findAll();
        model.addAttribute("tour", tour);
        return "admin/tour/tour-list";
    }
    @GetMapping("/create")
    public String tourAdd(Model model) {
        ChuyenDi tour = new ChuyenDi();
        model.addAttribute("tour", tour);
        model.addAttribute("phuongTienList", phuongTienService.getDistinctLoai());
        model.addAttribute("chauLucList", diemDenRepository.findDistinctChauLuc());
        return "admin/tour/tour-create";
    }
    @PostMapping("/save")
    public String save(@ModelAttribute ChuyenDi chuyenDi, @RequestParam("file") MultipartFile file) throws IOException {
        if (!file.isEmpty()) {
            String fileName = UUID.randomUUID() + file.getOriginalFilename();
            File dest = new File(imagePath + fileName);
            file.transferTo(dest);
            chuyenDi.setHinhAnh("/anh/chuyendi/" + fileName);
        }
        tourService.save(chuyenDi);
        return "redirect:/admin/tour";
    }

    @GetMapping("/edit/{id}")
    public String tourEdit(Model model, @PathVariable Integer id) {
        ChuyenDi tour = tourService.findById(id).orElseThrow(() -> new RuntimeException("Tour Not Found"));
        model.addAttribute("tour", tour);
        model.addAttribute("phuongTienList", phuongTienService.getDistinctLoai());
        model.addAttribute("chauLucList", diemDenRepository.findDistinctChauLuc());
        model.addAttribute("selectedChauLuc", tour.getIdDiemDen().getChauLuc());
        model.addAttribute("selectedQuocGia", tour.getIdDiemDen().getQuocGia());
        model.addAttribute("selectedThanhPho", tour.getIdDiemDen().getId());
        return "admin/tour/tour-edit";
    }
    @PostMapping("/update/{id}")
    public String update(@PathVariable Integer id, @ModelAttribute ChuyenDi chuyenDi, @RequestParam("file") MultipartFile file) throws IOException {
        ChuyenDi tour = tourService.findById(id).orElseThrow(() -> new RuntimeException("Tour Not Found"));
        if (!file.isEmpty()) {
            String fileName = UUID.randomUUID() + file.getOriginalFilename();
            File dest = new File(imagePath + fileName);
            file.transferTo(dest);
            tour.setHinhAnh("/anh/chuyendi/" + fileName);
        }else {
            chuyenDi.setHinhAnh(tour.getHinhAnh());
        }
        tourService.update(id, chuyenDi);
        return "redirect:/admin/tour/{id}";
    }

    @GetMapping("/{id}")
    public String tourDetail(@PathVariable Integer id, Model model) {
        ChuyenDi tour = tourService.findById(id).orElseThrow(() -> new RuntimeException("Tour Not Found"));
        model.addAttribute("tour", tour);
        return "admin/tour/tour-detail";
    }
    @GetMapping("delete/{id}")
    public String delete(@PathVariable Integer id) {
        tourService.delete(id);
        return "redirect:/admin/tour";
    }
}
