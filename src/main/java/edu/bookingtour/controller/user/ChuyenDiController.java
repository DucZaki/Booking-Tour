package edu.bookingtour.controller.user;

import edu.bookingtour.entity.Calendar;
import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.DanhGia;
import edu.bookingtour.entity.NgayKhoiHanh;
import edu.bookingtour.service.DanhGiaService;
import edu.bookingtour.service.NgayKhoiHanhService;
import edu.bookingtour.service.NguoiDungService;
import edu.bookingtour.service.TourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class ChuyenDiController {
    @Autowired
    private TourService tourService;
    @Autowired
    private DanhGiaService danhGiaService;
    @Autowired
    private NgayKhoiHanhService ngayKhoiHanhService;
    @Autowired
    private NguoiDungService nguoiDungService;

    @GetMapping("/tour")
    public String viewDiemDenPage(@RequestParam(required = false) String thanhPho,
            @RequestParam(required = false) String quocGia, @RequestParam(required = false) String diemDen,
            @RequestParam(required = false) String khoangGia, @RequestParam(required = false) String sort,
            @RequestParam(required = false) String ngayDi, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, Model model) {
        Page<ChuyenDi> dschuyendi = tourService.filterAndSort(thanhPho, quocGia, diemDen, khoangGia, ngayDi, sort, page,
                size);
        model.addAttribute("dschuyendi", dschuyendi);
        model.addAttribute("dem", dschuyendi.getTotalElements());
        model.addAttribute("diemDenSelected", diemDen);
        model.addAttribute("khoangGiaSelected", khoangGia);
        model.addAttribute("sortSelected", sort);
        model.addAttribute("page", page);
        model.addAttribute("perPage", size);
        model.addAttribute("totalPage", dschuyendi.getTotalPages());
        return "chuyendi/tour";
    }

    @GetMapping("/tour/{id}")
    public String viewChitietDenPage(Model model,
            @PathVariable Integer id,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            Principal principal,
            @RequestParam(required = false) String selectedDate) throws Exception {

        // Tính 3 tháng bắt đầu từ tháng hiện tại
        LocalDate now = LocalDate.now();
        int startMonth = now.getMonthValue();
        int startYear = now.getYear();

        // Default: tháng hiện tại
        int viewMonth = (month != null) ? month : startMonth;
        int viewYear = (year != null) ? year : startYear;

        // Giới hạn chỉ cho phép 3 tháng từ tháng hiện tại
        int[] months = new int[3];
        int[] years = new int[3];
        for (int i = 0; i < 3; i++) {
            int m = startMonth + i;
            int y = startYear;
            if (m > 12) {
                m -= 12;
                y++;
            }
            months[i] = m;
            years[i] = y;
        }

        ChuyenDi chuyenDi = tourService.findByIdd(Math.toIntExact(id));
        if (chuyenDi == null) {
            return "redirect:/tour";
        }

        // Lấy ngày khởi hành do admin đã set cho tháng hiện tại
        List<NgayKhoiHanh> departureDates = ngayKhoiHanhService.getDepartureDates(id, viewMonth, viewYear);

        // Tạo calendar với thông tin ngày khởi hành
        List<Calendar> calendar = tourService.getCalendar(viewMonth, viewYear, selectedDate, departureDates);

        // Tìm ngày khởi hành được chọn
        NgayKhoiHanh selectedNkh = null;
        double flightPrice = 0;
        if (selectedDate != null && !selectedDate.isEmpty()) {
            LocalDate localDate = LocalDate.parse(selectedDate);
            selectedNkh = ngayKhoiHanhService.findByChuyenDiAndNgay(id, localDate);
            if (selectedNkh != null) {
                flightPrice = selectedNkh.getTongGiaVe();
            }
        }

        try {
            model.addAttribute("calendar", calendar);
            model.addAttribute("currentMonth", viewMonth);
            model.addAttribute("currentYear", viewYear);
            model.addAttribute("selectedDate", selectedDate);
            model.addAttribute("id", chuyenDi);
            model.addAttribute("departureDates", departureDates);
            model.addAttribute("selectedNkh", selectedNkh);
            model.addAttribute("price", flightPrice);

            // 3 tháng cho sidebar
            model.addAttribute("months", months);
            model.addAttribute("years", years);

            // Đánh giá
            List<DanhGia> danhGiaList = danhGiaService.findByTourId(id);
            double avg = danhGiaList.stream().mapToInt(DanhGia::getDiem).average().orElse(0);
            DanhGia userReview = null;
            if (principal != null) {
                userReview = danhGiaService.findUserReview(id, principal.getName());
            }
            model.addAttribute("userReview", userReview);
            model.addAttribute("avgRating", Math.round(avg));
            model.addAttribute("totalReview", danhGiaList.size());
            model.addAttribute("danhGiaList", danhGiaList);

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }

        return "chuyendi/chitiet";
    }

    /**
     * Trang đặt tour (booking form)
     */
    @GetMapping("/tour/{tourId}/dat-tour")
    public String bookingForm(@PathVariable Integer tourId,
            @RequestParam Integer nkhId,
            Principal principal,
            Model model) {
        ChuyenDi chuyenDi = tourService.findByIdd(tourId);
        if (chuyenDi == null)
            return "redirect:/tour";

        NgayKhoiHanh nkh = ngayKhoiHanhService.findById(nkhId);
        if (nkh == null)
            return "redirect:/tour/" + tourId;

        double tongGia = chuyenDi.getGia().doubleValue() + nkh.getTongGiaVe();

        model.addAttribute("tour", chuyenDi);
        model.addAttribute("nkh", nkh);
        model.addAttribute("tongGia", tongGia);
        model.addAttribute("principal", principal);

        return "chuyendi/dat-tour";
    }
}