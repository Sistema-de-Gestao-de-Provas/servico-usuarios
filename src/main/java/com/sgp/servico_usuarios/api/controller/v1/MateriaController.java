/*
 * SGP - servico-usuarios
 */
package com.sgp.servico_usuarios.api.controller.v1;

import com.sgp.servico_usuarios.api.dto.request.MateriaRequest;
import com.sgp.servico_usuarios.api.dto.response.MateriaResponse;
import com.sgp.servico_usuarios.domain.service.MateriaService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author diego-casemiro
 */
@RestController
@RequestMapping("/v1/materias")
@RequiredArgsConstructor
public class MateriaController {

    private final MateriaService materiaService;

    @GetMapping
    public PagedModel<MateriaResponse> listar(
            @RequestParam(required = false) String nome,
            @PageableDefault(size = 50, sort = "nome") Pageable pageable) {
        return new PagedModel<>(materiaService.listar(nome, pageable));
    }

    @GetMapping("/{id}")
    public MateriaResponse buscarPorId(@PathVariable Long id) {
        return materiaService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MateriaResponse criar(@Valid @RequestBody MateriaRequest request) {
        return materiaService.criar(request);
    }

    @PutMapping("/{id}")
    public MateriaResponse atualizar(@PathVariable Long id, @Valid @RequestBody MateriaRequest request) {
        return materiaService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {
        materiaService.remover(id);
    }
}
