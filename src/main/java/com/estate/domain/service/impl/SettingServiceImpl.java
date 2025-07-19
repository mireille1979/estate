package com.estate.domain.service.impl;

import com.estate.domain.entity.Notification;
import com.estate.domain.entity.Log;
import com.estate.domain.entity.Setting;
import com.estate.domain.enumaration.SettingCode;
import com.estate.domain.form.SettingForm;
import com.estate.domain.service.face.SettingService;
import com.estate.repository.LogRepository;
import com.estate.repository.SettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettingServiceImpl implements SettingService {
    private final SettingRepository settingRepository;
    private final LogRepository logRepository;

    @Override
    public Optional<Setting> findById(String id) {
        return settingRepository.findById(id);
    }

    @Override
    public Optional<Setting> findByCode(SettingCode code) {
        return settingRepository.findByCode(code);
    }

    @Override
    public List<Setting> findAll() {
        return settingRepository.findAllByOrderById();
    }

    @Override
    public Notification update(SettingForm form) {
        Notification notification;
        Setting setting = settingRepository.findById(form.getId()).orElse(null);
        if(setting == null) return Notification.error("Paramètre introuvable");
        setting.setValue(form.getValue());
        try {
            settingRepository.save(setting);
            notification = Notification.info("Le paramètre <b>« " + setting.getCode().getName() + " »</b> a été modifié.");
            logRepository.save(Log.info(notification.getMessage()));
        } catch (Exception e){
            notification = Notification.error("Erreur lors de la modification du paramètre <b>« " + setting.getCode().getName() + " »</b> ");
            logRepository.save(Log.error(notification.getMessage(), ExceptionUtils.getStackTrace(e)));
        }
        return notification;
    }

    @Override
    public Notification savePolicy(MultipartFile file) {
        Notification notification = Notification.info();
        File root = new File("documents");
        if (!root.exists() && !root.mkdirs()) return Notification.error("Impossible de créer le dossier de sauvegarde des documents.");
        File policy;
        try {
            policy = new File(root.getAbsolutePath() + File.separator + "policy.pdf");
            file.transferTo(policy);
        } catch (IOException e) {
            log.error("unable to write policy file", e);
            return Notification.error("Impossible d'enregistrer le règlement intérieur de la cité.");
        }
        return notification;
    }
}
