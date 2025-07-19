package com.estate.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Arrays;
import java.util.Locale;

@Configuration
public class MessageSourceConfiguration {

    @Bean
    public ResourceBundleMessageSource messageSource(){
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasenames("lang/messages");
        source.setDefaultEncoding("UTF-8");
        source.setUseCodeAsDefaultMessage(true);
        return source;
    }

    @Bean
    public LocaleResolver localeResolver(){
        HeaderLocaleResolver localeResolver = new HeaderLocaleResolver();
        localeResolver.setName("language");
        localeResolver.setDefaultLocale(Locale.FRENCH);
        localeResolver.setSupportedLocales(Arrays.asList(new Locale("en"), new Locale("fr")));
        return localeResolver;
    }
}
