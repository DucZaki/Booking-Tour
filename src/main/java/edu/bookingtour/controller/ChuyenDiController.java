package edu.bookingtour.controller;

import edu.bookingtour.entity.ChuyenDi;
import org.springframework.ui.Model;
import edu.bookingtour.repo.ChuyenDiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class ChuyenDiController {

    @Autowired
    private ChuyenDiRepository chuyendirepository;
//    @GetMapping("/all")
//    public String getnoibat(Model model) {
//        List<ChuyenDi> dsnoibat = chuyendirepository.findByNoiBat(true);
//        model.addAttribute("dsnoibatcd", dsnoibat);
//        return "index";
//    }
}
