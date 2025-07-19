package com.estate.controller;

import com.estate.domain.entity.*;
import com.estate.domain.enumaration.Profil;
import com.estate.domain.enumaration.SettingCode;
import com.estate.domain.enumaration.Status;
import com.estate.domain.form.HousingSearch;
import com.estate.domain.form.PaymentForm;
import com.estate.domain.form.PaymentReject;
import com.estate.domain.form.PaymentSearch;
import com.estate.domain.service.face.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {
    private final StudentService studentService;
    private final StandingService standingService;
    private final SettingService settingService;
    private final HousingService housingService;
    private final PaymentService paymentService;

    @GetMapping(value="list")
    public String findAll(@RequestParam(required = false, defaultValue = "1") int page, Model model, HttpSession session, HttpServletRequest request){
        PaymentSearch form = new PaymentSearch();
        Page<Payment> payments;
        Map<String, ?> attributes = RequestContextUtils.getInputFlashMap(request);
        boolean search = attributes != null && attributes.containsKey("searchForm");
        if(search){
            form = (PaymentSearch) attributes.get("searchForm");
            payments = paymentService.findAll(form);
            if(payments.isEmpty()) model.addAttribute("notification", Notification.info("Aucun résultat"));
        } else {
            User user = (User) session.getAttribute("user");
            if(user != null && Profil.STUDENT.equals(user.getProfil())){
                payments = paymentService.findAllByUserId(user.getId(), page);
                model.addAttribute("orangeMerchantCode", settingService.findByCode(SettingCode.ORANGE_MONEY_MERCHANT_CODE).map(Setting::getValue).orElse(""));
                model.addAttribute("orangeMerchantName", settingService.findByCode(SettingCode.ORANGE_MONEY_MERCHANT_NAME).map(Setting::getValue).orElse(""));
                model.addAttribute("mtnMerchantCode", settingService.findByCode(SettingCode.MTN_MOBILE_MONEY_MERCHANT_CODE).map(Setting::getValue).orElse(""));
                model.addAttribute("mtnMerchantName", settingService.findByCode(SettingCode.MTN_MOBILE_MONEY_MERCHANT_NAME).map(Setting::getValue).orElse(""));
                model.addAttribute("bankName", settingService.findByCode(SettingCode.BANK_NAME).map(Setting::getValue).orElse(""));
                model.addAttribute("bankAccountName", settingService.findByCode(SettingCode.BANK_ACCOUNT_NAME).map(Setting::getValue).orElse(""));
                model.addAttribute("bankAccountNumber", settingService.findByCode(SettingCode.BANK_ACCOUNT_NUMBER).map(Setting::getValue).orElse(""));
                model.addAttribute("payPal", settingService.findByCode(SettingCode.PAYPAL_LINK).map(Setting::getValue).orElse(""));
            } else {
                payments = paymentService.findAll(page);
            }

        }
        model.addAttribute("payments", payments.toList());
        model.addAttribute("totalPages", payments.getTotalPages());
        model.addAttribute("currentPage", payments.getNumber() + 1);
        model.addAttribute("startIndex", payments.getPageable().getOffset());
        model.addAttribute("searchForm", form);
        model.addAttribute("search", search);
        return "admin/payment/list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value="accounting")
    public String accounting(@RequestParam(required = false) Integer year, Model model){
        int currentYear = LocalDate.now().getYear();
        List<Integer> years = IntStream.range(currentYear - 4, currentYear + 1).boxed().collect(Collectors.toList());
        if(year == null || !years.contains(year)) year = currentYear;
        List<Payment> payments = paymentService.findAllByStatusAndYear(Status.CONFIRMED, year);
        model.addAttribute("payments", payments);
        model.addAttribute("years", years);
        model.addAttribute("exercise", year);
        model.addAttribute("total", payments.stream().mapToLong(Payment::getAmount).reduce(Long::sum).orElse(0));
        return "admin/payment/accounting";
    }

    @GetMapping("save")
    public String findById(@RequestParam(required = false) String id, @RequestParam(required = false) String standingId, @RequestParam(required = false) String studentId, Model model, RedirectAttributes attributes){
        Payment payment = null;
        Standing standing = null;
        Student student = null;
        List<Housing> housings = new ArrayList<>();
        List<Standing> standings = standingService.findAll();
        if(StringUtils.isNotBlank(id)){
            payment = paymentService.findById(id).orElse(null);
        } else if(StringUtils.isNotBlank(studentId)){
            student = studentService.findById(studentId).orElse(null);
            if(student == null){
                attributes.addFlashAttribute("notification", Notification.error("Étudiant introuvable"));
                return "redirect:/student/list";
            }
            payment = new Payment();
            payment.setStudent(student);
        }
        if(payment == null){
            attributes.addFlashAttribute("notification", Notification.error("Paiement introuvable"));
            return "redirect:/payment/list";
        } else {
            if(student == null) student = payment.getStudent();
            if(student.getHousing() != null){
                payment.setDesiderata(student.getHousing());
                standing = student.getHousing().getStanding();
                if(standing != null){
                    payment.setStanding(standing);
                    payment.setRent(standing.getRent());
                }
            } else {
                if(standingId != null){
                    standing = standingService.findById(standingId).orElse(null);
                } else if(!standings.isEmpty()) {
                    standing = standings.get(0);
                }

                if(standing != null){
                    payment.setStanding(standing);
                    payment.setRent(standing.getRent());
                    if(student.getHousing() == null){
                        payment.setCaution(standing.getCaution());
                        payment.setRepair(standing.getRepair());
                    }
                }
            }
        }

        if(payment.getStanding() != null){
            housings = housingService.findAllByStandingIdAndActiveTrue(payment.getStanding().getId());
            if(payment.getDesiderata() == null && !housings.isEmpty()){
                payment.setDesiderata(housings.get(0));
            }
        }

        model.addAttribute("payment", payment.toForm());
        model.addAttribute("student", payment.getStudent());
        model.addAttribute("standings", standings);
        model.addAttribute("housings", housings);
        return "admin/payment/save";
    }

    @PostMapping("save")
    public String save(@Valid @ModelAttribute("payment") PaymentForm payment, BindingResult result, Model model, RedirectAttributes attributes, HttpServletRequest request){
        if(StringUtils.isBlank(payment.getId())) {
            String notNullMessage = "javax.validation.constraints.NotNull.message";
            String defaultMessage = "ne doit pas être nul";
            if(payment.getProofFile() == null || payment.getProofFile().isEmpty()) result.rejectValue("proofFile", notNullMessage, defaultMessage);
        }
        Notification notification = null;
        if(!result.hasErrors()) notification =  paymentService.save(payment, request);
        if(result.hasErrors() || (notification != null && notification.hasError())){
            List<Standing> standings = standingService.findAll();
            model.addAttribute("notification", notification);
            model.addAttribute("payment", payment);
            model.addAttribute("student", studentService.findById(payment.getStudentId()).orElse(null));
            model.addAttribute("standings", standings);
            model.addAttribute("housings", housingService.findAllByStandingIdAndActiveTrue(payment.getStandingId()));
            return "admin/payment/save";
        }
        attributes.addFlashAttribute("notification", notification);
        return "redirect:/payment/list";
    }

    @GetMapping(value="view/{id}")
    public String findById(@PathVariable String id, Model model, RedirectAttributes attributes){
        Payment payment = paymentService.findById(id).orElse(null);
        if(payment == null){
            attributes.addFlashAttribute("notification", Notification.error("Paiement introuvable"));
            return "redirect:/payment/list";
        }
        PaymentReject reject = new PaymentReject();
        reject.setId(payment.getId());
        reject.setComment(payment.getComment());
        model.addAttribute("payment", payment);
        model.addAttribute("reject", reject);
        return "admin/payment/view";
    }

    @GetMapping(value="submit/{id}")
    public String submitById(@PathVariable String id, RedirectAttributes attributes){
        attributes.addFlashAttribute("notification", paymentService.submit(id));
        return "redirect:/payment/list";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'JANITOR')")
    @GetMapping(value="validate/{id}")
    public String validateById(@PathVariable String id, RedirectAttributes attributes, HttpSession session){
        attributes.addFlashAttribute("notification", paymentService.validate(id, session));
        return "redirect:/payment/list";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'JANITOR')")
    @PostMapping(value="cancel")
    public String cancelById(@Valid @ModelAttribute("reject") PaymentReject reject, BindingResult result, Model model, RedirectAttributes attributes, HttpSession session){
        if(result.hasErrors()){
            Payment payment = paymentService.findById(reject.getId()).orElse(null);
            if(payment == null){
                attributes.addFlashAttribute("notification", Notification.error("Paiement introuvable"));
                return "redirect:/payment/list";
            }
            model.addAttribute("payment", payment);
            return "admin/payment/view";
        }
        attributes.addFlashAttribute("notification", paymentService.cancel(reject, session));
        return "redirect:/payment/list";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'JANITOR')")
    @PostMapping("search")
    public String search(HousingSearch form, RedirectAttributes attributes){
        attributes.addFlashAttribute("searchForm", form);
        return "redirect:/payment/list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value="delete")
    public String deleteById(@RequestParam String id, RedirectAttributes attributes, HttpServletRequest request){
        Notification notification =  paymentService.deleteById(id, request);
        attributes.addFlashAttribute("notification", notification);
        return "redirect:/payment/list";
    }
}
