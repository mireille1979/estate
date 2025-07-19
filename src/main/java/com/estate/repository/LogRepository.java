package com.estate.repository;

import com.estate.domain.entity.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface LogRepository extends JpaRepository<Log, String>, JpaSpecificationExecutor<Log> {
    Page<Log> findAllByOrderByCreationDateDesc(Pageable pageable);

    @Transactional
    @Modifying(clearAutomatically = true)
    void deleteAllByCreationDateBefore(LocalDateTime creationDate);
}
