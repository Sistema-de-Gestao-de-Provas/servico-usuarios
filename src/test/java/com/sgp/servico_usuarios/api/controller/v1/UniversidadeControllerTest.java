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

import com.sgp.servico_usuarios.api.dto.request.UniversidadeRequest;
import com.sgp.servico_usuarios.api.dto.response.UniversidadeResponse;
import com.sgp.servico_usuarios.config.internationalization.InternationalizationConfig;
import com.sgp.servico_usuarios.config.security.SecurityConfig;
import com.sgp.servico_usuarios.domain.exception.RecursoNaoEncontradoException;
import com.sgp.servico_usuarios.domain.service.UniversidadeService;
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
@WebMvcTest(UniversidadeController.class)
@Import({SecurityConfig.class, InternationalizationConfig.class})
class UniversidadeControllerTest {

    private static final String UNIVERSIDADE_JSON = """
            {"nome": "UFPR", "cidade": "Curitiba", "estado": "PR"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UniversidadeService universidadeService;

    // Os tokens dos testes já chegam prontos pelo jwt(); o decoder só existe para o contexto subir sem o Keycloak
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void listarDeveRepassarOsFiltrosEAPaginacaoParaOService() throws Exception {
        when(universidadeService.listar(any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of(response())));

        mockMvc.perform(get("/v1/universidades")
                        .param("nome", "federal")
                        .param("cidade", "curitiba")
                        .param("estado", "pr")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("UFPR"));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(universidadeService).listar(eq("federal"), eq("curitiba"), eq("pr"), pageable.capture());
        // Sem page/size/sort na URL, valem os padrões do @PageableDefault
        assertThat(pageable.getValue().getPageSize()).isEqualTo(50);
        assertThat(pageable.getValue().getSort()).isEqualTo(Sort.by("nome"));
    }

    @Test
    void listarSemFiltrosDevePassarNulo() throws Exception {
        when(universidadeService.listar(any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/v1/universidades").with(jwt()))
                .andExpect(status().isOk());

        verify(universidadeService).listar(isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void listarDeveExigirAutenticacao() throws Exception {
        mockMvc.perform(get("/v1/universidades"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(universidadeService);
    }

    @Test
    void buscarPorIdDeveRetornarAUniversidade() throws Exception {
        when(universidadeService.buscarPorId(1L)).thenReturn(response());

        mockMvc.perform(get("/v1/universidades/1").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cidade").value("Curitiba"));
    }

    @Test
    void buscarPorIdDeveResponder404QuandoAUniversidadeNaoExiste() throws Exception {
        when(universidadeService.buscarPorId(99L))
                .thenThrow(new RecursoNaoEncontradoException("universidade.nao-encontrada", "99"));

        mockMvc.perform(get("/v1/universidades/99").with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Universidade 99 não encontrada."));
    }

    @Test
    void criarDeveResponder201ComAUniversidadeCriada() throws Exception {
        when(universidadeService.criar(any(UniversidadeRequest.class))).thenReturn(response());

        mockMvc.perform(post("/v1/universidades")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UNIVERSIDADE_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("UFPR"));
    }

    @Test
    void criarDeveResponder400ComOsCamposInvalidos() throws Exception {
        mockMvc.perform(post("/v1/universidades")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"\", \"estado\": \"Paraná\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.nome").exists())
                .andExpect(jsonPath("$.erros.cidade").exists())
                .andExpect(jsonPath("$.erros.estado").value("deve ser a sigla da UF, ex.: RJ"));

        verifyNoInteractions(universidadeService);
    }

    @Test
    void atualizarDeveRetornarAUniversidadeAtualizada() throws Exception {
        when(universidadeService.atualizar(eq(1L), any(UniversidadeRequest.class))).thenReturn(response());

        mockMvc.perform(put("/v1/universidades/1")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UNIVERSIDADE_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void removerDeveResponder204() throws Exception {
        mockMvc.perform(delete("/v1/universidades/1").with(jwt()))
                .andExpect(status().isNoContent());

        verify(universidadeService).remover(1L);
    }

    private UniversidadeResponse response() {
        return new UniversidadeResponse(1L, "UFPR", "Curitiba", "PR");
    }
}
