package edu.bookingtour.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Tọa độ tham chiếu cho điểm đón — khớp {@code diem_don.ten} trong DB.
 */
public final class CityCoordinates {

    private record Coords(double lat, double lng) {
    }

    private static final Map<String, Coords> KNOWN = Map.ofEntries(
            Map.entry("ha noi", new Coords(21.0285, 105.8542)),
            Map.entry("ho chi minh", new Coords(10.8231, 106.6297)),
            Map.entry("da nang", new Coords(16.0544, 108.2022)),
            Map.entry("can tho", new Coords(10.0452, 105.7469)),
            Map.entry("hue", new Coords(16.4637, 107.5909)),
            Map.entry("nha trang", new Coords(12.2388, 109.1967)),
            Map.entry("phu quoc", new Coords(10.2899, 103.984)),
            Map.entry("hai phong", new Coords(20.8449, 106.6881)),
            Map.entry("vung tau", new Coords(10.4113, 107.1362)));

    private CityCoordinates() {
    }

    public static Optional<double[]> resolve(String cityName) {
        if (cityName == null || cityName.isBlank()) {
            return Optional.empty();
        }
        String key = normalize(cityName);
        Coords direct = KNOWN.get(key);
        if (direct != null) {
            return Optional.of(new double[] { direct.lat(), direct.lng() });
        }
        for (Map.Entry<String, Coords> e : KNOWN.entrySet()) {
            if (key.contains(e.getKey()) || e.getKey().contains(key)) {
                Coords c = e.getValue();
                return Optional.of(new double[] { c.lat(), c.lng() });
            }
        }
        return Optional.empty();
    }

    /** Suy điểm xuất phát từ tiêu đề tour khi chưa gán {@code id_diem_don}. */
    public static Optional<String> inferDepartureFromText(String text) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        String n = normalize(text);
        for (String key : KNOWN.keySet()) {
            if (n.contains(key)) {
                return Optional.of(displayName(key));
            }
        }
        return Optional.empty();
    }

    private static String displayName(String key) {
        return switch (key) {
            case "ha noi" -> "Hà Nội";
            case "ho chi minh" -> "Hồ Chí Minh";
            case "da nang" -> "Đà Nẵng";
            case "can tho" -> "Cần Thơ";
            case "hue" -> "Huế";
            case "nha trang" -> "Nha Trang";
            case "phu quoc" -> "Phú Quốc";
            case "hai phong" -> "Hải Phòng";
            case "vung tau" -> "Vũng Tàu";
            default -> key;
        };
    }

    public static String normalize(String input) {
        String n = Normalizer.normalize(input.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
        return n.replace('đ', 'd').replace('Đ', 'd');
    }
}
