package edu.bookingtour.controller;

import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.repo.DiemDenRepository;
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
    @GetMapping("/tour")
    public String viewDiemDenPage() {
        return "chuyendi/tour";
    }
}
