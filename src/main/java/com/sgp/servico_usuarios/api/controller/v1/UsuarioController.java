/*
 * SGP - servico-usuarios
 */
package com.sgp.servico_usuarios.api.controller.v1;

import com.sgp.servico_usuarios.api.dto.request.UsuarioRequest;
import com.sgp.servico_usuarios.api.dto.response.UsuarioResponse;
import com.sgp.servico_usuarios.domain.enums.Permissao;
import com.sgp.servico_usuarios.domain.service.UsuarioService;
import jakarta.validation.Valid;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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
@RequestMapping("/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    // Ex.: GET /v1/usuarios?page=0&size=20&sort=nome,asc
    @GetMapping
    public PagedModel<UsuarioResponse> listar(@PageableDefault(sort = "nome") Pageable pageable) {
        return new PagedModel<>(usuarioService.listar(pageable));
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

    // Cadastra o perfil do usuário autenticado: o keycloakId é o "sub" do token e a permissão vem das roles do Keycloak
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse criar(@Valid @RequestBody UsuarioRequest request, JwtAuthenticationToken token) {
        UUID keycloakId = UUID.fromString(token.getToken().getSubject());
        return usuarioService.criar(request, keycloakId, permissaoDoToken(token));
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

    // Se o token tiver mais de uma role, vale a de maior privilégio; sem nenhuma, PROFESSOR
    private Permissao permissaoDoToken(JwtAuthenticationToken token) {
        Set<String> roles = token.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        if (roles.contains("ROLE_" + Permissao.ADMIN.name())) {
            return Permissao.ADMIN;
        }
        if (roles.contains("ROLE_" + Permissao.DIRETOR.name())) {
            return Permissao.DIRETOR;
        }
        return Permissao.PROFESSOR;
    }
}
