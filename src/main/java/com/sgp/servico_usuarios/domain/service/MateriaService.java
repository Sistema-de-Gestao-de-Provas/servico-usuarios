/*
 * SGP - servico-usuarios
 */
package com.sgp.servico_usuarios.domain.service;

import com.sgp.servico_usuarios.domain.exception.ConflitoException;
import com.sgp.servico_usuarios.domain.exception.RecursoNaoEncontradoException;
import com.sgp.servico_usuarios.api.dto.request.MateriaRequest;
import com.sgp.servico_usuarios.api.dto.response.MateriaResponse;
import com.sgp.servico_usuarios.domain.entity.Materia;
import com.sgp.servico_usuarios.domain.repository.MateriaRepository;
import com.sgp.servico_usuarios.domain.repository.specification.MateriaSpecification;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author diego-casemiro
 */
@Service
@RequiredArgsConstructor
public class MateriaService {

    private final MateriaRepository materiaRepository;
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public Page<MateriaResponse> listar(String nome, Pageable pageable) {
        return materiaRepository.findAll(MateriaSpecification.filtrar(nome), pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public MateriaResponse buscarPorId(Long id) {
        return toResponse(buscarOuFalhar(id));
    }

    public Materia buscarOuFalhar(Long id) {
        return materiaRepository.findByIdAndDeletadoEmIsNull(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("materia.nao-encontrada", String.valueOf(id)));
    }

    public Set<Materia> buscarMaterias(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        Set<Materia> materias = new HashSet<>(materiaRepository.findAllByIdInAndDeletadoEmIsNull(ids));
        if (materias.size() != ids.size()) {
            Set<Long> encontrados = materias.stream().map(Materia::getId).collect(Collectors.toSet());
            String faltantes = ids.stream()
                    .filter(id -> !encontrados.contains(id))
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
            throw new RecursoNaoEncontradoException("materia.nao-encontrada", faltantes);
        }
        return materias;
    }

    @Transactional
    public MateriaResponse criar(MateriaRequest request) {
        if (materiaRepository.existsByNomeIgnoreCase(request.getNome())) {
            throw new ConflitoException("materia.nome-ja-cadastrado", request.getNome());
        }
        Materia materia = modelMapper.map(request, Materia.class);
        return toResponse(materiaRepository.save(materia));
    }

    @Transactional
    public MateriaResponse atualizar(Long id, MateriaRequest request) {
        Materia materia = buscarOuFalhar(id);
        if (materiaRepository.existsByNomeIgnoreCaseAndIdNot(request.getNome(), id)) {
            throw new ConflitoException("materia.nome-ja-cadastrado", request.getNome());
        }
        modelMapper.map(request, materia);
        return toResponse(materiaRepository.save(materia));
    }

    @Transactional
    public void remover(Long id) {
        Materia materia = buscarOuFalhar(id);
        materia.setDeletadoEm(LocalDateTime.now());
        materiaRepository.save(materia);
    }

    private MateriaResponse toResponse(Materia materia) {
        return modelMapper.map(materia, MateriaResponse.class);
    }
}
