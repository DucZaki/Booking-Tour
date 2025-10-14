package edu.bookingtour.entity;
import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Getter
@Setter
public class ChuyenDi {
    @Id
    private Integer id;
    @ManyToOne
    @JoinColumn(name="id_diem_den")
    private DiemDen id_diem_den;

    private Integer id_noi_luu_tru;

    @Column(precision = 10, scale = 2)
    private BigDecimal gia;
    private Integer id_phuong_tien;
    private String mo_ta;
    private Date ngay_ket_thuc;
    private Date ngay_khoi_hanh;
    @Column(name="noi_bat")
    private boolean noiBat;
    private String hinh_anh;
    private String tieu_de;
}
