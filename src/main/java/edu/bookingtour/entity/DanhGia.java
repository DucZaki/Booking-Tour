package edu.bookingtour.entity;
import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity

@Getter
@Setter
public class DanhGia {
    private String binh_luan;
    private int diem;
    @Id
    private int id;
    @ManyToOne
    @JoinColumn(name="id_chuyen_di")
    private ChuyenDi chuyenDi;
    @ManyToOne
    @JoinColumn(name="id_nguoi_dung")
    private User nguoiDung;
    private Date ngay_danh_gia;
}
