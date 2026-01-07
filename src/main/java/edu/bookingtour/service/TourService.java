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

//    public List<ChuyenDi> findAll() {
//        return chuyenDiRepository.findAll();
//    }

    public long count() {
        return chuyenDiRepository.count();
    }
    public ChuyenDi findByIdd(Integer id) {
        return chuyenDiRepository.findById(id).orElse(null);
    }
    public List<ChuyenDi> findAll() {
        return chuyenDiRepository.findAll();
    }
    public Optional<ChuyenDi> findById(Integer id) {
        return chuyenDiRepository.findById(id);
    }
    public ChuyenDi save(ChuyenDi chuyenDi, MultipartFile[] files) {
        ChuyenDi tour = new ChuyenDi();
        mapToChuyenDi(tour, chuyenDi, files);
        return chuyenDiRepository.save(tour);
    }
    public ChuyenDi update(Integer id, ChuyenDi chuyenDi, MultipartFile[] files) {
        ChuyenDi tour = chuyenDiRepository.findById(id).orElseThrow(()->new RuntimeException("ChuyenDi not found"));
        mapToChuyenDi(tour, chuyenDi, files);
        return chuyenDiRepository.save(tour);
    }
    public void delete(Integer id) {
        chuyenDiRepository.deleteById(id);
    }

    public void mapToChuyenDi(ChuyenDi tour, ChuyenDi chuyenDi, MultipartFile[] files) {
        tour.setTieuDe(chuyenDi.getTieuDe());
        tour.setMoTa(chuyenDi.getMoTa());
        tour.setGia(chuyenDi.getGia());
        tour.setNgayKhoiHanh(chuyenDi.getNgayKhoiHanh());
        tour.setNgayKetThuc(chuyenDi.getNgayKetThuc());
        tour.setIdDiemDen(chuyenDi.getIdDiemDen());
        tour.setIdPhuongTien(chuyenDi.getIdPhuongTien());
        tour.setIdNoiLuuTru(chuyenDi.getIdNoiLuuTru());
        tour.setNoiBat(chuyenDi.getNoiBat());
//        tour.getImages().clear();
//        for (MultipartFile file : files) {
//            if (!file.isEmpty()) {
//                Image img = new Image();
//                img.setImage_url(file.getOriginalFilename());
//                img.setTour(tour);
//                tour.getImages().add(img);
//            }
//        }
    }
}