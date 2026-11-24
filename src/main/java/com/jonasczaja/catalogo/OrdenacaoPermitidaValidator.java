package com.jonasczaja.catalogo;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class OrdenacaoPermitidaValidator implements
        ConstraintValidator<OrdenacaoPermitida, Pageable> {

    private List<String> permitidos;

    @Override
    public void initialize(OrdenacaoPermitida anotacao){
        this.permitidos = List.of(anotacao.value());
    }

    @Override
    public boolean isValid(Pageable pageable, ConstraintValidatorContext context){
        if (pageable == null){
            return true;
        }
        return pageable.getSort().stream().allMatch(ordem -> permitidos.contains(ordem.getProperty()));
    }
}
