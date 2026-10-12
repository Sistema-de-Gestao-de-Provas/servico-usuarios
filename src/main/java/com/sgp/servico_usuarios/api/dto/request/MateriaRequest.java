/*
 * SGP - servico-usuarios
 */
package com.sgp.servico_usuarios.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class MateriaRequest {

    @NotBlank
    @Size(max = 100)
    private String nome;
}
