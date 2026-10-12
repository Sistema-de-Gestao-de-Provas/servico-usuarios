/*
 * SGP - servico-usuarios
 */
package com.sgp.servico_usuarios.domain.entity;

import com.sgp.servico_usuarios.domain.enums.Permissao;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
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
public class Usuario extends EntidadeBase {

    @Column(name = "keycloak_id", nullable = false, unique = true, updatable = false)
    private UUID keycloakId;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 11)
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Permissao permissao;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "endereco_id", unique = true)
    private Endereco endereco;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "universidade_id", nullable = false)
    private Universidade universidade;

    @ManyToMany
    @JoinTable(
            name = "usuario_materia",
            joinColumns = @JoinColumn(name = "usuario_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "materia_id", nullable = false)
    )
    private Set<Materia> materias = new HashSet<>();
}
