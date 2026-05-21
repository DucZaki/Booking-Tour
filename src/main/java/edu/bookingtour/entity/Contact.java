package edu.bookingtour.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "contacts")
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Vui lòng nhập họ tên")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String name;

    @NotBlank(message = "Vui lòng nhập email")
    @Email(message = "Email không hợp lệ")
    private String email;

    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    private String phone_number;

    @NotBlank(message = "Vui lòng nhập tiêu đề")
    @Size(max = 200, message = "Tiêu đề tối đa 200 ký tự")
    private String tittle;
    @NotBlank(message = "Vui lòng nhập nội dung")
    @Size(min = 10, max = 5000, message = "Nội dung từ 10 đến 5000 ký tự")
    @Column(columnDefinition = "TEXT")
    private String content;
    private LocalDateTime createdAt;
    private String status;
    private Integer guest_number;
    private String address;
    private String type;
    public Contact() {
        this.createdAt = LocalDateTime.now();
        this.status = "NEW";
    }
}
