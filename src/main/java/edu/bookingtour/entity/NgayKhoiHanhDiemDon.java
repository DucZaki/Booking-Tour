package edu.bookingtour.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ngay_khoi_hanh_diem_don", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "ngay_khoi_hanh_id", "diem_don_id" })
})
public class NgayKhoiHanhDiemDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ngay_khoi_hanh_id", nullable = false)
    private NgayKhoiHanh ngayKhoiHanh;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "diem_don_id", nullable = false)
    private DiemDon diemDon;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "active_from")
    private LocalDateTime activeFrom;

    @Column(name = "active_to")
    private LocalDateTime activeTo;

    @Column(name = "gia_ve_di")
    private Double giaVeDi;

    @Column(name = "ma_chuyen_bay_di")
    private String maChuyenBayDi;

    @Column(name = "gio_bay_di")
    private String gioBayDi;

    @Column(name = "gio_den_di")
    private String gioDenDi;

    @Column(name = "gia_ve_ve")
    private Double giaVeVe;

    @Column(name = "ma_chuyen_bay_ve")
    private String maChuyenBayVe;

    @Column(name = "gio_bay_ve")
    private String gioBayVe;

    @Column(name = "gio_den_ve")
    private String gioDenVe;

    public double getTongGiaVe() {
        return (giaVeDi != null ? giaVeDi : 0) + (giaVeVe != null ? giaVeVe : 0);
    }

    public boolean isCurrentlyActive() {
        if (!active) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (activeFrom != null && now.isBefore(activeFrom)) {
            return false;
        }
        if (activeTo != null && now.isAfter(activeTo)) {
            return false;
        }
        return true;
    }
}
