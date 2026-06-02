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

    @Autowired
    private NgayKhoiHanhDiemDonService ngayKhoiHanhDiemDonService;

    @Autowired
    private TourCapacityService tourCapacityService;

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

        // Kiểm tra xem ngày khởi hành đã tồn tại chưa
        Optional<NgayKhoiHanh> existing = ngayKhoiHanhRepository.findByChuyenDiIdAndNgay(chuyenDiId, ngayDi);
        if (existing.isPresent()) {
            return existing.get(); // Trả về bản ghi cũ nếu đã tồn tại để tránh lỗi duplicate
        }


        NgayKhoiHanh nkh = new NgayKhoiHanh();
        nkh.setChuyenDi(chuyenDi);
        nkh.setNgay(ngayDi);
        nkh.setThang(ngayDi.getMonthValue());
        nkh.setNam(ngayDi.getYear());
        nkh.setNgayVe(ngayVe);
        tourCapacityService.applyDefaultCapacity(nkh, chuyenDi);

        // Kiểm tra phương tiện
        boolean isBus = chuyenDi.getIdPhuongTien() != null
                && "Bus".equalsIgnoreCase(chuyenDi.getIdPhuongTien().getLoai());

        if (isBus) {
            applyDefaultTransportInfo(nkh, chuyenDi);
        } else {
            String from = edu.bookingtour.util.AirportUtil.iataFromDiemDon(chuyenDi.getIdDiemDon());
            String to = edu.bookingtour.util.AirportUtil.iataFromDestination(chuyenDi);
            fetchFlightInfo(nkh, from, to, ngayDi, true);
            if (ngayVe != null) {
                fetchFlightInfo(nkh, to, from, ngayVe, false);
            } else {
                nkh.setGiaVeVe(0.0);
                nkh.setMaChuyenBayVe("N/A");
                nkh.setGioBayVe("N/A");
                nkh.setGioDenVe("N/A");
            }
        }

        nkh = ngayKhoiHanhRepository.save(nkh);
        ngayKhoiHanhDiemDonService.syncForNgayKhoiHanh(nkh, true);
        return nkh;
    }

    /**
     * Thêm ngày KH với giá/giờ mặc định — dùng bootstrap hàng loạt, không gọi Amadeus.
     */
    @Transactional
    public NgayKhoiHanh addDepartureDateWithDefaults(Integer chuyenDiId, LocalDate ngayDi, LocalDate ngayVe) {
        ChuyenDi chuyenDi = chuyenDiRepository.findById(chuyenDiId)
                .orElseThrow(() -> new RuntimeException("Tour không tồn tại"));

        Optional<NgayKhoiHanh> existing = ngayKhoiHanhRepository.findByChuyenDiIdAndNgay(chuyenDiId, ngayDi);
        if (existing.isPresent()) {
            return existing.get();
        }

        NgayKhoiHanh nkh = new NgayKhoiHanh();
        nkh.setChuyenDi(chuyenDi);
        nkh.setNgay(ngayDi);
        nkh.setThang(ngayDi.getMonthValue());
        nkh.setNam(ngayDi.getYear());
        nkh.setNgayVe(ngayVe);
        tourCapacityService.applyDefaultCapacity(nkh, chuyenDi);
        applyDefaultTransportInfo(nkh, chuyenDi);
        nkh = ngayKhoiHanhRepository.save(nkh);
        ngayKhoiHanhDiemDonService.syncForNgayKhoiHanh(nkh, false);
        return nkh;
    }

    @Transactional
    public void updateCapacity(Integer id, Integer sucChua) {
        tourCapacityService.updateDepartureCapacity(id, sucChua);
    }

    private void applyDefaultTransportInfo(NgayKhoiHanh nkh, ChuyenDi chuyenDi) {
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
            nkh.setGiaVeDi(1_454_000.0);
            nkh.setGiaVeVe(1_454_000.0);
            nkh.setMaChuyenBayDi("VJ197");
            nkh.setGioBayDi("05:30");
            nkh.setGioDenDi("07:40");
            nkh.setMaChuyenBayVe("VJ194");
            nkh.setGioBayVe("17:10");
            nkh.setGioDenVe("05:00");
        }
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
                    System.out.println("Fetched Departure Flight: " + flightNumber + " Price: " + price);
                    nkh.setGiaVeDi(price);
                    nkh.setMaChuyenBayDi(flightNumber);
                    nkh.setGioBayDi(gioBay);
                    nkh.setGioDenDi(gioDen);
                } else {
                    System.out.println("Fetched Return Flight: " + flightNumber + " Price: " + price);
                    nkh.setGiaVeVe(price);
                    nkh.setMaChuyenBayVe(flightNumber);
                    nkh.setGioBayVe(gioBay);
                    nkh.setGioDenVe(gioDen);
                }
            } else {
                System.out.println("No flight data found for " + from + " to " + to + " on " + date);
            }
        } catch (Exception e) {
            System.err.println("Lỗi fetch flight info (" + from + "-" + to + "): " + e.getMessage());
            e.printStackTrace();
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

        // Kiểm tra xem ngày mới có bị trùng với ngày khác của cùng 1 tour không
        Optional<NgayKhoiHanh> existing = ngayKhoiHanhRepository.findByChuyenDiIdAndNgay(chuyenDi.getId(), ngayDi);
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new RuntimeException("Ngày khởi hành " + ngayDi + " đã tồn tại cho tour này");
        }

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
            String from = edu.bookingtour.util.AirportUtil.iataFromDiemDon(chuyenDi.getIdDiemDon());
            String to = edu.bookingtour.util.AirportUtil.iataFromDestination(chuyenDi);
            fetchFlightInfo(nkh, from, to, ngayDi, true);
            if (ngayVe != null) {
                fetchFlightInfo(nkh, to, from, ngayVe, false);
            } else {
                nkh.setGiaVeVe(0.0);
                nkh.setMaChuyenBayVe("N/A");
                nkh.setGioBayVe("N/A");
                nkh.setGioDenVe("N/A");
            }
        }

        nkh = ngayKhoiHanhRepository.save(nkh);
        ngayKhoiHanhDiemDonService.syncForNgayKhoiHanh(nkh, true);
        return nkh;
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
