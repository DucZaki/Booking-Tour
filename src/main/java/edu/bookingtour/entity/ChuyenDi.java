package edu.bookingtour.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
}