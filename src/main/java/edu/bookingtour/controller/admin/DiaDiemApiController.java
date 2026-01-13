package edu.bookingtour.controller.admin;

import edu.bookingtour.entity.DiemDen;
import edu.bookingtour.repo.DiemDenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dia-diem")
public class DiaDiemApiController {

    @Autowired
    private DiemDenRepository diemDenRepository;

    @GetMapping("/quoc-gia")
    public List<String> getQuocGia(@RequestParam String chauLuc) {
        return diemDenRepository.findDistinctQuocGiaByChauLuc(chauLuc);
    }

    @GetMapping("/thanh-pho")
    public List<DiemDen> getThanhPho(@RequestParam String quocGia) {
        return diemDenRepository.findByQuocGia(quocGia);
    }
}

