/*
 * SGP - servico-usuarios
 */
package com.sgp.servico_usuarios.domain.repository.specification;

import com.sgp.servico_usuarios.domain.entity.Universidade;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 *
 * @author diego-casemiro
 */
public final class UniversidadeSpecification {

    private UniversidadeSpecification() {
    }

    public static Specification<Universidade> filtrar(String nome, String cidade, String estado) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();
            predicados.add(cb.isNull(root.get("deletadoEm")));

            if (StringUtils.hasText(nome)) {
                predicados.add(cb.like(cb.lower(root.get("nome")), "%" + nome.trim().toLowerCase() + "%"));
            }
            if (StringUtils.hasText(cidade)) {
                predicados.add(cb.like(cb.lower(root.get("cidade")), "%" + cidade.trim().toLowerCase() + "%"));
            }
            if (StringUtils.hasText(estado)) {
                predicados.add(cb.equal(root.get("estado"), estado.trim().toUpperCase()));
            }
            return cb.and(predicados.toArray(Predicate[]::new));
        };
    }
}
