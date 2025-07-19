package com.estate.repository;

import com.estate.domain.entity.Setting;
import com.estate.domain.enumaration.SettingCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SettingRepository extends JpaRepository<Setting, String> {
    boolean existsByCode(SettingCode code);
    List<Setting> findAllByOrderById();
    Optional<Setting> findByCode(SettingCode code);
}
