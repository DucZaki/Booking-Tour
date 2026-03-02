package edu.bookingtour.controller.user;

import edu.bookingtour.client.TravelPayoutsClient;
import edu.bookingtour.entity.Calendar;
import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.DanhGia;
import edu.bookingtour.service.DanhGiaService;
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
    private TourService tourService; // Thêm Service
    @Autowired
    private DanhGiaService danhGiaService;
    @Autowired
    private TravelPayoutsClient travelPayoutsClient;
    @Autowired
    private NguoiDungService nguoiDungService;
    @GetMapping("/tour")
    public String viewDiemDenPage(@RequestParam(required = false) String thanhPho, @RequestParam(required = false) String quocGia, @RequestParam(required = false) String diemDen, @RequestParam(required = false) String khoangGia, @RequestParam(required = false) String sort, @RequestParam(required = false) String ngayDi, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, Model model) {
        Page<ChuyenDi> dschuyendi = tourService.filterAndSort(thanhPho, quocGia,diemDen, khoangGia, ngayDi, sort, page, size);
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

    // Phương thức viewChitietDenPage cũng nên sử dụng Service
    @GetMapping("/tour/{id}")
    public String viewChitietDenPage(Model model,
                                     @PathVariable Integer id,
                                     @RequestParam(defaultValue = "1") Integer month,
                                     @RequestParam(defaultValue = "2026") Integer year,
                                     Principal principal,
                                     @RequestParam(required = false) String selectedDate) throws Exception {

        int viewMonth = (month != null) ? month : LocalDate.now().getMonthValue();
        int viewYear = (year != null) ? year : LocalDate.now().getYear();
        // Nếu có ngày được chọn
        LocalDate localDate;
        if (selectedDate != null && !selectedDate.isEmpty()) {
            localDate = LocalDate.parse(selectedDate); // ✅ Chuyển chuỗi "2026-01-10" thành LocalDate
        } else {
            localDate = LocalDate.now(); // nếu chưa chọn thì mặc định là ngày hiện tại
        }
        String Date = localDate.toString(); // yyyy-MM-dd
        List<Calendar> calendar = tourService.getCalendar(viewMonth, viewYear, selectedDate);
        List<ChuyenDi> dschuyendi = tourService.findAll();

        String from = "HAN";
        String to = "SGN";

        try {
            double FlightPrice = travelPayoutsClient.getCheapestPrice(from, to, Date);
            String carriers = travelPayoutsClient.getCarrierCode(from, to, Date);
            String Origin = travelPayoutsClient.takeorigin(from, to, Date);
            String departure = travelPayoutsClient.getdeparture(from, to, Date);
//            String username = nguoiDungService.findByTenDangNhap()
            model.addAttribute("carrier", carriers);
            model.addAttribute("price", FlightPrice);
            model.addAttribute("date", departure);
            model.addAttribute("origin", Origin);
            model.addAttribute("calendar", calendar);
            model.addAttribute("currentMonth", viewMonth);
            model.addAttribute("currentYear", viewYear);
            model.addAttribute("dschuyendi", dschuyendi);
            model.addAttribute("selectedDate", selectedDate);
            model.addAttribute("id", tourService.findByIdd(Math.toIntExact(id)));

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


}