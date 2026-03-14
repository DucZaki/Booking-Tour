package edu.bookingtour.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lich_trinh")
public class LichTrinh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tour_id", nullable = false)
    private Integer tourId;

    @Column(name = "ngay_thu")
    private Integer ngayThu;

    @Column(name = "tieu_de")
    private String tieuDe;

    @Column(name = "so_bua_an")
    private String soBuaAn;

    @Column(name = "noi_dung", columnDefinition = "TEXT")
    private String noiDung;

    @Column(name = "nghi_dem")
    private String nghiDem;
}
