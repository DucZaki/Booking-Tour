package edu.bookingtour.controller.user;

import edu.bookingtour.dto.FlightQuoteResponse;
import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.LichTrinh;
import edu.bookingtour.service.LichTrinhService;
import edu.bookingtour.service.NgayKhoiHanhDiemDonService;
import edu.bookingtour.service.TourCapacityService;
import edu.bookingtour.service.TourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tour")
public class TourApiController {

    @Autowired
    private TourService tourService;

    @Autowired
    private LichTrinhService lichTrinhService;

    @Autowired
    private NgayKhoiHanhDiemDonService ngayKhoiHanhDiemDonService;

    @Autowired
    private TourCapacityService tourCapacityService;

    @GetMapping("/departure/{nkhId}/capacity")
    public ResponseEntity<Map<String, Object>> departureCapacity(@PathVariable Integer nkhId) {
        TourCapacityService.CapacitySnapshot snap = tourCapacityService.getSnapshot(nkhId);
        Map<String, Object> body = new HashMap<>();
        body.put("nkhId", nkhId);
        body.put("capacity", snap.getCapacity());
        body.put("booked", snap.getBooked());
        body.put("remaining", snap.getRemaining());
        body.put("soldOut", snap.isSoldOut());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<?> getTourDetails(@PathVariable Integer id) {
        ChuyenDi tour = tourService.findByIdd(id);
        if (tour == null) {
            return ResponseEntity.notFound().build();
        }

        List<LichTrinh> itinerary = lichTrinhService.getByTour(id);

        Map<String, Object> response = new HashMap<>();
        response.put("id", tour.getId());
        response.put("tieuDe", tour.getTieuDe());
        response.put("ngayKhoiHanh", tour.getNgayKhoiHanh());
        tourService.normalizeTourDepartureOptions(tour);
        String dep = (tour.getIdDiemDon() != null) ? tour.getIdDiemDon().getTen() : "N/A";
        response.put("diemDon", dep);
        response.put("notes", tour.getHighlight());
        response.put("itinerary", itinerary);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/flight-quote")
    public ResponseEntity<FlightQuoteResponse> flightQuote(
            @PathVariable Integer id,
            @RequestParam Integer nkhId,
            @RequestParam Integer diemDonId,
            @RequestParam(defaultValue = "false") boolean refresh) {
        ChuyenDi tour = tourService.findByIdd(id);
        if (tour == null) {
            return ResponseEntity.notFound().build();
        }
        FlightQuoteResponse quote = ngayKhoiHanhDiemDonService.getQuote(nkhId, diemDonId, refresh);
        if (!quote.isAvailable()) {
            return ResponseEntity.badRequest().body(quote);
        }
        return ResponseEntity.ok(quote);
    }

    @GetMapping("/nearby")
    public ResponseEntity<Map<String, Object>> nearbyTours(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "100") double radiusKm,
            @RequestParam(defaultValue = "6") int limit,
            @RequestParam(defaultValue = "0") int page) {
        if (radiusKm < 1) {
            radiusKm = 100;
        }
        if (limit < 1 || limit > 100) {
            limit = 6;
        }
        if (page < 0) {
            page = 0;
        }
        return ResponseEntity.ok(tourService.findNearbyTours(lat, lng, city, radiusKm, limit, page));
    }
}
