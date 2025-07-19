package com.estate.domain.helper;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class EmailHelper {
    private final JavaMailSender mailSender;
    private final FreeMarkerConfigurer freeMarkerConfigurer;

    @Async
    public void sendMail(String to, String cc, String subject, String templateName, Locale locale, Map<String, Object> model, List<String> attachments) {
        try {
            Template freemarkerTemplate = freeMarkerConfigurer.getConfiguration().getTemplate(templateName, locale);
            String sender = Objects.requireNonNull(((JavaMailSenderImpl) mailSender).getUsername());
            String body = FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerTemplate, model);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true,"UTF-8");
            helper.setFrom(sender);
            helper.setTo(InternetAddress.parse(purge(StringUtils.defaultIfBlank(to, sender))));
            if(StringUtils.isNotEmpty(cc)) helper.setCc(InternetAddress.parse(purge(cc)));
            helper.setSubject(subject);
            helper.setText(body, true);
            for(int i = 0; i < attachments.size(); i++){
                try {
                    FileSystemResource file = new FileSystemResource(new File(attachments.get(i)));
                    helper.addAttachment(StringUtils.defaultString(file.getFilename() , "Fichier " + (i + 1)), file);
                } catch (Exception e) {
                    log.error("Unable to add attachment", e);
                }
            }
            mailSender.send(message);
        } catch (MessagingException | IOException | TemplateException e) {
            log.error("mail not sent", e);
        }
    }

    private String purge(String emails){
        return Arrays.stream(emails.split("[,;]")).filter(StringUtils::isNotBlank).distinct().collect(Collectors.joining(","));
    }
}
