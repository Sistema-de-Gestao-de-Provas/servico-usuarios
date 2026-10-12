/*
 * SGP - servico-usuarios
 */
package com.sgp.servico_usuarios.config.exceptionhandler;

import com.sgp.servico_usuarios.domain.exception.ApiException;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 *
 * @author diego-casemiro
 */
// Transforma as exceções em respostas no formato Problem Details (RFC 9457):
// { "status": 404, "title": "Not Found", "detail": "Universidade 99 não encontrada.", ... }
@RestControllerAdvice
@RequiredArgsConstructor
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(ApiException.class)
    public ProblemDetail handleApi(ApiException ex) {
        return ProblemDetail.forStatusAndDetail(ex.getStatus(), getMessage(ex.getMessage(), ex.getArgs()));
    }

    // Ex.: dois cadastros simultâneos com o mesmo CPF passam pela validação e um deles bate na constraint do banco
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, getMessage("dados.conflito"));
    }

    // Erros do @Valid: devolve a lista de campos inválidos em "erros", além da mensagem geral
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> erros = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(erro -> erros.putIfAbsent(erro.getField(), erro.getDefaultMessage()));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, getMessage("validacao.campos-invalidos"));
        problem.setProperty("erros", erros);
        return ResponseEntity.badRequest().body(problem);
    }

    // Se a chave não existir no arquivo de mensagens, devolve a própria chave em vez de estourar um 500
    private String getMessage(String messageKey, Object... args) {
        return messageSource.getMessage(messageKey, args, messageKey, LocaleContextHolder.getLocale());
    }
}
