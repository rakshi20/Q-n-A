package com.projects.qna.actuator;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.actuate.health.Status;

@Data
@AllArgsConstructor
public class CustomEndpointResponse {
    private Status status;
    private String details;
}
