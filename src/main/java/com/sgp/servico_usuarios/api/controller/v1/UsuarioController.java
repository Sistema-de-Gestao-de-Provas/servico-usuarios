/*
 * SGP - servico-usuarios
 */
package com.sgp.servico_usuarios.api.controller.v1;

import com.sgp.servico_usuarios.api.dto.request.UsuarioRequest;
import com.sgp.servico_usuarios.api.dto.response.UsuarioResponse;
import com.sgp.servico_usuarios.domain.enums.Permissao;
import com.sgp.servico_usuarios.domain.service.UsuarioService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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
@RequestMapping("/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    // Ex.: GET /v1/usuarios?nome=maria&permissao=PROFESSOR&universidadeId=1&page=0&size=20&sort=nome,asc
    @GetMapping
    public PagedModel<UsuarioResponse> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) Permissao permissao,
            @RequestParam(required = false) Long universidadeId,
            @PageableDefault(sort = "nome") Pageable pageable) {
        return new PagedModel<>(usuarioService.listar(nome, email, cpf, permissao, universidadeId, pageable));
    }

    // Dados do usuário dono do token
    @GetMapping("/me")
    public UsuarioResponse buscarAutenticado(JwtAuthenticationToken token) {
        return usuarioService.buscarPorKeycloakId(UUID.fromString(token.getToken().getSubject()));
    }

    @GetMapping("/{id}")
    public UsuarioResponse buscarPorId(@PathVariable Long id) {
        return usuarioService.buscarPorId(id);
    }

    // Público; o token só é necessário quando um ADMIN cadastra alguém com outra permissão
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse criar(@Valid @RequestBody UsuarioRequest request, Authentication authentication) {
        boolean solicitanteAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> ("ROLE_" + Permissao.ADMIN.name()).equals(authority.getAuthority()));
        return usuarioService.criar(request, solicitanteAdmin);
    }

    @PutMapping("/{id}")
    public UsuarioResponse atualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest request) {
        return usuarioService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {
        usuarioService.remover(id);
    }
}
