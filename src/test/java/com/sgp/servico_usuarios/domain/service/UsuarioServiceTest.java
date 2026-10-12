/*
 * SGP - servico-usuarios
 */
package com.sgp.servico_usuarios.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.sgp.servico_usuarios.api.dto.request.EnderecoRequest;
import com.sgp.servico_usuarios.api.dto.request.UsuarioRequest;
import com.sgp.servico_usuarios.api.dto.response.UsuarioResponse;
import com.sgp.servico_usuarios.client.keycloak.KeycloakAdminClient;
import com.sgp.servico_usuarios.config.mapper.ModelMapperConfig;
import com.sgp.servico_usuarios.domain.entity.Endereco;
import com.sgp.servico_usuarios.domain.entity.Materia;
import com.sgp.servico_usuarios.domain.entity.Universidade;
import com.sgp.servico_usuarios.domain.entity.Usuario;
import com.sgp.servico_usuarios.domain.enums.Permissao;
import com.sgp.servico_usuarios.domain.exception.AcessoNegadoException;
import com.sgp.servico_usuarios.domain.exception.ConflitoException;
import com.sgp.servico_usuarios.domain.exception.RecursoNaoEncontradoException;
import com.sgp.servico_usuarios.domain.exception.RegraNegocioException;
import com.sgp.servico_usuarios.domain.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
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
class UsuarioServiceTest {

    private static final UUID KEYCLOAK_ID = UUID.randomUUID();

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UniversidadeService universidadeService;

    @Mock
    private MateriaService materiaService;

    @Mock
    private KeycloakAdminClient keycloakAdminClient;

    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(usuarioRepository, universidadeService, materiaService, keycloakAdminClient,
                new ModelMapperConfig().modelMapper());
    }

    @Test
    void listarDeveMapearOsUsuariosDaPagina() {
        Pageable pageable = PageRequest.of(0, 20);
        when(usuarioRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(usuario(1L))));

        Page<UsuarioResponse> resultado = usuarioService.listar("maria", null, null, Permissao.PROFESSOR, 1L, pageable);

        assertThat(resultado.getContent()).hasSize(1);
        UsuarioResponse usuario = resultado.getContent().get(0);
        assertThat(usuario.getNome()).isEqualTo("Maria Silva");
        assertThat(usuario.getUniversidade().getNome()).isEqualTo("UFPR");
        assertThat(usuario.getMaterias()).extracting("nome").containsExactly("Cálculo");
    }

    @Test
    void buscarPorIdDeveRetornarOUsuario() {
        when(usuarioRepository.findByIdAndDeletadoEmIsNull(1L)).thenReturn(Optional.of(usuario(1L)));

        UsuarioResponse resultado = usuarioService.buscarPorId(1L);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getEmail()).isEqualTo("maria@sgp.com");
        assertThat(resultado.getEndereco().getCidade()).isEqualTo("Curitiba");
    }

    @Test
    void buscarPorIdDeveLancarNaoEncontradoQuandoOUsuarioNaoExiste() {
        when(usuarioRepository.findByIdAndDeletadoEmIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("usuario.nao-encontrado");
    }

    @Test
    void buscarPorKeycloakIdDeveRetornarOUsuarioDoToken() {
        when(usuarioRepository.findByKeycloakIdAndDeletadoEmIsNull(KEYCLOAK_ID)).thenReturn(Optional.of(usuario(1L)));

        assertThat(usuarioService.buscarPorKeycloakId(KEYCLOAK_ID).getId()).isEqualTo(1L);
    }

    @Test
    void buscarPorKeycloakIdDeveLancarNaoEncontradoQuandoOUsuarioNaoTemCadastro() {
        when(usuarioRepository.findByKeycloakIdAndDeletadoEmIsNull(KEYCLOAK_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarPorKeycloakId(KEYCLOAK_ID))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("usuario.sem-cadastro");
    }

    @Test
    void criarDeveCadastrarComoProfessorNoKeycloakENoBanco() {
        prepararCadastroValido();
        when(usuarioRepository.saveAndFlush(any(Usuario.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        UsuarioResponse resultado = usuarioService.criar(request("senha-forte", null), false);

        verify(keycloakAdminClient).criarUsuario("Maria Silva", "maria@sgp.com", "senha-forte");
        verify(keycloakAdminClient).atribuirRole(KEYCLOAK_ID, "PROFESSOR");
        ArgumentCaptor<Usuario> salvo = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).saveAndFlush(salvo.capture());
        assertThat(salvo.getValue().getKeycloakId()).isEqualTo(KEYCLOAK_ID);
        assertThat(salvo.getValue().getPermissao()).isEqualTo(Permissao.PROFESSOR);
        assertThat(salvo.getValue().getEndereco().getCep()).isEqualTo("80000000");
        assertThat(resultado.getPermissao()).isEqualTo(Permissao.PROFESSOR);
        assertThat(resultado.getMaterias()).extracting("nome").containsExactly("Cálculo");
    }

    @Test
    void criarDevePermitirQueOAdminDefinaAPermissao() {
        prepararCadastroValido();
        when(usuarioRepository.saveAndFlush(any(Usuario.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        UsuarioResponse resultado = usuarioService.criar(request("senha-forte", Permissao.ADMIN), true);

        verify(keycloakAdminClient).atribuirRole(KEYCLOAK_ID, "ADMIN");
        assertThat(resultado.getPermissao()).isEqualTo(Permissao.ADMIN);
    }

    @Test
    void criarDeveImpedirQueQuemNaoEAdminDefinaAPermissao() {
        assertThatThrownBy(() -> usuarioService.criar(request("senha-forte", Permissao.ADMIN), false))
                .isInstanceOf(AcessoNegadoException.class)
                .hasMessage("usuario.permissao-somente-admin");
        verifyNoInteractions(keycloakAdminClient, usuarioRepository);
    }

    @Test
    void criarDeveExigirSenha() {
        assertThatThrownBy(() -> usuarioService.criar(request("  ", null), false))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("usuario.senha-obrigatoria");
        verifyNoInteractions(keycloakAdminClient);
    }

    @Test
    void criarDeveRecusarEmailJaCadastrado() {
        when(usuarioRepository.existsByEmail("maria@sgp.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.criar(request("senha-forte", null), false))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("usuario.email-ja-cadastrado");
        verifyNoInteractions(keycloakAdminClient);
    }

    @Test
    void criarDeveRecusarCpfJaCadastrado() {
        when(usuarioRepository.existsByEmail("maria@sgp.com")).thenReturn(false);
        when(usuarioRepository.existsByCpf("12345678901")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.criar(request("senha-forte", null), false))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("usuario.cpf-ja-cadastrado");
        verifyNoInteractions(keycloakAdminClient);
    }

    @Test
    void criarNaoDeveChamarOKeycloakQuandoAUniversidadeNaoExiste() {
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCpf(anyString())).thenReturn(false);
        when(universidadeService.buscarOuFalhar(1L))
                .thenThrow(new RecursoNaoEncontradoException("universidade.nao-encontrada", "1"));

        assertThatThrownBy(() -> usuarioService.criar(request("senha-forte", null), false))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verifyNoInteractions(keycloakAdminClient);
    }

    @Test
    void criarDeveRemoverOUsuarioDoKeycloakQuandoOBancoFalha() {
        prepararCadastroValido();
        when(usuarioRepository.saveAndFlush(any(Usuario.class)))
                .thenThrow(new DataIntegrityViolationException("cpf duplicado"));

        assertThatThrownBy(() -> usuarioService.criar(request("senha-forte", null), false))
                .isInstanceOf(DataIntegrityViolationException.class);
        verify(keycloakAdminClient).removerUsuario(KEYCLOAK_ID);
    }

    @Test
    void atualizarDeveGravarNoBancoESincronizarOKeycloak() {
        Usuario usuario = usuario(1L);
        when(usuarioRepository.findByIdAndDeletadoEmIsNull(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmailAndIdNot("maria@sgp.com", 1L)).thenReturn(false);
        when(usuarioRepository.existsByCpfAndIdNot("12345678901", 1L)).thenReturn(false);
        when(universidadeService.buscarOuFalhar(1L)).thenReturn(universidade());
        when(materiaService.buscarMaterias(Set.of(1L))).thenReturn(Set.of(materia()));

        UsuarioRequest request = request("nova-senha-forte", null);
        request.setNome("Maria Souza");
        UsuarioResponse resultado = usuarioService.atualizar(1L, request);

        verify(usuarioRepository).saveAndFlush(usuario);
        verify(keycloakAdminClient).atualizarUsuario(KEYCLOAK_ID, "Maria Souza", "maria@sgp.com", "nova-senha-forte");
        assertThat(resultado.getNome()).isEqualTo("Maria Souza");
    }

    @Test
    void atualizarSemSenhaDeveManterASenhaAtualNoKeycloak() {
        when(usuarioRepository.findByIdAndDeletadoEmIsNull(1L)).thenReturn(Optional.of(usuario(1L)));
        when(universidadeService.buscarOuFalhar(1L)).thenReturn(universidade());
        when(materiaService.buscarMaterias(Set.of(1L))).thenReturn(Set.of(materia()));

        usuarioService.atualizar(1L, request("", null));

        verify(keycloakAdminClient).atualizarUsuario(eq(KEYCLOAK_ID), anyString(), anyString(), isNull());
    }

    @Test
    void atualizarDeveRecusarEmailDeOutroUsuario() {
        when(usuarioRepository.findByIdAndDeletadoEmIsNull(1L)).thenReturn(Optional.of(usuario(1L)));
        when(usuarioRepository.existsByEmailAndIdNot("maria@sgp.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.atualizar(1L, request(null, null)))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("usuario.email-ja-cadastrado");
        verify(usuarioRepository, never()).saveAndFlush(any());
        verifyNoInteractions(keycloakAdminClient);
    }

    @Test
    void atualizarDeveRecusarCpfDeOutroUsuario() {
        when(usuarioRepository.findByIdAndDeletadoEmIsNull(1L)).thenReturn(Optional.of(usuario(1L)));
        when(usuarioRepository.existsByEmailAndIdNot("maria@sgp.com", 1L)).thenReturn(false);
        when(usuarioRepository.existsByCpfAndIdNot("12345678901", 1L)).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.atualizar(1L, request(null, null)))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("usuario.cpf-ja-cadastrado");
        verify(usuarioRepository, never()).saveAndFlush(any());
        verifyNoInteractions(keycloakAdminClient);
    }

    @Test
    void removerDeveMarcarOUsuarioEOEnderecoComoDeletadosEDesativarNoKeycloak() {
        Usuario usuario = usuario(1L);
        when(usuarioRepository.findByIdAndDeletadoEmIsNull(1L)).thenReturn(Optional.of(usuario));

        usuarioService.remover(1L);

        verify(usuarioRepository).saveAndFlush(usuario);
        verify(keycloakAdminClient).desativarUsuario(KEYCLOAK_ID);
        assertThat(usuario.getDeletadoEm()).isNotNull();
        assertThat(usuario.getEndereco().getDeletadoEm()).isEqualTo(usuario.getDeletadoEm());
    }

    private void prepararCadastroValido() {
        when(usuarioRepository.existsByEmail("maria@sgp.com")).thenReturn(false);
        when(usuarioRepository.existsByCpf("12345678901")).thenReturn(false);
        when(universidadeService.buscarOuFalhar(1L)).thenReturn(universidade());
        when(materiaService.buscarMaterias(Set.of(1L))).thenReturn(Set.of(materia()));
        when(keycloakAdminClient.criarUsuario("Maria Silva", "maria@sgp.com", "senha-forte")).thenReturn(KEYCLOAK_ID);
    }

    private Usuario usuario(Long id) {
        Usuario usuario = new Usuario();
        ReflectionTestUtils.setField(usuario, "id", id);
        usuario.setKeycloakId(KEYCLOAK_ID);
        usuario.setNome("Maria Silva");
        usuario.setCpf("12345678901");
        usuario.setEmail("maria@sgp.com");
        usuario.setPermissao(Permissao.PROFESSOR);
        usuario.setUniversidade(universidade());
        usuario.getMaterias().add(materia());
        Endereco endereco = new Endereco();
        endereco.setRua("Rua XV");
        endereco.setBairro("Centro");
        endereco.setCidade("Curitiba");
        endereco.setEstado("PR");
        endereco.setPais("Brasil");
        endereco.setCep("80000000");
        usuario.setEndereco(endereco);
        return usuario;
    }

    private Universidade universidade() {
        Universidade universidade = new Universidade();
        ReflectionTestUtils.setField(universidade, "id", 1L);
        universidade.setNome("UFPR");
        universidade.setCidade("Curitiba");
        universidade.setEstado("PR");
        return universidade;
    }

    private Materia materia() {
        Materia materia = new Materia();
        ReflectionTestUtils.setField(materia, "id", 1L);
        materia.setNome("Cálculo");
        return materia;
    }

    private UsuarioRequest request(String senha, Permissao permissao) {
        EnderecoRequest endereco = new EnderecoRequest("Rua XV", "100", null, "Centro", "Curitiba", "PR", "Brasil",
                "80000000");
        return new UsuarioRequest("Maria Silva", "12345678901", "maria@sgp.com", senha, "41999999999", permissao,
                endereco, 1L, Set.of(1L));
    }
}
