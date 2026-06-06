package edu.bookingtour.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "ngay_khoi_hanh", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "id_chuyen_di", "ngay" })
})
public class NgayKhoiHanh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_chuyen_di", nullable = false)
    private ChuyenDi chuyenDi;

    @Column(name = "ngay", nullable = false)
    private LocalDate ngay;

    @Column(name = "thang")
    private Integer thang;

    @Column(name = "nam")
    private Integer nam;

    // === Vé máy bay chiều đi ===
    @Column(name = "gia_ve_di")
    private Double giaVeDi;

    @Column(name = "ma_chuyen_bay_di")
    private String maChuyenBayDi;

    @Column(name = "gio_bay_di")
    private String gioBayDi;

    @Column(name = "gio_den_di")
    private String gioDenDi;

    // === Vé máy bay chiều về ===
    @Column(name = "ngay_ve")
    private LocalDate ngayVe;

    @Column(name = "gia_ve_ve")
    private Double giaVeVe;

    @Column(name = "ma_chuyen_bay_ve")
    private String maChuyenBayVe;

    @Column(name = "gio_bay_ve")
    private String gioBayVe;

    @Column(name = "gio_den_ve")
    private String gioDenVe;

    @Column(name = "suc_chua")
    private Integer sucChua;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guide_id")
    private NguoiDung guide;

    @Column(name = "trang_thai_doan", length = 30, nullable = false)
    private String trangThaiDoan = TrangThaiDoan.SCHEDULED.name();

    public TrangThaiDoan getTrangThaiDoanEnum() {
        return TrangThaiDoan.fromDb(trangThaiDoan);
    }

    public void setTrangThaiDoanEnum(TrangThaiDoan status) {
        this.trangThaiDoan = status != null ? status.name() : TrangThaiDoan.SCHEDULED.name();
    }

    // Tổng giá vé (đi + về)
    public double getTongGiaVe() {
        return (giaVeDi != null ? giaVeDi : 0) + (giaVeVe != null ? giaVeVe : 0);
    }
}
