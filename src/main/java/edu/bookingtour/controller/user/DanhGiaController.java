package edu.bookingtour.controller.user;

import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.DatCho;
import edu.bookingtour.entity.NguoiDung;
import edu.bookingtour.repo.DatChoRepository;
import edu.bookingtour.service.DanhGiaService;
import edu.bookingtour.service.NguoiDungService;
import edu.bookingtour.service.TourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/danh-gia")
public class DanhGiaController {

    @Autowired
    private DanhGiaService danhGiaService;

    @Autowired
    private DatChoRepository datChoRepository;

    @Autowired
    private NguoiDungService nguoiDungService;

    @Autowired
    private TourService tourService;

    @PostMapping("/add")
    public String addDanhGia(@RequestParam Integer tourId, @RequestParam Integer diem, @RequestParam String binhLuan,
            Principal principal, RedirectAttributes redirectAttributes) {
        NguoiDung user = nguoiDungService.findByTenDangNhap(principal.getName()).orElse(null);
        ChuyenDi tour = tourService.findByIdd(tourId);

        if (user == null || tour == null) {
            return "redirect:/tour";
        }

        // Check if tour has ended
        if (tour.getNgayKetThuc() == null || tour.getNgayKetThuc().isAfter(LocalDate.now())) {
            redirectAttributes.addFlashAttribute("error", "Bạn chỉ có thể đánh giá sau khi chuyến đi đã kết thúc!");
            return "redirect:/tour/" + tourId;
        }

        // Check if user has a PAID booking for this tour
        List<DatCho> bookings = datChoRepository.findByIdNguoiDungAndIdChuyenDiAndTrangThai(user, tour, "PAID");
        if (bookings.isEmpty()) {
            redirectAttributes.addFlashAttribute("error",
                    "Bạn cần hoàn thành thanh toán cho tour này trước khi đánh giá!");
            return "redirect:/tour/" + tourId;
        }

        danhGiaService.save(tourId, diem, binhLuan, principal.getName());
        redirectAttributes.addFlashAttribute("success", "Cảm ơn bạn đã đánh giá!");
        return "redirect:/tour/" + tourId;
    }
}
