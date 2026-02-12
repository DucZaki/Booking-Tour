package edu.bookingtour.entity;

import jakarta.persistence.*;
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
    private String name;
    private String email;
    private String phone_number;
    private String tittle;
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
