package com.estate.configuration;

import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

@Setter
public class HeaderLocaleResolver extends AcceptHeaderLocaleResolver {
    private String name = "Accept-Language";
    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        String header = request.getHeader(name);
        return StringUtils.isEmpty(header) ? getDefaultLocale() : Locale.lookup(Locale.LanguageRange.parse(header), getSupportedLocales());
    }
}
