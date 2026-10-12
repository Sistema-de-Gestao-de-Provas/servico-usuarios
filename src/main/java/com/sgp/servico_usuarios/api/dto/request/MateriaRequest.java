/*
 * SGP - servico-usuarios
 */
package com.sgp.servico_usuarios.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class MateriaRequest {

    @NotBlank
    @Size(max = 100)
    private String nome;
}
