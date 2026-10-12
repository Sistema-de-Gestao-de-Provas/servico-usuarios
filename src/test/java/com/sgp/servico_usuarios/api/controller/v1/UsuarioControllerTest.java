/*
 * SGP - servico-usuarios
 */
package com.sgp.servico_usuarios.api.controller.v1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
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

import com.sgp.servico_usuarios.api.dto.request.UsuarioRequest;
import com.sgp.servico_usuarios.api.dto.response.UniversidadeResponse;
import com.sgp.servico_usuarios.api.dto.response.UsuarioResponse;
import com.sgp.servico_usuarios.config.internationalization.InternationalizationConfig;
import com.sgp.servico_usuarios.config.security.SecurityConfig;
import com.sgp.servico_usuarios.domain.enums.Permissao;
import com.sgp.servico_usuarios.domain.exception.ConflitoException;
import com.sgp.servico_usuarios.domain.exception.RecursoNaoEncontradoException;
import com.sgp.servico_usuarios.domain.service.UsuarioService;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 *
 * @author diego-casemiro
 */
@WebMvcTest(UsuarioController.class)
@Import({SecurityConfig.class, InternationalizationConfig.class})
class UsuarioControllerTest {

    private static final UUID KEYCLOAK_ID = UUID.randomUUID();

    private static final String USUARIO_JSON = """
            {
              "nome": "Maria Silva",
              "cpf": "12345678901",
              "email": "maria@sgp.com",
              "senha": "senha-forte",
              "universidadeId": 1,
              "materiaIds": [1, 2]
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    // Os tokens dos testes já chegam prontos pelo jwt(); o decoder só existe para o contexto subir sem o Keycloak
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void listarDeveRepassarOsFiltrosEAPaginacaoParaOService() throws Exception {
        when(usuarioService.listar(any(), any(), any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of(response())));

        mockMvc.perform(get("/v1/usuarios")
                        .param("nome", "maria")
                        .param("email", "sgp.com")
                        .param("cpf", "123.456.789-01")
                        .param("permissao", "PROFESSOR")
                        .param("universidadeId", "1")
                        .with(usuario("PROFESSOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Maria Silva"))
                .andExpect(jsonPath("$.content[0].universidade.nome").value("UFPR"));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(usuarioService).listar(eq("maria"), eq("sgp.com"), eq("123.456.789-01"), eq(Permissao.PROFESSOR), eq(1L),
                pageable.capture());
        // Sem sort na URL, vale a ordenação padrão do @PageableDefault
        assertThat(pageable.getValue().getSort()).isEqualTo(Sort.by("nome"));
    }

    @Test
    void listarSemFiltrosDevePassarNulo() throws Exception {
        when(usuarioService.listar(any(), any(), any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/v1/usuarios").with(usuario("ADMIN")))
                .andExpect(status().isOk());

        verify(usuarioService).listar(isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void listarDeveExigirAutenticacao() throws Exception {
        mockMvc.perform(get("/v1/usuarios"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void buscarAutenticadoDeveUsarOSubjectDoToken() throws Exception {
        when(usuarioService.buscarPorKeycloakId(KEYCLOAK_ID)).thenReturn(response());

        mockMvc.perform(get("/v1/usuarios/me").with(usuario("PROFESSOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("maria@sgp.com"));
    }

    @Test
    void buscarAutenticadoDeveResponder404QuandoOUsuarioNaoTemCadastro() throws Exception {
        when(usuarioService.buscarPorKeycloakId(KEYCLOAK_ID))
                .thenThrow(new RecursoNaoEncontradoException("usuario.sem-cadastro"));

        mockMvc.perform(get("/v1/usuarios/me").with(usuario("PROFESSOR")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("O usuário autenticado não possui cadastro no SGP."));
    }

    @Test
    void buscarPorIdDeveRetornarOUsuario() throws Exception {
        when(usuarioService.buscarPorId(1L)).thenReturn(response());

        mockMvc.perform(get("/v1/usuarios/1").with(usuario("PROFESSOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.permissao").value("PROFESSOR"));
    }

    @Test
    void buscarPorIdDeveResponder404QuandoOUsuarioNaoExiste() throws Exception {
        when(usuarioService.buscarPorId(99L)).thenThrow(new RecursoNaoEncontradoException("usuario.nao-encontrado", "99"));

        mockMvc.perform(get("/v1/usuarios/99").with(usuario("PROFESSOR")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Usuário 99 não encontrado."));
    }

    @Test
    void criarDeveSerPublico() throws Exception {
        when(usuarioService.criar(any(UsuarioRequest.class), eq(false))).thenReturn(response());

        mockMvc.perform(post("/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(USUARIO_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        ArgumentCaptor<UsuarioRequest> request = ArgumentCaptor.forClass(UsuarioRequest.class);
        verify(usuarioService).criar(request.capture(), eq(false));
        assertThat(request.getValue().getMateriaIds()).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void criarDeveIdentificarQuandoQuemCadastraEAdmin() throws Exception {
        when(usuarioService.criar(any(UsuarioRequest.class), anyBoolean())).thenReturn(response());

        mockMvc.perform(post("/v1/usuarios")
                        .with(usuario("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(USUARIO_JSON))
                .andExpect(status().isCreated());

        verify(usuarioService).criar(any(UsuarioRequest.class), eq(true));
    }

    @Test
    void criarNaoDeveTratarProfessorComoAdmin() throws Exception {
        when(usuarioService.criar(any(UsuarioRequest.class), anyBoolean())).thenReturn(response());

        mockMvc.perform(post("/v1/usuarios")
                        .with(usuario("PROFESSOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(USUARIO_JSON))
                .andExpect(status().isCreated());

        verify(usuarioService).criar(any(UsuarioRequest.class), eq(false));
    }

    @Test
    void criarDeveResponder400ComOsCamposInvalidos() throws Exception {
        mockMvc.perform(post("/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "", "cpf": "123", "email": "invalido", "senha": "curta"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Um ou mais campos são inválidos."))
                .andExpect(jsonPath("$.erros.nome").exists())
                .andExpect(jsonPath("$.erros.cpf").value("deve conter 11 dígitos, sem pontuação"))
                .andExpect(jsonPath("$.erros.email").exists())
                .andExpect(jsonPath("$.erros.senha").exists())
                .andExpect(jsonPath("$.erros.universidadeId").exists());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void criarDeveResponder409QuandoOEmailJaExiste() throws Exception {
        when(usuarioService.criar(any(UsuarioRequest.class), anyBoolean()))
                .thenThrow(new ConflitoException("usuario.email-ja-cadastrado", "maria@sgp.com"));

        mockMvc.perform(post("/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(USUARIO_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Já existe um usuário com o e-mail maria@sgp.com."));
    }

    @Test
    void atualizarDeveRetornarOUsuarioAtualizado() throws Exception {
        when(usuarioService.atualizar(eq(1L), any(UsuarioRequest.class))).thenReturn(response());

        mockMvc.perform(put("/v1/usuarios/1")
                        .with(usuario("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(USUARIO_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void atualizarDeveExigirAutenticacao() throws Exception {
        mockMvc.perform(put("/v1/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(USUARIO_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void removerDeveResponder204() throws Exception {
        mockMvc.perform(delete("/v1/usuarios/1").with(usuario("ADMIN")))
                .andExpect(status().isNoContent());

        verify(usuarioService).remover(1L);
    }

    private JwtRequestPostProcessor usuario(String role) {
        return jwt().jwt(jwt -> jwt.subject(KEYCLOAK_ID.toString()))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    private UsuarioResponse response() {
        return new UsuarioResponse(1L, "Maria Silva", "12345678901", "maria@sgp.com", "41999999999",
                Permissao.PROFESSOR, null, new UniversidadeResponse(1L, "UFPR", "Curitiba", "PR"), Set.of(), null, null);
    }
}
