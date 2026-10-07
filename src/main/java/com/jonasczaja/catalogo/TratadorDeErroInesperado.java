package com.jonasczaja.catalogo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice
public class TratadorDeErroInesperado {

    private static final Logger log = LoggerFactory.getLogger(TratadorDeErroInesperado.class);

    @ExceptionHandler(Exception.class)
    public ProblemDetail tratarErroInesperado(Exception ex){
        log.error("Erro não tratado", ex);

        ProblemDetail problema = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problema.setTitle("Erro Interno");
        problema.setDetail("Ocorreu um erro inesperado.");
        return problema;
    }
}
