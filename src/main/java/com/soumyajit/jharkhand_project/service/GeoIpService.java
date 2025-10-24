package com.soumyajit.jharkhand_project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;

@Service
public class GeoIpService {

    // Inject the configured RestTemplate bean
    private final RestTemplate restTemplate;

    @Autowired
    public GeoIpService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getLocation(String ip) {
        String url = "http://ip-api.com/json/" + ip;
        try {
            String response = restTemplate.getForObject(url, String.class);
            JSONObject json = new JSONObject(response);
            String city = json.optString("city", "Unknown");
            String country = json.optString("country", "Unknown");
            return city + ", " + country;
        } catch (Exception e) {
            // Log the error
            return "Unknown";
        }
    }
}
