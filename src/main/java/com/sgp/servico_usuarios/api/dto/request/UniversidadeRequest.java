/*
 * SGP - servico-usuarios
 */
package com.sgp.servico_usuarios.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
public class UniversidadeRequest {

    @NotBlank
    @Size(max = 150)
    private String nome;

    @NotBlank
    @Size(max = 100)
    private String cidade;

    @NotBlank
    @Pattern(regexp = "[A-Z]{2}", message = "deve ser a sigla da UF, ex.: RJ")
    private String estado;
}
