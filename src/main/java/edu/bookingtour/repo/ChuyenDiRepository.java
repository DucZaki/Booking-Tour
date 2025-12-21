package edu.bookingtour.repo;

import edu.bookingtour.entity.ChuyenDi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChuyenDiRepository extends JpaRepository<ChuyenDi, Integer>,
                                            JpaSpecificationExecutor<ChuyenDi> {
    List<ChuyenDi> findByNoiBat (boolean noiBat);
    List<ChuyenDi> findAll();
    long count();
}
