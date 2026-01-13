package edu.bookingtour.service;

import edu.bookingtour.entity.PhuongTien;
import edu.bookingtour.repo.PhuongTienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PhuongTienService {
    @Autowired
    private PhuongTienRepository phuongTienRepository;
    public List<PhuongTien> getAllPhuongTien() {
        return phuongTienRepository.findAll();
    }

    public List<PhuongTien> getDistinctLoai() {
        return phuongTienRepository.findDistinctLoai();
    }
}
