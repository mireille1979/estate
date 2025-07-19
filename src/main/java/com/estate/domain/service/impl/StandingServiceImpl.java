package com.estate.domain.service.impl;

import com.estate.domain.entity.Notification;
import com.estate.domain.entity.Log;
import com.estate.domain.entity.Standing;
import com.estate.domain.enumaration.Level;
import com.estate.domain.form.StandingForm;
import com.estate.domain.service.face.StandingService;
import com.estate.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StandingServiceImpl implements StandingService {
    private final StandingRepository standingRepository;
    private final LeaseRepository leaseRepository;
    private final PaymentRepository paymentRepository;
    private final HousingRepository housingRepository;
    private final LogRepository logRepository;

    @Override
    public List<Standing> findAll() {
        return standingRepository.findAllByOrderByNameAsc();
    }

    @Override
    public List<Standing> findAllByActiveTrue() {
        return standingRepository.findAllByActiveTrueOrderByNameAsc();
    }

    @Override
    public List<Standing> findAllByActiveTrueOrderByRentAsc() {
        return standingRepository.findAllByActiveTrueOrderByRentAsc();
    }

    @Override
    public Optional<Standing> findById(String id) {
        return standingRepository.findById(id);
    }

    @Override
    public Notification deleteById(String id, boolean force, HttpServletRequest request){
        Notification notification;
        Standing standing = standingRepository.findById(id).orElse(null);
        if(standing == null) return Notification.error("Standing introuvable");
        try {
            if(force){
                leaseRepository.deleteAllByPaymentStandingId(id);
                paymentRepository.deleteAllByStandingId(id);
                housingRepository.deleteAllByStandingId(id);
            }
            standingRepository.deleteById(id);
            if(StringUtils.isNotBlank(standing.getPicture())){
                File picture = new File(standing.getPicture());
                try {
                    if(picture.exists()) FileUtils.deleteQuietly(picture);
                } catch (Exception ignored) {}
            }
            notification = Notification.info("Le <b>" + standing.getName() + "</b> standing a été supprimé");
            logRepository.save(Log.info(notification.getMessage()));
        }catch (Throwable e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            notification = Notification.error("Erreur lors de la suppression du <b>" + standing.getName() + "</b> standing.");
            logRepository.save(Log.error(notification.getMessage(), ExceptionUtils.getStackTrace(e)));
            if(!force){
                String actions = "";
                if(standing.isActive()) actions = "<a class='lazy-link' href='" + request.getContextPath() + "/standing/toggle/" + id + "'><b>Désactiver</b></a> ou ";
                actions += "<a class='lazy-link text-danger' href='" + request.getRequestURI() + "?id=" + id + "&force=true" + "'><b>Forcer la suppression</b></a> (cette action supprimera tout logement, paiement ou contrat de bail associé).";
                notification = Notification.warn("Ce standing est utilisé dans certains enregistrements. " + actions);
            }
        }
        return notification;
    }

    @Override
    public Notification save(StandingForm form) {
        boolean creation = StringUtils.isBlank(form.getId());
        Notification notification = Notification.info();
        Standing standing = creation ? new Standing() : standingRepository.findById(form.getId()).orElse(null);
        if(standing == null) return Notification.error("Standing introuvable");
        standing.setName(form.getName());
        standing.setDescription(form.getDescription());
        standing.setRent(form.getRent());
        standing.setCaution(form.getCaution());
        standing.setRepair(form.getRepair());
        long date = System.currentTimeMillis();
        String extension;
        File root = new File("documents");
        if (!root.exists() && !root.mkdirs()) return Notification.error("Impossible de créer le dossier de sauvegarde des documents.");
        if(form.getPicture() != null && !form.getPicture().isEmpty()){
            File picture;
            if(StringUtils.isNotBlank(standing.getPicture())){
                picture = new File(standing.getPicture());
                try {
                    if(picture.exists()) FileUtils.deleteQuietly(picture);
                } catch (Exception ignored) {}
            }
            try {
                extension = FilenameUtils.getExtension(form.getPicture().getOriginalFilename());
                picture = new File(root.getAbsolutePath() + File.separator + "standing-" + date + "." + extension);
                form.getPicture().transferTo(picture);
                standing.setPicture(root.getName() + File.separator + picture.getName());
            } catch (IOException e) {
                log.error("unable to write standing picture file", e);
                return Notification.error("Impossible d'enregistrer une image du standing.");
            }
        }
        try {
            standingRepository.saveAndFlush(standing);
            notification.setMessage("Un standing a été " + (creation ? "ajouté." : "modifié."));
            log.info(notification.getMessage());
            logRepository.save(Log.info(notification.getMessage()));
        } catch (Throwable e){
            String message = ExceptionUtils.getRootCauseMessage(e);
            notification.setType(Level.ERROR);
            if(StringUtils.containsIgnoreCase(message, "UK_NAME")){
                notification.setMessage("Le <b>" + standing.getName() + "</b> standing existe déjà.");
            } else {
                notification.setMessage("Erreur lors de la " + (creation ? "création" : "modification") + " du standing.");
            }
            log.error(notification.getMessage(), e);
            logRepository.save(Log.error(notification.getMessage(), ExceptionUtils.getStackTrace(e)));
        }

        return notification;
    }

    @Override
    public Notification toggleById(String id) {
        Notification notification = new Notification();
        Standing standing = standingRepository.findById(id).orElse(null);
        if(standing == null) return Notification.error("Standing introuvable");
        try {
            standing.setActive(!standing.isActive());
            standingRepository.save(standing);
            notification.setMessage("Le <b>" + standing.getName() + "</b> standing a été " + (standing.isActive() ? "activé" : "désactivé") + " avec succès.");
            logRepository.save(Log.info(notification.getMessage()));
        } catch (Throwable e){
            notification = Notification.error("Erreur lors du changement de statut du <b>" + standing.getName() + "</b> standing.");
            logRepository.save(Log.error(notification.getMessage(), ExceptionUtils.getStackTrace(e)));
        }
        return notification;
    }
}
