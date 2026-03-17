package edu.bookingtour.controller.user;

import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.LichTrinh;
import edu.bookingtour.service.LichTrinhService;
import edu.bookingtour.service.TourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
        response.put("diemDon", (tour.getIdDiemDon() != null) ? tour.getIdDiemDon().getTen() : "N/A");
        response.put("notes", tour.getHighlight());
        response.put("itinerary", itinerary);

        return ResponseEntity.ok(response);
    }
}
