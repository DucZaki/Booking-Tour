package edu.bookingtour.util;

import edu.bookingtour.entity.DiemDen;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class NavDestinationUtil {

    private static final List<String> DOMESTIC_MARKERS = List.of(
            "viet nam", "vietnam", "trong nuoc", "mien bac", "mien trung", "mien nam"
    );

    private NavDestinationUtil() {
    }

    public static boolean isDomestic(DiemDen d) {
        if (d == null) {
            return true;
        }
        String quocGia = normalize(d.getQuocGia());
        String chauLuc = normalize(d.getChauLuc());
        String thanhPho = normalize(d.getThanhPho());

        for (String marker : DOMESTIC_MARKERS) {
            if (quocGia.contains(marker) || chauLuc.contains(marker) || thanhPho.contains(marker)) {
                return true;
            }
        }
        return false;
    }

    public static String destinationLabel(DiemDen d) {
        if (d == null) {
            return "";
        }
        return d.getThanhPho() != null && !d.getThanhPho().isBlank() ? d.getThanhPho() : d.getQuocGia();
    }

    public static String destinationThumb(DiemDen d) {
        if (d != null && d.getHinhAnh() != null && !d.getHinhAnh().isBlank()) {
            return d.getHinhAnh().startsWith("/") ? d.getHinhAnh() : "/" + d.getHinhAnh();
        }
        String label = normalize(destinationLabel(d));
        if (label.contains("sapa")) {
            return "/anh/diemden/sapa.jpg";
        }
        if (label.contains("ha long") || label.contains("halong")) {
            return "/anh/diemden/halong.jpg";
        }
        if (label.contains("da nang") || label.contains("danang")) {
            return "/anh/diemden/danang.jpg";
        }
        if (label.contains("hue")) {
            return "/anh/diemden/hue.jpg";
        }
        if (label.contains("phu quoc")) {
            return "/anh/diemden/phuquoc.jpg";
        }
        if (label.contains("tokyo")) {
            return "/anh/diemden/tokyo.jpg";
        }
        if (label.contains("seoul")) {
            return "/anh/diemden/seoul.jpg";
        }
        if (label.contains("kyoto")) {
            return "/anh/diemden/kyoto.jpg";
        }
        if (label.contains("osaka")) {
            return "/anh/diemden/osaka.jpg";
        }
        if (label.contains("bac kinh") || label.contains("backinh")) {
            return "/anh/diemden/backinh.jpg";
        }
        if (label.contains("thuong hai") || label.contains("thuonghai")) {
            return "/anh/diemden/thuonghai.jpg";
        }
        if (label.contains("truong gia gioi") || label.contains("truonggiagioi")) {
            return "/anh/diemden/truonggiagioi.jpg";
        }
        return "/anh/diemden/hanoi.jpg";
    }

    public static NavDestinationSplit split(List<DiemDen> all) {
        List<DiemDen> domestic = new ArrayList<>();
        List<DiemDen> international = new ArrayList<>();
        for (DiemDen d : all) {
            if (isDomestic(d)) {
                domestic.add(d);
            } else {
                international.add(d);
            }
        }
        Comparator<DiemDen> byCity = Comparator.comparing(
                NavDestinationUtil::destinationLabel, String.CASE_INSENSITIVE_ORDER);
        domestic.sort(byCity);
        international.sort(byCity);
        return new NavDestinationSplit(domestic, international);
    }

    private static String normalize(String s) {
        if (s == null) {
            return "";
        }
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replace("đ", "d")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public record NavDestinationSplit(List<DiemDen> domestic, List<DiemDen> international) {
    }
}
