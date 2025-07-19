package com.estate.controller;

import com.estate.domain.entity.Notification;
import com.estate.domain.entity.Student;
import com.estate.domain.form.StudentForm;
import com.estate.domain.form.StudentSearch;
import com.estate.domain.helper.SmsHelper;
import com.estate.domain.service.face.HousingService;
import com.estate.domain.service.face.StudentService;
import lombok.RequiredArgsConstructor;
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
import javax.validation.Valid;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/student")
public class StudentController {
    private final HousingService housingService;
    private final StudentService studentService;

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'JANITOR')")
    @GetMapping("list")
    public String list(Model model, @RequestParam(required = false, defaultValue = "1") int page, HttpServletRequest request) {
        Page<Student> students;
        StudentSearch form = new StudentSearch();
        Map<String, ?> attributes = RequestContextUtils.getInputFlashMap(request);
        boolean search = attributes != null && attributes.containsKey("searchForm");
        if(search){
            form = (StudentSearch) attributes.get("searchForm");
            students = studentService.findAll(form);
            if(students.isEmpty()) model.addAttribute("notification", Notification.info("Aucun résultat"));
        } else {
            students = studentService.findAll(page);
        }
        model.addAttribute("housings", housingService.findAll());
        model.addAttribute("students", students.toList());
        model.addAttribute("totalPages", students.getTotalPages());
        model.addAttribute("currentPage", students.getNumber() + 1);
        model.addAttribute("startIndex", students.getPageable().getOffset());
        model.addAttribute("searchForm", form);
        model.addAttribute("search", search);
        return "admin/student/list";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'JANITOR')")
    @PostMapping("search")
    public String search(StudentSearch form, RedirectAttributes attributes){
        attributes.addFlashAttribute("searchForm", form);
        return "redirect:/student/list";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'JANITOR')")
    @GetMapping("save")
    public String save(Model model, @RequestParam(required = false) String id, RedirectAttributes attributes){
        Student student = new Student();
        if(StringUtils.isNotBlank(id))  student = studentService.findById(id).orElse(null);
        if(student == null){
            attributes.addFlashAttribute("notification", Notification.error("Étudiant introuvable"));
            return "redirect:/student/list";
        }
        model.addAttribute("countryCodes", SmsHelper.countryCodes);
        model.addAttribute("student", student.toForm());
        return "admin/student/save";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'JANITOR')")
    @PostMapping("save")
    public String save(@Valid @ModelAttribute("student") StudentForm student, BindingResult result, @RequestParam(required = false, defaultValue = "false") boolean multiple, Model model, RedirectAttributes attributes){
        if(StringUtils.isBlank(student.getId())) {
            String notNullMessage = "javax.validation.constraints.NotNull.message";
            String defaultMessage = "ne doit pas être nul";
            if(student.getCniRectoFile() == null || student.getCniRectoFile().isEmpty()) result.rejectValue("cniRectoFile", notNullMessage, defaultMessage);
            if(student.getCniVersoFile() == null || student.getCniVersoFile().isEmpty()) result.rejectValue("cniVersoFile", notNullMessage, defaultMessage);
            if(student.getBirthCertificateFile() == null || student.getBirthCertificateFile().isEmpty()) result.rejectValue("birthCertificateFile", notNullMessage, defaultMessage);
        }
        if(result.hasErrors()){
            model.addAttribute("countryCodes", SmsHelper.countryCodes);
            return "admin/student/save";
        }
        Notification notification =  studentService.save(student);
        if(multiple || notification.hasError()){
            model.addAttribute("notification", notification);
            model.addAttribute("countryCodes", SmsHelper.countryCodes);
            model.addAttribute("student", notification.hasError() ? student : new StudentForm());
            return "admin/student/save";
        }
        attributes.addFlashAttribute("notification", notification);
        return "redirect:/student/list";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'JANITOR')")
    @GetMapping("view/{id}")
    public String view(@PathVariable String id, Model model, RedirectAttributes attributes){
        Student student = studentService.findById(id).orElse(null);
        if(student == null){
            attributes.addFlashAttribute("notification", Notification.error("Étudiant introuvable"));
            return "redirect:/student/list";
        }
        model.addAttribute("student", student);
        return "admin/student/view";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'JANITOR')")
    @RequestMapping(value="toggle/{id}")
    public String toggle(@PathVariable String id, RedirectAttributes attributes){
        attributes.addFlashAttribute("notification", studentService.toggleById(id));
        return "redirect:/student/list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value="delete")
    public String deleteById(@RequestParam String id, @RequestParam(required = false, defaultValue = "false") boolean force, RedirectAttributes attributes, HttpServletRequest request){
        Notification notification =  studentService.deleteById(id, force, request);
        attributes.addFlashAttribute("notification", notification);
        return "redirect:/student/list";
    }
}
