package com.estate.domain.service.impl;

import com.estate.domain.entity.Log;
import com.estate.domain.entity.Notification;
import com.estate.domain.entity.Student;
import com.estate.domain.enumaration.Level;
import com.estate.domain.enumaration.Profil;
import com.estate.domain.enumaration.Role;
import com.estate.domain.form.StudentForm;
import com.estate.domain.form.StudentSearch;
import com.estate.domain.helper.EmailHelper;
import com.estate.domain.service.face.StudentService;
import com.estate.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import com.estate.domain.helper.TextUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {
    private final StudentRepository studentRepository;
    private final PaymentRepository paymentRepository;
    private final LogRepository logRepository;
    private final LeaseRepository leaseRepository;
    private final HousingRepository housingRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailHelper emailHelper;

    @Override
    public long count() {
        return studentRepository.count();
    }

    @Override
    public Page<Student> findAll(int page) {
        return studentRepository.findAllByOrderByCreationDateDesc(PageRequest.of(page - 1, 100));
    }

    @Override
    public Page<Student> findAll(StudentSearch form) {
        return studentRepository.findAll(form.toSpecification(), PageRequest.of(form.getPage() == null ? 0 : (form.getPage() - 1), 100));
    }

    @Override
    public Optional<Student> findById(String id) {
        return studentRepository.findById(id);
    }

    @Override
    public Optional<Student> findByUserId(String userId) {
        return studentRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public Notification save(StudentForm form) {
        boolean creation = StringUtils.isBlank(form.getId());
        Notification notification = Notification.info();
        Student student = creation ? new Student() : studentRepository.findById(form.getId()).orElse(null);
        if(student == null) return Notification.error("Étudiant introuvable");
        student.getUser().setFirstName(form.getFirstName());
        student.getUser().setLastName(form.getLastName());
        student.getUser().setProfil(Profil.STUDENT);
        student.setDateOfBirth(form.getDateOfBirth());
        student.setPlaceOfBirth(form.getPlaceOfBirth());
        student.setCniNumber(form.getCniNumber());
        student.getUser().setGender(form.getGender());

        student.setSchool(form.getSchool());
        student.setSpecialities(form.getSpecialities());
        student.setGrade(form.getGrade());
        student.setMatricule(form.getMatricule());
        student.getUser().setPhone(form.getPhone().format());
        student.getUser().setMobile(form.getMobile().format());
        student.getUser().setEmail(form.getEmail());

        student.setFirstParentRelation(form.getFirstParentRelation());
        student.setFirstParentName(form.getFirstParentName());
        student.setFirstParentAddress(form.getFirstParentAddress());
        student.setFirstParentPhone(form.getFirstParentPhone().format());
        student.setFirstParentMobile(form.getFirstParentMobile().format());
        student.setFirstParentEmail(form.getFirstParentEmail());

        student.setSecondParentRelation(form.getSecondParentRelation());
        student.setSecondParentName(form.getSecondParentName());
        student.setSecondParentAddress(form.getSecondParentAddress());
        student.setSecondParentPhone(form.getSecondParentPhone().format());
        student.setSecondParentMobile(form.getSecondParentMobile().format());
        student.setSecondParentEmail(form.getSecondParentEmail());
        long date = System.currentTimeMillis();
        String extension;
        File root = new File("documents");
        if (!root.exists() && !root.mkdirs()) return Notification.error("Impossible de créer le dossier de sauvegarde des documents.");
        if(form.getCniRectoFile() != null && !form.getCniRectoFile().isEmpty()){
            File cniRecto;
            if(StringUtils.isNotBlank(student.getCniRecto())){
                cniRecto = new File(student.getCniRecto());
                try {
                    if(cniRecto.exists()) FileUtils.deleteQuietly(cniRecto);
                }catch (Exception ignored) {}
            }
            try {
                extension = FilenameUtils.getExtension(form.getCniRectoFile().getOriginalFilename());
                cniRecto = new File(root.getAbsolutePath() + File.separator + "identification-recto-" + date + "." + extension);
                form.getCniRectoFile().transferTo(cniRecto);
                student.setCniRecto(root.getName() + File.separator + cniRecto.getName());
            } catch (IOException e) {
                log.error("unable to write identification file recto face", e);
                return Notification.error("Impossible d'enregistrer la pièce d'identification (face recto).");
            }
        }
        if(form.getCniVersoFile() != null && !form.getCniVersoFile().isEmpty()){
            File cniVerso;
            if(StringUtils.isNotBlank(student.getCniVerso())){
                cniVerso = new File(student.getCniVerso());
                try {
                    if(cniVerso.exists()) FileUtils.deleteQuietly(cniVerso);
                }catch (Exception ignored) {}
            }
            try {
                extension = FilenameUtils.getExtension(form.getCniVersoFile().getOriginalFilename());
                cniVerso = new File(root.getAbsolutePath() + File.separator + "identification-verso-" + date + "." + extension);
                form.getCniVersoFile().transferTo(cniVerso);
                student.setCniVerso(root.getName() + File.separator + cniVerso.getName());
            } catch (IOException e) {
                log.error("unable to write identification file verso face", e);
                return Notification.error("Impossible d'enregistrer la pièce d'identification (face verso).");
            }
        }
        if(form.getBirthCertificateFile() != null && !form.getBirthCertificateFile().isEmpty()){
            File birthCertificate;
            if(StringUtils.isNotBlank(student.getBirthCertificate())){
                birthCertificate = new File(student.getBirthCertificate());
                try {
                    if(birthCertificate.exists()) FileUtils.deleteQuietly(birthCertificate);
                }catch (Exception ignored) {}
            }
            try {
                extension = FilenameUtils.getExtension(form.getBirthCertificateFile().getOriginalFilename());
                birthCertificate = new File(root.getAbsolutePath() + File.separator + "birth-certificate-" + date + "." + extension);
                form.getBirthCertificateFile().transferTo(birthCertificate);
                student.setBirthCertificate(root.getName() + File.separator + birthCertificate.getName());
            } catch (IOException e) {
                log.error("unable to write birth certificate", e);
                return Notification.error("Impossible d'enregistrer l'acte de naissance.");
            }
        }
        if(form.getStudentCardFile() != null && !form.getStudentCardFile().isEmpty()){
            File studentCard;
            if(StringUtils.isNotBlank(student.getStudentCard())){
                studentCard = new File(student.getStudentCard());
                try {
                    if(studentCard.exists()) FileUtils.deleteQuietly(studentCard);
                }catch (Exception ignored) {}
            }
            try {
                extension = FilenameUtils.getExtension(form.getStudentCardFile().getOriginalFilename());
                studentCard = new File(root.getAbsolutePath() + File.separator + "student-card-" + date + "." + extension);
                form.getStudentCardFile().transferTo(studentCard);
                student.setStudentCard(root.getName() + File.separator + studentCard.getName());
            } catch (IOException e) {
                log.error("unable to write student card", e);
                return Notification.error("Impossible d'enregistrer la carte d'étudiant.");
            }
        }

        try {
            if(creation){
                String password = TextUtils.generatePassword();
                student.getUser().setStudent(student);
                student.getUser().setPassword(passwordEncoder.encode(password));
                student.getUser().setRoles(Collections.singletonList(Role.ROLE_STUDENT));
                studentRepository.saveAndFlush(student);
                String name = student.getUser().getOneName();
                HashMap<String, Object> context = new HashMap<>();
                context.put("name", name);
                context.put("password", password);
                context.put("link", ServletUriComponentsBuilder.fromCurrentContextPath().path("/237in").build());
                String cc = student.getFirstParentEmail() +";" + student.getSecondParentEmail();
                emailHelper.sendMail(student.getUser().getEmail(), cc, "BIENVENUE DANS LA MINI CITÉ CONCORDE", "welcome.ftl", Locale.FRENCH, context, Collections.emptyList());
            } else {
                studentRepository.saveAndFlush(student);
            }
            notification.setMessage("Un étudiant a été " + (creation ? "ajouté." : "modifié."));
            log.info(notification.getMessage());
        } catch (Throwable e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            String message = ExceptionUtils.getRootCauseMessage(e);
            notification.setType(Level.ERROR);
            if(StringUtils.containsIgnoreCase(message, "UK_EMAIL")){
                notification.setMessage("Adresse e-mail existante");
            } else {
                notification.setMessage("Erreur lors de la " + (creation ? "création" : "modification") + " de l'étudiant.");
            }
            log.error(notification.getMessage(), e);
        }

        return notification;
    }

    @Override
    public Notification toggleById(String id) {
        Notification notification = new Notification();
        Student student = studentRepository.findById(id).orElse(null);
        if(student == null) return Notification.error("Étudiant introuvable");
        try {
            student.getUser().setActive(!student.getUser().isActive());
            studentRepository.save(student);
            notification.setMessage("L'étudiant <b>" + student.getUser().getName() + "</b> a été " + (student.getUser().isActive() ? "activé" : "désactivé") + " avec succès.");
            logRepository.save(Log.info(notification.getMessage()));
        } catch (Throwable e){
            notification = Notification.error("Erreur lors de la modification de l'étudiant <b>" + student.getUser().getName() + "</b>.");
            logRepository.save(Log.error(notification.getMessage(), ExceptionUtils.getStackTrace(e)));
        }
        return notification;
    }

    @Override
    public Notification deleteById(String id, boolean force, HttpServletRequest request) {
        Notification notification;
        Student student = studentRepository.findById(id).orElse(null);
        if(student == null) return Notification.error("Étudiant introuvable");
        try {
            if(force){
                housingRepository.setResidentToNullByStudentId(id);
                studentRepository.setCurrentLeaseToNullByStudentId(id);
                leaseRepository.deleteAllByPaymentStudentId(id);
                paymentRepository.deleteAllByStudentId(id);
            }
            studentRepository.deleteById(id);
            if(StringUtils.isNotBlank(student.getBirthCertificate())){
                File birthCertificate = new File(student.getBirthCertificate());
                try {
                    if(birthCertificate.exists()) FileUtils.deleteQuietly(birthCertificate);
                } catch (Exception ignored) {}
            }
            if(StringUtils.isNotBlank(student.getCniRecto())){
                File cniRecto = new File(student.getCniRecto());
                try {
                    if(cniRecto.exists()) FileUtils.deleteQuietly(cniRecto);
                } catch (Exception ignored) {}
            }
            if(StringUtils.isNotBlank(student.getCniVerso())){
                File cniVerso = new File(student.getCniVerso());
                try {
                    if(cniVerso.exists()) FileUtils.deleteQuietly(cniVerso);
                } catch (Exception ignored) {}
            }
            if(StringUtils.isNotBlank(student.getStudentCard())){
                File studentCard = new File(student.getStudentCard());
                try {
                    if(studentCard.exists()) FileUtils.deleteQuietly(studentCard);
                } catch (Exception ignored) {}
            }
            notification = Notification.info("L'étudiant <b>" + student.getUser().getName() + "</b> a été supprimé");
            logRepository.save(Log.info(notification.getMessage()));
        } catch (Throwable e){
            notification = Notification.error("Erreur lors de la suppression de l'étudiant <b>" + student.getUser().getName() + "</b>.");
            logRepository.save(Log.error(notification.getMessage(), ExceptionUtils.getStackTrace(e)));
            if(!force){
                String actions = "";
                if(student.getUser().isActive()) actions = "<a class='lazy-link' href='" + request.getContextPath() + "/student/toggle/" + id + "'><b>Désactiver</b></a> ou ";
                actions += "<a class='lazy-link text-danger' href='" + request.getRequestURI() + "?id=" + id + "&force=true" + "'><b>Forcer la suppression</b></a> (cette action supprimera tout paiement et contrat de bail associé).";
                notification = Notification.warn("Cet étudiant est utilisé dans certains enregistrements. " + actions);
            }
        }
        return notification;
    }
}
