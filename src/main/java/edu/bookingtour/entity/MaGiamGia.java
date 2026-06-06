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
    /** @deprecated dùng {@link #LOAI_AMOUNT} — giữ để tương thích dữ liệu cũ */
    public static final String LOAI_SO_TIEN = "FIXED";
    public static final String LOAI_AMOUNT = "AMOUNT";

    public static final String KIEU_STANDARD = "STANDARD";
    public static final String KIEU_EARLY_BIRD = "EARLY_BIRD";
    public static final String KIEU_LAST_MINUTE = "LAST_MINUTE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma", length = 50, unique = true)
    private String ma;

    @Column(name = "mo_ta")
    private String moTa;

    /** PERCENT hoặc AMOUNT (FIXED cũ) */
    @Column(name = "loai_giam", length = 20)
    private String loaiGiam = LOAI_PHAN_TRAM;

    /** % (0-100) hoặc số tiền VND tùy loaiGiam */
    @Column(name = "gia_tri_giam", precision = 14, scale = 0)
    private BigDecimal giaTriGiam;

    /** Giữ tương thích dữ liệu cũ */
    @Column(name = "phan_tram_giam")
    private Integer phanTramGiam;

    /** Giá mỗi khách (tour + vé) tối thiểu — legacy */
    @Column(name = "gia_toi_thieu", precision = 14, scale = 0)
    private BigDecimal giaToiThieu;

    /** Trần giảm (VND) khi loại = PERCENT */
    @Column(name = "giam_toi_da", precision = 14, scale = 0)
    private BigDecimal giamToiDa;

    /** Tổng đơn tối thiểu để áp mã */
    @Column(name = "don_toi_thieu", precision = 14, scale = 0)
    private BigDecimal donToiThieu;

    @Column(name = "ap_dung_tat_ca")
    private Boolean apDungTatCa = true;

    @Column(name = "ngay_bat_dau")
    private LocalDate ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private LocalDate ngayKetThuc;

    @Column(name = "so_lan_dung_toi_da")
    private Integer soLanDungToiDa;

    @Column(name = "so_lan_da_dung")
    private Integer soLanDaDung = 0;

    @Column(name = "active")
    private Boolean active = true;

    @Column(name = "gioi_han_moi_user")
    private Integer gioiHanMoiUser;

    @Column(name = "kieu_chien_dich", length = 30)
    private String kieuChienDich = KIEU_STANDARD;

    @Column(name = "so_ngay_dat_truoc")
    private Integer soNgayDatTruoc;

    @Column(name = "so_gio_last_minute")
    private Integer soGioLastMinute;

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
        if (loaiGiam == null || loaiGiam.isBlank()) {
            return LOAI_PHAN_TRAM;
        }
        if (LOAI_SO_TIEN.equalsIgnoreCase(loaiGiam)) {
            return LOAI_AMOUNT;
        }
        return loaiGiam;
    }

    public boolean isAmountType() {
        String loai = resolvedLoai();
        return LOAI_AMOUNT.equalsIgnoreCase(loai) || LOAI_SO_TIEN.equalsIgnoreCase(loai);
    }

    public String kieuChienDichLabel() {
        if (KIEU_EARLY_BIRD.equalsIgnoreCase(kieuChienDich)) {
            return "Early Bird " + (soNgayDatTruoc != null ? soNgayDatTruoc : "") + " ngày";
        }
        if (KIEU_LAST_MINUTE.equalsIgnoreCase(kieuChienDich)) {
            return "Last-min " + (soGioLastMinute != null ? soGioLastMinute : 48) + "h";
        }
        return "Thường";
    }
}
