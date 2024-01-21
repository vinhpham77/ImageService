package org.caykhe.imageservice.configs;

import lombok.RequiredArgsConstructor;
import org.caykhe.imageservice.dtos.ApiException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@RequiredArgsConstructor
public class ExceptionHandlerConfig {

    @ExceptionHandler({ApiException.class})
    public ResponseEntity<?> handleException(final ApiException exception) {
        return new ResponseEntity<>(exception.getMessage(), exception.getStatus());
    }

}