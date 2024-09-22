package com.projects.qna.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;


@RequiredArgsConstructor
@Getter
public enum ServiceError {
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "Entity not found");
    private final HttpStatus status;
    private final String message;
}
