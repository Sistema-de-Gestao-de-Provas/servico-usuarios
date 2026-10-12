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
public class EnderecoRequest {

    @NotBlank
    @Size(max = 150)
    private String rua;

    @Size(max = 10)
    private String numero;

    @Size(max = 100)
    private String complemento;

    @NotBlank
    @Size(max = 100)
    private String bairro;

    @NotBlank
    @Size(max = 100)
    private String cidade;

    @NotBlank
    @Pattern(regexp = "[A-Z]{2}", message = "deve ser a sigla da UF, ex.: RJ")
    private String estado;

    @NotBlank
    @Size(max = 60)
    private String pais;

    @NotBlank
    @Pattern(regexp = "\\d{8}", message = "deve conter 8 dígitos, sem pontuação")
    private String cep;
}
