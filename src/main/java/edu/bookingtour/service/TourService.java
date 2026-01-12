package edu.bookingtour.service;

import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.repo.ChuyenDiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
public class TourService {
    @Autowired
    private ChuyenDiRepository chuyenDiRepository;
    public long count() {
        return chuyenDiRepository.count();
    }
    public Optional<ChuyenDi> findById(Integer id) {
        return chuyenDiRepository.findById(id);
    }
    public List<ChuyenDi> findAll() {
        return chuyenDiRepository.findAll();
    }
    public ChuyenDi save(ChuyenDi chuyenDi) {
        ChuyenDi tour = new ChuyenDi();
        mapToChuyenDi(tour, chuyenDi);
        return chuyenDiRepository.save(tour);
    }
    public ChuyenDi update(Integer id, ChuyenDi chuyenDi) {
        ChuyenDi tour = chuyenDiRepository.findById(id).orElseThrow(()->new RuntimeException("ChuyenDi not found"));
        mapToChuyenDi(tour, chuyenDi);
        return chuyenDiRepository.save(tour);
    }
    public void delete(Integer id) {
        chuyenDiRepository.deleteById(id);
    }

    public void mapToChuyenDi(ChuyenDi tour, ChuyenDi chuyenDi) {
        tour.setTieuDe(chuyenDi.getTieuDe());
        tour.setMoTa(chuyenDi.getMoTa());
        tour.setGia(chuyenDi.getGia());
        tour.setHinhAnh(chuyenDi.getHinhAnh());
        tour.setNgayKhoiHanh(chuyenDi.getNgayKhoiHanh());
        tour.setNgayKetThuc(chuyenDi.getNgayKetThuc());
        tour.setIdDiemDen(chuyenDi.getIdDiemDen());
        tour.setIdPhuongTien(chuyenDi.getIdPhuongTien());
        tour.setIdNoiLuuTru(chuyenDi.getIdNoiLuuTru());
        tour.setNoiBat(chuyenDi.getNoiBat());
    }
}