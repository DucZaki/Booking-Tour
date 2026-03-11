package edu.bookingtour.service;

import edu.bookingtour.entity.Calendar;
import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.NgayKhoiHanh;
import edu.bookingtour.repo.ChuyenDiRepository;
import edu.bookingtour.repo.DiemDenRepository;
import edu.bookingtour.repo.DiemDonRepository;
import edu.bookingtour.repo.NoiLuuTruRepository;
import edu.bookingtour.repo.PhuongTienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

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

    @Autowired
    private DiemDonRepository diemDonRepository;

    public ChuyenDi findByIdd(Integer id) {
        return chuyenDiRepository.findById(id).orElse(null);
    }

    public Optional<ChuyenDi> findById(Integer id) {
        return chuyenDiRepository.findById(id);
    }

    public List<ChuyenDi> findAll() {
        return chuyenDiRepository.findAll();
    }

    public ChuyenDi save(ChuyenDi chuyenDi) {
        return chuyenDiRepository.save(chuyenDi);
    }

    public ChuyenDi createTour(ChuyenDi chuyenDi) {
        if (chuyenDi.getIdPhuongTien() != null && chuyenDi.getIdPhuongTien().getId() != null) {
            chuyenDi.setIdPhuongTien(phuongTienRepository.findById(chuyenDi.getIdPhuongTien().getId()).orElse(null));
        }
        if (chuyenDi.getIdDiemDen() != null && chuyenDi.getIdDiemDen().getId() != null) {
            chuyenDi.setIdDiemDen(diemDenRepository.findById(chuyenDi.getIdDiemDen().getId()).orElse(null));
        }
        if (chuyenDi.getIdDiemDon() != null && chuyenDi.getIdDiemDon().getId() != null) {
            chuyenDi.setIdDiemDon(diemDonRepository.findById(chuyenDi.getIdDiemDon().getId()).orElse(null));
        }
        return chuyenDiRepository.save(chuyenDi);
    }

    public ChuyenDi update(Integer id, ChuyenDi chuyenDi) {
        ChuyenDi tour = chuyenDiRepository.findById(id).orElseThrow(() -> new RuntimeException("ChuyenDi not found"));
        tour.setTieuDe(chuyenDi.getTieuDe());
        tour.setMoTa(chuyenDi.getMoTa());
        tour.setGia(chuyenDi.getGia());
        tour.setHinhAnh(chuyenDi.getHinhAnh());
        tour.setNgayKhoiHanh(chuyenDi.getNgayKhoiHanh());
        tour.setNgayKetThuc(chuyenDi.getNgayKetThuc());
        tour.setNoiBat(chuyenDi.getNoiBat());
        tour.setHighlight(chuyenDi.getHighlight());

        if (chuyenDi.getIdPhuongTien() != null && chuyenDi.getIdPhuongTien().getId() != null) {
            tour.setIdPhuongTien(phuongTienRepository.findById(chuyenDi.getIdPhuongTien().getId()).orElse(null));
        }
        if (chuyenDi.getIdDiemDen() != null && chuyenDi.getIdDiemDen().getId() != null) {
            tour.setIdDiemDen(diemDenRepository.findById(chuyenDi.getIdDiemDen().getId()).orElse(null));
        }
        if (chuyenDi.getIdNoiLuuTru() != null && chuyenDi.getIdNoiLuuTru().getId() != null) {
            tour.setIdNoiLuuTru(noiLuuTruRepository.findById(chuyenDi.getIdNoiLuuTru().getId()).orElse(null));
        }
        if (chuyenDi.getIdDiemDon() != null && chuyenDi.getIdDiemDon().getId() != null) {
            tour.setIdDiemDon(diemDonRepository.findById(chuyenDi.getIdDiemDon().getId()).orElse(null));
        }
        return chuyenDiRepository.save(tour);
    }

    public void delete(Integer id) {
        chuyenDiRepository.deleteById(id);
    }

    public List<Calendar> getCalendar(int month, int year, String selectedDateStr) {
        return getCalendar(month, year, selectedDateStr, Collections.emptyList());
    }

    public List<Calendar> getCalendar(int month, int year, String selectedDateStr, List<NgayKhoiHanh> departureDates) {
        List<Calendar> days = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate firstOfMonth = LocalDate.of(year, month, 1);
        LocalDate start = firstOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        LocalDate selectedDate = null;
        if (selectedDateStr != null && !selectedDateStr.isEmpty()) {
            selectedDate = LocalDate.parse(selectedDateStr);
        }

        // Tạo set ngày khởi hành để tra cứu nhanh
        Map<LocalDate, NgayKhoiHanh> departureMap = new HashMap<>();
        for (NgayKhoiHanh nkh : departureDates) {
            departureMap.put(nkh.getNgay(), nkh);
        }

        for (int i = 0; i < 42; i++) {
            LocalDate current = start.plusDays(i);
            Calendar day = new Calendar();
            day.setDate(current);
            day.setCurrentMonth(current.getMonthValue() == month);
            day.setPast(current.isBefore(today));

            // Kiểm tra ngày khởi hành
            NgayKhoiHanh nkh = departureMap.get(current);
            if (nkh != null) {
                day.setHasDeparture(true);
                day.setNgayKhoiHanhId(nkh.getId());
                if (nkh.getGiaVeDi() != null) {
                    day.setFlightPrice(nkh.getGiaVeDi());
                }
            }

            // Kiểm tra ngày được chọn
            if (selectedDate != null && current.equals(selectedDate)) {
                day.setSelected(true);
            } else {
                day.setSelected(false);
            }

            days.add(day);
        }
        return days;
    }

    public Page<ChuyenDi> getActiveTours(int page, int perPage) {
        Pageable pageable = PageRequest.of(page, perPage);
        return chuyenDiRepository.findByNgayKetThucAfter(LocalDate.now(), pageable);
    }

    public Page<ChuyenDi> getCompleteTours(int page, int perPage) {
        Pageable pageable = PageRequest.of(page, perPage);
        return chuyenDiRepository.findByNgayKetThucBefore(LocalDate.now(), pageable);
    }

    public Page<ChuyenDi> filterAndSort(String thanhPho, String quocGia, String diemDen, String khoangGia,
            String ngayDi, String sort, int page, int size) {
        LocalDate date = (ngayDi == null || ngayDi.isBlank()) ? null : LocalDate.parse(ngayDi);
        BigDecimal minGia = null;
        BigDecimal maxGia = null;
        if (khoangGia != null) {
            BigDecimal five = BigDecimal.valueOf(5_000_000);
            BigDecimal ten = BigDecimal.valueOf(10_000_000);
            switch (khoangGia) {
                case "DUOI5" -> {
                    minGia = BigDecimal.ZERO;
                    maxGia = five;
                }
                case "5_10" -> {
                    minGia = five;
                    maxGia = ten;
                }
                case "TREN10" -> {
                    minGia = ten;
                    maxGia = BigDecimal.valueOf(Long.MAX_VALUE);
                }
            }
        }
        Sort sortOption = Sort.unsorted();
        if ("priceAsc".equals(sort)) {
            sortOption = Sort.by("gia").ascending();
        } else if ("priceDesc".equals(sort)) {
            sortOption = Sort.by("gia").descending();
        }
        Pageable pageable = PageRequest.of(page, size, sortOption);
        return chuyenDiRepository.filterTour(emptyToNull(thanhPho), emptyToNull(quocGia), emptyToNull(diemDen), date,
                minGia, maxGia, pageable);
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}