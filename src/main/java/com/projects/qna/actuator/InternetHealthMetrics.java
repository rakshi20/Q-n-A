package com.projects.qna.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.net.URLConnection;

@Component
public class InternetHealthMetrics implements HealthIndicator {
    @Override
    public Health health() {
        return (internetConnected()) ? Health.up().withDetail("Internet", "Connected to Internet").build()
                : Health.up().withDetail("Internet", "Not Connected to Internet !").build();
    }

    private boolean internetConnected() {
        boolean connected = true;
        try {
            URL url = new URL("https://google.com");
            URLConnection connection = url.openConnection();
            connection.connect();
        }
        catch (Exception e) {
            connected = false;
        }
        return connected;
    }
}
