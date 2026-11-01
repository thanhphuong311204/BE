package com.datn.fashion.repository;
import com.datn.fashion.entity.FaceShapeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import java.util.List;
public interface FaceShapeHistoryRepository extends JpaRepository<FaceShapeHistory,Long> {
 List<FaceShapeHistory> findByUserIdAndIdLessThanOrderByIdDesc(Long userId,Long beforeId,Pageable pageable);
}
