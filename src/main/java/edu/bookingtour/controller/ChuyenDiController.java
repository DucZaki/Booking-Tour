package edu.bookingtour.controller;

import edu.bookingtour.client.TravelPayoutsClient;
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
    public String viewChitietDenPage(Model model, @PathVariable Long id) throws Exception {
        // SỬA: Lấy chi tiết tour và danh sách tour (nếu cần) thông qua Service
        List<ChuyenDi> dschuyendi = tourService.findAll();
        String from = "HAN";
        String to = "SGN";
        String date = "2026-01-13";
        try{
            double price = travelPayoutsClient.getCheapestPrice(from, to, date);
            String carriers = travelPayoutsClient.getCarrierCode(from, to, date);
            model.addAttribute("price", carriers);
            model.addAttribute("price", price);
        }
        catch(Exception e){
            model.addAttribute("Lỗi", e.getMessage());
        }
        model.addAttribute("dschuyendi", dschuyendi);
        model.addAttribute("id", tourService.findByIdd(Math.toIntExact(id)));
        return "chuyendi/chitiet";
    }

}