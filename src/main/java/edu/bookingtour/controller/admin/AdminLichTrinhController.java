package edu.bookingtour.controller.admin;

import edu.bookingtour.entity.LichTrinh;
import edu.bookingtour.service.LichTrinhService;
import edu.bookingtour.service.TourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/lich-trinh")
public class AdminLichTrinhController {

    @Autowired
    private LichTrinhService lichTrinhService;

    @Autowired
    private TourService tourService;

    @GetMapping("/create/{tourId}")
    public String createForm(@PathVariable Integer tourId, Model model) {
        LichTrinh lt = new LichTrinh();
        lt.setTourId(tourId);

        model.addAttribute("lichTrinh", lt);
        model.addAttribute("tour",
                tourService.findById(tourId).orElseThrow(() -> new IllegalArgumentException("Tour not found")));
        return "admin/tour/tour-detail";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute LichTrinh lichTrinh) {
        lichTrinhService.create(lichTrinh);
        return "redirect:/admin/tour/detail/" + lichTrinh.getTourId();
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        LichTrinh lt = lichTrinhService.getDetail(id);
        model.addAttribute("tour", tourService.findById(lt.getTourId()).orElseThrow());
        model.addAttribute("lichTrinhs", lichTrinhService.getByTour(lt.getTourId()));
        model.addAttribute("editingId", id);
        model.addAttribute("lichTrinh", lt);
        return "admin/tour/tour-detail";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Integer id, @ModelAttribute LichTrinh lichTrinh) {
        lichTrinhService.update(id, lichTrinh);
        return "redirect:/admin/tour/detail/" + lichTrinh.getTourId();
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        LichTrinh lt = lichTrinhService.getDetail(id);
        Integer tourId = lt.getTourId();
        lichTrinhService.delete(id);
        return "redirect:/admin/tour/detail/" + tourId;
    }
}
