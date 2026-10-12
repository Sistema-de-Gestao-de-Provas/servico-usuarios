/*
 * SGP - servico-usuarios
 */
package com.sgp.servico_usuarios.client.keycloak;

import com.sgp.servico_usuarios.config.keycloak.KeycloakProperties;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

/**
 *
 * @author diego-casemiro
 */
// Chama a Admin API do Keycloak autenticado como o service account do client servico-usuarios
@Slf4j
@Component
public class KeycloakAdminClient {

    private static final ParameterizedTypeReference<Map<String, Object>> JSON_OBJECT = new ParameterizedTypeReference<>() {
    };

    // Renova o token um pouco antes de expirar, para não usar um token vencido no meio da requisição
    private static final Duration MARGEM_RENOVACAO_TOKEN = Duration.ofSeconds(30);

    private final KeycloakProperties properties;
    private final RestClient restClient;

    private String accessToken;
    private Instant tokenExpiraEm = Instant.EPOCH;

    public KeycloakAdminClient(KeycloakProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder().baseUrl(properties.serverUrl()).build();
    }

    // Cria o usuário já com a senha definitiva e devolve o id gerado pelo Keycloak (o "sub" dos tokens).
    // O realm usa o e-mail como username, então não é preciso enviar username.
    public UUID criarUsuario(String nome, String email, String senha) {
        Map<String, Object> usuario = new HashMap<>();
        preencherNome(usuario, nome);
        usuario.put("email", email);
        usuario.put("enabled", true);
        usuario.put("emailVerified", true);
        usuario.put("credentials", List.of(credencial(senha)));

        URI location = executar(() -> restClient.post()
                .uri("/admin/realms/{realm}/users", properties.realm())
                .headers(this::autenticar)
                .contentType(MediaType.APPLICATION_JSON)
                .body(usuario)
                .retrieve()
                .onStatus(status -> status.isSameCodeAs(HttpStatus.CONFLICT), (request, response) -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um usuário com o e-mail " + email);
                })
                .toBodilessEntity()
                .getHeaders()
                .getLocation());

        // O Keycloak responde 201 com o header Location: .../users/{id}
        String path = location.getPath();
        return UUID.fromString(path.substring(path.lastIndexOf('/') + 1));
    }

    public void atribuirRole(UUID usuarioId, String role) {
        Map<String, Object> representacaoRole = executar(() -> restClient.get()
                .uri("/admin/realms/{realm}/roles/{role}", properties.realm(), role)
                .headers(this::autenticar)
                .retrieve()
                .body(JSON_OBJECT));

        executar(() -> restClient.post()
                .uri("/admin/realms/{realm}/users/{id}/role-mappings/realm", properties.realm(), usuarioId)
                .headers(this::autenticar)
                .contentType(MediaType.APPLICATION_JSON)
                .body(List.of(representacaoRole))
                .retrieve()
                .toBodilessEntity());
    }

    // Mantém nome e e-mail do Keycloak iguais aos do banco; senha nula mantém a senha atual
    public void atualizarUsuario(UUID usuarioId, String nome, String email, String senha) {
        Map<String, Object> usuario = buscarUsuario(usuarioId);
        preencherNome(usuario, nome);
        usuario.put("email", email);
        atualizar(usuarioId, usuario, email);

        if (senha != null) {
            executar(() -> restClient.put()
                    .uri("/admin/realms/{realm}/users/{id}/reset-password", properties.realm(), usuarioId)
                    .headers(this::autenticar)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(credencial(senha))
                    .retrieve()
                    .toBodilessEntity());
        }
    }

    // Usado no soft delete: o usuário continua no Keycloak, mas não consegue mais fazer login
    public void desativarUsuario(UUID usuarioId) {
        Map<String, Object> usuario = buscarUsuario(usuarioId);
        usuario.put("enabled", false);
        atualizar(usuarioId, usuario, null);
    }

    // Usado para desfazer um cadastro que falhou depois de o usuário já existir no Keycloak.
    // Não lança exceção, para não esconder o erro original do cadastro.
    public void removerUsuario(UUID usuarioId) {
        try {
            restClient.delete()
                    .uri("/admin/realms/{realm}/users/{id}", properties.realm(), usuarioId)
                    .headers(this::autenticar)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException ex) {
            log.error("Não foi possível remover o usuário {} do Keycloak; remova manualmente pelo painel", usuarioId, ex);
        }
    }

    // O PUT de usuário do Keycloak espera a representação completa; por isso a atualização parte do GET
    private Map<String, Object> buscarUsuario(UUID usuarioId) {
        return executar(() -> restClient.get()
                .uri("/admin/realms/{realm}/users/{id}", properties.realm(), usuarioId)
                .headers(this::autenticar)
                .retrieve()
                .body(JSON_OBJECT));
    }

    private void atualizar(UUID usuarioId, Map<String, Object> usuario, String email) {
        executar(() -> restClient.put()
                .uri("/admin/realms/{realm}/users/{id}", properties.realm(), usuarioId)
                .headers(this::autenticar)
                .contentType(MediaType.APPLICATION_JSON)
                .body(usuario)
                .retrieve()
                .onStatus(status -> status.isSameCodeAs(HttpStatus.CONFLICT), (request, response) -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um usuário com o e-mail " + email);
                })
                .toBodilessEntity());
    }

    // O Keycloak separa nome e sobrenome; o primeiro nome vai em firstName e o resto em lastName
    private void preencherNome(Map<String, Object> usuario, String nome) {
        String[] partes = nome.trim().split("\\s+", 2);
        usuario.put("firstName", partes[0]);
        usuario.put("lastName", partes.length > 1 ? partes[1] : partes[0]);
    }

    private Map<String, Object> credencial(String senha) {
        return Map.of("type", "password", "value", senha, "temporary", false);
    }

    private void autenticar(HttpHeaders headers) {
        headers.setBearerAuth(obterToken());
    }

    private synchronized String obterToken() {
        if (accessToken == null || Instant.now().isAfter(tokenExpiraEm.minus(MARGEM_RENOVACAO_TOKEN))) {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type", "client_credentials");
            form.add("client_id", properties.clientId());
            form.add("client_secret", properties.clientSecret());

            Map<String, Object> resposta = restClient.post()
                    .uri("/realms/{realm}/protocol/openid-connect/token", properties.realm())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(JSON_OBJECT);

            accessToken = (String) resposta.get("access_token");
            tokenExpiraEm = Instant.now().plusSeconds(((Number) resposta.get("expires_in")).longValue());
        }
        return accessToken;
    }

    // Erros de rede ou respostas inesperadas do Keycloak viram 503 para o cliente da API
    private <T> T executar(Supplier<T> chamada) {
        try {
            return chamada.get();
        } catch (RestClientException ex) {
            log.error("Erro ao chamar a Admin API do Keycloak", ex);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "O serviço de autenticação está indisponível");
        }
    }
}
