package com.estate.domain.service.impl;

import com.estate.domain.entity.Housing;
import com.estate.domain.entity.Log;
import com.estate.domain.entity.Notification;
import com.estate.domain.entity.Student;
import com.estate.domain.enumaration.Level;
import com.estate.domain.form.HousingForm;
import com.estate.domain.form.HousingSearch;
import com.estate.domain.service.face.HousingService;
import com.estate.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HousingServiceImpl implements HousingService {
    private final HousingRepository housingRepository;
    private final StandingRepository standingRepository;
    private final StudentRepository studentRepository;
    private final PaymentRepository paymentRepository;
    private final LeaseRepository leaseRepository;
    private final LogRepository logRepository;

    @Override
    public List<Housing> findAll() {
        return housingRepository.findAllByOrderByNameAsc();
    }

    @Override
    public List<Housing> findAllByAvailableAndActiveTrue(boolean available) {
        return housingRepository.findAllByAvailableAndActiveTrueOrderByNameAsc(available);
    }

    @Override
    public List<Housing> findAllByStandingIdAndActiveTrue(String standingId) {
        return housingRepository.findAllByStandingIdAndActiveTrueOrderByNameAsc(standingId);
    }

    @Override
    public List<Housing> findAll(HousingSearch form) {
        return housingRepository.findAll(form.toSpecification(), Sort.by(Sort.Direction.ASC, "name"));
    }

    @Override
    public Optional<Housing> findById(String id) {
        return housingRepository.findById(id);
    }

    @Override
    public long count() {
        return housingRepository.count();
    }

    @Override
    public Notification save(HousingForm form) {
        boolean creation = StringUtils.isBlank(form.getId());
        Notification notification = Notification.info();
        Housing housing = creation ? new Housing() : housingRepository.findById(form.getId()).orElse(null);
        if(housing == null) return Notification.error("Logement introuvable");
        housing.setName(form.getName());
        housing.setStanding(standingRepository.getReferenceById(form.getStandingId()));
        housing.setCategory(form.getCategory());
        housing.setAvailable(form.getAvailable());
        if(housing.isAvailable() && housing.getResident() != null) return Notification.warn("Ce logement est occupé par <b>" + housing.getResident().getUser().getName() + "</b>");
        try {
            housingRepository.saveAndFlush(housing);
            notification.setMessage("Un logement a été " + (creation ? "ajouté." : "modifié."));
            log.info(notification.getMessage());
            logRepository.save(Log.info(notification.getMessage()));
        } catch (Throwable e){
            String message = ExceptionUtils.getRootCauseMessage(e);
            notification.setType(Level.ERROR);
            if(StringUtils.containsIgnoreCase(message, "UK_NAME")){
                notification.setMessage("Le logement <b>" + housing.getName() + "</b> existe déjà.");
            } else {
                notification.setMessage("Erreur lors de la " + (creation ? "création" : "modification") + " du logement.");
            }
            log.error(notification.getMessage(), e);
            logRepository.save(Log.error(notification.getMessage(), ExceptionUtils.getStackTrace(e)));
        }

        return notification;
    }

    @Override
    public Notification deleteById(String id, boolean force, HttpServletRequest request) {
        Notification notification;
        Housing housing = housingRepository.findById(id).orElse(null);
        if(housing == null) return Notification.error("Logement introuvable");
        try {
            if(force){
                leaseRepository.setHousingToNullByHousingId(id);
                paymentRepository.setDesiderataToNullByHousingId(id);
            }
            housingRepository.deleteById(id);
            notification = Notification.info("Le logement <b>" + housing.getName() + "</b> a été supprimé");
            logRepository.save(Log.info(notification.getMessage()));
        }catch (Throwable e){
            notification = Notification.error("Erreur lors de la suppression du logement <b>" + housing.getName() + "</b>.");
            logRepository.save(Log.error(notification.getMessage(), ExceptionUtils.getStackTrace(e)));
            if(!force){
                String actions = "";
                if(housing.isActive()) actions = "<a class='lazy-link' href='" + request.getContextPath() + "/housing/toggle/" + id + "'><b>Désactiver</b></a> ou ";
                actions += "<a class='lazy-link text-danger' href='" + request.getRequestURI() + "?id=" + id + "&force=true" + "'><b>Forcer la suppression</b></a>.";
                notification = Notification.warn("Ce logement est utilisé dans certains enregistrements. " + actions);
            }
        }
        return notification;
    }

    @Override
    public Notification toggleById(String id) {
        Notification notification = new Notification();
        Housing housing = housingRepository.findById(id).orElse(null);
        if(housing == null) return Notification.error("Logement introuvable");
        try {
            housing.setActive(!housing.isActive());
            if(!housing.isActive() && housing.getResident() != null) return Notification.warn("Ce logement est encore occupé");
            housingRepository.save(housing);
            notification.setMessage("Le logement <b>" + housing.getName() + "</b> a été " + (housing.isActive() ? "activé" : "désactivé") + " avec succès.");
            logRepository.save(Log.info(notification.getMessage()));
        } catch (Throwable e){
            notification = Notification.error("Erreur lors de la modification du logement <b>" + housing.getName() + "</b>.");
            logRepository.save(Log.error(notification.getMessage(), ExceptionUtils.getStackTrace(e)));
        }
        return notification;
    }

    @Override
    public Notification liberate(String id, HttpServletRequest request) {
        Notification notification = new Notification();
        Housing housing = housingRepository.findById(id).orElse(null);
        if(housing == null) return Notification.error("Logement introuvable");
        try {
            Student student = housing.getResident();
            if(student != null) {
                student.setHousing(null);
                if(student.getCurrentLease() != null) {
                    String name = student.getUser().getName();
                    LocalDate expiration = student.getCurrentLease().getRealEndDate();
                    if(LocalDate.now().isBefore(student.getCurrentLease().getRealEndDate()) && student.getCurrentLease().isActive()){
                        String actions = "<a class='lazy-link text-danger' href='" + request.getContextPath() + "/lease/disable/" + student.getCurrentLease().getId() + "'><b>Résilier</b></a> ce contrat avant de continuer.";
                        return Notification.warn("Ce logement est encore occupé par <b>" + name + "</b> dont le contrat de bail expire le <b>" + (DateTimeFormatter.ofPattern("dd/MM/yyyy").format(expiration)) + "</b>. " + actions);
                    }
                    student.setCurrentLease(null);
                }
                studentRepository.save(student);
            }
            housing.setResident(null);
            housing.setAvailable(true);
            housingRepository.save(housing);
            notification.setMessage("Le logement <b>" + housing.getName() + "</b> a été libéré avec succès.");
            logRepository.save(Log.info(notification.getMessage()));
        } catch (Throwable e){
            notification = Notification.error("Erreur lors de la libération du logement <b>" + housing.getName() + "</b>.");
            logRepository.save(Log.error(notification.getMessage(), ExceptionUtils.getStackTrace(e)));
        }
        return notification;
    }
}
