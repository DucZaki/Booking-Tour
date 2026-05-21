package edu.bookingtour.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "ma_giam_gia")
public class MaGiamGia {
    public static final String LOAI_PHAN_TRAM = "PERCENT";
    public static final String LOAI_SO_TIEN = "FIXED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma", length = 50, unique = true)
    private String ma;

    @Column(name = "mo_ta")
    private String moTa;

    /** PERCENT hoặc FIXED */
    @Column(name = "loai_giam", length = 20)
    private String loaiGiam = LOAI_PHAN_TRAM;

    /** % (0-100) hoặc số tiền VND tùy loaiGiam */
    @Column(name = "gia_tri_giam", precision = 14, scale = 0)
    private BigDecimal giaTriGiam;

    /** Giữ tương thích dữ liệu cũ */
    @Column(name = "phan_tram_giam")
    private Integer phanTramGiam;

    /** Giá mỗi khách (tour + vé) tối thiểu để áp mã */
    @Column(name = "gia_toi_thieu", precision = 14, scale = 0)
    private BigDecimal giaToiThieu;

    @Column(name = "ap_dung_tat_ca")
    private Boolean apDungTatCa = true;

    @Column(name = "ngay_bat_dau")
    private LocalDate ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private LocalDate ngayKetThuc;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ma_giam_gia_tour", joinColumns = @JoinColumn(name = "ma_giam_gia_id"))
    @Column(name = "chuyen_di_id")
    private Set<Integer> tourIds = new HashSet<>();

    public BigDecimal resolvedGiaTri() {
        if (giaTriGiam != null) {
            return giaTriGiam;
        }
        if (phanTramGiam != null) {
            return BigDecimal.valueOf(phanTramGiam);
        }
        return BigDecimal.ZERO;
    }

    public String resolvedLoai() {
        return loaiGiam != null && !loaiGiam.isBlank() ? loaiGiam : LOAI_PHAN_TRAM;
    }
}
