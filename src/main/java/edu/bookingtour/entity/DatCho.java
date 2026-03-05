package edu.bookingtour.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "trang_thai", length = 50)
    private String trangThai;

    @Column(name = "ho_ten", length = 255)
    private String hoTen;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "so_dien_thoai", length = 20)
    private String soDienThoai;

    @Column(name = "dia_chi", length = 500)
    private String diaChi;

    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String ghiChu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ma_giam_gia")
    private MaGiamGia idMaGiamGia;

    @Column(name = "tong_gia")
    private Double tongGia;

    @OneToMany(mappedBy = "idDatCho", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChoXacNhan> choXacNhans;
}