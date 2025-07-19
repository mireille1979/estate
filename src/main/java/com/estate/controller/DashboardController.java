package com.estate.controller;

import com.estate.domain.entity.Student;
import com.estate.domain.entity.User;
import com.estate.domain.enumaration.Profil;
import com.estate.domain.enumaration.Status;
import com.estate.domain.exception.NotFoundException;
import com.estate.domain.service.face.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@Controller
@RequiredArgsConstructor
public class DashboardController {
    private final StudentService studentService;
    private final HousingService housingService;
    private final PaymentService paymentService;
    private final UserService userService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("dashboard")
    public String home(Model model, HttpSession session){
        User user = (User) session.getAttribute("user");
        if(user == null) throw new NotFoundException();
        if(Profil.STAFF.equals(user.getProfil())){
            model.addAttribute("users", userService.countByProfil(Profil.STAFF));
            model.addAttribute("students", studentService.count());
            model.addAttribute("housings", housingService.count());
            model.addAttribute("payments", paymentService.countByStatus(Status.SUBMITTED));
        } else {
            Student student = studentService.findByUserId(user.getId()).orElseThrow(NotFoundException::new);
            model.addAttribute("student", student);
        }

        return "dashboard";
    }

    @ResponseBody
    @GetMapping("explorer/**")
    public ResponseEntity<byte[]> moduleStrings(HttpServletRequest request, @RequestParam(required = false, defaultValue = "false") boolean download) {
        String requestURL = request.getRequestURL().toString();
        String path = requestURL.split("explorer/")[1];
        try {
            Path filePath = Paths.get(URLDecoder.decode(path, String.valueOf(StandardCharsets.UTF_8))).toAbsolutePath().normalize();
            MediaType contentType = MediaType.asMediaType(MimeType.valueOf(Files.probeContentType(filePath)));
            HttpHeaders headers = new HttpHeaders();
            ContentDisposition.Builder builder = ContentDisposition.inline();
            if(download) builder = ContentDisposition.attachment();
            headers.setContentDisposition(builder.filename(filePath.toFile().getName()).build());
            return ResponseEntity.ok().contentType(contentType).headers(headers).body(Files.readAllBytes(filePath));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("policy")
    public String getPolicy(RedirectAttributes attributes) {
        attributes.addAttribute("download", true);
        return "redirect:/explorer/documents/policy.pdf";
    }
}