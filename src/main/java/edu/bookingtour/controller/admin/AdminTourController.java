package edu.bookingtour.controller.admin;

import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.repo.DiemDenRepository;
import edu.bookingtour.repo.NoiLuuTruRepository;
import edu.bookingtour.service.LichTrinhService;
import edu.bookingtour.service.PhuongTienService;
import edu.bookingtour.service.TourBootstrapService;
import edu.bookingtour.service.TourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/tour")
public class AdminTourController {
    @Autowired
    private TourService tourService;
    @Autowired
    private PhuongTienService phuongTienService;
    @Autowired
    private LichTrinhService lichTrinhService;
    @Autowired
    private DiemDenRepository diemDenRepository;
    @Autowired
    private edu.bookingtour.repo.DiemDonRepository diemDonRepository;
    @Autowired
    private NoiLuuTruRepository noiLuuTruRepository;
    @Autowired
    private TourBootstrapService tourBootstrapService;
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
        tour.setIdNoiLuuTru(new edu.bookingtour.entity.NoiLuuTru());
        model.addAttribute("tour", tour);
        model.addAttribute("phuongTienList", phuongTienService.getDistinctLoai());
        model.addAttribute("chauLucList", diemDenRepository.findDistinctChauLuc());
        model.addAttribute("diemDonList", diemDonRepository.findAll());
        model.addAttribute("noiLuuTruList", noiLuuTruRepository.findAll());
        model.addAttribute("defaultGiaPhongDon", BigDecimal.valueOf(500000));
        return "admin/tour/tour-create";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute ChuyenDi chuyenDi,
            @RequestParam(value = "departureIds", required = false) java.util.List<Integer> departureIds,
            @RequestParam(value = "giaPhongDon", required = false) BigDecimal giaPhongDon,
            @RequestParam(value = "file", required = false) MultipartFile file,
            RedirectAttributes redirectAttributes) throws IOException {
        if (file == null || file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn ảnh tour.");
            return "redirect:/admin/tour/create";
        }
        if (!file.isEmpty()) {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            File dest = new File(imagePath + fileName);
            dest.getParentFile().mkdirs();
            file.transferTo(dest);
            chuyenDi.setHinhAnh("/uploads/" + fileName);
        }
        if (departureIds != null && !departureIds.isEmpty()) {
            java.util.Set<edu.bookingtour.entity.DiemDon> deps = new java.util.HashSet<>();
            for (Integer depId : departureIds) {
                diemDonRepository.findById(depId).ifPresent(deps::add);
            }
            chuyenDi.setDiemDons(deps);
        }
        saveSingleRoomPrice(chuyenDi, giaPhongDon);
        tourService.createTour(chuyenDi);
        return "redirect:/admin/tour/active";
    }

    @GetMapping("/edit/{id}")
    public String tourEdit(Model model, @PathVariable Integer id) {
        ChuyenDi tour = tourService.findById(id).orElseThrow(() -> new RuntimeException("Tour Not Found"));
        if (tour.getIdNoiLuuTru() == null) {
            tour.setIdNoiLuuTru(new edu.bookingtour.entity.NoiLuuTru());
        }
        if (tour.getSucChuaMacDinh() == null) {
            tour.setSucChuaMacDinh(edu.bookingtour.service.TourCapacityService.DEFAULT_CAPACITY);
        }
        model.addAttribute("tour", tour);
        model.addAttribute("phuongTienList", phuongTienService.getDistinctLoai());
        model.addAttribute("chauLucList", diemDenRepository.findDistinctChauLuc());
        model.addAttribute("diemDonList", diemDonRepository.findAll());
        model.addAttribute("noiLuuTruList", noiLuuTruRepository.findAll());
        model.addAttribute("defaultGiaPhongDon", BigDecimal.valueOf(500000));
        model.addAttribute("selectedDepartureIds", resolveSelectedDepartureIds(tour));
        if (tour.getIdDiemDen() != null) {
            model.addAttribute("selectedChauLuc", tour.getIdDiemDen().getChauLuc());
            model.addAttribute("selectedQuocGia", tour.getIdDiemDen().getQuocGia());
            model.addAttribute("selectedThanhPho", tour.getIdDiemDen().getId());
        }
        return "admin/tour/tour-edit";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Integer id, @ModelAttribute ChuyenDi chuyenDi,
            @RequestParam(value = "departureIds", required = false) java.util.List<Integer> departureIds,
            @RequestParam(value = "giaPhongDon", required = false) BigDecimal giaPhongDon,
            @RequestParam(value = "file", required = false) MultipartFile file,
            RedirectAttributes redirectAttributes) throws IOException {
        ChuyenDi tour = tourService.findById(id).orElseThrow(() -> new RuntimeException("Tour Not Found"));
        if (file != null && !file.isEmpty()) {
            String fileName = UUID.randomUUID() + file.getOriginalFilename();
            File dest = new File(imagePath + fileName);
            file.transferTo(dest);
            chuyenDi.setHinhAnh("/uploads/" + fileName);
        } else {
            chuyenDi.setHinhAnh(tour.getHinhAnh());
        }
        if (departureIds != null) {
            java.util.Set<edu.bookingtour.entity.DiemDon> deps = new java.util.HashSet<>();
            for (Integer depId : departureIds) {
                diemDonRepository.findById(depId).ifPresent(deps::add);
            }
            chuyenDi.setDiemDons(deps);
        }
        saveSingleRoomPrice(chuyenDi, giaPhongDon);
        try {
            tourService.update(id, chuyenDi);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật chuyến đi.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi cập nhật: " + ex.getMessage());
            return "redirect:/admin/tour/edit/" + id;
        }
        return "redirect:/admin/tour/detail/" + id + "?source=active";
    }

    @GetMapping("/detail/{id}")
    public String tourDetail(@PathVariable Integer id, @RequestParam(required = false, defaultValue = "active") String source, Model model) {
        ChuyenDi tour = tourService.findById(id).orElseThrow(() -> new RuntimeException("Tour Not Found"));
        model.addAttribute("tour", tour);
        model.addAttribute("source", source);
        model.addAttribute("lichTrinhs", lichTrinhService.getByTour(id));
        model.addAttribute("noiLuuTruList", noiLuuTruRepository.findAll());
        model.addAttribute("defaultGiaPhongDon", BigDecimal.valueOf(500000));
        return "admin/tour/tour-detail";
    }

    @PostMapping("/{id}/khach-san")
    public String updateHotel(@PathVariable Integer id,
            @RequestParam(value = "noiLuuTruId", required = false) Integer noiLuuTruId,
            @RequestParam(value = "giaPhongDon", required = false) BigDecimal giaPhongDon,
            @RequestParam(required = false, defaultValue = "active") String source,
            RedirectAttributes redirectAttributes) {
        ChuyenDi tour = tourService.findById(id).orElseThrow(() -> new RuntimeException("Tour Not Found"));
        if (noiLuuTruId != null) {
            noiLuuTruRepository.findById(noiLuuTruId).ifPresent(noiLuuTru -> {
                noiLuuTru.setGiaPhongDon(giaPhongDon != null ? giaPhongDon : BigDecimal.valueOf(500000));
                noiLuuTruRepository.save(noiLuuTru);
                tour.setIdNoiLuuTru(noiLuuTru);
                tourService.save(tour);
            });
        } else {
            tour.setIdNoiLuuTru(null);
            tourService.save(tour);
        }
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật khách sạn và giá phòng đơn.");
        return "redirect:/admin/tour/detail/" + id + "?source=" + source + "&tab=hotel";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, @RequestParam(defaultValue = "active") String source) {
        tourService.delete(id);
        return "redirect:/admin/tour/" + source;
    }

    @PostMapping("/bootstrap/activate-all")
    public String bootstrapActivateAll(org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        TourBootstrapService.BootstrapResult result = tourBootstrapService.activateAllToursForCurrentMonth();
        redirectAttributes.addFlashAttribute("successMessage",
                "Đã kích hoạt " + result.totalTours() + " tour, thêm " + result.departuresAdded()
                        + " ngày khởi hành (T" + result.month() + "/" + result.year() + ").");
        return "redirect:/admin/tour/active";
    }

    private Set<Integer> resolveSelectedDepartureIds(ChuyenDi tour) {
        if (tour.getDiemDons() == null || tour.getDiemDons().isEmpty()) {
            return Set.of();
        }
        return tour.getDiemDons().stream()
                .map(edu.bookingtour.entity.DiemDon::getId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private void saveSingleRoomPrice(ChuyenDi chuyenDi, BigDecimal giaPhongDon) {
        if (chuyenDi.getIdNoiLuuTru() == null || chuyenDi.getIdNoiLuuTru().getId() == null) {
            return;
        }
        noiLuuTruRepository.findById(chuyenDi.getIdNoiLuuTru().getId()).ifPresent(noiLuuTru -> {
            noiLuuTru.setGiaPhongDon(giaPhongDon != null ? giaPhongDon : BigDecimal.valueOf(500000));
            noiLuuTruRepository.save(noiLuuTru);
        });
    }
}
