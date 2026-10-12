/*
 * SGP - servico-usuarios
 */
package com.sgp.servico_usuarios.domain.service;

import com.sgp.servico_usuarios.domain.exception.RecursoNaoEncontradoException;
import com.sgp.servico_usuarios.api.dto.request.UniversidadeRequest;
import com.sgp.servico_usuarios.api.dto.response.UniversidadeResponse;
import com.sgp.servico_usuarios.domain.entity.Universidade;
import com.sgp.servico_usuarios.domain.repository.UniversidadeRepository;
import com.sgp.servico_usuarios.domain.repository.specification.UniversidadeSpecification;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author diego-casemiro
 */
@Service
@RequiredArgsConstructor
public class UniversidadeService {

    private final UniversidadeRepository universidadeRepository;
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public Page<UniversidadeResponse> listar(
            String nome,
            String cidade,
            String estado,
            Pageable pageable) {
        return universidadeRepository.findAll(UniversidadeSpecification.filtrar(nome, cidade, estado), pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public UniversidadeResponse buscarPorId(Long id) {
        return toResponse(buscarOuFalhar(id));
    }

    public Universidade buscarOuFalhar(Long id) {
        return universidadeRepository.findByIdAndDeletadoEmIsNull(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("universidade.nao-encontrada", String.valueOf(id)));
    }

    @Transactional
    public UniversidadeResponse criar(UniversidadeRequest request) {
        Universidade universidade = modelMapper.map(request, Universidade.class);
        return toResponse(universidadeRepository.save(universidade));
    }

    @Transactional
    public UniversidadeResponse atualizar(Long id, UniversidadeRequest request) {
        Universidade universidade = buscarOuFalhar(id);
        modelMapper.map(request, universidade);
        return toResponse(universidadeRepository.save(universidade));
    }

    @Transactional
    public void remover(Long id) {
        Universidade universidade = buscarOuFalhar(id);
        universidade.setDeletadoEm(LocalDateTime.now());
        universidadeRepository.save(universidade);
    }

    private UniversidadeResponse toResponse(Universidade universidade) {
        return modelMapper.map(universidade, UniversidadeResponse.class);
    }
}
