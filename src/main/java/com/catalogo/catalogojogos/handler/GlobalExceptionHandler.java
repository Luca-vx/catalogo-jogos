package com.catalogo.catalogojogos.handler;

import com.catalogo.catalogojogos.exception.GameNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetail.setTitle("Internal error");
        problemDetail.setDetail("An unexpected error occurred.");
        return problemDetail;
    }

    @ExceptionHandler(GameNotFoundException.class)
    public ProblemDetail handleGameNotFoundException(GameNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Game Not Found");
        problem.setDetail(exception.getMessage());
        return problem;
    }
}
