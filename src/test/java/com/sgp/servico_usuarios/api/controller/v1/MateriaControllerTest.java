/*
 * SGP - servico-usuarios
 */
package com.sgp.servico_usuarios.api.controller.v1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sgp.servico_usuarios.api.dto.request.MateriaRequest;
import com.sgp.servico_usuarios.api.dto.response.MateriaResponse;
import com.sgp.servico_usuarios.config.internationalization.InternationalizationConfig;
import com.sgp.servico_usuarios.config.security.SecurityConfig;
import com.sgp.servico_usuarios.domain.exception.ConflitoException;
import com.sgp.servico_usuarios.domain.exception.RecursoNaoEncontradoException;
import com.sgp.servico_usuarios.domain.service.MateriaService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 *
 * @author diego-casemiro
 */
@WebMvcTest(MateriaController.class)
@Import({SecurityConfig.class, InternationalizationConfig.class})
class MateriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MateriaService materiaService;

    // Os tokens dos testes já chegam prontos pelo jwt(); o decoder só existe para o contexto subir sem o Keycloak
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void listarDeveRepassarOFiltroEAPaginacaoParaOService() throws Exception {
        when(materiaService.listar(any(), any())).thenReturn(new PageImpl<>(List.of(new MateriaResponse(1L, "Cálculo"))));

        mockMvc.perform(get("/v1/materias").param("nome", "calc").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Cálculo"));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(materiaService).listar(eq("calc"), pageable.capture());
        // Sem page/size/sort na URL, valem os padrões do @PageableDefault
        assertThat(pageable.getValue().getPageSize()).isEqualTo(50);
        assertThat(pageable.getValue().getSort()).isEqualTo(Sort.by("nome"));
    }

    @Test
    void listarSemFiltroDevePassarNulo() throws Exception {
        when(materiaService.listar(any(), any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/v1/materias").with(jwt()))
                .andExpect(status().isOk());

        verify(materiaService).listar(isNull(), any(Pageable.class));
    }

    @Test
    void listarDeveSerPublicoParaOCadastro() throws Exception {
        when(materiaService.listar(any(), any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/v1/materias"))
                .andExpect(status().isOk());
    }

    @Test
    void buscarPorIdDeveRetornarAMateria() throws Exception {
        when(materiaService.buscarPorId(1L)).thenReturn(new MateriaResponse(1L, "Cálculo"));

        mockMvc.perform(get("/v1/materias/1").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Cálculo"));
    }

    @Test
    void buscarPorIdDeveResponder404QuandoAMateriaNaoExiste() throws Exception {
        when(materiaService.buscarPorId(99L)).thenThrow(new RecursoNaoEncontradoException("materia.nao-encontrada", "99"));

        mockMvc.perform(get("/v1/materias/99").with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Matéria(s) não encontrada(s): 99."));
    }

    @Test
    void criarDeveResponder201ComAMateriaCriada() throws Exception {
        when(materiaService.criar(any(MateriaRequest.class))).thenReturn(new MateriaResponse(1L, "Química"));

        mockMvc.perform(post("/v1/materias")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Química\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Química"));
    }

    @Test
    void criarDeveResponder400QuandoONomeEstaVazio() throws Exception {
        mockMvc.perform(post("/v1/materias")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.nome").exists());

        verifyNoInteractions(materiaService);
    }

    @Test
    void criarDeveResponder409QuandoONomeJaExiste() throws Exception {
        when(materiaService.criar(any(MateriaRequest.class)))
                .thenThrow(new ConflitoException("materia.nome-ja-cadastrado", "Cálculo"));

        mockMvc.perform(post("/v1/materias")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Cálculo\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Já existe uma matéria com o nome Cálculo."));
    }

    @Test
    void atualizarDeveRetornarAMateriaAtualizada() throws Exception {
        when(materiaService.atualizar(eq(1L), any(MateriaRequest.class))).thenReturn(new MateriaResponse(1L, "Cálculo I"));

        mockMvc.perform(put("/v1/materias/1")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Cálculo I\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Cálculo I"));
    }

    @Test
    void removerDeveResponder204() throws Exception {
        mockMvc.perform(delete("/v1/materias/1").with(jwt()))
                .andExpect(status().isNoContent());

        verify(materiaService).remover(1L);
    }
}
