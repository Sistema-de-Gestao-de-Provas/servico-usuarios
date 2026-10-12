/*
 * SGP - servico-usuarios
 */
package com.sgp.servico_usuarios.api.dto.request;

import com.sgp.servico_usuarios.domain.enums.Permissao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 *
 * @author diego-casemiro
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequest {

    @NotBlank
    @Size(max = 150)
    private String nome;

    @NotBlank
    @Pattern(regexp = "\\d{11}", message = "deve conter 11 dígitos, sem pontuação")
    private String cpf;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    // Enviada só para o Keycloak, nunca gravada no banco do serviço.
    // Obrigatória no cadastro; na atualização, vazia mantém a senha atual
    @Size(min = 8, max = 128)
    private String senha;

    @Pattern(regexp = "\\d{10,11}", message = "deve conter DDD e número, sem pontuação")
    private String telefone;

    // Só um ADMIN pode definir, e só no cadastro; sem valor, o usuário é cadastrado como PROFESSOR
    private Permissao permissao;

    @Valid
    private EnderecoRequest endereco;

    @NotNull
    private Long universidadeId;

    private Set<Long> materiaIds;
}
