/*
 * SGP - servico-usuarios
 */
package com.sgp.servico_usuarios.api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author diego-casemiro
 */
// keycloakId e permissao não vêm no corpo: são lidos do token do usuário autenticado
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

    @Pattern(regexp = "\\d{10,11}", message = "deve conter DDD e número, sem pontuação")
    private String telefone;

    @Valid
    private EnderecoRequest endereco;

    @NotNull
    private Long universidadeId;

    private Set<Long> materiaIds;
}
