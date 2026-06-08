package edu.bookingtour.controller.admin;

import edu.bookingtour.entity.NgayKhoiHanh;
import edu.bookingtour.repo.NgayKhoiHanhRepository;
import edu.bookingtour.service.TourCapacityService;
import edu.bookingtour.service.TourManifestService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lịch quản lý đoàn theo ngày — tất cả tour đang hoạt động.
 */
@Controller
@RequestMapping("/admin/departures")
public class AdminDepartureController {

    private final NgayKhoiHanhRepository ngayKhoiHanhRepository;
    private final TourCapacityService tourCapacityService;
    private final TourManifestService tourManifestService;

    public AdminDepartureController(NgayKhoiHanhRepository ngayKhoiHanhRepository,
            TourCapacityService tourCapacityService,
            TourManifestService tourManifestService) {
        this.ngayKhoiHanhRepository = ngayKhoiHanhRepository;
        this.tourCapacityService = tourCapacityService;
        this.tourManifestService = tourManifestService;
    }

    @GetMapping
    public String calendar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            Model model) {
        LocalDate today = LocalDate.now();
        LocalDate selectedDate = date != null ? date : today;
        int viewMonth = month != null ? month : selectedDate.getMonthValue();
        int viewYear = year != null ? year : selectedDate.getYear();

        YearMonth ym = YearMonth.of(viewYear, viewMonth);
        LocalDate rangeFrom = ym.atDay(1);
        LocalDate rangeTo = ym.atEndOfMonth();

        List<NgayKhoiHanh> monthDepartures = ngayKhoiHanhRepository.findActiveDeparturesInRange(rangeFrom, rangeTo);
        List<NgayKhoiHanh> dayDepartures = ngayKhoiHanhRepository.findAllDeparturesOnDate(selectedDate);

        Map<Integer, TourCapacityService.CapacitySnapshot> capacityByNkh =
                tourCapacityService.snapshotsForDepartures(monthDepartures);

        Map<LocalDate, Long> countByDate = new HashMap<>();
        for (NgayKhoiHanh nkh : monthDepartures) {
            countByDate.merge(nkh.getNgay(), 1L, Long::sum);
        }

        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("viewMonth", viewMonth);
        model.addAttribute("viewYear", viewYear);
        model.addAttribute("monthDepartures", monthDepartures);
        model.addAttribute("dayDepartures", dayDepartures);
        model.addAttribute("capacityByNkh", capacityByNkh);
        model.addAttribute("countByDate", countByDate);
        model.addAttribute("today", today);
        model.addAttribute("guides", tourManifestService.listGuides());
        return "admin/departure/departure-calendar";
    }
}
