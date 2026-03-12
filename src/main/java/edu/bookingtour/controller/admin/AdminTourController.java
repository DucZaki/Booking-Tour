package edu.bookingtour.controller.admin;

import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.repo.DiemDenRepository;
import edu.bookingtour.service.PhuongTienService;
import edu.bookingtour.service.TourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
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
    @Autowired
    private edu.bookingtour.repo.DiemDonRepository diemDonRepository;
    @Value("${image.path}")
    private String imagePath;

    @GetMapping("/active")
    public String activeTours(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "8") int perPage,
            Model model) {
        Page<ChuyenDi> tours = tourService.getActiveTours(page, perPage);
        model.addAttribute("tour", tours);
        model.addAttribute("totalPage", tours.getTotalPages());
        model.addAttribute("perPage", perPage);
        model.addAttribute("page", page);
        return "admin/tour/tour-active";
    }

    @GetMapping("/completed")
    public String completeTours(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int perPage, Model model) {
        Page<ChuyenDi> tours = tourService.getCompleteTours(page, perPage);
        model.addAttribute("tour", tours);
        model.addAttribute("totalPage", tours.getTotalPages());
        model.addAttribute("perPage", perPage);
        model.addAttribute("page", page);
        return "admin/tour/tour-complete";
    }

    @GetMapping("/extend/{id}")
    public String extendForm(@PathVariable Integer id, Model model) {
        ChuyenDi tour = tourService.findById(id).orElseThrow(() -> new RuntimeException("Tour Not Found"));
        model.addAttribute("tour", tour);
        return "admin/tour/tour-extend";
    }

    @PostMapping("/extend")
    public String extendTour(@RequestParam Integer id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayKhoiHanh,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayKetThuc) {
        ChuyenDi tour = tourService.findById(id).orElseThrow(() -> new RuntimeException("Tour Not Found"));
        tour.setNgayKhoiHanh(ngayKhoiHanh);
        tour.setNgayKetThuc(ngayKetThuc);
        tourService.save(tour);
        return "redirect:/admin/tour/active";
    }

    @GetMapping("/create")
    public String tourAdd(Model model) {
        ChuyenDi tour = new ChuyenDi();
        tour.setIdPhuongTien(new edu.bookingtour.entity.PhuongTien());
        tour.setIdDiemDon(new edu.bookingtour.entity.DiemDon());
        tour.setIdDiemDen(new edu.bookingtour.entity.DiemDen());
        model.addAttribute("tour", tour);
        model.addAttribute("phuongTienList", phuongTienService.getDistinctLoai());
        model.addAttribute("chauLucList", diemDenRepository.findDistinctChauLuc());
        model.addAttribute("diemDonList", diemDonRepository.findAll());
        return "admin/tour/tour-create";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute ChuyenDi chuyenDi, @RequestParam("file") MultipartFile file) throws IOException {
        if (!file.isEmpty()) {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            File dest = new File(imagePath + fileName);
            dest.getParentFile().mkdirs();
            file.transferTo(dest);
            chuyenDi.setHinhAnh("/uploads/" + fileName);
        }
        tourService.createTour(chuyenDi);
        return "redirect:/admin/tour/active";
    }

    @GetMapping("/edit/{id}")
    public String tourEdit(Model model, @PathVariable Integer id) {
        ChuyenDi tour = tourService.findById(id).orElseThrow(() -> new RuntimeException("Tour Not Found"));
        model.addAttribute("tour", tour);
        model.addAttribute("phuongTienList", phuongTienService.getDistinctLoai());
        model.addAttribute("chauLucList", diemDenRepository.findDistinctChauLuc());
        model.addAttribute("diemDonList", diemDonRepository.findAll());
        if (tour.getIdDiemDen() != null) {
            model.addAttribute("selectedChauLuc", tour.getIdDiemDen().getChauLuc());
            model.addAttribute("selectedQuocGia", tour.getIdDiemDen().getQuocGia());
            model.addAttribute("selectedThanhPho", tour.getIdDiemDen().getId());
        }
        return "admin/tour/tour-edit";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Integer id, @ModelAttribute ChuyenDi chuyenDi,
            @RequestParam("file") MultipartFile file) throws IOException {
        ChuyenDi tour = tourService.findById(id).orElseThrow(() -> new RuntimeException("Tour Not Found"));
        if (!file.isEmpty()) {
            String fileName = UUID.randomUUID() + file.getOriginalFilename();
            File dest = new File(imagePath + fileName);
            file.transferTo(dest);
            chuyenDi.setHinhAnh("/uploads/" + fileName);
        } else {
            chuyenDi.setHinhAnh(tour.getHinhAnh());
        }
        tourService.update(id, chuyenDi);
        return "redirect:/admin/tour/detail/" + id;
    }

    @GetMapping("/detail/{id}")
    public String tourDetail(@PathVariable Integer id,
            @RequestParam(required = false, defaultValue = "active") String source, Model model) {
        ChuyenDi tour = tourService.findById(id).orElseThrow(() -> new RuntimeException("Tour Not Found"));
        model.addAttribute("tour", tour);
        model.addAttribute("source", source);
        return "admin/tour/tour-detail";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, @RequestParam(defaultValue = "active") String source) {
        tourService.delete(id);
        return "redirect:/admin/tour/" + source;
    }
}
