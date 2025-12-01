package com.stationflow.backend.security.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.util.Map;

@Service
public class RecaptchaService {

    private static final String RECAPTCHA_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
    // Test Secret Key
    @org.springframework.beans.factory.annotation.Value("${recaptcha.secret}")
    private String RECAPTCHA_SECRET;

    public boolean verify(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("secret", RECAPTCHA_SECRET);
        map.add("response", token);

        try {
            Map<String, Object> response = restTemplate.postForObject(RECAPTCHA_VERIFY_URL, map, Map.class);
            return response != null && (Boolean) response.get("success");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
