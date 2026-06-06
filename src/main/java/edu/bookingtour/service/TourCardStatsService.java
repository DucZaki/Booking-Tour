package edu.bookingtour.service;

import edu.bookingtour.dto.TourCardStatsView;
import edu.bookingtour.repo.DanhGiaRepository;
import edu.bookingtour.repo.DatChoRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TourCardStatsService {

    private final DanhGiaRepository danhGiaRepository;
    private final DatChoRepository datChoRepository;

    public TourCardStatsService(DanhGiaRepository danhGiaRepository, DatChoRepository datChoRepository) {
        this.danhGiaRepository = danhGiaRepository;
        this.datChoRepository = datChoRepository;
    }

    public Map<Integer, TourCardStatsView> forTours(List<Integer> tourIds) {
        Map<Integer, TourCardStatsView> map = new HashMap<>();
        if (tourIds == null) {
            return map;
        }
        for (Integer id : tourIds) {
            if (id == null) {
                continue;
            }
            double avg = danhGiaRepository.avgRatingByTourId(id);
            long ratings = danhGiaRepository.countByIdChuyenDi_Id(id);
            long bookings = datChoRepository.countByIdChuyenDi_IdAndTrangThaiIn(
                    id, List.of("PAID", "PENDING"));
            map.put(id, new TourCardStatsView(avg, ratings, bookings));
        }
        return map;
    }
}
