/*
 * SGP - servico-usuarios
 */
package com.sgp.servico_usuarios.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

/**
 *
 * @author diego-casemiro
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@SoftDelete(strategy = SoftDeleteType.TIMESTAMP, columnName = "deletado_em")
public class Universidade extends EntidadeBase {

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 100)
    private String cidade;

    @Column(nullable = false, length = 2)
    private String estado;
}
