/*
 * SGP - servico-usuarios
 */
package com.sgp.servico_usuarios.domain.service;

import com.sgp.servico_usuarios.api.dto.request.UniversidadeRequest;
import com.sgp.servico_usuarios.api.dto.response.UniversidadeResponse;
import com.sgp.servico_usuarios.domain.entity.Universidade;
import com.sgp.servico_usuarios.domain.repository.UniversidadeRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    public Page<UniversidadeResponse> listar(Pageable pageable) {
        return universidadeRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public UniversidadeResponse buscarPorId(Long id) {
        return toResponse(buscarOuFalhar(id));
    }
    
    public Universidade buscarOuFalhar(Long id) {
        return universidadeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Universidade " + id + " não encontrada"));
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

    // Soft delete: o @SoftDelete da entidade transforma o DELETE em UPDATE de deletado_em
    @Transactional
    public void remover(Long id) {
        universidadeRepository.delete(buscarOuFalhar(id));
    }

    private UniversidadeResponse toResponse(Universidade universidade) {
        return modelMapper.map(universidade, UniversidadeResponse.class);
    }
}
