package edu.bookingtour.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "dat_cho")
public class DatCho {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nguoi_dung")
    private NguoiDung idNguoiDung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_chuyen_di")
    private ChuyenDi idChuyenDi;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "ngay_dat")
    private LocalDate ngayDat;

    @Column(name = "trang_thai", length = 50)
    private String trangThai;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ma_giam_gia")
    private MaGiamGia idMaGiamGia;

}