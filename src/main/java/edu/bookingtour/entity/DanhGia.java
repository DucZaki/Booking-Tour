package edu.bookingtour.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "danh_gia")
public class DanhGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_chuyen_di", nullable = false)
    private ChuyenDi idChuyenDi;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_nguoi_dung", nullable = false)
    private NguoiDung idNguoiDung;

    @Column(name = "diem")
    private Integer diem;

    @Column(name = "binh_luan")
    private String binhLuan;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "ngay_danh_gia")
    private Instant ngayDanhGia;

}