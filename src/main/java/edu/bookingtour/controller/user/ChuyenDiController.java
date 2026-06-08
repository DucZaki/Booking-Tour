package edu.bookingtour.controller.user;

import edu.bookingtour.entity.*;
import edu.bookingtour.service.*;
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
import java.util.Map;

@Controller
public class ChuyenDiController {
    @Autowired
    private TourService tourService;
    @Autowired
    private DanhGiaService danhGiaService;
    @Autowired
    private NgayKhoiHanhService ngayKhoiHanhService;
    @Autowired
    private NgayKhoiHanhDiemDonService ngayKhoiHanhDiemDonService;
    @Autowired
    private NguoiDungService nguoiDungService;
    @Autowired
    private LichTrinhService lichTrinhService;
    @Autowired
    private BookingPricingService bookingPricingService;
    @Autowired
    private TourCapacityService tourCapacityService;
    @Autowired
    private DepartureBookingPolicy departureBookingPolicy;
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
        model.addAttribute("ngayDiSelected", ngayDi);
        model.addAttribute("thanhPhoSelected", thanhPho);
        model.addAttribute("quocGiaSelected", quocGia);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
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
            @RequestParam(required = false) String selectedDate,
            @RequestParam(required = false) String capacityError,
            @RequestParam(required = false) String bookingError) throws Exception {

        if ("full".equals(capacityError)) {
            model.addAttribute("capacityError", "Đợt tour này đã hết chỗ. Vui lòng chọn ngày khác.");
        }
        if ("closed".equals(bookingError)) {
            model.addAttribute("bookingError",
                    "Chuyến đi đã đóng đặt vé. Vui lòng đặt trước ít nhất "
                            + departureBookingPolicy.getCutoffHours()
                            + " giờ so với giờ tập trung (không đặt trong ngày khởi hành).");
        }
        if ("unavailable".equals(bookingError)) {
            model.addAttribute("bookingError",
                    "Ngày khởi hành này hiện không còn điểm xuất phát khả dụng. Vui lòng chọn ngày khác.");
        }

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

        // Ensure departure options are consistent for templates that rely on idDiemDon.
        tourService.normalizeTourDepartureOptions(chuyenDi);
        List<DiemDon> departureOptions = new java.util.ArrayList<>(chuyenDi.getDiemDons());
        departureOptions.sort(java.util.Comparator.comparing(DiemDon::getId));
        // Lấy ngày khởi hành do admin đã set cho tháng hiện tại (chỉ ngày còn điểm xuất phát bật)
        List<NgayKhoiHanh> departureDates = ngayKhoiHanhService.getDepartureDates(id, viewMonth, viewYear);
        departureDates = new java.util.ArrayList<>(ngayKhoiHanhDiemDonService.filterBookableDepartures(departureDates));

        java.util.Map<Integer, java.util.Set<Integer>> activeDepartureIdsByNkh = new java.util.HashMap<>();
        for (NgayKhoiHanh nkh : departureDates) {
            activeDepartureIdsByNkh.put(nkh.getId(), ngayKhoiHanhDiemDonService.getActiveDepartureIds(nkh.getId()));
        }

        Integer selectedDepartureId = null;

        // Tạo calendar với thông tin ngày khởi hành
        List<Calendar> calendar = tourService.getCalendar(viewMonth, viewYear, selectedDate, departureDates);
        Map<Integer, TourCapacityService.CapacitySnapshot> capacityByNkh =
                tourCapacityService.snapshotsForDepartures(departureDates);
        Map<Integer, NgayKhoiHanh> nkhById = new java.util.HashMap<>();
        for (NgayKhoiHanh nkh : departureDates) {
            nkhById.put(nkh.getId(), nkh);
        }
        for (Calendar day : calendar) {
            if (day.getNgayKhoiHanhId() != null) {
                TourCapacityService.CapacitySnapshot snap = capacityByNkh.get(day.getNgayKhoiHanhId());
                if (snap != null) {
                    day.setRemainingGuests(snap.getRemaining());
                    day.setSoldOut(snap.isSoldOut());
                }
                NgayKhoiHanh nkh = nkhById.get(day.getNgayKhoiHanhId());
                if (nkh != null && !departureBookingPolicy.isBookingOpen(nkh)) {
                    day.setBookingClosed(true);
                }
            }
        }

        // Tìm ngày khởi hành được chọn
        NgayKhoiHanh selectedNkh = null;
        double flightPrice = 0;
        if (selectedDate != null && !selectedDate.isEmpty()) {
            LocalDate localDate = LocalDate.parse(selectedDate);
            selectedNkh = ngayKhoiHanhService.findByChuyenDiAndNgay(id, localDate);
            if (selectedNkh != null && !ngayKhoiHanhDiemDonService.hasAnyActiveDeparturePoint(selectedNkh.getId())) {
                selectedNkh = null;
            }
            if (selectedNkh != null) {
                selectedDepartureId = ngayKhoiHanhDiemDonService.resolveDefaultDepartureId(departureOptions, selectedNkh.getId());
                flightPrice = selectedNkh.getTongGiaVe();
                if (selectedDepartureId != null) {
                    edu.bookingtour.dto.FlightQuoteResponse initialQuote = ngayKhoiHanhDiemDonService
                            .getQuote(selectedNkh.getId(), selectedDepartureId, false);
                    if (initialQuote.isAvailable()) {
                        flightPrice = initialQuote.getTongGiaVe();
                    }
                }
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
            if (selectedNkh != null) {
                model.addAttribute("selectedCapacity", tourCapacityService.getSnapshot(selectedNkh.getId()));
                model.addAttribute("selectedBookingOpen", departureBookingPolicy.isBookingOpen(selectedNkh));
            }
            model.addAttribute("capacityByNkh", capacityByNkh);
            model.addAttribute("bookingCutoffHours", departureBookingPolicy.getCutoffHours());
            model.addAttribute("price", flightPrice);
            model.addAttribute("departureOptions", departureOptions);
            model.addAttribute("selectedDepartureId", selectedDepartureId);
            model.addAttribute("activeDepartureIdsByNkh", activeDepartureIdsByNkh);
            if (selectedNkh != null) {
                model.addAttribute("activeDepartureIds",
                        activeDepartureIdsByNkh.getOrDefault(selectedNkh.getId(), java.util.Set.of()));
            }
            model.addAttribute("singleRoomSurcharge", bookingPricingService.getSingleRoomSurcharge(chuyenDi));

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

            List<LichTrinh> lichTrinhs = lichTrinhService.getByTour(id);
            model.addAttribute("lichTrinhs", lichTrinhs);

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

        TourCapacityService.CapacitySnapshot capacity = tourCapacityService.getSnapshot(nkhId);
        if (capacity.isSoldOut()) {
            return "redirect:/tour/" + tourId + "?capacityError=full";
        }
        if (!departureBookingPolicy.isBookingOpen(nkh)) {
            return "redirect:/tour/" + tourId + "?bookingError=closed";
        }
        if (!ngayKhoiHanhDiemDonService.hasAnyActiveDeparturePoint(nkhId)) {
            return "redirect:/tour/" + tourId + "?bookingError=unavailable";
        }

        tourService.normalizeTourDepartureOptions(chuyenDi);
        java.util.List<DiemDon> departureOptions = chuyenDi.getDiemDons() == null
                ? new java.util.ArrayList<>()
                : new java.util.ArrayList<>(chuyenDi.getDiemDons());
        departureOptions.sort(java.util.Comparator.comparing(DiemDon::getId));
        Integer selectedDepartureId = ngayKhoiHanhDiemDonService.resolveDefaultDepartureId(departureOptions, nkhId);
        java.util.Set<Integer> activeDepartureIds = ngayKhoiHanhDiemDonService.getActiveDepartureIds(nkhId);

        edu.bookingtour.dto.FlightQuoteResponse initialQuote = null;
        double baseTourPrice = chuyenDi.getGia() != null ? chuyenDi.getGia().doubleValue() : 0d;
        double baseFlightPrice = nkh.getTongGiaVe();
        double tongGia = baseTourPrice + baseFlightPrice;
        if (selectedDepartureId != null) {
            try {
                initialQuote = ngayKhoiHanhDiemDonService.getQuote(nkhId, selectedDepartureId, false);
                if (initialQuote != null && initialQuote.isAvailable()) {
                    tongGia = initialQuote.getUnitPrice();
                }
            } catch (Exception ignored) {
                // Nếu không lấy được quote theo điểm đón thì fallback về giá gốc.
            }
        }

        model.addAttribute("tour", chuyenDi);
        model.addAttribute("nkh", nkh);
        model.addAttribute("capacity", capacity);
        model.addAttribute("tongGia", tongGia);
        model.addAttribute("singleRoomSurcharge", bookingPricingService.getSingleRoomSurcharge(chuyenDi));
        model.addAttribute("initialQuote", initialQuote);
        model.addAttribute("principal", principal);

        model.addAttribute("departureOptions", departureOptions);
        model.addAttribute("selectedDepartureId", selectedDepartureId);
        model.addAttribute("activeDepartureIds", activeDepartureIds);

        // Tự điền thông tin user nếu đã đăng nhập
        if (principal != null) {
            nguoiDungService.findByTenDangNhap(principal.getName()).ifPresent(user -> {
                model.addAttribute("userHoTen", user.getHoTen());
                model.addAttribute("userEmail", user.getEmail());
                model.addAttribute("userSoDienThoai", user.getNumber());
            });
        }

        return "chuyendi/dat-tour";
    }
}
