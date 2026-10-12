package io.github.hd4nielbu.lazystudio.adapters.in.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.github.hd4nielbu.lazystudio.application.NoEncontradoException;
import io.github.hd4nielbu.lazystudio.domain.ReglaVioladaException;

/** Traduce las excepciones del núcleo a respuestas HTTP; el campo "detail" lleva el mensaje para el alumno. */
@RestControllerAdvice
class ManejoDeErrores {

    @ExceptionHandler
    ProblemDetail reglaViolada(ReglaVioladaException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler
    ProblemDetail noEncontrado(NoEncontradoException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
