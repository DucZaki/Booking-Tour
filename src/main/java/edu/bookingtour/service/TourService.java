package edu.bookingtour.service;

import edu.bookingtour.entity.Calendar;
import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.repo.ChuyenDiRepository;
import edu.bookingtour.repo.DiemDenRepository;
import edu.bookingtour.repo.NoiLuuTruRepository;
import edu.bookingtour.repo.PhuongTienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TourService {
    @Autowired
    private ChuyenDiRepository chuyenDiRepository;
    @Autowired
    private PhuongTienRepository phuongTienRepository;

    @Autowired
    private DiemDenRepository diemDenRepository;

    @Autowired
    private NoiLuuTruRepository noiLuuTruRepository;
    public long count() {
        return chuyenDiRepository.count();
    }
    public ChuyenDi findByIdd(Integer id) {return chuyenDiRepository.findById(id).orElse(null);}
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
        tour.setNoiBat(chuyenDi.getNoiBat());
        if(chuyenDi.getIdPhuongTien() != null) {
            tour.setIdPhuongTien(phuongTienRepository.findById(chuyenDi.getIdPhuongTien().getId()).orElse(null));
        }
        if(chuyenDi.getIdDiemDen() != null) {
            tour.setIdDiemDen(diemDenRepository.findById(chuyenDi.getIdDiemDen().getId()).orElse(null));
        }
        if(chuyenDi.getIdNoiLuuTru() != null) {
            tour.setIdNoiLuuTru(noiLuuTruRepository.findById(chuyenDi.getIdNoiLuuTru().getId()).orElse(null));
        }
    }
    public List<Calendar> getCalendar(int month, int year, String selectedDateStr) {
        List<Calendar> days = new ArrayList<>();
        LocalDate today = LocalDate.now(); // Ngày hiện tại: 2026-01-10
        LocalDate firstOfMonth = LocalDate.of(year, month, 1);
        LocalDate start = firstOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        LocalDate selectedDate = null;
        if (selectedDateStr != null && !selectedDateStr.isEmpty()) {
            selectedDate = LocalDate.parse(selectedDateStr);
        }

        for (int i = 0; i < 42; i++) {
            LocalDate current = start.plusDays(i);
            Calendar day = new Calendar();
            day.setDate(current);
            day.setCurrentMonth(current.getMonthValue() == month);

            // 1. Kiểm tra ngày đã qua (isPast)
            // Nếu ngày hiện tại trong vòng lặp trước ngày hôm nay -> True
            day.setPast(current.isBefore(today));

            // 2. Kiểm tra ngày được chọn
            if (selectedDate != null && current.equals(selectedDate)) {
                day.setSelected(true);
            } else {
                day.setSelected(false);
                day.setFlightPrice(0.0);
            }

            days.add(day);
        }
        return days;
    }

    public Page<ChuyenDi> getAllChuyenDi(int page, int perPage) {
        Pageable pageable = PageRequest.of(page, perPage);
        return chuyenDiRepository.findAll(pageable);
    }
}