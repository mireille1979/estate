package com.estate.domain.service.impl;

import com.estate.domain.entity.*;
import com.estate.domain.enumaration.SettingCode;
import com.estate.domain.form.LeaseSearch;
import com.estate.domain.form.MutationForm;
import com.estate.domain.service.face.LeaseService;
import com.estate.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.security.Principal;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaseServiceImpl implements LeaseService {
    private final TemplateEngine templateEngine;
    private final HousingRepository housingRepository;
    private final StudentRepository studentRepository;
    private final SettingRepository settRepository;
    private final LeaseRepository leaseRepository;
    private final LogRepository logRepository;

    @Override
    public Page<Lease> findAll(int page) {
        return leaseRepository.findAllByOrderByCreationDateDesc(PageRequest.of(page - 1, 100));
    }

    @Override
    public Page<Lease> findAll(LeaseSearch form) {
        return leaseRepository.findAll(form.toSpecification(), PageRequest.of(form.getPage() == null ? 0 : (form.getPage() - 1), 100, Sort.by(Sort.Direction.DESC, "creationDate")));
    }

    @Override
    public Page<Lease> findAllByUserId(String userId, int page) {
        return leaseRepository.findAllByPaymentStudentUserIdOrderByCreationDateDesc(userId, PageRequest.of(page - 1, 100));
    }

    @Override
    public Optional<Lease> findById(String id) {
        return leaseRepository.findById(id);
    }

    @Override
    public ResponseEntity<?> download(String id) {
        Lease lease = findById(id).orElse(null);
        Context context = new Context();
        context.setVariable("lease", lease);
        String contractId = "..........";
        if(lease != null && lease.getStartDate() != null && lease.getHousing() != null) {
            contractId = lease.getStartDate().getYear() + "/" + lease.getHousing().getName();
        }
        context.setVariable("contractId", contractId);
        context.setVariable("landlordName", settRepository.findByCode(SettingCode.LANDLORD_NAME).map(Setting::getValue).orElse(""));
        context.setVariable("landlordCardId", settRepository.findByCode(SettingCode.LANDLORD_CARD_ID).map(Setting::getValue).orElse(""));
        context.setVariable("landlordAddress", settRepository.findByCode(SettingCode.LANDLORD_ADDRESS).map(Setting::getValue).orElse(""));
        context.setVariable("landlordPhone", settRepository.findByCode(SettingCode.LANDLORD_PHONE).map(Setting::getValue).orElse(""));
        context.setVariable("cityCategory", settRepository.findByCode(SettingCode.CITY_CATEGORY).map(Setting::getValue).orElse(""));
        String html = templateEngine.process("contract", context);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(stream);
        String fileName = "contract.pdf";
        MediaType contentType = MediaType.APPLICATION_PDF;
        HttpHeaders header = new HttpHeaders();
        header.setContentType(contentType);
        header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        return new ResponseEntity<>(new ByteArrayResource(stream.toByteArray()), header, HttpStatus.OK);
    }

    @Override
    public long count() {
        return leaseRepository.count();
    }

    @Override
    public Notification disable(String id) {
        Notification notification = new Notification();
        Lease lease = leaseRepository.findById(id).orElse(null);
        if(lease == null) return Notification.error("Contrat de bail introuvable");
        if(lease.getStartDate() == null) return Notification.warn("Ce contrat de bail n'est pas encore actif");
        try {
            lease.setActive(false);
            notification.setMessage("Le contract de bail de l'étudiant <b>" + lease.getPayment().getStudent().getUser().getName() + "</b> a été résilié avec succès.");
            leaseRepository.save(lease);
            logRepository.save(Log.info(notification.getMessage()));
        } catch (Throwable e){
            notification = Notification.error("Erreur lors de la résiliation du contrat de bail.");
            logRepository.save(Log.error(notification.getMessage(), ExceptionUtils.getStackTrace(e)));
        }
        return notification;
    }

    @Override
    public Notification mutate(MutationForm mutation, Principal principal) {
        Notification notification = Notification.info();
        Lease lease = leaseRepository.findById(mutation.getLeaseId()).orElse(null);
        if(lease == null) return Notification.warn("Contrat de bail introuvable");
        if(!lease.isMutable()) return Notification.warn("Ce contrat de bail n'est pas modifiable");
        try {
            Housing housing = housingRepository.findById(mutation.getHousingId()).orElse(null);
            if(housing == null) return Notification.error("Logement sollicité introuvable");
            boolean transfer = false;
            if(lease.getHousing() != null && !housing.getId().equals(lease.getHousing().getId())){
                if(!housing.isActive() || !housing.isAvailable()) return Notification.error("Logement sollicité n'est pas disponible");
                lease.getHousing().setResident(null);
                lease.getHousing().setAvailable(true);
                housingRepository.save(lease.getHousing());
                transfer = true;
            }
            lease.setHousing(housing);
            lease.setStartDate(mutation.getStartDate());
            lease.setEndDate(lease.getStartDate().plusMonths(lease.getPayment().getMonths()));
            Student student = lease.getPayment().getStudent();
            housing.setResident(student);
            housing.setAvailable(false);
            housingRepository.save(housing);
            student.setHousing(housing);
            studentRepository.save(student);
            if(transfer) {
                notification.setMessage("L'étudiant <b>" + student.getUser().getName() + "</b> a été transféré dans le logement <b>" + housing.getName() + "</b> avec succès.");
            } else {
                notification.setMessage("Le contrat de bail de l'étudiant <b>" + student.getUser().getName() + "</b> a été modifié avec succès.");
            }
            leaseRepository.save(lease);
            logRepository.save(Log.info(notification.getMessage()));
        } catch (Throwable e){
            notification = Notification.error("Erreur lors de la mutation du contrat de bail.");
            logRepository.save(Log.error(notification.getMessage(), ExceptionUtils.getStackTrace(e)));
        }
        return notification;
    }
}
