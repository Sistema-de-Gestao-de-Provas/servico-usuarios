/*
 * SGP - servico-usuarios
 */
package com.sgp.servico_usuarios.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author diego-casemiro
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MateriaResponse {

    private Long id;

    private String nome;
}
