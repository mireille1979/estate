package com.estate.domain.service.face;

import org.springframework.http.ResponseEntity;

import java.util.Locale;

public interface AuthenticationService {

    ResponseEntity<?> refreshToken(String token, Locale locale);

    long resetPassword(String email);

}
