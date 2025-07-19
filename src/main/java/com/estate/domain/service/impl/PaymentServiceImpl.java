package com.estate.domain.service.impl;

import com.estate.domain.entity.Notification;
import com.estate.domain.entity.*;
import com.estate.domain.enumaration.Level;
import com.estate.domain.enumaration.Profil;
import com.estate.domain.enumaration.Status;
import com.estate.domain.form.PaymentForm;
import com.estate.domain.form.PaymentReject;
import com.estate.domain.form.PaymentSearch;
import com.estate.domain.helper.EmailHelper;
import com.estate.domain.service.face.PaymentService;
import com.estate.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final StandingRepository standingRepository;
    private final StudentRepository studentRepository;
    private final PaymentRepository paymentRepository;
    private final HousingRepository housingRepository;
    private final UserRepository userRepository;
    private final LogRepository logRepository;
    private final LeaseRepository leaseRepository;
    private final EmailHelper emailHelper;

    @Override
    public Page<Payment> findAll(int page){
        return paymentRepository.findAllByOrderByCreationDateDesc(PageRequest.of(page  - 1, 500));
    }

    @Override
    public Page<Payment> findAll(PaymentSearch form) {
        return paymentRepository.findAll(form.toSpecification(), PageRequest.of(form.getPage()  - 1, 500, Sort.by(Sort.Direction.DESC, "creationDate")));
    }

    @Override
    public Page<Payment> findAllByUserId(String userId, int page) {
        return paymentRepository.findAllByStudentUserIdOrderByCreationDateDesc(userId, PageRequest.of(page  - 1, 500));
    }

    @Override
    public Optional<Payment> findById(String id) {
        return paymentRepository.findById(id);
    }

    @Override
    public long countByStatus(Status status) {
        return paymentRepository.countAllByStatus(status);
    }

    @Override
    public Notification save(PaymentForm form, HttpServletRequest request) {
        boolean creation = StringUtils.isBlank(form.getId());
        Notification notification = Notification.info();
        Payment payment = creation ? new Payment() : paymentRepository.findById(form.getId()).orElse(null);
        if(payment == null) return Notification.error("Paiement introuvable");
        Student student = studentRepository.findById(form.getStudentId()).orElse(null);
        if(student == null) return Notification.error("Étudiant introuvable");
        payment.setStudent(student);
        if(student.getCurrentLease() != null && student.getCurrentLease().getNextLease() != null) return Notification.warn("Cet étudiant possède déjà un contrat de bail en attente d'activation");
        Standing standing = standingRepository.findById(form.getStandingId()).orElse(null);
        if(standing == null) return Notification.error("Standing introuvable");
        payment.setStanding(standing);
        Housing desiderata = housingRepository.findById(form.getDesiderataId()).orElse(null);
        if(desiderata == null) return Notification.error("Logement introuvable");
        payment.setDesiderata(desiderata);
        payment.setMonths(form.getMonths());
        payment.setRent(standing.getRent());
        if(student.getHousing() == null){
            payment.setCaution(standing.getCaution());
            payment.setRepair(standing.getRepair());
        }
        payment.setMode(form.getMode());
        if(form.getProofFile() != null && !form.getProofFile().isEmpty()){
            File root = new File("documents");
            if (!root.exists() && !root.mkdirs()) return Notification.error("Impossible de créer le dossier de sauvegarde des documents.");
            File proof;
            if(StringUtils.isNotBlank(payment.getProof())){
                proof = new File(payment.getProof());
                try {
                    if(proof.exists()) FileUtils.deleteQuietly(proof);
                }catch (Exception ignored) {}
            }
            try {
                String extension = FilenameUtils.getExtension(form.getProofFile().getOriginalFilename());
                proof = new File(root.getAbsolutePath() + File.separator + "payment-proof-" + System.currentTimeMillis() + "." + extension);
                form.getProofFile().transferTo(proof);
                payment.setProof(root.getName() + File.separator + proof.getName());
            } catch (IOException e) {
                log.error("unable to write payment proof file", e);
                return Notification.error("Impossible d'enregistrer la preuve de paiement.");
            }
        }
        try {
            payment = paymentRepository.saveAndFlush(payment);
            String action = "<a class='lazy-link' href='" + request.getContextPath() + "/payment/submit/" + payment.getId() + "'><b>Soumettre</b></a> ";
            notification.setMessage("Un paiement a été " + (creation ? "ajouté" : "modifié"));
            log.info(notification.getMessage());
            logRepository.save(Log.info(notification.getMessage()));
            notification.setMessage(notification.getMessage() + " Vérifiez que les informations renseignées sont correctes puis " + action + " pour validation.");
        } catch (Throwable e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            notification.setType(Level.ERROR);
            notification.setMessage("Erreur lors de la " + (creation ? "création" : "modification") + " du paiement.");
            log.error(notification.getMessage(), e);
            logRepository.save(Log.error(notification.getMessage(), ExceptionUtils.getStackTrace(e)));
        }
        return notification;
    }

    @Override
    public List<Payment> findAllByStatusAndYear(Status status, Integer year) {
        LocalDateTime startDate = LocalDate.of(year, Month.JANUARY, 1).atStartOfDay();
        LocalDateTime endDate = LocalDate.of(year, Month.DECEMBER, 31).atTime(LocalTime.MAX);
        return paymentRepository.findAllByStatusAndCreationDateBetweenOrderByCreationDateDesc(status, startDate, endDate);
    }

    @Override
    public Notification validate(String id, HttpSession session) {
        Notification notification = Notification.info();
        try {
            Payment payment = paymentRepository.findById(id).orElse(null);
            if(payment == null) return Notification.error("Paiement introuvable");
            if(!Status.SUBMITTED.equals(payment.getStatus())) return Notification.warn("Le paiement ne peut être validé dans son statut actuel");
            payment.setStatus(Status.CONFIRMED);
            User validator = (User) session.getAttribute("user");
            payment.setValidator(validator);
            if(validator == null || !validator.getModes().contains(payment.getMode())) return Notification.error("Vous n'êtes pas chargé de la vérification des paiements par <b>" + payment.getMode().name() + "</b>.");
            Student student = payment.getStudent();
            Housing housing = payment.getDesiderata();
            Lease lease = new Lease();
            lease.setPayment(payment);
            if(housing == null){
                notification = Notification.error("Logement introuvable");
                throw new IllegalAccessException(notification.getMessage());
            }
            if(student.getCurrentLease() == null){
                if(!housing.isActive()) return Notification.error("Le logement <b>" + housing.getName() + "</b> sollicité est désactivé");
                if(!housing.isAvailable()){
                    return Notification.warn("Le logement <b>" + housing.getName() + "</b> est occupé par <b>" + housing.getResident().getUser().getName() + "</b>.");
                } else {
                    lease.setHousing(housing);
                    lease.setStartDate(LocalDate.now());
                    lease.setEndDate(lease.getStartDate().plusMonths(payment.getMonths()));
                    lease = leaseRepository.save(lease);
                    student.setHousing(housing);
                    student.setCurrentLease(lease);
                    housing.setResident(student);
                    housing.setAvailable(false);
                    notification = Notification.info("Le paiement a été confirmé. Le contrat de bail a été enregistré avec succès.");
                    notifyForPayment(payment.getStudent().getUser().getEmail(), payment,"validate.ftl",true,"PAIEMENT CONFIRMÉ - NOUVEAU CONTRAT DE BAIL", Collections.singletonList("documents/policy.pdf"));
                }
                studentRepository.save(student);
                housingRepository.save(housing);
            } else {
                Lease currentLease = student.getCurrentLease();
                LocalDate today = LocalDate.now();
                boolean currentLeaseExpired = false;
                if(today.isBefore(currentLease.getEndDate())){
                    lease.setStartDate(currentLease.getEndDate().plusDays(1));
                } else {
                    lease.setStartDate(LocalDate.now());
                    currentLeaseExpired = true;
                }
                lease.setEndDate(lease.getStartDate().plusMonths(payment.getMonths()));
                lease.setHousing(currentLease.getHousing());
                lease = leaseRepository.save(lease);
                if(currentLeaseExpired){
                    student.setCurrentLease(lease);
                    studentRepository.save(student);
                }
                currentLease.setNextLease(lease);
                leaseRepository.save(currentLease);
                notification = Notification.info("Le paiement a été confirmé. Le contrat de bail a été renouvelé avec succès.");
                notifyForPayment(payment.getStudent().getUser().getEmail(), payment,"validate.ftl",true,"PAIEMENT CONFIRMÉ - RENOUVELLEMENT DE BAIL", Collections.emptyList());
            }
            paymentRepository.save(payment);
        } catch (Exception e){
            log.error("Error on payment validation", e);
        }
        return notification;
    }

    @Override
    public Notification submit(String id) {
        Notification notification = Notification.info("Le paiement a été soumis pour validation");
        Payment payment = paymentRepository.findById(id).orElse(null);
        if(payment == null) return Notification.error("Paiement introuvable");
        if(Status.INITIATED.equals(payment.getStatus())){
            payment.setStatus(Status.SUBMITTED);
            paymentRepository.save(payment);
            String receiver = userRepository.findByProfil(Profil.STAFF).stream().map(User::getEmail).collect(Collectors.joining(","));
            notifyForPayment(receiver, payment,"payment.ftl",false,"NOTIFICATION DE PAIEMENT", Collections.singletonList(payment.getProof()));
        } else {
            notification = Notification.warn("Le paiement ne peut être soumis dans son statut actuel");
        }
        return notification;
    }

    @Override
    public Notification cancel(PaymentReject form, HttpSession session) {
        Notification notification = Notification.info();
        Payment payment = paymentRepository.findById(form.getId()).orElse(null);
        if(payment == null) return Notification.error("Paiement introuvable");
        payment.setStatus(Status.CANCELLED);
        payment.setComment(StringUtils.trim(form.getComment()));
        User validator = (User) session.getAttribute("user");
        payment.setValidator(validator);
        if(validator == null || !validator.getModes().contains(payment.getMode())) return Notification.error("Vous n'êtes pas chargé de la vérification des paiements par <b>" + payment.getMode().name() + "</b>.");
        paymentRepository.save(payment);
        notifyForPayment(payment.getStudent().getUser().getEmail(), payment,"cancel.ftl",true,"PAIEMENT REJETÉ", Collections.emptyList());
        return notification;
    }

    @Override
    public Notification deleteById(String id, HttpServletRequest request) {
        Notification notification;
        Payment payment = paymentRepository.findById(id).orElse(null);
        if(payment == null) return Notification.error("Paiement introuvable");
        if(Status.SUBMITTED.equals(payment.getStatus())) return Notification.warn("Le paiement a déjà été soumis pour vérification.");
        if(Status.CONFIRMED.equals(payment.getStatus())) return Notification.warn("Le paiement a déjà été validé.");
        try {
            paymentRepository.deleteById(id);
            if(StringUtils.isNotBlank(payment.getProof())){
                File proofFile = new File(payment.getProof());
                try {
                    if(proofFile.exists()) FileUtils.deleteQuietly(proofFile);
                } catch (Exception ignored) {}
            }
            notification = Notification.info("Le paiement a été supprimé");
            logRepository.save(Log.info(notification.getMessage()));
        }catch (Throwable e){
            notification = Notification.error("Erreur lors de la suppression du paiement.");
            logRepository.save(Log.error(notification.getMessage(), ExceptionUtils.getStackTrace(e)));
        }
        return notification;
    }

    public void notifyForPayment(String to, Payment payment, String template, boolean copy, String subject, List<String> attachments) {
        HashMap<String, Object> context = new HashMap<>();
        context.put("name", payment.getStudent().getUser().getName());
        context.put("amount", payment.getAmount());
        context.put("rent", payment.getRent());
        context.put("repair", payment.getRepair());
        context.put("month", payment.getMonths());
        context.put("caution", payment.getCaution());
        context.put("mode", payment.getMode().getName());
        context.put("motif", payment.getComment());
        String cc = "";
        if(copy){
            cc = payment.getStudent().getFirstParentEmail() + ";" + payment.getStudent().getSecondParentEmail();
        }
        emailHelper.sendMail(to, cc, subject, template, Locale.FRENCH, context, attachments);
    }
}
