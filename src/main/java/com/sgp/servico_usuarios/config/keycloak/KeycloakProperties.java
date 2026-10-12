/*
 * SGP - servico-usuarios
 */
package com.sgp.servico_usuarios.config.keycloak;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *
 * @author diego-casemiro
 */
// Dados do client confidencial usado para chamar a Admin API do Keycloak
@ConfigurationProperties(prefix = "keycloak")
public record KeycloakProperties(String serverUrl, String realm, String clientId, String clientSecret) {
}
