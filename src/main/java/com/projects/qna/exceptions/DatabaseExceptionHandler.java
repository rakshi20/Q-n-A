package com.projects.qna.exceptions;

import jakarta.validation.ConstraintViolationException;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class DatabaseExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDatabaseExceptions(DataIntegrityViolationException e) {
        Map<String, String> errors = new HashMap<>();
        if(e.getRootCause() instanceof PSQLException sqlEx) {
            errors.put("server error message", String.valueOf(sqlEx.getServerErrorMessage()));
            errors.put("SQL state", sqlEx.getSQLState());
        }
        else if(e.getRootCause() instanceof ConstraintViolationException constraintEx) {
            errors.put("constraints", constraintEx.getConstraintViolations().toString());
        }
        else {
            errors.put("message", e.getMostSpecificCause().getMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}
