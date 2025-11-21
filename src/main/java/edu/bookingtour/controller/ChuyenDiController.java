package edu.bookingtour.controller;

import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.repo.ChuyenDiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class ChuyenDiController {

    @Autowired
    private ChuyenDiRepository chuyendirepository;
    @GetMapping("/tour")
    public String viewDiemDenPage(Model model) {
        List<ChuyenDi> dschuyendi = chuyendirepository.findAll();
        long dem=chuyendirepository.count();
        model.addAttribute("dschuyendi", dschuyendi);
        model.addAttribute("dem",dem);
        return "chuyendi/tour";
    }
    @GetMapping("/tour/{id}")
    public String viewChitietDenPage(Model model, @PathVariable Long id) {
        List<ChuyenDi>  dschuyendi = chuyendirepository.findAll();
        model.addAttribute("dschuyendi", dschuyendi);
        model.addAttribute("id",chuyendirepository.findById(Math.toIntExact(id)).orElse(null));
        return "chuyendi/chitiet";
    }
}
