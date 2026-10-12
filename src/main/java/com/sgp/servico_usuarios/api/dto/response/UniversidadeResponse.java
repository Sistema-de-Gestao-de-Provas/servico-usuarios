/*
 * SGP - servico-usuarios
 */
package com.sgp.servico_usuarios.api.dto.response;

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
public class UniversidadeResponse {

    private Long id;

    private String nome;

    private String cidade;

    private String estado;
}
