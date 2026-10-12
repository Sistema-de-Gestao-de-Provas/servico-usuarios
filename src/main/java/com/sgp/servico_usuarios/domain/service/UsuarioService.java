/*
 * SGP - servico-usuarios
 */
package com.sgp.servico_usuarios.domain.service;

import com.sgp.servico_usuarios.api.dto.request.UsuarioRequest;
import com.sgp.servico_usuarios.api.dto.response.UsuarioResponse;
import com.sgp.servico_usuarios.domain.entity.Endereco;
import com.sgp.servico_usuarios.domain.entity.Usuario;
import com.sgp.servico_usuarios.domain.enums.Permissao;
import com.sgp.servico_usuarios.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 *
 * @author diego-casemiro
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UniversidadeService universidadeService;
    private final MateriaService materiaService;
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listar(Pageable pageable) {
        return usuarioRepository.findAllByDeletadoEmIsNull(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        return toResponse(buscarOuFalhar(id));
    }

    private Usuario buscarOuFalhar(Long id) {
        return usuarioRepository.findByIdAndDeletadoEmIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário " + id + " não encontrado"));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorKeycloakId(UUID keycloakId) {
        Usuario usuario = usuarioRepository.findByKeycloakIdAndDeletadoEmIsNull(keycloakId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "O usuário autenticado não possui cadastro"));
        return toResponse(usuario);
    }

    // O usuário já existe no Keycloak; aqui é gravado o perfil dele, ligado pelo keycloakId (sub do token)
    @Transactional
    public UsuarioResponse criar(UsuarioRequest request, UUID keycloakId, Permissao permissao) {
        if (usuarioRepository.existsByKeycloakId(keycloakId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O usuário autenticado já possui cadastro");
        }
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um usuário com o e-mail " + request.getEmail());
        }
        if (usuarioRepository.existsByCpf(request.getCpf())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um usuário com o CPF " + request.getCpf());
        }
        Usuario usuario = new Usuario();
        usuario.setKeycloakId(keycloakId);
        usuario.setPermissao(permissao);
        aplicarRequest(usuario, request);
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioRequest request) {
        Usuario usuario = buscarOuFalhar(id);
        if (usuarioRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um usuário com o e-mail " + request.getEmail());
        }
        if (usuarioRepository.existsByCpfAndIdNot(request.getCpf(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um usuário com o CPF " + request.getCpf());
        }
        aplicarRequest(usuario, request);
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public void remover(Long id) {
        Usuario usuario = buscarOuFalhar(id);
        LocalDateTime agora = LocalDateTime.now();
        usuario.setDeletadoEm(agora);
        if (usuario.getEndereco() != null) {
            usuario.getEndereco().setDeletadoEm(agora);
        }
        usuarioRepository.save(usuario);
    }

    private void aplicarRequest(Usuario usuario, UsuarioRequest request) {
        usuario.setNome(request.getNome());
        usuario.setCpf(request.getCpf());
        usuario.setEmail(request.getEmail());
        usuario.setTelefone(request.getTelefone());
        usuario.setUniversidade(universidadeService.buscarOuFalhar(request.getUniversidadeId()));
        usuario.getMaterias().clear();
        usuario.getMaterias().addAll(materiaService.buscarMaterias(request.getMateriaIds()));

        if (request.getEndereco() == null) {
            usuario.setEndereco(null);
        } else {
            Endereco endereco = usuario.getEndereco() != null ? usuario.getEndereco() : new Endereco();
            modelMapper.map(request.getEndereco(), endereco);
            usuario.setEndereco(endereco);
        }
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return modelMapper.map(usuario, UsuarioResponse.class);
    }
}
