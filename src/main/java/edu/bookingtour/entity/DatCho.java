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

    // Selected departure point for this booking (may differ per tour options)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_diem_don")
    private DiemDon idDiemDon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ngay_khoi_hanh")
    private NgayKhoiHanh idNgayKhoiHanh;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "so_nguoi_lon")
    private Integer soNguoiLon;

    @Column(name = "so_tre_em")
    private Integer soTreEm;

    @Column(name = "so_tre_nho")
    private Integer soTreNho;

    @Column(name = "so_em_be")
    private Integer soEmBe;

    @Column(name = "so_phong_don")
    private Integer soPhongDon;

    @Column(name = "phu_thu_phong_don")
    private Double phuThuPhongDon;

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

    @Column(name = "ma_check_in", length = 64, unique = true)
    private String maCheckIn;

    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt;

    @OneToMany(mappedBy = "idDatCho", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChoXacNhan> choXacNhans;
}
