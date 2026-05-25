package edu.bookingtour.util;

import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.DiemDon;

import java.util.Map;
import java.util.Optional;

/** Map tên điểm đón / điểm đến sang mã sân bay IATA cho Amadeus. */
public final class AirportUtil {

    private static final Map<String, String> IATA = Map.ofEntries(
            Map.entry("ha noi", "HAN"),
            Map.entry("ho chi minh", "SGN"),
            Map.entry("tp ho chi minh", "SGN"),
            Map.entry("tp. ho chi minh", "SGN"),
            Map.entry("sai gon", "SGN"),
            Map.entry("da nang", "DAD"),
            Map.entry("phu quoc", "PQC"),
            Map.entry("nha trang", "CXR"),
            Map.entry("can tho", "VCA"),
            Map.entry("hue", "HUI"),
            Map.entry("hai phong", "HPH"),
            Map.entry("vung tau", "VCS"));

    private AirportUtil() {
    }

    public static Optional<String> iataFromCityName(String cityName) {
        if (cityName == null || cityName.isBlank()) {
            return Optional.empty();
        }
        String key = CityCoordinates.normalize(cityName);
        String direct = IATA.get(key);
        if (direct != null) {
            return Optional.of(direct);
        }
        for (Map.Entry<String, String> e : IATA.entrySet()) {
            if (key.contains(e.getKey()) || e.getKey().contains(key)) {
                return Optional.of(e.getValue());
            }
        }
        return Optional.empty();
    }

    public static String iataFromDiemDon(DiemDon diemDon) {
        if (diemDon == null || diemDon.getTen() == null) {
            return "HAN";
        }
        return iataFromCityName(diemDon.getTen()).orElse("HAN");
    }

    public static String iataFromDestination(ChuyenDi tour) {
        if (tour == null || tour.getIdDiemDen() == null) {
            return "SGN";
        }
        String city = tour.getIdDiemDen().getThanhPho();
        if (city == null || city.isBlank()) {
            city = tour.getIdDiemDen().getQuocGia();
        }
        return iataFromCityName(city).orElse("SGN");
    }
}
