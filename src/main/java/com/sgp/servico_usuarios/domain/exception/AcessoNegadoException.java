/*
 * SGP - servico-usuarios
 */
package com.sgp.servico_usuarios.domain.exception;

import org.springframework.http.HttpStatus;

/**
 *
 * @author diego-casemiro
 */
public class AcessoNegadoException extends ApiException {

    public AcessoNegadoException(String messageKey, Object... args) {
        super(messageKey, args);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.FORBIDDEN;
    }
}
