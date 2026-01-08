package edu.bookingtour.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "chuyen_di")
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

    @Column(name = "ngay_khoi_hanh")
    private Instant ngayKhoiHanh;

    @Column(name = "ngay_ket_thuc")
    private Instant ngayKetThuc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_diem_den")
    private DiemDen idDiemDen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_phuong_tien")
    private PhuongTien idPhuongTien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_noi_luu_tru")
    private NoiLuuTru idNoiLuuTru;

    @ColumnDefault("0")
    @Column(name = "noi_bat")
    private Boolean noiBat;

    @Column(name = "hinh_anh")
    private String hinhAnh;
}