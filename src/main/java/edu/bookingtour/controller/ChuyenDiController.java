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
    public String viewChitietDenPage(Model model, @PathVariable Long id,
    @RequestParam(defaultValue="1") Integer month,
    @RequestParam(defaultValue="2026") Integer year,
    @RequestParam(required = false ) String selectedDate) throws Exception {
        int viewMonth = (month != null) ? month : LocalDate.now().getMonthValue();
        int viewYear = (year != null) ? year : LocalDate.now().getYear();
        LocalDate localDate = LocalDate.of(viewYear, viewMonth, Integer.parseInt(selectedDate));
        String Date = localDate.toString();
        List<Calendar> calendar = tourService.getCalendar(viewMonth, viewYear,selectedDate);
        List<ChuyenDi> dschuyendi = tourService.findAll();
        String from = "HAN";
        String to = "SGN";
        double flightPrice=0;
        if (selectedDate != null) {
            LocalDate selDate = LocalDate.parse(selectedDate);
            for (Calendar day : calendar) {
                if (day.getDate().equals(selDate)) {
                    day.setSelected(true);
                    double price = travelPayoutsClient.getCheapestPrice(from, to, Date);
                    flightPrice=price;
                }
            }
        }
        try{
            double FlightPrice = travelPayoutsClient.getCheapestPrice(from, to, Date);
            String carriers = travelPayoutsClient.getCarrierCode(from, to, Date);
            String Origin= travelPayoutsClient.takeorigin(from, to, Date);
            String departure =travelPayoutsClient.getdeparture(from, to, Date);
            model.addAttribute("carrier", carriers);
            model.addAttribute("price", FlightPrice);
            model.addAttribute("date", departure);
            model.addAttribute("origin", Origin);
            model.addAttribute("calendar", calendar);
        }
        catch(Exception e){
            model.addAttribute("Lỗi", e.getMessage());
        }
        model.addAttribute("dschuyendi", dschuyendi);
        model.addAttribute("id", tourService.findByIdd(Math.toIntExact(id)));
        return "chuyendi/chitiet";
    }

}