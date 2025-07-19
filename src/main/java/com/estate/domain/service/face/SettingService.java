package com.estate.domain.service.face;

import com.estate.domain.entity.Notification;
import com.estate.domain.entity.Setting;
import com.estate.domain.enumaration.SettingCode;
import com.estate.domain.form.SettingForm;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface SettingService {
    List<Setting> findAll();

    Optional<Setting> findById(String id);

    Optional<Setting> findByCode(SettingCode code);

    Notification update(SettingForm form);

    Notification savePolicy(MultipartFile file);
}
