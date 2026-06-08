package edu.bookingtour.service;

import edu.bookingtour.client.AmadeusClient;
import edu.bookingtour.dto.FlightQuoteResponse;
import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.DiemDon;
import edu.bookingtour.entity.NgayKhoiHanh;
import edu.bookingtour.entity.NgayKhoiHanhDiemDon;
import edu.bookingtour.repo.DiemDonRepository;
import edu.bookingtour.repo.NgayKhoiHanhDiemDonRepository;
import edu.bookingtour.repo.NgayKhoiHanhRepository;
import edu.bookingtour.util.AirportUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class NgayKhoiHanhDiemDonService {

    private final NgayKhoiHanhDiemDonRepository repository;
    private final NgayKhoiHanhRepository ngayKhoiHanhRepository;
    private final DiemDonRepository diemDonRepository;
    private final AmadeusClient amadeusClient;
    private final TourService tourService;

    public NgayKhoiHanhDiemDonService(NgayKhoiHanhDiemDonRepository repository,
            NgayKhoiHanhRepository ngayKhoiHanhRepository,
            DiemDonRepository diemDonRepository,
            AmadeusClient amadeusClient,
            TourService tourService) {
        this.repository = repository;
        this.ngayKhoiHanhRepository = ngayKhoiHanhRepository;
        this.diemDonRepository = diemDonRepository;
        this.amadeusClient = amadeusClient;
        this.tourService = tourService;
    }

    @Transactional
    public void syncForNgayKhoiHanh(NgayKhoiHanh nkh, boolean fetchFlights) {
        ChuyenDi tour = nkh.getChuyenDi();
        tourService.normalizeTourDepartureOptions(tour);
        Set<DiemDon> diemDons = tour.getDiemDons() != null ? tour.getDiemDons() : Set.of();
        if (diemDons.isEmpty() && tour.getIdDiemDon() != null) {
            diemDons = Set.of(tour.getIdDiemDon());
        }

        Map<Integer, NgayKhoiHanhDiemDon> existing = new HashMap<>();
        for (NgayKhoiHanhDiemDon row : repository.findByNgayKhoiHanhIdOrderByDiemDonIdAsc(nkh.getId())) {
            existing.put(row.getDiemDon().getId(), row);
        }

        for (DiemDon diemDon : diemDons) {
            NgayKhoiHanhDiemDon row = existing.remove(diemDon.getId());
            if (row == null) {
                row = new NgayKhoiHanhDiemDon();
                row.setNgayKhoiHanh(nkh);
                row.setDiemDon(diemDon);
                row.setActive(true);
            }
            if (fetchFlights || row.getGiaVeDi() == null) {
                populateTransport(row, tour, nkh, fetchFlights);
            }
            repository.save(row);
        }

        existing.values().forEach(repository::delete);
        refreshNgayKhoiHanhSummary(nkh);
    }

    @Transactional
    public FlightQuoteResponse getQuote(Integer nkhId, Integer diemDonId, boolean refresh) {
        NgayKhoiHanh nkh = ngayKhoiHanhRepository.findById(nkhId).orElse(null);
        if (nkh == null) {
            return FlightQuoteResponse.unavailable("Ngày khởi hành không tồn tại");
        }
        ChuyenDi tour = nkh.getChuyenDi();
        tourService.normalizeTourDepartureOptions(tour);

        boolean allowed = tour.getDiemDons() != null && tour.getDiemDons().stream()
                .anyMatch(d -> d.getId().equals(diemDonId));
        if (!allowed) {
            return FlightQuoteResponse.unavailable("Điểm đón không hợp lệ cho tour này");
        }

        NgayKhoiHanhDiemDon row = repository.findByNgayKhoiHanhIdAndDiemDonId(nkhId, diemDonId).orElse(null);
        if (row == null) {
            DiemDon diemDon = diemDonRepository.findById(diemDonId).orElse(null);
            if (diemDon == null) {
                return FlightQuoteResponse.unavailable("Không tìm thấy điểm đón");
            }
            row = new NgayKhoiHanhDiemDon();
            row.setNgayKhoiHanh(nkh);
            row.setDiemDon(diemDon);
            row.setActive(true);
            populateTransport(row, tour, nkh, false);
            row = repository.save(row);
        } else if (refresh) {
            // User quote reads admin-managed ticket data only. Amadeus refresh is admin-only via refreshFlight().
            populateTransport(row, tour, nkh, false);
            row = repository.save(row);
        }

        if (!row.isCurrentlyActive()) {
            return FlightQuoteResponse.unavailable("Địa điểm xuất phát không hợp lệ");
        }

        double tourPrice = tour.getGia() != null ? tour.getGia().doubleValue() : 0;
        double ticketTotal = row.getTongGiaVe();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return FlightQuoteResponse.builder()
                .available(true)
                .message("OK")
                .diemDonId(diemDonId)
                .diemDonTen(row.getDiemDon().getTen())
                .diemDenTen(tour.getIdDiemDen() != null ? tour.getIdDiemDen().getThanhPho() : "—")
                .giaVeDi(row.getGiaVeDi() != null ? row.getGiaVeDi() : 0)
                .giaVeVe(row.getGiaVeVe() != null ? row.getGiaVeVe() : 0)
                .tongGiaVe(ticketTotal)
                .giaTour(tourPrice)
                .unitPrice(tourPrice + ticketTotal)
                .maChuyenBayDi(nullToNa(row.getMaChuyenBayDi()))
                .gioBayDi(nullToNa(row.getGioBayDi()))
                .gioDenDi(nullToNa(row.getGioDenDi()))
                .maChuyenBayVe(nullToNa(row.getMaChuyenBayVe()))
                .gioBayVe(nullToNa(row.getGioBayVe()))
                .gioDenVe(nullToNa(row.getGioDenVe()))
                .ngayDi(nkh.getNgay() != null ? nkh.getNgay().format(fmt) : "")
                .ngayVe(nkh.getNgayVe() != null ? nkh.getNgayVe().format(fmt) : "")
                .build();
    }

    @Transactional
    public NgayKhoiHanhDiemDon toggleActive(Integer id, boolean active) {
        NgayKhoiHanhDiemDon row = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cấu hình điểm đón"));
        row.setActive(active);
        NgayKhoiHanhDiemDon saved = repository.save(row);
        refreshNgayKhoiHanhSummary(saved.getNgayKhoiHanh());
        return saved;
    }

    @Transactional
    public NgayKhoiHanhDiemDon updateSchedule(Integer id, LocalDateTime activeFrom, LocalDateTime activeTo) {
        NgayKhoiHanhDiemDon row = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cấu hình điểm đón"));
        row.setActiveFrom(activeFrom);
        row.setActiveTo(activeTo);
        return repository.save(row);
    }

    @Transactional
    public NgayKhoiHanhDiemDon refreshFlight(Integer id) {
        NgayKhoiHanhDiemDon row = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cấu hình điểm đón"));
        NgayKhoiHanh nkh = row.getNgayKhoiHanh();
        populateTransport(row, nkh.getChuyenDi(), nkh, true);
        NgayKhoiHanhDiemDon saved = repository.save(row);
        refreshNgayKhoiHanhSummary(nkh);
        return saved;
    }

    public List<NgayKhoiHanhDiemDon> findByNgayKhoiHanhId(Integer nkhId) {
        return repository.findByNgayKhoiHanhIdOrderByDiemDonIdAsc(nkhId);
    }

    public boolean hasAnyActiveDeparturePoint(Integer nkhId) {
        if (nkhId == null) {
            return false;
        }
        return repository.findByNgayKhoiHanhIdOrderByDiemDonIdAsc(nkhId).stream()
                .anyMatch(NgayKhoiHanhDiemDon::isCurrentlyActive);
    }

    public Set<Integer> getActiveDepartureIds(Integer nkhId) {
        if (nkhId == null) {
            return Set.of();
        }
        Set<Integer> ids = new LinkedHashSet<>();
        for (NgayKhoiHanhDiemDon row : repository.findByNgayKhoiHanhIdOrderByDiemDonIdAsc(nkhId)) {
            if (row.isCurrentlyActive() && row.getDiemDon() != null && row.getDiemDon().getId() != null) {
                ids.add(row.getDiemDon().getId());
            }
        }
        return ids;
    }

    public List<NgayKhoiHanh> filterBookableDepartures(List<NgayKhoiHanh> departures) {
        if (departures == null || departures.isEmpty()) {
            return List.of();
        }
        return departures.stream()
                .filter(n -> hasAnyActiveDeparturePoint(n.getId()))
                .toList();
    }

    public Integer resolveDefaultDepartureId(List<DiemDon> options, Integer nkhId) {
        if (options == null || options.isEmpty() || nkhId == null) {
            return null;
        }
        Set<Integer> activeIds = getActiveDepartureIds(nkhId);
        return options.stream()
                .map(DiemDon::getId)
                .filter(Objects::nonNull)
                .filter(activeIds::contains)
                .findFirst()
                .orElse(null);
    }

    public double resolveTicketTotal(Integer nkhId, Integer diemDonId) {
        FlightQuoteResponse quote = getQuote(nkhId, diemDonId, false);
        if (!quote.isAvailable()) {
            throw new IllegalArgumentException(quote.getMessage());
        }
        return quote.getTongGiaVe();
    }

    private void populateTransport(NgayKhoiHanhDiemDon row, ChuyenDi tour, NgayKhoiHanh nkh, boolean callAmadeus) {
        boolean isBus = tour.getIdPhuongTien() != null
                && "Bus".equalsIgnoreCase(tour.getIdPhuongTien().getLoai());
        if (isBus) {
            applyBusDefaults(row);
            return;
        }

        String from = AirportUtil.iataFromDiemDon(row.getDiemDon());
        String to = AirportUtil.iataFromDestination(tour);
        if (callAmadeus) {
            fetchFlightLeg(row, from, to, nkh.getNgay(), true);
            if (nkh.getNgayVe() != null) {
                fetchFlightLeg(row, to, from, nkh.getNgayVe(), false);
            } else {
                clearReturnLeg(row);
            }
        } else if (row.getGiaVeDi() == null) {
            applyPlaneDefaults(row);
        }
    }

    private void fetchFlightLeg(NgayKhoiHanhDiemDon row, String from, String to, LocalDate date, boolean outbound) {
        try {
            Map<String, Object> flight = amadeusClient.getCheapestFlight(from, to, date.toString());
            if (flight.isEmpty()) {
                if (outbound && row.getGiaVeDi() == null) {
                    applyPlaneDefaults(row);
                }
                return;
            }
            double price = (double) flight.getOrDefault("price", 0.0);
            String flightNumber = (String) flight.getOrDefault("flightNumber", "N/A");
            String gioBay = formatTime((String) flight.getOrDefault("departureTime", ""));
            String gioDen = formatTime((String) flight.getOrDefault("arrivalTime", ""));
            if (outbound) {
                row.setGiaVeDi(price);
                row.setMaChuyenBayDi(flightNumber);
                row.setGioBayDi(gioBay);
                row.setGioDenDi(gioDen);
            } else {
                row.setGiaVeVe(price);
                row.setMaChuyenBayVe(flightNumber);
                row.setGioBayVe(gioBay);
                row.setGioDenVe(gioDen);
            }
        } catch (Exception e) {
            if (outbound && row.getGiaVeDi() == null) {
                applyPlaneDefaults(row);
            }
        }
    }

    private void applyBusDefaults(NgayKhoiHanhDiemDon row) {
        row.setGiaVeDi(300_000.0);
        row.setGiaVeVe(0.0);
        row.setMaChuyenBayDi("BUS");
        row.setGioBayDi("08:00");
        row.setGioDenDi("12:00");
        row.setMaChuyenBayVe("BUS");
        row.setGioBayVe("14:00");
        row.setGioDenVe("18:00");
    }

    private void applyPlaneDefaults(NgayKhoiHanhDiemDon row) {
        row.setGiaVeDi(1_454_000.0);
        row.setGiaVeVe(1_454_000.0);
        row.setMaChuyenBayDi("VJ197");
        row.setGioBayDi("05:30");
        row.setGioDenDi("07:40");
        row.setMaChuyenBayVe("VJ194");
        row.setGioBayVe("17:10");
        row.setGioDenVe("19:20");
    }

    private void clearReturnLeg(NgayKhoiHanhDiemDon row) {
        row.setGiaVeVe(0.0);
        row.setMaChuyenBayVe("N/A");
        row.setGioBayVe("N/A");
        row.setGioDenVe("N/A");
    }

    private void refreshNgayKhoiHanhSummary(NgayKhoiHanh nkh) {
        List<NgayKhoiHanhDiemDon> rows = repository.findByNgayKhoiHanhIdOrderByDiemDonIdAsc(nkh.getId());
        NgayKhoiHanhDiemDon pick = rows.stream()
                .filter(NgayKhoiHanhDiemDon::isCurrentlyActive)
                .min(Comparator.comparingDouble(NgayKhoiHanhDiemDon::getTongGiaVe))
                .orElse(rows.isEmpty() ? null : rows.get(0));
        if (pick == null) {
            return;
        }
        nkh.setGiaVeDi(pick.getGiaVeDi());
        nkh.setMaChuyenBayDi(pick.getMaChuyenBayDi());
        nkh.setGioBayDi(pick.getGioBayDi());
        nkh.setGioDenDi(pick.getGioDenDi());
        nkh.setGiaVeVe(pick.getGiaVeVe());
        nkh.setMaChuyenBayVe(pick.getMaChuyenBayVe());
        nkh.setGioBayVe(pick.getGioBayVe());
        nkh.setGioDenVe(pick.getGioDenVe());
        edu.bookingtour.util.DepartureTimeUtil.syncGatheringTime(nkh, nkh.getChuyenDi());
        ngayKhoiHanhRepository.save(nkh);
    }

    private String formatTime(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.isEmpty()) {
            return "N/A";
        }
        try {
            return LocalDateTime.parse(isoDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .format(DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            return isoDateTime;
        }
    }

    private String nullToNa(String value) {
        return value == null || value.isBlank() ? "N/A" : value;
    }
}
