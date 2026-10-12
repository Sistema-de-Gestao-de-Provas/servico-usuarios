/*
 * SGP - servico-usuarios
 */
package com.sgp.servico_usuarios.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sgp.servico_usuarios.api.dto.request.UniversidadeRequest;
import com.sgp.servico_usuarios.api.dto.response.UniversidadeResponse;
import com.sgp.servico_usuarios.config.mapper.ModelMapperConfig;
import com.sgp.servico_usuarios.domain.entity.Universidade;
import com.sgp.servico_usuarios.domain.exception.RecursoNaoEncontradoException;
import com.sgp.servico_usuarios.domain.repository.UniversidadeRepository;
import java.util.List;
import java.util.Optional;
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
class UniversidadeServiceTest {

    @Mock
    private UniversidadeRepository universidadeRepository;

    private UniversidadeService universidadeService;

    @BeforeEach
    void setUp() {
        universidadeService = new UniversidadeService(universidadeRepository, new ModelMapperConfig().modelMapper());
    }

    @Test
    void listarDeveMapearAsUniversidadesDaPagina() {
        Pageable pageable = PageRequest.of(0, 20);
        when(universidadeRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(universidade(1L, "UFPR", "Curitiba", "PR"))));

        Page<UniversidadeResponse> resultado = universidadeService.listar("uf", "curitiba", "pr", pageable);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getNome()).isEqualTo("UFPR");
        assertThat(resultado.getContent().get(0).getEstado()).isEqualTo("PR");
    }

    @Test
    void buscarPorIdDeveRetornarAUniversidade() {
        when(universidadeRepository.findByIdAndDeletadoEmIsNull(1L))
                .thenReturn(Optional.of(universidade(1L, "UFPR", "Curitiba", "PR")));

        UniversidadeResponse resultado = universidadeService.buscarPorId(1L);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getCidade()).isEqualTo("Curitiba");
    }

    @Test
    void buscarPorIdDeveLancarNaoEncontradoQuandoAUniversidadeNaoExiste() {
        when(universidadeRepository.findByIdAndDeletadoEmIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> universidadeService.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("universidade.nao-encontrada");
    }

    @Test
    void criarDeveGravarAUniversidade() {
        when(universidadeRepository.save(any(Universidade.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        UniversidadeResponse resultado = universidadeService.criar(new UniversidadeRequest("UFRJ", "Rio de Janeiro", "RJ"));

        assertThat(resultado.getNome()).isEqualTo("UFRJ");
        assertThat(resultado.getCidade()).isEqualTo("Rio de Janeiro");
        assertThat(resultado.getEstado()).isEqualTo("RJ");
    }

    @Test
    void atualizarDeveAlterarOsDadosDaUniversidade() {
        Universidade universidade = universidade(1L, "UFPR", "Curitiba", "PR");
        when(universidadeRepository.findByIdAndDeletadoEmIsNull(1L)).thenReturn(Optional.of(universidade));
        when(universidadeRepository.save(universidade)).thenReturn(universidade);

        UniversidadeResponse resultado = universidadeService.atualizar(1L,
                new UniversidadeRequest("UTFPR", "Ponta Grossa", "PR"));

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNome()).isEqualTo("UTFPR");
        assertThat(resultado.getCidade()).isEqualTo("Ponta Grossa");
    }

    @Test
    void atualizarDeveLancarNaoEncontradoQuandoAUniversidadeNaoExiste() {
        when(universidadeRepository.findByIdAndDeletadoEmIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> universidadeService.atualizar(99L, new UniversidadeRequest("UFPR", "Curitiba", "PR")))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(universidadeRepository, never()).save(any());
    }

    @Test
    void removerDeveMarcarAUniversidadeComoDeletada() {
        Universidade universidade = universidade(1L, "UFPR", "Curitiba", "PR");
        when(universidadeRepository.findByIdAndDeletadoEmIsNull(1L)).thenReturn(Optional.of(universidade));

        universidadeService.remover(1L);

        ArgumentCaptor<Universidade> salva = ArgumentCaptor.forClass(Universidade.class);
        verify(universidadeRepository).save(salva.capture());
        assertThat(salva.getValue().getDeletadoEm()).isNotNull();
    }

    private Universidade universidade(Long id, String nome, String cidade, String estado) {
        Universidade universidade = new Universidade();
        ReflectionTestUtils.setField(universidade, "id", id);
        universidade.setNome(nome);
        universidade.setCidade(cidade);
        universidade.setEstado(estado);
        return universidade;
    }
}
