package com.soumyajit.jharkhand_project.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import java.util.TimeZone;

@Configuration
public class TimezoneConfig {

    @PostConstruct
    public void init() {
        System.setProperty("user.timezone", "Asia/Kolkata");
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        System.out.println("✅ JVM Timezone successfully set to: " + TimeZone.getDefault().getID());
        System.out.println("✅ System property user.timezone: " + System.getProperty("user.timezone"));
    }
}
