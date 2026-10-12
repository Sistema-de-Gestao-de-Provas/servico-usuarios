/*
 * SGP - servico-usuarios
 */
package com.sgp.servico_usuarios.api.controller.v1;

import com.sgp.servico_usuarios.api.dto.request.UniversidadeRequest;
import com.sgp.servico_usuarios.api.dto.response.UniversidadeResponse;
import com.sgp.servico_usuarios.domain.service.UniversidadeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author diego-casemiro
 */
@RestController
@RequestMapping("/v1/universidades")
@RequiredArgsConstructor
public class UniversidadeController {

    private final UniversidadeService universidadeService;

    // Ex.: GET /v1/universidades?page=0&size=20&sort=nome,asc
    @GetMapping
    public PagedModel<UniversidadeResponse> listar(@PageableDefault(size = 50, sort = "nome") Pageable pageable) {
        return new PagedModel<>(universidadeService.listar(pageable));
    }

    @GetMapping("/{id}")
    public UniversidadeResponse buscarPorId(@PathVariable Long id) {
        return universidadeService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UniversidadeResponse criar(@Valid @RequestBody UniversidadeRequest request) {
        return universidadeService.criar(request);
    }

    @PutMapping("/{id}")
    public UniversidadeResponse atualizar(@PathVariable Long id, @Valid @RequestBody UniversidadeRequest request) {
        return universidadeService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {
        universidadeService.remover(id);
    }
}
