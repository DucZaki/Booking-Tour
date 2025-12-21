package edu.bookingtour.specifications;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

import edu.bookingtour.entity.ChuyenDi;
import edu.bookingtour.entity.DiemDen;
import org.springframework.data.jpa.domain.Specification;

public class ChuyenDiSpecification {

    public static Specification<ChuyenDi> diemDen(String thanhPho){
          return (root, query, criteriaBuilder) -> {
              if(thanhPho==null || thanhPho.isEmpty()){
                  return null;
              }
              Join<ChuyenDi, DiemDen> thanhphojoin = root.join("diemDen", JoinType.INNER);
              String likePattern ="%"+thanhPho.toLowerCase()+"%";
          return criteriaBuilder.like(
                  criteriaBuilder.lower(thanhphojoin.get("thanhPho")), likePattern
          );
          };
    }
}