package edu.bookingtour.controller;

import edu.bookingtour.client.TravelPayoutsClient;
import edu.bookingtour.entity.Calendar;
import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.repo.ChuyenDiRepository;
import edu.bookingtour.service.TourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Controller
public class ChuyenDiController {
    @Autowired
    private TourService tourService; // Thêm Service
    @Autowired
    private TravelPayoutsClient travelPayoutsClient;
    @GetMapping("/tour")
    public String viewDiemDenPage(Model model) {

        List<ChuyenDi> dschuyendi = tourService.findAll();
        long dem = tourService.count();
        model.addAttribute("dschuyendi", dschuyendi);
        model.addAttribute("dem", dem);
        return "chuyendi/tour";
    }

    // Phương thức viewChitietDenPage cũng nên sử dụng Service
    @GetMapping("/tour/{id}")
    public String viewChitietDenPage(Model model,
                                     @PathVariable Long id,
                                     @RequestParam(defaultValue = "1") Integer month,
                                     @RequestParam(defaultValue = "2026") Integer year,
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

            model.addAttribute("carrier", carriers);
            model.addAttribute("price", FlightPrice);
            model.addAttribute("date", departure);
            model.addAttribute("origin", Origin);
            model.addAttribute("calendar", calendar);
            model.addAttribute("currentMonth", viewMonth);
            model.addAttribute("currentYear", viewYear);
            model.addAttribute("dschuyendi", dschuyendi);
            model.addAttribute("id", tourService.findById(Math.toIntExact(id)));
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }

        return "chuyendi/chitiet";
    }


}