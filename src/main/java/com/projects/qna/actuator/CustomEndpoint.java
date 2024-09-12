package com.projects.qna.actuator;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

@Endpoint(id = "custom")
@Component
public class CustomEndpoint {

    @ReadOperation
    public CustomEndpointResponse display() {
        return new CustomEndpointResponse(Status.UP, "This is a custom endpoint");
    }
}
