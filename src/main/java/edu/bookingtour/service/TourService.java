package edu.bookingtour.service;

import edu.bookingtour.entity.Calendar;
import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.DiemDon;
import edu.bookingtour.entity.NgayKhoiHanh;
import edu.bookingtour.repo.ChuyenDiRepository;
import edu.bookingtour.util.CityCoordinates;
import edu.bookingtour.util.GeoUtils;
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
        if (chuyenDi.getIdNoiLuuTru() != null && chuyenDi.getIdNoiLuuTru().getId() != null) {
            chuyenDi.setIdNoiLuuTru(noiLuuTruRepository.findById(chuyenDi.getIdNoiLuuTru().getId()).orElse(null));
        } else {
            chuyenDi.setIdNoiLuuTru(null);
        }
        if (chuyenDi.getIdDiemDon() != null && chuyenDi.getIdDiemDon().getId() != null) {
            chuyenDi.setIdDiemDon(diemDonRepository.findById(chuyenDi.getIdDiemDon().getId()).orElse(null));
        }
        if (chuyenDi.getSucChuaMacDinh() == null || chuyenDi.getSucChuaMacDinh() < TourCapacityService.MIN_CAPACITY) {
            chuyenDi.setSucChuaMacDinh(TourCapacityService.DEFAULT_CAPACITY);
        }
        normalizeTourDepartureOptions(chuyenDi);
        return chuyenDiRepository.save(chuyenDi);
    }

    public ChuyenDi update(Integer id, ChuyenDi chuyenDi) {
        ChuyenDi tour = chuyenDiRepository.findById(id).orElseThrow(() -> new RuntimeException("ChuyenDi not found"));
        tour.setTieuDe(chuyenDi.getTieuDe());
        tour.setMoTa(chuyenDi.getMoTa());
        tour.setLoaiHinh(chuyenDi.getLoaiHinh());
        tour.setGia(chuyenDi.getGia());
        tour.setHinhAnh(chuyenDi.getHinhAnh());
        tour.setNgayKhoiHanh(chuyenDi.getNgayKhoiHanh());
        tour.setNgayKetThuc(chuyenDi.getNgayKetThuc());
        tour.setNoiBat(chuyenDi.getNoiBat());
        tour.setHighlight(chuyenDi.getHighlight());
        if (chuyenDi.getSucChuaMacDinh() != null && chuyenDi.getSucChuaMacDinh() >= TourCapacityService.MIN_CAPACITY) {
            tour.setSucChuaMacDinh(Math.min(chuyenDi.getSucChuaMacDinh(), TourCapacityService.MAX_CAPACITY));
        }

        if (chuyenDi.getIdPhuongTien() != null && chuyenDi.getIdPhuongTien().getId() != null) {
            tour.setIdPhuongTien(phuongTienRepository.findById(chuyenDi.getIdPhuongTien().getId()).orElse(null));
        }
        if (chuyenDi.getIdDiemDen() != null && chuyenDi.getIdDiemDen().getId() != null) {
            tour.setIdDiemDen(diemDenRepository.findById(chuyenDi.getIdDiemDen().getId()).orElse(null));
        }
        if (chuyenDi.getIdNoiLuuTru() != null && chuyenDi.getIdNoiLuuTru().getId() != null) {
            tour.setIdNoiLuuTru(noiLuuTruRepository.findById(chuyenDi.getIdNoiLuuTru().getId()).orElse(null));
        } else {
            tour.setIdNoiLuuTru(null);
        }
        if (chuyenDi.getIdDiemDon() != null && chuyenDi.getIdDiemDon().getId() != null) {
            tour.setIdDiemDon(diemDonRepository.findById(chuyenDi.getIdDiemDon().getId()).orElse(null));
        }
        if (chuyenDi.getDiemDons() != null) {
            tour.setDiemDons(chuyenDi.getDiemDons());
        }
        normalizeTourDepartureOptions(tour);
        return chuyenDiRepository.save(tour);
    }

    /**
     * Ensure {@code diemDons} is populated and {@code idDiemDon} stays consistent.
     * If admin didn't pick options, apply sensible defaults based on destination.
     */
    public void normalizeTourDepartureOptions(ChuyenDi tour) {
        // If user picked a single departure (legacy UI), treat it as the only allowed one.
        if ((tour.getDiemDons() == null || tour.getDiemDons().isEmpty()) && tour.getIdDiemDon() != null
                && tour.getIdDiemDon().getId() != null) {
            tour.setDiemDons(new HashSet<>(List.of(tour.getIdDiemDon())));
        }

        if (tour.getDiemDons() == null) {
            tour.setDiemDons(new HashSet<>());
        }

        if (tour.getDiemDons().isEmpty()) {
            // Default rules per requirement.
            String dest = (tour.getIdDiemDen() != null) ? tour.getIdDiemDen().getThanhPho() : null;
            String nd = (dest == null) ? "" : CityCoordinates.normalize(dest);
            List<DiemDon> all = diemDonRepository.findAll();
            Map<String, DiemDon> byKey = new HashMap<>();
            for (DiemDon d : all) {
                byKey.put(CityCoordinates.normalize(d.getTen()), d);
            }
            DiemDon hn = byKey.get("ha noi");
            DiemDon hcm = byKey.getOrDefault("ho chi minh", byKey.get("tp. ho chi minh"));
            DiemDon dn = byKey.get("da nang");

            Set<DiemDon> opts = new HashSet<>();
            if (nd.contains("ha noi")) {
                if (hcm != null) opts.add(hcm);
                if (dn != null) opts.add(dn);
            } else if (nd.contains("da nang") || nd.contains("ho chi minh") || nd.contains("tp ho chi minh")
                    || nd.contains("tp.hcm") || nd.contains("sai gon")) {
                if (hn != null) opts.add(hn);
            } else {
                if (hn != null) opts.add(hn);
                if (dn != null) opts.add(dn);
                if (hcm != null) opts.add(hcm);
            }
            tour.setDiemDons(opts);
        }

        // Keep legacy single field as a stable default.
        if (tour.getIdDiemDon() == null || tour.getIdDiemDon().getId() == null
                || !tour.getDiemDons().stream().anyMatch(d -> d.getId().equals(tour.getIdDiemDon().getId()))) {
            tour.getDiemDons().stream().findFirst().ifPresent(tour::setIdDiemDon);
        }
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
            day.setPastDay(current.isBefore(today));

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
        return filterAndSort(thanhPho, quocGia, diemDen, null, khoangGia, ngayDi, sort, page, size);
    }

    public Page<ChuyenDi> filterAndSort(String thanhPho, String quocGia, String diemDen, String loaiHinh,
            String khoangGia, String ngayDi, String sort, int page, int size) {
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
        return chuyenDiRepository.filterTour(emptyToNull(thanhPho), emptyToNull(quocGia), emptyToNull(diemDen),
                emptyToNull(loaiHinh), date, minGia, maxGia, pageable);
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    /**
     * Tour có điểm đón ({@code DiemDon}) gần vị trí user.
     * Ưu tiên lat/lng từ trình duyệt; có thể truyền {@code city} khi user từ chối GPS.
     */
    public Map<String, Object> findNearbyTours(Double lat, Double lng, String city, double radiusKm, int limit, int page) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("tours", List.of());
        result.put("inRange", false);
        result.put("page", Math.max(0, page));
        result.put("limit", Math.max(1, limit));

        if (lat == null || lng == null) {
            if (city == null || city.isBlank()) {
                result.put("message", "Thiếu vị trí");
                return result;
            }
            Optional<double[]> coords = CityCoordinates.resolve(city);
            if (coords.isEmpty()) {
                result.put("message", "Chưa hỗ trợ thành phố này");
                return result;
            }
            lat = coords.get()[0];
            lng = coords.get()[1];
        }

        List<DiemDon> diemDons = diemDonRepository.findAll();
        if (diemDons.isEmpty()) {
            result.put("message", "Chưa có điểm đón");
            return result;
        }

        final double userLat = lat;
        final double userLng = lng;

        record RankedDon(DiemDon diemDon, double distanceKm) {
        }

        List<RankedDon> ranked = diemDons.stream()
                .map(d -> CityCoordinates.resolve(d.getTen())
                        .map(c -> new RankedDon(d, GeoUtils.haversineKm(userLat, userLng, c[0], c[1])))
                        .orElse(null))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(RankedDon::distanceKm))
                .toList();

        if (ranked.isEmpty()) {
            result.put("message", "Chưa có tọa độ cho điểm đón");
            return result;
        }

        RankedDon nearest = ranked.get(0);
        double nearestDist = nearest.distanceKm();
        result.put("nearestDepartureCity", nearest.diemDon().getTen());
        result.put("nearestDistanceKm", Math.round(nearestDist * 10.0) / 10.0);
        result.put("radiusKm", radiusKm);

        if (nearestDist > radiusKm) {
            result.put("message",
                    "Không có điểm xuất phát tour trong bán kính " + (int) radiusKm + " km quanh bạn.");
            return result;
        }

        result.put("departureCity", nearest.diemDon().getTen());
        result.put("distanceKm", Math.round(nearestDist * 10.0) / 10.0);
        result.put("inRange", true);

        // Consider departure points within radius, then fetch only bookable tours for those departures.
        List<Integer> inRangeDonIds = ranked.stream()
                .filter(r -> r.distanceKm() <= radiusKm)
                .map(r -> r.diemDon().getId())
                .distinct()
                .toList();

        List<ChuyenDi> tours = inRangeDonIds.isEmpty() ? List.of()
                : chuyenDiRepository.findByDiemDonIdsAndBookable(inRangeDonIds, LocalDate.now());

        List<Map<String, Object>> allItems = tours.stream()
                .map(t -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", t.getId());
                    m.put("tieuDe", t.getTieuDe());
                    m.put("gia", t.getGia());
                    m.put("hinhAnh", t.getHinhAnh());
                    // Rank by nearest allowed departure
                    String nearestDep = null;
                    double best = Double.MAX_VALUE;
                    for (DiemDon d : (t.getDiemDons() == null ? Set.<DiemDon>of() : t.getDiemDons())) {
                        Optional<double[]> dc = CityCoordinates.resolve(d.getTen());
                        if (dc.isEmpty()) continue;
                        double dist = GeoUtils.haversineKm(userLat, userLng, dc.get()[0], dc.get()[1]);
                        if (dist < best) {
                            best = dist;
                            nearestDep = d.getTen();
                        }
                    }
                    if (nearestDep == null) {
                        nearestDep = resolveTourDepartureCity(t).orElse(null);
                        best = CityCoordinates.resolve(nearestDep)
                                .map(c -> GeoUtils.haversineKm(userLat, userLng, c[0], c[1]))
                                .orElse(nearestDist);
                    }
                    m.put("diemDon", nearestDep);
                    if (t.getIdDiemDen() != null) {
                        m.put("diemDen", t.getIdDiemDen().getThanhPho());
                    }
                    m.put("distanceKm", Math.round(best * 10.0) / 10.0);
                    return m;
                })
                .sorted(Comparator.comparingDouble(m -> ((Number) m.get("distanceKm")).doubleValue()))
                .toList();

        int safeLimit = Math.max(1, limit);
        int total = allItems.size();
        int totalPages = (int) Math.ceil(total / (double) safeLimit);
        int safePage = Math.max(0, page);
        if (totalPages > 0 && safePage > totalPages - 1) {
            safePage = totalPages - 1;
        }
        int from = safePage * safeLimit;
        int to = Math.min(from + safeLimit, total);
        List<Map<String, Object>> pageItems = (from >= 0 && from < to) ? allItems.subList(from, to) : List.of();

        result.put("page", safePage);
        result.put("limit", safeLimit);
        result.put("total", total);
        result.put("totalPages", totalPages);
        result.put("hasPrev", safePage > 0);
        result.put("hasNext", safePage + 1 < totalPages);
        result.put("tours", pageItems);
        result.put("count", pageItems.size());
        if (pageItems.isEmpty()) {
            result.put("message",
                    "Chưa có tour khả dụng xuất phát từ " + nearest.diemDon().getTen() + " trong bán kính "
                            + (int) radiusKm + " km.");
        }
        return result;
    }

    private Optional<String> resolveTourDepartureCity(ChuyenDi tour) {
        if (tour.getIdDiemDon() != null && tour.getIdDiemDon().getTen() != null
                && !tour.getIdDiemDon().getTen().isBlank()) {
            return Optional.of(tour.getIdDiemDon().getTen());
        }
        return CityCoordinates.inferDepartureFromText(tour.getTieuDe());
    }
}
