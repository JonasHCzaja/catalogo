package com.jonasczaja.catalogo;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail tratarValidacao(MethodArgumentNotValidException exception){
        ProblemDetail problema = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problema.setTitle("Requisição inválida");
        problema.setDetail("Um ou mais campos estão incorretos");

        Map<String, String> campos = exception.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (a, b) -> a + "; " + b));

        problema.setProperty("campos", campos);
        return problema;
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ProblemDetail tratarRegraDeNegocio(RegraDeNegocioException exception){
        ProblemDetail problema = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problema.setTitle("Regra de negócio violada");
        problema.setDetail(exception.getMessage());
        return problema;
    }

    @ExceptionHandler(ProdutoNaoEncontradoException.class)
    public ProblemDetail tratarNaoEncontrado(ProdutoNaoEncontradoException exception){
        ProblemDetail problema = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problema.setTitle("Recurso não encontrado");
        problema.setDetail(exception.getMessage());
        return problema;
    }
}
