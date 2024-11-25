package com.projects.qna.exceptions;

import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ServiceExceptionHandler {

    @ExceptionHandler({ServiceException.class})
    public ResponseEntity<ServiceErrorBody> handleServiceException(ServiceException exception) {
        return constructErrorResponseEntity(exception.getError());
    }

    private ResponseEntity<ServiceErrorBody> constructErrorResponseEntity(ServiceError error) {
        return ResponseEntity.status(error.getStatus()).body(new ServiceErrorBody(error.name(), error.getMessage()));
    }

    @Data
    public static class ServiceErrorBody {
        private final String code;
        private final String message;
    }
}
