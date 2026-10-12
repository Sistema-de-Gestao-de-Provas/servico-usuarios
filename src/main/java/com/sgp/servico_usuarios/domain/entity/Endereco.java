/*
 * SGP - servico-usuarios
 */
package com.sgp.servico_usuarios.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author diego-casemiro
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Endereco extends EntidadeBase {

    @Column(nullable = false, length = 150)
    private String rua;

    @Column(length = 10)
    private String numero;

    @Column(length = 100)
    private String complemento;

    @Column(nullable = false, length = 100)
    private String bairro;

    @Column(nullable = false, length = 100)
    private String cidade;

    @Column(nullable = false, length = 2)
    private String estado;

    @Column(nullable = false, length = 60)
    private String pais;

    @Column(nullable = false, length = 8)
    private String cep;
}
