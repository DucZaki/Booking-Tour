package edu.bookingtour.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        if (ngayKhoiHanh != null && ngayKetThuc != null) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(ngayKhoiHanh, ngayKetThuc) + 1;
            long nights = days - 1;
            if (nights <= 0) {
                return days + " Ngày";
            }
            return days + " Ngày " + nights + " Đêm";
        }
        return "3 Ngày 2 Đêm"; // Mặc định nếu không có ngày cụ thể
    }
}
