package edu.bookingtour.service;

import edu.bookingtour.client.AmadeusClient;
import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.NgayKhoiHanh;
import edu.bookingtour.repo.ChuyenDiRepository;
import edu.bookingtour.repo.NgayKhoiHanhRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class NgayKhoiHanhService {

    @Autowired
    private NgayKhoiHanhRepository ngayKhoiHanhRepository;

    @Autowired
    private ChuyenDiRepository chuyenDiRepository;

    @Autowired
    private AmadeusClient amadeusClient;

    /**
     * Lấy danh sách ngày khởi hành của tour theo tháng/năm (do admin set)
     */
    public List<NgayKhoiHanh> getDepartureDates(Integer chuyenDiId, int month, int year) {
        return ngayKhoiHanhRepository.findByChuyenDiIdAndThangAndNam(chuyenDiId, month, year);
    }

    /**
     * Admin thêm ngày khởi hành mới cho tour, tự fetch flight info từ Amadeus
     */
    @Transactional
    public NgayKhoiHanh addDepartureDate(Integer chuyenDiId, LocalDate ngayDi, LocalDate ngayVe) {
        ChuyenDi chuyenDi = chuyenDiRepository.findById(chuyenDiId)
                .orElseThrow(() -> new RuntimeException("Tour không tồn tại"));

        NgayKhoiHanh nkh = new NgayKhoiHanh();
        nkh.setChuyenDi(chuyenDi);
        nkh.setNgay(ngayDi);
        nkh.setThang(ngayDi.getMonthValue());
        nkh.setNam(ngayDi.getYear());
        nkh.setNgayVe(ngayVe);

        // Kiểm tra phương tiện
        boolean isBus = chuyenDi.getIdPhuongTien() != null
                && "Bus".equalsIgnoreCase(chuyenDi.getIdPhuongTien().getLoai());

        if (isBus) {
            nkh.setGiaVeDi(300000.0);
            nkh.setGiaVeVe(0.0);
            nkh.setMaChuyenBayDi("BUS");
            nkh.setGioBayDi("08:00");
            nkh.setGioDenDi("12:00");
            nkh.setMaChuyenBayVe("BUS");
            nkh.setGioBayVe("14:00");
            nkh.setGioDenVe("18:00");
        } else {
            String from = "HAN";
            String to = "SGN";
            // Fetch thông tin vé máy bay chiều đi từ Amadeus
            fetchFlightInfo(nkh, from, to, ngayDi, true);
            // Fetch thông tin vé máy bay chiều về từ Amadeus (chỉ khi có ngày về)
            if (ngayVe != null) {
                fetchFlightInfo(nkh, to, from, ngayVe, false);
            } else {
                nkh.setGiaVeVe(0.0);
                nkh.setMaChuyenBayVe("N/A");
                nkh.setGioBayVe("N/A");
                nkh.setGioDenVe("N/A");
            }
        }

        return ngayKhoiHanhRepository.save(nkh);
    }

    /**
     * Fetch thông tin vé máy bay từ Amadeus và gán vào NgayKhoiHanh
     */
    private void fetchFlightInfo(NgayKhoiHanh nkh, String from, String to, LocalDate date, boolean isDeparture) {
        try {
            Map<String, Object> flight = amadeusClient.getCheapestFlight(from, to, date.toString());

            if (!flight.isEmpty()) {
                double price = (double) flight.getOrDefault("price", 0.0);
                String flightNumber = (String) flight.getOrDefault("flightNumber", "N/A");
                String departureTime = (String) flight.getOrDefault("departureTime", "");
                String arrivalTime = (String) flight.getOrDefault("arrivalTime", "");

                String gioBay = formatTime(departureTime);
                String gioDen = formatTime(arrivalTime);

                if (isDeparture) {
                    nkh.setGiaVeDi(price);
                    nkh.setMaChuyenBayDi(flightNumber);
                    nkh.setGioBayDi(gioBay);
                    nkh.setGioDenDi(gioDen);
                } else {
                    nkh.setGiaVeVe(price);
                    nkh.setMaChuyenBayVe(flightNumber);
                    nkh.setGioBayVe(gioBay);
                    nkh.setGioDenVe(gioDen);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi fetch flight info: " + e.getMessage());
        }
    }

    /**
     * Format ISO datetime string sang HH:mm
     */
    private String formatTime(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.isEmpty())
            return "N/A";
        try {
            LocalDateTime dt = LocalDateTime.parse(isoDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return dt.format(DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            return isoDateTime;
        }
    }

    /**
     * Admin cập nhật ngày đi/ngày về → re-fetch flight info
     */
    @Transactional
    public NgayKhoiHanh updateDepartureDate(Integer id, LocalDate ngayDi, LocalDate ngayVe) {
        NgayKhoiHanh nkh = ngayKhoiHanhRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ngày khởi hành không tồn tại"));

        ChuyenDi chuyenDi = nkh.getChuyenDi();
        boolean isBus = chuyenDi.getIdPhuongTien() != null
                && "Bus".equalsIgnoreCase(chuyenDi.getIdPhuongTien().getLoai());

        nkh.setNgay(ngayDi);
        nkh.setThang(ngayDi.getMonthValue());
        nkh.setNam(ngayDi.getYear());
        nkh.setNgayVe(ngayVe);

        if (isBus) {
            nkh.setGiaVeDi(300000.0);
            nkh.setGiaVeVe(0.0);
        } else {
            String from = "HAN";
            String to = "SGN";
            // Re-fetch flight info chiều đi
            fetchFlightInfo(nkh, from, to, ngayDi, true);
            // Re-fetch flight info chiều về (chỉ khi có ngày về)
            if (ngayVe != null) {
                fetchFlightInfo(nkh, to, from, ngayVe, false);
            } else {
                nkh.setGiaVeVe(0.0);
                nkh.setMaChuyenBayVe("N/A");
                nkh.setGioBayVe("N/A");
                nkh.setGioDenVe("N/A");
            }
        }

        return ngayKhoiHanhRepository.save(nkh);
    }

    /**
     * Admin xoá ngày khởi hành
     */
    @Transactional
    public void deleteDepartureDate(Integer id) {
        ngayKhoiHanhRepository.deleteById(id);
    }

    /**
     * Lấy tất cả ngày khởi hành của 1 tour
     */
    public List<NgayKhoiHanh> findByChuyenDiId(Integer chuyenDiId) {
        return ngayKhoiHanhRepository.findByChuyenDiId(chuyenDiId);
    }

    /**
     * Tìm theo ID
     */
    public NgayKhoiHanh findById(Integer id) {
        return ngayKhoiHanhRepository.findById(id).orElse(null);
    }

    /**
     * Tìm ngày khởi hành theo tour + ngày cụ thể
     */
    public NgayKhoiHanh findByChuyenDiAndNgay(Integer chuyenDiId, LocalDate ngay) {
        return ngayKhoiHanhRepository.findByChuyenDiIdAndNgay(chuyenDiId, ngay).orElse(null);
    }
}
