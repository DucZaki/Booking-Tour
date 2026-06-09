package edu.bookingtour.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
@Entity
@Table(name = "chuyen_di")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ChuyenDi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "tieu_de")
    private String tieuDe;

    @Column(name = "mo_ta")
    private String moTa;

    /** Loại hình tour: GIA_DINH, TREKKING, NGHI_DUONG, GHEP_DOAN. */
    @Column(name = "loai_hinh", length = 20)
    private String loaiHinh;

    @Column(name = "gia", precision = 10, scale = 2)
    private BigDecimal gia;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "ngay_khoi_hanh")
    private LocalDate ngayKhoiHanh;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "ngay_ket_thuc")
    private LocalDate ngayKetThuc;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_diem_den")
    private DiemDen idDiemDen;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_phuong_tien")
    private PhuongTien idPhuongTien;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_noi_luu_tru")
    private NoiLuuTru idNoiLuuTru;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_diem_don")
    private DiemDon idDiemDon;

    // Allowed departure points for this tour (admin-configurable)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "chuyen_di_diem_don", joinColumns = @JoinColumn(name = "chuyen_di_id"), inverseJoinColumns = @JoinColumn(name = "diem_don_id"))
    private Set<DiemDon> diemDons = new HashSet<>();

    @ColumnDefault("0")
    @Column(name = "noi_bat")
    private Boolean noiBat;

    @Column(name = "hinh_anh")
    private String hinhAnh;

    @Column(name = "highlight", columnDefinition = "TEXT")
    private String highlight;

    @ColumnDefault("50")
    @Column(name = "suc_chua_mac_dinh", nullable = false)
    private Integer sucChuaMacDinh = 50;

    @OneToMany(mappedBy = "chuyenDi", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NgayKhoiHanh> ngayKhoiHanhs;

    @OneToMany(mappedBy = "idChuyenDi", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DanhGia> danhGias;

    @OneToMany(mappedBy = "idChuyenDi", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DatCho> datChos;

    @OneToMany(mappedBy = "idChuyenDi", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<YeuThich> yeuThichs;

    public double getAverageRating() {
        if (danhGias == null || danhGias.isEmpty()) {
            return 0.0;
        }
        double sum = 0;
        for (DanhGia dg : danhGias) {
            sum += dg.getDiem();
        }
        return Math.round((sum / danhGias.size()) * 10.0) / 10.0;
    }

    public int getRatingCount() {
        return danhGias == null ? 0 : danhGias.size();
    }

    public int getBookingCount() {
        if (datChos == null) {
            return 0;
        }
        int count = 0;
        for (DatCho dc : datChos) {
            if (dc.getSoLuong() != null) {
                count += dc.getSoLuong();
            }
        }
        return count;
    }

    public String getDuration() {
        String fromTitle = parseDurationFromTitle(tieuDe);
        if (fromTitle != null) {
            return fromTitle;
        }
        if (ngayKhoiHanhs != null) {
            for (NgayKhoiHanh nkh : ngayKhoiHanhs) {
                if (nkh.getNgay() != null && nkh.getNgayVe() != null) {
                    return formatDuration(
                            ChronoUnit.DAYS.between(nkh.getNgay(), nkh.getNgayVe()) + 1);
                }
            }
        }
        return "Liên hệ";
    }

    private static String parseDurationFromTitle(String title) {
        if (title == null || title.isBlank()) {
            return null;
        }
        Matcher shortForm = Pattern.compile("(\\d+)\\s*[Nn]\\s*(\\d+)\\s*[ĐDđ]").matcher(title);
        if (shortForm.find()) {
            return formatDuration(Long.parseLong(shortForm.group(1)), Long.parseLong(shortForm.group(2)));
        }
        Matcher longForm = Pattern.compile("(\\d+)\\s*[Nn]gày(?:\\s*(\\d+)\\s*[ĐD]êm)?", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
                .matcher(title);
        if (longForm.find()) {
            long days = Long.parseLong(longForm.group(1));
            long nights = longForm.group(2) != null ? Long.parseLong(longForm.group(2)) : days - 1;
            return formatDuration(days, nights);
        }
        return null;
    }

    private static String formatDuration(long days) {
        long nights = days - 1;
        return formatDuration(days, nights);
    }

    private static String formatDuration(long days, long nights) {
        if (nights <= 0) {
            return days + " Ngày";
        }
        return days + " Ngày " + nights + " Đêm";
    }
}
