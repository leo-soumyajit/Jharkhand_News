package com.soumyajit.jharkhand_project.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;

@Service
public class GeoIpService {
    private final RestTemplate restTemplate = new RestTemplate();

    public String getLocation(String ip) {
        String url = "http://ip-api.com/json/" + ip;
        try {
            String response = restTemplate.getForObject(url, String.class);
            JSONObject json = new JSONObject(response);
            String city = json.optString("city", "Unknown");
            String country = json.optString("country", "Unknown");
            return city + ", " + country;
        } catch (Exception e) {
            return "Unknown";
        }
    }
}
