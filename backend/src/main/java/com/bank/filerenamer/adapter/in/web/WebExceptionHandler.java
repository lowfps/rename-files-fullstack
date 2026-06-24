package com.bank.filerenamer.adapter.in.web;

import com.bank.filerenamer.domain.exception.RuleNotFoundException;
import com.bank.filerenamer.domain.exception.RunNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduce las excepciones de dominio a respuestas HTTP. */
@RestControllerAdvice
public class WebExceptionHandler {

    @ExceptionHandler({RuleNotFoundException.class, RunNotFoundException.class})
    public ProblemDetail handleNotFound(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadRequest(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}
