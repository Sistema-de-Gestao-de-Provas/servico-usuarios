/*
 * SGP - servico-usuarios
 */
package com.sgp.servico_usuarios.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.sgp.servico_usuarios.api.dto.request.MateriaRequest;
import com.sgp.servico_usuarios.api.dto.response.MateriaResponse;
import com.sgp.servico_usuarios.config.mapper.ModelMapperConfig;
import com.sgp.servico_usuarios.domain.entity.Materia;
import com.sgp.servico_usuarios.domain.exception.ConflitoException;
import com.sgp.servico_usuarios.domain.exception.RecursoNaoEncontradoException;
import com.sgp.servico_usuarios.domain.repository.MateriaRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author diego-casemiro
 */
@ExtendWith(MockitoExtension.class)
class MateriaServiceTest {

    @Mock
    private MateriaRepository materiaRepository;

    private MateriaService materiaService;

    @BeforeEach
    void setUp() {
        materiaService = new MateriaService(materiaRepository, new ModelMapperConfig().modelMapper());
    }

    @Test
    void listarDeveMapearAsMateriasDaPagina() {
        Pageable pageable = PageRequest.of(0, 20);
        when(materiaRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(materia(1L, "Cálculo"), materia(2L, "Física"))));

        Page<MateriaResponse> resultado = materiaService.listar("c", pageable);

        assertThat(resultado.getContent()).extracting(MateriaResponse::getNome).containsExactly("Cálculo", "Física");
        verify(materiaRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void buscarPorIdDeveRetornarAMateria() {
        when(materiaRepository.findByIdAndDeletadoEmIsNull(1L)).thenReturn(Optional.of(materia(1L, "Cálculo")));

        MateriaResponse resultado = materiaService.buscarPorId(1L);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNome()).isEqualTo("Cálculo");
    }

    @Test
    void buscarPorIdDeveLancarNaoEncontradoQuandoAMateriaNaoExiste() {
        when(materiaRepository.findByIdAndDeletadoEmIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> materiaService.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("materia.nao-encontrada");
    }

    @Test
    void buscarMateriasDeveRetornarConjuntoVazioSemConsultarOBanco() {
        assertThat(materiaService.buscarMaterias(null)).isEmpty();
        assertThat(materiaService.buscarMaterias(Set.of())).isEmpty();

        verifyNoInteractions(materiaRepository);
    }

    @Test
    void buscarMateriasDeveRetornarTodasAsMateriasEncontradas() {
        when(materiaRepository.findAllByIdInAndDeletadoEmIsNull(Set.of(1L, 2L)))
                .thenReturn(List.of(materia(1L, "Cálculo"), materia(2L, "Física")));

        Set<Materia> resultado = materiaService.buscarMaterias(Set.of(1L, 2L));

        assertThat(resultado).extracting(Materia::getId).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void buscarMateriasDeveInformarOsIdsQueNaoForamEncontrados() {
        when(materiaRepository.findAllByIdInAndDeletadoEmIsNull(Set.of(1L, 99L)))
                .thenReturn(List.of(materia(1L, "Cálculo")));

        assertThatThrownBy(() -> materiaService.buscarMaterias(Set.of(1L, 99L)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("materia.nao-encontrada")
                .extracting("args")
                .isEqualTo(new Object[]{"99"});
    }

    @Test
    void criarDeveGravarAMateria() {
        when(materiaRepository.existsByNomeIgnoreCase("Química")).thenReturn(false);
        when(materiaRepository.save(any(Materia.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        MateriaResponse resultado = materiaService.criar(new MateriaRequest("Química"));

        assertThat(resultado.getNome()).isEqualTo("Química");
        verify(materiaRepository).save(any(Materia.class));
    }

    @Test
    void criarDeveRecusarNomeJaCadastrado() {
        when(materiaRepository.existsByNomeIgnoreCase("Cálculo")).thenReturn(true);

        assertThatThrownBy(() -> materiaService.criar(new MateriaRequest("Cálculo")))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("materia.nome-ja-cadastrado");
        verify(materiaRepository, never()).save(any());
    }

    @Test
    void atualizarDeveAlterarONomeDaMateria() {
        Materia materia = materia(1L, "Calculo");
        when(materiaRepository.findByIdAndDeletadoEmIsNull(1L)).thenReturn(Optional.of(materia));
        when(materiaRepository.existsByNomeIgnoreCaseAndIdNot("Cálculo", 1L)).thenReturn(false);
        when(materiaRepository.save(materia)).thenReturn(materia);

        MateriaResponse resultado = materiaService.atualizar(1L, new MateriaRequest("Cálculo"));

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNome()).isEqualTo("Cálculo");
    }

    @Test
    void atualizarDeveRecusarNomeDeOutraMateria() {
        when(materiaRepository.findByIdAndDeletadoEmIsNull(1L)).thenReturn(Optional.of(materia(1L, "Cálculo")));
        when(materiaRepository.existsByNomeIgnoreCaseAndIdNot("Física", 1L)).thenReturn(true);

        assertThatThrownBy(() -> materiaService.atualizar(1L, new MateriaRequest("Física")))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("materia.nome-ja-cadastrado");
        verify(materiaRepository, never()).save(any());
    }

    @Test
    void removerDeveMarcarAMateriaComoDeletada() {
        Materia materia = materia(1L, "Cálculo");
        when(materiaRepository.findByIdAndDeletadoEmIsNull(1L)).thenReturn(Optional.of(materia));

        materiaService.remover(1L);

        ArgumentCaptor<Materia> salva = ArgumentCaptor.forClass(Materia.class);
        verify(materiaRepository).save(salva.capture());
        assertThat(salva.getValue().getDeletadoEm()).isNotNull();
    }

    private Materia materia(Long id, String nome) {
        Materia materia = new Materia();
        ReflectionTestUtils.setField(materia, "id", id);
        materia.setNome(nome);
        return materia;
    }
}
