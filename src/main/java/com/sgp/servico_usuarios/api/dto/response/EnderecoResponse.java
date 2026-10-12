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
public class EnderecoResponse {

    private String rua;

    private String numero;

    private String complemento;

    private String bairro;

    private String cidade;

    private String estado;

    private String pais;

    private String cep;
}
