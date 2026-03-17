package edu.bookingtour.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TourReviewDTO {
    private Integer id;
    private String tieuDe;
    private String hinhAnh;
    private double avgRating;
    private long totalReviews;
    private long positivePercentage;
}
