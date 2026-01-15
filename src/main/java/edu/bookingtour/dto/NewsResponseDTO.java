package edu.bookingtour.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class NewsResponseDTO {
    private String status;
    private int totalResults;
    private List<ArticleDTO> articles;

}