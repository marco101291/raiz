package com.raiz.web;

import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

/**
 * Errores como {@link ProblemDetail} (RFC 9457), con textos en español. Extender
 * {@link ResponseEntityExceptionHandler} cubre las excepciones estándar de Spring MVC; aquí solo se
 * ajustan los mensajes y se agregan las propias.
 */
@RestControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NerveRootNotFoundException.class)
    ProblemDetail notFound(NerveRootNotFoundException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Raíz no encontrada");
        return problem;
    }

    /** Las invariantes del dominio (p. ej. intensidad fuera de 0–10) lanzan IllegalArgumentException. */
    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail invalidDomainData(IllegalArgumentException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Datos no válidos");
        return problem;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var problem = ProblemDetail.forStatusAndDetail(status, "Hay campos con errores.");
        problem.setTitle("Datos no válidos");
        List<FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new FieldError(e.getField(), e.getDefaultMessage()))
                .toList();
        problem.setProperty("errors", errors);
        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var problem = ProblemDetail.forStatusAndDetail(status,
                "El cuerpo no es JSON válido o contiene valores que no existen en el catálogo.");
        problem.setTitle("Petición no legible");
        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var problem = ProblemDetail.forStatusAndDetail(status,
                "Valor no válido para '" + ex.getPropertyName() + "': " + ex.getValue());
        problem.setTitle("Parámetro no válido");
        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    record FieldError(String field, String message) {
    }
}
