package com.jonasczaja.catalogo;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = OrdenacaoPermitidaValidator.class)
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface OrdenacaoPermitida {

    String[] value();

    String message() default "Ordenação não permitida. Aceitas: {value}";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
