package com.estate.repository;

import com.estate.domain.entity.Standing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StandingRepository extends JpaRepository<Standing, String> {
    List<Standing> findAllByOrderByNameAsc();
    List<Standing> findAllByActiveTrueOrderByNameAsc();
    List<Standing> findAllByActiveTrueOrderByRentAsc();

    Optional<Standing> findByName(String name);
}
