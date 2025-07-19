package com.estate.repository;

import com.estate.domain.entity.Housing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface HousingRepository extends JpaRepository<Housing, String>, JpaSpecificationExecutor<Housing> {
    List<Housing> findAllByOrderByNameAsc();
    List<Housing> findAllByAvailableAndActiveTrueOrderByNameAsc(boolean available);
    List<Housing> findAllByStandingIdAndActiveTrueOrderByNameAsc(String standingId);

    @Transactional
    @Modifying(clearAutomatically = true)
    void deleteAllByStandingId(String standingId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Housing h SET h.resident = null, h.available = true WHERE h.resident.id = ?1")
    void setResidentToNullByStudentId(String studentId);

    long countAllByAvailableTrueAndActiveTrue();
}
