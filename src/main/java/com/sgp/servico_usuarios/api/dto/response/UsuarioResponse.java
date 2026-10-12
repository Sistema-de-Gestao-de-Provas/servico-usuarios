/*
 * SGP - servico-usuarios
 */
package com.sgp.servico_usuarios.api.dto.response;

import com.sgp.servico_usuarios.domain.enums.Permissao;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author diego-casemiro
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse {

    private Long id;

    private String nome;

    private String cpf;

    private String email;

    private String telefone;

    private Permissao permissao;

    private EnderecoResponse endereco;

    private UniversidadeResponse universidade;

    private Set<MateriaResponse> materias;

    private LocalDateTime criadoEm;

    private LocalDateTime atualizadoEm;
}
