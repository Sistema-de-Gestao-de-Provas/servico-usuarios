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
public class Materia extends EntidadeBase {

    @Column(nullable = false, unique = true, length = 100)
    private String nome;
}
