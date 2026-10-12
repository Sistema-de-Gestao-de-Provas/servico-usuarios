/*
 * SGP - servico-usuarios
 */
package com.sgp.servico_usuarios.domain.repository.specification;

import com.sgp.servico_usuarios.domain.entity.Materia;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 *
 * @author diego-casemiro
 */
public final class MateriaSpecification {

    private MateriaSpecification() {
    }

    public static Specification<Materia> filtrar(String nome) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();
            predicados.add(cb.isNull(root.get("deletadoEm")));

            if (StringUtils.hasText(nome)) {
                predicados.add(cb.like(cb.lower(root.get("nome")), "%" + nome.trim().toLowerCase() + "%"));
            }
            return cb.and(predicados.toArray(Predicate[]::new));
        };
    }
}
