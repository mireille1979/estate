package com.estate.domain.service.impl;

import com.estate.domain.entity.Log;
import com.estate.domain.form.ObitResponse;
import com.estate.domain.helper.EmailHelper;
import com.estate.domain.service.face.NotificationService;
import com.estate.repository.LogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final LogRepository logRepository;
    private final EmailHelper emailHelper;
    @Value("${sms.api.url}")
    private String baseUrl;
    @Value("${sms.api.key}")
    private String apiKey;

    @Override
    public ObitResponse sendSMS(String sender, String destination, String message) {
        ObitResponse obitResponse = null;
        if(baseUrl != null && apiKey != null){
            OkHttpClient client = new OkHttpClient();
            HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl).newBuilder();
            urlBuilder.addQueryParameter("key_api", apiKey);
            urlBuilder.addQueryParameter("sender", sender);
            urlBuilder.addQueryParameter("destination", destination.replaceAll(" ", "").replace("+", ""));
            urlBuilder.addQueryParameter("message", message);
            String url = urlBuilder.build().toString();
            Request request = new Request.Builder().url(url).build();
            try {
                Response response = client.newCall(request).execute();
                try (ResponseBody obitResponseBody = response.body()) {
                    obitResponse = new ObjectMapper().readValue(obitResponseBody.string(), ObitResponse.class);
                    if(obitResponse.getCode() == 901) {
                        emailHelper.sendMail("", "gatienjordanlonlaep@gmail.com", "SOLDE SMS INSUFFISANT", "insufficient_balance.ftl", Locale.FRENCH, new HashMap<>(), new ArrayList<>());
                    }
                } catch (IOException e) {
                    logRepository.save(Log.error("Unable to read SMS API response body", ExceptionUtils.getStackTrace(e)));
                    log.error("Unable to read SMS API response body", e);
                }
            } catch (IOException e) {
                logRepository.save(Log.error("Unable to send SMS API request", ExceptionUtils.getStackTrace(e)));
                log.error("Unable to send SMS API request", e);
            }
        }
        return obitResponse;
    }
}
