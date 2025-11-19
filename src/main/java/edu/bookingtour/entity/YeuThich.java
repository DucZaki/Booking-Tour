package edu.bookingtour.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "yeu_thich")
public class YeuThich {
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

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "ngay_them")
    private Instant ngayThem;

}